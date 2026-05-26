package net.kdt.pojavlaunch.examples;

import android.util.Log;

import net.kdt.pojavlaunch.managers.ModManager;
import net.kdt.pojavlaunch.managers.ModrinthManager;
import net.kdt.pojavlaunch.managers.RemoteAsset;
import net.kdt.pojavlaunch.managers.ResourceManager;
import net.kdt.pojavlaunch.managers.ShaderManager;
import net.kdt.pojavlaunch.managers.WorldManager;

import java.io.File;
import java.util.List;

public class ExampleManagers {
    private static final String TAG = "ExampleManagers";

    public static void runModrinthExample() {
        try {
            List<RemoteAsset> list = ModrinthManager.search("iris", 5);
            for (RemoteAsset a : list) {
                Log.i(TAG, "Found: " + a.name + " (" + a.id + ") url=" + a.downloadUrl);
                if (a.downloadUrl != null) {
                    File out = new File("/sdcard/Download/" + a.name.replaceAll("\\s+", "_") + ".jar");
                    ModrinthManager.download(a, out, (downloaded, total) -> {
                        Log.i(TAG, "Progress: " + downloaded + "/" + total);
                    });
                    Log.i(TAG, "Downloaded to " + out.getAbsolutePath());
                    break;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Example failed", e);
        }
    }

    public static void runAllExamples() {
        try {
            List<RemoteAsset> res = ResourceManager.search("vanilla", 3);
            if (!res.isEmpty()) {
                ResourceManager.install(new ResourceManager.ResourceInstallRequest(res.get(0), (d, t) -> Log.i(TAG, "Resource progress " + d + "/" + t)));
            }

            List<RemoteAsset> shaders = ShaderManager.search("sildur", 3);
            if (!shaders.isEmpty()) {
                ShaderManager.install(shaders.get(0), (d, t) -> Log.i(TAG, "Shader progress " + d + "/" + t));
            }

            List<RemoteAsset> worlds = WorldManager.search("scenic", 3);
            if (!worlds.isEmpty()) {
                WorldManager.install(worlds.get(0), (d, t) -> Log.i(TAG, "World progress " + d + "/" + t));
            }
        } catch (Exception e) {
            Log.e(TAG, "runAllExamples failed", e);
        }
    }

    public static void runModExample() {
        try {
            List<RemoteAsset> mods = ModManager.search("fabric", 3);
            if (!mods.isEmpty()) {
                ModManager.install(mods.get(0), (d, t) -> Log.i(TAG, "Mod progress " + d + "/" + t));
            }
        } catch (Exception e) {
            Log.e(TAG, "runModExample failed", e);
        }
    }
}
