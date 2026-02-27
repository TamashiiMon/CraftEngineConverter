package fr.robie.craftengineconverter.common.items;

import fr.robie.craftengineconverter.common.utils.ItemConfigurationSerializable;
import fr.robie.craftengineconverter.common.utils.enums.FurniturePlacement;
import fr.robie.craftengineconverter.common.utils.enums.ItemDisplayType;
import net.momirealms.craftengine.core.entity.display.Billboard;
import net.momirealms.craftengine.core.entity.display.TextDisplayAlignment;
import org.bukkit.DyeColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class FurnitureConfiguration implements ItemConfigurationSerializable {
    private Settings settings;
    private String loot;
    private final Map<FurniturePlacement, Placement> placements = new EnumMap<>(FurniturePlacement.class);

    @NotNull
    public Settings getOrCreateSettings(String itemId) {
        if (this.settings == null)
            this.settings = new Settings(itemId);
        return this.settings;
    }

    public Placement getOrCreatePlacement(FurniturePlacement type) {
        return this.placements.computeIfAbsent(type, Placement::new);
    }

    public void setLoot(String loot) { this.loot = loot; }

    @Override
    public void serialize(@NotNull YamlConfiguration yamlConfiguration, @NotNull String path, @NotNull ConfigurationSection itemSection, @NotNull String itemId) {
        ConfigurationSection behaviorSection = getOrCreateSection(itemSection, "behavior");
        behaviorSection.set("type", "furniture_item");

        ConfigurationSection settingsSection = getOrCreateSection(behaviorSection, "settings");
        settingsSection.set("item", itemId);

        if (this.settings != null) {
            ConfigurationSection furnitureSettingsSection = getOrCreateSection(settingsSection, "furniture");
            furnitureSettingsSection.set("item", this.settings.getItem());
            if (this.settings.getHitTimes() != null)
                furnitureSettingsSection.set("hit-times", this.settings.getHitTimes());
            if (this.settings.getBreakSound() != null)
                getOrCreateSection(furnitureSettingsSection, "sounds").set("break", this.settings.getBreakSound());
            if (this.settings.getPlaceSound() != null)
                getOrCreateSection(furnitureSettingsSection, "sounds").set("place", this.settings.getPlaceSound());
            if (this.settings.getHitSound() != null)
                getOrCreateSection(furnitureSettingsSection, "sounds").set("hit", this.settings.getHitSound());
        }

        ConfigurationSection furnitureSection = getOrCreateSection(behaviorSection, "furniture");

        if (!this.placements.isEmpty()) {
            ConfigurationSection placementSection = getOrCreateSection(furnitureSection, "placement");
            for (Placement placement : this.placements.values()) {
                placement.serialize(placementSection);
            }
        }
    }

    public static class Settings {
        private final String item;
        private Integer hitTimes;
        private String breakSound;
        private String placeSound;
        private String hitSound;

        public Settings(String item) { this.item = item; }

        public String getItem() { return this.item; }
        public String getBreakSound() { return this.breakSound; }
        public void setBreakSound(String breakSound) { this.breakSound = breakSound; }
        public String getPlaceSound() { return this.placeSound; }
        public void setPlaceSound(String placeSound) { this.placeSound = placeSound; }
        public String getHitSound() { return this.hitSound; }
        public void setHitSound(String hitSound) { this.hitSound = hitSound; }
        public Integer getHitTimes() { return this.hitTimes; }
        public void setHitTimes(Integer hitTimes) { this.hitTimes = hitTimes; }
    }

    public static class Placement {
        private final FurniturePlacement type;
        private final float[] lootSpawnOffset = new float[3];
        private boolean entityCulling = false;

        private final List<Element> elements = new ArrayList<>();
        private final List<Hitboxe> hitboxes = new ArrayList<>();

        private String betterModel;
        private String modelEngine;

        public Placement(FurniturePlacement type) { this.type = type; }

        public FurniturePlacement getType() { return this.type; }

        public void setBetterModel(String betterModel) { this.betterModel = betterModel; }

        public void setModelEngine(String modelEngine) { this.modelEngine = modelEngine; }

        public void setLootSpawnOffset(float x, float y, float z) {
            this.lootSpawnOffset[0] = x;
            this.lootSpawnOffset[1] = y;
            this.lootSpawnOffset[2] = z;
        }

        public void addElement(Element element) { this.elements.add(element); }

        public void addHitbox(Hitboxe hitbox) { this.hitboxes.add(hitbox); }

        public void setEntityCulling(boolean entityCulling) { this.entityCulling = entityCulling; }

        public void serialize(ConfigurationSection placementSection) {
            ConfigurationSection typeSection = placementSection.createSection(this.type.name().toLowerCase());

            if (this.entityCulling)
                typeSection.set("entity-culling", true);

            if (this.betterModel != null) {
                typeSection.set("better-model", this.betterModel);
                return;
            }
            if (this.modelEngine != null) {
                typeSection.set("model-engine", this.modelEngine);
                return;
            }

            if (this.lootSpawnOffset[0] != 0 || this.lootSpawnOffset[1] != 0 || this.lootSpawnOffset[2] != 0)
                typeSection.set("loot-spawn-offset", this.lootSpawnOffset);

            if (!this.elements.isEmpty()) {
                List<Map<String, Object>> serializedElements = new ArrayList<>();
                for (Element element : this.elements) {
                    serializedElements.add(element.serialize());
                }
                typeSection.set("elements", serializedElements);
            }

            if (!this.hitboxes.isEmpty()) {
                List<Map<String, Object>> serializedHitboxes = new ArrayList<>();
                for (Hitboxe hitbox : this.hitboxes) {
                    serializedHitboxes.add(hitbox.serialize());
                }
                typeSection.set("hitboxes", serializedHitboxes);
            }
        }
    }

    public interface Element {
        Map<String, Object> serialize();
    }

    public static abstract class ItemElement implements Element {
        protected final String item;
        protected boolean applyDyedColor = true;
        protected final float[] position = new float[3];

        protected ItemElement(@NotNull String item) { this.item = item; }

        public void setApplyDyedColor(boolean applyDyedColor) { this.applyDyedColor = applyDyedColor; }
        public void setPosition(float x, float y, float z) { this.position[0] = x; this.position[1] = y; this.position[2] = z; }

        protected void serialize(Map<String, Object> data) {
            data.put("item", this.item);
            if (!this.applyDyedColor)
                data.put("apply-dyed-color", false);
            if (this.position[0] != 0 || this.position[1] != 0 || this.position[2] != 0)
                data.put("position", this.position);
        }
    }


    public static class SimpleItemElement extends ItemElement {
        public SimpleItemElement(@NotNull String item) { super(item); }

        @Override
        public Map<String, Object> serialize() {
            Map<String, Object> data = new HashMap<>();
            data.put("type", "item");
            super.serialize(data);
            return data;
        }
    }

    public static class ArmorStandElement extends ItemElement {
        private final float[] scale = new float[]{1, 1, 1};
        private boolean isSmall = false;
        private DyeColor glowColor = DyeColor.WHITE;

        public ArmorStandElement(@NotNull String item) { super(item); }

        public void setScale(float x, float y, float z) { this.scale[0] = x; this.scale[1] = y; this.scale[2] = z; }
        public void setSmall(boolean small) { this.isSmall = small; }
        public void setGlowColor(@NotNull DyeColor glowColor) { this.glowColor = glowColor; }

        @Override
        public Map<String, Object> serialize() {
            Map<String, Object> data = new HashMap<>();
            data.put("type", "armor_stand");
            super.serialize(data);
            if (this.scale[0] != 1 || this.scale[1] != 1 || this.scale[2] != 1)
                data.put("scale", this.scale);
            if (this.isSmall)
                data.put("small", true);
            if (this.glowColor != DyeColor.WHITE)
                data.put("glow-color", this.glowColor.name().toLowerCase());
            return data;
        }
    }

    public static class ItemDisplayElement extends ItemElement {
        private ItemDisplayType displayTransform = ItemDisplayType.FIXED;

        private final DisplayProperties display = new DisplayProperties();

        public ItemDisplayElement(@NotNull String item) { super(item); }

        public void setDisplayTransform(@NotNull ItemDisplayType transform) { this.displayTransform = transform; }
        public DisplayProperties display() { return this.display; }

        @Override
        public Map<String, Object> serialize() {
            Map<String, Object> data = new HashMap<>();
            data.put("type", "item_display");
            super.serialize(data);
            if (this.displayTransform != ItemDisplayType.FIXED)
                data.put("display-transform", this.displayTransform.name().toLowerCase());
            this.display.serialize(data);
            return data;
        }
    }

    public static class TextDisplayElement implements Element {
        private final String text;
        private int lineWidth = -1;
        private int[] backgroundColor = null;
        private int textOpacity = -1;
        private boolean hasShadow = false;
        private boolean seeThrough = false;
        private boolean useDefaultBackground = true;
        private TextDisplayAlignment alignment = null;

        private final DisplayProperties display = new DisplayProperties();

        public TextDisplayElement(@NotNull String text) { this.text = text; }

        public DisplayProperties display() { return this.display; }
        public void setLineWidth(int lineWidth) { this.lineWidth = lineWidth; }
        public void setBackgroundColor(int a, int r, int g, int b) { this.backgroundColor = new int[]{a, r, g, b}; }
        public void setTextOpacity(int textOpacity) { this.textOpacity = textOpacity; }
        public void setHasShadow(boolean hasShadow) { this.hasShadow = hasShadow; }
        public void setSeeThrough(boolean seeThrough) { this.seeThrough = seeThrough; }
        public void setUseDefaultBackground(boolean useDefaultBackground) { this.useDefaultBackground = useDefaultBackground; }
        public void setAlignment(@NotNull TextDisplayAlignment alignment) { this.alignment = alignment; }

        @Override
        public Map<String, Object> serialize() {
            Map<String, Object> data = new HashMap<>();
            data.put("type", "text_display");
            data.put("text", this.text);
            this.display.serialize(data);
            if (this.lineWidth != -1) data.put("line-width", this.lineWidth);
            if (this.backgroundColor != null) data.put("background-color", this.backgroundColor);
            if (this.textOpacity != -1) data.put("text-opacity", this.textOpacity);
            if (this.hasShadow) data.put("has-shadow", true);
            if (this.seeThrough) data.put("is-see-through", true);
            if (!this.useDefaultBackground) data.put("use-default-background-color", false);
            if (this.alignment != null) data.put("alignment", this.alignment.name().toLowerCase());
            return data;
        }
    }

    public static class DisplayProperties {
        private Billboard billboard = Billboard.FIXED;
        private final float[] translation = new float[3];
        private final float[] scale = new float[]{1, 1, 1};
        private final float[] glowColor = {-1, -1, -1};
        private int brightnessBlockLight = -1;
        private int brightnessSkyLight = -1;
        private float viewRange = 1.0f;
        private float[] rotationEuler = null;
        private float[] rotationQuaternion = null;
        private Float rotationYaw = null;

        public void setBillboard(@NotNull Billboard billboard) { this.billboard = billboard; }
        public void setTranslation(float x, float y, float z) { this.translation[0] = x; this.translation[1] = y; this.translation[2] = z; }
        public void setScale(float x, float y, float z) { this.scale[0] = x; this.scale[1] = y; this.scale[2] = z; }
        public void setGlowColor(float r, float g, float b) { this.glowColor[0] = r; this.glowColor[1] = g; this.glowColor[2] = b; }
        public void setBrightness(int blockLight, int skyLight) { this.brightnessBlockLight = blockLight; this.brightnessSkyLight = skyLight; }
        public void setViewRange(float viewRange) { this.viewRange = viewRange; }
        public void setRotationYaw(float yaw) { this.rotationYaw = yaw; this.rotationEuler = null; this.rotationQuaternion = null; }
        public void setRotationEuler(float x, float y, float z) { this.rotationEuler = new float[]{x, y, z}; this.rotationQuaternion = null; this.rotationYaw = null; }
        public void setRotationQuaternion(float x, float y, float z, float w) { this.rotationQuaternion = new float[]{x, y, z, w}; this.rotationEuler = null; this.rotationYaw = null; }

        public void serialize(Map<String, Object> data) {
            if (this.billboard != Billboard.FIXED)
                data.put("billboard", this.billboard.name().toLowerCase());
            if (this.translation[0] != 0 || this.translation[1] != 0 || this.translation[2] != 0)
                data.put("translation", this.translation);
            if (this.scale[0] != 1 || this.scale[1] != 1 || this.scale[2] != 1)
                data.put("scale", this.scale);
            if (this.glowColor[0] != -1 || this.glowColor[1] != -1 || this.glowColor[2] != -1)
                data.put("glow-color", this.glowColor);
            if (this.brightnessBlockLight != -1 || this.brightnessSkyLight != -1) {
                Map<String, Integer> brightness = new HashMap<>();
                if (this.brightnessBlockLight != -1) brightness.put("block-light", this.brightnessBlockLight);
                if (this.brightnessSkyLight != -1) brightness.put("sky-light", this.brightnessSkyLight);
                data.put("brightness", brightness);
            }
            if (this.viewRange != 1.0f)
                data.put("view-range", this.viewRange);
            if (this.rotationYaw != null)
                data.put("rotation", this.rotationYaw);
            else if (this.rotationEuler != null)
                data.put("rotation", this.rotationEuler);
            else if (this.rotationQuaternion != null)
                data.put("rotation", this.rotationQuaternion);
        }
    }

    public interface Hitboxe extends Element {}

    public static abstract class BaseHitbox implements Hitboxe {
        protected final float[] position = new float[3];
        protected final List<String> seats = new ArrayList<>();

        public void setPosition(float x, float y, float z) { this.position[0] = x; this.position[1] = y; this.position[2] = z; }
        public void addSeat(float x, float y, float z, float yaw) { this.seats.add(x + "," + y + "," + z + " " + yaw); }

        protected void serialize(Map<String, Object> data) {
            if (this.position[0] != 0 || this.position[1] != 0 || this.position[2] != 0)
                data.put("position", this.position[0] + "," + this.position[1] + "," + this.position[2]);
            if (!this.seats.isEmpty())
                data.put("seats", this.seats);
        }
    }

    public static abstract class CollidableHitbox extends BaseHitbox {
        protected boolean canUseItemOn;
        protected boolean canBeHitByProjectile;
        protected boolean blocksBuilding;
        protected boolean interactive = true;
        protected boolean invisible = false;

        protected CollidableHitbox(boolean canUseItemOnDefault, boolean canBeHitByProjectileDefault, boolean blocksBuildingDefault) {
            this.canUseItemOn = canUseItemOnDefault;
            this.canBeHitByProjectile = canBeHitByProjectileDefault;
            this.blocksBuilding = blocksBuildingDefault;
        }

        public void setCanUseItemOn(boolean canUseItemOn) { this.canUseItemOn = canUseItemOn; }
        public void setCanBeHitByProjectile(boolean canBeHitByProjectile) { this.canBeHitByProjectile = canBeHitByProjectile; }
        public void setBlocksBuilding(boolean blocksBuilding) { this.blocksBuilding = blocksBuilding; }
        public void setInteractive(boolean interactive) { this.interactive = interactive; }
        public void setInvisible(boolean invisible) { this.invisible = invisible; }

        @Override
        protected void serialize(Map<String, Object> data) {
            super.serialize(data);
            if (this.canUseItemOn != getDefaultCanUseItemOn()) data.put("can-use-item-on", this.canUseItemOn);
            if (this.canBeHitByProjectile != getDefaultCanBeHitByProjectile()) data.put("can-be-hit-by-projectile", this.canBeHitByProjectile);
            if (this.blocksBuilding != getDefaultBlocksBuilding()) data.put("blocks-building", this.blocksBuilding);
            if (!this.interactive) data.put("interactive", false);
            if (this.invisible) data.put("invisible", true);
        }

        protected abstract boolean getDefaultCanUseItemOn();
        protected abstract boolean getDefaultCanBeHitByProjectile();
        protected abstract boolean getDefaultBlocksBuilding();
    }

    public static class InteractionHitbox extends CollidableHitbox {
        private float width = 1;
        private float height = 2;

        public InteractionHitbox() { super(false, false, false); }

        public void setWidth(float width) { this.width = width; }
        public void setHeight(float height) { this.height = height; }

        @Override
        protected boolean getDefaultCanUseItemOn() { return false; }
        @Override
        protected boolean getDefaultCanBeHitByProjectile() { return false; }
        @Override
        protected boolean getDefaultBlocksBuilding() { return false; }

        @Override
        public Map<String, Object> serialize() {
            Map<String, Object> data = new HashMap<>();
            data.put("type", "interaction");
            super.serialize(data);
            if (this.width != 1) data.put("width", this.width);
            if (this.height != 2) data.put("height", this.height);
            return data;
        }
    }

    public static class ShulkerHitbox extends CollidableHitbox {
        private float scale = 1;
        private int peek = 0;
        private String direction = "UP";
        private boolean interactionEntity = true;

        public ShulkerHitbox() { super(true, true, true); }

        public void setScale(float scale) { this.scale = scale; }
        public void setPeek(int peek) { this.peek = peek; }
        public void setDirection(@NotNull String direction) { this.direction = direction; }
        public void setInteractionEntity(boolean interactionEntity) { this.interactionEntity = interactionEntity; }

        @Override protected boolean getDefaultCanUseItemOn() { return true; }
        @Override protected boolean getDefaultCanBeHitByProjectile() { return true; }
        @Override protected boolean getDefaultBlocksBuilding() { return true; }

        @Override
        public Map<String, Object> serialize() {
            Map<String, Object> data = new HashMap<>();
            data.put("type", "shulker");
            super.serialize(data);
            if (this.scale != 1) data.put("scale", this.scale);
            if (this.peek != 0) data.put("peek", this.peek);
            if (!this.direction.equals("UP")) data.put("direction", this.direction.toLowerCase());
            if (!this.interactionEntity) data.put("interaction-entity", false);
            return data;
        }
    }

    public static class HappyGhastHitbox extends CollidableHitbox {
        private boolean hardCollision = true;
        private float scale = 0.25f;

        public HappyGhastHitbox() { super(true, true, true); }

        public void setHardCollision(boolean hardCollision) { this.hardCollision = hardCollision; }
        public void setScale(float scale) { this.scale = scale; }

        @Override protected boolean getDefaultCanUseItemOn() { return true; }
        @Override protected boolean getDefaultCanBeHitByProjectile() { return true; }
        @Override protected boolean getDefaultBlocksBuilding() { return true; }

        @Override
        public Map<String, Object> serialize() {
            Map<String, Object> data = new HashMap<>();
            data.put("type", "happy_ghast");
            super.serialize(data);
            if (!this.hardCollision) data.put("hard-collision", false);
            if (this.scale != 0.25f) data.put("scale", this.scale);
            return data;
        }
    }

    public static class CustomHitbox extends BaseHitbox {
        private float scale = 5;
        private String entityType = "slime";

        public void setScale(float scale) { this.scale = scale; }
        public void setEntityType(@NotNull String entityType) { this.entityType = entityType; }

        @Override
        public Map<String, Object> serialize() {
            Map<String, Object> data = new HashMap<>();
            data.put("type", "custom");
            super.serialize(data);
            if (this.scale != 5) data.put("scale", this.scale);
            if (!this.entityType.equals("slime")) data.put("entity-type", this.entityType);
            return data;
        }
    }
}