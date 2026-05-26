package net.kdt.pojavlaunch.managers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ModManager {
    private static ModManager sInstance;
    private String modsDir;

    public static ModManager getInstance() {
        if (sInstance == null) sInstance = new ModManager();
        return sInstance;
    }

    public void init(android.content.Context ctx) {
        modsDir = Tools.DIR_GAME_NEW + "/mods";
    }

    public static final String MODS_DIR = Tools.DIR_GAME_NEW + "/mods";

    public static List<RemoteAsset> search(String query, int limit) throws IOException {
        List<RemoteAsset> results = new ArrayList<>();
        try {
            results.addAll(ModrinthManager.search(query, limit));
        } catch (Exception ignored) {
        }
        return results;
    }

    public static void install(RemoteAsset asset, DownloadManager.ProgressListener listener) throws IOException {
        if (asset.downloadUrl == null) throw new IllegalArgumentException("Asset download URL is null");
        File modsDir = new File(MODS_DIR);
        net.kdt.pojavlaunch.utils.FileUtils.ensureDirectory(modsDir);
        File out = new File(modsDir, asset.name.replaceAll("\\\\s+","_") + ".jar");
        DownloadManager.downloadToFile(asset.downloadUrl, out, asset.sha1, listener);
    }
}
