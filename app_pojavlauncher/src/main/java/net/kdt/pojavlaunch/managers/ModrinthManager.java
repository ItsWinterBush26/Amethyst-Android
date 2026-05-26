package net.kdt.pojavlaunch.managers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.utils.DownloadUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ModrinthManager {
    private static final String API_BASE = "https://api.modrinth.com/v2";

    public static List<RemoteAsset> search(String query, int limit) throws IOException {
        String url = API_BASE + "/search?query=" + java.net.URLEncoder.encode(query, "UTF-8") + "&limit=" + limit;
        String json = DownloadUtils.downloadString(url);
        JsonObject root = JsonParser.parseString(json).getAsJsonObject();
        JsonArray hits = root.has("hits") ? root.getAsJsonArray("hits") : new JsonArray();
        List<RemoteAsset> results = new ArrayList<>();
        for (JsonElement el : hits) {
            JsonObject obj = el.getAsJsonObject();
            String id = obj.has("project_id") ? obj.get("project_id").getAsString() : (obj.has("id") ? obj.get("id").getAsString() : null);
            if (id == null) continue;
            String title = obj.has("title") ? obj.get("title").getAsString() : (obj.has("name") ? obj.get("name").getAsString() : id);

            // Try to get latest version and file info
            try {
                String versionsJson = DownloadUtils.downloadString(API_BASE + "/project/" + id + "/version");
                JsonArray versions = JsonParser.parseString(versionsJson).getAsJsonArray();
                if (versions.size() > 0) {
                    JsonObject ver = versions.get(0).getAsJsonObject();
                    JsonArray files = ver.has("files") ? ver.getAsJsonArray("files") : new JsonArray();
                    if (files.size() > 0) {
                        JsonObject file = files.get(0).getAsJsonObject();
                        String fileUrl = file.has("url") ? file.get("url").getAsString() : null;
                        String sha1 = null;
                        if (file.has("hashes") && file.get("hashes").getAsJsonObject().has("sha1")) {
                            sha1 = file.get("hashes").getAsJsonObject().get("sha1").getAsString();
                        }
                        long size = file.has("size") ? file.get("size").getAsLong() : -1;
                        results.add(new RemoteAsset(id, title, fileUrl, sha1, size, RemoteAsset.Source.MODRINTH));
                        continue;
                    }
                }
            } catch (Exception ignored) {
            }

            // Fallback: add without download url
            results.add(new RemoteAsset(id, title, null, null, -1, RemoteAsset.Source.MODRINTH));
        }
        return results;
    }

    public static void download(RemoteAsset asset, File output, DownloadManager.ProgressListener listener) throws IOException {
        if (asset == null || asset.downloadUrl == null) throw new IllegalArgumentException("Asset or download URL is null");
        DownloadManager.downloadToFile(asset.downloadUrl, output, asset.sha1, listener);
    }
}
