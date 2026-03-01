package fr.robie.craftengineconverter.converter.nexo;

import fr.robie.craftengineconverter.common.BlockStatesMapper;
import fr.robie.craftengineconverter.common.builder.TimerBuilder;
import fr.robie.craftengineconverter.common.configuration.Configuration;
import fr.robie.craftengineconverter.common.enums.ArmorConverter;
import fr.robie.craftengineconverter.common.enums.BukkitFlagToComponentFlag;
import fr.robie.craftengineconverter.common.enums.ComponentFlag;
import fr.robie.craftengineconverter.common.format.Message;
import fr.robie.craftengineconverter.common.items.*;
import fr.robie.craftengineconverter.common.logger.LogType;
import fr.robie.craftengineconverter.common.logger.Logger;
import fr.robie.craftengineconverter.common.utils.AbstractEffectsConfiguration;
import fr.robie.craftengineconverter.common.utils.CecAttributeModifier;
import fr.robie.craftengineconverter.common.utils.FloatsUtils;
import fr.robie.craftengineconverter.common.utils.enums.*;
import fr.robie.craftengineconverter.common.utils.enums.nexo.NexoBestTool;
import fr.robie.craftengineconverter.common.utils.enums.nexo.NexoMinimalType;
import fr.robie.craftengineconverter.converter.Converter;
import fr.robie.craftengineconverter.converter.ItemConverter;
import fr.robie.craftengineconverter.utils.Position;
import fr.robie.craftengineconverter.utils.Tuple;
import fr.robie.craftengineconverter.utils.loots.CraftEngineItemLoot;
import fr.robie.craftengineconverter.utils.loots.ItemLoot;
import fr.robie.craftengineconverter.utils.loots.MinecraftItemLoot;
import fr.robie.craftengineconverter.utils.manager.InternalTemplateManager;
import net.momirealms.craftengine.core.attribute.AttributeModifier;
import net.momirealms.craftengine.core.entity.EquipmentSlot;
import net.momirealms.craftengine.core.entity.display.Billboard;
import net.momirealms.craftengine.core.item.setting.AnvilRepairItem;
import net.momirealms.craftengine.core.util.Direction;
import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.MultipleFacing;
import org.bukkit.block.data.type.Tripwire;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class NexoItemConverter extends ItemConverter {
    private final ConfigurationSection nexoItemSection;

    public NexoItemConverter(Converter converter, ConfigurationSection nexoItemSection, String itemId, ConfigurationSection craftEngineItemSection, YamlConfiguration convertedConfig) {
        super(itemId, craftEngineItemSection,converter,convertedConfig);
        this.nexoItemSection = nexoItemSection;
    }

    @Override
    public void convertMaterial() {
        try {
            this.craftEngineItemsConfiguration.setMaterial(Material.valueOf(this.nexoItemSection.getString("material", Configuration.defaultMaterial.name()).toUpperCase()));
        } catch (Exception ignored) {
        }
    }

    @Override
    public void convertItemName() {
        String itemName = this.nexoItemSection.getString("itemname");
        if (isValidString(itemName)){
            this.craftEngineItemsConfiguration.addItemConfiguration(new ItemNameConfiguration(itemName));
        }
    }

    @Override
    public void convertLore() {
        List<String> lore = this.nexoItemSection.getStringList("lore");
        if (!lore.isEmpty()) {
            this.craftEngineItemsConfiguration.addItemConfiguration(new LoreConfiguration(lore));
        }
    }

    @Override
    public void convertExcludeFromInventory(){
        this.excludeFromInventory = this.nexoItemSection.getBoolean("excludeFromInventory", false);
    }

    @Override
    public void convertDyedColor() {
        Object color = this.nexoItemSection.get("color");
        if (isNotNull(color)) {
            this.craftEngineItemsConfiguration.addItemConfiguration(new DyedColorConfiguration(color));
        }
    }

    @Override
    public void convertUnbreakable() {
        boolean unbreakable = this.nexoItemSection.getBoolean("unbreakable", false);
        if (unbreakable) {
            this.craftEngineItemsConfiguration.addItemConfiguration(new UnbreakableConfiguration(true));
        }
    }

    @Override
    public void convertItemFlags() {
        List<String> itemFlags = this.nexoItemSection.getStringList("ItemFlags");
        if (!itemFlags.isEmpty()) {
            List<ComponentFlag> convertedFlags = new ArrayList<>();
            for (String flag : itemFlags){
                try {
                    ItemFlag bukkitFlag = ItemFlag.valueOf(flag.toUpperCase());
                    convertedFlags.add(BukkitFlagToComponentFlag.fromBukkitItemFlag(bukkitFlag));
                } catch (Exception ignored){
                }
            }
            this.craftEngineItemsConfiguration.addItemConfiguration(new ComponentFlagsConfiguration(convertedFlags));
        }
    }

    @Override
    public void convertAttributeModifiers() {
        List<Map<?, ?>> mapList = this.nexoItemSection.getMapList("AttributeModifiers");
        if (mapList.isEmpty()) return;

        List<CecAttributeModifier> attributeModifiers = new ArrayList<>();
        for (Map<?, ?> attributeModifier : mapList) {
            Object attribute = attributeModifier.get("attribute");
            if (!(attribute instanceof String stringAttribute)) continue;

            Object rawAmount = attributeModifier.get("amount");
            if (!(rawAmount instanceof Double amount)) continue;

            Object rawOperation = attributeModifier.get("operation");
            AttributeModifier.Operation operation;
            if (rawOperation instanceof String strOperation) {
                try {
                    operation = AttributeModifier.Operation.valueOf(strOperation.toUpperCase());
                } catch (Exception e) {
                    if (strOperation.equalsIgnoreCase("ADD_NUMBER")) {
                        operation = AttributeModifier.Operation.ADD_VALUE;
                    } else if (strOperation.equalsIgnoreCase("ADD_SCALAR")) {
                        operation = AttributeModifier.Operation.ADD_MULTIPLIED_BASE;
                    } else if (strOperation.equalsIgnoreCase("MULTIPLY_SCALAR_1")) {
                        operation = AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL;
                    } else {
                        continue;
                    }
                }
            } else if (rawOperation instanceof Integer intOperation) {
                try {
                    operation = switch (intOperation) {
                        case 0 -> AttributeModifier.Operation.ADD_VALUE;
                        case 1 -> AttributeModifier.Operation.ADD_MULTIPLIED_BASE;
                        case 2 -> AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL;
                        default -> throw new IllegalArgumentException("Invalid operation id: " + intOperation);
                    };
                } catch (Exception e) {
                    continue;
                }
            } else {
                continue;
            }

            AttributeModifier.Slot attributeSlot = null;
            Object rawSlot = attributeModifier.get("slot");
            if (!(rawSlot instanceof String strSlot)) continue;
            try {
                attributeSlot = AttributeModifier.Slot.valueOf(strSlot.toUpperCase());
            } catch (Exception ignored) {
            }
            if (attributeSlot == null) continue;
            CecAttributeModifier.Display display = null;
                Object rawDisplay = attributeModifier.get("display");
                if (rawDisplay instanceof Map<?, ?> displayMap) {
                    Object typeObj = displayMap.get("type");
                    Object textObj = displayMap.get("text");
                    if (typeObj instanceof String typeStr && textObj instanceof String textStr) {
                        AttributeModifier.Display.Type displayType;
                        try {
                            displayType = AttributeModifier.Display.Type.valueOf(typeStr.toUpperCase());
                        } catch (Exception e) {
                            continue;
                        }
                        display = new CecAttributeModifier.Display(displayType, textStr);
                    }
                }

            attributeModifiers.add(new CecAttributeModifier(stringAttribute.toLowerCase(), attributeSlot, null, amount, operation, display));
        }

        if (!attributeModifiers.isEmpty()) {
            this.craftEngineItemsConfiguration.addItemConfiguration(new AttributeModifiersConfiguration(attributeModifiers));
        }
    }

    @Override
    public void convertEnchantments() {
        ConfigurationSection configurationSection = this.nexoItemSection.getConfigurationSection("Enchantments");
        if (configurationSection == null) return;
        EnchantmentConfiguration enchantmentConfiguration = new EnchantmentConfiguration();
        for (String enchantmentKey : configurationSection.getKeys(false)) {
            int level = configurationSection.getInt(enchantmentKey, 1);
            String enchantmentName;
            try {
                enchantmentName = Enchantment.getByName(enchantmentKey).key().toString();
            } catch (Exception e) {
                enchantmentName = enchantmentKey;
            }
            enchantmentConfiguration.addEnchantment(enchantmentName.toLowerCase(), level);
        }
        if (enchantmentConfiguration.hasEnchantments())
            this.craftEngineItemsConfiguration.addItemConfiguration(enchantmentConfiguration);
    }

    @Override
    public void convertCustomModelData() {
        int customModelData = this.nexoItemSection.getInt("Pack.custom_model_data", 0);
        if (customModelData != 0) {
            this.craftEngineItemsConfiguration.addItemConfiguration(new CustomModelDataConfiguration(customModelData));
        }
    }

    @Override
    public void convertItemModel() {
        String itemModel = this.nexoItemSection.getString("Components.item_model");
        if (isValidString(itemModel)){
            this.craftEngineItemsConfiguration.addItemConfiguration(new ItemModelConfiguration(itemModel));
        }
    }

    @Override
    public void convertMaxStackSize() {
        int maxStackSize = this.nexoItemSection.getInt("Components.max_stack_size", 0);
        if (maxStackSize > 0 && maxStackSize <= 99) {
            this.craftEngineItemsConfiguration.addItemConfiguration(new MaxStackSizeConfiguration(maxStackSize));
        }
    }

    @Override
    public void convertEnchantmentGlintOverride() {
        if (this.nexoItemSection.getBoolean("Components.enchantment_glint_override", false)) {
            this.craftEngineItemsConfiguration.addItemConfiguration(new EnchantmentGlintOverrideConfiguration(true));
        }
    }

    @Override
    public void convertFireResistance() {
        if (!this.nexoItemSection.getBoolean("Components.fire_resistant", false)) return;

        this.craftEngineItemsConfiguration.addItemConfiguration(new InvulnerableSettingConfiguration(Set.of(InvulnerableSettingConfiguration.InvulnerableType.FIRE, InvulnerableSettingConfiguration.InvulnerableType.FIRE_TICK, InvulnerableSettingConfiguration.InvulnerableType.LAVA), true));
    }

    @Override
    public void convertMaxDamage() {
        int maxDamage = this.nexoItemSection.getInt("Components.max_damage", 0);
        if (maxDamage > 0) {
            this.craftEngineItemsConfiguration.addItemConfiguration(new MaxDamageConfiguration(maxDamage));
        }
    }

    @Override
    public void convertHideTooltip() {
        if (!this.nexoItemSection.getBoolean("Components.hide_tooltip", false)) return;

        this.craftEngineItemsConfiguration.addItemConfiguration(new HideTooltip(true));
    }

    @Override
    public void convertFood() {
        ConfigurationSection foodSection = this.nexoItemSection.getConfigurationSection("Components.food");
        if (foodSection != null) {
            int nutrition = foodSection.getInt("nutrition", -1);
            float saturation = (float) foodSection.getDouble("saturation", -1);
            boolean canAlwaysEat = foodSection.getBoolean("can_always_eat", false);
            if (nutrition >= 0 && saturation >= 0) {
                this.craftEngineItemsConfiguration.addItemConfiguration(new FoodConfiguration(nutrition, saturation, canAlwaysEat));
            }
        }
    }

    @Override
    public void convertTool() {
        ConfigurationSection nexoToolSection = this.nexoItemSection.getConfigurationSection("Components.tool");
        if (isNotNull(nexoToolSection)) {
            float defaultMiningSpeed = (float) nexoToolSection.getDouble("default_mining_speed", 1.0);
            int damagePerBlock = nexoToolSection.getInt("damage_per_block", 1);
            // can_destroy_blocks_in_creative not supported in Nexo, defaults to false
            boolean canDestroyBlocksInCreative = false;

            List<ToolConfiguration.Rule> ceRulesList = new ArrayList<>();
            var rulesList = nexoToolSection.getMapList("rules");

            if (!rulesList.isEmpty()) {
                //noinspection unchecked
                List<Map<String, Object>> nexoRulesList = (List<Map<String, Object>>) (Object) rulesList;

                for (var nexoRule : nexoRulesList) {
                    float speed = 0f;
                    Object speedObj = nexoRule.get("speed");
                    if (isNotNull(speedObj) && speedObj instanceof Number speedNum) {
                        speed = speedNum.floatValue();
                    }

                    boolean correctForDrops = false;
                    Object correctForDropsObj = nexoRule.get("correct_for_drops");
                    if (isNotNull(correctForDropsObj) && correctForDropsObj instanceof Boolean correctForDropsBool) {
                        correctForDrops = correctForDropsBool;
                    }

                    // --- Blocks (material / materials) ---
                    List<String> materialBlocks = new ArrayList<>();
                    Object material = nexoRule.get("material");
                    if (isNotNull(material) && material instanceof String materialStr && !materialStr.isEmpty()) {
                        String normalized = materialStr.toLowerCase(Locale.ROOT);
                        if (!normalized.contains(":")) normalized = "minecraft:" + normalized;
                        materialBlocks.add(normalized);
                    }
                    Object materials = nexoRule.get("materials");
                    if (isNotNull(materials) && materials instanceof List<?> materialsList && !materialsList.isEmpty()) {
                        //noinspection unchecked
                        for (String mat : (List<String>) materialsList) {
                            String normalized = mat.toLowerCase(Locale.ROOT);
                            if (!normalized.contains(":")) normalized = "minecraft:" + normalized;
                            materialBlocks.add(normalized);
                        }
                    }

                    if (!materialBlocks.isEmpty()) {
                        ceRulesList.add(new ToolConfiguration.Rule(speed, correctForDrops, materialBlocks));
                    }

                    // --- Tags (tag / tags) ---
                    List<String> tagsList = new ArrayList<>();
                    Object tag = nexoRule.get("tag");
                    if (isNotNull(tag) && tag instanceof String tagStr && !tagStr.isEmpty()) {
                        tagsList.add(tagStr);
                    }
                    Object tags = nexoRule.get("tags");
                    if (isNotNull(tags) && tags instanceof List<?> tagsListObj && !tagsListObj.isEmpty()) {
                        tagsList.addAll((List<String>) tagsListObj);
                    }

                    for (String tagStr : tagsList) {
                        String normalized = tagStr.toLowerCase(Locale.ROOT);
                        if (!normalized.startsWith("#")) normalized = "#" + normalized;
                        if (!normalized.contains(":")) normalized = normalized.replace("#", "#minecraft:");
                        ceRulesList.add(new ToolConfiguration.Rule(speed, correctForDrops, normalized));
                    }
                }

                if (ceRulesList.isEmpty()) {
                    Logger.info(Message.WARNING__CONVERTER__NEXO__TOOL__NO_BLOCKS_FOUND, "item", this.itemId);
                }
            }

            this.craftEngineItemsConfiguration.addItemConfiguration(new ToolConfiguration(defaultMiningSpeed, damagePerBlock, canDestroyBlocksInCreative, ceRulesList));
        }
    }

    @Override
    public void convertCustomData() {
        ConfigurationSection customDataSection = this.nexoItemSection.getConfigurationSection("Components.custom_data");
        if (customDataSection != null) {
            List<CustomDataConfiguration.CustomDataEntry> customDataEntries = new ArrayList<>();
            for (String key : customDataSection.getKeys(false)) {
                Object value = customDataSection.get(key);
                if (isNotNull(value)) {
                    customDataEntries.add(new CustomDataConfiguration.CustomDataEntry(key, value));
                }
            }
            if (!customDataEntries.isEmpty()) {
                this.craftEngineItemsConfiguration.addItemConfiguration(new CustomDataConfiguration(customDataEntries));
            }
        }
    }

    @Override
    public void convertJukeboxPlayable() {
        String song = this.nexoItemSection.getString("Components.jukebox_playable.song_key");
        if (isValidString(song)) {
            this.craftEngineItemsConfiguration.addItemConfiguration(new JukeboxPlayableConfiguration(song));
        }
    }

    @Override
    public void convertConsumable() {
        ConfigurationSection consumableSection = this.nexoItemSection.getConfigurationSection("Components.consumable");
        if (consumableSection == null) return;

        String sound = consumableSection.getString("sound", "entity.generic.eat");
        boolean hasConsumeParticles = consumableSection.getBoolean("consume_particles", true);
        double consumeSeconds = consumableSection.getDouble("consume_seconds", 1.6);

        ConsumableConfiguration.Animation animation;
        try {
            animation = ConsumableConfiguration.Animation.valueOf(
                    consumableSection.getString("animation", "eat").toUpperCase()
            );
        } catch (IllegalArgumentException e) {
            animation = ConsumableConfiguration.Animation.EAT;
        }

        List<AbstractEffectsConfiguration.ConsumeEffect> consumeEffects = new ArrayList<>();

        ConfigurationSection effectsSection = consumableSection.getConfigurationSection("effects");
        if (effectsSection != null) {
            ConfigurationSection applyEffectsSection = effectsSection.getConfigurationSection("APPLY_EFFECTS");
            if (applyEffectsSection != null) {
                List<ConsumableConfiguration.ApplyEffectsConsumeEffect.ApplyEffect> effects = new ArrayList<>();
                for (String effectKey : applyEffectsSection.getKeys(false)) {
                    effects.add(new ConsumableConfiguration.ApplyEffectsConsumeEffect.ApplyEffect(
                            effectKey,
                            applyEffectsSection.getInt(effectKey + ".amplifier", 0),
                            applyEffectsSection.getInt(effectKey + ".duration", 1),
                            applyEffectsSection.getBoolean(effectKey + ".ambient", false),
                            applyEffectsSection.getBoolean(effectKey + ".show_particles", false),
                            applyEffectsSection.getBoolean(effectKey + ".show_icon", false),
                            applyEffectsSection.getDouble(effectKey + ".probability", 1.0)
                    ));
                }
                consumeEffects.add(new ConsumableConfiguration.ApplyEffectsConsumeEffect(effects));
            }

            List<String> removeEffects = effectsSection.getStringList("REMOVE_EFFECTS");
            if (!removeEffects.isEmpty()) {
                consumeEffects.add(new ConsumableConfiguration.RemoveEffectsConsumeEffect(removeEffects));
            }

            if (effectsSection.get("CLEAR_ALL_EFFECTS") != null) {
                consumeEffects.add(new ConsumableConfiguration.ClearAllEffectsConsumeEffect());
            }

            double diameter = effectsSection.getDouble("TELEPORT_RANDOMLY.diameter", -1.0);
            if (diameter > 0) {
                consumeEffects.add(new ConsumableConfiguration.TeleportRandomlyConsumeEffect(diameter));
            }

            ConfigurationSection playSoundSection = effectsSection.getConfigurationSection("PLAY_SOUND");
            if (playSoundSection != null) {
                consumeEffects.add(new ConsumableConfiguration.PlaySoundConsumeEffect(
                        playSoundSection.getString("sound", "entity.player.levelup"),
                        playSoundSection.getDouble("range", 16.0)
                ));
            }
        }

        if (!consumeEffects.isEmpty()) {
            this.craftEngineItemsConfiguration.addItemConfiguration(new ConsumableConfiguration(sound, hasConsumeParticles, consumeSeconds, animation, consumeEffects));
        }
    }

    @Override
    public void convertEquippable() {
        ConfigurationSection equipableSection = this.nexoItemSection.getConfigurationSection("Components.equippable");
        if (equipableSection == null) return;

        String assetId = equipableSection.getString("asset_id");
        String slot = equipableSection.getString("slot");
        String equipSound = equipableSection.getString("equip_sound", "item.armor.equip_generic");
        String cameraOverlay = equipableSection.getString("camera_overlay");
        boolean dispensable = equipableSection.getBoolean("dispensable", true);
        boolean swappable = equipableSection.getBoolean("swappable", true);
        boolean damageOnHurt = equipableSection.getBoolean("damage_on_hurt", true);
        boolean equipOnInteract = equipableSection.getBoolean("equip_on_interact", false);
        boolean canBeSheared = equipableSection.getBoolean("can_be_sheared", false);
        String shearingSound = equipableSection.getString("shearing_sound", "item.shears.snip");

        Object allowedEntities = null;
        List<String> allowedEntityTypes = equipableSection.getStringList("allowed_entity_types");
        if (!allowedEntityTypes.isEmpty()) {
            allowedEntities = allowedEntityTypes.size() == 1 ? allowedEntityTypes.getFirst() : allowedEntityTypes;
        }

        if (isValidString(assetId) && this.craftEngineItemsConfiguration.getMaterial() == Material.ELYTRA) {
            for (String keyToCheck : new String[]{"_elytra"}) {
                if (assetId.endsWith(keyToCheck)) {
                    assetId = assetId.substring(0, assetId.length() - keyToCheck.length());
                }
            }
            if (!isValidString(slot)) {
                slot = "chest";
            }
        }

        EquipmentSlot equipmentSlot = null;
        if (isValidString(slot)) {
            try {
                equipmentSlot = EquipmentSlot.valueOf(slot.toUpperCase());
            } catch (IllegalArgumentException e) {
                Logger.debug(Message.WARNING__CONVERTER__NEXO__EQUIPPABLE__UNKNOWN_SLOT, LogType.WARNING, "slot", slot, "item", this.itemId);
            }
        }

        if (isValidString(assetId)) {
            this.setAssetId(assetId);
        }

        this.craftEngineItemsConfiguration.addItemConfiguration(new EquippableConfiguration(equipmentSlot, equipSound, assetId, allowedEntities, dispensable, swappable, damageOnHurt, equipOnInteract, cameraOverlay, canBeSheared, shearingSound, null));
    }

    @Override
    public void convertDamageResistance() {
        String damageResistance = this.nexoItemSection.getString("Components.damage_resistant");
        if (isValidString(damageResistance)) {
            this.craftEngineItemsConfiguration.addItemConfiguration(new DamageResistantConfiguration(damageResistance));
        }
    }

    @Override
    public void convertEnchantableComponent() {
        int maxEnchantableLevel = this.nexoItemSection.getInt("Components.enchantable", -1);
        if (maxEnchantableLevel >= 0) {
            this.craftEngineItemsConfiguration.addItemConfiguration(new EnchantableConfiguration(maxEnchantableLevel));
        }
    }

    @Override
    public void convertGliderComponent() {
        if (this.nexoItemSection.getBoolean("Components.glider", false)) {
            this.craftEngineItemsConfiguration.addItemConfiguration(new GliderConfiguration(true));
        }
    }

    @Override
    public void convertToolTipStyle() {
        String toolTipStyle = this.nexoItemSection.getString("Components.tooltip_style");
        if (isValidString(toolTipStyle)) {
            try {
                this.craftEngineItemsConfiguration.addItemConfiguration(new TooltipStyleConfiguration(NamespacedKey.fromString(toolTipStyle)));
            } catch (IllegalArgumentException e) {
                Logger.debug(Message.WARNING__CONVERTER__NEXO__TOOLTIP_STYLE__UNKNOWN_STYLE, LogType.WARNING, "style", toolTipStyle, "item", this.itemId);
            }
        }
    }

    @Override
    public void convertUseCooldown() {
        ConfigurationSection useCooldownSection = this.nexoItemSection.getConfigurationSection("Components.use_cooldown");
        if (useCooldownSection == null) return;

        if (!useCooldownSection.contains("seconds")) return;
        float seconds = (float) useCooldownSection.getDouble("seconds", 1.0);
        if (seconds <= 0) {
            seconds = 1.0f;
            Logger.debug(Message.WARNING__CONVERTER__NEXO__USE_COOLDOWN__INVALID_SECONDS, LogType.WARNING, "seconds", seconds, "item", this.itemId);
        }
        String cooldownGroup = useCooldownSection.getString("group");
        this.craftEngineItemsConfiguration.addItemConfiguration(new UseCooldownConfiguration(seconds, cooldownGroup));
    }

    @Override
    public void convertUseRemainderComponent() {
        ConfigurationSection useRemainderSection = this.nexoItemSection.getConfigurationSection("Components.use_remainder");
        if (useRemainderSection == null) return;

        String minecraftType = useRemainderSection.getString("minecraft_type");
        if (minecraftType != null) {
            if (!minecraftType.contains(":")) {
                minecraftType = "minecraft:" + minecraftType;
            }
            this.craftEngineItemsConfiguration.addItemConfiguration(new UseRemainderConfiguration(minecraftType.toLowerCase(), 1));
        }
    }

    @Override
    public void convertAnvilRepairable() {
        ConfigurationSection componentsSection = this.nexoItemSection.getConfigurationSection("Components");
        if (componentsSection == null) return;

        List<AnvilRepairItem> anvilRepairItems = new ArrayList<>();
        Object singleRepairItem = componentsSection.get("anvil_repairable.repairable");
        if (singleRepairItem instanceof String singleRepairItemStr && isValidString(singleRepairItemStr)) {
            anvilRepairItems.add(new AnvilRepairItem(List.of(singleRepairItemStr),1, 1.0));
        } else if (singleRepairItem instanceof List<?> singleRepairItemList) {
            for (Object item : singleRepairItemList) {
                if (item instanceof String itemStr && isValidString(itemStr)) {
                    anvilRepairItems.add(new AnvilRepairItem(List.of(itemStr),1, 1.0));
                }
            }
        }
        if (!anvilRepairItems.isEmpty()) {
            this.craftEngineItemsConfiguration.addItemConfiguration(new AnvilRepairItemConfiguration(anvilRepairItems));
            this.craftEngineItemsConfiguration.addItemConfiguration(new RepairableSettingConfiguration(false, true, false));
        }
    }

    @Override
    public void convertDeathProtection() {
        ConfigurationSection nexoDeathProtectionSection = nexoItemSection.getConfigurationSection("Components.death_protection");
        if (isNull(nexoDeathProtectionSection)) return;

        ConfigurationSection deathEffectsSection = nexoDeathProtectionSection.getConfigurationSection("death_effects");

        if (isNull(deathEffectsSection)) {
            this.craftEngineItemsConfiguration.addItemConfiguration(new DeathProtectionConfiguration(null));
            return;
        }

        List<AbstractEffectsConfiguration.ConsumeEffect> deathEffects = new ArrayList<>();

        ConfigurationSection applyEffectsSection = deathEffectsSection.getConfigurationSection("APPLY_EFFECTS");
        if (isNotNull(applyEffectsSection)) {
            List<AbstractEffectsConfiguration.ApplyEffectsConsumeEffect.ApplyEffect> effects = new ArrayList<>();
            for (String key : applyEffectsSection.getKeys(false)) {
                effects.add(new AbstractEffectsConfiguration.ApplyEffectsConsumeEffect.ApplyEffect(
                        key,
                        applyEffectsSection.getInt(key + ".amplifier", 0),
                        applyEffectsSection.getInt(key + ".duration", 1),
                        applyEffectsSection.getBoolean(key + ".ambient", false),
                        applyEffectsSection.getBoolean(key + ".show_particles", true),
                        applyEffectsSection.getBoolean(key + ".show_icon", true),
                        applyEffectsSection.getDouble(key + ".probability", 1.0)
                ));
            }
            deathEffects.add(new AbstractEffectsConfiguration.ApplyEffectsConsumeEffect(effects));
        }

        List<String> removeEffects = deathEffectsSection.getStringList("REMOVE_EFFECTS");
        if (!removeEffects.isEmpty())
            deathEffects.add(new AbstractEffectsConfiguration.RemoveEffectsConsumeEffect(removeEffects));

        if (deathEffectsSection.get("CLEAR_ALL_EFFECTS") != null)
            deathEffects.add(new AbstractEffectsConfiguration.ClearAllEffectsConsumeEffect());

        double diameter = deathEffectsSection.getDouble("TELEPORT_RANDOMLY.diameter", -1.0);
        if (diameter > 0)
            deathEffects.add(new AbstractEffectsConfiguration.TeleportRandomlyConsumeEffect(diameter));

        ConfigurationSection playSoundSection = deathEffectsSection.getConfigurationSection("PLAY_SOUND");
        if (isNotNull(playSoundSection)) {
            String sound = playSoundSection.getString("sound");
            if (isValidString(sound))
                deathEffects.add(new AbstractEffectsConfiguration.PlaySoundConsumeEffect(
                        sound,
                        playSoundSection.getDouble("range", 16.0)
                ));
        }

        if (!deathEffects.isEmpty()) {
            this.craftEngineItemsConfiguration.addItemConfiguration(new DeathProtectionConfiguration(deathEffects));
        }
    }

    @Override
    public void convertToolTipDisplay() {
        List<String> tooltipDisplay = this.nexoItemSection.getStringList("Components.tooltip_display");
        if (!tooltipDisplay.isEmpty()) {
            this.craftEngineItemsConfiguration.addItemConfiguration(new TooltipDisplayConfiguration(tooltipDisplay));
        }
    }

    @Override
    public void convertBreakSound() {
        String breakSound = this.nexoItemSection.getString("Components.break_sound");
        if (isValidString(breakSound)) {
            this.craftEngineItemsConfiguration.addItemConfiguration(new BreakSoundConfiguration(breakSound, 16.0f));
        }
    }

    @Override
    public void convertWeaponComponent() {
        ConfigurationSection weaponSection = this.nexoItemSection.getConfigurationSection("Components.weapon");
        if (weaponSection == null) return;

        int damagePerAttack = weaponSection.getInt("item_damage_per_attack", 1);
        float disableBlocking = (float) weaponSection.getDouble("disable_blocking", 0);
        this.craftEngineItemsConfiguration.addItemConfiguration(new WeaponConfiguration(damagePerAttack, disableBlocking));
    }

    @Override
    public void convertBlocksAttackComponent() {
        ConfigurationSection nexoBlocksAttacksSection = this.nexoItemSection.getConfigurationSection("Components.blocks_attacks");
        if (isNull(nexoBlocksAttacksSection)) return;

        double blockDelay = nexoBlocksAttacksSection.getDouble("block_delay", 0);
        double disableCooldownScale = nexoBlocksAttacksSection.getDouble("disable_cooldown_scale", 1);
        String blockSound = nexoBlocksAttacksSection.getString("block_sound");
        String disabledSound = nexoBlocksAttacksSection.getString("disabled_sound");
        String bypassedBy = nexoBlocksAttacksSection.getString("bypassed_by");

        BlocksAttacksConfiguration.ItemDamage itemDamage = new BlocksAttacksConfiguration.ItemDamage(
                nexoBlocksAttacksSection.getDouble("item_damage.threshold", 0),
                nexoBlocksAttacksSection.getDouble("item_damage.base", 0),
                nexoBlocksAttacksSection.getDouble("item_damage.factor", 1.5)
        );

        List<BlocksAttacksConfiguration.DamageReduction> damageReductions = new ArrayList<>();
        for (var dr : nexoBlocksAttacksSection.getMapList("damage_reductions")) {
            Object baseObj = dr.get("base");
            Object factorObj = dr.get("factor");
            if (!(baseObj instanceof Double base) || !(factorObj instanceof Double factor)) continue;

            double horizontalBlockingAngle = 90;
            Object angleObj = dr.get("horizontal_blocking");
            if (angleObj instanceof Double angleDouble) horizontalBlockingAngle = angleDouble;

            List<String> types = new ArrayList<>();
            Object typesObj = dr.get("types");
            if (typesObj instanceof List<?> list) {
                types.addAll((List<String>) list);
            } else if (typesObj instanceof String str) {
                types.add(str);
            }

            damageReductions.add(new BlocksAttacksConfiguration.DamageReduction(base, factor, horizontalBlockingAngle, types));
        }

        this.craftEngineItemsConfiguration.addItemConfiguration(new BlocksAttacksConfiguration(blockDelay, disableCooldownScale, blockSound, disabledSound, bypassedBy, itemDamage, damageReductions));
    }

    @Override
    public void convertCanPlaceOnComponent() {
        convertBlockPredicateComponent("can_place_on");
    }

    @Override
    public void convertCanBreakComponent() {
        convertBlockPredicateComponent("can_break");
    }

    private void convertBlockPredicateComponent(String componentName) {
        ConfigurationSection nexoSection = this.nexoItemSection.getConfigurationSection("Components." + componentName);
        if (isNull(nexoSection)) return;

        List<String> blockArray = new ArrayList<>();
        List<String> tagsArray = new ArrayList<>();

        String block = nexoSection.getString("block");
        if (isValidString(block)) processBlockOrTag(block, blockArray, tagsArray);

        for (String blockItem : nexoSection.getStringList("blocks")) {
            if (isValidString(blockItem)) processBlockOrTag(blockItem, blockArray, tagsArray);
        }

        List<BlockPredicateConfiguration.BlockPredicate> predicates = new ArrayList<>();

        if (!blockArray.isEmpty())
            predicates.add(new BlockPredicateConfiguration.BlockPredicate(blockArray));

        for (String tag : tagsArray)
            predicates.add(new BlockPredicateConfiguration.BlockPredicate(tag));

        if (predicates.isEmpty()) return;

        BlockPredicateConfiguration.Type type = componentName.equals("can_place_on")
                ? BlockPredicateConfiguration.Type.CAN_PLACE_ON
                : BlockPredicateConfiguration.Type.CAN_BREAK;

        this.craftEngineItemsConfiguration.addItemConfiguration(new BlockPredicateConfiguration(type, predicates));
    }

    private void processBlockOrTag(String input, List<String> blockArray, List<String> tagsArray) {
        try {
            Material.valueOf(input.toUpperCase());
            String normalized = input.toLowerCase(Locale.ROOT);
            if (!normalized.contains(":")) {
                normalized = "minecraft:" + normalized;
            }
            blockArray.add(normalized);
        } catch (IllegalArgumentException e) {
            String normalized = input.toLowerCase(Locale.ROOT);
            if (!normalized.startsWith("#")) {
                normalized = "#" + normalized;
            }
            if (!normalized.contains(":")) {
                normalized = normalized.replace("#", "#minecraft:");
            }
            tagsArray.add(normalized);
        }
    }

    @Override
    public void convertOversizedInGui() {
        if (this.nexoItemSection.getBoolean("Pack.oversized_in_gui", false)) {
            this.craftEngineItemsConfiguration.addItemConfiguration(new OversizedInGuiConfiguration(true));
        }
    }

    @Override
    public void convertPaintingVariant(){
        String paintingVariant = this.nexoItemSection.getString("Components.painting_variant");
        if (isValidString(paintingVariant)) {
            this.craftEngineItemsConfiguration.addItemConfiguration(new PaintingVariantConfiguration(paintingVariant));
        }
    }

    @Override
    public void convertKineticComponent() {
        ConfigurationSection kineticSection = this.nexoItemSection.getConfigurationSection("Components.kinetic_weapon");
        if (isNull(kineticSection)) return;

        long delayTicks = TimerBuilder.parseTimeToTicks(kineticSection.getString("delay", "0t"));
        double damageMultiplier = kineticSection.getDouble("damage_multiplier", 1.0);
        double forwardMovement = kineticSection.getDouble("forward_movement", 0.0);
        String sound = kineticSection.getString("sound");
        String hitSound = kineticSection.getString("hit_sound");

        KineticWeaponConfiguration.KineticConditions dismountConditions = parseKineticConditions(kineticSection.getConfigurationSection("dismount_conditions"));
        KineticWeaponConfiguration.KineticConditions knockbackConditions = parseKineticConditions(kineticSection.getConfigurationSection("knockback_conditions"));
        KineticWeaponConfiguration.KineticConditions damageConditions = parseKineticConditions(kineticSection.getConfigurationSection("damage_conditions"));

        this.craftEngineItemsConfiguration.addItemConfiguration(new KineticWeaponConfiguration(delayTicks, damageMultiplier, forwardMovement, sound, hitSound, dismountConditions, knockbackConditions, damageConditions));
    }

    private KineticWeaponConfiguration.KineticConditions parseKineticConditions(ConfigurationSection section) {
        if (section == null) return null;
        return new KineticWeaponConfiguration.KineticConditions(
                TimerBuilder.parseTimeToTicks(section.getString("max_duration", "0t")),
                section.getDouble("min_speed", 0.0),
                section.getDouble("min_relative_speed", 0.0)
        );
    }

    @Override
    public void convertPiercingWeaponComponent() {
        ConfigurationSection piercingSection = this.nexoItemSection.getConfigurationSection("Components.piercing_weapon");
        if (isNull(piercingSection)) return;

        this.craftEngineItemsConfiguration.addItemConfiguration(new PiercingWeaponConfiguration(
                piercingSection.getBoolean("deals_knockback", true),
                piercingSection.getBoolean("dismounts", false),
                piercingSection.getString("sound"),
                piercingSection.getString("hit_sound")
        ));
    }

    @Override
    public void convertAttackRangeComponent() {
        ConfigurationSection attackRangeSection = this.nexoItemSection.getConfigurationSection("Components.attack_range");
        if (isNull(attackRangeSection)) return;

        double minReach = 0.0;
        double maxReach = 3.0;

        String reach = attackRangeSection.getString("reach");
        if (isValidString(reach)) {
            if (reach.contains("..")) {
                String[] parts = reach.split("\\.\\.");
                if (parts.length == 2) {
                    try {
                        minReach = Double.parseDouble(parts[0].trim());
                        maxReach = Double.parseDouble(parts[1].trim());
                    } catch (NumberFormatException e) {
                        Logger.debug(Message.WARNING__CONVERTER__NEXO__ATTACK_RANGE__INVALID_REACH_FORMAT, LogType.WARNING, "reach", reach, "item", this.itemId);
                    }
                }
            } else {
                try {
                    maxReach = Double.parseDouble(reach.trim());
                } catch (NumberFormatException e) {
                    Logger.debug(Message.WARNING__CONVERTER__NEXO__ATTACK_RANGE__INVALID_REACH_VALUE, LogType.WARNING, "reach", reach, "item", this.itemId);
                }
            }
        }

        if (attackRangeSection.contains("min_reach")) minReach = attackRangeSection.getDouble("min_reach", 0.0);
        if (attackRangeSection.contains("max_reach")) maxReach = attackRangeSection.getDouble("max_reach", 3.0);

        this.craftEngineItemsConfiguration.addItemConfiguration(new AttackRangeConfiguration(
                minReach,
                maxReach,
                attackRangeSection.getDouble("min_creative_reach", 0.0),
                attackRangeSection.getDouble("max_creative_reach", 5.0),
                attackRangeSection.getDouble("hitbox_margin", 0.3),
                attackRangeSection.getDouble("mob_factor", 1.0)
        ));
    }

    @Override
    public void convertSwingAnimationComponent() {
        ConfigurationSection swingAnimationSection = this.nexoItemSection.getConfigurationSection("Components.swing_animation");
        if (isNull(swingAnimationSection)) return;

        SwingAnimationConfiguration.AnimationType type;
        try {
            type = SwingAnimationConfiguration.AnimationType.valueOf(
                    swingAnimationSection.getString("type", "whack").toUpperCase()
            );
        } catch (IllegalArgumentException e) {
            Logger.debug(Message.WARNING__CONVERTER__NEXO__SWING_ANIMATION__INVALID_TYPE, LogType.WARNING, "item", this.itemId);
            type = SwingAnimationConfiguration.AnimationType.WHACK;
        }

        long durationTicks = TimerBuilder.parseTimeToTicks(swingAnimationSection.getString("duration", "6t"));
        if (durationTicks <= 0) {
            Logger.debug(Message.WARNING__CONVERTER__NEXO__SWING_ANIMATION__INVALID_DURATION, LogType.WARNING, "duration", durationTicks, "item", this.itemId);
            durationTicks = 6;
        }

        this.craftEngineItemsConfiguration.addItemConfiguration(new SwingAnimationConfiguration(type, (int) durationTicks));
    }

    @Override
    public void convertUseEffectsComponent() {
        ConfigurationSection useEffectsSection = this.nexoItemSection.getConfigurationSection("Components.use_effects");
        if (isNull(useEffectsSection)) return;

        this.craftEngineItemsConfiguration.addItemConfiguration(new UseEffectsConfiguration(
                useEffectsSection.getBoolean("can_sprint", false),
                useEffectsSection.getDouble("speed_multiplier", 0.2),
                useEffectsSection.getBoolean("interact_vibrations", true)
        ));
    }

    @Override
    public void convertDamageTypeComponent(){
        String damageType = this.nexoItemSection.getString("Components.damage_type");
        if (isValidString(damageType)) {
            this.craftEngineItemsConfiguration.addItemConfiguration(new DamageTypeConfiguration(damageType));
        }
    }

    @Override
    public void convertMinimumAttackChargeComponent(){
        double minAttackCharge = this.nexoItemSection.getDouble("Components.minimum_attack_charge", -1f);
        if (minAttackCharge >= 0f) {
            minAttackCharge = Math.max(0.0, Math.min(1.0, minAttackCharge));
            this.craftEngineItemsConfiguration.addItemConfiguration(new MinimumAttackChargeConfiguration((float) minAttackCharge));
        }
    }

    @Override
    public void convertProfileComponent() {
        ConfigurationSection profileSection = this.nexoItemSection.getConfigurationSection("Components.profile");
        if (isNull(profileSection)) return;

        String name = profileSection.getString("name");
        String uuid = profileSection.getString("uuid");

        List<PlayerProfileConfiguration.Property> properties = new ArrayList<>();
        ConfigurationSection propertiesSection = profileSection.getConfigurationSection("properties");
        if (propertiesSection != null) {
            String propName = propertiesSection.getString("name");
            String propValue = propertiesSection.getString("value");
            String propSignature = propertiesSection.getString("signature");
            if (isValidString(propName) && isValidString(propValue)) {
                properties.add(new PlayerProfileConfiguration.Property(propName, propValue, propSignature));
            }
        }

        this.craftEngineItemsConfiguration.addItemConfiguration(new PlayerProfileConfiguration(
                name,
                uuid,
                properties,
                profileSection.getString("texture"),
                profileSection.getString("cape"),
                profileSection.getString("elytra"),
                profileSection.getString("model")
        ));
    }

    @Override
    public void convertItemTexture() {
        ConfigurationSection packSection = this.nexoItemSection.getConfigurationSection("Pack");
        if (packSection == null) return;

        String parentModel = packSection.getString("parent_model");

        if (!isValidString(parentModel)) {
            convertModelWithoutParent(packSection);
        } else {
            convertModelWithParent(packSection, parentModel);
        }
    }

    private void convertModelWithoutParent(ConfigurationSection packSection) {
        String modelPath = packSection.getString("model");
        if (!isValidString(modelPath)) {
            if (this.craftEngineItemsConfiguration.getMaterial() == Material.ELYTRA){
                buildElytraModel(packSection);
            }
            if (packSection.isConfigurationSection("CustomArmor")){
                ConfigurationSection customArmorSection = packSection.getConfigurationSection("CustomArmor");
                ConfigurationSection fileEquipementsSection = getEquipmentsSection();

                String assetId = determineAssetId(packSection, List.of("_wolf_armor","_llama_armor","_horse_armor","_nautilus_armor"));

                if (isNotNull(customArmorSection) && isNotNull(assetId)){
                    Set<String> keys = customArmorSection.getKeys(false);
                    Map<String, Set<Tuple<String>>> equipmentLayers = new HashMap<>();

                    for (String key : keys) {
                        if (key.equals("harness")) continue;
                        String val = switch (key) {
                            case "wolf_armor" -> "wolf-body";
                            case "llama_armor" -> "llama-body";
                            case "horse_armor" -> "horse-body";
                            case "nautilus_armor" -> "nautilus-body";
                            case "layer1" -> "humanoid";
                            case "layer2" -> "humanoid-leggings";
                            default -> key;
                        };
                        equipmentLayers.computeIfAbsent(val, k -> new HashSet<>()).add(new Tuple<>(key, val));
                    }

                    if (!equipmentLayers.isEmpty()){
                        List<ArmorConverter> convertersToProcess = Configuration.armorConverterType.getComposition();
                        Map<ArmorConverter, ConfigurationSection> converterSections = ArmorConverter.createArmorConverterSections(fileEquipementsSection, assetId);

                        for (var layerTypeTuple : equipmentLayers.entrySet()) {
                            String layerType = layerTypeTuple.getKey();
                            Map<ArmorConverter, Set<String>> converterTextures = new HashMap<>();

                            for (var layer : layerTypeTuple.getValue()) {
                                String originalKey = layer.getFirst();
                                String mobTexture = customArmorSection.getString(originalKey);
                                String namespacedTexture = namespaced(mobTexture);

                                if (isValidString(namespacedTexture)) {
                                    String[] split = namespacedTexture.split(":", 2);
                                    String namespace = split[0];
                                    String path = split[1];
                                    String equipmentFolder = layer.getSecond().replace("-","_");

                                    // For layer1 and layer2 (humanoid/humanoid-leggings)
                                    if (originalKey.equals("layer1") || originalKey.equals("layer2")) {
                                        int lastSlash = path.lastIndexOf("/");
                                        String fileName = lastSlash != -1 ? path.substring(lastSlash + 1) : path;

                                        String targetPath = "textures/entity/equipment/" + equipmentFolder + "/";
                                        getConverter().addPackMapping(namespace, "textures/" + path + ".png", namespace, targetPath);

                                        for (ArmorConverter converter : convertersToProcess) {
                                            String convertedPath = converter.getTexturePath(namespace, equipmentFolder,fileName);
                                            converterTextures.computeIfAbsent(converter, k -> new HashSet<>()).add(convertedPath);
                                        }
                                    } else {
                                        // For other types (wolf, llama, horse, etc.)
                                        String pathPrefix = "";
                                        int lastSlash = path.lastIndexOf("/");
                                        if (lastSlash != -1) {
                                            pathPrefix = path.substring(0, lastSlash + 1);
                                        }

                                        String targetPath = "textures/entity/equipment/" + equipmentFolder + "/" + pathPrefix;
                                        getConverter().addPackMapping(namespace, "textures/" + path + ".png", namespace, targetPath);

                                        for (ArmorConverter converter : convertersToProcess) {
                                            converterTextures.computeIfAbsent(converter, k -> new HashSet<>()).add(namespacedTexture);
                                        }
                                    }

//                                    getOrCreateSection(this.craftEngineItemUtils.getSettingsSection(),"equipment").set("asset-id",assetId);
                                    this.craftEngineItemsConfiguration.addItemConfiguration(new EquippableConfiguration(assetId,(EquipmentSlot) null));
                                }
                            }

                            for (Map.Entry<ArmorConverter, Set<String>> entry : converterTextures.entrySet()) {
                                ConfigurationSection section = converterSections.get(entry.getKey());
                                if (isNotNull(section) && !entry.getValue().isEmpty()) {
                                    ArmorConverter.addEquipmentTextures(section, layerType, entry.getValue());
                                }
                            }
                        }

                        String texturePath = packSection.getString("texture");
                        if (isValidString(texturePath)){
                            String namespacedTexturePath = namespaced(texturePath);
                            Map<String, Object> parsedTemplate = InternalTemplateManager.parseTemplate(Template.MODEL_ITEM_GENERATED, "%model_path%", namespacedTexturePath, "%texture_path%", namespacedTexturePath);
                            this.craftEngineItemUtils.getGeneralSection().createSection("model", parsedTemplate);
                        }
                        return;
                    }
                }
            }
            if (this.itemId.endsWith("_helmet") || this.itemId.endsWith("_chestplate") || this.itemId.endsWith("_leggings") || this.itemId.endsWith("_boots")) {
                String texturePath = packSection.getString("texture");
                String modelTexturePath = packSection.getString("model");
                if (isValidString(texturePath) && isNull(modelTexturePath) && !packSection.isConfigurationSection("CustomArmor")){
                    String namespacedTexturePath = namespaced(texturePath);
                    ConfigurationSection fileEquipementsSection = getEquipmentsSection();

                    String assetId = determineAssetId(packSection, List.of("_helmet","_chestplate","_leggings","_boots"));

                    if (isValidString(assetId)){
                        List<ArmorConverter> convertersToProcess = Configuration.armorConverterType.getComposition();
                        Map<ArmorConverter, ConfigurationSection> converterSections = ArmorConverter.createArmorConverterSections(fileEquipementsSection, assetId);

                        String armorName = assetId.split(":", 2)[1];
                        String[] split = namespacedTexturePath.split(":",2);
                        String namespace = split[0];

                        String textureBasePath = namespacedTexturePath.split(":", 2)[1];
                        String textureDir = "";
                        int lastSlashIndex = textureBasePath.lastIndexOf("/");
                        if (lastSlashIndex != -1) {
                            textureDir = textureBasePath.substring(0, lastSlashIndex + 1);
                        }

                        // Layer 1 - Humanoid (helmet, chestplate, boots)
                        String layer1FileName = armorName + "_armor_layer_1";
                        String originalPathLayer1 = "textures/" + textureDir + layer1FileName + ".png";
                        getConverter().addPackMapping(namespace, originalPathLayer1, namespace, "textures/entity/equipment/humanoid/");

                        // Layer 2 - Humanoid-leggings (leggings)
                        String layer2FileName = armorName + "_armor_layer_2";
                        String originalPathLayer2 = "textures/" + textureDir + layer2FileName + ".png";
                        getConverter().addPackMapping(namespace, originalPathLayer2, namespace, "textures/entity/equipment/humanoid_leggings/");

                        for (ArmorConverter converter : convertersToProcess) {
                            ConfigurationSection section = converterSections.get(converter);
                            if (isNotNull(section)) {
                                String layer1Texture = converter.getTexturePath(namespace, "humanoid",layer1FileName);
                                String layer2Texture = converter.getTexturePath(namespace, "humanoid_leggings",layer2FileName);

                                ArmorConverter.addEquipmentTextures(section, "humanoid", Set.of(layer1Texture));
                                ArmorConverter.addEquipmentTextures(section, "humanoid-leggings", Set.of(layer2Texture));
                            }
                        }

//                        getOrCreateSection(this.craftEngineItemUtils.getSettingsSection(),"equipment").set("asset-id",assetId);
                        this.craftEngineItemsConfiguration.addItemConfiguration(new EquippableConfiguration(assetId,(EquipmentSlot) null));
                        Map<String, Object> parsedTemplate = InternalTemplateManager.parseTemplate(Template.MODEL_ITEM_GENERATED, "%model_path%", namespacedTexturePath, "%texture_path%", namespacedTexturePath);
                        this.craftEngineItemUtils.getGeneralSection().createSection("model", parsedTemplate);
                    }
                }
            }
            return;
        }

        modelPath = cleanPath(modelPath);
        if (isNull(modelPath)) {
            Logger.debug(Message.WARNING__CONVERTER__NEXO__MODEL__PROCESS_FAILURE, LogType.WARNING, "item", this.itemId);
            return;
        }

        if (tryBuildTridentModel(packSection, modelPath)) return;
        if (tryBuildShieldModel(packSection, modelPath)) return;
        if (tryBuildPullingModel(packSection)) return;
        if (tryBuildFishingRodModel(packSection, modelPath)) return;

        String namespacedPath = namespaced(modelPath);
        if (isNull(namespacedPath)) {
            Logger.debug(Message.WARNING__CONVERTER__NEXO__MODEL__NAMESPACE_FAILURE, LogType.WARNING, "item", this.itemId);
            return;
        }
        Map<String, Object> parsedTemplate = InternalTemplateManager.parseTemplate(Template.MODEL_ITEM_DEFAULT, "%model_path%", namespacedPath);
        setSavedModelTemplates(parsedTemplate);
        this.craftEngineItemUtils.getGeneralSection().createSection("model", parsedTemplate);
    }

    private void buildElytraModel(ConfigurationSection packSection) {
        String elytraModel = cleanPath(packSection.getString("texture"));
        if (isValidString(elytraModel)) {
            String namespacedElytra = namespaced(elytraModel);
            Map<String, Object> parseTemplate = InternalTemplateManager.parseTemplate(Template.MODEL_ITEM_ELYTRA, "%model_path%", namespacedElytra, "%texture_path%", namespacedElytra, "%broken_model_path%", namespacedElytra, "%broken_texture_path%", namespacedElytra);
            this.craftEngineItemUtils.getGeneralSection().createSection("model", parseTemplate);
            String[] split = namespacedElytra.split(":", 2);
            String itemIdPartTwo = this.itemId.split(":")[1];
//            getOrCreateSection(this.craftEngineItemUtils.getSettingsSection(),"equippable").set("wings", split[0]+":"+itemIdPartTwo);
            this.craftEngineItemsConfiguration.addItemConfiguration(new EquippableConfiguration(this.assetId, split[0]+":"+itemIdPartTwo));
            String string = split[1];
            int lastIndexOf = string.lastIndexOf("/");
            if (lastIndexOf != -1) {
                string = string.substring(0, lastIndexOf)+"/"+itemIdPartTwo;
            }
            String originalPath = "textures/" + string + ".png";
            getConverter().addPackMapping(split[0], originalPath, split[0], "textures/entity/equipment/wings/");
        }
    }

    private boolean tryBuildTridentModel(ConfigurationSection packSection, String modelPath){
        if (this.craftEngineItemsConfiguration.getMaterial() != Material.TRIDENT) return false;
        String namespacedModel = namespaced(modelPath);
        if (isNull(namespacedModel)) return false;
        String throwingModel = packSection.getString("throwing_model", namespacedModel+"_throwing");
        String namespacedThrowingModel = namespaced(throwingModel);
        this.craftEngineItemUtils.getGeneralSection().createSection("model",InternalTemplateManager.parseTemplate(Template.MODEL_TRIDENT, "%model_path%",namespacedModel,"%throwing_model_path%",namespacedThrowingModel));
//        this.craftEngineItemUtils.getSettingsSection().set("projectile",InternalTemplateManager.parseTemplate(Template.SETTINGS_PROJECTILE, "%item_id%", this.itemId));
        this.craftEngineItemsConfiguration.addItemConfiguration(new CecProjectileSettingConfiguration(InternalTemplateManager.parseTemplate(Template.SETTINGS_PROJECTILE, "%item_id%", this.itemId)));
        return true;
    }

    private boolean tryBuildShieldModel(ConfigurationSection packSection, String modelPath) {
        String shieldBlockingModel = packSection.getString("blocking_model");
        if (isValidString(shieldBlockingModel)) {
            shieldBlockingModel = cleanPath(shieldBlockingModel);
            if (isNotNull(shieldBlockingModel)) {
                String namespacedBlocking = namespaced(shieldBlockingModel);
                String namespacedModel = namespaced(modelPath);
                if (isValidString(namespacedModel)) {
                    this.craftEngineItemUtils.getGeneralSection().createSection("model",InternalTemplateManager.parseTemplate(Template.MODEL_3D_SHIELD, "%blocking_model_path%",namespacedBlocking,"%default_model_path%",namespacedModel));
                    return true;
                }
            }
        }
        return false;
    }

    private boolean tryBuildPullingModel(ConfigurationSection packSection) {
        List<String> pullingModels = packSection.getStringList("pulling_models");
        if (pullingModels.isEmpty()) return false;

        if (this.craftEngineItemsConfiguration.getMaterial() == Material.CROSSBOW) {
            buildCrossbowModel(packSection);
            return true;
        } else if (this.craftEngineItemsConfiguration.getMaterial() == Material.BOW) {
            buildBowModel(packSection);
            return true;
        }
        return false;
    }

    private boolean tryBuildFishingRodModel(ConfigurationSection packSection, String modelPath) {
        String castModel = packSection.getString("cast_model");
        if (isValidString(castModel)) {
            castModel = cleanPath(castModel);
            if (isNotNull(castModel)) {
                String namespacedCast = namespaced(castModel);
                String namespacedModel = namespaced(modelPath);
                if (isNotNull(namespacedModel)) {
                    this.craftEngineItemUtils.getGeneralSection().createSection("model",InternalTemplateManager.parseTemplate(Template.MODEL_3D_FISHING_ROD, "%casting_model_path%", namespacedCast, "%default_model_path%", namespacedModel));
                    return true;
                }
            }
        }
        return false;
    }

    private void convertModelWithParent(ConfigurationSection packSection, String parentModel) {
        switch (parentModel) {
            case "item/generated" -> buildGeneratedModel(packSection, "minecraft:item/generated", Template.MODEL_ITEM_GENERATED);
            case "block/cube_all" -> buildGeneratedModel(packSection, "minecraft:block/cube_all", Template.MODEL_CUBE_ALL);
            case "block/cube_top" -> buildCubeTopModel(packSection);
            case "item/handheld" -> buildGeneratedModel(packSection, "minecraft:item/handheld", Template.MODEL_ITEM_HANDHELD);

            default -> Logger.info(Message.WARNING__CONVERTER__NEXO__MODEL__PARENT_NOT_SUPPORTED, LogType.WARNING, "parent", parentModel, "item", this.itemId);
        }
    }

    private void buildGeneratedModel(ConfigurationSection packSection, String parent, Template template) {
        String texturePath = getTexturePath(packSection);
        if (isValidString(texturePath)) {
            String finalTexturePath = namespaced(texturePath);
            String finalModelPath = finalTexturePath;
            if (template.getType() == TemplateType.BLOCK) {
                finalModelPath = filterModelPath(finalTexturePath);
            }
            Map<String, Object> parsedTemplate = InternalTemplateManager.parseTemplate(template, "%model_path%", finalModelPath, "%texture_path%", finalTexturePath);
            ConfigurationSection generalSection = this.craftEngineItemUtils.getGeneralSection();
            if (template.getType() == TemplateType.BLOCK) {
                setSavedModelTemplates(parsedTemplate);
                ConfigurationSection ceModelSection = generalSection.createSection("model");
                ceModelSection.set("path", finalModelPath);
            } else {
                parsedTemplate.put("type", "minecraft:model");
                generalSection.createSection("model", parsedTemplate);
            }
        } else {
            Logger.debug(Message.WARNING__CONVERTER__NEXO__MODEL__GENERATED__MISSING_TEXTURE, LogType.WARNING, "item", this.itemId, "parent", parent);
        }

    }

    private void buildCubeTopModel(ConfigurationSection packSection) {
        String sideTexture = packSection.getString("textures.side");
        String topTexture = packSection.getString("textures.top");

        if (isValidString(sideTexture) && isValidString(topTexture)) {
            String finalSideTexture = namespaced(sideTexture);
            String finalTopTexture = namespaced(topTexture);

            if (isNotNull(finalSideTexture) && isNotNull(finalTopTexture)) {
                String modelPath = finalSideTexture;
                modelPath = filterModelPath(modelPath);
                Map<String, Object> parseTemplate = InternalTemplateManager.parseTemplate(Template.MODEL_CUBE_TOP, "%model_path%", modelPath, "%texture_side_path%", finalSideTexture, "%texture_top_path%", finalTopTexture);
                setSavedModelTemplates(parseTemplate);
                ConfigurationSection ceModelSection = this.craftEngineItemUtils.getGeneralSection().createSection("model");
                ceModelSection.set("path", modelPath);
            } else {
                Logger.debug(Message.WARNING__CONVERTER__NEXO__MODEL__CUBE_TOP__PROCESS_FAILURE, LogType.WARNING, "item", this.itemId);
            }
        } else {
            Logger.debug(Message.WARNING__CONVERTER__NEXO__MODEL__CUBE_TOP__MISSING_TEXTURE, LogType.WARNING, "item", this.itemId);
        }
    }

    private static @NotNull String filterModelPath(String modelPath) {
        for (String key : new String[]{"side","top"}){
            if (modelPath.endsWith("_"+key)){
                modelPath = modelPath.substring(0, modelPath.length() - ("_"+key).length());
            }
        }
        return modelPath;
    }

    /**
     * Determines the asset-id from the itemId or texture
     *
     * @param packSection Nexo configuration section
     * @param suffixesToRemove List of suffixes to remove from the itemId
     * @return The asset-id or null if invalid
     */
    private String determineAssetId(ConfigurationSection packSection, List<String> suffixesToRemove) {
        if (isValidString(this.assetId)){
            return this.assetId;
        }

        String texturePath = packSection.getString("texture");
        if (isValidString(texturePath)){
            String namespacedTexturePath = namespaced(texturePath);
            if (isValidString(namespacedTexturePath)){
                String[] split = this.itemId.split(":", 2);
                String secondPart = removeEndWith(split[1], suffixesToRemove, null);
                if (isValidString(secondPart)){
                    return namespacedTexturePath.split(":", 2)[0] + ":" + secondPart;
                }
            }
        }

        return null;
    }

    private void buildBowModel(ConfigurationSection packSection) {
        String baseModel = namespaced(packSection.getString("model"));
        List<String> pullingModels = packSection.getStringList("pulling_models");
        String pulling0 = namespaced(notEmptyOrNull(pullingModels, 0) ? pullingModels.get(0) : packSection.getString("pulling_0_model"));
        String pulling1 = namespaced(notEmptyOrNull(pullingModels, 1) ? pullingModels.get(1) : packSection.getString("pulling_1_model"));
        String pulling2 = namespaced(notEmptyOrNull(pullingModels, 2) ? pullingModels.get(2) : packSection.getString("pulling_2_model"));

        if (isNotNull(baseModel) && isNotNull(pulling0) && isNotNull(pulling1) && isNotNull(pulling2)) {
            this.craftEngineItemUtils.getGeneralSection().createSection("model",InternalTemplateManager.parseTemplate(Template.MODEL_3D_BOW, "%default_model_path%",baseModel,"%pulling_0_model_path%",pulling0,"%pulling_1_model_path%",pulling1,"%pulling_2_model_path%",pulling2));
        } else {
            Logger.debug(Message.WARNING__CONVERTER__NEXO__MODEL__BOW__PROCESS_FAILURE, LogType.WARNING, "item", this.itemId);
        }
    }

    private void buildCrossbowModel(ConfigurationSection packSection) {
        String baseModel = namespaced(packSection.getString("model"));
        String arrowModel = namespaced(packSection.getString("charged_model"));
        String fireworkModel = namespaced(packSection.getString("firework_model"));

        List<String> pullingModels = packSection.getStringList("pulling_models");
        String pulling0 = namespaced(notEmptyOrNull(pullingModels, 0) ? pullingModels.get(0) : packSection.getString("pulling_0_model"));
        String pulling1 = namespaced(notEmptyOrNull(pullingModels, 1) ? pullingModels.get(1) : packSection.getString("pulling_1_model"));
        String pulling2 = namespaced(notEmptyOrNull(pullingModels, 2) ? pullingModels.get(2) : packSection.getString("pulling_2_model"));

        if (isNotNull(baseModel) && isNotNull(pulling0) && isNotNull(pulling1) && isNotNull(pulling2)){
            this.craftEngineItemUtils.getGeneralSection().createSection("model",InternalTemplateManager.parseTemplate(Template.MODEL_3D_CROSSBOW,"%charged_arrow_model_path%",arrowModel==null?pulling2:arrowModel,"%charged_rocket_model_path%",fireworkModel==null?pulling2:fireworkModel,"%default_model_path%",baseModel,"%pulling_0_model_path%",pulling0,"%pulling_1_model_path%",pulling1,"%pulling_2_model_path%",pulling2));
        } else {
            Logger.debug(Message.WARNING__CONVERTER__NEXO__MODEL__CROSSBOW__PROCESS_FAILURE, LogType.WARNING, "item", this.itemId);
        }
    }

    @Override
    public void convertOther(){
        ConfigurationSection mechanicsSection = this.nexoItemSection.getConfigurationSection("Mechanics");
        if (mechanicsSection == null) return;
        Set<String> mechanicsKeys = mechanicsSection.getKeys(false);
        for (String mechanicsKey : mechanicsKeys) {
            switch(mechanicsKey){
                case "furniture" -> {
                    ConfigurationSection nexoFurnitureSection = mechanicsSection.getConfigurationSection(mechanicsKey);
                    convertFurnitureMechanic(nexoFurnitureSection);
                }
                case "custom_block" -> {
                    ConfigurationSection nexoCustomBlockSection = mechanicsSection.getConfigurationSection(mechanicsKey);
                    convertCustomBlockMechanic(nexoCustomBlockSection);
                }
                //TODO: convert energyblast mechanic
                default -> {}
            }
        }
    }

    private void convertCustomBlockMechanic(ConfigurationSection nexoCustomBlockSection) {
        Map<String, Object> savedModel = getSavedModelTemplates();
        if (savedModel.isEmpty()) return;
        ConfigurationSection ceBehaviorSection = this.craftEngineItemUtils.getBehaviorSection();
        ceBehaviorSection.set("type", "block_item");
        String nexoCustomBlockType = nexoCustomBlockSection.getString("type","NOTEBLOCK");
        int customVariation = nexoCustomBlockSection.getInt("custom_variation", -1);
        ConfigurationSection ceBlockSection = getOrCreateSection(ceBehaviorSection, "block");
        ConfigurationSection ceStateSection = getOrCreateSection(ceBlockSection, "state");
        String state;
        if (nexoCustomBlockType.equals("CHORUSBLOCK")){
            state = "leaves";
            if (customVariation >= 0){
                BlockData blockData = Bukkit.createBlockData(Material.CHORUS_PLANT);
                if (blockData instanceof MultipleFacing multipleFacing) {

                    multipleFacing.setFace(BlockFace.NORTH, (customVariation & 0b000001) != 0);
                    multipleFacing.setFace(BlockFace.SOUTH, (customVariation & 0b000010) != 0);
                    multipleFacing.setFace(BlockFace.EAST,  (customVariation & 0b000100) != 0);
                    multipleFacing.setFace(BlockFace.WEST,  (customVariation & 0b001000) != 0);
                    multipleFacing.setFace(BlockFace.UP,    (customVariation & 0b010000) != 0);
                    multipleFacing.setFace(BlockFace.DOWN,  (customVariation & 0b100000) != 0);

                    BlockStatesMapper.getInstance().storeMapping(
                        this.getConverter().getPluginType(),
                        blockData,
                        this.itemId
                    );
                }
            }
        } else if (nexoCustomBlockType.equals("TRIPWIRE")){
            state = "tripwire";
            if (customVariation >= 0){
                BlockData blockData = Bukkit.createBlockData(Material.TRIPWIRE);
                if (blockData instanceof Tripwire tripwire) {

                    tripwire.setFace(BlockFace.NORTH, (customVariation & 0b0000001) != 0);
                    tripwire.setFace(BlockFace.SOUTH, (customVariation & 0b0000010) != 0);
                    tripwire.setFace(BlockFace.EAST,  (customVariation & 0b0000100) != 0);
                    tripwire.setFace(BlockFace.WEST,  (customVariation & 0b0001000) != 0);

                    tripwire.setAttached((customVariation & 0b0010000) != 0);

                    tripwire.setDisarmed((customVariation & 0b0100000) != 0);

                    tripwire.setPowered((customVariation & 0b1000000) != 0);

                    BlockStatesMapper.getInstance().storeMapping(
                        this.getConverter().getPluginType(),
                        blockData,
                        this.itemId
                    );
                }
            }
        } else {
            state = "solid";
            if (customVariation >= 0){
                int notesPerInstrument = Note.Tone.TONES_COUNT * 2;
                int variationsPerInstrument = notesPerInstrument * 2;

                int instrumentIndex = (customVariation - 1) / variationsPerInstrument;
                int noteIndex = ((customVariation - 1) % variationsPerInstrument) % notesPerInstrument;
                boolean powered = ((customVariation - 1) % variationsPerInstrument) >= notesPerInstrument;

                if (instrumentIndex == 0 && !powered) {
                    noteIndex = noteIndex + 1;
                }

                int note = noteIndex;


                Instrument instrument = Instrument.values()[instrumentIndex];
                String instrumentName = switch (instrument) {
                    case PIANO -> "harp";
                    case BASS_DRUM -> "basedrum";
                    case SNARE_DRUM -> "snare";
                    case STICKS -> "hat";
                    case BASS_GUITAR -> "bass";
                    case FLUTE -> "flute";

                    default -> instrument.name().toLowerCase();
                };
                String dataString = "[instrument="
                        + instrumentName +
                        ",note=" + note +
                        ",powered=" + powered + "]";

                try {
                    BlockData blockData = Bukkit.createBlockData(Material.NOTE_BLOCK, dataString);
                    BlockStatesMapper.getInstance().storeMapping(this.getConverter().getPluginType(), blockData, this.itemId);
                } catch (IllegalArgumentException e) {
                    Logger.debug(Message.WARNING__CONVERTER__NEXO__CUSTOM_BLOCK__BLOCK_DATA_FAILURE, LogType.WARNING, "variation", customVariation, "item", this.itemId);
                    e.printStackTrace();
                }
            }
        }
        ceStateSection.set("auto-state",state);
        ceStateSection.createSection("model", savedModel);
        ConfigurationSection sounds = nexoCustomBlockSection.getConfigurationSection("block_sounds");
        if (sounds != null) {
            ConfigurationSection settings = getOrCreateSection(ceBlockSection, "settings");
            for (String soundKey : new String[]{"place_sound","break_sound","hit_sound","step_sound","fall_sound"}) {
                String soundValue = sounds.getString(soundKey);
                if (isValidString(soundValue)) {
                    ConfigurationSection ceSoundsSection = getOrCreateSection(settings, "sounds");
                    String ceSoundKey = soundKey.replace("_sound", "");
                    ceSoundsSection.set(ceSoundKey, soundValue);
                }
            }
        }
        double hardness = nexoCustomBlockSection.getDouble("hardness",2.0);
        if (hardness >= 0 && hardness != 2.0){
            ConfigurationSection settings = this.craftEngineItemUtils.getSettingsSection();
            settings.set("hardness", hardness);
        }
        boolean canBeBeaconBaseBlock = nexoCustomBlockSection.getBoolean("beacon_base_block",false);
        if (canBeBeaconBaseBlock){
            ConfigurationSection settings = this.craftEngineItemUtils.getSettingsSection();
            List<String> blockTags = settings.getStringList("tags");
            if (!blockTags.contains("minecraft:beacon_base_blocks")){
                blockTags.add("minecraft:beacon_base_blocks");
                settings.set("tags",blockTags);
            }
        }
        boolean isFallingBlock = nexoCustomBlockSection.getBoolean("is_falling",false);
        if (isFallingBlock){
            ConfigurationSection ceBlockBehaviorSection = getOrCreateSection(ceBlockSection, "behavior");
            ceBlockBehaviorSection.set("type","falling_block");
        }
        ConfigurationSection nexoSaplingSection = nexoCustomBlockSection.getConfigurationSection("sapling");
        if (isNotNull(nexoSaplingSection)){
            Logger.debug(Message.WARNING__CONVERTER__NEXO__CUSTOM_BLOCK__SAPLING_NOT_SUPPORTED, LogType.WARNING, "item", this.itemId);
            // TODO implement sapling behavior conversion
            boolean growsNaturally = nexoSaplingSection.getBoolean("grows_naturally",true);
            if (!growsNaturally){
                Logger.info(Message.WARNING__CONVERTER__NEXO__CUSTOM_BLOCK__SAPLING_NATURAL_ONLY, LogType.INFO, "item", this.itemId);
            }
        }
        ConfigurationSection nexoDropSection = nexoCustomBlockSection.getConfigurationSection("drop");
        if (isNotNull(nexoDropSection)){
            boolean dropSelfWithSilktouch = nexoDropSection.getBoolean("silktouch",false);
            boolean fortuneAffectsDrop = nexoDropSection.getBoolean("fortune",false);
            String minimalType = nexoDropSection.getString("minimal_type",null);
            String bestTool = nexoDropSection.getString("best_tool",null);
            if (isValidString(minimalType)){
                NexoMinimalType nexoMinimalType = null;
                try {
                    nexoMinimalType = NexoMinimalType.valueOf(minimalType.toUpperCase());
                } catch (IllegalArgumentException e){
                    Logger.debug(Message.WARNING__CONVERTER__NEXO__CUSTOM_BLOCK__UNKNOWN_MINIMAL_TYPE, LogType.WARNING, "type", minimalType, "item", this.itemId);
                }
                if (isNotNull(nexoMinimalType)){
                    ConfigurationSection ceBlockSettings = getOrCreateSection(ceBlockSection, "settings");
                    ceBlockSettings.set("require-correct-tools",true);
                    ceBlockSettings.set("correct-tools", nexoMinimalType.getCorrectTools());
                }
            }
            if (isValidString(bestTool)){
                NexoBestTool nexoBestTool = null;
                try {
                    nexoBestTool = NexoBestTool.valueOf(bestTool.toUpperCase());
                } catch (IllegalArgumentException e){
                    Logger.debug(Message.WARNING__CONVERTER__NEXO__CUSTOM_BLOCK__UNKNOWN_BEST_TOOL, LogType.WARNING, "tool", bestTool, "item", this.itemId);
                }
                if (isNotNull(nexoBestTool)){
                    ConfigurationSection ceBlockSettings = getOrCreateSection(ceBlockSection, "settings");
                    ceBlockSettings.set("tags", ceBlockSettings.getStringList("tags").add(nexoBestTool.getBestTool()));
                }
            }
            //TODO: implement drop conversion according to silktouch and fortune
        }
    }

    private void convertFurnitureMechanic(ConfigurationSection nexoFurnitureMechanicsSection) {
        String nexoMEGModel = nexoFurnitureMechanicsSection.getString("modelengine_id");
        String nexoBetterModel = nexoFurnitureMechanicsSection.getString("better-model");

        FurnitureConfiguration furnitureConfiguration = new FurnitureConfiguration();

        // --- Sounds ---
        ConfigurationSection nexoBlockSoundSection = nexoFurnitureMechanicsSection.getConfigurationSection("block_sounds");
        if (isNotNull(nexoBlockSoundSection)) {
            FurnitureConfiguration.Settings settings = furnitureConfiguration.getOrCreateSettings(this.itemId);
            settings.setPlaceSound(nexoBlockSoundSection.getString("place_sound"));
            settings.setBreakSound(nexoBlockSoundSection.getString("break_sound"));
            // hit_sound / step_sound / fall_sound are not supported in CE for furniture
        }

        // --- Rotation ---
        FurnitureRotation furnitureRotation = FurnitureRotation.EIGHT;
        if (!nexoFurnitureMechanicsSection.getBoolean("rotatable", true)) {
            furnitureRotation = FurnitureRotation.FOUR;
        }
        String restrictedRotation = nexoFurnitureMechanicsSection.getString("restricted_rotation");
        if (isValidString(restrictedRotation)) {
            if (restrictedRotation.equals("VERY_STRICT")) {
                furnitureRotation = FurnitureRotation.FOUR;
            } else if (restrictedRotation.equals("STRICT")) {
                furnitureRotation = FurnitureRotation.EIGHT;
            }
        }

        // --- Seats ---
        FloatsUtils seatPosition = new FloatsUtils(3, new float[]{0f, 0f, 0f});
        List<String> seats = nexoFurnitureMechanicsSection.getStringList("seats");
        if (!seats.isEmpty()) {
            String seat = seats.getFirst();
            String[] split = seat.split(",", 3);
            try {
                seatPosition.setValue(0, Float.parseFloat(split[0].trim()));
                seatPosition.setValue(1, Float.parseFloat(split[1].trim()));
                seatPosition.setValue(2, Float.parseFloat(split[2].trim()));
            } catch (Exception e) {
                Logger.debug(Message.WARNING__FURNITURE__INVALID_SEAT_FORMAT, LogType.WARNING, "item", this.itemId, "value", seat);
            }
        }

        // --- Display properties ---
        net.momirealms.craftengine.core.entity.display.Billboard transformType = net.momirealms.craftengine.core.entity.display.Billboard.FIXED;
        ItemDisplayType displayType = ItemDisplayType.FIXED;
        FloatsUtils displayTranslation = new FloatsUtils(3, new float[]{0f, 0.5f, 0f});
        FloatsUtils scale = new FloatsUtils(3, new float[]{1f, 1f, 1f});

        ConfigurationSection nexoPropertiesSection = nexoFurnitureMechanicsSection.getConfigurationSection("properties");
        if (isNotNull(nexoPropertiesSection)) {
            String display_transform = nexoPropertiesSection.getString("display_transform", "NONE");
            try {
                displayType = ItemDisplayType.valueOf(display_transform);
            } catch (IllegalArgumentException e) {
                Logger.debug(Message.WARNING__FURNITURE__UNKNOWN_DISPLAY_TRANSFORM, LogType.WARNING, "item", this.itemId, "transform", display_transform);
                displayType = ItemDisplayType.NONE;
            }
            String tracking_rotation = nexoPropertiesSection.getString("tracking_rotation", net.momirealms.craftengine.core.entity.display.Billboard.FIXED.name());
            try {
                transformType = Billboard.valueOf(tracking_rotation);
            } catch (IllegalArgumentException e) {
                Logger.debug(Message.WARNING__FURNITURE__UNKNOWN_TRACKING_ROTATION, LogType.WARNING, "item", this.itemId, "rotation", tracking_rotation);
            }
            List<Float> translations = nexoPropertiesSection.getFloatList("translation");
            if (translations.size() >= 3) {
                displayTranslation.setValue(0, translations.get(0));
                displayTranslation.setValue(1, translations.get(1));
                displayTranslation.setValue(2, translations.get(2));
            } else if (!translations.isEmpty()) {
                Logger.debug(Message.WARNING__FURNITURE__INVALID_TRANSLATION_SIZE, LogType.WARNING, "item", this.itemId, "size", translations.size());
            }
            String scales = nexoPropertiesSection.getString("scale");
            if (isNotNull(scales)) {
                String[] split = scales.split(",", 3);
                try {
                    scale.setValue(0, Float.parseFloat(split[0].trim()));
                    scale.setValue(1, Float.parseFloat(split[1].trim()));
                    scale.setValue(2, Float.parseFloat(split[2].trim()));
                } catch (Exception e) {
                    Logger.debug(Message.WARNING__FURNITURE__INVALID_SCALE_FORMAT, LogType.WARNING, "item", this.itemId, "value", scales);
                }
            }
        }

        // --- Loot ---
        ConfigurationSection dropSection = nexoFurnitureMechanicsSection.getConfigurationSection("drop");
        if (isNotNull(dropSection)) {
            boolean dropSelfWithSilktouch = dropSection.getBoolean("silktouch", false);
            boolean fortuneAffectsDrop = dropSection.getBoolean("fortune", false);
            String minimal_type = dropSection.getString("minimal_type", null);
            String best_tool = dropSection.getString("best_tool", null);
            List<Map<?, ?>> loots = dropSection.getMapList("loots");
            List<ItemLoot> itemLoots = new ArrayList<>();
            for (Map<?, ?> lootMap : loots) {
                ItemLoot itemLoot = null;
                int minAmount = 1;
                int maxAmount = 1;
                Object amount = lootMap.get("amount");
                if (amount instanceof Integer intAmount) {
                    minAmount = intAmount;
                    maxAmount = intAmount;
                } else if (amount instanceof String amountString) {
                    String[] split = amountString.split("\\.\\.", 2);
                    try {
                        minAmount = Integer.parseInt(split[0].trim());
                        maxAmount = split.length == 2 ? Integer.parseInt(split[1].trim()) : minAmount;
                    } catch (NumberFormatException e) {
                        Logger.debug(Message.WARNING__FURNITURE__INVALID_AMOUNT_FORMAT, LogType.WARNING, "item", this.itemId, "amount", amountString);
                    }
                }
                float probability = 1.0f;
                Object probObj = lootMap.get("probability");
                if (probObj instanceof Number num) {
                    probability = num.floatValue();
                } else if (probObj instanceof String probString) {
                    try {
                        probability = Float.parseFloat(probString);
                    } catch (NumberFormatException e) {
                        Logger.debug(Message.WARNING__FURNITURE__INVALID_PROBABILITY_FORMAT, LogType.WARNING, "item", this.itemId, "probability", probString);
                    }
                }
                if (lootMap.get("nexo_item") instanceof String nexoItemString) {
                    itemLoot = new CraftEngineItemLoot(nexoItemString, minAmount, maxAmount, probability);
                } else if (lootMap.get("minecraft_type") instanceof String minecraftTypeString) {
                    itemLoot = new MinecraftItemLoot(minecraftTypeString, minAmount, maxAmount, probability);
                }
                if (isNotNull(itemLoot)) itemLoots.add(itemLoot);
            }
            if (isValidString(minimal_type) || isValidString(best_tool)) {
                Logger.debug(Message.WARNING__FURNITURE__CUSTOM_DROP_CONDITIONS_NOT_SUPPORTED, LogType.WARNING, "item", this.itemId);
            }
            if (dropSelfWithSilktouch && !fortuneAffectsDrop) {
                furnitureConfiguration.setLoot(InternalTemplateManager.parseTemplate(Template.LOOT_TABLE_SILK_TOUCH_ONLY, "%type%", "item", "%item%", this.itemId));
            } else if (!dropSelfWithSilktouch && fortuneAffectsDrop) {
                if (itemLoots.isEmpty()) {
                    Logger.info(Message.WARNING__FURNITURE__FORTUNE_DROP_NO_LOOTS, "item", this.itemId);
                }
                // TODO: Wait for CE support
            } else if (dropSelfWithSilktouch) {
                // TODO: Wait for CE support (fortuneAffectsDrop == true)
            }
        } else {
            furnitureConfiguration.setLoot(InternalTemplateManager.parseTemplate(Template.LOOT_TABLE_BASIC_DROP, "%type%", "furniture_item", "%item%", this.itemId));
        }

        // --- Placements ---
        Set<FurniturePlacement> placementKeys = new HashSet<>();
        ConfigurationSection limitedPlacingSection = nexoFurnitureMechanicsSection.getConfigurationSection("limited_placing");
        if (isNotNull(limitedPlacingSection)) {
            if (limitedPlacingSection.getBoolean("floor", false)) placementKeys.add(FurniturePlacement.GROUND);
            if (limitedPlacingSection.getBoolean("wall", false)) placementKeys.add(FurniturePlacement.WALL);
            if (limitedPlacingSection.getBoolean("roof", false)) placementKeys.add(FurniturePlacement.CEILING);
        } else {
            placementKeys.addAll(List.of(FurniturePlacement.values()));
        }

        if (!placementKeys.isEmpty()) {
            if (isValidString(nexoBetterModel) || isValidString(nexoMEGModel)) {
                for (FurniturePlacement placement : placementKeys) {
                    FurnitureConfiguration.Placement p = furnitureConfiguration.getOrCreatePlacement(placement);
                    if (isValidString(nexoBetterModel)) p.setBetterModel(nexoBetterModel);
                    else p.setModelEngine(nexoMEGModel);
                }
            } else {
                // Build element
                FurnitureConfiguration.ItemDisplayElement element = new FurnitureConfiguration.ItemDisplayElement(this.itemId);
                element.setDisplayTransform(displayType);
                element.display().setBillboard(transformType);
                element.display().setTranslation(displayTranslation.getValue(0), displayTranslation.getValue(1), displayTranslation.getValue(2));
                element.display().setScale(scale.getValue(0), scale.getValue(1), scale.getValue(2));

                // Build hitboxes
                List<FurnitureConfiguration.Hitbox> hitboxList = new ArrayList<>();
                ConfigurationSection nexoHitboxesSection = nexoFurnitureMechanicsSection.getConfigurationSection("hitbox");
                if (isNotNull(nexoHitboxesSection)) {
                    AtomicBoolean seatsAdded = new AtomicBoolean(false);

                    // Barriers
                    for (Position pos : expandBarrierPositions(nexoHitboxesSection.getStringList("barriers"))) {
                        FurnitureConfiguration.ShulkerHitbox hitbox = new FurnitureConfiguration.ShulkerHitbox();
                        hitbox.setPosition(pos.x(), pos.y(), pos.z());
                        hitboxList.add(hitbox);
                    }

                    // Shulkers
                    for (String shulker : nexoHitboxesSection.getStringList("shulkers")) {
                        if (!isValidString(shulker)) continue;
                        String[] parts = shulker.trim().split("\\s+");
                        if (parts.length < 3) {
                            Logger.debug(Message.WARNING__FURNITURE__INVALID_SHULKER_ENTRY, LogType.WARNING, "item", this.itemId, "entry", shulker);
                            continue;
                        }
                        String[] coords = parts[0].split("\\s*,\\s*");
                        if (coords.length != 3) continue;
                        try {
                            FurnitureConfiguration.ShulkerHitbox hitbox = new FurnitureConfiguration.ShulkerHitbox();
                            hitbox.setPosition(Float.parseFloat(coords[0]), Float.parseFloat(coords[1]), Float.parseFloat(coords[2]));
                            hitbox.setScale(Float.parseFloat(parts[1]));
                            hitbox.setPeek((int) (Float.parseFloat(parts[2]) * 100));
                            if (parts.length >= 4) {
                                String dir = parts[3].toUpperCase();
                                hitbox.setDirection(getDirectionFromString(dir));
                            }
                            if (seatPosition.isUpdated() && !seatsAdded.getAndSet(true))
                                hitbox.addSeat(seatPosition.getValue(0), seatPosition.getValue(1), seatPosition.getValue(2), 0);
                            hitboxList.add(hitbox);
                        } catch (NumberFormatException e) {
                            Logger.debug(Message.WARNING__FURNITURE__NON_NUMERIC_SHULKER_VALUES, LogType.WARNING, "item", this.itemId, "entry", shulker);
                        }
                    }

                    // Ghasts
                    for (String ghast : nexoHitboxesSection.getStringList("ghasts")) {
                        if (!isValidString(ghast)) continue;
                        String[] parts = ghast.trim().split("\\s+");
                        if (parts.length < 2) {
                            Logger.debug(Message.WARNING__FURNITURE__INVALID_GHAST_ENTRY, LogType.WARNING, "item", this.itemId, "entry", ghast);
                            continue;
                        }
                        String[] coords = parts[0].split("\\s*,\\s*");
                        if (coords.length != 3) continue;
                        try {
                            FurnitureConfiguration.HappyGhastHitbox hitbox = new FurnitureConfiguration.HappyGhastHitbox();
                            hitbox.setPosition(Float.parseFloat(coords[0]), Float.parseFloat(coords[1]), Float.parseFloat(coords[2]));
                            hitbox.setScale(Float.parseFloat(parts[1]));
                            if (seatPosition.isUpdated() && !seatsAdded.getAndSet(true))
                                hitbox.addSeat(seatPosition.getValue(0), seatPosition.getValue(1), seatPosition.getValue(2), 0);
                            hitboxList.add(hitbox);
                        } catch (NumberFormatException e) {
                            Logger.debug(Message.WARNING__FURNITURE__NON_NUMERIC_GHAST_VALUES, LogType.WARNING, "item", this.itemId, "entry", ghast);
                        }
                    }

                    // Interactions
                    for (String interaction : nexoHitboxesSection.getStringList("interactions")) {
                        if (!isValidString(interaction)) continue;
                        String[] parts = interaction.trim().split("\\s+");
                        if (parts.length != 2) {
                            Logger.debug(Message.WARNING__FURNITURE__INVALID_INTERACTION_ENTRY, LogType.WARNING, "item", this.itemId, "entry", interaction);
                            continue;
                        }
                        String[] coords = parts[0].split("\\s*,\\s*");
                        String[] size = parts[1].split("\\s*,\\s*");
                        if (coords.length != 3 || size.length != 2) continue;
                        try {
                            FurnitureConfiguration.InteractionHitbox hitbox = new FurnitureConfiguration.InteractionHitbox();
                            hitbox.setPosition(Float.parseFloat(coords[0]), Float.parseFloat(coords[1]), Float.parseFloat(coords[2]));
                            hitbox.setWidth(Float.parseFloat(size[0]));
                            hitbox.setHeight(Float.parseFloat(size[1]));
                            hitboxList.add(hitbox);
                        } catch (NumberFormatException e) {
                            Logger.debug(Message.WARNING__FURNITURE__NON_NUMERIC_INTERACTION_VALUES, LogType.WARNING, "item", this.itemId, "entry", interaction);
                        }
                    }
                }

                for (FurniturePlacement furniturePlacement : placementKeys) {
                    FurnitureConfiguration.Placement p = furnitureConfiguration.getOrCreatePlacement(furniturePlacement);
                    FurnitureConfiguration.Rules rules = p.getRules();
                    rules.setRotation(furnitureRotation);
                    p.addElement(element);
                    hitboxList.forEach(p::addHitbox);
                }
            }
        }

        this.getCraftEngineItemsConfiguration().addItemConfiguration(furnitureConfiguration);
    }

    private List<Position> expandBarrierPositions(List<String> barriersList) {
        List<Position> result = new ArrayList<>();
        Set<String> duplicateGuard = new HashSet<>();

        for (String barrier : barriersList) {
            if (!isValidString(barrier)) continue;
            String[] parts = barrier.trim().split("\\s*,\\s*");
            if (parts.length != 3) {
                Logger.debug(Message.WARNING__FURNITURE__INVALID_BARRIER_ENTRY, LogType.WARNING, "item", this.itemId, "entry", barrier);
                continue;
            }

            int[][] axisValues = new int[3][];
            for (int i = 0; i < 3; i++) {
                axisValues[i] = parseAxisPart(parts[i], barrier);
                if (axisValues[i].length == 0) {
                    axisValues[i] = new int[]{0};
                }
            }

            for (int x : axisValues[0]) {
                for (int y : axisValues[1]) {
                    for (int z : axisValues[2]) {
                        String key = x + "," + y + "," + z;
                        if (duplicateGuard.add(key)) {
                            result.add(new Position(x, y, z));
                        }
                    }
                }
            }
        }
        return result;
    }

    private int[] parseAxisPart(String part, String original) {
        part = part.trim();
        if (part.isEmpty()) return new int[0];

        if (part.contains("..")) {
            String[] rangeSplit = part.split("\\.\\.");
            if (rangeSplit.length != 2) {
                Logger.debug(Message.WARNING__FURNITURE__INVALID_BARRIER_RANGE, LogType.WARNING, "range", part, "entry", original);
                return new int[0];
            }
            try {
                int start = Integer.parseInt(rangeSplit[0].trim());
                int end = Integer.parseInt(rangeSplit[1].trim());
                int min = Math.min(start, end);
                int max = Math.max(start, end);
                int[] values = new int[max - min + 1];
                for (int i = 0; i < values.length; i++) {
                    values[i] = min + i;
                }
                return values;
            } catch (NumberFormatException e) {
                Logger.debug(Message.WARNING__FURNITURE__NON_NUMERIC_BARRIER_RANGE_BOUNDS, LogType.WARNING, "range", part, "entry", original);
                return new int[0];
            }
        } else {
            try {
                return new int[]{Integer.parseInt(part)};
            } catch (NumberFormatException e) {
                Logger.debug(Message.WARNING__FURNITURE__NON_NUMERIC_BARRIER_VALUE, LogType.WARNING, "value", part, "entry", original);
                return new int[0];
            }
        }
    }

    public Direction getDirectionFromString(String direction) {
        return switch (direction.toUpperCase()) {
            case "DOWN" -> Direction.DOWN;
            case "NORTH" -> Direction.NORTH;
            case "SOUTH" -> Direction.SOUTH;
            case "EAST" -> Direction.EAST;
            case "WEST" -> Direction.WEST;
            default -> Direction.UP;
        };
    }
}