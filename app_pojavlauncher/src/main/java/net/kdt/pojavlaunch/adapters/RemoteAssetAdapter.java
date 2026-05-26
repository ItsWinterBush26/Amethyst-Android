package net.kdt.pojavlaunch.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.managers.RemoteAsset;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RemoteAssetAdapter extends RecyclerView.Adapter<RemoteAssetAdapter.ViewHolder> {
    public interface DownloadClickListener {
        void onDownloadClicked(RemoteAsset asset);
    }

    private final List<RemoteAsset> assets = new ArrayList<>();
    private final Set<String> downloading = Collections.synchronizedSet(new HashSet<>());
    private final DownloadClickListener listener;
    private final DecimalFormat sizeFormatter = new DecimalFormat("0.00");

    public RemoteAssetAdapter(DownloadClickListener listener) {
        this.listener = listener;
    }

    public void setAssets(List<RemoteAsset> newAssets) {
        assets.clear();
        if (newAssets != null) assets.addAll(newAssets);
        notifyDataSetChanged();
    }

    public void setDownloading(String assetId, boolean isDownloading) {
        if (assetId == null) return;
        if (isDownloading) downloading.add(assetId);
        else downloading.remove(assetId);
        notifyItemChanged(indexOf(assetId));
    }

    private int indexOf(String assetId) {
        if (assetId == null) return -1;
        for (int i = 0; i < assets.size(); i++) {
            if (assetId.equals(assets.get(i).id)) return i;
        }
        return -1;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_remote_asset, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(assets.get(position));
    }

    @Override
    public int getItemCount() {
        return assets.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView titleView;
        private final TextView sourceView;
        private final TextView sizeView;
        private final Button downloadButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            titleView = itemView.findViewById(R.id.remote_asset_title);
            sourceView = itemView.findViewById(R.id.remote_asset_source);
            sizeView = itemView.findViewById(R.id.remote_asset_size);
            downloadButton = itemView.findViewById(R.id.remote_asset_download_button);
        }

        public void bind(RemoteAsset asset) {
            titleView.setText(asset.name);
            sourceView.setText(asset.source.name());
            if (asset.size > 0) {
                float sizeMb = asset.size / 1024f / 1024f;
                sizeView.setText(sizeFormatter.format(sizeMb) + " MB");
            } else {
                sizeView.setText("Unknown size");
            }

            boolean isDownloading = downloading.contains(asset.id);
            downloadButton.setEnabled(!isDownloading && asset.downloadUrl != null);
            downloadButton.setText(isDownloading ? itemView.getContext().getString(R.string.status_searching) : itemView.getContext().getString(R.string.action_download));
            downloadButton.setOnClickListener(v -> {
                setDownloading(asset.id, true);
                listener.onDownloadClicked(asset);
            });
        }
    }
}
