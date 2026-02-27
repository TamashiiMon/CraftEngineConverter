package fr.robie.craftengineconverter.common.items;

import fr.robie.craftengineconverter.common.utils.ItemConfigurationSerializable;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

public class DropDisplayConfiguration implements ItemConfigurationSerializable {
    private final boolean enabled;
    private final String displayName;

    public DropDisplayConfiguration(boolean enabled) {
        this.enabled = enabled;
        this.displayName = null;
    }

    public DropDisplayConfiguration(boolean enabled, String displayName) {
        this.enabled = enabled;
        this.displayName = displayName;
    }

    @Override
    public void serialize(@NotNull YamlConfiguration yamlConfiguration, @NotNull String path, @NotNull ConfigurationSection itemSection, @NotNull String itemId) {
        ConfigurationSection settings = getOrCreateSection(itemSection, "settings");
        if (this.enabled && this.displayName != null && !this.displayName.isEmpty()) {
            settings.set("drop-display", this.displayName);
        } else {
            settings.set("drop-display", this.enabled);
        }
    }
}
