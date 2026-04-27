package org.example;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.List;

public class chat {

    // This list will store the entire conversation history
    private static List<String> history = new ArrayList<>();

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("--- Gemini 2.5 Chatbot Initialized (Type 'exit' to quit) ---");

        while (true) {
            System.out.print("You: ");
            String userInput = scanner.nextLine();

            if (userInput.equalsIgnoreCase("exit")) {
                System.out.println("Goodbye!");
                break;
            }

            String response = geminiChat(userInput);
            System.out.println("Gemini: " + response);
            System.out.println("---------------------------------------------------------");
        }
        scanner.close();
    }

    static String geminiChat(String message) {
    try {
        String apiKey = System.getenv("GEMINI_API_KEY");
        String urlStr = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + apiKey;

        // 1. Properly escape quotes and newlines in user message
        String escapedUserMsg = message.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
        history.add("{\"role\": \"user\", \"parts\": [{\"text\": \"" + escapedUserMsg + "\"}]}");

        String body = "{\"contents\": [" + String.join(",", history) + "]}";

        HttpURLConnection con = (HttpURLConnection) URI.create(urlStr).toURL().openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");
        con.setDoOutput(true);

        try (OutputStream os = con.getOutputStream()) {
            os.write(body.getBytes(StandardCharsets.UTF_8));
        }

        if (con.getResponseCode() != 200) {
            // Read error stream to see EXACTLY why it failed (very helpful for 400 errors)
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(con.getErrorStream()))) {
                System.err.println("API Error Details: " + reader.readLine());
            }
            return "Error: " + con.getResponseCode();
        }

        StringBuilder fullResponse = new StringBuilder();
        StringBuilder rawResponse = new StringBuilder();
        
        try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                rawResponse.append(line);
                if (line.contains("\"text\":")) {
                    String extracted = line.split("\"text\":")[1].split("\"")[1];
                    fullResponse.append(extracted);
                }
            }
        }

        String botText = fullResponse.toString();
        
        // 2. CRITICAL: Check if a thought_signature exists in the raw JSON
        String signaturePart = "";
        if (rawResponse.toString().contains("\"thought_signature\":")) {
            String sig = rawResponse.toString().split("\"thought_signature\":")[1].split("\"")[1];
            signaturePart = ", \"thought_signature\": \"" + sig + "\"";
        }

        // 3. Add model response back to history with the signature if present
        String escapedBotText = botText.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
        history.add("{\"role\": \"model\", \"parts\": [{\"text\": \"" + escapedBotText + "\"}]}" + signaturePart);
        
        return botText;

    } catch (Exception e) {
        return "Exception: " + e.getMessage();
    }
}
}