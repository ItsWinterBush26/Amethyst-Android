package net.kdt.pojavlaunch.managers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.kdt.pojavlaunch.modloaders.modpacks.api.ApiHandler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CurseForgeManager {
    private static final int CURSEFORGE_MINECRAFT_GAME_ID = 432;
    private static final int CURSEFORGE_MOD_CLASS_ID = 6;
    private static final int CURSEFORGE_SORT_RELEVANCY = 1;

    private final ApiHandler apiHandler;

    public CurseForgeManager(String apiKey) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalArgumentException("CurseForge API key is required");
        }
        apiHandler = new ApiHandler("https://api.curseforge.com/v1", apiKey);
    }

    public List<RemoteAsset> search(String query, int limit) throws IOException {
        HashMap<String, Object> params = new HashMap<>();
        params.put("gameId", CURSEFORGE_MINECRAFT_GAME_ID);
        params.put("classId", CURSEFORGE_MOD_CLASS_ID);
        params.put("searchFilter", query);
        params.put("sortField", CURSEFORGE_SORT_RELEVANCY);
        params.put("sortOrder", "desc");
        params.put("pageSize", limit);

        JsonObject response = apiHandler.get("mods/search", params, JsonObject.class);
        if (response == null) return new ArrayList<>();

        JsonArray dataArray = response.getAsJsonArray("data");
        if (dataArray == null) return new ArrayList<>();

        List<RemoteAsset> result = new ArrayList<>(dataArray.size());
        for (JsonElement element : dataArray) {
            JsonObject project = element.getAsJsonObject();
            if (project == null) continue;

            JsonElement allowModDistribution = project.get("allowModDistribution");
            if (allowModDistribution != null && !allowModDistribution.isJsonNull() && !allowModDistribution.getAsBoolean()) {
                continue;
            }

            long projectId = project.get("id").getAsLong();
            String name = project.has("name") ? project.get("name").getAsString() : String.valueOf(projectId);

            FileInfo fileInfo = getLatestFileInfo(projectId);
            if (fileInfo == null || fileInfo.downloadUrl == null) continue;

            result.add(new RemoteAsset(
                    String.valueOf(projectId),
                    name,
                    fileInfo.downloadUrl,
                    fileInfo.sha1,
                    fileInfo.size,
                    RemoteAsset.Source.CURSEFORGE));
        }

        return result;
    }

    public void download(RemoteAsset asset, File output, DownloadManager.ProgressListener listener) throws IOException {
        if (asset == null) throw new IllegalArgumentException("Asset cannot be null");
        if (asset.downloadUrl == null) {
            if (asset.id == null) throw new IllegalArgumentException("CurseForge project id required");
            FileInfo fileInfo = getLatestFileInfo(Long.parseLong(asset.id));
            if (fileInfo == null || fileInfo.downloadUrl == null) {
                throw new IOException("Unable to obtain a download URL for CurseForge asset " + asset.id);
            }
            asset = new RemoteAsset(asset.id, asset.name, fileInfo.downloadUrl, fileInfo.sha1, fileInfo.size, asset.source);
        }
        DownloadManager.downloadToFile(asset.downloadUrl, output, asset.sha1, listener);
    }

    private FileInfo getLatestFileInfo(long projectId) throws IOException {
        HashMap<String, Object> params = new HashMap<>();
        params.put("index", 0);
        params.put("pageSize", 1);

        JsonObject response = apiHandler.get("mods/" + projectId + "/files", params, JsonObject.class);
        if (response == null) return null;

        JsonArray dataArray = response.getAsJsonArray("data");
        if (dataArray == null || dataArray.size() == 0) return null;

        JsonObject latestFile = dataArray.get(0).getAsJsonObject();
        if (latestFile == null) return null;

        long fileId = latestFile.get("id").getAsLong();
        long size = latestFile.has("fileLength") ? latestFile.get("fileLength").getAsLong() : -1;
        String downloadUrl = getDownloadUrl(projectId, fileId);
        String sha1 = getSha1FromFileInfo(latestFile);

        if (downloadUrl == null) return null;
        return new FileInfo(downloadUrl, sha1, size);
    }

    private String getDownloadUrl(long projectId, long fileId) {
        JsonObject response = apiHandler.get("mods/" + projectId + "/files/" + fileId + "/download-url", JsonObject.class);
        if (response != null && response.has("data") && !response.get("data").isJsonNull()) {
            return response.get("data").getAsString();
        }

        JsonObject fallback = apiHandler.get("mods/" + projectId + "/files/" + fileId, JsonObject.class);
        if (fallback != null && fallback.has("data") && !fallback.get("data").isJsonNull()) {
            JsonObject data = fallback.getAsJsonObject("data");
            long id = data.get("id").getAsLong();
            String fileName = data.has("fileName") ? data.get("fileName").getAsString() : null;
            if (fileName != null) {
                return String.format("https://edge.forgecdn.net/files/%s/%s/%s", id / 1000, id % 1000, fileName);
            }
        }

        return null;
    }

    private String getSha1FromFileInfo(JsonObject fileInfo) {
        if (fileInfo == null || !fileInfo.has("hashes")) return null;
        JsonArray hashes = fileInfo.getAsJsonArray("hashes");
        if (hashes == null) return null;
        for (JsonElement hashElement : hashes) {
            JsonObject hashObject = hashElement.getAsJsonObject();
            if (hashObject == null) continue;
            if (hashObject.has("algo") && hashObject.get("algo").getAsInt() == 1) {
                return hashObject.has("value") ? hashObject.get("value").getAsString() : null;
            }
        }
        return null;
    }

    private static class FileInfo {
        final String downloadUrl;
        final String sha1;
        final long size;

        FileInfo(String downloadUrl, String sha1, long size) {
            this.downloadUrl = downloadUrl;
            this.sha1 = sha1;
            this.size = size;
        }
    }
}
