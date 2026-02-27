package fr.robie.craftengineconverter.common.items;

import fr.robie.craftengineconverter.common.utils.AbstractEffectsConfiguration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class DeathProtectionConfiguration extends AbstractEffectsConfiguration {

    private final List<ConsumeEffect> deathEffects;

    public DeathProtectionConfiguration(List<ConsumeEffect> deathEffects) {
        this.deathEffects = deathEffects;
    }

    @Override
    public void serialize(@NotNull YamlConfiguration yamlConfiguration, @NotNull String path, @NotNull ConfigurationSection itemSection) {
        ConfigurationSection components = getOrCreateSection(itemSection, "components");
        ConfigurationSection deathProtectionSection = getOrCreateSection(components, "minecraft:death_protection");

        if (deathEffects == null || deathEffects.isEmpty()) {
            deathProtectionSection.set("death_effects", new ArrayList<>());
            return;
        }

        deathProtectionSection.set("death_effects", serializeEffects(deathEffects));
    }
}