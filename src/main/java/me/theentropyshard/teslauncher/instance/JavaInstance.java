package me.theentropyshard.teslauncher.instance;

import java.util.Set;

public abstract class JavaInstance extends Instance {
    private String javaPath;
    private int minimumMemoryMegabytes;
    private int maximumMemoryMegabytes;
    private Set<String> customJvmFlags;

    public JavaInstance() {

    }

    public String getJavaPath() {
        return this.javaPath;
    }

    public void setJavaPath(String javaPath) {
        this.javaPath = javaPath;
    }

    public int getMinimumMemoryMegabytes() {
        return this.minimumMemoryMegabytes;
    }

    public void setMinimumMemoryMegabytes(int minimumMemoryMegabytes) {
        this.minimumMemoryMegabytes = minimumMemoryMegabytes;
    }

    public int getMaximumMemoryMegabytes() {
        return this.maximumMemoryMegabytes;
    }

    public void setMaximumMemoryMegabytes(int maximumMemoryMegabytes) {
        this.maximumMemoryMegabytes = maximumMemoryMegabytes;
    }

    public Set<String> getCustomJvmFlags() {
        return this.customJvmFlags;
    }

    public void setCustomJvmFlags(Set<String> customJvmFlags) {
        this.customJvmFlags = customJvmFlags;
    }
}
