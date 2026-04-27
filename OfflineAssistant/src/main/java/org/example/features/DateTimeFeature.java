package org.example.features;

import java.time.*;
import java.time.format.*;
import java.util.Locale;

public class DateTimeFeature implements Feature {

    @Override
    public boolean canHandle(String input) {
        String lower = input.toLowerCase();
        return lower.contains("what time") || lower.contains("current time") || lower.equals("time")
                || lower.contains("what date") || lower.contains("today's date") || lower.equals("date")
                || lower.contains("what day") || lower.contains("day today")
                || lower.contains("what year") || lower.contains("current year")
                || lower.contains("what month") || lower.contains("current month");
    }

    @Override
    public void execute(String input, ChatCallback callback) {
        String lower = input.toLowerCase();
        LocalDateTime now = LocalDateTime.now();

        if (lower.contains("time")) {
            String time = now.format(DateTimeFormatter.ofPattern("hh:mm:ss a"));
            callback.onMessage("🕐 Date & Time", "Current time: " + time);
        } else if (lower.contains("date")) {
            String date = now.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy", Locale.ENGLISH));
            callback.onMessage("📅 Date & Time", "Today is: " + date);
        } else if (lower.contains("day")) {
            String day = now.format(DateTimeFormatter.ofPattern("EEEE", Locale.ENGLISH));
            callback.onMessage("📅 Date & Time", "Today is: " + day);
        } else if (lower.contains("year")) {
            callback.onMessage("📅 Date & Time", "Current year: " + now.getYear());
        } else if (lower.contains("month")) {
            String month = now.format(DateTimeFormatter.ofPattern("MMMM", Locale.ENGLISH));
            callback.onMessage("📅 Date & Time", "Current month: " + month);
        } else {
            String full = now.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy  •  hh:mm:ss a", Locale.ENGLISH));
            callback.onMessage("🕐 Date & Time", full);
        }
    }
}