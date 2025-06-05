package com.filesystem;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class FileSystemSimulator {

    private final Directory root;
    private final Journal journal;
    private static final String BASE_FILE = "src/main/java/com/filesystem/base.txt";

    public FileSystemSimulator() {
        root = new Directory("root");
        journal = new Journal();
        try {
            carregarBase();
        } catch (IOException e) {
            System.out.println("Não foi possível carregar o base.txt. Iniciando sistema vazio.");
        }
    }

    private static void criarBaseFileSeNaoExistir() throws IOException {
        File baseFile = new File(BASE_FILE);
        File parentDir = baseFile.getParentFile();

        if (!parentDir.exists()) {
            boolean created = parentDir.mkdirs();
            if (!created) {
                System.out.println("Erro ao criar diretórios pai para base.txt");
                return;
            }
        }

        if (!baseFile.exists()) {
            boolean created = baseFile.createNewFile();
            if (created) {
                System.out.println("Arquivo base.txt criado em: " + baseFile.getAbsolutePath());
            } else {
                System.out.println("Erro ao criar base.txt");
            }
        }
    }

    private void salvarBase() throws IOException {
        criarBaseFileSeNaoExistir();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(BASE_FILE))) {
            salvarDiretorio(root, "", writer);
        }
    }

    private void salvarDiretorio(Directory dir, String path, BufferedWriter writer) throws IOException {
        
        String currentPath = path.isEmpty() ? dir.getName() : path + "/" + dir.getName();
        if (!dir.getName().equals("root")) {
            writer.write("[DIR] " + currentPath);
            writer.newLine();
        }

        for (FSFile file : dir.getFiles()) {
            writer.write("[FILE] " + currentPath + "/" + file.getName());
            writer.newLine();
        }
        for (Directory subDir : dir.getSubDirectories()) {
            salvarDiretorio(subDir, currentPath, writer);
        }
    }

    private void carregarBase() throws IOException {
        root.getSubDirectories().clear();
        root.getFiles().clear();

        try (BufferedReader reader = new BufferedReader(new FileReader(BASE_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                if (line.startsWith("[DIR] ")) {
                    String dirPath = line.substring(6);
                    criarDiretorioPeloPath(dirPath);
                } else if (line.startsWith("[FILE] ")) {
                    String filePath = line.substring(7);
                    criarArquivoPeloPath(filePath);
                }
            }
        }
    }

    private void criarDiretorioPeloPath(String dirPath) {
        String[] partes = dirPath.split("/");
        Directory current = root;

        for (int i = 1; i < partes.length; i++) {
            String parte = partes[i];
            Optional<Directory> existente = current.getSubDirectories().stream()
                .filter(directory -> directory.getName().equals(parte))
                .findFirst();

            if (existente.isPresent()) {
                current = existente.get();

            } else {
                if (i == partes.length - 1) {
                    Directory novoDir = new Directory(parte);
                    current.getSubDirectories().add(novoDir);
                    current = novoDir;
                } else {
                    System.out.println("Caminho inválido ao carregar diretório: " + dirPath);
                    return;
                }
            }
        }
    }

    private void criarArquivoPeloPath(String filePath) {

        int lastSlash = filePath.lastIndexOf("/");
        String dirPath = filePath.substring(0, lastSlash);
        String fileName = filePath.substring(lastSlash + 1);
        Directory dir = findDirectory(dirPath);

        if (dir != null) {
            boolean exists = dir.getFiles().stream()
                    .anyMatch(files -> files.getName().equals(fileName));
            if (!exists) {
                dir.getFiles().add(new FSFile(fileName));
            }
        }
    }

     private Directory findDirectory(String path) {

        if (path.equals("root")) {
            return root;
        }

        String[] names = path.split("/");
        Directory current = root;

        if (!names[0].equals("root")) {
            return null;
        }

        for (int i = 1; i < names.length; i++) {
            final String segment = names[i];
            Optional<Directory> next = current.getSubDirectories().stream()
                .filter(d -> d.getName().equals(segment))
                .findFirst();

            if (next.isPresent()) {
                current = next.get();
            } else {
                return null;
            }
        }
        return current;
    }

    private void registrarNoJournal(String comando) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        journal.logCommand(timestamp + " " + comando);
    }

    public void createDirectory(String parentPath, String dirName) {
        createDirectory(parentPath, dirName, true);
    }

    private void createDirectory(String parentPath, String dirName, boolean log) {
    
        Directory parent = findDirectory(parentPath);
    
        if (parent != null) {
    
            boolean exists = parent.getSubDirectories().stream()
                .anyMatch(directory -> directory.getName().equals(dirName));

                if (exists) {        
                    System.out.println("Diretório já existe: " + dirName);
                    return;
                }

            Directory newDir = new Directory(dirName);
            parent.getSubDirectories().add(newDir);

            if (log) {
                registrarNoJournal("CREATE_DIR " + parentPath + " " + dirName);
                try {
                    salvarBase();
                } catch (IOException e) {
                    System.out.println("Erro ao salvar base.txt: " + e.getMessage());
                }
            }
    
        } else {
            System.out.println("Diretório pai não encontrado: " + parentPath);
        }
    }

    public void createFile(String dirPath, String fileName) {
        createFile(dirPath, fileName, true);
    }

    private void createFile(String dirPath, String fileName, boolean log) {
        Directory dir = findDirectory(dirPath);
    
        if (dir != null) {
            boolean exists = dir.getFiles().stream()
                    .anyMatch(file -> file.getName().equals(fileName));

            if (exists) {
                System.out.println("Arquivo já existe: " + fileName);
                return;
            }

            dir.getFiles().add(new FSFile(fileName));
            if (log) {
                registrarNoJournal("CREATE_FILE " + dirPath + " " + fileName);
                try {
                    salvarBase();
                } catch (IOException e) {
                    System.out.println("Erro ao salvar base.txt: " + e.getMessage());
                }
            }
        } else {
            System.out.println("Diretório não encontrado: " + dirPath);
        }
    }

    public void deleteFile(String dirPath, String fileName) {
        deleteFile(dirPath, fileName, true);
    }

    private void deleteFile(String dirPath, String fileName, boolean log) {
        Directory dir = findDirectory(dirPath);
        if (dir != null) {
            boolean removed = dir.getFiles().removeIf(f -> f.getName().equals(fileName));
            if (removed && log) {
                registrarNoJournal("DELETE_FILE " + dirPath + " " + fileName);
                try {
                    salvarBase();
                } catch (IOException e) {
                    System.out.println("Erro ao salvar base.txt: " + e.getMessage());
                }
            } else {
                System.out.println("Arquivo não encontrado: " + fileName);
            }
        }
    }

    public void deleteDirectory(String parentPath, String dirName) {
        deleteDirectory(parentPath, dirName, true);
    }

    private void deleteDirectory(String parentPath, String dirName, boolean log) {
        Directory parent = findDirectory(parentPath);
        if (parent != null) {
            boolean removed = parent.getSubDirectories().removeIf(d -> d.getName().equals(dirName));
            if (removed && log) {
                registrarNoJournal("DELETE_DIR " + parentPath + " " + dirName);
                try {
                    salvarBase();
                } catch (IOException e) {
                    System.out.println("Erro ao salvar base.txt: " + e.getMessage());
                }
            } else {
                System.out.println("Diretório não encontrado: " + dirName);
            }
        }
    }

    public void renameFile(String dirPath, String oldName, String newName) {
        renameFile(dirPath, oldName, newName, true);
    }

    private void renameFile(String dirPath, String oldName, String newName, boolean log) {
        Directory dir = findDirectory(dirPath);
        if (dir != null) {
            for (FSFile f : dir.getFiles()) {
                if (f.getName().equals(oldName)) {
                    f.setName(newName);
                    if (log) {
                        registrarNoJournal("RENAME_FILE " + dirPath + " " + oldName + " " + newName);
                        try {
                            salvarBase();
                        } catch (IOException e) {
                            System.out.println("Erro ao salvar base.txt: " + e.getMessage());
                        }
                    }
                    break;
                } else {
                    System.out.println("Arquivo não encontrado: " + oldName);
                }
            }
        }
    }

    public void renameDirectory(String parentPath, String oldName, String newName) {
        renameDirectory(parentPath, oldName, newName, true);
    }

    private void renameDirectory(String parentPath, String oldName, String newName, boolean log) {
        Directory parent = findDirectory(parentPath);
        if (parent != null) {
            for (Directory d : parent.getSubDirectories()) {
                if (d.getName().equals(oldName)) {
                    d.setName(newName);
                    if (log) {
                        registrarNoJournal("RENAME_DIR " + parentPath + " " + oldName + " " + newName);
                        try {
                            salvarBase();
                        } catch (IOException e) {
                            System.out.println("Erro ao salvar base.txt: " + e.getMessage());
                        }
                    }
                    break;
                }
            }
        }
    }

    public void listDirectory(String path) {
        Directory dir = findDirectory(path);
        if (dir != null) {
            System.out.println("Conteúdo do diretório '" + path + "':");
            for (Directory d : dir.getSubDirectories()) {
                System.out.println("[DIR] " + d.getName());
            }
            for (FSFile f : dir.getFiles()) {
                System.out.println("[FILE] " + f.getName());
            }
        } else {
            System.out.println("Diretório '" + path + "' não encontrado.");
        }
    }

    public void copyFile(String sourceDirPath, String fileName, String targetDirPath) throws IOException {
        copyFile(sourceDirPath, fileName, targetDirPath, true);
    }

    private void copyFile(String sourceDirPath, String fileName, String targetDirPath, boolean log) throws IOException {
        Directory sourceDir = findDirectory(sourceDirPath);
        Directory targetDir = findDirectory(targetDirPath);

        if (sourceDir == null) {
            System.out.println("Diretório origem não encontrado: " + sourceDirPath);
            return;
        }

        if (targetDir == null) {
            System.out.println("Diretório destino não encontrado: " + targetDirPath);
            return;
        }

        Optional<FSFile> fileToCopy = sourceDir.getFiles().stream()
            .filter(file -> file.getName().equals(fileName))
            .findFirst();

        if (!fileToCopy.isPresent()) {
            System.out.println("Arquivo não encontrado no diretório origem: " + fileName);
            return;
        }

        boolean existsInTarget = targetDir.getFiles().stream()
            .anyMatch(file -> file.getName().equals(fileName));

        if (existsInTarget) {
            System.out.println("Arquivo já existe no diretório destino: " + fileName);
            return;
        }

        FSFile copiedFile = new FSFile(fileName);
        targetDir.getFiles().add(copiedFile);

        if (log) {
            registrarNoJournal("COPY_FILE " + sourceDirPath + " " + fileName + " " + targetDirPath);
            salvarBase();
        };
    }
    public Directory getRoot() {
        return root;
    }
    
}