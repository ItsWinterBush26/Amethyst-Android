package net.kdt.pojavlaunch;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import net.kdt.pojavlaunch.managers.ModManager;
import net.kdt.pojavlaunch.managers.ResourceManager;
import net.kdt.pojavlaunch.managers.ShaderManager;
import net.kdt.pojavlaunch.managers.WorldManager;
import net.kdt.pojavlaunch.examples.ExampleManagers;
import net.kdt.pojavlaunch.browser.AssetSearchActivity;

public class ManagersActivity extends AppCompatActivity {
    private TextView statusView;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_managers);

        statusView = findViewById(R.id.manager_status_view);
        progressBar = findViewById(R.id.manager_progress_bar);

        Button modButton = findViewById(R.id.button_run_mod_example);
        Button resourceButton = findViewById(R.id.button_run_resource_example);
        Button shaderButton = findViewById(R.id.button_run_shader_example);
        Button worldButton = findViewById(R.id.button_run_world_example);
        Button allButton = findViewById(R.id.button_run_all_examples);
        Button browserButton = findViewById(R.id.button_open_asset_browser);

        modButton.setOnClickListener(v -> runTask("Running mod example", this::runModExample));
        resourceButton.setOnClickListener(v -> runTask("Running resource example", this::runResourceExample));
        shaderButton.setOnClickListener(v -> runTask("Running shader example", this::runShaderExample));
        worldButton.setOnClickListener(v -> runTask("Running world example", this::runWorldExample));
        allButton.setOnClickListener(v -> runTask("Running all manager examples", ExampleManagers::runAllExamples));
        browserButton.setOnClickListener(v -> startActivity(new Intent(this, AssetSearchActivity.class)));
    }

    private void runTask(String description, Runnable task) {
        statusView.setText(description + "...\n");
        progressBar.setVisibility(View.VISIBLE);
        new Thread(() -> {
            try {
                task.run();
                runOnUiThread(() -> statusView.append("Finished: " + description + "\n"));
            } catch (Exception e) {
                runOnUiThread(() -> statusView.append("Error: " + e.getMessage() + "\n"));
            } finally {
                runOnUiThread(() -> progressBar.setVisibility(View.GONE));
            }
        }).start();
    }

    private void runModExample() {
        try {
            if (Tools.DIR_GAME_NEW == null) {
                runOnUiThread(() -> statusView.append(getString(R.string.status_no_storage) + "\n"));
                return;
            }
            ExampleManagers.runModExample();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void runResourceExample() {
        try {
            if (Tools.DIR_GAME_NEW == null) {
                runOnUiThread(() -> statusView.append(getString(R.string.status_no_storage) + "\n"));
                return;
            }
            ResourceManager.install(ResourceManager.search("vanilla", 1).get(0), (d, t) -> runOnUiThread(() -> statusView.append("Resource progress " + d + "/" + t + "\n")));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void runShaderExample() {
        try {
            if (Tools.DIR_GAME_NEW == null) {
                runOnUiThread(() -> statusView.append(getString(R.string.status_no_storage) + "\n"));
                return;
            }
            ShaderManager.install(ShaderManager.search("sildur", 1).get(0), (d, t) -> runOnUiThread(() -> statusView.append("Shader progress " + d + "/" + t + "\n")));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void runWorldExample() {
        try {
            if (Tools.DIR_GAME_NEW == null) {
                runOnUiThread(() -> statusView.append(getString(R.string.status_no_storage) + "\n"));
                return;
            }
            WorldManager.install(WorldManager.search("scenic", 1).get(0), (d, t) -> runOnUiThread(() -> statusView.append("World progress " + d + "/" + t + "\n")));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
