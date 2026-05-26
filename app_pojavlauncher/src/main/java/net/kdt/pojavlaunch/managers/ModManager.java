package net.kdt.pojavlaunch.managers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ModManager {
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
        File modsDir = new File(MODS_DIR);
        net.kdt.pojavlaunch.utils.FileUtils.ensureDirectory(modsDir);
        File out = new File(modsDir, asset.name.replaceAll("\\s+","_") + ".jar");
        ModrinthManager.download(asset, out, listener);
    }
}
