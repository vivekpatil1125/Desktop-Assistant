package org.example.features;

import java.util.HashMap;
import java.util.Map;

public class AppLaunchFeature implements Feature {

    private static final Map<String, String[]> WIN_APPS = new HashMap<>();
    private static final Map<String, String[]> MAC_APPS = new HashMap<>();

    static {
        // Windows app commands
        WIN_APPS.put("chrome",        new String[]{"cmd", "/c", "start", "chrome"});
        WIN_APPS.put("firefox",       new String[]{"cmd", "/c", "start", "firefox"});
        WIN_APPS.put("edge",          new String[]{"cmd", "/c", "start", "msedge"});
        WIN_APPS.put("notepad",       new String[]{"notepad.exe"});
        WIN_APPS.put("calculator",    new String[]{"calc.exe"});
        WIN_APPS.put("calc",          new String[]{"calc.exe"});
        WIN_APPS.put("paint",         new String[]{"mspaint.exe"});
        WIN_APPS.put("explorer",      new String[]{"explorer.exe"});
        WIN_APPS.put("file explorer", new String[]{"explorer.exe"});
        WIN_APPS.put("task manager",  new String[]{"taskmgr.exe"});
        WIN_APPS.put("cmd",           new String[]{"cmd.exe"});
        WIN_APPS.put("terminal",      new String[]{"cmd.exe"});
        WIN_APPS.put("powershell",    new String[]{"powershell.exe"});
        WIN_APPS.put("word",          new String[]{"cmd", "/c", "start", "winword"});
        WIN_APPS.put("excel",         new String[]{"cmd", "/c", "start", "excel"});
        WIN_APPS.put("powerpoint",    new String[]{"cmd", "/c", "start", "powerpnt"});
        WIN_APPS.put("vscode",        new String[]{"cmd", "/c", "start", "code"});
        WIN_APPS.put("vs code",       new String[]{"cmd", "/c", "start", "code"});
        WIN_APPS.put("spotify",       new String[]{"cmd", "/c", "start", "spotify"});
        WIN_APPS.put("vlc",           new String[]{"cmd", "/c", "start", "vlc"});
        WIN_APPS.put("zoom",          new String[]{"cmd", "/c", "start", "zoom"});
        WIN_APPS.put("discord",       new String[]{"cmd", "/c", "start", "discord"});
        WIN_APPS.put("whatsapp",      new String[]{"cmd", "/c", "start", "whatsapp"});
        WIN_APPS.put("telegram",      new String[]{"cmd", "/c", "start", "telegram"});
        WIN_APPS.put("settings",      new String[]{"cmd", "/c", "start", "ms-settings:"});
        WIN_APPS.put("control panel", new String[]{"control.exe"});
        WIN_APPS.put("snipping tool", new String[]{"snippingtool.exe"});
        WIN_APPS.put("wordpad",       new String[]{"wordpad.exe"});
        WIN_APPS.put("winamp",        new String[]{"cmd", "/c", "start", "winamp"});

        // Mac app commands
        MAC_APPS.put("chrome",     new String[]{"open", "-a", "Google Chrome"});
        MAC_APPS.put("firefox",    new String[]{"open", "-a", "Firefox"});
        MAC_APPS.put("safari",     new String[]{"open", "-a", "Safari"});
        MAC_APPS.put("terminal",   new String[]{"open", "-a", "Terminal"});
        MAC_APPS.put("finder",     new String[]{"open", "-a", "Finder"});
        MAC_APPS.put("vscode",     new String[]{"open", "-a", "Visual Studio Code"});
        MAC_APPS.put("vs code",    new String[]{"open", "-a", "Visual Studio Code"});
        MAC_APPS.put("spotify",    new String[]{"open", "-a", "Spotify"});
        MAC_APPS.put("zoom",       new String[]{"open", "-a", "zoom.us"});
        MAC_APPS.put("discord",    new String[]{"open", "-a", "Discord"});
        MAC_APPS.put("settings",   new String[]{"open", "-a", "System Preferences"});
        MAC_APPS.put("calculator", new String[]{"open", "-a", "Calculator"});
    }

    @Override
    public boolean canHandle(String input) {
        String lower = input.toLowerCase();
        return lower.startsWith("open ")
                || lower.startsWith("launch ")
                || lower.startsWith("start ")
                || lower.startsWith("run ")
                || lower.startsWith("close ")
                || lower.startsWith("kill ")
                || lower.startsWith("exit ");
    }

    @Override
    public void execute(String input, ChatCallback callback) {
        String lower = input.toLowerCase().trim();
        boolean isClose = lower.startsWith("close ")
                || lower.startsWith("kill ")
                || lower.startsWith("exit ");

        String appName = lower
                .replaceAll("^(open|launch|start|run|close|kill|exit)\\s+", "")
                .trim();

        if (appName.isEmpty()) {
            callback.onMessage("⚠️ System", "Please specify an app name.");
            return;
        }

        if (isClose) {
            closeApp(appName, callback);
            return;
        }

        callback.onMessage("⚙️ System", "Launching: " + appName + "...");
        String os = System.getProperty("os.name").toLowerCase();

        try {
            String[] cmd;

            if (os.contains("win")) {
                cmd = WIN_APPS.get(appName);

                if (cmd == null) {
                    for (Map.Entry<String, String[]> entry : WIN_APPS.entrySet()) {
                        if (appName.contains(entry.getKey()) || entry.getKey().contains(appName)) {
                            cmd = entry.getValue();
                            break;
                        }
                    }
                }

                if (cmd == null) {
                    cmd = new String[]{"cmd", "/c", "start", appName};
                }
            } else if (os.contains("mac")) {
                cmd = MAC_APPS.get(appName);
                if (cmd == null) {
                    cmd = new String[]{"open", "-a", appName};
                }
            } else {
                cmd = new String[]{"xdg-open", appName};
            }

            Runtime.getRuntime().exec(cmd);
            UsageTracker.trackApp(appName);
            callback.onMessage("✅ System", "Launched: " + appName);

        } catch (Exception e) {
            callback.onMessage("❌ Error", "Could not launch '" + appName + "': " + e.getMessage());
        }
    }

    private void closeApp(String appName, ChatCallback callback) {
        String os = System.getProperty("os.name").toLowerCase();
        callback.onMessage("⚙️ System", "Closing: " + appName + "...");

        try {
            if (os.contains("win")) {
                String procName = appName;

                if (appName.contains("chrome")) procName = "chrome.exe";
                else if (appName.contains("firefox")) procName = "firefox.exe";
                else if (appName.contains("notepad")) procName = "notepad.exe";
                else if (appName.contains("calculator") || appName.contains("calc")) procName = "calc.exe";
                else if (appName.contains("explorer")) procName = "explorer.exe";
                else if (appName.contains("spotify")) procName = "spotify.exe";
                else if (appName.contains("discord")) procName = "discord.exe";
                else if (appName.contains("zoom")) procName = "zoom.exe";
                else if (appName.contains("vlc")) procName = "vlc.exe";
                else if (!procName.endsWith(".exe")) procName = appName + ".exe";

                Runtime.getRuntime().exec("taskkill /F /IM " + procName);
                callback.onMessage("✅ System", "Closed: " + appName);

            } else if (os.contains("mac")) {
                Runtime.getRuntime().exec(new String[]{"pkill", "-a", "-i", appName});
                callback.onMessage("✅ System", "Closed: " + appName);
            }
        } catch (Exception e) {
            callback.onMessage("❌ Error", "Could not close '" + appName + "': " + e.getMessage());
        }
    }
}