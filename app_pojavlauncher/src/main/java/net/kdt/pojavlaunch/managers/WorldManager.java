package net.kdt.pojavlaunch.managers;

import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.utils.FileUtils;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class WorldManager {
    private static WorldManager sInstance;
    private String savesDir;

    public static WorldManager getInstance() {
        if (sInstance == null) sInstance = new WorldManager();
        return sInstance;
    }

    public void init(android.content.Context ctx) {
        savesDir = Tools.DIR_GAME_NEW + "/saves";
    }

    public String getSavesDir(){ return savesDir; }

    public static final String SAVES_DIR = Tools.DIR_GAME_NEW + "/saves";

    public static List<RemoteAsset> search(String query, int limit) throws IOException {
        List<RemoteAsset> result = new ArrayList<>();
        try {
            result.addAll(ModrinthManager.search(query, limit));
        } catch (Exception ignored) {
        }
        return result;
    }

    public static void install(RemoteAsset asset, DownloadManager.ProgressListener listener) throws IOException {
        File saves = new File(SAVES_DIR);
        FileUtils.ensureDirectory(saves);

        File tmp = File.createTempFile("world_", ".tmp");
        try {
            if (asset.downloadUrl == null) throw new IllegalArgumentException("Asset download URL is null");
            DownloadManager.downloadToFile(asset.downloadUrl, tmp, asset.sha1, listener);
            // If it is a zip, extract it
            if (isZipFile(tmp)) {
                unzipTo(tmp, saves);
            } else {
                File dest = new File(saves, asset.name.replaceAll("\\\\s+","_"));
                Files.move(tmp.toPath(), dest.toPath());
            }
        } finally {
            if (tmp.exists()) tmp.delete();
        }
    }

    private static boolean isZipFile(File f) {
        try (DataInputStream in = new DataInputStream(new FileInputStream(f))) {
            int magic = in.readInt();
            return magic == 0x504B0304; // PK\x03\x04
        } catch (IOException e) {
            return false;
        }
    }

    private static void unzipTo(File zipFile, File targetDir) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry entry;
            byte[] buffer = new byte[8192];
            while ((entry = zis.getNextEntry()) != null) {
                File outFile = new File(targetDir, entry.getName());
                if (entry.isDirectory()) {
                    outFile.mkdirs();
                } else {
                    File parent = outFile.getParentFile();
                    if (!parent.exists()) parent.mkdirs();
                    try (FileOutputStream fos = new FileOutputStream(outFile)) {
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }
                }
                zis.closeEntry();
            }
        }
    }
}
