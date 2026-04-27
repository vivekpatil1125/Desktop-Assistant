package org.example.features;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

public class FileSearchFeature implements Feature {

    @Override
    public boolean canHandle(String input) {
        String lower = input.toLowerCase();
        return lower.matches(".*(find|search|locate|look for).*(file|folder|document).*")
                || lower.matches(".*\\b(find|search|locate)\\b.*\\.(java|txt|pdf|doc|docx|png|jpg|jpeg|exe|mp3|mp4|zip|xlsx|pptx|csv).*")
                || lower.startsWith("delete file ")
                || lower.startsWith("delete folder ")
                || lower.startsWith("rename file ")
                || lower.startsWith("open file ")
                || lower.startsWith("create folder ")
                || lower.startsWith("create file ");
    }

    @Override
    public void execute(String input, ChatCallback callback) {
        String lower = input.toLowerCase().trim();

        if (lower.startsWith("delete file ")) {
            deleteFile(input.substring("delete file ".length()).trim(), false, callback);
        } else if (lower.startsWith("delete folder ")) {
            deleteFile(input.substring("delete folder ".length()).trim(), true, callback);
        } else if (lower.startsWith("rename file ")) {
            renameFile(input.substring("rename file ".length()).trim(), callback);
        } else if (lower.startsWith("open file ")) {
            openFile(input.substring("open file ".length()).trim(), callback);
        } else if (lower.startsWith("create folder ")) {
            createFolder(input.substring("create folder ".length()).trim(), callback);
        } else if (lower.startsWith("create file ")) {
            createNewFile(input.substring("create file ".length()).trim(), callback);
        } else {
            searchFile(input, callback);
        }
    }

