package net.kdt.pojavlaunch.managers;

import androidx.annotation.Nullable;

public class RemoteAsset {
    public enum Source {
        MODRINTH, CURSEFORGE, GENERIC
    }

    public final String id;
    public final String name;
    @Nullable
    public final String downloadUrl;
    @Nullable
    public final String sha1;
    public final long size;
    public final Source source;

    public RemoteAsset(String id, String name, @Nullable String downloadUrl, @Nullable String sha1, long size, Source source) {
        this.id = id;
        this.name = name;
        this.downloadUrl = downloadUrl;
        this.sha1 = sha1;
        this.size = size;
        this.source = source;
    }
}
