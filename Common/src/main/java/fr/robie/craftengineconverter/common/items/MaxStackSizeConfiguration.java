package fr.robie.craftengineconverter.common.items;

import fr.robie.craftengineconverter.common.utils.ItemConfigurationSerializable;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

public class MaxStackSizeConfiguration implements ItemConfigurationSerializable {
    private final int maxStackSize;

    public MaxStackSizeConfiguration(int maxStackSize) {
        this.maxStackSize = maxStackSize;
    }

    @Override
    public void serialize(@NotNull YamlConfiguration yamlConfiguration, @NotNull String path, @NotNull ConfigurationSection itemSection, @NotNull String itemId) {
        getOrCreateSection(itemSection, "components").set("minecraft:max_stack_size", this.maxStackSize);
    }
}
