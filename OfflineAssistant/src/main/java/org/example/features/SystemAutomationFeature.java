package org.example.features;

public class SystemAutomationFeature implements Feature {

    @Override
    public boolean canHandle(String input) {
        String lower = input.toLowerCase().trim();
        return lower.equals("start coding mode")
                || lower.equals("start study mode")
                || lower.equals("start work mode")
                || lower.equals("start entertainment mode");
    }

    @Override
    public void execute(String input, ChatCallback callback) {
        String lower = input.toLowerCase().trim();

        try {
            switch (lower) {
                case "start coding mode":
                    launchApp("chrome", callback);
                    launchApp("vscode", callback);
                    launchApp("notepad", callback);
                    callback.onMessage("⚙️ Automation", "Coding mode started: Chrome, VS Code, and Notepad launched.");
                    break;

                case "start study mode":
                    launchApp("chrome", callback);
                    launchApp("notepad", callback);
                    callback.onMessage("⚙️ Automation", "Study mode started: Chrome and Notepad launched.");
                    break;

                case "start work mode":
                    launchApp("chrome", callback);
                    launchApp("notepad", callback);
                    launchApp("explorer", callback);
                    callback.onMessage("⚙️ Automation", "Work mode started: Chrome, Notepad, and File Explorer launched.");
                    break;

                case "start entertainment mode":
                    launchApp("spotify", callback);
                    launchApp("chrome", callback);
                    callback.onMessage("⚙️ Automation", "Entertainment mode started: Spotify and Chrome launched.");
                    break;

                default:
                    callback.onMessage("❌ Automation", "Unknown automation command.");
            }
        } catch (Exception e) {
            callback.onMessage("❌ Automation", "Automation failed: " + e.getMessage());
        }
    }

    private void launchApp(String app, ChatCallback callback) {
        try {
            String os = System.getProperty("os.name").toLowerCase();

            if (os.contains("win")) {
                switch (app.toLowerCase()) {
                    case "chrome":
                        new ProcessBuilder("cmd", "/c", "start", "chrome").start();
                        break;
                    case "vscode":
                        new ProcessBuilder("cmd", "/c", "start", "code").start();
                        break;
                    case "notepad":
                        new ProcessBuilder("notepad.exe").start();
                        break;
                    case "spotify":
                        new ProcessBuilder("cmd", "/c", "start", "spotify").start();
                        break;
                    case "explorer":
                        new ProcessBuilder("explorer.exe").start();
                        break;
                    default:
                        new ProcessBuilder("cmd", "/c", "start", app).start();
                        break;
                }
            } else if (os.contains("mac")) {
                switch (app.toLowerCase()) {
                    case "chrome":
                        new ProcessBuilder("open", "-a", "Google Chrome").start();
                        break;
                    case "vscode":
                        new ProcessBuilder("open", "-a", "Visual Studio Code").start();
                        break;
                    case "notepad":
                        new ProcessBuilder("open", "-a", "TextEdit").start();
                        break;
                    case "spotify":
                        new ProcessBuilder("open", "-a", "Spotify").start();
                        break;
                    case "explorer":
                        new ProcessBuilder("open", ".").start();
                        break;
                    default:
                        new ProcessBuilder("open", "-a", app).start();
                        break;
                }
            } else {
                new ProcessBuilder("xdg-open", app).start();
            }

            UsageTracker.trackApp(app);
        } catch (Exception e) {
            callback.onMessage("⚠️ Automation", "Could not open " + app + ": " + e.getMessage());
        }
    }
}