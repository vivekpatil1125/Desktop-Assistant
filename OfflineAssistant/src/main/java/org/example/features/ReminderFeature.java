package org.example.features;

import javax.swing.*;
import java.util.concurrent.*;
import java.util.regex.*;

public class ReminderFeature implements Feature {

    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);

    @Override
    public boolean canHandle(String input) {
        String lower = input.toLowerCase();
        return lower.startsWith("remind me") || lower.startsWith("set reminder")
                || lower.startsWith("set alarm") || lower.startsWith("alarm in")
                || lower.startsWith("remind in");
    }

    @Override
    public void execute(String input, ChatCallback callback) {
        // Patterns: "remind me in 5 minutes to call John"
        //           "set reminder in 1 hour for meeting"
        //           "remind me after 30 seconds to check email"
        String lower = input.toLowerCase();

        long delaySeconds = parseDelay(lower);
        String message = parseMessage(input);

        if (delaySeconds <= 0) {
            callback.onMessage("⏰ Reminder", "I couldn't understand the time. Try:\n" +
                    "• \"Remind me in 5 minutes to call John\"\n" +
                    "• \"Set reminder in 1 hour for meeting\"\n" +
                    "• \"Remind me in 30 seconds to check oven\"");
            return;
        }

        String timeLabel = formatDelay(delaySeconds);
        callback.onMessage("⏰ Reminder", "✅ Reminder set for " + timeLabel + "!\nMessage: " + message);

        final String finalMessage = message;
        scheduler.schedule(() -> {
            callback.onMessage("🔔 REMINDER", "⏰ " + finalMessage);
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(null,
                        finalMessage,
                        "⏰ Reminder!",
                        JOptionPane.INFORMATION_MESSAGE);
            });
        }, delaySeconds, TimeUnit.SECONDS);
    }

    private long parseDelay(String lower) {
        Pattern p = Pattern.compile("(\\d+)\\s*(second|sec|minute|min|hour|hr)s?");
        Matcher m = p.matcher(lower);
        if (m.find()) {
            long amount = Long.parseLong(m.group(1));
            String unit = m.group(2);
            if (unit.startsWith("sec")) return amount;
            if (unit.startsWith("min")) return amount * 60;
            if (unit.startsWith("hour") || unit.startsWith("hr")) return amount * 3600;
        }
        return -1;
    }

    private String parseMessage(String input) {
        String msg = input
                .replaceAll("(?i)remind(\\s+me)?\\s+(in|after|every)\\s+\\d+\\s+\\w+", "")
                .replaceAll("(?i)set\\s+(reminder|alarm)\\s+(in|after)\\s+\\d+\\s+\\w+", "")
                .replaceAll("(?i)\\b(to|for|about|that)\\b", "")
                .trim();
        return msg.isEmpty() ? "Reminder!" : msg;
    }

    private String formatDelay(long seconds) {
        if (seconds < 60) return seconds + " second" + (seconds != 1 ? "s" : "");
        if (seconds < 3600) {
            long mins = seconds / 60;
            return mins + " minute" + (mins != 1 ? "s" : "");
        }
        long hours = seconds / 3600;
        return hours + " hour" + (hours != 1 ? "s" : "");
    }
}