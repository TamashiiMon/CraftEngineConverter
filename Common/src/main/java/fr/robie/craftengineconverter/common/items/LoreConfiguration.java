package fr.robie.craftengineconverter.common.items;

import fr.robie.craftengineconverter.common.configuration.Configuration;
import fr.robie.craftengineconverter.common.utils.ItemConfigurationSerializable;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class LoreConfiguration implements ItemConfigurationSerializable {
    private final List<String> lore;

    public LoreConfiguration(List<String> lore) {
        this.lore = lore;
    }

    @Override
    public void serialize(@NotNull YamlConfiguration yamlConfiguration, @NotNull String path, @NotNull ConfigurationSection itemSection, @NotNull String itemId) {
        if (Configuration.disableDefaultItalic) {
            List<String> convertedLore = new ArrayList<>();
            for (String line : lore) {
                convertedLore.add("<!i>" + line);
            }
            itemSection.set("lore", convertedLore);
        } else {
            itemSection.set("lore", this.lore);
        }
    }
}
