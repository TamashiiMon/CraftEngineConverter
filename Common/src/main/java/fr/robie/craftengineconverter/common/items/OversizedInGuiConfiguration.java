package fr.robie.craftengineconverter.common.items;

import fr.robie.craftengineconverter.common.utils.ItemConfigurationSerializable;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

public class OversizedInGuiConfiguration implements ItemConfigurationSerializable {
    private final boolean isOversizedInGui;

    public OversizedInGuiConfiguration(boolean isOversizedInGui) {
        this.isOversizedInGui = isOversizedInGui;
    }

    @Override
    public void serialize(@NotNull YamlConfiguration yamlConfiguration, @NotNull String path, @NotNull ConfigurationSection itemSection, @NotNull String itemId) {
        if (!this.isOversizedInGui) return;
        itemSection.set("oversized-in-gui", true);
    }
}
