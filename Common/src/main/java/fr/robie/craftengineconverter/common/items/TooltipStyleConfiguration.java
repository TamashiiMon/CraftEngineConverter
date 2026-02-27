package fr.robie.craftengineconverter.common.items;

import fr.robie.craftengineconverter.common.utils.ItemConfigurationSerializable;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

public class TooltipStyleConfiguration implements ItemConfigurationSerializable {
    private final NamespacedKey styleKey;

    public TooltipStyleConfiguration(NamespacedKey styleKey) {
        this.styleKey = styleKey;
    }

    public TooltipStyleConfiguration(String styleKey) {
        this.styleKey = NamespacedKey.fromString(styleKey);
    }

    @Override
    public void serialize(@NotNull YamlConfiguration yamlConfiguration, @NotNull String path, @NotNull ConfigurationSection itemSection, @NotNull String itemId) {
        if (this.styleKey != null) {
            getOrCreateSection(itemSection, "data").set("tooltip-style", this.styleKey.asString());
        }
    }
}
