package fr.robie.craftengineconverter.common;

import fr.robie.craftengineconverter.common.enums.Plugins;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PluginNameMapper {
    private static final PluginNameMapper INSTANCE = new PluginNameMapper();
    private final Map<String, Map<String, String>> mappings = new ConcurrentHashMap<>();

    private PluginNameMapper() {}

    public boolean hasMapping(Plugins plugin, String oldName) {
        Map<String, String> pluginMappings = mappings.get(plugin.name());
        return pluginMappings != null && pluginMappings.containsKey(oldName);
    }

    @Nullable
    public String getNewName(Plugins plugin, String oldName) {
        Map<String, String> pluginMappings = mappings.get(plugin.name());
        return pluginMappings != null ? pluginMappings.get(oldName) : null;
    }

    public void storeMapping(Plugins plugin, String oldName, String newName) {
        mappings.computeIfAbsent(plugin.name(), k -> new ConcurrentHashMap<>())
                .put(oldName, newName);
    }

    public static PluginNameMapper getInstance() {
        return INSTANCE;
    }

    public void clearMappingsForPlugin(Plugins plugins) {
        mappings.remove(plugins.name());
    }
}