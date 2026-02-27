package fr.robie.craftengineconverter.common.items;

import fr.robie.craftengineconverter.common.utils.ItemConfigurationSerializable;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

public class PaintingVariantConfiguration implements ItemConfigurationSerializable {
    private final String variant;

    public PaintingVariantConfiguration(@NotNull String variant) {
        this.variant = variant;
    }

    @Override
    public void serialize(@NotNull YamlConfiguration yamlConfiguration, @NotNull String path, @NotNull ConfigurationSection itemSection, @NotNull String itemId) {
        if (this.variant.isEmpty()) return;
        ConfigurationSection components = getOrCreateSection(itemSection, "components");
        components.set("minecraft:painting/variant", this.variant);
    }
}
