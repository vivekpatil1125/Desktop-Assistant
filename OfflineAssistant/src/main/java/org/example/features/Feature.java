package org.example.features;

public interface Feature {
    boolean canHandle(String input);
    void execute(String input, ChatCallback callback);
}