package fr.robie.craftengineconverter.common.items;

import fr.robie.craftengineconverter.common.utils.ItemConfigurationSerializable;
import org.bukkit.DyeColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

public class GlowDropColor implements ItemConfigurationSerializable {
    private final DyeColor color;

    public GlowDropColor(@NotNull DyeColor color) {
        this.color = color;
    }

    @Override
    public void serialize(@NotNull YamlConfiguration yamlConfiguration, @NotNull String path, @NotNull ConfigurationSection itemSection, @NotNull String itemId) {
        getOrCreateSection(itemSection, "settings").set("glow-color", this.color.name().toLowerCase());
    }
}
