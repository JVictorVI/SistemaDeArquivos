package com.filesystem;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;
import java.io.IOException;

public class FileSystemGUI extends JFrame {
    private final FileSystemSimulator fs;
    private final JTree tree;
    private final DefaultTreeModel treeModel;
    private final JTextArea logArea;

    public FileSystemGUI() {
        super("Simulador de Sistema de Arquivos");

        fs = new FileSystemSimulator();

        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(new TreeItem("root", true));
        treeModel = new DefaultTreeModel(rootNode);
        tree = new JTree(treeModel);
        tree.setShowsRootHandles(true);
        tree.setRootVisible(true);
        tree.setCellRenderer(new CustomTreeCellRenderer()); 

        JScrollPane treeScroll = new JScrollPane(tree);

        JButton criarDiretorioBtn = new JButton("Criar Diretório");
        JButton criarArquivoBtn = new JButton("Criar Arquivo");
        JButton renomearBtn = new JButton("Renomear");
        JButton deletarBtn = new JButton("Deletar");
        JButton copiarArquivoBtn = new JButton("Copiar Arquivo");
        JButton atualizarBtn = new JButton("Atualizar");

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(2, 3));
        buttonPanel.add(criarDiretorioBtn);
        buttonPanel.add(criarArquivoBtn);
        buttonPanel.add(renomearBtn);
        buttonPanel.add(deletarBtn);
        buttonPanel.add(copiarArquivoBtn);
        buttonPanel.add(atualizarBtn);

        logArea = new JTextArea(5, 20);
        logArea.setEditable(false);
        JScrollPane logScroll = new JScrollPane(logArea);

        add(treeScroll, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.NORTH);
        add(logScroll, BorderLayout.SOUTH);

        setSize(600, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);

        criarDiretorioBtn.addActionListener(e -> criarDiretorio());
        criarArquivoBtn.addActionListener(e -> criarArquivo());
        renomearBtn.addActionListener(e -> renomear());
        deletarBtn.addActionListener(e -> deletar());
        copiarArquivoBtn.addActionListener(e -> copiarArquivo());
        atualizarBtn.addActionListener(e -> atualizarArvore());

        atualizarArvore(); 
    }

    private void criarDiretorio() {
        TreePath path = tree.getSelectionPath();
        if (path == null) return;

        String nome = JOptionPane.showInputDialog(this, "Nome do diretório:");
        if (nome != null && !nome.isEmpty()) {
            String parentPath = getCaminhoCompleto(path);
            fs.createDirectory(parentPath, nome);
            atualizarArvore();
            log("Diretório criado: " + parentPath + "/" + nome);
        }
    }

    private void criarArquivo() {
        TreePath path = tree.getSelectionPath();
        if (path == null) return;

        String nome = JOptionPane.showInputDialog(this, "Nome do arquivo:");
        if (nome != null && !nome.isEmpty()) {
            String dirPath = getCaminhoCompleto(path);
            fs.createFile(dirPath, nome);
            atualizarArvore();
            log("Arquivo criado: " + dirPath + "/" + nome);
        }
    }

    private void renomear() {
        TreePath path = tree.getSelectionPath();
        if (path == null) return;

        DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
        TreeItem item = (TreeItem) node.getUserObject();

        String oldName = item.getName();
        String newName = JOptionPane.showInputDialog(this, "Novo nome:", oldName);
        if (newName != null && !newName.isEmpty()) {
            String parentPath = getCaminhoCompleto(path.getParentPath());
            if (item.isDirectory()) {
                fs.renameDirectory(parentPath, oldName, newName);
                log("Diretório renomeado: " + oldName + " → " + newName);
            } else {
                fs.renameFile(parentPath, oldName, newName);
                log("Arquivo renomeado: " + oldName + " → " + newName);
            }
            atualizarArvore();
        }
    }

    private void deletar() {
        TreePath path = tree.getSelectionPath();
        if (path == null || path.getPathCount() == 1) return;

        DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
        TreeItem item = (TreeItem) node.getUserObject();

        String nome = item.getName();
        String parentPath = getCaminhoCompleto(path.getParentPath());

        if (item.isDirectory()) {
            fs.deleteDirectory(parentPath, nome);
            log("Diretório deletado: " + parentPath + "/" + nome);
        } else {
            fs.deleteFile(parentPath, nome);
            log("Arquivo deletado: " + parentPath + "/" + nome);
        }
        atualizarArvore();
    }

    private void copiarArquivo() {
        TreePath path = tree.getSelectionPath();
        if (path == null) return;

        DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
        TreeItem item = (TreeItem) node.getUserObject();

        if (item.isDirectory()) {
            JOptionPane.showMessageDialog(this, "Selecione um arquivo para copiar. ");
            return;
        }

        String fileName = item.getName();
        String origem = getCaminhoCompleto(path.getParentPath());

        String destino = JOptionPane.showInputDialog(this, "Caminho do diretório destino (Ex: root/movies):");

        if (destino != null && !destino.isEmpty()) {
            try {
                fs.copyFile(origem, fileName, destino);
                atualizarArvore();
                log("Arquivo copiado de " + origem + " para " + destino);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Erro ao copiar arquivo: " + e.getMessage());
            }
        }
    }

    private void atualizarArvore() {
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(new TreeItem("root", true));
        treeModel.setRoot(rootNode);
        carregarDiretorios(fs.getRoot(), "root", rootNode);
        treeModel.reload();
    }

    private void carregarDiretorios(Directory dir, String path, DefaultMutableTreeNode node) {
        for (Directory sub : dir.getSubDirectories()) {
            DefaultMutableTreeNode subNode = new DefaultMutableTreeNode(new TreeItem(sub.getName(), true));
            node.add(subNode);
            carregarDiretorios(sub, path + "/" + sub.getName(), subNode);
        }
        for (FSFile file : dir.getFiles()) {
            node.add(new DefaultMutableTreeNode(new TreeItem(file.getName(), false)));
        }
    }

    private String getCaminhoCompleto(TreePath path) {
        Object[] nodes = path.getPath();
        StringBuilder fullPath = new StringBuilder();
        for (Object obj : nodes) {
            TreeItem item = (TreeItem) ((DefaultMutableTreeNode) obj).getUserObject();
            if (!fullPath.isEmpty()) fullPath.append("/");
            fullPath.append(item.getName());
        }
        return fullPath.toString();
    }

    private void log(String msg) {
        logArea.append(msg + "\n");
    }

    private static class CustomTreeCellRenderer extends DefaultTreeCellRenderer {
        private final Icon folderIcon = UIManager.getIcon("FileView.directoryIcon");
        private final Icon fileIcon = UIManager.getIcon("FileView.fileIcon");

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value,
                boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {

            super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);

            DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
            Object userObject = node.getUserObject();

            if (userObject instanceof TreeItem item) {
                setIcon(item.isDirectory() ? folderIcon : fileIcon);
            }

            return this;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(FileSystemGUI::new);
    }
}
