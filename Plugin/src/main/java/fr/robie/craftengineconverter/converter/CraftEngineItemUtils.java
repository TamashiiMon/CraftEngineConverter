package fr.robie.craftengineconverter.converter;

import fr.robie.craftengineconverter.common.ObjectUtils;
import fr.robie.craftengineconverter.common.configuration.Configuration;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CraftEngineItemUtils extends ObjectUtils {
    private Material material;
    private final ConfigurationSection craftEngineItemSection;

    public CraftEngineItemUtils(ConfigurationSection craftEngineItemSection){
        this.craftEngineItemSection = craftEngineItemSection;
    }

    public void setMaterial(@NotNull Material material){
        this.craftEngineItemSection.set("material", material.name().toUpperCase());
        this.material = material;
    }

    public void setItemName(@NotNull String itemName){
        getDataSection().set("item-name", (Configuration.disableDefaultItalic?"<!i>":"")+itemName);
    }

    public void setLore(@NotNull List<String> lore){
        if (Configuration.disableDefaultItalic){
            List<String> convertedLore = new ArrayList<>();
            for (String line : lore) {
                convertedLore.add("<!i>" + line);
            }
            lore = convertedLore;
        }
        getDataSection().set("lore", lore);
    }

    public void setJukeboxPlayable(@Nullable String song){
        if (!isValidString(song)) return;
        getComponentsSection().set("minecraft:jukebox_playable", Map.of("song", song));
    }

    public void enableEnchantmentGlint(){
        getComponentsSection().set("minecraft:enchantment_glint_override", true);
    }

    public Material getMaterial(){
        return this.material;
    }

    public void setOversizedInGui(boolean oversized){
        craftEngineItemSection.set("oversized-in-gui", oversized);
    }

    public ConfigurationSection getDataSection() {
        return getOrCreateSection(craftEngineItemSection, "data");
    }

    public ConfigurationSection getComponentsSection() {
        return getOrCreateSection(getDataSection(), "components");
    }

    public ConfigurationSection getSettingsSection() {
        return getOrCreateSection(craftEngineItemSection, "settings");
    }

    public ConfigurationSection getGeneralSection() {
        return this.craftEngineItemSection;
    }

    public ConfigurationSection getBehaviorSection() {
        return getOrCreateSection(craftEngineItemSection, "behavior");
    }

    public ConfigurationSection getStateSection() {
        return getOrCreateSection(craftEngineItemSection, "state");
    }

}
