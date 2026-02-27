package fr.robie.craftengineconverter.common.items;

import fr.robie.craftengineconverter.common.utils.ItemConfigurationSerializable;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

public class WeaponConfiguration implements ItemConfigurationSerializable {
    private final int itemDamagePerAttack;
    private final float disableBlockingForSeconds;

    public WeaponConfiguration(int itemDamagePerAttack, float disableBlockingForSeconds) {
        this.itemDamagePerAttack = itemDamagePerAttack;
        this.disableBlockingForSeconds = disableBlockingForSeconds;
    }

    @Override
    public void serialize(@NotNull YamlConfiguration yamlConfiguration, @NotNull String path, @NotNull ConfigurationSection itemSection, @NotNull String itemId) {
        ConfigurationSection components = getOrCreateSection(itemSection, "components");
        ConfigurationSection weaponComponent = getOrCreateSection(components, "minecraft:weapon");
        if (this.itemDamagePerAttack != 1)
            weaponComponent.set("item_damage_per_attack", this.itemDamagePerAttack);
        if (this.disableBlockingForSeconds != 0)
            weaponComponent.set("disable_blocking_for_seconds", this.disableBlockingForSeconds);
    }
}
