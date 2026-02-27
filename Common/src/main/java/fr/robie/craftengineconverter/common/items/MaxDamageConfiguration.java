package fr.robie.craftengineconverter.common.items;

import fr.robie.craftengineconverter.common.utils.ItemConfigurationSerializable;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

public class MaxDamageConfiguration implements ItemConfigurationSerializable {
    private final int maxDamage;

    public MaxDamageConfiguration(int maxDamage) {
        this.maxDamage = maxDamage;
    }

    @Override
    public void serialize(@NotNull YamlConfiguration yamlConfiguration, @NotNull String path, @NotNull ConfigurationSection itemSection, @NotNull String itemId) {
        getOrCreateSection(itemSection, "data").set("max-damage", this.maxDamage);
    }
}
