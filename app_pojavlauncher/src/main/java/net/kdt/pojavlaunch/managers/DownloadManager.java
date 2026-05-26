package net.kdt.pojavlaunch.managers;

import androidx.annotation.Nullable;

import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.utils.DownloadUtils;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;

public class DownloadManager {

    public interface ProgressListener {
        void onProgress(int downloaded, int total);
    }

    public static void downloadToFile(String url, File output, @Nullable String sha1, @Nullable ProgressListener listener) throws IOException {
        Callable<Void> task = () -> {
            if(listener != null) {
                DownloadUtils.downloadFileMonitored(url, output, null, (overall, length) -> {
                    int l = length <= 0 ? -1 : length;
                    listener.onProgress(overall, l);
                });
            } else {
                DownloadUtils.downloadFile(url, output);
            }
            return null;
        };
        DownloadUtils.ensureSha1(output, sha1, task);
    }
}
