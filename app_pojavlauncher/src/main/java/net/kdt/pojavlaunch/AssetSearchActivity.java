package net.kdt.pojavlaunch;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import net.kdt.pojavlaunch.adapters.RemoteAssetAdapter;
import net.kdt.pojavlaunch.managers.CurseForgeManager;
import net.kdt.pojavlaunch.managers.ModManager;
import net.kdt.pojavlaunch.managers.ModrinthManager;
import net.kdt.pojavlaunch.managers.RemoteAsset;
import net.kdt.pojavlaunch.managers.ResourceManager;
import net.kdt.pojavlaunch.managers.ShaderManager;
import net.kdt.pojavlaunch.managers.WorldManager;
import net.kdt.pojavlaunch.managers.DownloadManager;
import net.kdt.pojavlaunch.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AssetSearchActivity extends AppCompatActivity {
    private EditText queryEditText;
    private Spinner sourceSpinner;
    private Spinner typeSpinner;
    private Button searchButton;
    private ProgressBar progressBar;
    private TextView statusText;
    private RemoteAssetAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asset_search);

        queryEditText = findViewById(R.id.asset_search_query);
        sourceSpinner = findViewById(R.id.asset_search_source_spinner);
        typeSpinner = findViewById(R.id.asset_search_type_spinner);
        searchButton = findViewById(R.id.asset_search_button);
        progressBar = findViewById(R.id.asset_search_progress);
        statusText = findViewById(R.id.asset_search_status);

        adapter = new RemoteAssetAdapter(this::onDownloadRequested);
        RecyclerView recyclerView = findViewById(R.id.asset_search_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        sourceSpinner.setAdapter(ArrayAdapter.createFromResource(this,
                R.array.asset_search_sources, android.R.layout.simple_spinner_item));
        typeSpinner.setAdapter(ArrayAdapter.createFromResource(this,
                R.array.asset_search_types, android.R.layout.simple_spinner_item));

        searchButton.setOnClickListener(v -> searchAssets());
    }

    private void searchAssets() {
        String query = queryEditText.getText().toString().trim();
        if (query.isEmpty()) {
            statusText.setText(R.string.status_no_query);
            return;
        }
        statusText.setText(R.string.status_searching);
        progressBar.setVisibility(View.VISIBLE);
        adapter.setAssets(new ArrayList<>());

        new Thread(() -> {
            try {
                List<RemoteAsset> results = performSearch(query);
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    if (results.isEmpty()) {
                        statusText.setText(R.string.search_modpack_no_result);
                    } else {
                        statusText.setText(getString(R.string.search_assets_title) + ": " + results.size());
                    }
                    adapter.setAssets(results);
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    statusText.setText(getString(R.string.status_download_failed) + ": " + e.getMessage());
                });
            }
        }).start();
    }

    private List<RemoteAsset> performSearch(String query) throws IOException {
        String sourceLabel = sourceSpinner.getSelectedItem().toString();
        String typeLabel = typeSpinner.getSelectedItem().toString();
        AssetSource source = AssetSource.fromLabel(sourceLabel);
        AssetType type = AssetType.fromLabel(typeLabel);

        List<RemoteAsset> results;
        if (source == AssetSource.CURSEFORGE) {
            CurseForgeManager curseForgeManager = new CurseForgeManager(getString(R.string.curseforge_api_key));
            results = curseForgeManager.search(query, 50);
        } else {
            results = ModrinthManager.search(query, 50);
        }
        return filterByType(results, type);
    }

    private List<RemoteAsset> filterByType(List<RemoteAsset> candidates, AssetType type) {
        if (type == AssetType.MODS) return candidates;
        List<RemoteAsset> filtered = new ArrayList<>();
        String predicate = type.getQueryHint();
        for (RemoteAsset asset : candidates) {
            String text = (asset.name + " " + (asset.downloadUrl != null ? asset.downloadUrl : "")).toLowerCase();
            if (text.contains(predicate)) filtered.add(asset);
        }
        return filtered.isEmpty() ? candidates : filtered;
    }

    private void onDownloadRequested(RemoteAsset asset) {
        AssetType type = AssetType.fromLabel(typeSpinner.getSelectedItem().toString());
        runOnUiThread(() -> statusText.setText(getString(R.string.status_download_started)));
        new Thread(() -> {
            try {
                installAsset(asset, type);
                runOnUiThread(() -> {
                    statusText.setText(getString(R.string.status_download_completed));
                    adapter.setDownloading(asset.id, false);
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    statusText.setText(getString(R.string.status_download_failed) + ": " + e.getMessage());
                    adapter.setDownloading(asset.id, false);
                });
            }
        }).start();
    }

    private void installAsset(RemoteAsset asset, AssetType type) throws IOException {
        switch (type) {
            case MODS:
                ModManager.install(asset, createProgressListener());
                break;
            case RESOURCES:
                ResourceManager.install(new ResourceManager.ResourceInstallRequest(asset, createProgressListener()));
                break;
            case SHADERS:
                ShaderManager.install(asset, createProgressListener());
                break;
            case WORLDS:
                WorldManager.install(asset, createProgressListener());
                break;
        }
    }

    private DownloadManager.ProgressListener createProgressListener() {
        return (downloaded, total) -> runOnUiThread(() -> {
            if (total <= 0) {
                statusText.setText(getString(R.string.status_download_started) + " " + downloaded + " bytes");
            } else {
                statusText.setText(getString(R.string.status_download_started) + " " + downloaded + "/" + total);
            }
        });
    }

    private enum AssetSource {
        MODRINTH("Modrinth"),
        CURSEFORGE("CurseForge");

        final String label;

        AssetSource(String label) {
            this.label = label;
        }

        static AssetSource fromLabel(String label) {
            if (label == null) return MODRINTH;
            for (AssetSource source : values()) {
                if (source.label.equalsIgnoreCase(label)) return source;
            }
            return MODRINTH;
        }
    }

    private enum AssetType {
        MODS("Mods", "mod"),
        RESOURCES("Resource Packs", "resource"),
        SHADERS("Shaders", "shader"),
        WORLDS("Worlds", "world");

        final String label;
        final String queryHint;

        AssetType(String label, String queryHint) {
            this.label = label;
            this.queryHint = queryHint;
        }

        String getQueryHint() {
            return queryHint;
        }

        static AssetType fromLabel(String label) {
            if (label == null) return MODS;
            for (AssetType type : values()) {
                if (type.label.equalsIgnoreCase(label)) return type;
            }
            return MODS;
        }
    }
}
