package org.example.features;
import org.example.AppConfig;

import com.google.gson.*;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GeminiFeature implements Feature {

    private static final String GEMINI_API_KEY = AppConfig.GEMINI_API_KEY;
    // gemini-1.5-flash: 15 RPM, 1500 req/day FREE — much higher than 2.0-flash free tier
    private static final String GEMINI_API_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent";

    private static final int  MAX_RETRIES   = 3;
    private static final long BASE_RETRY_MS = 5000;

    private final List<JsonObject> conversationHistory = new ArrayList<>();

    private static final String SYSTEM_PROMPT =
            "You are Assistant — an intelligent, helpful, and friendly desktop assistant. " +
                    "You help users control their PC, answer questions, and assist with tasks. " +
                    "Be concise and clear. Use emojis occasionally to be friendly. " +
                    "When asked about system operations, remind the user they can use voice/text commands directly.";

    @Override
    public boolean canHandle(String input) { return true; }

    @Override
    public void execute(String input, ChatCallback callback) {
        callback.onMessage("🤖 Desktop Assitant", "Thinking...");

        new Thread(() -> {
            try {
                if (GEMINI_API_KEY == null || GEMINI_API_KEY.isEmpty()) {
                    callback.onMessage("🤖 Desktop Assitant",
                            "I need my API key! Please set the GEMINI_API_KEY environment variable.\n\n" +
                                    "How to set it:\n" +
                                    "  Windows: setx GEMINI_API_KEY \"your-key-here\"\n" +
                                    "  Mac/Linux: export GEMINI_API_KEY=\"your-key-here\"\n\n" +
                                    "Get a free key at: https://aistudio.google.com/app/apikey");
                    return;
                }

                // Add user message to history
                JsonObject userContent = new JsonObject();
                userContent.addProperty("role", "user");
                JsonArray userParts = new JsonArray();
                JsonObject userPart = new JsonObject();
                userPart.addProperty("text", input);
                userParts.add(userPart);
                userContent.add("parts", userParts);
                conversationHistory.add(userContent);

                int attempt = 0;
                while (attempt <= MAX_RETRIES) {

                    CloseableHttpClient client = HttpClients.createDefault();
                    HttpPost post = new HttpPost(GEMINI_API_URL + "?key=" + GEMINI_API_KEY);
                    post.setHeader("Content-Type", "application/json");

                    // Build request
                    JsonObject requestBody = new JsonObject();

                    JsonObject sysInstruct = new JsonObject();
                    JsonArray  sysParts    = new JsonArray();
                    JsonObject sysPart     = new JsonObject();
                    sysPart.addProperty("text", SYSTEM_PROMPT);
                    sysParts.add(sysPart);
                    sysInstruct.add("parts", sysParts);
                    requestBody.add("system_instruction", sysInstruct);

                    JsonArray contents = new JsonArray();
                    int start = Math.max(0, conversationHistory.size() - 10);
                    for (int i = start; i < conversationHistory.size(); i++) {
                        contents.add(conversationHistory.get(i));
                    }
                    requestBody.add("contents", contents);

                    JsonObject genConfig = new JsonObject();
                    genConfig.addProperty("temperature", 0.7);
                    genConfig.addProperty("maxOutputTokens", 1024);
                    requestBody.add("generationConfig", genConfig);

                    post.setEntity(new StringEntity(requestBody.toString(), "UTF-8"));

                    String responseBody = EntityUtils.toString(
                            client.execute(post).getEntity(), "UTF-8");
                    JsonObject jsonResponse = JsonParser.parseString(responseBody).getAsJsonObject();

                    // Error handling
                    if (jsonResponse.has("error")) {
                        String errMsg = jsonResponse.getAsJsonObject("error")
                                .get("message").getAsString();

                        boolean isRateLimit = errMsg.contains("quota")
                                || errMsg.contains("rate")
                                || errMsg.contains("429")
                                || errMsg.contains("retry");

                        if (isRateLimit && attempt < MAX_RETRIES) {
                            long waitMs = BASE_RETRY_MS;
                            Matcher m = Pattern.compile("retry in ([\\d.]+)s").matcher(errMsg);
                            if (m.find()) {
                                waitMs = (long)(Double.parseDouble(m.group(1)) * 1000) + 1000;
                            }
                            long waitSecs = waitMs / 1000;
                            callback.onMessage("⏳ Desktop Assitant",
                                    "Rate limit hit — auto-retrying in " + waitSecs +
                                            "s... (attempt " + (attempt + 1) + "/" + MAX_RETRIES + ")");
                            Thread.sleep(waitMs);
                            attempt++;
                            continue;
                        }

                        callback.onMessage("❌ API Error",
                                "Gemini quota exceeded.\n\n" +
                                        "Quick fixes:\n" +
                                        "  1. Wait ~1 minute (free tier: 15 req/min)\n" +
                                        "  2. Get a new key: https://aistudio.google.com/app/apikey\n" +
                                        "  3. Check limits: https://ai.dev/rate-limit\n\n" +
                                        "Detail: " + errMsg);
                        conversationHistory.remove(conversationHistory.size() - 1);
                        return;
                    }

                    // Success
                    if (jsonResponse.has("candidates")) {
                        String aiResponse = jsonResponse.getAsJsonArray("candidates")
                                .get(0).getAsJsonObject()
                                .getAsJsonObject("content")
                                .getAsJsonArray("parts")
                                .get(0).getAsJsonObject()
                                .get("text").getAsString();

                        JsonObject assistantContent = new JsonObject();
                        assistantContent.addProperty("role", "model");
                        JsonArray asstParts = new JsonArray();
                        JsonObject asstPart = new JsonObject();
                        asstPart.addProperty("text", aiResponse);
                        asstParts.add(asstPart);
                        assistantContent.add("parts", asstParts);
                        conversationHistory.add(assistantContent);

                        callback.onMessage("🤖 Desktop Assitant", aiResponse);
                        return;
                    } else {
                        callback.onMessage("❌ Parser Error",
                                "Unexpected format from Gemini:\n" + responseBody);
                        return;
                    }
                }

                callback.onMessage("❌ Desktop Assitant",
                        "Max retries (" + MAX_RETRIES + ") reached. Please wait a minute and try again.");

            } catch (Exception e) {
                callback.onMessage("❌ AI Error", "Connection failed: " + e.getMessage());
            }
        }).start();
    }

    public void clearHistory() {
        conversationHistory.clear();
    }
}