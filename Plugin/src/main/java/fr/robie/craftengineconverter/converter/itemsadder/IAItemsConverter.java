package fr.robie.craftengineconverter.converter.itemsadder;

import fr.robie.craftengineconverter.common.enums.BukkitFlagToComponentFlag;
import fr.robie.craftengineconverter.common.enums.ComponentFlag;
import fr.robie.craftengineconverter.common.enums.CraftEngineBlockState;
import fr.robie.craftengineconverter.common.enums.Plugins;
import fr.robie.craftengineconverter.common.format.Message;
import fr.robie.craftengineconverter.common.items.*;
import fr.robie.craftengineconverter.common.logger.Logger;
import fr.robie.craftengineconverter.common.utils.CecAttributeModifier;
import fr.robie.craftengineconverter.common.utils.FloatsUtils;
import fr.robie.craftengineconverter.common.utils.enums.BlockParent;
import fr.robie.craftengineconverter.common.utils.enums.FurniturePlacement;
import fr.robie.craftengineconverter.common.utils.enums.ItemDisplayType;
import fr.robie.craftengineconverter.common.utils.enums.Template;
import fr.robie.craftengineconverter.common.utils.enums.ia.IADirectionalMode;
import fr.robie.craftengineconverter.common.utils.enums.ia.IAEntityTypes;
import fr.robie.craftengineconverter.common.utils.enums.ia.IAModelsKeys;
import fr.robie.craftengineconverter.common.utils.enums.ia.IAPlacedModelTypes;
import fr.robie.craftengineconverter.converter.Converter;
import fr.robie.craftengineconverter.converter.ItemConverter;
import fr.robie.craftengineconverter.utils.manager.InternalTemplateManager;
import net.momirealms.craftengine.core.attribute.AttributeModifier;
import net.momirealms.craftengine.core.entity.EquipmentSlot;
import net.momirealms.craftengine.core.entity.display.Billboard;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemFlag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class IAItemsConverter extends ItemConverter {
    private final ConfigurationSection iaItemSection;
    private final String namespace;

    public IAItemsConverter(@NotNull String itemId, ConfigurationSection craftEngineItemSection, Converter converter, YamlConfiguration fileConfig, ConfigurationSection iaItemSection, String namespace) {
        super(itemId, craftEngineItemSection, converter, fileConfig);
        this.iaItemSection = iaItemSection;
        this.namespace = namespace;
    }

    @Override
    public void convertMaterial(){
        ConfigurationSection resourceSection = this.iaItemSection.getConfigurationSection("resource");
        if (isNotNull(resourceSection)){
            try {
                this.craftEngineItemsConfiguration.setMaterial(Material.valueOf(resourceSection.getString("material","").toUpperCase()));
            } catch (Exception ignored) {
            }
        }
    }

    @Override
    public void convertItemName(){
        String itemName = this.iaItemSection.getString("name", this.iaItemSection.getString("display_name"));
        if (isValidString(itemName)){
            this.craftEngineItemsConfiguration.addItemConfiguration(new ItemNameConfiguration(itemName));
        }
    }

    @Override
    public void convertLore(){
        List<String> lore = this.iaItemSection.getStringList("lore");
        if (!lore.isEmpty()){
            this.craftEngineItemsConfiguration.addItemConfiguration(new LoreConfiguration(lore));
        }
    }

    @Override
    public void convertDyedColor(){
        Object color = this.iaItemSection.get("graphics.color");
        if (isNotNull(color)){
            this.craftEngineItemsConfiguration.addItemConfiguration(new DyedColorConfiguration(color));
        }
    }

    @Override
    public void convertUnbreakable(){
        ConfigurationSection durabilitySection = this.iaItemSection.getConfigurationSection("durability");
        if (isNotNull(durabilitySection)){
            boolean unbreakable = durabilitySection.getBoolean("unbreakable", false);
            if (unbreakable){
                this.craftEngineItemsConfiguration.addItemConfiguration(new UnbreakableConfiguration(true));
            }
        }
    }

    @Override
    public void convertItemFlags(){
        List<String> itemFlags = this.iaItemSection.getStringList("item_flags");
        if (!itemFlags.isEmpty()){
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
    public void convertAttributeModifiers(){
        ConfigurationSection attributesSection = this.iaItemSection.getConfigurationSection("attribute_modifiers");
        if (isNotNull(attributesSection)) {
            List<CecAttributeModifier> attributeModifiers = new ArrayList<>();

            for (String equipmentSlot : attributesSection.getKeys(false)) {
                ConfigurationSection slotSection = attributesSection.getConfigurationSection(equipmentSlot);
                if (isNull(slotSection)) continue;
                AttributeModifier.Slot slot;
                try {
                    slot = AttributeModifier.Slot.valueOf(equipmentSlot.toUpperCase());
                } catch (Exception e) {
                    Logger.debug("[IAItemsConverter] Invalid equipment slot " + equipmentSlot + " for attribute modifiers for item " + this.itemId);
                    continue;
                }
                for (String attributeKey : slotSection.getKeys(false)) {
                    try {
                        Attribute attribute = Registry.ATTRIBUTE.getOrThrow(NamespacedKey.fromString(attributeKey));
                        int amount = slotSection.getInt(attributeKey);
                        attributeModifiers.add(new CecAttributeModifier(attribute.name(), slot, null, amount, AttributeModifier.Operation.ADD_VALUE, null));
                    } catch (Exception e) {
                        Logger.debug("[IAItemsConverter] Invalid attribute " + attributeKey + " for attribute modifiers for item " + this.itemId);
                    }
                }
            }

            if (!attributeModifiers.isEmpty())
                this.craftEngineItemsConfiguration.addItemConfiguration(new AttributeModifiersConfiguration(attributeModifiers));
        }
    }

    @Override
    public void convertEnchantments(){
        ConfigurationSection enchantsSection = this.iaItemSection.getConfigurationSection("enchants");
        if (isNotNull(enchantsSection)){
            EnchantmentConfiguration enchantmentConfiguration = new EnchantmentConfiguration();
            for (String enchantmentKey : enchantsSection.getKeys(false)){
                int enchantLevel = enchantsSection.getInt(enchantmentKey, 1);
                enchantmentConfiguration.addEnchantment(enchantmentKey, enchantLevel);
            }
            if (enchantmentConfiguration.hasEnchantments())
                this.craftEngineItemsConfiguration.addItemConfiguration(enchantmentConfiguration);
        }
        List<String> enchantments = this.iaItemSection.getStringList("enchants");
        if (!enchantments.isEmpty()){
            EnchantmentConfiguration enchantmentConfiguration = new EnchantmentConfiguration();
            for (String enchantmentEntry : enchantments){
                String enchantName;
                int enchantLevel = 1;
                int lastIndexOf = enchantmentEntry.lastIndexOf(':');
                if (lastIndexOf != -1){
                    enchantName = enchantmentEntry.substring(0, lastIndexOf);
                    try {
                        enchantLevel = Integer.parseInt(enchantmentEntry.substring(lastIndexOf + 1));
                    } catch (NumberFormatException ignored){
                    }
                } else {
                    enchantName = enchantmentEntry;
                }
                enchantmentConfiguration.addEnchantment(enchantName, enchantLevel);
            }
            if (enchantmentConfiguration.hasEnchantments())
                this.craftEngineItemsConfiguration.addItemConfiguration(enchantmentConfiguration);
        }
    }

    @Override
    public void convertCustomModelData(){
        ConfigurationSection resourceSection = this.iaItemSection.getConfigurationSection("resource");
        if (isNotNull(resourceSection)){
            int customModelData = resourceSection.getInt("custom_model_data", resourceSection.getInt("model_id", 0));
            if (customModelData != 0){
                this.craftEngineItemsConfiguration.addItemConfiguration(new CustomModelDataConfiguration(customModelData));
            }
        }
    }

    @Override
    public void convertItemModel(){
        String itemModel = this.iaItemSection.getString("item_model");
        if (isValidString(itemModel)){
            this.craftEngineItemsConfiguration.addItemConfiguration(new ItemModelConfiguration(itemModel));
        }
    }

    @Override
    public void convertMaxStackSize(){
        int maxStackSize = this.iaItemSection.getInt("max_stack_size", -1);
        if (maxStackSize > 0 && maxStackSize <= 99){
            this.craftEngineItemsConfiguration.addItemConfiguration(new MaxStackSizeConfiguration(maxStackSize));
        }
    }

    @Override
    public void convertEnchantmentGlintOverride(){
        if (this.iaItemSection.getBoolean("glint", false)){
            this.craftEngineItemsConfiguration.addItemConfiguration(new EnchantmentGlintOverrideConfiguration(true));
        }
    }

    @Override
    public void convertFireResistance(){
        // Not supported ?
    }

    @Override
    public void convertMaxDamage(){
        ConfigurationSection durability = this.iaItemSection.getConfigurationSection("durability");
        if (isNotNull(durability)){
            int maxDamage = durability.getInt("max_durability", -1);
            if (maxDamage > 0){
                this.craftEngineItemsConfiguration.addItemConfiguration(new MaxDamageConfiguration(maxDamage));
            }
        }
    }

    @Override
    public void convertGlowDropColor(){
        ConfigurationSection dropSection = this.iaItemSection.getConfigurationSection("drop");
        if (isNotNull(dropSection)){
            ConfigurationSection glowSection = dropSection.getConfigurationSection("glow");
            if (isNotNull(glowSection)){
                boolean glow = glowSection.getBoolean("enabled", false);
                if (glow){
                    String color = glowSection.getString("color");
                    try {
                        this.craftEngineItemsConfiguration.addItemConfiguration(new GlowDropColor(DyeColor.valueOf(color.toLowerCase())));
                    } catch (Exception e){
                        Logger.debug(Message.ERROR__CONVERTER__INVALID_GLOW_DROP_COLOR,"converter", "IAItemsConverter", "item", this.itemId, "color", color, "valid_colors", Arrays.toString(DyeColor.values()));
                    }
                }
            }
        }
    }

    @Override
    public void convertDropShowName(){
        ConfigurationSection dropSection = this.iaItemSection.getConfigurationSection("drop");
        if (isNotNull(dropSection)){
            boolean showName = dropSection.getBoolean("show_name", true);
            if (!showName){
                this.craftEngineItemsConfiguration.addItemConfiguration(new DropDisplayConfiguration(false));
            }
        }
    }

    @Override
    public void convertHideTooltip(){
        // Not supported ?
    }

    @Override
    public void convertToolTipStyle(){
        String toolTipStyle = this.iaItemSection.getString("tooltip_style");
        if (isValidString(toolTipStyle)){
            this.craftEngineItemsConfiguration.addItemConfiguration(new TooltipStyleConfiguration(toolTipStyle));
        }
    }

    @Override
    public void convertFood(){
        ConfigurationSection consumableSection = this.iaItemSection.getConfigurationSection("consumable");
        if (isNotNull(consumableSection)){
            int nutrition = consumableSection.getInt("nutrition", -1);
            float saturation = (float) consumableSection.getDouble("saturation", -1.0);
            if (nutrition >= 0 && saturation >= 0){
                this.craftEngineItemsConfiguration.addItemConfiguration(new FoodConfiguration(nutrition, saturation));
            }
        }
    }

    @Override
    public void convertJukeboxPlayable() {
        String song = this.iaItemSection.getString("jukebox_disc.song", this.iaItemSection.getString("behaviours.music_disc.song.name"));
        if (isValidString(song)){
            this.craftEngineItemsConfiguration.addItemConfiguration(new JukeboxPlayableConfiguration(song));
        }
    }

    @Override
    public void convertEquippable() {
        convertEquipmentSection();
        convertSpecificPropertiesArmorSection();
    }

    private void convertEquipmentSection() {
        ConfigurationSection equipmentSection = this.iaItemSection.getConfigurationSection("equipment");
        if (!isNotNull(equipmentSection)) return;

        String assetId = equipmentSection.getString("id");
        if (!isValidString(assetId)) return;

        assetId = namespaced(assetId, this.namespace);
        EquipmentSlot equipmentSlot = resolveEquipmentSlot(equipmentSection);

        this.craftEngineItemsConfiguration.addItemConfiguration(new EquippableConfiguration(assetId, equipmentSlot));
        applySlotAttributeModifiers(equipmentSection, equipmentSlot);
    }

    private EquipmentSlot resolveEquipmentSlot(ConfigurationSection equipmentSection) {
        EquipmentSlot fromItemId = getEquipmentSlotFromSuffix(this.itemId.toLowerCase(), false);
        if (fromItemId != null) return fromItemId;

        String slot = equipmentSection.getString("slot");
        if (isValidString(slot)) return null;

        return getEquipmentSlotFromSuffix(this.craftEngineItemsConfiguration.getMaterial().name(), true);
    }

    private EquipmentSlot getEquipmentSlotFromSuffix(String name, boolean uppercase) {
        String upString = uppercase ? name.toUpperCase() : name;
        if (upString.endsWith(uppercase ? "_HELMET" : "_helmet") || upString.endsWith("_SKULL") || upString.endsWith("_HAT")) return EquipmentSlot.HEAD;
        if (upString.endsWith(uppercase ? "_CHESTPLATE" : "_chestplate") || upString.endsWith("_ELYTRA")) return EquipmentSlot.CHEST;
        if (upString.endsWith(uppercase ? "_LEGGINGS" : "_leggings")) return EquipmentSlot.LEGS;
        if (upString.endsWith(uppercase ? "_BOOTS" : "_boots")) return EquipmentSlot.FEET;
        return null;
    }

    private void applySlotAttributeModifiers(ConfigurationSection equipmentSection, EquipmentSlot equipmentSlot) {
        if (equipmentSlot == null) return;

        ConfigurationSection slotAttributeModifiers = equipmentSection.getConfigurationSection("slot_attribute_modifiers");
        if (!isNotNull(slotAttributeModifiers)) return;

        AttributeModifier.Slot attributeSlot = toAttributeSlot(equipmentSlot);
        if (attributeSlot == null) return;

        double armor = slotAttributeModifiers.getDouble("armor", 0.0);
        CecAttributeModifier modifier = new CecAttributeModifier("minecraft:armor", attributeSlot, null, armor, AttributeModifier.Operation.ADD_VALUE, null);
        this.craftEngineItemsConfiguration.addItemConfiguration(new AttributeModifiersConfiguration(List.of(modifier)));
    }

    private AttributeModifier.Slot toAttributeSlot(EquipmentSlot equipmentSlot) {
        return switch (equipmentSlot) {
            case HEAD -> AttributeModifier.Slot.HEAD;
            case CHEST -> AttributeModifier.Slot.CHEST;
            case LEGS -> AttributeModifier.Slot.LEGS;
            case FEET -> AttributeModifier.Slot.FEET;
            default -> null;
        };
    }

    private void convertSpecificPropertiesArmorSection() {
        ConfigurationSection specificPropertiesSection = this.iaItemSection.getConfigurationSection("specific_properties");
        if (!isNotNull(specificPropertiesSection)) return;

        ConfigurationSection armorSection = specificPropertiesSection.getConfigurationSection("armor");
        if (!isNotNull(armorSection)) return;

        String assetId = armorSection.getString("custom_armor");
        if (!isValidString(assetId)) return;

        assetId = namespaced(assetId, this.namespace);
        this.isValidString(assetId);

        this.setAssetId(assetId);

        EquipmentSlot equipmentSlot = parseEquipmentSlot(armorSection.getString("slot"));
        this.craftEngineItemsConfiguration.addItemConfiguration(new EquippableConfiguration(assetId, equipmentSlot));
    }

    private EquipmentSlot parseEquipmentSlot(String slot) {
        if (slot == null) return null;
        try {
            return EquipmentSlot.valueOf(slot.toUpperCase());
        } catch (IllegalArgumentException e) {
            Logger.debug("[IAItemsConverter] Invalid equipment slot '" + slot + "' for item " + this.itemId);
            return null;
        }
    }

    @Override
    public void convertItemTexture() {
        ConfigurationSection resourceSection = this.iaItemSection.getConfigurationSection("resource");

        if (isNotNull(resourceSection)) {
            handleResourceSection(resourceSection);
        } else {
            handleGraphicsSection();
        }
    }

    private void handleResourceSection(ConfigurationSection resourceSection) {
        boolean generate = resourceSection.getBoolean("generate", false);

        if (generate) {
            handleGeneratedResource(resourceSection);
        } else {
            handleExistingResource(resourceSection);
        }
    }

    private void handleGeneratedResource(ConfigurationSection resourceSection) {
        IADirectionalMode directionalMode = getDirectionalMode();

        switch (directionalMode) {
            case NONE -> handleNoneDirectionalMode(resourceSection);
            case ALL, LOG -> handleAllOrLogDirectionalMode(resourceSection);
            case FURNACE -> handleFurnaceDirectionalMode(resourceSection);
            default -> Logger.debug("[IAItemsConverter] Directional mode " + directionalMode + " is not supported for item " + this.itemId);
        }
    }

    private IADirectionalMode getDirectionalMode() {
        try {
            String mode = this.iaItemSection.getString("specific_properties.block.placed_model.directional_mode", "NONE");
            return IADirectionalMode.valueOf(mode.toUpperCase());
        } catch (Exception e) {
            return IADirectionalMode.NONE;
        }
    }

    private void handleNoneDirectionalMode(ConfigurationSection resourceSection) {
        String texturePath = getTexturePath(resourceSection);
        if (isValidString(texturePath)) {
            texturePath = namespaced(texturePath, this.namespace);
            ConfigurationSection blockSection = this.iaItemSection.getConfigurationSection("specific_properties.block");
            if (isNotNull(blockSection)){
                this.craftEngineItemUtils.setModel(InternalTemplateManager.parseTemplate(fr.robie.craftengineconverter.common.utils.enums.Template.MODEL_ITEM_DEFAULT, "%model_path%", texturePath));
                handleBlockItem(resourceSection, blockSection);

                return;
            }
            Map<String, Object> parsedTemplate = InternalTemplateManager.parseTemplate(
                    fr.robie.craftengineconverter.common.utils.enums.Template.MODEL_ITEM_GENERATED,
                    "%model_path%", texturePath,
                    "%texture_path%", texturePath
            );
            this.craftEngineItemUtils.getGeneralSection().createSection("model", parsedTemplate);
        }
    }

    private void handleAllOrLogDirectionalMode(ConfigurationSection resourceSection) {
        Map<BlockFace, String> faceTextureMap = buildFaceTextureMap(resourceSection, "ALL");
        if (faceTextureMap == null) return;

        Map<String, Object> parsedTemplate = createCubeModelTemplate(faceTextureMap);
        this.setSavedModelTemplates(parsedTemplate);

        ConfigurationSection behaviorSection = this.craftEngineItemUtils.getBehaviorSection();
        behaviorSection.set("type", "block_item");

        ConfigurationSection stateSection = setupLogBlockState(behaviorSection);
        stateSection.set("appearances", InternalTemplateManager.parseTemplate(
                fr.robie.craftengineconverter.common.utils.enums.Template.BLOCK_STATE_LOG_APPEARANCE,
                "%model%", parsedTemplate,
                "%auto-state-x%", getAutoStateForPlacedModelType(IAPlacedModelTypes.REAL),
                "%auto-state-y%", getAutoStateForPlacedModelType(IAPlacedModelTypes.REAL),
                "%auto-state-z%", getAutoStateForPlacedModelType(IAPlacedModelTypes.REAL)
        ));

        setupLogVariants(stateSection);
    }

    private void handleFurnaceDirectionalMode(ConfigurationSection resourceSection) {
        Map<BlockFace, String> faceTextureMap = buildFaceTextureMap(resourceSection, "Furnace");
        if (faceTextureMap == null) return;

        Map<String, Object> parsedTemplate = createCubeModelTemplate(faceTextureMap);
        this.setSavedModelTemplates(parsedTemplate);

        ConfigurationSection behaviorSection = this.craftEngineItemUtils.getBehaviorSection();
        behaviorSection.set("type", "block_item");
        ConfigurationSection stateSection = getOrCreateSection(getOrCreateSection(behaviorSection, "block"), "states");
        stateSection.set("auto-state", getAutoStateForPlacedModelType(IAPlacedModelTypes.REAL));

        setupFurnaceFacingProperty(stateSection);
        stateSection.set("appearances", InternalTemplateManager.parseTemplate(
                fr.robie.craftengineconverter.common.utils.enums.Template.BLOCK_STATE_4_DIRECTIONS_APPEARANCE,
                "%model%", parsedTemplate,
                "%auto-state-east%", getAutoStateForPlacedModelType(IAPlacedModelTypes.REAL),
                "%auto-state-west%", getAutoStateForPlacedModelType(IAPlacedModelTypes.REAL),
                "%auto-state-north%", getAutoStateForPlacedModelType(IAPlacedModelTypes.REAL),
                "%auto-state-south%", getAutoStateForPlacedModelType(IAPlacedModelTypes.REAL)
        ));

        setupFurnaceVariants(stateSection);
    }

    private Map<BlockFace, String> buildFaceTextureMap(ConfigurationSection resourceSection, String modeName) {
        List<String> faceTextures = resourceSection.getStringList("textures");

        if (faceTextures.size() != 6) {
            Logger.debug("[IAItemsConverter] Directional mode " + modeName + " requires 6 textures for item " + this.itemId);
            return null;
        }


        Map<BlockFace, String> faceTextureMap = new HashMap<>();

        for (String faceTexture : faceTextures) {
            String cleanedTexture = cleanPath(faceTexture);
            if (isNull(cleanedTexture)) continue;

            BlockFace face = determineBlockFace(cleanedTexture);
            if (face != null) {
                faceTextureMap.put(face, namespaced(cleanedTexture, this.namespace));
            } else {
                Logger.debug("[IAItemsConverter] Invalid texture name " + faceTexture + " for directional mode " + modeName + " for item " + this.itemId);
                return null;
            }
        }

        if (faceTextureMap.size() != 6) {
            Logger.debug("[IAItemsConverter] Directional mode " + modeName + " requires 6 valid textures for item " + this.itemId);
            return null;
        }

        return faceTextureMap;
    }

    private BlockFace determineBlockFace(String textureName) {
        if (textureName.endsWith("_down")) return BlockFace.DOWN;
        if (textureName.endsWith("_up")) return BlockFace.UP;
        if (textureName.endsWith("_north")) return BlockFace.NORTH;
        if (textureName.endsWith("_south")) return BlockFace.SOUTH;
        if (textureName.endsWith("_west")) return BlockFace.WEST;
        if (textureName.endsWith("_east")) return BlockFace.EAST;
        return null;
    }

    private Map<String, Object> createCubeModelTemplate(Map<BlockFace, String> faceTextureMap) {
        return InternalTemplateManager.parseTemplate(
                fr.robie.craftengineconverter.common.utils.enums.Template.MODEL_CUBE,
                "%model_path%", faceTextureMap.get(BlockFace.NORTH),
                "%texture_down_path%", faceTextureMap.get(BlockFace.DOWN),
                "%texture_up_path%", faceTextureMap.get(BlockFace.UP),
                "%texture_north_path%", faceTextureMap.get(BlockFace.NORTH),
                "%texture_south_path%", faceTextureMap.get(BlockFace.SOUTH),
                "%texture_west_path%", faceTextureMap.get(BlockFace.WEST),
                "%texture_east_path%", faceTextureMap.get(BlockFace.EAST)
        );
    }

    private ConfigurationSection setupLogBlockState(ConfigurationSection behaviorSection) {
        ConfigurationSection stateSection = getOrCreateSection(
                getOrCreateSection(behaviorSection, "block"),
                "states"
        );
        stateSection.set("auto-state", getAutoStateForPlacedModelType(IAPlacedModelTypes.REAL));

        ConfigurationSection properties = getOrCreateSection(stateSection, "properties");
        ConfigurationSection axis = getOrCreateSection(properties, "axis");
        axis.set("type", "axis");
        axis.set("default", "y");

        return stateSection;
    }

    private void setupLogVariants(ConfigurationSection stateSection) {
        ConfigurationSection variants = getOrCreateSection(stateSection, "variants");

        ConfigurationSection axisX = getOrCreateSection(variants, "axis=x");
        axisX.set("appearance", "axisX");

        ConfigurationSection axisY = getOrCreateSection(variants, "axis=y");
        axisY.set("appearance", "axisY");

        ConfigurationSection axisZ = getOrCreateSection(variants, "axis=z");
        axisZ.set("appearance", "axisZ");
    }

    private void setupFurnaceFacingProperty(ConfigurationSection stateSection) {
        ConfigurationSection properties = getOrCreateSection(stateSection, "properties");
        ConfigurationSection facing = getOrCreateSection(properties, "facing");
        facing.set("type", "4-direction");
        facing.set("default", "north");
    }

    private void setupFurnaceVariants(ConfigurationSection stateSection) {
        ConfigurationSection variants = getOrCreateSection(stateSection, "variants");

        String[] directions = {"north", "south", "west", "east"};
        for (String direction : directions) {
            ConfigurationSection variant = getOrCreateSection(variants, "facing=" + direction);
            variant.set("appearance", direction);
        }
    }

    private void handleExistingResource(ConfigurationSection resourceSection) {
        ConfigurationSection blockSection = this.iaItemSection.getConfigurationSection("specific_properties.block");

        if (isNotNull(blockSection)) {
            handleBlockItem(resourceSection, blockSection);
        }
        String modelPath = resourceSection.getString("model_path");
        if (!isValidString(modelPath)) {
            return;
        }
        modelPath = namespaced(modelPath, this.namespace);
        if (isNull(modelPath)){
            Logger.debug("[IAItemsConverter] Missing model path for item " + this.itemId + ". Cannot convert item texture.");
            return;
        }
        Material itemMaterial = this.craftEngineItemsConfiguration.getMaterial();
        if (itemMaterial == Material.FISHING_ROD){
            handleFishingRod3D(modelPath,modelPath+"_cast");
            return;
        }
        if (itemMaterial == Material.BOW){
            handleBow3D(modelPath,modelPath+"_0",modelPath+"_1",modelPath+"_2");
            return;
        }
        if (itemMaterial == Material.SHIELD){
            handleShield3D(modelPath,modelPath+"_blocking");
            return;
        }
        handleSimpleModelPath(modelPath);
    }

    private void handleBlockItem(ConfigurationSection resourceSection, ConfigurationSection blockSection) {
        IAPlacedModelTypes placedModelType = getPlacedModelType(blockSection);

        ConfigurationSection behaviorSection = this.craftEngineItemUtils.getBehaviorSection();
        behaviorSection.set("type", "block_item");
        ConfigurationSection blockBehaviorSection = getOrCreateSection(behaviorSection, "block");

        configureBlockProperties(blockSection, blockBehaviorSection);
        configureBlockSounds(blockSection, blockBehaviorSection);
        configureLiquidPlacement(blockSection, blockBehaviorSection);

        String modelPath = resourceSection.getString("model_path");
        if (!isValidString(modelPath)) {
            boolean isGenerated = resourceSection.getBoolean("generate", false);
            if (isGenerated) {
                String texturePath = getTexturePath(resourceSection);
                if (!isValidString(texturePath)) {
                    Logger.debug("[IAItemsConverter] Missing texture path for generated block item " + this.itemId + ". Cannot convert item texture.");
                    return;
                }
                texturePath = namespaced(texturePath, this.namespace);
                setupBlockStateFromTexture(blockBehaviorSection, placedModelType, texturePath);
                return;
            } else {
                Logger.debug("[IAItemsConverter] Missing model path for block item " + this.itemId + ". Cannot convert item texture.");
                return;
            }
        }

        modelPath = namespaced(modelPath, this.namespace);
        setupBlockState(blockBehaviorSection, placedModelType, modelPath);


    }

    private IAPlacedModelTypes getPlacedModelType(ConfigurationSection blockSection) {
        try {
            String type = blockSection.getString("placed_model.type", "REAL");
            IAPlacedModelTypes modelType = IAPlacedModelTypes.valueOf(type.toUpperCase());

            if (modelType == IAPlacedModelTypes.FIRE) {
                Logger.info("[IAItemsConverter] Placed model type FIRE is not supported by CraftEngine so it will be converted as REAL for item " + this.itemId);
                return IAPlacedModelTypes.REAL;
            }

            return modelType;
        } catch (Exception e) {
            return IAPlacedModelTypes.REAL;
        }
    }

    private void configureBlockProperties(ConfigurationSection blockSection, ConfigurationSection blockBehaviorSection) {
        int lightLevel = blockSection.getInt("light_level", 0);
        if (lightLevel > 0) {
            ConfigurationSection settings = getOrCreateSection(blockBehaviorSection, "settings");
            settings.set("luminance", lightLevel);
        }

        double hardness = blockSection.getDouble("hardness", 2d);
        if (hardness != 2d) {
            ConfigurationSection settings = getOrCreateSection(blockBehaviorSection, "settings");
            settings.set("hardness", hardness);
        }

        double blastResistance = blockSection.getDouble("blast_resistance", 2d);
        if (blastResistance != 2d) {
            ConfigurationSection settings = getOrCreateSection(blockBehaviorSection, "settings");
            settings.set("resistance", blastResistance);
        }

        // TODO: implement break tools blacklist/whitelist conversion
        List<String> breakToolsBlackList = blockSection.getStringList("break_tools_blacklist");
        List<String> breakToolsWhiteList = blockSection.getStringList("break_tools_whitelist");
    }

    private void configureBlockSounds(ConfigurationSection blockSection, ConfigurationSection blockBehaviorSection) {
        ConfigurationSection soundSection = blockSection.getConfigurationSection("sounds");
        if (isNotNull(soundSection)) {
            List<String> soundEvents = List.of("fall", "hit", "break", "step", "place");
            for (String eventKey : soundEvents) {
                processBlockSound(soundSection, eventKey, blockBehaviorSection);
            }
        }
    }

    private void configureLiquidPlacement(ConfigurationSection blockSection, ConfigurationSection blockBehaviorSection) {
        boolean placeableOnWater = blockSection.getBoolean("placeable_on_water", false);
        boolean placeableOnLava = blockSection.getBoolean("placeable_on_lava", false);

        if (placeableOnWater || placeableOnLava) {
            List<Map<?, ?>> behaviors = blockBehaviorSection.getMapList("behaviors");
            Map<String, Object> liquidPlacementBehavior = new HashMap<>();
            liquidPlacementBehavior.put("type", "on_liquid_block");

            List<String> liquids = new ArrayList<>();
            if (placeableOnWater) liquids.add("water");
            if (placeableOnLava) liquids.add("lava");

            liquidPlacementBehavior.put("liquid-type", liquids);
            behaviors.add(liquidPlacementBehavior);
            blockBehaviorSection.set("behaviors", behaviors);
        }
    }

    private void setupBlockState(ConfigurationSection blockBehaviorSection, IAPlacedModelTypes placedModelType, String modelPath) {
        ConfigurationSection stateSection = getOrCreateSection(blockBehaviorSection, "state");

        String autoState = getAutoStateForPlacedModelType(placedModelType);
        if (isNull(autoState)){
            Logger.info("Limit reached for placed model type " + placedModelType + " for item " + this.itemId + ". Defaulting to SOLID auto-state.");
            autoState = "solid";
        }

        stateSection.set("auto-state", autoState);
        stateSection.set("model", InternalTemplateManager.parseTemplate(
                fr.robie.craftengineconverter.common.utils.enums.Template.BLOCK_MODEL,
                "%model_path%", modelPath
        ));
    }

    private void setupBlockStateFromTexture(ConfigurationSection blockBehaviorSection, IAPlacedModelTypes placedModelType, String texturePath){
        ConfigurationSection stateSection = getOrCreateSection(blockBehaviorSection, "state");
        String autoState = getAutoStateForPlacedModelType(placedModelType);
        if (isNull(autoState)){
            Logger.info("Limit reached for placed model type " + placedModelType + " for item " + this.itemId + ". Defaulting to SOLID auto-state.");
            autoState = "solid";
        }
        stateSection.set("auto-state", autoState);
        stateSection.set("model", InternalTemplateManager.parseTemplate(
                fr.robie.craftengineconverter.common.utils.enums.Template.MODEL_CUBE_ALL,
                "%model_path%", texturePath,
                "%texture_path%", texturePath
        ));
    }

    @Nullable
    private String getAutoStateForPlacedModelType(IAPlacedModelTypes placedModelType) {
        Plugins plugin = this.getConverter().getPluginType();
        CraftEngineBlockState blockState = switch (placedModelType) {
            case REAL_NOTE -> CraftEngineBlockState.NOTE_BLOCK.getAvailableAndIncrement(plugin);
            case REAL_TRANSPARENT -> CraftEngineBlockState.CHORUS.getAvailableAndIncrement(plugin);
            case REAL_WIRE -> CraftEngineBlockState.TRIPWIRE.getAvailableAndIncrement(plugin);
            case REAL -> CraftEngineBlockState.MUSHROOM.getAvailableAndIncrement(plugin);
            default -> CraftEngineBlockState.SOLID.getAvailableAndIncrement(plugin);
        };
        if (isNull(blockState)) {
            return null;
        }

        return blockState.name().toLowerCase();
    }

    private void handleSimpleModelPath(@NotNull String namespacedModelPath) {
        Map<String, Object> parsedTemplate = InternalTemplateManager.parseTemplate(
                fr.robie.craftengineconverter.common.utils.enums.Template.MODEL_ITEM_DEFAULT,
                "%model_path%", namespacedModelPath
        );
        this.setSavedModelTemplates(parsedTemplate);
        this.craftEngineItemUtils.getGeneralSection().createSection("model", parsedTemplate);

    }

    private void handleGraphicsSection() {
        ConfigurationSection graphicsSection = this.iaItemSection.getConfigurationSection("graphics");
        if (isNull(graphicsSection)) return;

        if (handleGraphicsModel(graphicsSection)) return;

        boolean isBlock = this.iaItemSection.contains("behaviours.block.placed_model.type");
        String texturePath = graphicsSection.getString("texture");

        if (isValidString(texturePath) && !isBlock) {
            handleSimpleTexture(texturePath);
        } else if (isBlock) {
            handleBlockGraphics(graphicsSection, texturePath);
        } else {
            handleComplexModels(graphicsSection);
        }
    }

    private boolean handleGraphicsModel(ConfigurationSection graphicsSection) {
        String modelPath = graphicsSection.getString("model");
        if (isValidString(modelPath)) {
            modelPath = namespaced(modelPath, this.namespace);
            Map<String, Object> parsedTemplate = InternalTemplateManager.parseTemplate(
                    fr.robie.craftengineconverter.common.utils.enums.Template.MODEL_ITEM_DEFAULT,
                    "%model_path%", modelPath
            );
            this.craftEngineItemUtils.getGeneralSection().createSection("model", parsedTemplate);
            return true;
        }
        return false;
    }

    private void handleSimpleTexture(String texturePath) {
        texturePath = namespaced(texturePath, this.namespace);
        Map<String, Object> parsedTemplate = InternalTemplateManager.parseTemplate(
                fr.robie.craftengineconverter.common.utils.enums.Template.MODEL_ITEM_GENERATED,
                "%model_path%", texturePath,
                "%texture_path%", texturePath
        );
        this.craftEngineItemUtils.getGeneralSection().createSection("model", parsedTemplate);
    }

    private void handleBlockGraphics(ConfigurationSection graphicsSection, String texturePath) {
        BlockParent parent = getBlockParent(graphicsSection);

        if (isNotNull(parent)) {
            handleBlockIcon(graphicsSection);

            if (parent == BlockParent.CROSS) {
                handleCrossBlock(graphicsSection);
            } else {
                Logger.debug("[IAItemsConverter] Block parent " + parent + " is not supported for item " + this.itemId + ". Please open an issue to request support.");
            }
        } else if (isValidString(texturePath)) {
            texturePath = namespaced(texturePath, this.namespace);
            this.craftEngineItemUtils.setModel(InternalTemplateManager.parseTemplate(
                    fr.robie.craftengineconverter.common.utils.enums.Template.MODEL_CUBE_ALL,
                    "%model_path%", texturePath,
                    "%texture_path%", texturePath
            ));
        }
    }

    private BlockParent getBlockParent(ConfigurationSection graphicsSection) {
        try {
            String parentStr = graphicsSection.getString("parent", "");
            return BlockParent.valueOf(parentStr.toUpperCase());
        } catch (Exception e) {
            return null;
        }
    }

    private void handleBlockIcon(ConfigurationSection graphicsSection) {
        String iconPath = graphicsSection.getString("icon");
        if (isValidString(iconPath)) {
            iconPath = namespaced(iconPath, this.namespace);
            this.craftEngineItemUtils.getGeneralSection().createSection("model",
                    InternalTemplateManager.parseTemplate(
                            fr.robie.craftengineconverter.common.utils.enums.Template.MODEL_ITEM_GENERATED,
                            "%model_path%", iconPath,
                            "%texture_path%", iconPath
                    )
            );
        }
    }

    private void handleCrossBlock(ConfigurationSection graphicsSection) {
        String crossTexture = graphicsSection.getString("textures.cross", graphicsSection.getString("texture"));
        if (!isValidString(crossTexture)) return;

        crossTexture = namespaced(crossTexture, this.namespace);

        ConfigurationSection behaviorSection = this.craftEngineItemUtils.getBehaviorSection();
        behaviorSection.set("type", "block_item");
        ConfigurationSection blockSection = getOrCreateSection(behaviorSection, "block");

        ConfigurationSection stateSection = getOrCreateSection(blockSection, "state");
        stateSection.set("properties", InternalTemplateManager.parseTemplate(fr.robie.craftengineconverter.common.utils.enums.Template.BLOCK_STATE_PROPERTIES_STAGE));
        stateSection.set("appearances", InternalTemplateManager.parseTemplate(
                fr.robie.craftengineconverter.common.utils.enums.Template.BLOCK_STATE_SAPLING_APPEARANCE,
                "%model%", InternalTemplateManager.parseTemplate(
                        fr.robie.craftengineconverter.common.utils.enums.Template.MODEL_CROSS,
                        "%model_path%", crossTexture,
                        "%texture_path%", crossTexture
                )
        ));
    }

    private void handleComplexModels(ConfigurationSection graphicsSection) {
        ConfigurationSection texturesSection = graphicsSection.getConfigurationSection("textures");
        if (isNotNull(texturesSection)) {
            handle2DModels(texturesSection);
            return;
        }

        ConfigurationSection modelsSection = graphicsSection.getConfigurationSection("models");
        if (isNotNull(modelsSection)) {
            handle3DModels(modelsSection);
        }
    }

    private void handle2DModels(ConfigurationSection texturesSection) {
        Set<String> keys = texturesSection.getKeys(false);

        if (IAModelsKeys.BOW.containsAny(keys) && keys.size() == IAModelsKeys.BOW.getKeysCount()) {
            handleBow2D(texturesSection);
        } else if (IAModelsKeys.FISHING_ROD.containsAny(keys) && keys.size() == IAModelsKeys.FISHING_ROD.getKeysCount()) {
            handleFishingRod2D(texturesSection);
        } else if (IAModelsKeys.CROSSBOW.containsAny(keys) && keys.size() == IAModelsKeys.CROSSBOW.getKeysCount()) {
            handleCrossbow2D(texturesSection);
        }
    }

    private void handle3DModels(ConfigurationSection modelsSection) {
        Set<String> keys = modelsSection.getKeys(false);

        if (IAModelsKeys.BOW.containsAny(keys) && keys.size() == IAModelsKeys.BOW.getKeysCount()) {
            handleBow3D(namespaced(modelsSection.getString("normal"), this.namespace),
                    namespaced(modelsSection.getString("pulling_0"), this.namespace),
                    namespaced(modelsSection.getString("pulling_1"), this.namespace),
                    namespaced(modelsSection.getString("pulling_2"), this.namespace)
                );
        } else if (IAModelsKeys.FISHING_ROD.containsAny(keys) && keys.size() == IAModelsKeys.FISHING_ROD.getKeysCount()) {
            handleFishingRod3D(namespaced(modelsSection.getString("normal"),this.namespace),namespaced(modelsSection.getString("cast"),this.namespace));
        } else if (IAModelsKeys.CROSSBOW.containsAny(keys) && keys.size() == IAModelsKeys.CROSSBOW.getKeysCount()) {
            handleCrossbow3D(modelsSection);
        } else if (IAModelsKeys.TRIDENT.containsAny(keys) && keys.size() == IAModelsKeys.TRIDENT.getKeysCount()) {
            handleTrident3D(modelsSection);
        } else if (IAModelsKeys.SHIELD.containsAny(keys) && keys.size() == IAModelsKeys.SHIELD.getKeysCount()) {
            handleShield3D(namespaced(modelsSection.getString("normal"), this.namespace),
                    namespaced(modelsSection.getString("blocking"), this.namespace)
            );
        }
    }

    private void handleBow2D(ConfigurationSection texturesSection) {
        this.craftEngineItemUtils.setModel(InternalTemplateManager.parseTemplate(
                fr.robie.craftengineconverter.common.utils.enums.Template.MODEL_2D_BOW_SIMPLIFIED,
                "%default_texture_path%", namespaced(texturesSection.getString("normal"), this.namespace),
                "%pulling_0_texture_path%", namespaced(texturesSection.getString("pulling_0"), this.namespace),
                "%pulling_1_texture_path%", namespaced(texturesSection.getString("pulling_1"), this.namespace),
                "%pulling_2_texture_path%", namespaced(texturesSection.getString("pulling_2"), this.namespace)
        ));
    }

    private void handleFishingRod2D(ConfigurationSection texturesSection) {
        this.craftEngineItemUtils.setModel(InternalTemplateManager.parseTemplate(
                fr.robie.craftengineconverter.common.utils.enums.Template.MODEL_2D_FISHING_ROD_SIMPLIFIED,
                "%default_texture_path%", namespaced(texturesSection.getString("normal"), this.namespace),
                "%cast_texture_path%", namespaced(texturesSection.getString("cast"), this.namespace)
        ));
    }

    private void handleCrossbow2D(ConfigurationSection texturesSection) {
        this.craftEngineItemUtils.setModel(InternalTemplateManager.parseTemplate(
                fr.robie.craftengineconverter.common.utils.enums.Template.MODEL_2D_CROSSBOW_SIMPLIFIED,
                "%default_texture_path%", namespaced(texturesSection.getString("normal"), this.namespace),
                "%pulling_0_texture_path%", namespaced(texturesSection.getString("pulling_0"), this.namespace),
                "%pulling_1_texture_path%", namespaced(texturesSection.getString("pulling_1"), this.namespace),
                "%pulling_2_texture_path%", namespaced(texturesSection.getString("pulling_2"), this.namespace),
                "%charged_rocket_texture_path%", namespaced(texturesSection.getString("rocket"), this.namespace),
                "%charged_arrow_texture_path%", namespaced(texturesSection.getString("arrow"), this.namespace)
        ));
    }

    private void handleBow3D(String defaultModelPath, String pulling0ModelPath, String pulling1ModelPath, String pulling2ModelPath) {
        this.craftEngineItemUtils.setModel(InternalTemplateManager.parseTemplate(
                fr.robie.craftengineconverter.common.utils.enums.Template.MODEL_3D_BOW,
                "%default_model_path%", defaultModelPath,
                "%pulling_0_model_path%", pulling0ModelPath,
                "%pulling_1_model_path%", pulling1ModelPath,
                "%pulling_2_model_path%", pulling2ModelPath
        ));
    }

    private void handleFishingRod3D(String defaultModelPath, String castingModelPath) {
        this.craftEngineItemUtils.setModel(InternalTemplateManager.parseTemplate(
                fr.robie.craftengineconverter.common.utils.enums.Template.MODEL_3D_FISHING_ROD,
                "%default_model_path%", defaultModelPath,
                "%casting_model_path%", castingModelPath
        ));
    }

    private void handleCrossbow3D(ConfigurationSection modelsSection) {
        this.craftEngineItemUtils.setModel(InternalTemplateManager.parseTemplate(
                fr.robie.craftengineconverter.common.utils.enums.Template.MODEL_3D_CROSSBOW,
                "%default_model_path%", namespaced(modelsSection.getString("normal"), this.namespace),
                "%pulling_0_model_path%", namespaced(modelsSection.getString("pulling_0"), this.namespace),
                "%pulling_1_model_path%", namespaced(modelsSection.getString("pulling_1"), this.namespace),
                "%pulling_2_model_path%", namespaced(modelsSection.getString("pulling_2"), this.namespace),
                "%charged_rocket_model_path%", namespaced(modelsSection.getString("rocket"), this.namespace),
                "%charged_arrow_model_path%", namespaced(modelsSection.getString("arrow"), this.namespace)
        ));
    }

    private void handleTrident3D(ConfigurationSection modelsSection) {
        this.craftEngineItemUtils.setModel(InternalTemplateManager.parseTemplate(
                fr.robie.craftengineconverter.common.utils.enums.Template.MODEL_TRIDENT,
                "%model_path%", namespaced(modelsSection.getString("normal"), this.namespace),
                "%throwing_model_path%", namespaced(modelsSection.getString("throwing"), this.namespace)
        ));
    }

    private void handleShield3D(String defaultModelPath, String blockingModelPath) {
        this.craftEngineItemUtils.setModel(InternalTemplateManager.parseTemplate(
                fr.robie.craftengineconverter.common.utils.enums.Template.MODEL_3D_SHIELD,
                "%default_model_path%", defaultModelPath,
                "%blocking_model_path%", blockingModelPath
        ));
    }

    @Override
    public void convertOther(){
        ConfigurationSection behavioursSection = this.iaItemSection.getConfigurationSection("behaviours");
        if (isNotNull(behavioursSection)){
            for (String behaviourKey : behavioursSection.getKeys(false)){
                switch (behaviourKey){
                    case "furniture" -> {
                        ConfigurationSection furnitureSection = behavioursSection.getConfigurationSection("furniture");
                        if (isNotNull(furnitureSection)){
                            this.convertFurniture(furnitureSection, behavioursSection);
                        }
                    }
                    case "fuel"->{
                        ConfigurationSection fuelSection = behavioursSection.getConfigurationSection("fuel");
                        if (isNotNull(fuelSection)){
                            int burnTicks = fuelSection.getInt("burn_ticks", -1);
                            if (burnTicks > 0){
                                this.craftEngineItemsConfiguration.addItemConfiguration(new FuelTimeSettingConfiguration(burnTicks));
                            }
                            // machines fuel type not supported
                        }
                    }
                    default -> {

                    }
                }
            }
        }
    }

    private void convertFurniture(ConfigurationSection furnitureSection, ConfigurationSection behavioursSection) {
        IAEntityTypes entityType = IAEntityTypes.ITEM_FRAME;
        try {
            entityType = IAEntityTypes.valueOf(furnitureSection.getString("entity", "ITEM_FRAME").toUpperCase());
        } catch (Exception ignored) {}

        boolean isBig = furnitureSection.getBoolean("small", true);

        Set<FurniturePlacement> placements = new HashSet<>();
        ConfigurationSection placeableSection = furnitureSection.getConfigurationSection("placeable_on");
        if (isNotNull(placeableSection)) {
            if (placeableSection.getBoolean("floor", true)) placements.add(FurniturePlacement.GROUND);
            if (placeableSection.getBoolean("ceiling", true)) placements.add(FurniturePlacement.CEILING);
            if (placeableSection.getBoolean("wall", true)) placements.add(FurniturePlacement.WALL);
        } else {
            placements.addAll(List.of(FurniturePlacement.values()));
        }

        if (placements.isEmpty()) return;

        FurnitureConfiguration furnitureConfiguration = new FurnitureConfiguration();

        // --- Display properties ---
        Billboard transformType = Billboard.FIXED;
        ItemDisplayType displayType = ItemDisplayType.NONE;
        FloatsUtils displayTranslation = new FloatsUtils(3, new float[]{0f, 0.5f, 0f});
        if (isBig) displayTranslation.addValue(1, 1f);
        FloatsUtils scale = new FloatsUtils(3, new float[]{1f, 1f, 1f});

        ConfigurationSection displayTransformationSection = furnitureSection.getConfigurationSection("display_transformation");
        if (isNotNull(displayTransformationSection)) {
            try {
                displayType = ItemDisplayType.valueOf(displayTransformationSection.getString("transform", "FIXED").toUpperCase());
            } catch (Exception e) {
                Logger.debug(Message.WARNING__CONVERTER__IA__FURNITURE__UNKNOWN_DISPLAY_TRANSFORM, "item", this.itemId, "transform", displayTransformationSection.getString("transform"));
            }
            ConfigurationSection translationSection = displayTransformationSection.getConfigurationSection("translation");
            if (isNotNull(translationSection)) {
                double x = translationSection.getDouble("x");
                double y = translationSection.getDouble("y");
                double z = translationSection.getDouble("z");
                if (x != 0d) displayTranslation.setValue(0, (float) x);
                if (y != 0d) displayTranslation.setValue(1, (float) y);
                if (z != 0d) displayTranslation.setValue(2, (float) z);
            }
            ConfigurationSection scaleSection = displayTransformationSection.getConfigurationSection("scale");
            if (isNotNull(scaleSection)) {
                double x = scaleSection.getDouble("x", 1.0);
                double y = scaleSection.getDouble("y", 1.0);
                double z = scaleSection.getDouble("z", 1.0);
                if (x != 1.0) scale.setValue(0, (float) x);
                if (y != 1.0) scale.setValue(1, (float) y);
                if (z != 1.0) scale.setValue(2, (float) z);
            }
        }

        // --- Element ---
        FurnitureConfiguration.ItemElement element;
        if (entityType == IAEntityTypes.ARMOR_STAND) {
            FurnitureConfiguration.ArmorStandElement armorStand = new FurnitureConfiguration.ArmorStandElement(this.itemId);
            if (scale.isUpdated())
                armorStand.setScale(scale.getValue(0), scale.getValue(1), scale.getValue(2));
            if (!isBig) armorStand.setSmall(true);
            element = armorStand;
        } else {
            FurnitureConfiguration.ItemDisplayElement itemDisplay = new FurnitureConfiguration.ItemDisplayElement(this.itemId);
            int light = furnitureSection.getInt("light_level", -1);
            if (light >= 0) itemDisplay.display().setBrightness(light, -1);
            if (displayType != ItemDisplayType.NONE) itemDisplay.setDisplayTransform(displayType);
            itemDisplay.display().setBillboard(transformType);
            if (displayTranslation.isUpdated())
                itemDisplay.display().setTranslation(displayTranslation.getValue(0), displayTranslation.getValue(1), displayTranslation.getValue(2));
            if (scale.isUpdated())
                itemDisplay.display().setScale(scale.getValue(0), scale.getValue(1), scale.getValue(2));
            element = itemDisplay;
        }

        // --- Hitboxes ---
        double sitHeight = behavioursSection.getDouble("furniture_sit.sit_height", 0d);
        List<FurnitureConfiguration.Hitbox> hitboxList = new ArrayList<>();
        ConfigurationSection iaHitboxesSection = furnitureSection.getConfigurationSection("hitbox");
        if (isNotNull(iaHitboxesSection)) {
            parseItemsAdderHitboxes(iaHitboxesSection, hitboxList, sitHeight);
        }

        // --- Loot ---
        furnitureConfiguration.setLoot(InternalTemplateManager.parseTemplate(Template.LOOT_TABLE_BASIC_DROP, "%type%", "furniture_item", "%item%", this.itemId));

        // --- Placements ---
        for (FurniturePlacement furniturePlacement : placements) {
            FurnitureConfiguration.Placement placement = furnitureConfiguration.getOrCreatePlacement(furniturePlacement);
            placement.addElement(element);
            hitboxList.forEach(placement::addHitbox);
        }

        this.getCraftEngineItemsConfiguration().addItemConfiguration(furnitureConfiguration);
    }

    private void parseItemsAdderHitboxes(ConfigurationSection iaHitboxesSection, List<FurnitureConfiguration.Hitbox> hitboxes, double seatPosition) {
        if (iaHitboxesSection == null) return;

        int length = iaHitboxesSection.getInt("length", 1);
        int width = iaHitboxesSection.getInt("width", 1);
        int height = iaHitboxesSection.getInt("height", 1);
        int lengthOffset = iaHitboxesSection.getInt("length_offset", 0);
        int widthOffset = iaHitboxesSection.getInt("width_offset", 0);
        int heightOffset = iaHitboxesSection.getInt("height_offset", 0);

        for (int x = 0; x < length; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < width; z++) {
                    FurnitureConfiguration.ShulkerHitbox hitbox = new FurnitureConfiguration.ShulkerHitbox();
                    hitbox.setPosition(x + lengthOffset, y + heightOffset, z + widthOffset);
                    if (x == 0 && y == 0 && z == 0)
                        hitbox.addSeat(0, (float) seatPosition, 0, 0);
                    hitboxes.add(hitbox);
                }
            }
        }
    }

    /**
     * Process a single block sound event from the source section and write it into the converted
     * block behaviour section under settings.sounds.<eventKey>.
     * If volume/pitch differ from defaults (1.0) a map with id/volume/pitch is written,
     * otherwise the raw sound name is stored.
     */
    private void processBlockSound(ConfigurationSection soundSection, String eventKey, ConfigurationSection blockBehaviorSection) {
        if (soundSection == null || eventKey == null) return;

        ConfigurationSection eventSection = soundSection.getConfigurationSection(eventKey);
        if (isNull(eventSection)) return;

        String soundName = eventSection.getString("name");
        double volume = eventSection.getDouble("volume", 1.0);
        double pitch = eventSection.getDouble("pitch", 1.0);

        if (!isValidString(soundName)) return;

        ConfigurationSection soundSettingsSection = getOrCreateSection(getOrCreateSection(blockBehaviorSection, "settings"), "sounds");

        if (volume != 1d || pitch != 1d) {
            Map<String, Object> soundMap = new HashMap<>();
            soundMap.put("id", soundName.toLowerCase().replace("_","."));
            soundMap.put("volume", volume);
            soundMap.put("pitch", pitch);
            soundSettingsSection.set(eventKey, soundMap);
        } else {
            soundSettingsSection.set(eventKey, soundName);
        }
    }
}
