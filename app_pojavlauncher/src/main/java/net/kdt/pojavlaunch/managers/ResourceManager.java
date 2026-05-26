package net.kdt.pojavlaunch.managers;

import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ResourceManager {

    private static ResourceManager sInstance;
    private String resourceDir;

    public static ResourceManager getInstance() {
        if (sInstance == null) sInstance = new ResourceManager();
        return sInstance;
    }

    public void init(android.content.Context ctx) {
        resourceDir = Tools.DIR_GAME_NEW + "/resourcepacks";
    }

    public String getResourceDir(){ return resourceDir; }

    public static final String RESOURCEPACKS_DIR = Tools.DIR_GAME_NEW + "/resourcepacks";

    public static List<RemoteAsset> search(String query, int limit) throws IOException {
        // Prefer Modrinth for modern packs; fall back to CurseForge if needed
        List<RemoteAsset> result = new ArrayList<>();
        try {
            result.addAll(ModrinthManager.search(query, limit));
        } catch (Exception e) {
            // ignore
        }
        // CurseForge integration is optional; its search is currently a stub.
        return result;
    }

    public static void install(ResourceInstallRequest req) throws IOException {
        if (req.asset.downloadUrl == null) throw new IllegalArgumentException("Asset download URL is null");
        File targetDir = new File(RESOURCEPACKS_DIR);
        FileUtils.ensureDirectory(targetDir);
        File out = new File(targetDir, req.asset.name.replaceAll("\\\\s+","_") + ".zip");
        DownloadManager.downloadToFile(req.asset.downloadUrl, out, req.asset.sha1, req.listener);
    }

    public static class ResourceInstallRequest {
        public final RemoteAsset asset;
        public final DownloadManager.ProgressListener listener;

        public ResourceInstallRequest(RemoteAsset asset, DownloadManager.ProgressListener listener) {
            this.asset = asset;
            this.listener = listener;
        }
    }
}
