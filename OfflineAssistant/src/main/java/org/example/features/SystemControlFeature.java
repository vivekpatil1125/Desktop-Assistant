package org.example.features;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.imageio.ImageIO;

public class SystemControlFeature implements Feature {

    @Override
    public boolean canHandle(String input) {
        String lower = input.toLowerCase();
        return lower.contains("volume") || lower.contains("sound") || lower.contains("mute") || lower.contains("unmute")
                || lower.contains("brightness")
                || lower.contains("screenshot") || lower.contains("screen capture")
                || lower.contains("shutdown") || lower.contains("shut down")
                || lower.contains("restart") || lower.contains("reboot")
                || lower.contains("lock screen") || lower.contains("lock pc") || lower.contains("lock computer")
                || lower.contains("sleep") || lower.contains("hibernate")
                || lower.contains("empty trash") || lower.contains("empty recycle")
                || lower.contains("refresh desktop");
    }

    @Override
    public void execute(String input, ChatCallback callback) {
        String lower = input.toLowerCase();
        String os = System.getProperty("os.name").toLowerCase();

        try {
            // --- VOLUME CONTROLS ---
            if (lower.contains("volume") || lower.contains("sound") || lower.contains("mute")) {
                handleVolume(lower, os, callback);
            }
            // --- BRIGHTNESS ---
            else if (lower.contains("brightness")) {
                handleBrightness(lower, os, callback);
            }
            // --- SCREENSHOT ---
            else if (lower.contains("screenshot") || lower.contains("screen capture")) {
                takeScreenshot(callback);
            }
            // --- SHUTDOWN ---
            else if (lower.contains("shutdown") || lower.contains("shut down")) {
                handleShutdown(os, callback);
            }
            // --- RESTART ---
            else if (lower.contains("restart") || lower.contains("reboot")) {
                handleRestart(os, callback);
            }
            // --- LOCK ---
            else if (lower.contains("lock")) {
                handleLock(os, callback);
            }
            // --- SLEEP ---
            else if (lower.contains("sleep") || lower.contains("hibernate")) {
                handleSleep(os, callback);
            }
            // --- EMPTY RECYCLE BIN ---
            else if (lower.contains("empty trash") || lower.contains("empty recycle")) {
                emptyRecycleBin(os, callback);
            }
            // --- REFRESH DESKTOP ---
            else if (lower.contains("refresh desktop")) {
                Runtime.getRuntime().exec(new String[]{"powershell", "-Command", "Stop-Process -Name explorer -Force; Start-Process explorer"});
                callback.onMessage("✅ System", "Desktop refreshed.");
            }

        } catch (Exception e) {
            callback.onMessage("❌ Error", "System control failed: " + e.getMessage());
        }
    }

    private void handleVolume(String lower, String os, ChatCallback callback) throws Exception {
        if (os.contains("win")) {
            if (lower.contains("up") || lower.contains("increase") || lower.contains("louder")) {
                for (int i = 0; i < 5; i++)
                    Runtime.getRuntime().exec("powershell -Command \"(new-object -com wscript.shell).SendKeys([char]175)\"");
                callback.onMessage("🔊 System", "Volume increased.");
            } else if (lower.contains("down") || lower.contains("decrease") || lower.contains("lower")) {
                for (int i = 0; i < 5; i++)
                    Runtime.getRuntime().exec("powershell -Command \"(new-object -com wscript.shell).SendKeys([char]174)\"");
                callback.onMessage("🔉 System", "Volume decreased.");
            } else if (lower.contains("mute")) {
                Runtime.getRuntime().exec("powershell -Command \"(new-object -com wscript.shell).SendKeys([char]173)\"");
                callback.onMessage("🔇 System", "System muted.");
            } else if (lower.contains("unmute")) {
                Runtime.getRuntime().exec("powershell -Command \"(new-object -com wscript.shell).SendKeys([char]173)\"");
                callback.onMessage("🔊 System", "System unmuted.");
            } else if (lower.contains("max")) {
                for (int i = 0; i < 50; i++)
                    Runtime.getRuntime().exec("powershell -Command \"(new-object -com wscript.shell).SendKeys([char]175)\"");
                callback.onMessage("🔊 System", "Volume set to maximum.");
            } else if (lower.contains("min")) {
                for (int i = 0; i < 50; i++)
                    Runtime.getRuntime().exec("powershell -Command \"(new-object -com wscript.shell).SendKeys([char]174)\"");
                callback.onMessage("🔇 System", "Volume set to minimum.");
            }
        } else if (os.contains("mac")) {
            if (lower.contains("up"))   Runtime.getRuntime().exec(new String[]{"osascript", "-e", "set volume output volume (output volume of (get volume settings) + 10)"});
            else if (lower.contains("down")) Runtime.getRuntime().exec(new String[]{"osascript", "-e", "set volume output volume (output volume of (get volume settings) - 10)"});
            else if (lower.contains("mute"))   Runtime.getRuntime().exec(new String[]{"osascript", "-e", "set volume with output muted"});
            else if (lower.contains("unmute")) Runtime.getRuntime().exec(new String[]{"osascript", "-e", "set volume without output muted"});
            callback.onMessage("🔊 System", "Volume adjusted.");
        }
    }

