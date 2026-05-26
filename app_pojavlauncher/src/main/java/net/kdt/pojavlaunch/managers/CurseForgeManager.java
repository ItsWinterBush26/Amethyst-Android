package net.kdt.pojavlaunch.managers;

import net.kdt.pojavlaunch.utils.DownloadUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Lightweight CurseForge manager. Requires an API key to be set before making requests.
 * See: https://docs.curseforge.com/
 */
public class CurseForgeManager {
    private static final String API_BASE = "https://api.curseforge.com/v1";
    private String apiKey;

    public CurseForgeManager() {
    }

    public void setApiKey(String key) {
        this.apiKey = key;
    }

    public List<RemoteAsset> search(String query, int pageSize) throws IOException {
        // Placeholder implementation. CurseForge API requires API key and more complex parsing.
        // We'll return an empty list to keep caller safe.
        return new ArrayList<>();
    }

    public void download(RemoteAsset asset, File output, DownloadManager.ProgressListener listener) throws IOException {
        if (asset == null || asset.downloadUrl == null) throw new IllegalArgumentException("Asset or download URL is null");
        DownloadManager.downloadToFile(asset.downloadUrl, output, asset.sha1, listener);
    }
}
