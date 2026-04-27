package org.example.features;
import java.awt.*;
import java.awt.datatransfer.*;
import java.util.ArrayList;
import java.util.List;

public class ClipboardFeature implements Feature {

        private static final List<String> clipboardHistory = new ArrayList<>();
        private static final int MAX_HISTORY = 15;

        @Override
        public boolean canHandle(String input) {
            String lower = input.toLowerCase();
            return lower.contains("clipboard") || lower.startsWith("copy ")
                    || lower.equals("paste") || lower.equals("show clipboard")
                    || lower.equals("clear clipboard") || lower.contains("clipboard history");
        }

        @Override
        public void execute(String input, ChatCallback callback) {
            String lower = input.toLowerCase().trim();

            if (lower.startsWith("copy ")) {
                String textToCopy = input.substring(5).trim();
                copyToClipboard(textToCopy, callback);
            } else if (lower.equals("paste") || lower.contains("get clipboard")) {
                pasteFromClipboard(callback);
            } else if (lower.contains("clipboard history") || lower.equals("show clipboard")) {
                showHistory(callback);
            } else if (lower.equals("clear clipboard")) {
                clearClipboard(callback);
            } else {
                showHistory(callback);
            }
        }

        public static void trackClipboard(String text) {
            if (text != null && !text.isEmpty() && (clipboardHistory.isEmpty() || !clipboardHistory.get(clipboardHistory.size() - 1).equals(text))) {
                if (clipboardHistory.size() >= MAX_HISTORY) clipboardHistory.remove(0);
                clipboardHistory.add(text);
            }
        }

        private void copyToClipboard(String text, ChatCallback callback) {
            try {
                Toolkit.getDefaultToolkit().getSystemClipboard()
                        .setContents(new StringSelection(text), null);
                trackClipboard(text);
                callback.onMessage("📋 Clipboard", "Copied: \"" + text + "\"");
            } catch (Exception e) {
                callback.onMessage("❌ Error", "Could not copy: " + e.getMessage());
            }
        }

        private void pasteFromClipboard(ChatCallback callback) {
            try {
                Transferable contents = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
                if (contents != null && contents.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                    String text = (String) contents.getTransferData(DataFlavor.stringFlavor);
                    callback.onMessage("📋 Clipboard", "Current clipboard:\n\"" + text + "\"");
                } else {
                    callback.onMessage("📋 Clipboard", "Clipboard is empty or contains non-text content.");
                }
            } catch (Exception e) {
                callback.onMessage("❌ Error", "Could not read clipboard: " + e.getMessage());
            }
        }

        private void showHistory(ChatCallback callback) {
            if (clipboardHistory.isEmpty()) {
                callback.onMessage("📋 Clipboard", "Clipboard history is empty.");
                return;
            }
            StringBuilder sb = new StringBuilder("📋 Clipboard History (" + clipboardHistory.size() + " items):\n\n");
            for (int i = clipboardHistory.size() - 1; i >= 0; i--) {
                String item = clipboardHistory.get(i);
                String preview = item.length() > 60 ? item.substring(0, 60) + "..." : item;
                sb.append((clipboardHistory.size() - i)).append(". ").append(preview).append("\n");
            }
            callback.onMessage("📋 Clipboard", sb.toString());
        }

        private void clearClipboard(ChatCallback callback) {
            try {
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(""), null);
                clipboardHistory.clear();
                callback.onMessage("✅ Clipboard", "Clipboard cleared.");
            } catch (Exception e) {
                callback.onMessage("❌ Error", "Could not clear clipboard: " + e.getMessage());
            }
        }
    }
