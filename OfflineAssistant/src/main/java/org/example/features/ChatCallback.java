package org.example.features;

@FunctionalInterface
public interface ChatCallback {
    void onMessage(String sender, String message);
}