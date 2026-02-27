package fr.robie.craftengineconverter.common.items;

import fr.robie.craftengineconverter.common.enums.ComponentFlag;
import fr.robie.craftengineconverter.common.utils.ItemConfigurationSerializable;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ComponentFlagsConfiguration implements ItemConfigurationSerializable {
    private final Set<ComponentFlag> flags = new HashSet<>();

    public ComponentFlagsConfiguration(Collection<ComponentFlag> flags) {
        this.flags.addAll(flags);
    }

    @Override
    public void serialize(@NotNull YamlConfiguration yamlConfiguration, @NotNull String path, @NotNull ConfigurationSection itemSection, @NotNull String itemId) {
        List<String> flagNames = new ArrayList<>();
        for (ComponentFlag flag : this.flags) {
            flagNames.add(flag.getKey());
        }
        getOrCreateSection(itemSection, "data").set("hide-tooltip", flagNames);
    }
}
