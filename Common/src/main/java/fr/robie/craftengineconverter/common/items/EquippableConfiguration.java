package fr.robie.craftengineconverter.common.items;

import fr.robie.craftengineconverter.common.utils.ItemConfigurationSerializable;
import net.momirealms.craftengine.core.entity.EquipmentSlot;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

public class EquippableConfiguration implements ItemConfigurationSerializable {
    private final EquipmentSlot equipmentSlot;
    private final String equipSound;
    private final String assetId;
    private final Object allowedEntities;
    private final boolean dispensable;
    private final boolean swappable;
    private final boolean damageOnHurt;
    private final boolean equipOnInteract;
    private final String cameraOverlay;
    private final boolean canBeSheared;
    private final String shearingSound;

    public EquippableConfiguration(EquipmentSlot equipmentSlot, String equipSound, String assetId, Object allowedEntities, boolean dispensable, boolean swappable, boolean damageOnHurt, boolean equipOnInteract, String cameraOverlay, boolean canBeSheared, String shearingSound) {
        this.equipmentSlot = equipmentSlot;
        this.equipSound = equipSound;
        this.assetId = assetId;
        this.allowedEntities = allowedEntities;
        this.dispensable = dispensable;
        this.swappable = swappable;
        this.damageOnHurt = damageOnHurt;
        this.equipOnInteract = equipOnInteract;
        this.cameraOverlay = cameraOverlay;
        this.canBeSheared = canBeSheared;
        this.shearingSound = shearingSound;
    }

    public EquippableConfiguration(String assetId, EquipmentSlot equipmentSlot) {
        this.assetId = assetId;
        this.equipmentSlot = equipmentSlot;
        this.equipSound = "item.armor.equip_generic";
        this.allowedEntities = null;
        this.dispensable = true;
        this.swappable = true;
        this.damageOnHurt = true;
        this.equipOnInteract = false;
        this.cameraOverlay = null;
        this.canBeSheared = false;
        this.shearingSound = "item.shears.snip";
    }

    @Override
    public void serialize(@NotNull YamlConfiguration yamlConfiguration, @NotNull String path, @NotNull ConfigurationSection itemSection) {
        ConfigurationSection ceEquippableSection = this.assetId != null && !this.assetId.isBlank()
                ? getOrCreateSection(getOrCreateSection(itemSection, "settings"), "equippable")
                : getOrCreateSection(getOrCreateSection(itemSection, "data"), "equippable");

        if (this.equipmentSlot != null)
            ceEquippableSection.set("slot", this.equipmentSlot.name().toLowerCase());

        if (this.equipSound != null && !this.equipSound.isBlank() && !this.equipSound.equals("item.armor.equip_generic"))
            ceEquippableSection.set("equip_sound", this.equipSound);

        if (this.assetId != null && !this.assetId.isBlank())
            ceEquippableSection.set("asset_id", this.assetId);

        if (this.allowedEntities != null)
            ceEquippableSection.set("allowed_entities", this.allowedEntities);

        if (!this.dispensable)
            ceEquippableSection.set("dispensable", false);

        if (!this.swappable)
            ceEquippableSection.set("swappable", false);

        if (!this.damageOnHurt)
            ceEquippableSection.set("damage_on_hurt", false);

        if (this.equipOnInteract)
            ceEquippableSection.set("equip_on_interact", true);

        if (this.cameraOverlay != null && !this.cameraOverlay.isBlank())
            ceEquippableSection.set("camera_overlay", this.cameraOverlay);

        if (this.canBeSheared)
            ceEquippableSection.set("can_be_sheared", true);

        if (this.shearingSound != null && !this.shearingSound.isBlank() && !this.shearingSound.equals("item.shears.snip"))
            ceEquippableSection.set("shearing_sound", this.shearingSound);
    }
}