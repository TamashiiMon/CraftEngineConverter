package fr.robie.craftengineconverter.common.items;

import fr.robie.craftengineconverter.common.utils.ItemConfigurationSerializable;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

public class AttackRangeConfiguration implements ItemConfigurationSerializable {

    private final double minReach;
    private final double maxReach;
    private final double minCreativeReach;
    private final double maxCreativeReach;
    private final double hitboxMargin;
    private final double mobFactor;

    public AttackRangeConfiguration(double minReach, double maxReach, double minCreativeReach, double maxCreativeReach, double hitboxMargin, double mobFactor) {
        this.minReach = minReach;
        this.maxReach = maxReach;
        this.minCreativeReach = minCreativeReach;
        this.maxCreativeReach = maxCreativeReach;
        this.hitboxMargin = hitboxMargin;
        this.mobFactor = mobFactor;
    }

    @Override
    public void serialize(@NotNull YamlConfiguration yamlConfiguration, @NotNull String path, @NotNull ConfigurationSection itemSection, @NotNull String itemId) {
        ConfigurationSection components = getOrCreateSection(itemSection, "components");
        ConfigurationSection attackRangeSection = getOrCreateSection(components, "minecraft:attack_range");

        if (this.minReach != 0.0)
            attackRangeSection.set("min_reach", clamp(this.minReach, 0.0, 64.0));

        if (this.maxReach != 3.0)
            attackRangeSection.set("max_reach", clamp(this.maxReach, 0.0, 64.0));

        if (this.minCreativeReach != 0.0)
            attackRangeSection.set("min_creative_reach", clamp(this.minCreativeReach, 0.0, 64.0));

        if (this.maxCreativeReach != 5.0)
            attackRangeSection.set("max_creative_reach", clamp(this.maxCreativeReach, 0.0, 64.0));

        if (this.hitboxMargin != 0.3)
            attackRangeSection.set("hitbox_margin", clamp(this.hitboxMargin, 0.0, 1.0));

        if (this.mobFactor != 1.0)
            attackRangeSection.set("mob_factor", clamp(this.mobFactor, 0.0, 2.0));
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}