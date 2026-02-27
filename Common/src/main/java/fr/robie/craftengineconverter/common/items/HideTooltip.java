package fr.robie.craftengineconverter.common.items;

import fr.robie.craftengineconverter.common.utils.ItemConfigurationSerializable;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

public class HideTooltip implements ItemConfigurationSerializable {
    private final boolean hideTooltip;

    public HideTooltip(boolean hideTooltip) {
        this.hideTooltip = hideTooltip;
    }

    @Override
    public void serialize(@NotNull YamlConfiguration yamlConfiguration, @NotNull String path, @NotNull ConfigurationSection itemSection, @NotNull String itemId) {
        ConfigurationSection components = getOrCreateSection(itemSection, "components");
        ConfigurationSection tooltipDisplay = getOrCreateSection(components, "minecraft:tooltip_display");
        tooltipDisplay.set("hide_tooltip", this.hideTooltip);
    }
}
