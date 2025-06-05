package com.filesystem;

import java.util.*;

public class Directory {
        private String name;
        private List<Directory> subDirectories = new ArrayList<>();
        private List<FSFile> files = new ArrayList<>();

        public Directory(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<Directory> getSubDirectories() {
            return subDirectories;
        }

        public List<FSFile> getFiles() {
            return files;
        }

        public boolean hasSubDirectory(String name) {
            return subDirectories.stream().anyMatch(directory -> directory.getName().equals(name));
        }

        public boolean hasFile(String name) {
            return files.stream().anyMatch(file -> file.getName().equals(name));
        }

        @Override
        public String toString() {
            return "Diretório: " + name;
        }
    }
