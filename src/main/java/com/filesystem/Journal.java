package com.filesystem;

import java.io.*;

public class Journal {
    private final File logFile;

    public Journal() {
        this.logFile = new File("journal.log");
        try {
            if (!logFile.exists()) {
                logFile.createNewFile();
            }
        } catch (IOException e) {
            System.err.println("Erro ao criar arquivo de log: " + e.getMessage());
        }
    }

    public void logCommand(String command) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(logFile, true))) {
            writer.write(command);
            writer.newLine();
        } catch (IOException e) {
            System.err.println("Erro ao escrever no journal: " + e.getMessage());
        }
    }
}
