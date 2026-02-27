package fr.robie.craftengineconverter.common.items;

import fr.robie.craftengineconverter.common.utils.ItemConfigurationSerializable;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CanPlaceOnConfiguration implements ItemConfigurationSerializable {
    private final BlockPredicateConfiguration delegate;

    public CanPlaceOnConfiguration(List<BlockPredicateConfiguration.BlockPredicate> predicates) {
        this.delegate = new BlockPredicateConfiguration(BlockPredicateConfiguration.Type.CAN_PLACE_ON, predicates);
    }

    @Override
    public void serialize(@NotNull YamlConfiguration yamlConfiguration, @NotNull String path, @NotNull ConfigurationSection itemSection, @NotNull String itemId) {
        this.delegate.serialize(yamlConfiguration, path, itemSection, itemId);
    }
}