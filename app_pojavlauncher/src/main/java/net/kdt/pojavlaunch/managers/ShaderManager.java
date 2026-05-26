package net.kdt.pojavlaunch.managers;

import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ShaderManager {
    private static ShaderManager sInstance;
    private String shaderDir;

    public static ShaderManager getInstance() {
        if (sInstance == null) sInstance = new ShaderManager();
        return sInstance;
    }

    public void init(android.content.Context ctx) {
        shaderDir = Tools.DIR_GAME_NEW + "/shaderpacks";
    }

    public String getShaderDir(){ return shaderDir; }

    public static final String SHADERPACKS_DIR = Tools.DIR_GAME_NEW + "/shaderpacks";

    public static List<RemoteAsset> search(String query, int limit) throws IOException {
        List<RemoteAsset> result = new ArrayList<>();
        try {
            result.addAll(ModrinthManager.search(query, limit));
        } catch (Exception ignored) {
        }
        return result;
    }

    public static void install(RemoteAsset asset, DownloadManager.ProgressListener listener) throws IOException {
        File targetDir = new File(SHADERPACKS_DIR);
        FileUtils.ensureDirectory(targetDir);
        File out = new File(targetDir, asset.name.replaceAll("\\\\s+","_") + ".zip");
        ModrinthManager.download(asset, out, listener);
    }
}
