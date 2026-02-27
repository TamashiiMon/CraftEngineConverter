package fr.robie.craftengineconverter.common.items;

import fr.robie.craftengineconverter.common.utils.ItemConfigurationSerializable;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

public class UseCooldownConfiguration implements ItemConfigurationSerializable {
    private final float seconds;
    private final String cooldownGroup;

    public UseCooldownConfiguration(float seconds, String cooldownGroup) {
        this.seconds = seconds;
        this.cooldownGroup = cooldownGroup;
    }

    @Override
    public void serialize(@NotNull YamlConfiguration yamlConfiguration, @NotNull String path, @NotNull ConfigurationSection itemSection) {
        ConfigurationSection components = getOrCreateSection(itemSection, "components");
        ConfigurationSection useCooldownComponent = getOrCreateSection(components, "minecraft:use_cooldown");
        useCooldownComponent.set("seconds", this.seconds);
        if (this.cooldownGroup != null) {
            useCooldownComponent.set("cooldown_group", this.cooldownGroup);
        }
    }
}
