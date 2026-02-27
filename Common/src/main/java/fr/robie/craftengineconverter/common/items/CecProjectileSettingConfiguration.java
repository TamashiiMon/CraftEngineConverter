package fr.robie.craftengineconverter.common.items;

import fr.robie.craftengineconverter.common.utils.ItemConfigurationSerializable;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class CecProjectileSettingConfiguration implements ItemConfigurationSerializable {
    private final Map<String, Object> settings;

    public CecProjectileSettingConfiguration(Map<String, Object> settings) {
        this.settings = settings;
    }

    @Override
    public void serialize(@NotNull YamlConfiguration yamlConfiguration, @NotNull String path, @NotNull ConfigurationSection itemSection, @NotNull String itemId) {
        ConfigurationSection settings = getOrCreateSection(itemSection, "settings");
        settings.set("projectile", this.settings);
    }
}
