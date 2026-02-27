package fr.robie.craftengineconverter.common.items;

import fr.robie.craftengineconverter.common.utils.ItemConfigurationSerializable;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

public class UnbreakableConfiguration implements ItemConfigurationSerializable {
    private final boolean unbreakable;

    public UnbreakableConfiguration(boolean unbreakable) {
        this.unbreakable = unbreakable;
    }

    @Override
    public void serialize(@NotNull YamlConfiguration yamlConfiguration, @NotNull String path, @NotNull ConfigurationSection itemSection, @NotNull String itemId) {
        getOrCreateSection(itemSection, "data").set("unbreakable", this.unbreakable);
    }
}
