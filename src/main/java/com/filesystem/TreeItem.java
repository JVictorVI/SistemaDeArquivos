package com.filesystem;

public class TreeItem {
    private final String name;
    private final boolean isDirectory;

    public TreeItem(String name, boolean isDirectory) {
        this.name = name;
        this.isDirectory = isDirectory;
    }

    public String getName() {
        return name;
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    @Override
    public String toString() {
        return name;
    }
}
