package fr.robie.craftengineconverter.common.items;

import fr.robie.craftengineconverter.common.utils.ItemConfigurationSerializable;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

public class MinimumAttackChargeConfiguration implements ItemConfigurationSerializable {
    private final float minimumAttackCharge;

    public MinimumAttackChargeConfiguration(float minimumAttackCharge) {
        this.minimumAttackCharge = minimumAttackCharge;
    }

    @Override
    public void serialize(@NotNull YamlConfiguration yamlConfiguration, @NotNull String path, @NotNull ConfigurationSection itemSection, @NotNull String itemId) {
        if (this.minimumAttackCharge < 0 || this.minimumAttackCharge > 1) return;
        ConfigurationSection components = getOrCreateSection(itemSection, "components");
        ConfigurationSection attackChargeComponent = getOrCreateSection(components, "minecraft:attack_charge");
        attackChargeComponent.set("minimum_attack_charge", this.minimumAttackCharge);
    }
}
