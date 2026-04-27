package org.example.features;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TextToSpeech {

    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private static volatile boolean enabled = false;
    private static volatile Process currentProcess = null;

    public static void setEnabled(boolean value) {
        enabled = value;
        if (!enabled) {
            stop();
        }
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void speak(String text) {
        if (!enabled || text == null || text.trim().isEmpty()) {
            return;
        }

        executor.submit(() -> {
            if (!enabled) return;

            try {
                stop();

                String cleaned = text
                        .replace("\r", " ")
                        .replace("\n", " ")
                        .replaceAll("\\s+", " ")
                        .trim();

                if (cleaned.isEmpty()) return;

                String os = System.getProperty("os.name").toLowerCase();

                if (os.contains("win")) {
                    String escaped = cleaned
                            .replace("'", "''")
                            .replace("\"", "");

                    String command =
                            "Add-Type -AssemblyName System.Speech; " +
                                    "$speaker = New-Object System.Speech.Synthesis.SpeechSynthesizer; " +
                                    "$speaker.Volume = 100; " +
                                    "$speaker.Rate = 0; " +
                                    "$speaker.Speak('" + escaped + "');";

                    ProcessBuilder pb = new ProcessBuilder(
                            "powershell.exe",
                            "-NoProfile",
                            "-ExecutionPolicy", "Bypass",
                            "-Command",
                            command
                    );

                    pb.redirectErrorStream(true);
                    currentProcess = pb.start();
                    currentProcess.waitFor();
                    currentProcess = null;
                }
            } catch (Exception e) {
                e.printStackTrace();
                currentProcess = null;
            }
        });
    }

    public static void stop() {
        try {
            if (currentProcess != null && currentProcess.isAlive()) {
                currentProcess.destroyForcibly();
            }
        } catch (Exception ignored) {
        } finally {
            currentProcess = null;
        }
    }
}