    // ─────────────────────────────────────────────────────────────────
    //  SEARCH
    // ─────────────────────────────────────────────────────────────────
    private void searchFile(String input, ChatCallback callback) {
        String query = extractSearchTerm(input);
        if (query.isEmpty()) {
            callback.onMessage("⚠️ Assistant", "Please specify a file name to search for.");
            return;
        }

        callback.onMessage("🔍 Assistant",
                "Searching for: \"" + query + "\"\nScanning your home folder (this may take a moment)...");

        new Thread(() -> {
            try {
                List<Path> results = new ArrayList<>();
                Path rootPath = Paths.get(System.getProperty("user.home"));
                final String queryLower = query.toLowerCase();

                Files.walkFileTree(rootPath, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                        if (file.getFileName().toString().toLowerCase().contains(queryLower)) {
                            results.add(file);
                            if (results.size() >= 10) return FileVisitResult.TERMINATE;
                        }
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFileFailed(Path file, IOException exc) {
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                        String dirName = dir.getFileName() != null
                                ? dir.getFileName().toString().toLowerCase()
                                : "";

                        if (dirName.equals("windows")
                                || dirName.equals("$recycle.bin")
                                || dirName.equals("system volume information")
                                || dirName.startsWith(".")) {
                            return FileVisitResult.SKIP_SUBTREE;
                        }
                        return FileVisitResult.CONTINUE;
                    }
                });

                if (results.isEmpty()) {
                    callback.onMessage("❌ Assistant", "No files found matching: \"" + query + "\"");
                } else {
                    StringBuilder sb = new StringBuilder("Found " + results.size() + " result(s):\n\n");
                    for (int i = 0; i < results.size(); i++) {
                        sb.append(i + 1)
                                .append(". ")
                                .append(results.get(i).toString())
                                .append("\n");
                    }

                    Path first = results.get(0);
                    sb.append("\nOpening the first result location...");
                    callback.onMessage("✅ Assistant", sb.toString());

                    openFileLocation(first, callback);
                }
            } catch (Exception e) {
                callback.onMessage("❌ Assistant", "Search error: " + e.getMessage());
            }
        }).start();
    }

    private void openFileLocation(Path filePath, ChatCallback callback) {
        try {
            File file = filePath.toFile();
            String os = System.getProperty("os.name").toLowerCase();

            if (os.contains("win")) {
                new ProcessBuilder("explorer.exe", "/select,", file.getAbsolutePath()).start();
            } else if (Desktop.isDesktopSupported()) {
                File parent = file.getParentFile();
                if (parent != null) {
                    Desktop.getDesktop().open(parent);
                }
            }

            callback.onMessage("📂 Assistant", "Opened location: " + file.getAbsolutePath());
        } catch (Exception e) {
            callback.onMessage("❌ Assistant", "Could not open file location: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────────
    //  DELETE
    // ─────────────────────────────────────────────────────────────────
    private void deleteFile(String pathStr, boolean isFolder, ChatCallback callback) {
        File target = resolveFile(pathStr);
        if (!target.exists()) {
            callback.onMessage("❌ Assistant", "File/folder not found: " + pathStr);
            return;
        }

        boolean success = isFolder ? deleteRecursive(target) : target.delete();

        if (success) {
            callback.onMessage("🗑️ Assistant", "Deleted: " + target.getAbsolutePath());
        } else {
            callback.onMessage("❌ Assistant", "Could not delete: " + target.getAbsolutePath());
        }
    }

    private boolean deleteRecursive(File file) {
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    deleteRecursive(child);
                }
            }
        }
        return file.delete();
    }

    // ─────────────────────────────────────────────────────────────────
    //  RENAME
    // ─────────────────────────────────────────────────────────────────
    private void renameFile(String args, ChatCallback callback) {
        String[] parts = args.split("\\s+to\\s+", 2);
        if (parts.length < 2) {
            callback.onMessage("⚠️ Assistant", "Usage: rename file <old name> to <new name>");
            return;
        }

        File oldFile = resolveFile(parts[0].trim());
        File newFile = new File(oldFile.getParent(), parts[1].trim());

        if (!oldFile.exists()) {
            callback.onMessage("❌ Assistant", "File not found: " + parts[0].trim());
            return;
        }

        if (oldFile.renameTo(newFile)) {
            callback.onMessage("✅ Assistant", "Renamed to: " + newFile.getName());
        } else {
            callback.onMessage("❌ Assistant", "Could not rename file.");
        }
    }

    // ─────────────────────────────────────────────────────────────────
    //  OPEN
    // ─────────────────────────────────────────────────────────────────
    private void openFile(String pathStr, ChatCallback callback) {
        File file = resolveFile(pathStr);
        if (!file.exists()) {
            callback.onMessage("❌ Assistant", "File not found: " + pathStr);
            return;
        }

        try {
            Desktop.getDesktop().open(file);
            callback.onMessage("✅ Assistant", "Opened: " + file.getAbsolutePath());
        } catch (Exception e) {
            callback.onMessage("❌ Assistant", "Could not open file: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────────
    //  CREATE FOLDER / FILE
    // ─────────────────────────────────────────────────────────────────
    private void createFolder(String name, ChatCallback callback) {
        File folder = new File(System.getProperty("user.home")
                + File.separator + "Desktop"
                + File.separator + name);

        if (folder.mkdirs()) {
            callback.onMessage("📁 Assistant", "Folder created on Desktop: " + name);
        } else {
            callback.onMessage("❌ Assistant", "Could not create folder. It may already exist.");
        }
    }

    private void createNewFile(String name, ChatCallback callback) {
        try {
            File file = new File(System.getProperty("user.home")
                    + File.separator + "Desktop"
                    + File.separator + name);

            if (file.createNewFile()) {
                callback.onMessage("📄 Assistant", "File created on Desktop: " + name);
            } else {
                callback.onMessage("⚠️ Assistant", "File already exists: " + name);
            }
        } catch (IOException e) {
            callback.onMessage("❌ Assistant", "Could not create file: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────────
    //  HELPERS
    // ─────────────────────────────────────────────────────────────────
    private File resolveFile(String pathStr) {
        File f = new File(pathStr);
        if (!f.isAbsolute()) {
            File desktop = new File(System.getProperty("user.home")
                    + File.separator + "Desktop"
                    + File.separator + pathStr);
            if (desktop.exists()) return desktop;

            return new File(System.getProperty("user.home") + File.separator + pathStr);
        }
        return f;
    }

    private String extractSearchTerm(String input) {
        return input.toLowerCase()
                .replaceAll("\\b(find|search|locate|look for|file|folder|document|for|me|the|a|an)\\b", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }
}