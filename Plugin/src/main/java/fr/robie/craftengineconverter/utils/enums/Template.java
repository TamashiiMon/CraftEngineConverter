package fr.robie.craftengineconverter.utils.enums;

public enum Template {
    MODEL_CUBE("templates/model/cube", TemplateType.BLOCK),
    MODEL_CUBE_ALL("templates/model/cube_all", TemplateType.BLOCK),
    MODEL_CUBE_TOP("templates/model/cube_top", TemplateType.BLOCK),
    MODEL_CROSS("templates/model/block_cross", TemplateType.BLOCK),

    BLOCK_MODEL("templates/model/block_model", TemplateType.BLOCK),

    MODEL_ITEM_GENERATED("templates/model/item_generated"),

    MODEL_3D_CROSSBOW("templates/model/items/3d_crossbow"),
    MODEL_2D_CROSSBOW("templates/model/items/2d_crossbow"),
    MODEL_2D_CROSSBOW_SIMPLIFIED("templates/model/items/2d_crossbow_simplified"),

    MODEL_3D_FISHING_ROD("templates/model/items/3d_fishing_rod"),
    MODEL_2D_FISHING_ROD("templates/model/items/2d_fishing_rod"),
    MODEL_2D_FISHING_ROD_SIMPLIFIED("templates/model/items/2d_fishing_rod_simplified"),

    MODEL_3D_BOW("templates/model/items/3d_bow"),
    MODEL_2D_BOW("templates/model/items/2d_bow"),
    MODEL_2D_BOW_SIMPLIFIED("templates/model/items/2d_bow_simplified"),

    MODEL_TRIDENT("templates/model/items/trident"),

    MODEL_3D_SHIELD("templates/model/items/3d_shield"),

    MODEL_ITEM_ELYTRA("templates/model/items/elytra"),

    MODEL_ITEM_DEFAULT("templates/model/item_default"),

    SETTINGS_PROJECTILE("templates/settings/projectile", TemplateType.SETTINGS),

    LOOT_TABLE_BASIC_DROP("templates/loot_table/basic_drop", TemplateType.LOOT_TABLE),
    LOOT_TABLE_SILK_TOUCH_ONLY("templates/loot_table/silk_touch_only_drop", TemplateType.LOOT_TABLE),
    LOOT_TABLE_FORTUNE_ONLY("templates/loot_table/fortune_only_drop", TemplateType.LOOT_TABLE),

    MINECRAFT_EFFECT("templates/minecraft/effect",TemplateType.OTHER),

    BLOCK_STATE_LOG_APPEARANCE("templates/block_state/log_appearance", TemplateType.BLOCK_APPEARANCE),
    BLOCK_STATE_4_DIRECTIONS_APPEARANCE("templates/block_state/4_directions_appearance", TemplateType.BLOCK_APPEARANCE),
    BLOCK_STATE_SAPLING_APPEARANCE("templates/block_state/sapling_appearance", TemplateType.BLOCK_APPEARANCE),

    BLOCK_STATE_PROPERTIES_STAGE("templates/block_state/properties/stage", TemplateType.BLOCK_APPEARANCE_PROPERTIES)
    ;
    private final String path;
    private final TemplateType type;

    Template(String path){
        this.path = path;
        this.type = TemplateType.ITEM;
    }

    Template(String path, TemplateType type) {
        this.path = path;
        this.type = type;
    }

    public String getPath() {return this.path;}
    public TemplateType getType() {return this.type;}
}
