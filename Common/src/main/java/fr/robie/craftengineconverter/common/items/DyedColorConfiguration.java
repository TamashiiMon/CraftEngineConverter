package fr.robie.craftengineconverter.common.items;

import fr.robie.craftengineconverter.common.utils.ItemConfigurationSerializable;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

public class DyedColorConfiguration implements ItemConfigurationSerializable {
    private final Object color;

    public DyedColorConfiguration(Object color) {
        this.color = color;
    }

    @Override
    public void serialize(@NotNull YamlConfiguration yamlConfiguration, @NotNull String path, @NotNull ConfigurationSection itemSection, @NotNull String itemId) {
        getOrCreateSection(itemSection, "data").set("dyed-color", this.color);
    }
}
