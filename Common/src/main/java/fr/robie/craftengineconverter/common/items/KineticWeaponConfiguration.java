package fr.robie.craftengineconverter.common.items;

import fr.robie.craftengineconverter.common.utils.ItemConfigurationSerializable;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

public class KineticWeaponConfiguration implements ItemConfigurationSerializable {

    private final long delayTicks;
    private final double damageMultiplier;
    private final double forwardMovement;
    private final String sound;
    private final String hitSound;
    private final KineticConditions dismountConditions;
    private final KineticConditions knockbackConditions;
    private final KineticConditions damageConditions;

    public KineticWeaponConfiguration(long delayTicks, double damageMultiplier, double forwardMovement, String sound, String hitSound, KineticConditions dismountConditions, KineticConditions knockbackConditions, KineticConditions damageConditions) {
        this.delayTicks = delayTicks;
        this.damageMultiplier = damageMultiplier;
        this.forwardMovement = forwardMovement;
        this.sound = sound;
        this.hitSound = hitSound;
        this.dismountConditions = dismountConditions;
        this.knockbackConditions = knockbackConditions;
        this.damageConditions = damageConditions;
    }

    public record KineticConditions(long maxDurationTicks, double minSpeed, double minRelativeSpeed) {

        public void serialize(@NotNull ConfigurationSection section) {
            if (maxDurationTicks > 0)
                section.set("max_duration_ticks", maxDurationTicks);
            if (minSpeed > 0.0)
                section.set("min_speed", minSpeed);
            if (minRelativeSpeed > 0.0)
                section.set("min_relative_speed", minRelativeSpeed);
        }
    }

    @Override
    public void serialize(@NotNull YamlConfiguration yamlConfiguration, @NotNull String path, @NotNull ConfigurationSection itemSection, @NotNull String itemId) {
        ConfigurationSection components = getOrCreateSection(itemSection, "components");
        ConfigurationSection kineticSection = getOrCreateSection(components, "minecraft:kinetic_weapon");

        if (delayTicks > 0)
            kineticSection.set("delay_ticks", delayTicks);

        if (damageMultiplier != 1.0)
            kineticSection.set("damage_multiplier", damageMultiplier);

        if (forwardMovement != 0.0)
            kineticSection.set("forward_movement", forwardMovement);

        if (sound != null && !sound.isBlank())
            kineticSection.set("sound", sound);

        if (hitSound != null && !hitSound.isBlank())
            kineticSection.set("hit_sound", hitSound);

        if (dismountConditions != null)
            dismountConditions.serialize(getOrCreateSection(kineticSection, "dismount_conditions"));

        if (knockbackConditions != null)
            knockbackConditions.serialize(getOrCreateSection(kineticSection, "knockback_conditions"));

        if (damageConditions != null)
            damageConditions.serialize(getOrCreateSection(kineticSection, "damage_conditions"));
    }
}