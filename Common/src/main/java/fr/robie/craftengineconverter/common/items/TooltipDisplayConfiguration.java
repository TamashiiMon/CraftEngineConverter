package fr.robie.craftengineconverter.common.items;

import fr.robie.craftengineconverter.common.utils.ItemConfigurationSerializable;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class TooltipDisplayConfiguration implements ItemConfigurationSerializable {
    private final List<String> hiddenComponents;

    public TooltipDisplayConfiguration(@NotNull List<String> hiddenComponents) {
        this.hiddenComponents = hiddenComponents;
    }

    @Override
    public void serialize(@NotNull YamlConfiguration yamlConfiguration, @NotNull String path, @NotNull ConfigurationSection itemSection, @NotNull String itemId) {
        if (this.hiddenComponents.isEmpty()) return;
        ConfigurationSection components = getOrCreateSection(itemSection, "components");
        ConfigurationSection tooltipDisplayComponent = getOrCreateSection(components, "minecraft:tooltip_display");
        tooltipDisplayComponent.set("hidden_components", this.hiddenComponents);
    }
}
