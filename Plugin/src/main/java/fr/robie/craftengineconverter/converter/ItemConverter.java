package fr.robie.craftengineconverter.converter;

import fr.robie.craftengineconverter.common.CraftEngineItemsConfiguration;
import fr.robie.craftengineconverter.common.utils.ObjectUtils;
import fr.robie.craftengineconverter.utils.enums.Template;
import fr.robie.craftengineconverter.utils.manager.InternalTemplateManager;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class ItemConverter extends ObjectUtils {
    protected final @NotNull String itemId;
    private final Converter converter;
    private final Map<String,Object> savedModelTemplates = new HashMap<>();
    public final CraftEngineItemUtils craftEngineItemUtils;
    protected boolean excludeFromInventory = false;
    protected YamlConfiguration fileConfig;
    protected String assetId;

    protected CraftEngineItemsConfiguration craftEngineItemsConfiguration = new CraftEngineItemsConfiguration();

    public ItemConverter(@NotNull String itemId, ConfigurationSection craftEngineItemSection, Converter converter, YamlConfiguration fileConfig) {
        this.itemId = itemId;
        this.converter = converter;
        this.craftEngineItemUtils = new CraftEngineItemUtils(craftEngineItemSection);
        this.fileConfig = fileConfig;
        this.fileConfig.options().pathSeparator('\n');
    }

    public void convertItem(){
        convertMaterial();
        convertItemName();
        convertLore();
        convertDyedColor();
        convertUnbreakable();
        convertItemFlags();
        convertExcludeFromInventory();
        convertAttributeModifiers();
        convertEnchantments();
        convertCustomModelData();
        convertItemModel();
        convertMaxStackSize();
        convertEnchantmentGlintOverride();
        convertFireResistance();
        convertMaxDamage();
        convertGlowDropColor();
        convertDropShowName();
        convertHideTooltip();
        convertFood();
        convertTool();
        convertCustomData();
        convertJukeboxPlayable();
        convertConsumable();
        convertEquippable();
        convertDamageResistance();
        convertEnchantableComponent();
        convertGliderComponent();
        convertToolTipStyle();
        convertUseCooldown();
        convertUseRemainderComponent();
        convertAnvilRepairable();
        convertDeathProtection();
        convertToolTipDisplay();
        convertBreakSound();
        convertWeaponComponent();
        convertBlocksAttackComponent();
        convertCanPlaceOnComponent();
        convertCanBreakComponent();
        convertOversizedInGui();
        convertPaintingVariant();
        convertKineticComponent();
        convertPiercingWeaponComponent();
        convertAttackRangeComponent();
        convertSwingAnimationComponent();
        convertUseEffectsComponent();
        convertDamageTypeComponent();
        convertMinimumAttackChargeComponent();
        convertProfileComponent();
        convertItemTexture();
        convertOther();
    }

    public void convertMaterial(){}
    public void convertItemName(){}
    public void convertLore(){}
    public void convertDyedColor(){}
    public void convertUnbreakable(){}
    public void convertGlowDropColor(){}
    public void convertDropShowName(){}
    public void convertItemFlags(){}
    public void convertAttributeModifiers(){}
    public void convertEnchantments(){}
    public void convertCustomModelData(){}
    public void convertItemModel(){}
    public void convertMaxStackSize(){}
    public void convertEnchantmentGlintOverride(){}
    public void convertFireResistance(){}
    public void convertMaxDamage(){}
    public void convertHideTooltip(){}
    public void convertFood(){}
    public void convertTool(){}
    public void convertCustomData(){}
    public void convertJukeboxPlayable(){}
    public void convertConsumable(){}
    public void convertEquippable(){}
    public void convertDamageResistance(){}
    public void convertEnchantableComponent(){}
    public void convertGliderComponent(){}
    public void convertToolTipStyle(){}
    public void convertUseCooldown(){}
    public void convertUseRemainderComponent(){}
    public void convertAnvilRepairable(){}
    public void convertDeathProtection(){}
    public void convertToolTipDisplay(){}
    public void convertBreakSound(){}
    public void convertWeaponComponent(){}
    public void convertBlocksAttackComponent(){}
    public void convertCanPlaceOnComponent(){}
    public void convertCanBreakComponent(){}
    public void convertOversizedInGui(){}
    public void convertPaintingVariant(){}
    public void convertKineticComponent(){}
    public void convertPiercingWeaponComponent(){}
    public void convertAttackRangeComponent(){}
    public void convertSwingAnimationComponent(){}
    public void convertUseEffectsComponent(){}
    public void convertDamageTypeComponent(){}
    public void convertMinimumAttackChargeComponent(){}
    public void convertProfileComponent(){}
    public void convertItemTexture(){}
    public void convertExcludeFromInventory(){}
    public void convertOther(){}

    public void setSavedModelTemplates(Map<String,Object> savedModelTemplates){
        this.savedModelTemplates.clear();
        if (savedModelTemplates != null && !savedModelTemplates.isEmpty()) {
            this.savedModelTemplates.putAll(savedModelTemplates);
        }
    }

    public Map<String,Object> getSavedModelTemplates(){
        return new HashMap<>(this.savedModelTemplates);
    }

    protected boolean notEmptyOrNull(List<String> list, int index) {
        return list != null && list.size() > index && list.get(index) != null && !list.get(index).isEmpty();
    }

    protected void setIfNotNull(ConfigurationSection section, String key, Object value) {
        if (value != null) {
            section.set(key, value);
        }
    }

    protected void setIfNotEmpty(ConfigurationSection section, String key, String value) {
        if (value != null && !value.isEmpty()) {
            section.set(key, value);
        }
    }

    protected void setIfTrue(ConfigurationSection section, String key, boolean value) {
        if (value) {
            section.set(key, true);
        }
    }

    public void setAssetId(String assetId){
        this.assetId = assetId;
    }

    @Nullable
    protected String getTexturePath(@NotNull ConfigurationSection packSection) {
        List<String> textures = packSection.getStringList("textures");
        if (!textures.isEmpty()) {
            return textures.getFirst();
        }
        String string = packSection.getString("textures");

        return isValidString(string) ? string : packSection.getString("texture");
    }

    protected ConfigurationSection getEquipmentsSection(){
        ConfigurationSection equipementsSection = this.fileConfig.getConfigurationSection("equipments");
        if (equipementsSection == null) {
            return this.fileConfig.createSection("equipments");
        }
        return equipementsSection;
    }

    public CraftEngineItemsConfiguration getCraftEngineItemsConfiguration() {
        return this.craftEngineItemsConfiguration;
    }

    public boolean isExcludeFromInventory() {
        return this.excludeFromInventory;
    }

    public Converter getConverter() {
        return this.converter;
    }

    public Map<String,Object> getEffectMap(String effectName,double amplifier,int duration, boolean ambient, boolean show_particles, boolean show_icon){
        return InternalTemplateManager.parseTemplate(Template.MINECRAFT_EFFECT,"%effect_id%",effectName,"%effect_amplifier%",amplifier,"%effect_duration%",
                duration, "%effect_ambient%", ambient, "%effect_show_particles%", show_particles,
                "%effect_show_icon%" ,show_icon);
    }
}
