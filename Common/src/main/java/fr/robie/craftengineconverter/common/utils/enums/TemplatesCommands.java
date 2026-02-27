package fr.robie.craftengineconverter.common.utils.enums;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public enum TemplatesCommands {
    APPEND((args, replacements) -> {
        Map<String, Object> result = new LinkedHashMap<>();

        String[] pairs = args.split(",");
        for (String pair : pairs) {
            String[] keyValue = pair.split(":", 2);
            if (keyValue.length == 2) {
                String key = replaceInString(keyValue[0].trim(), replacements);
                String valueStr = keyValue[1].trim();
                Object value = parseValue(valueStr, replacements);
                result.put(key, value);
            }
        }

        return result;
    }),

    LIST((args, replacements) -> {
        List<Object> result = new ArrayList<>();
        String[] items = args.split(",");

        for (String item : items) {
            String trimmed = item.trim();
            result.add(parseValue(trimmed, replacements));
        }

        return result;
    }),
    MAP(APPEND::execute),

    MERGE((args, replacements) -> {
        Map<String, Object> result = new LinkedHashMap<>();
        String[] placeholders = args.split(",");

        for (String placeholder : placeholders) {
            String trimmed = placeholder.trim();
            Object value = replacements.get(trimmed);

            if (value instanceof Map<?, ?> map) {
                map.forEach((k, v) -> result.put(String.valueOf(k), v));
            }
        }

        return result;
    }),

    IF((args, replacements) -> {
        String[] parts = args.split("\\?", 2);
        if (parts.length != 2) return args;

        String condition = parts[0].trim();
        String[] values = parts[1].split(":", 2);
        if (values.length != 2) return args;

        Object conditionValue = replacements.get(condition);
        boolean isTrue = conditionValue != null &&
                (conditionValue.equals(true) ||
                        conditionValue.equals("true") ||
                        (conditionValue instanceof Number && ((Number)conditionValue).doubleValue() != 0));

        String selectedValue = isTrue ? values[0].trim() : values[1].trim();
        return parseValue(selectedValue, replacements);
    }),

    RANGE((args, replacements) -> {
        String[] parts = args.split(":");
        if (parts.length < 2) return new ArrayList<>();

        try {
            int start = Integer.parseInt(replaceInString(parts[0].trim(), replacements));
            int end = Integer.parseInt(replaceInString(parts[1].trim(), replacements));
            int step = parts.length > 2 ? Integer.parseInt(replaceInString(parts[2].trim(), replacements)) : 1;

            List<Integer> result = new ArrayList<>();
            if (step > 0) {
                for (int i = start; i <= end; i += step) {
                    result.add(i);
                }
            } else if (step < 0) {
                for (int i = start; i >= end; i += step) {
                    result.add(i);
                }
            }
            return result;
        } catch (NumberFormatException e) {
            return new ArrayList<>();
        }
    }),

    CONCAT((args, replacements) -> {
        String[] parts = args.split(",");
        StringBuilder result = new StringBuilder();

        for (String part : parts) {
            String trimmed = part.trim();
            Object value = parseValue(trimmed, replacements);
            result.append(value);
        }

        return result.toString();
    }),

    DEFAULT((args, replacements) -> {
        String[] parts = args.split(":", 2);
        if (parts.length != 2) return args;

        String placeholder = parts[0].trim();
        String defaultValue = parts[1].trim();

        Object value = replacements.get(placeholder);
        if (value == null || (value instanceof String str && str.isEmpty())) {
            return parseValue(defaultValue, replacements);
        }

        return value;
    });

    private final TemplatesCommandsFunctionalInterface function;

    TemplatesCommands(TemplatesCommandsFunctionalInterface function) {
        this.function = function;
    }

    public Object execute(String args, Map<String, Object> replacements) {
        if (function == null) {
            return "${" + name().toLowerCase() + ":" + args + "}";
        }
        return function.apply(args, replacements);
    }

    private static Object parseValue(String valueStr, Map<String, Object> replacements) {
        if (valueStr.startsWith("%") && valueStr.endsWith("%")) {
            return replacements.getOrDefault(valueStr, valueStr);
        }

        if (valueStr.contains("${")) {
            return parseStringWithDirectives(valueStr, replacements);
        }

        try {
            if (valueStr.equalsIgnoreCase("true")) return true;
            if (valueStr.equalsIgnoreCase("false")) return false;
            if (valueStr.contains(".")) {
                return Double.parseDouble(valueStr);
            } else {
                return Integer.parseInt(valueStr);
            }
        } catch (NumberFormatException e) {
            return replaceInString(valueStr, replacements);
        }
    }

    private static String replaceInString(String str, Map<String, Object> replacements) {
        String result = str;
        for (Map.Entry<String, Object> entry : replacements.entrySet()) {
            if (entry.getValue() != null) {
                result = result.replace(entry.getKey(), String.valueOf(entry.getValue()));
            }
        }
        return result;
    }

    private static Object parseStringWithDirectives(String str, Map<String, Object> replacements) {
        return str;
    }

    public static TemplatesCommands fromString(String command) {
        try {
            return valueOf(command.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}