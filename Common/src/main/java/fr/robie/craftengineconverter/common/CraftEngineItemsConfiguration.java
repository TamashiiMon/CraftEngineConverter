package fr.robie.craftengineconverter.common;

import fr.robie.craftengineconverter.common.configuration.Configuration;
import fr.robie.craftengineconverter.common.utils.ItemConfigurationSerializable;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class CraftEngineItemsConfiguration {
    private Material material;

    private final List<ItemConfigurationSerializable> itemsConfigurations = new ArrayList<>();

    public void setMaterial(@Nullable Material material){
        this.material = material;
    }

    @NotNull
    public Material getMaterial(){
        return (this.material == null ? Configuration.defaultMaterial : this.material);
    }

    public void addItemConfiguration(@NotNull ItemConfigurationSerializable itemConfiguration){
        this.itemsConfigurations.add(itemConfiguration);
    }


    public void serialize(@NotNull YamlConfiguration yamlConfiguration, @NotNull String path, @NotNull ConfigurationSection itemSection, @NotNull String itemId) {
        itemSection.set("material", (this.material == null ? Configuration.defaultMaterial : this.material).name().toLowerCase());
        for (ItemConfigurationSerializable itemConfiguration : this.itemsConfigurations) {
            itemConfiguration.serialize(yamlConfiguration, path, itemSection, itemId);
        }
    }
}
