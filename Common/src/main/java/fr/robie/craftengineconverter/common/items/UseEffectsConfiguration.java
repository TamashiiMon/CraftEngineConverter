package fr.robie.craftengineconverter.common.items;

import fr.robie.craftengineconverter.common.utils.ItemConfigurationSerializable;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

public class UseEffectsConfiguration implements ItemConfigurationSerializable {

    private final boolean canSprint;
    private final double speedMultiplier;
    private final boolean interactVibrations;

    public UseEffectsConfiguration(boolean canSprint, double speedMultiplier, boolean interactVibrations) {
        this.canSprint = canSprint;
        this.speedMultiplier = speedMultiplier;
        this.interactVibrations = interactVibrations;
    }

    @Override
    public void serialize(@NotNull YamlConfiguration yamlConfiguration, @NotNull String path, @NotNull ConfigurationSection itemSection, @NotNull String itemId) {
        ConfigurationSection components = getOrCreateSection(itemSection, "components");
        ConfigurationSection useEffectsSection = getOrCreateSection(components, "minecraft:use_effects");

        if (this.canSprint)
            useEffectsSection.set("can_sprint", true);

        if (this.speedMultiplier != 0.2)
            useEffectsSection.set("speed_multiplier", Math.max(0.0, Math.min(1.0, this.speedMultiplier)));

        if (!this.interactVibrations)
            useEffectsSection.set("interact_vibrations", false);
    }
}