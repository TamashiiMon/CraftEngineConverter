package fr.robie.craftengineconverter.common.items;

import fr.robie.craftengineconverter.common.utils.ItemConfigurationSerializable;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

public class BreakSoundConfiguration implements ItemConfigurationSerializable {
    private final String breakSound;
    private final float range;

    public BreakSoundConfiguration(@NotNull String breakSound, float range) {
        this.breakSound = breakSound;
        this.range = range;
    }

    @Override
    public void serialize(@NotNull YamlConfiguration yamlConfiguration, @NotNull String path, @NotNull ConfigurationSection itemSection, @NotNull String itemId) {
        ConfigurationSection components = getOrCreateSection(itemSection, "components");
        ConfigurationSection breakSoundComponent = getOrCreateSection(components, "minecraft:break_sound");
        breakSoundComponent.set("sound_id", this.breakSound);
        breakSoundComponent.set("range", this.range);
    }
}
