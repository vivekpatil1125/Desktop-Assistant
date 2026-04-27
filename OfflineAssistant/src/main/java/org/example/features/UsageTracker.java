package org.example.features;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UsageTracker {

    private static final Map<String, Integer> appUsage = new ConcurrentHashMap<>();
    private static final Map<String, Integer> commandUsage = new ConcurrentHashMap<>();

    public static void trackApp(String appName) {
        if (appName == null || appName.trim().isEmpty()) return;

        String key = appName.trim().toLowerCase();
        appUsage.put(key, appUsage.getOrDefault(key, 0) + 1);
    }

    public static void trackCommand(String command) {
        if (command == null || command.trim().isEmpty()) return;

        String key = command.trim().toLowerCase();
        commandUsage.put(key, commandUsage.getOrDefault(key, 0) + 1);
    }

    public static String getTopAppsReport() {
        if (appUsage.isEmpty()) {
            return "No app usage tracked yet.";
        }

        List<Map.Entry<String, Integer>> list = new ArrayList<>(appUsage.entrySet());
        list.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));

        StringBuilder sb = new StringBuilder("Top used apps:\n");
        for (int i = 0; i < Math.min(5, list.size()); i++) {
            sb.append(i + 1)
                    .append(". ")
                    .append(list.get(i).getKey())
                    .append(" → ")
                    .append(list.get(i).getValue())
                    .append(" time(s)\n");
        }
        return sb.toString();
    }

    public static String getTopCommandsReport() {
        if (commandUsage.isEmpty()) {
            return "No commands tracked yet.";
        }

        List<Map.Entry<String, Integer>> list = new ArrayList<>(commandUsage.entrySet());
        list.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));

        StringBuilder sb = new StringBuilder("Top used commands:\n");
        for (int i = 0; i < Math.min(5, list.size()); i++) {
            sb.append(i + 1)
                    .append(". ")
                    .append(list.get(i).getKey())
                    .append(" → ")
                    .append(list.get(i).getValue())
                    .append(" time(s)\n");
        }
        return sb.toString();
    }
}