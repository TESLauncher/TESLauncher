package me.theentropyshard.teslauncher.instance;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public interface InstanceManager {
    void load() throws IOException;

    void reload() throws IOException;

    void save(Instance instance) throws IOException;

    void createInstance(String name, String groupName, String minecraftVersion) throws IOException;

    void removeInstance(String name) throws IOException;

    boolean instanceExists(String name);

    Path getInstanceDir(Instance instance);

    Path getMinecraftDir(Instance instance);

    Instance getInstanceByName(String name);

    List<Instance> getInstances();
}