    private void handleBrightness(String lower, String os, ChatCallback callback) throws Exception {
        if (os.contains("win")) {
            int level = 50;
            if (lower.contains("up") || lower.contains("increase"))   level = 80;
            else if (lower.contains("down") || lower.contains("decrease")) level = 30;
            else if (lower.contains("max")) level = 100;
            else if (lower.contains("min")) level = 10;
            else {
                // Try to extract a number
                java.util.regex.Matcher m = java.util.regex.Pattern.compile("\\d+").matcher(lower);
                if (m.find()) level = Integer.parseInt(m.group());
            }
            Runtime.getRuntime().exec(new String[]{"powershell", "-Command",
                    "(Get-WmiObject -Namespace root/WMI -Class WmiMonitorBrightnessMethods).WmiSetBrightness(1," + level + ")"});
            callback.onMessage("☀️ System", "Brightness set to " + level + "%.");
        } else if (os.contains("mac")) {
            if (lower.contains("up"))   Runtime.getRuntime().exec(new String[]{"osascript", "-e", "tell application \"System Events\" to key code 144"});
            else if (lower.contains("down")) Runtime.getRuntime().exec(new String[]{"osascript", "-e", "tell application \"System Events\" to key code 145"});
            callback.onMessage("☀️ System", "Brightness adjusted.");
        } else {
            callback.onMessage("⚠️ System", "Brightness control is supported on Windows and Mac only.");
        }
    }

    private void takeScreenshot(ChatCallback callback) {
        new Thread(() -> {
            try {
                Robot robot = new Robot();
                Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
                BufferedImage screenshot = robot.createScreenCapture(screenRect);

                String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                File outputFile = new File(System.getProperty("user.home") + File.separator
                        + "Desktop" + File.separator + "screenshot_" + timestamp + ".png");
                ImageIO.write(screenshot, "PNG", outputFile);

                callback.onMessage("📸 System", "Screenshot saved to Desktop:\n" + outputFile.getName());
            } catch (Exception e) {
                callback.onMessage("❌ Error", "Screenshot failed: " + e.getMessage());
            }
        }).start();
    }

    private void handleShutdown(String os, ChatCallback callback) throws Exception {
        callback.onMessage("⚠️ System", "Shutting down in 10 seconds... Close your work!");
        if (os.contains("win")) Runtime.getRuntime().exec("shutdown /s /t 10");
        else if (os.contains("mac")) Runtime.getRuntime().exec(new String[]{"osascript", "-e", "tell app \"System Events\" to shut down"});
        else Runtime.getRuntime().exec("shutdown -h +1");
    }

    private void handleRestart(String os, ChatCallback callback) throws Exception {
        callback.onMessage("🔄 System", "Restarting in 10 seconds...");
        if (os.contains("win")) Runtime.getRuntime().exec("shutdown /r /t 10");
        else if (os.contains("mac")) Runtime.getRuntime().exec(new String[]{"osascript", "-e", "tell app \"System Events\" to restart"});
        else Runtime.getRuntime().exec("shutdown -r +1");
    }

    private void handleLock(String os, ChatCallback callback) throws Exception {
        if (os.contains("win")) Runtime.getRuntime().exec("rundll32.exe user32.dll,LockWorkStation");
        else if (os.contains("mac")) Runtime.getRuntime().exec(new String[]{"osascript", "-e",
                "tell application \"System Events\" to keystroke \"q\" using {command down, control down}"});
        else Runtime.getRuntime().exec("gnome-screensaver-command -l");
        callback.onMessage("🔒 System", "Screen locked.");
    }

    private void handleSleep(String os, ChatCallback callback) throws Exception {
        if (os.contains("win")) Runtime.getRuntime().exec("rundll32.exe powrprof.dll,SetSuspendState 0,1,0");
        else if (os.contains("mac")) Runtime.getRuntime().exec(new String[]{"osascript", "-e", "tell app \"System Events\" to sleep"});
        callback.onMessage("💤 System", "Going to sleep...");
    }

    private void emptyRecycleBin(String os, ChatCallback callback) throws Exception {
        if (os.contains("win")) {
            Runtime.getRuntime().exec(new String[]{"powershell", "-Command", "Clear-RecycleBin -Force"});
            callback.onMessage("🗑️ System", "Recycle Bin emptied.");
        } else if (os.contains("mac")) {
            Runtime.getRuntime().exec(new String[]{"osascript", "-e", "tell application \"Finder\" to empty trash"});
            callback.onMessage("🗑️ System", "Trash emptied.");
        }
    }
}