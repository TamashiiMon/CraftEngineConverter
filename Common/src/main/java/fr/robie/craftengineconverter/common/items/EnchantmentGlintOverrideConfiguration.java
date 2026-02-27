package fr.robie.craftengineconverter.common.items;

import fr.robie.craftengineconverter.common.utils.ItemConfigurationSerializable;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

public class EnchantmentGlintOverrideConfiguration implements ItemConfigurationSerializable {
    private final boolean enchantGlintOverride;

    public EnchantmentGlintOverrideConfiguration(boolean enchantGlintOverride) {
        this.enchantGlintOverride = enchantGlintOverride;
    }

    @Override
    public void serialize(@NotNull YamlConfiguration yamlConfiguration, @NotNull String path, @NotNull ConfigurationSection itemSection, @NotNull String itemId) {
        getOrCreateSection(itemSection, "components").set("minecraft:enchantment_glint_override", this.enchantGlintOverride);
    }
}
