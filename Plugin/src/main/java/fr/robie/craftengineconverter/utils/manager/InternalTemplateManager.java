package fr.robie.craftengineconverter.utils.manager;

import fr.robie.craftengineconverter.CraftEngineConverter;
import fr.robie.craftengineconverter.common.format.Message;
import fr.robie.craftengineconverter.common.logger.LogType;
import fr.robie.craftengineconverter.common.logger.Logger;
import fr.robie.craftengineconverter.common.utils.enums.Template;
import fr.robie.craftengineconverter.common.utils.enums.TemplatesCommands;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InternalTemplateManager {
    private static final char PLACEHOLDER_PREFIX = '$';
    private static final char PLACEHOLDER_OPEN = '{';
    private static final char PLACEHOLDER_CLOSE = '}';

    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile(
        Pattern.quote(String.valueOf(PLACEHOLDER_PREFIX)) +
            Pattern.quote(String.valueOf(PLACEHOLDER_OPEN)) +
            "([^" + Pattern.quote(String.valueOf(PLACEHOLDER_CLOSE)) + "]+)" +
            Pattern.quote(String.valueOf(PLACEHOLDER_CLOSE))
    );

    private static final Map<Template, YamlConfiguration> templates = new HashMap<>();
    private final CraftEngineConverter craftEngineConverter;

    public InternalTemplateManager(CraftEngineConverter craftEngineConverter) {
        this.craftEngineConverter = craftEngineConverter;
    }

    public void loadTemplates() {
        for (Template template : Template.values()) {
            try {
                InputStream inputStream = this.craftEngineConverter.getResource(template.getPath() + ".yml");
                if (inputStream == null) {
                    Logger.info(Message.WARNING__TEMPLATE_MANAGER__MISSING_TEMPLATE,LogType.ERROR, "template_name",template.name());
                    continue;
                }

                YamlConfiguration yamlConfiguration = YamlConfiguration.loadConfiguration(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
                templates.put(template, yamlConfiguration);
            } catch (Exception ex) {
                Logger.debug(Message.WARNING__TEMPLATE_MANAGER__ERROR_LOADING_TEMPLATE, LogType.ERROR, "template_name", template.name());
                Logger.showException("loading template " + template.name(), ex);
            }
        }
    }


    public static @Nullable Map<String, Object> getTemplate(Template template) {
        YamlConfiguration yamlConfiguration = templates.get(template);
        if (yamlConfiguration == null) return null;

        String yamlString = yamlConfiguration.saveToString();
        YamlConfiguration copy = YamlConfiguration.loadConfiguration(new java.io.StringReader(yamlString));
        return convertConfigurationSectionToMap(copy);
    }

    private static Map<String, Object> convertConfigurationSectionToMap(ConfigurationSection section) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (String key : section.getKeys(false)) {
            Object value = section.get(key);
            if (value instanceof ConfigurationSection configurationSection) {
                result.put(key, convertConfigurationSectionToMap(configurationSection));
            } else if (value instanceof List<?> lst) {
                result.put(key, convertList(lst));
            } else {
                result.put(key, value);
            }
        }
        return result;
    }

    private static List<Object> convertList(List<?> list) {
        List<Object> result = new ArrayList<>();
        for (Object item : list) {
            if (item instanceof ConfigurationSection configurationSection) {
                result.add(convertConfigurationSectionToMap(configurationSection));
            } else if (item instanceof List<?> lst) {
                result.add(convertList(lst));
            } else {
                result.add(item);
            }
        }
        return result;
    }

    public static @NotNull Map<String, Object> parseTemplate(Template template, Object... args) {
        if (args.length % 2 != 0) {
            Logger.debug(Message.WARNING__TEMPLATE_MANAGER__INVALID_ARGS_NUMBER, "template_name", template.name(), "args_length", args.length);
            return new LinkedHashMap<>();
        }

        Map<String, Object> replacements = new LinkedHashMap<>();
        for (int i = 0; i < args.length; i += 2) {
            replacements.put(String.valueOf(args[i]), args[i + 1]);
        }

        Map<String, Object> templateMap = getTemplate(template);
        if (templateMap == null) {
            return new LinkedHashMap<>();
        }

        return parseValues(templateMap, replacements);
    }

    @SuppressWarnings({"unchecked"})
    private static Object parseObject(Object obj, Map<String, Object> replacements) {
        return switch (obj) {
            case String string -> parseStringValue(string, replacements);
            case Map<?, ?> map -> parseValues((Map<String, Object>) map, replacements);
            case List<?> list -> parseList(list, replacements);
            case null, default -> obj;
        };
    }

    private static Map<String, Object> parseValues(Map<String, Object> templatesMap, Map<String, Object> replacements) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : templatesMap.entrySet()) {
            Object parsedKey = parseStringValue(entry.getKey(), replacements);
            result.put(parsedKey instanceof String string ? string : String.valueOf(parsedKey),
                    parseObject(entry.getValue(), replacements));
        }
        return result;
    }

    private static List<Object> parseList(List<?> list, Map<String, Object> replacements) {
        List<Object> result = new ArrayList<>();
        for (Object item : list) {
            result.add(parseObject(item, replacements));
        }
        return result;
    }

    private static Object parseStringValue(String str, Map<String, Object> replacements) {
        Object directReplacement = replacements.get(str);
        if (directReplacement != null) {
            return directReplacement;
        }

        List<Object> components = new ArrayList<>();
        int lastIndex = 0;

        Matcher matcher = PLACEHOLDER_PATTERN.matcher(str);

        while (matcher.find()) {
            if (matcher.start() > lastIndex) {
                String before = str.substring(lastIndex, matcher.start());
                components.add(replaceSimplePlaceholders(before, replacements));
            }

            String directiveContent = matcher.group(1);
            String[] parts = directiveContent.split(":", 2);

            if (parts.length == 2) {
                TemplatesCommands command = TemplatesCommands.fromString(parts[0].trim());
                if (command != null) {
                    try {
                        Object result = command.execute(parts[1], replacements);
                        components.add(result);
                    } catch (Exception e) {
                        Logger.debug("Error executing command " + command + ": " + e.getMessage());
                        components.add(buildPlaceholderString(directiveContent));
                    }
                } else {
                    components.add(buildPlaceholderString(directiveContent));
                }
            } else {
                components.add(buildPlaceholderString(directiveContent));
            }

            lastIndex = matcher.end();
        }

        if (lastIndex < str.length()) {
            String after = str.substring(lastIndex);
            components.add(replaceSimplePlaceholders(after, replacements));
        }

        if (components.isEmpty()) {
            return replaceSimplePlaceholders(str, replacements);
        }

        if (components.size() == 1) {
            return components.getFirst();
        }

        return combineComponents(components);
    }

    private static String buildPlaceholderString(String content) {
        return String.valueOf(PLACEHOLDER_PREFIX) +
                PLACEHOLDER_OPEN +
                content +
                PLACEHOLDER_CLOSE;
    }

    private static Object combineComponents(List<Object> components) {
        boolean allStrings = true;
        for (Object component : components) {
            if (!(component instanceof String)) {
                allStrings = false;
                break;
            }
        }

        if (allStrings) {
            StringBuilder result = new StringBuilder();
            for (Object component : components) {
                result.append(component);
            }
            return result.toString();
        }

        if (components.size() == 2) {
            Object first = components.get(0);
            Object second = components.get(1);

            if (first instanceof Map && second instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> merged = new LinkedHashMap<>((Map<String, Object>) first);
                @SuppressWarnings("unchecked")
                Map<String, Object> toAppend = (Map<String, Object>) second;

                merged.putAll(toAppend);
                return merged;
            }
        }

        boolean allMaps = true;
        for (Object component : components) {
            if (!(component instanceof Map)) {
                allMaps = false;
                break;
            }
        }

        if (allMaps) {
            Map<String, Object> merged = new LinkedHashMap<>();
            for (Object component : components) {
                @SuppressWarnings("unchecked")
                Map<String, Object> map = (Map<String, Object>) component;
                merged.putAll(map);
            }
            return merged;
        }

        return components;
    }

    private static String replaceSimplePlaceholders(String str, Map<String, Object> replacements) {
        String result = str;
        for (Map.Entry<String, Object> replacement : replacements.entrySet()) {
            if (replacement.getValue() != null) {
                result = result.replace(replacement.getKey(), String.valueOf(replacement.getValue()));
            }
        }
        return result;
    }
}