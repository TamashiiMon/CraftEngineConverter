package fr.robie.craftengineconverter.common.items;

import fr.robie.craftengineconverter.common.utils.ItemConfigurationSerializable;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

public class JukeboxPlayableConfiguration implements ItemConfigurationSerializable {
    private final String song;

    public JukeboxPlayableConfiguration(@NotNull String song) {
        this.song = song;
    }

    @Override
    public void serialize(@NotNull YamlConfiguration yamlConfiguration, @NotNull String path, @NotNull ConfigurationSection itemSection, @NotNull String itemId) {
        ConfigurationSection components = getOrCreateSection(itemSection, "components");
        ConfigurationSection jukeboxPlayableComponent = getOrCreateSection(components, "minecraft:jukebox_playable");
        jukeboxPlayableComponent.set("song", this.song);
    }
}
