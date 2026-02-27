package fr.robie.craftengineconverter.common.items;

import fr.robie.craftengineconverter.common.utils.ItemConfigurationSerializable;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public class BlocksAttacksConfiguration implements ItemConfigurationSerializable {
    private final double blockDelaySeconds;
    private final double disableCooldownScale;
    private final String blockSound;
    private final String disabledSound;
    private final String bypassedBy;
    private final ItemDamage itemDamage;
    private final List<DamageReduction> damageReductions;

    public BlocksAttacksConfiguration(double blockDelaySeconds, double disableCooldownScale, String blockSound, String disabledSound, String bypassedBy, ItemDamage itemDamage, List<DamageReduction> damageReductions) {
        this.blockDelaySeconds = blockDelaySeconds;
        this.disableCooldownScale = disableCooldownScale;
        this.blockSound = blockSound;
        this.disabledSound = disabledSound;
        this.bypassedBy = bypassedBy;
        this.itemDamage = itemDamage;
        this.damageReductions = damageReductions;
    }

    public record ItemDamage(double threshold, double base, double factor) {}

    public record DamageReduction(double base, double factor, double horizontalBlockingAngle, List<String> types) {}

    @Override
    public void serialize(@NotNull YamlConfiguration yamlConfiguration, @NotNull String path, @NotNull ConfigurationSection itemSection, @NotNull String itemId) {
        ConfigurationSection components = getOrCreateSection(itemSection, "components");
        ConfigurationSection blocksAttacksSection = getOrCreateSection(components, "minecraft:blocks_attacks");

        if (blockDelaySeconds != 0)
            blocksAttacksSection.set("block_delay_seconds", blockDelaySeconds);

        if (disableCooldownScale != 1)
            blocksAttacksSection.set("disable_cooldown_scale", disableCooldownScale);

        if (blockSound != null && !blockSound.isBlank())
            blocksAttacksSection.set("block_sound", blockSound);

        if (disabledSound != null && !disabledSound.isBlank())
            blocksAttacksSection.set("disabled_sound", disabledSound);

        if (bypassedBy != null && !bypassedBy.isBlank())
            blocksAttacksSection.set("bypassed_by", bypassedBy);

        if (itemDamage != null) {
            ConfigurationSection itemDamageSection = getOrCreateSection(blocksAttacksSection, "item_damage");
            if (itemDamage.threshold() != 0)
                itemDamageSection.set("threshold", itemDamage.threshold());
            if (itemDamage.base() != 0)
                itemDamageSection.set("base", itemDamage.base());
            if (itemDamage.factor() != 1.5)
                itemDamageSection.set("factor", itemDamage.factor());
        }

        if (damageReductions != null && !damageReductions.isEmpty()) {
            List<Map<String, Object>> serialized = damageReductions.stream().map(dr -> {
                Map<String, Object> map = new java.util.HashMap<>();
                map.put("base", dr.base());
                map.put("factor", dr.factor());
                if (dr.horizontalBlockingAngle() != 90)
                    map.put("horizontal_blocking_angle", dr.horizontalBlockingAngle());
                if (dr.types() != null && !dr.types().isEmpty())
                    map.put("type", dr.types().size() == 1 ? dr.types().getFirst() : dr.types());
                return map;
            }).toList();
            blocksAttacksSection.set("damage_reductions", serialized);
        }
    }
}