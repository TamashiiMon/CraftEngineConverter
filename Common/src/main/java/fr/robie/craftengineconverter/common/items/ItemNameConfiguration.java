package fr.robie.craftengineconverter.common.items;

import fr.robie.craftengineconverter.common.configuration.Configuration;
import fr.robie.craftengineconverter.common.utils.ItemConfigurationSerializable;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

public class ItemNameConfiguration implements ItemConfigurationSerializable {
    private final String itemName;

    public ItemNameConfiguration(String itemName) {
        this.itemName = itemName;
    }

    @Override
    public void serialize(@NotNull YamlConfiguration yamlConfiguration, @NotNull String path, @NotNull ConfigurationSection itemSection, @NotNull String itemId) {
        itemSection.set("item-name", (Configuration.disableDefaultItalic ? "<!i>" : "")+this.itemName);
    }
}
