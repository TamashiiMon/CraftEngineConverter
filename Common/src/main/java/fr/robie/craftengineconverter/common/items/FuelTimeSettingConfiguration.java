package fr.robie.craftengineconverter.common.items;

import fr.robie.craftengineconverter.common.utils.ItemConfigurationSerializable;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

public class FuelTimeSettingConfiguration implements ItemConfigurationSerializable {
    private final int fuelTime;

    public FuelTimeSettingConfiguration(int fuelTime) {
        this.fuelTime = fuelTime;
    }

    @Override
    public void serialize(@NotNull YamlConfiguration yamlConfiguration, @NotNull String path, @NotNull ConfigurationSection itemSection, @NotNull String itemId) {
        if (fuelTime <= 0) return;

        ConfigurationSection settings = getOrCreateSection(itemSection, "settings");
        settings.set("fuel-time", this.fuelTime);
    }
}
