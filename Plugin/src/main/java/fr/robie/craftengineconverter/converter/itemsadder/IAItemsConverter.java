package fr.robie.craftengineconverter.converter.itemsadder;

import fr.robie.craftengineconverter.common.configuration.Configuration;
import fr.robie.craftengineconverter.common.logger.Logger;
import fr.robie.craftengineconverter.converter.Converter;
import fr.robie.craftengineconverter.converter.ItemConverter;
import fr.robie.craftengineconverter.utils.FloatsUtils;
import fr.robie.craftengineconverter.utils.enums.*;
import fr.robie.craftengineconverter.utils.enums.ia.IADirectionalMode;
import fr.robie.craftengineconverter.utils.enums.ia.IAModelsKeys;
import fr.robie.craftengineconverter.utils.manager.InternalTemplateManager;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

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
        Material material = Configuration.defaultMaterial;
        if (isNotNull(resourceSection)){
            try {
                material = Material.valueOf(resourceSection.getString("material").toUpperCase());
            } catch (Exception ignored) {
            }
        }
        this.craftEngineItemUtils.setMaterial(material);
    }

    @Override
    public void convertItemName(){
        String itemName = this.iaItemSection.getString("name", this.iaItemSection.getString("display_name"));
        if (isValidString(itemName)){
            this.craftEngineItemUtils.setItemName(itemName);
        }
    }

    @Override
    public void convertLore(){
        List<String> lore = this.iaItemSection.getStringList("lore");
        if (!lore.isEmpty()){
            this.craftEngineItemUtils.setLore(lore);
        }
    }

    @Override
    public void convertDyedColor(){
        Object color = this.iaItemSection.get("graphics.color");
        if (isNotNull(color)){
            this.craftEngineItemUtils.getDataSection().set("dyed-color", color);
        }
    }

    @Override
    public void convertUnbreakable(){
        ConfigurationSection durabilitySection = this.iaItemSection.getConfigurationSection("durability");
        if (isNotNull(durabilitySection)){
            boolean unbreakable = durabilitySection.getBoolean("unbreakable", false);
            if (unbreakable){
                this.craftEngineItemUtils.getDataSection().set("unbreakable", true);
            }
        }
    }

    @Override
    public void convertItemFlags(){
        List<String> itemFlags = this.iaItemSection.getStringList("item_flags");
        if (!itemFlags.isEmpty()){
            this.craftEngineItemUtils.getDataSection().set("hide-tooltip", itemFlags);
        }
    }

    @Override
    public void convertAttributeModifiers(){
        ConfigurationSection attributesSection = this.iaItemSection.getConfigurationSection("attribute_modifiers");
        if (isNotNull(attributesSection)) {
            List<Map<String, Object>> ceAttributes = new ArrayList<>();
            //TODO: implement attribute modifiers conversion from ItemsAdder to CraftEngine
        }
    }

    @Override
    public void convertEnchantments(){
        ConfigurationSection enchantsSection = this.iaItemSection.getConfigurationSection("enchants");
        if (isNotNull(enchantsSection)){
            for (String enchantmentKey : enchantsSection.getKeys(false)){
                int enchantLevel = enchantsSection.getInt(enchantmentKey, 1);
                ConfigurationSection ceEnchantSection = getOrCreateSection(this.craftEngineItemUtils.getDataSection(), "enchantment");
                ceEnchantSection.set(enchantmentKey, enchantLevel);
            }
        }
        List<String> enchantments = this.iaItemSection.getStringList("enchants");
        if (!enchantments.isEmpty()){
            ConfigurationSection ceEnchantSection = getOrCreateSection(this.craftEngineItemUtils.getDataSection(), "enchantment");
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
                ceEnchantSection.set(enchantName, enchantLevel);
            }
        }
    }

    @Override
    public void convertCustomModelData(){
        ConfigurationSection resourceSection = this.iaItemSection.getConfigurationSection("resource");
        if (isNotNull(resourceSection)){
            int customModelData = resourceSection.getInt("custom_model_data", resourceSection.getInt("model_id", 0));
            if (customModelData != 0){
                this.craftEngineItemUtils.getGeneralSection().set("custom-model-data", customModelData);
            }
        }
    }

    @Override
    public void convertItemModel(){
        String itemModel = this.iaItemSection.getString("item_model");
        if (isValidString(itemModel)){
            this.craftEngineItemUtils.getComponentsSection().set("item_model", itemModel);
        }
    }

    @Override
    public void convertMaxStackSize(){
        int maxStackSize = this.iaItemSection.getInt("max_stack_size", -1);
        if (maxStackSize > 0){
            this.craftEngineItemUtils.getComponentsSection().set("minecraft:max_stack_size", maxStackSize);
        }
    }

    @Override
    public void convertEnchantmentGlintOverride(){
        if (this.iaItemSection.getBoolean("glint", false)){
            this.craftEngineItemUtils.enableEnchantmentGlint();
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
                this.craftEngineItemUtils.getDataSection().set("max-damage", maxDamage);
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
                    if (isValidString(color)){
                        this.craftEngineItemUtils.getSettingsSection().set("glow-color", color.toLowerCase());
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
                this.craftEngineItemUtils.getSettingsSection().set("drop-display", true);
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
            this.craftEngineItemUtils.getDataSection().set("tooltip-style", toolTipStyle);
        }
    }

    @Override
    public void convertFood(){
        ConfigurationSection consumableSection = this.iaItemSection.getConfigurationSection("consumable");
        if (isNotNull(consumableSection)){
            int nutrition = consumableSection.getInt("nutrition", -1);
            float saturation = (float) consumableSection.getDouble("saturation", -1.0);
            if (nutrition > 0 || saturation > 0){
                ConfigurationSection foodSection = getOrCreateSection(this.craftEngineItemUtils.getComponentsSection(), "minecraft:food");
                if (nutrition > 0){
                    foodSection.set("nutrition", nutrition);
                }
                if (saturation > 0) {
                    foodSection.set("saturation", saturation);
                }
            }
        }
    }

    @Override
    public void convertJukeboxPlayable() {
        String song = this.iaItemSection.getString("jukebox_disc.song", this.iaItemSection.getString("behaviours.music_disc.song.name"));
        this.craftEngineItemUtils.setJukeboxPlayable(song);
    }

    @Override
    public void convertEquipable() {
        ConfigurationSection equipmentSection = this.iaItemSection.getConfigurationSection("equipment");
        if (isNotNull(equipmentSection)) {
            String assetId = equipmentSection.getString("id");
            if (!isValidString(assetId)) return;
            assetId = namespaced(assetId,this.namespace);
            ConfigurationSection ceEquipableSection = this.isValidString(assetId) ? getOrCreateSection(this.craftEngineItemUtils.getSettingsSection(),"equippable") : getOrCreateSection(this.craftEngineItemUtils.getDataSection(),"equippable");
            if (isValidString(assetId)) {
                ceEquipableSection.set("asset-id", assetId);
                this.setAssetId(assetId);
            }
            String slot;
            if (this.itemId.endsWith("_helmet")){
                slot = "head";
            } else if (this.itemId.endsWith("_chestplate")){
                slot = "chest";
            } else if (this.itemId.endsWith("_leggings")){
                slot = "legs";
            } else if (this.itemId.endsWith("_boots")){
                slot = "feet";
            } else {
                slot = equipmentSection.getString("slot");
                if (!isValidString(slot)){
                    Material material = this.craftEngineItemUtils.getMaterial();
                    String materialName = material.name();
                    if (materialName.endsWith("_HELMET") || materialName.endsWith("_SKULL") || materialName.endsWith("_HAT")){
                        slot = "head";
                    } else if (materialName.endsWith("_CHESTPLATE") || materialName.endsWith("_ELYTRA")){
                        slot = "chest";
                    } else if (materialName.endsWith("_LEGGINGS")){
                        slot = "legs";
                    } else if (materialName.endsWith("_BOOTS")){
                        slot = "feet";
                    } else {
                        slot = null;
                    }
                }
            }
            if (isValidString(slot)){
                ceEquipableSection.set("slot", slot);
                ConfigurationSection slotAttributeModifiers = equipmentSection.getConfigurationSection("slot_attribute_modifiers");
                if (isNotNull(slotAttributeModifiers)){
                    ConfigurationSection attributeModifiers = getOrCreateSection(this.craftEngineItemUtils.getComponentsSection(), "minecraft:attribute_modifiers");
                    List<Map<?, ?>> attributeModifiersMapList = attributeModifiers.getMapList("");
                    Map<String, Object> attributeModifiersMap = new HashMap<>();
                    attributeModifiersMap.put("type", "minecraft:armor");
                    attributeModifiersMap.put("slot", slot);
                    attributeModifiersMap.put("amount", slotAttributeModifiers.getDouble("armor",0f));
                    attributeModifiersMap.put("operation", "add_value");
                    attributeModifiersMap.put("id", UUID.randomUUID().toString());
                    attributeModifiersMapList.add(attributeModifiersMap);
                    this.craftEngineItemUtils.getComponentsSection().set("minecraft:attribute_modifiers", attributeModifiersMapList);
                }
            }
        }
        ConfigurationSection specificPropertiesSection = this.iaItemSection.getConfigurationSection("specific_properties");
        if (isNotNull(specificPropertiesSection)) {
            ConfigurationSection armorSection = specificPropertiesSection.getConfigurationSection("armor");
            if (isNotNull(armorSection)) {
                String assetId = armorSection.getString("custom_armor");
                if (isValidString(assetId)){
                    assetId = namespaced(assetId,this.namespace);
                    this.isValidString(assetId);
                    ConfigurationSection ceEquipableSection = getOrCreateSection(this.craftEngineItemUtils.getSettingsSection(),"equippable");
                    ceEquipableSection.set("asset-id", assetId);
                    this.setAssetId(assetId);
                    String slot = armorSection.getString("slot");
                    if (isValidString(slot)){
                        ceEquipableSection.set("slot", slot);
                    }
                }
            }
        }
    }

    @Override
    public void convertItemTexture(){
        ConfigurationSection resourceSection = this.iaItemSection.getConfigurationSection("resource");
        if (isNotNull(resourceSection)){
            boolean generate = resourceSection.getBoolean("generate", false);
            if (generate){
                IADirectionalMode directionalMode = IADirectionalMode.NONE;
                try {
                    directionalMode = IADirectionalMode.valueOf(resourceSection.getString("specific_properties.block.placed_model.directional_mode","NONE").toUpperCase());
                } catch (Exception ignored){
                }
                switch (directionalMode){
                    case NONE -> {
                        String texturePath = getTexturePath(resourceSection);
                        if (isValidString(texturePath)){
                            texturePath = namespaced(texturePath,this.namespace);
                            Map<String, Object> parsedTemplate = InternalTemplateManager.parseTemplate(Template.MODEL_ITEM_GENERATED, "%model_path%", texturePath, "%texture_path%", texturePath);
                            this.craftEngineItemUtils.getGeneralSection().createSection("model",parsedTemplate);
                        }
                    }
                    case ALL,LOG -> {
                        List<String> faceTextures = resourceSection.getStringList("textures");
                        Map<BlockFace, String> faceTextureMap = new HashMap<>();
                        if (faceTextures.size() != 6){
                            Logger.debug("[IAItemsConverter] Directional mode ALL requires 6 textures for item " + this.itemId);
                            return;
                        }
                        for (String faceTexture : faceTextures){
                            String cleanedTexture = cleanPath(faceTexture);
                            if (isNotNull(cleanedTexture)){
                                if (cleanedTexture.endsWith("_down")){
                                    faceTextureMap.put(BlockFace.DOWN, namespaced(cleanedTexture,this.namespace));
                                } else if (cleanedTexture.endsWith("_up")){
                                    faceTextureMap.put(BlockFace.UP, namespaced(cleanedTexture,this.namespace));
                                } else if (cleanedTexture.endsWith("_north")){
                                    faceTextureMap.put(BlockFace.NORTH, namespaced(cleanedTexture,this.namespace));
                                } else if (cleanedTexture.endsWith("_south")){
                                    faceTextureMap.put(BlockFace.SOUTH, namespaced(cleanedTexture,this.namespace));
                                } else if (cleanedTexture.endsWith("_west")){
                                    faceTextureMap.put(BlockFace.WEST, namespaced(cleanedTexture,this.namespace));
                                } else if (cleanedTexture.endsWith("_east")) {
                                    faceTextureMap.put(BlockFace.EAST, namespaced(cleanedTexture,this.namespace));
                                } else {
                                    Logger.debug("[IAItemsConverter] Invalid texture name " + faceTexture + " for directional mode ALL for item " + this.itemId);
                                    return;
                                }
                            }
                        }
                        if (faceTextureMap.size() != 6){
                            Logger.debug("[IAItemsConverter] Directional mode ALL requires 6 valid textures for item " + this.itemId);
                            return;
                        }
                        Map<String, Object> parsedTemplate = InternalTemplateManager.parseTemplate(Template.MODEL_CUBE, "%model_path%", faceTextureMap.get(BlockFace.NORTH),
                                "%texture_down_path%", faceTextureMap.get(BlockFace.DOWN),
                                "%texture_up_path%", faceTextureMap.get(BlockFace.UP),
                                "%texture_north_path%", faceTextureMap.get(BlockFace.NORTH),
                                "%texture_south_path%", faceTextureMap.get(BlockFace.SOUTH),
                                "%texture_west_path%", faceTextureMap.get(BlockFace.WEST),
                                "%texture_east_path%", faceTextureMap.get(BlockFace.EAST)
                        );
                        this.setSavedModelTemplates(parsedTemplate);
                        ConfigurationSection stateSection = this.craftEngineItemUtils.getStateSection();
                        stateSection.set("auto-state", "note_block");
                        ConfigurationSection properties = getOrCreateSection(stateSection, "properties");
                        ConfigurationSection axis = getOrCreateSection(properties, "axis");
                        axis.set("type", "axis");
                        axis.set("default", "y");
                        stateSection.set("appearances", InternalTemplateManager.parseTemplate(Template.BLOCK_STATE_LOG_APPEARANCE, "%model%", parsedTemplate));
                        ConfigurationSection variants = getOrCreateSection(stateSection, "variants");
                        ConfigurationSection axisX = getOrCreateSection(variants, "axis=x");
                        axisX.set("appearance", "axisX");
                        ConfigurationSection axisY = getOrCreateSection(variants, "axis=y");
                        axisY.set("appearance", "axisY");
                        ConfigurationSection axisZ = getOrCreateSection(variants, "axis=z");
                        axisZ.set("appearance", "axisZ");
                    }
                    case FURNACE -> {
                        List<String> faceTextures = resourceSection.getStringList("textures");
                        Map<BlockFace, String> faceTextureMap = new HashMap<>();
                        if (faceTextures.size() != 6){
                            Logger.debug("[IAItemsConverter] Directional mode ALL requires 6 textures for item " + this.itemId);
                            return;
                        }
                        for (String faceTexture : faceTextures){
                            String cleanedTexture = cleanPath(faceTexture);
                            if (isNotNull(cleanedTexture)){
                                if (cleanedTexture.endsWith("_down")){
                                    faceTextureMap.put(BlockFace.DOWN, namespaced(cleanedTexture,this.namespace));
                                } else if (cleanedTexture.endsWith("_up")){
                                    faceTextureMap.put(BlockFace.UP, namespaced(cleanedTexture,this.namespace));
                                } else if (cleanedTexture.endsWith("_north")){
                                    faceTextureMap.put(BlockFace.NORTH, namespaced(cleanedTexture,this.namespace));
                                } else if (cleanedTexture.endsWith("_south")){
                                    faceTextureMap.put(BlockFace.SOUTH, namespaced(cleanedTexture,this.namespace));
                                } else if (cleanedTexture.endsWith("_west")){
                                    faceTextureMap.put(BlockFace.WEST, namespaced(cleanedTexture,this.namespace));
                                } else if (cleanedTexture.endsWith("_east")) {
                                    faceTextureMap.put(BlockFace.EAST, namespaced(cleanedTexture,this.namespace));
                                } else {
                                    Logger.debug("[IAItemsConverter] Invalid texture name " + faceTexture + " for directional mode ALL for item " + this.itemId);
                                    return;
                                }
                            }
                        }
                        if (faceTextureMap.size() != 6){
                            Logger.debug("[IAItemsConverter] Directional mode Furnace requires 6 valid textures for item " + this.itemId);
                            return;
                        }
                        Map<String, Object> parsedTemplate = InternalTemplateManager.parseTemplate(Template.MODEL_CUBE, "%model_path%", faceTextureMap.get(BlockFace.NORTH),
                                "%texture_down_path%", faceTextureMap.get(BlockFace.DOWN),
                                "%texture_up_path%", faceTextureMap.get(BlockFace.UP),
                                "%texture_north_path%", faceTextureMap.get(BlockFace.NORTH),
                                "%texture_south_path%", faceTextureMap.get(BlockFace.SOUTH),
                                "%texture_west_path%", faceTextureMap.get(BlockFace.WEST),
                                "%texture_east_path%", faceTextureMap.get(BlockFace.EAST)
                        );
                        this.setSavedModelTemplates(parsedTemplate);
                        ConfigurationSection stateSection = this.craftEngineItemUtils.getStateSection();
                        stateSection.set("auto-state", "note_block");
                        ConfigurationSection properties = getOrCreateSection(stateSection, "properties");
                        ConfigurationSection facing = getOrCreateSection(properties, "facing");
                        facing.set("type", "4-direction");
                        facing.set("default", "north");
                        stateSection.set("appearances", InternalTemplateManager.parseTemplate(Template.BLOCK_STATE_4_DIRECTIONS_APPEARANCE, "%model%", parsedTemplate));
                        ConfigurationSection variants = getOrCreateSection(stateSection, "variants");
                        ConfigurationSection facingNorth = getOrCreateSection(variants, "facing=north");
                        facingNorth.set("appearance", "north");
                        ConfigurationSection facingSouth = getOrCreateSection(variants, "facing=south");
                        facingSouth.set("appearance", "south");
                        ConfigurationSection facingWest = getOrCreateSection(variants, "facing=west");
                        facingWest.set("appearance", "west");
                        ConfigurationSection facingEast = getOrCreateSection(variants, "facing=east");
                        facingEast.set("appearance", "east");
                    }
                    default -> {
                        Logger.debug("[IAItemsConverter] Directional mode " + directionalMode + " is not supported for item " + this.itemId);
                    }
                }
            } else {
                String modelPath = resourceSection.getString("model_path");
                if (isValidString(modelPath)){
                    modelPath = cleanPath(modelPath);
                    if (isNull(modelPath)){
                        Logger.debug("[IAItemsConverter] Invalid model path for item " + this.itemId);
                        return;
                    }

                    String namespacedModelPath = namespaced(modelPath,this.namespace);
                    Map<String, Object> parsedTemplate = InternalTemplateManager.parseTemplate(Template.MODEL_ITEM_DEFAULT, "%model_path%", namespacedModelPath);
                    this.setSavedModelTemplates(parsedTemplate);
                    this.craftEngineItemUtils.getGeneralSection().createSection("model",parsedTemplate);
                }
            }
        } else {
            ConfigurationSection graphicsSection = this.iaItemSection.getConfigurationSection("graphics");
            if (isNotNull(graphicsSection)){
                String modelPath = graphicsSection.getString("model");
                if (isValidString(modelPath)){
                    modelPath = namespaced(modelPath, this.namespace);
                    Map<String, Object> parsedTemplate = InternalTemplateManager.parseTemplate(Template.MODEL_ITEM_DEFAULT, "%model_path%", modelPath);
                    this.craftEngineItemUtils.getGeneralSection().createSection("model",parsedTemplate);
                    return;
                }
                boolean isBlock = this.iaItemSection.contains("behaviours.block.placed_model.type");
                String texturePath = graphicsSection.getString("texture");;
                if (isValidString(texturePath) && !isBlock){
                    texturePath = namespaced(texturePath,this.namespace);
                    Map<String, Object> parsedTemplate = InternalTemplateManager.parseTemplate(Template.MODEL_ITEM_GENERATED, "%model_path%", texturePath, "%texture_path%", texturePath);
                    this.craftEngineItemUtils.getGeneralSection().createSection("model",parsedTemplate);
                } else if (isBlock) {
                    BlockParent parent = null;
                    try {
                        parent = BlockParent.valueOf(graphicsSection.getString("parent", "").toUpperCase());
                    } catch (Exception ignored){
                    }
                    if (isNotNull(parent)){
                        String iconPath = graphicsSection.getString("icon");
                        if (isValidString(iconPath)){
                            iconPath = namespaced(iconPath,this.namespace);
                            this.craftEngineItemUtils.getGeneralSection().createSection("model",InternalTemplateManager.parseTemplate(Template.MODEL_ITEM_GENERATED, "%model_path%", iconPath, "%texture_path%", iconPath));
                        }
                        switch (parent){
                            case CROSS -> {
                                String crossTexture = graphicsSection.getString("textures.cross", graphicsSection.getString("texture"));
                                if (isValidString(crossTexture)){
                                    crossTexture = namespaced(crossTexture,this.namespace);
                                    ConfigurationSection behaviorSection = this.craftEngineItemUtils.getBehaviorSection();
                                    behaviorSection.set("type", "block_item");
                                    ConfigurationSection blockSection = getOrCreateSection(behaviorSection, "block");

                                    ConfigurationSection stateSection = getOrCreateSection(blockSection, "state");
                                    stateSection.set("properties",InternalTemplateManager.parseTemplate(Template.BLOCK_STATE_PROPERTIES_STAGE));
                                    stateSection.set("appearances",InternalTemplateManager.parseTemplate(Template.BLOCK_STATE_SAPLING_APPEARANCE, "%model%",
                                            InternalTemplateManager.parseTemplate(Template.MODEL_CROSS, "%model_path%", crossTexture, "%texture_path%", crossTexture)
                                            ));

                                }
                            }
                            default -> Logger.debug("[IAItemsConverter] Block parent " + parent + " is not supported for item " + this.itemId+". Please open an issue to request support.");
                        }
                    } else {
                        if (isValidString(texturePath)){
                            texturePath = namespaced(texturePath,this.namespace);
                            this.craftEngineItemUtils.setModel(InternalTemplateManager.parseTemplate(Template.MODEL_CUBE_ALL, "%model_path%", texturePath, "%texture_path%", texturePath));
                        }
                    }
                } else {
                    ConfigurationSection texturesSection = graphicsSection.getConfigurationSection("textures");
                    if (isNotNull(texturesSection)){
                        Set<String> keys = texturesSection.getKeys(false);
                        if (IAModelsKeys.BOW.containsAny(keys) && keys.size() == IAModelsKeys.BOW.getKeysCount()){
                            this.craftEngineItemUtils.setModel(InternalTemplateManager.parseTemplate(Template.MODEL_2D_BOW_SIMPLIFIED,
                                    "%default_texture_path%", namespaced(texturesSection.getString("normal"),this.namespace),
                                    "%pulling_0_texture_path%", namespaced(texturesSection.getString("pulling_0"),this.namespace),
                                    "%pulling_1_texture_path%", namespaced(texturesSection.getString("pulling_1"),this.namespace),
                                    "%pulling_2_texture_path%", namespaced(texturesSection.getString("pulling_2"),this.namespace)
                            ));
                        } else if (IAModelsKeys.FISHING_ROD.containsAny(keys) && keys.size() == IAModelsKeys.FISHING_ROD.getKeysCount()){
                            this.craftEngineItemUtils.setModel(InternalTemplateManager.parseTemplate(Template.MODEL_2D_FISHING_ROD_SIMPLIFIED,
                                    "%default_texture_path%", namespaced(texturesSection.getString("normal"),this.namespace),
                                    "%cast_texture_path%", namespaced(texturesSection.getString("cast"),this.namespace)
                            ));
                        } else if (IAModelsKeys.CROSSBOW.containsAny(keys) && keys.size() == IAModelsKeys.CROSSBOW.getKeysCount()){
                            this.craftEngineItemUtils.setModel(InternalTemplateManager.parseTemplate(Template.MODEL_2D_CROSSBOW_SIMPLIFIED,
                                    "%default_texture_path%", namespaced(texturesSection.getString("normal"),this.namespace),
                                    "%pulling_0_texture_path%", namespaced(texturesSection.getString("pulling_0"),this.namespace),
                                    "%pulling_1_texture_path%", namespaced(texturesSection.getString("pulling_1"),this.namespace),
                                    "%pulling_2_texture_path%", namespaced(texturesSection.getString("pulling_2"),this.namespace),
                                    "%charged_rocket_texture_path%", namespaced(texturesSection.getString("rocket"),this.namespace),
                                    "%charged_arrow_texture_path%", namespaced(texturesSection.getString("arrow"),this.namespace)
                            ));
                        }
                    } else {
                        ConfigurationSection modelsSection = graphicsSection.getConfigurationSection("models");
                        if (isNotNull(modelsSection)){
                            Set<String> keys = modelsSection.getKeys(false);
                            if (IAModelsKeys.BOW.containsAny(keys) && keys.size() == IAModelsKeys.BOW.getKeysCount()){
                                this.craftEngineItemUtils.setModel(InternalTemplateManager.parseTemplate(Template.MODEL_3D_BOW,
                                        "%default_model_path%", namespaced(modelsSection.getString("normal"),this.namespace),
                                        "%pulling_0_model_path%", namespaced(modelsSection.getString("pulling_0"),this.namespace),
                                        "%pulling_1_model_path%", namespaced(modelsSection.getString("pulling_1"),this.namespace),
                                        "%pulling_2_model_path%", namespaced(modelsSection.getString("pulling_2"),this.namespace)
                                ));
                            } else if (IAModelsKeys.FISHING_ROD.containsAny(keys) && keys.size() == IAModelsKeys.FISHING_ROD.getKeysCount()){
                                this.craftEngineItemUtils.setModel(InternalTemplateManager.parseTemplate(Template.MODEL_3D_FISHING_ROD,
                                        "%default_model_path%", namespaced(modelsSection.getString("normal"),this.namespace),
                                        "%casting_model_path%", namespaced(modelsSection.getString("cast"),this.namespace)
                                ));
                            } else if (IAModelsKeys.CROSSBOW.containsAny(keys) && keys.size() == IAModelsKeys.CROSSBOW.getKeysCount()){
                                this.craftEngineItemUtils.setModel(InternalTemplateManager.parseTemplate(Template.MODEL_3D_CROSSBOW,
                                        "%default_model_path%", namespaced(modelsSection.getString("normal"),this.namespace),
                                        "%pulling_0_model_path%", namespaced(modelsSection.getString("pulling_0"),this.namespace),
                                        "%pulling_1_model_path%", namespaced(modelsSection.getString("pulling_1"),this.namespace),
                                        "%pulling_2_model_path%", namespaced(modelsSection.getString("pulling_2"),this.namespace),
                                        "%charged_rocket_model_path%", namespaced(modelsSection.getString("rocket"),this.namespace),
                                        "%charged_arrow_model_path%", namespaced(modelsSection.getString("arrow"),this.namespace)
                                ));
                            } else if (IAModelsKeys.TRIDENT.containsAny(keys) && keys.size() == IAModelsKeys.TRIDENT.getKeysCount()) {
                                this.craftEngineItemUtils.setModel(InternalTemplateManager.parseTemplate(Template.MODEL_TRIDENT,
                                        "%model_path%", namespaced(modelsSection.getString("normal"), this.namespace),
                                        "%throwing_model_path%", namespaced(modelsSection.getString("throwing"), this.namespace)
                                ));
                            } else if (IAModelsKeys.SHIELD.containsAny(keys) && keys.size() == IAModelsKeys.SHIELD.getKeysCount()) {
                                this.craftEngineItemUtils.setModel(InternalTemplateManager.parseTemplate(Template.MODEL_3D_SHIELD,
                                        "%default_model_path%", namespaced(modelsSection.getString("normal"), this.namespace),
                                        "%blocking_model_path%", namespaced(modelsSection.getString("blocking"), this.namespace)
                                ));
                            }
                        }
                    }
                }
            }
        }
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
                                this.craftEngineItemUtils.getSettingsSection().set("fuel-time", burnTicks);
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
        Set<FurniturePlacement> placements = new HashSet<>();
        ConfigurationSection placeableSection = furnitureSection.getConfigurationSection("placeable_on");
        if (isNotNull(placeableSection)){
            boolean placeableOnFloor = placeableSection.getBoolean("floor", true);
            boolean placeableOnCeiling = placeableSection.getBoolean("ceiling", true);
            boolean placeableOnWall = placeableSection.getBoolean("wall", true);
            if (placeableOnFloor){
                placements.add(FurniturePlacement.GROUND);
            }
            if (placeableOnCeiling){
                placements.add(FurniturePlacement.CEILING);
            }
            if (placeableOnWall){
                placements.add(FurniturePlacement.WALL);
            }
        } else {
            placements.addAll(List.of(FurniturePlacement.values()));
        }
        if (!placements.isEmpty()){
            Billboard transformType = Billboard.FIXED;
            ItemDisplayType displayType = ItemDisplayType.FIXED;

            FloatsUtils displayTranslation = new FloatsUtils(3,new float[]{0f,0.5f,0f});;
            FloatsUtils scale = new FloatsUtils(3, new float[]{1f,1f,1f});

            ConfigurationSection displayTransformationSection = furnitureSection.getConfigurationSection("display_transformation");
            if (isNotNull(displayTransformationSection)){
                try {
                    displayType = ItemDisplayType.valueOf(displayTransformationSection.getString("transform","FIXED").toUpperCase());
                } catch (Exception exception){
                    Logger.debug("[IAItemsConverter] Unknown furniture display transform type for item "+this.itemId+": "+displayTransformationSection.getString("transform"));
                }
                ConfigurationSection translationSection = displayTransformationSection.getConfigurationSection("translation");
                if (isNotNull(translationSection)){
                    double x = translationSection.getDouble("x");
                    double y = translationSection.getDouble("y");
                    double z = translationSection.getDouble("z");
                    if (x != 0d){
                        displayTranslation.setValue(0,(float)x);
                    }
                    if (y != 0d){
                        displayTranslation.setValue(1,(float)y);
                    }
                    if (z != 0d) {
                        displayTranslation.setValue(2, (float) z);
                    }
                }
                ConfigurationSection scaleSection = displayTransformationSection.getConfigurationSection("scale");
                if (isNotNull(scaleSection)){
                    double x = scaleSection.getDouble("x",1.0);
                    double y = scaleSection.getDouble("y",1.0);
                    double z = scaleSection.getDouble("z",1.0);
                    if (x != 1.0){
                        scale.setValue(0,(float)x);
                    }
                    if (y != 1.0){
                        scale.setValue(1,(float)y);
                    }
                    if (z != 1.0) {
                        scale.setValue(2, (float) z);
                    }
                }
            }

            List<Map<String,Object>> elements = new ArrayList<>();
            Map<String,Object> map = new HashMap<>();
            map.put("item", this.itemId);
            map.put("display-transform", displayType.name());
            map.put("billboard", transformType.name());
            map.put("translation", displayTranslation.toString());
            map.put("scale", scale.toString());
            elements.add(map);

            double sitHeight = behavioursSection.getDouble("furniture_sit.sit_height", 0d);
            List<Map<String,Object>> hitboxes = new ArrayList<>();
            ConfigurationSection iaHitboxesSection = furnitureSection.getConfigurationSection("hitbox");

            if (isNotNull(iaHitboxesSection)) {
                parseItemsAdderHitboxes(iaHitboxesSection, hitboxes, sitHeight);
            }
            ConfigurationSection behaviorSection = this.craftEngineItemUtils.getBehaviorSection();
            behaviorSection.set("type","furniture_item");
            getOrCreateSection(behaviorSection, "settings").set("item", this.itemId);
            ConfigurationSection ceFurnitureSection = getOrCreateSection(behaviorSection, "furniture");
            ConfigurationSection cePlacementSection = getOrCreateSection(ceFurnitureSection, "placement");

            for (FurniturePlacement furniturePlacement : placements){
                ConfigurationSection ceTypePlacementSection = cePlacementSection.createSection(furniturePlacement.name().toLowerCase());
                ceTypePlacementSection.set("elements", elements);
                if (!hitboxes.isEmpty()){
                    ceTypePlacementSection.set("hitboxes", hitboxes);
                }
            }

            ceFurnitureSection.set("loot", InternalTemplateManager.parseTemplate(Template.LOOT_TABLE_BASIC_DROP, "%type%","furniture_item","%item%", this.itemId));

        }
    }

    private void parseItemsAdderHitboxes(ConfigurationSection iaHitboxesSection, List<Map<String,Object>> hitboxes, double seatPosition) {
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
                    float posX = x + lengthOffset;
                    float posY = y + heightOffset;
                    float posZ = z + widthOffset;

                    Map<String, Object> hitbox = new HashMap<>();
                    hitbox.put("type", "shulker");
                    hitbox.put("position", posX + "," + posY + "," + posZ);

                    if (x == 0 && y == 0 && z == 0) {
                        hitbox.put("seats", List.of("0,"+seatPosition+",0 0"));
                    }

                    hitboxes.add(hitbox);
                }
            }
        }
    }
}