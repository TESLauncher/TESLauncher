import me.theentropyshard.teslauncher.http.FileDownloader;
import me.theentropyshard.teslauncher.http.FileDownloaderImpl;
import me.theentropyshard.teslauncher.http.ProgressListener;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileDownloaderTest {
    public static void main(String[] args) {
        ProgressListener listener = new ProgressListener() {
            private boolean firstTime;

            @Override
            public void onProgress(long bytesRead, long contentLength, boolean done) {
                if (done) {
                    System.out.println("Completed");
                } else {
                    if (this.firstTime) {
                        this.firstTime = false;

                        if (contentLength == -1) {
                            System.out.println("Content-Length: unknown");
                        } else {
                            System.out.format("Content-Length: %d\n", contentLength);
                        }
                    }

                    System.out.println(bytesRead);

                    if (contentLength != -1) {
                        System.out.format("%d%% done\n", (100 * bytesRead) / contentLength);
                    }
                }
            }
        };
        FileDownloader downloader = new FileDownloaderImpl("TESLauncher/1.0.0", listener);

        Path savePath = Paths.get("./1.12.2.jar");
        try {
            downloader.download("https://piston-data.mojang.com/v1/objects/0f275bc1547d01fa5f56ba34bdc87d981ee12daf/client.jar", savePath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
