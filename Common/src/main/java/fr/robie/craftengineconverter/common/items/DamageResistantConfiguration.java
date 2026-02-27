package fr.robie.craftengineconverter.common.items;

import fr.robie.craftengineconverter.common.utils.ItemConfigurationSerializable;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

public class DamageResistantConfiguration implements ItemConfigurationSerializable {
    private final String damageResistantType;

    public DamageResistantConfiguration(@NotNull String damageResistantType) {
        this.damageResistantType = damageResistantType;
    }

    @Override
    public void serialize(@NotNull YamlConfiguration yamlConfiguration, @NotNull String path, @NotNull ConfigurationSection itemSection) {
        ConfigurationSection components = getOrCreateSection(itemSection, "components");
        ConfigurationSection damageResistantComponent = getOrCreateSection(components, "minecraft:damage_resistant");
        damageResistantComponent.set("types", this.damageResistantType);
    }
}
