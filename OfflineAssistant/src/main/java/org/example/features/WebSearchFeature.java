package org.example.features;

import java.awt.Desktop;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class WebSearchFeature implements Feature {

    @Override
    public boolean canHandle(String input) {
        String lower = input.toLowerCase();
        return lower.startsWith("search for ") || lower.startsWith("google ")
                || lower.startsWith("search ") || lower.startsWith("youtube ")
                || lower.startsWith("play ") || lower.startsWith("wikipedia ")
                || lower.startsWith("wiki ") || lower.startsWith("browse ")
                || lower.startsWith("open website ") || lower.startsWith("go to ")
                || lower.startsWith("open url ");
    }

    @Override
    public void execute(String input, ChatCallback callback) {
        String lower = input.toLowerCase().trim();

        try {
            if (lower.startsWith("youtube ") || lower.startsWith("play ")) {
                String query = lower.startsWith("youtube ") ? input.substring(8).trim() : input.substring(5).trim();
                openUrl("https://www.youtube.com/results?search_query=" + encode(query), callback, "YouTube: " + query);
            } else if (lower.startsWith("wikipedia ") || lower.startsWith("wiki ")) {
                String query = lower.startsWith("wikipedia ") ? input.substring(10).trim() : input.substring(5).trim();
                openUrl("https://en.wikipedia.org/wiki/Special:Search?search=" + encode(query), callback, "Wikipedia: " + query);
            } else if (lower.startsWith("go to ") || lower.startsWith("open url ") || lower.startsWith("browse ")) {
                String url = lower.startsWith("go to ") ? input.substring(6).trim()
                        : lower.startsWith("open url ") ? input.substring(9).trim()
                        : input.substring(7).trim();
                if (!url.startsWith("http")) url = "https://" + url;
                openUrl(url, callback, url);
            } else if (lower.startsWith("open website ")) {
                String url = input.substring(13).trim();
                if (!url.startsWith("http")) url = "https://" + url;
                openUrl(url, callback, url);
            } else {
                // Generic Google search
                String query = lower.replaceAll("^(search for|google|search)\\s+", "");
                openUrl("https://www.google.com/search?q=" + encode(query), callback, "Google: " + query);
            }
        } catch (Exception e) {
            callback.onMessage("❌ Web", "Could not open browser: " + e.getMessage());
        }
    }

    private void openUrl(String url, ChatCallback callback, String label) {
        try {
            Desktop.getDesktop().browse(new URI(url));
            callback.onMessage("🌐 Web", "Opened: " + label);
        } catch (Exception e) {
            callback.onMessage("❌ Web", "Could not open: " + e.getMessage());
        }
    }

    private String encode(String text) {
        return URLEncoder.encode(text, StandardCharsets.UTF_8);
    }
}