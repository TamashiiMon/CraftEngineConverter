package fr.robie.craftengineconverter.common.items;

import fr.robie.craftengineconverter.common.utils.ItemConfigurationSerializable;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

public class ItemModelConfiguration implements ItemConfigurationSerializable {
    private final String model;

    public ItemModelConfiguration(String model) {
        this.model = model;
    }

    @Override
    public void serialize(@NotNull YamlConfiguration yamlConfiguration, @NotNull String path, @NotNull ConfigurationSection itemSection, @NotNull String itemId) {
        getOrCreateSection(itemSection, "components").set("minecraft:item_model", this.model);
    }
}
