package fr.robie.craftengineconverter.common.items;

import fr.robie.craftengineconverter.common.utils.ItemConfigurationSerializable;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class EnchantmentConfiguration implements ItemConfigurationSerializable {
    private final Map<String, Integer> enchantments = new HashMap<>();

    public boolean hasEnchantments() {
        return !this.enchantments.isEmpty();
    }

    public void addEnchantment(@NotNull String enchantmentName, int level) {
        this.enchantments.put(enchantmentName, level);
    }

    @Override
    public void serialize(@NotNull YamlConfiguration yamlConfiguration, @NotNull String path, @NotNull ConfigurationSection itemSection, @NotNull String itemId) {
        ConfigurationSection data = getOrCreateSection(itemSection, "data");
        ConfigurationSection enchantment = getOrCreateSection(data, "enchantment");
        for (Map.Entry<String, Integer> entry : enchantments.entrySet()) {
            enchantment.set(entry.getKey(), entry.getValue());
        }
    }
}
