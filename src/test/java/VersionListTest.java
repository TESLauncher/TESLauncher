import me.theentropyshard.teslauncher.version.VersionType;
import me.theentropyshard.teslauncher.version.list.LocalVersionList;
import me.theentropyshard.teslauncher.version.list.RemoteVersionList;
import me.theentropyshard.teslauncher.version.list.VersionList;
import me.theentropyshard.teslauncher.version.model.VersionInfo;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class VersionListTest {
    private static final String VERSION_MANIFEST_V2 = "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json";
    private static final Path JSON_FILE = Paths.get("version_manifest_v2.json");

    public static void main(String[] args) throws IOException {
        VersionList versionList = getVersionList(false);
        System.out.println("Loading versions...");
        versionList.load();
        System.out.println("Latest Minecraft release: " + versionList.getLatestVersion(VersionType.RELEASE));
        System.out.println("Latest Minecraft snapshot: " + versionList.getLatestVersion(VersionType.SNAPSHOT));
        System.out.println("Latest Minecraft Beta: " + versionList.getLatestVersion(VersionType.OLD_BETA));
        System.out.println("Latest Minecraft Alpha: " + versionList.getLatestVersion(VersionType.OLD_ALPHA));
        System.out.println("Versions:");
        for (VersionInfo version : versionList.getVersions()) {
            System.out.println(version);
        }
    }

    private static VersionList getVersionList(boolean remote) {
        return remote ? new RemoteVersionList(VersionListTest.VERSION_MANIFEST_V2) :
                new LocalVersionList(VersionListTest.JSON_FILE);
    }
}
