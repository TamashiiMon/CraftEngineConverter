package fr.robie.craftengineconverter.common.items;

import fr.robie.craftengineconverter.common.utils.ItemConfigurationSerializable;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

public class EnchantableConfiguration implements ItemConfigurationSerializable {
    private final int enchantability;

    public EnchantableConfiguration(int enchantability) {
        this.enchantability = enchantability;
    }

    @Override
    public void serialize(@NotNull YamlConfiguration yamlConfiguration, @NotNull String path, @NotNull ConfigurationSection itemSection, @NotNull String itemId) {
        ConfigurationSection components = getOrCreateSection(itemSection, "components");
        ConfigurationSection enchantableComponent = getOrCreateSection(components, "minecraft:enchantable");
        enchantableComponent.set("value", this.enchantability);
    }
}
