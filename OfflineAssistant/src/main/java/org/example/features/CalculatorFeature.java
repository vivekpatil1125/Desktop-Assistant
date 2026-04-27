package org.example.features;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.util.regex.*;

public class CalculatorFeature implements Feature {

    @Override
    public boolean canHandle(String input) {
        String lower = input.toLowerCase();
        return lower.startsWith("calculate ") || lower.startsWith("compute ")
                || lower.startsWith("what is ") && containsMath(lower)
                || lower.startsWith("solve ") || lower.startsWith("eval ")
                || containsDirectMath(lower);
    }

    private boolean containsMath(String lower) {
        return lower.matches(".*[\\d]+.*[+\\-*/^%].*[\\d]+.*") ||
                lower.contains("plus") || lower.contains("minus") ||
                lower.contains("times") || lower.contains("divided") ||
                lower.contains("percent") || lower.contains("squared");
    }

    private boolean containsDirectMath(String lower) {
        return lower.matches("^[\\d\\s+\\-*/().^%]+$") && lower.matches(".*\\d.*[+\\-*/].*\\d.*");
    }

    @Override
    public void execute(String input, ChatCallback callback) {
        String expr = prepareExpression(input);

        try {
            // Use Java's built-in script engine to evaluate math
            ScriptEngineManager manager = new ScriptEngineManager();
            ScriptEngine engine = manager.getEngineByName("JavaScript");

            if (engine == null) {
                // Fallback: simple evaluator
                double result = evalSimple(expr);
                callback.onMessage("🧮 Calculator", expr + " = " + formatResult(result));
                return;
            }

            Object result = engine.eval(expr);
            callback.onMessage("🧮 Calculator", input.trim() + "\n= " + result);
        } catch (Exception e) {
            callback.onMessage("❌ Calculator", "Could not evaluate: \"" + expr + "\"\nMake sure it's a valid math expression.");
        }
    }

    private String prepareExpression(String input) {
        String expr = input.toLowerCase()
                .replaceAll("^(calculate|compute|what is|solve|eval)\\s+", "")
                .replaceAll("\\bplus\\b", "+")
                .replaceAll("\\bminus\\b", "-")
                .replaceAll("\\btimes\\b", "*")
                .replaceAll("\\bmultiplied by\\b", "*")
                .replaceAll("\\bdivided by\\b", "/")
                .replaceAll("\\bover\\b", "/")
                .replaceAll("\\bsquared\\b", "**2")
                .replaceAll("\\bcubed\\b", "**3")
                .replaceAll("\\bto the power of\\b", "**")
                .replaceAll("\\bpercent of\\b", "/100*")
                .replaceAll("\\bmod\\b", "%")
                .trim();
        return expr;
    }

    private double evalSimple(String expr) {
        // Very basic left-to-right evaluator for + and -
        expr = expr.trim();
        String[] addParts = expr.split("\\+");
        double sum = 0;
        for (String part : addParts) {
            String[] subParts = part.trim().split("-");
            double sub = Double.parseDouble(subParts[0].trim());
            for (int i = 1; i < subParts.length; i++) sub -= Double.parseDouble(subParts[i].trim());
            sum += sub;
        }
        return sum;
    }

    private String formatResult(double result) {
        if (result == Math.floor(result)) return String.valueOf((long) result);
        return String.format("%.4f", result);
    }
}