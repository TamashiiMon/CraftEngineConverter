package fr.robie.craftengineconverter.common.items;

import fr.robie.craftengineconverter.common.utils.ItemConfigurationSerializable;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

public class GliderConfiguration implements ItemConfigurationSerializable {
    private final boolean hasGlider;

    public GliderConfiguration(boolean hasGlider) {
        this.hasGlider = hasGlider;
    }

    @Override
    public void serialize(@NotNull YamlConfiguration yamlConfiguration, @NotNull String path, @NotNull ConfigurationSection itemSection, @NotNull String itemId) {
        if (!this.hasGlider) return;
        ConfigurationSection components = getOrCreateSection(itemSection, "components");
        components.set("minecraft:glider", true);
    }
}
