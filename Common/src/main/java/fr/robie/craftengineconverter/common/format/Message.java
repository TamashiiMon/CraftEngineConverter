package fr.robie.craftengineconverter.common.format;

import org.bukkit.inventory.ItemStack;

import java.util.*;

/**
 * __ = . and _ = - when storing in a file.
 */
public enum Message {
    /**
     * Time format messages.
     */
    TIME__FORMAT__YEAR("%02d %year% %02d %month% %02d %day% %02d %hour% %02d %minute% %02d %second% %02d %millisecond%"),
    TIME__FORMAT__MONTH("%02d %month% %02d %day% %02d %hour% %02d %minute% %02d %second% %02d %millisecond%"),
    TIME__FORMAT__WEEK("%02d %week% %02d %day% %02d %hour% %02d %minute% %02d %second% %02d %millisecond%"),
    TIME__FORMAT__DAY("%02d %day% %02d %hour% %02d %minute% %02d %second% %02d %millisecond%"),
    TIME__FORMAT__HOUR("%02d %hour% %02d %minute% %02d %second% %02d %millisecond%"),
    TIME__FORMAT__MINUTE("%02d %minute% %02d %second% %02d %millisecond%"),
    TIME__FORMAT__SECOND("%02d %second% %02d %millisecond%"),
    TIME__FORMAT__MILLISECOND("%02d %millisecond%"),
    TIME__UNIT__YEAR("year"), TIME__UNIT__YEARS("years"),
    TIME__UNIT__MONTH("month"), TIME__UNIT__MONTHS("months"),
    TIME__UNIT__WEEK("week"), TIME__UNIT__WEEKS("weeks"),
    TIME__UNIT__DAY("day"), TIME__UNIT__DAYS("days"),
    TIME__UNIT__HOUR("hour"), TIME__UNIT__HOURS("hours"),
    TIME__UNIT__MINUTE("minute"), TIME__UNIT__MINUTES("minutes"),
    TIME__UNIT__SECOND("second"), TIME__UNIT__SECONDS("seconds"),
    TIME__UNIT__MILLISECOND("millisecond"), TIME__UNIT__MILLISECONDS("milliseconds"),

    /**
     * Command prefix message.
     */
    COMMAND__PREFIX("&#FFD166C&#FFC863r&#FEBF61a&#FEB65Ef&#FEAD5Bt&#FEA459E&#FD9B56n&#FD9253g&#FD8951i&#FC814En&#FC784Ce&#FC6F49C&#FB6646o&#FB5D44n&#FB5441v&#FB4B3Ee&#FA423Cr&#FA3939t ┃&r "),
    COMMAND__NO_PERMISSION("§cYou do not have permission to run this command."),
    COMMAND__PLAYER_ONLY("§cOnly one player can execute this command."),
    COMMAND__NO_ARGS("§cImpossible to find the command with its arguments."),
    COMMAND__SYNTAX__ERROR("§cYou must execute the command like this§7: §a%syntax%"),
    COMMAND__SYNTAX__HELP("§f%syntax% §7» §7%description%"),

    COMMAND__RELOAD__DESCRIPTION("Reloads the plugin configuration and messages."),
    COMMAND__RELOAD__SUCCESS("§aPlugin configuration and messages reloaded in §c%time%§a."),
    COMMAND__RELOAD__FAILURE("§cAn error occurred while reloading the plugin configuration and messages. Check the console for more details."),

    COMMAND__CONVERTER__DESCRIPTION("Converts items from another plugin to CraftEngine format."),
    COMMAND__CONVERTER__NOT_SUPPORTED("§cThe plugin §e%plugin%§c is not supported for conversion."),
    COMMAND__CONVERTER__START__SINGLE("§aStarting conversion for §e%plugin%§a..."),
    COMMAND__CONVERTER__START__ALL("§aStarting conversion for all supported plugins..."),
    COMMAND__CONVERTER__COMPLETE__SINGLE("§aConversion completed for §e%plugin%§a! In §c%time%§a."),
    COMMAND__CONVERTER__COMPLETE__ALL("§aConversion completed for all plugins! In §c%time%§a."),
    COMMAND__CONVERTER__DRY_RUN_NOTE("§eNote§7: This was a dry run, no changes were applied."),
    COMMAND__CONVERTER__ALREADY_RUNNING("§cA conversion is already running. Please wait for it to complete before starting a new one. Or add --force to force start a new conversion (the previous one will be cancelled)."),
    COMMAND__CONVERTER__FORCE_STOPPING("§eForce flag detected. Stopping all ongoing conversions..."),
    COMMAND__CONVERTER__THREADS__INFO("§aUsing §e%threads%§a threads for conversion."),
    COMMAND__CONVERTER__THREADS__ERROR_TOO_MANY("§cThe number of threads specified exceeds the number of available processors (%max%). Using the maximum available."),

    COMMAND__CLEAR_FILES_CACHE__DESCRIPTION("Clears the file cache used by the plugin. Add --all to clear all cached files else only stale files will be cleared."),
    COMMAND__CLEAR_FILES_CACHE__COMPLETE("§aCleared §e%cleared_files%§a files from the cache in §c%time%§a."),

    COMMAND__WORLD_CONVERTER__DESCRIPTION("Converts world blocks from other plugins to CraftEngine format."),
    COMMAND__WORLD_CONVERTER__START("§aStarting world conversion for §e%chunks%§a chunks..."),
    COMMAND__WORLD_CONVERTER__COMPLETE("§aWorld conversion completed! Processed §e%chunks%§a chunks with §e%blocks%§a blocks converted in §c%time%§a."),
    COMMAND__WORLD_CONVERTER__ALREADY_RUNNING("§cA world conversion is already running. Use --force to cancel the current conversion and start a new one."),
    COMMAND__WORLD_CONVERTER__FORCE_STOPPING("§eForce flag detected. Stopping ongoing world conversion..."),

    COMMAND__WORLD_CONVERTER__CLEAR_CACHED_CHUNKS__DESCRIPTION("Clears the processed chunks cache for world converter."),
    COMMAND__WORLD_CONVERTER__CLEAR_CACHED_CHUNKS__COMPLETE("§aCleared §e%chunks%§a processed chunks from cache in §c%time%§a."),

    COMMAND__WORLD_CONVERTER__RESTORE__DESCRIPTION("Restores converted blocks and entities to their original state."),
    COMMAND__WORLD_CONVERTER__RESTORE__START("§aStarting restoration of §e%count%§a blocks/entities..."),
    COMMAND__WORLD_CONVERTER__RESTORE__SINGLE__SUCCESS("§aRestored block/entity at §e%x%§a, §e%y%§a, §e%z%§a to original state."),
    COMMAND__WORLD_CONVERTER__RESTORE__SINGLE__NOT_FOUND("§cNo conversion history found for block/entity at §e%x%§a, §e%y%§a, §e%z%§a."),
    COMMAND__WORLD_CONVERTER__RESTORE__SINGLE__ALREADY_REVERTED("§eBlock/entity at §e%x%§a, §e%y%§a, §e%z%§a was already reverted."),
    COMMAND__WORLD_CONVERTER__RESTORE__ALL__CONFIRM("§eThis will restore §c%count%§e converted blocks/entities. Use §a--confirm§e to proceed."),
    COMMAND__WORLD_CONVERTER__RESTORE__ALL__START("§aStarting restoration of §e%count%§a blocks/entities..."),
    COMMAND__WORLD_CONVERTER__RESTORE__ALL__COMPLETE("§aRestored §e%restored%§a/§e%total%§a blocks/entities in §c%time%§a."),
    COMMAND__WORLD_CONVERTER__RESTORE__ALL__NONE("§eThere are no converted blocks or entities to restore."),
    COMMAND__WORLD_CONVERTER__RESTORE__DATABASE_DISABLED("§cDatabase is not enabled. Restoration requires database history."),


    MESSAGE__PLUGIN__STARTUP("Enabling plugin ..."),
    MESSAGE__PLUGIN__STARTUP__COMPLETE("Plugin enabled in §c%time%§a!"),
    MESSAGE__PLUGIN__SHUTDOWN("Disabling plugin ..."),
    MESSAGE__PLUGIN__SHUTDOWN__COMPLETE("Plugin disabled in §c%time%§a!"),
    MESSAGES__AUTO_CONVERTER__STARTUP__START("Auto-conversion for supported plugins is starting..."),
    MESSAGES__AUTO_CONVERTER__STARTUP__COMPLETE("Auto-conversion for supported plugins completed! In §c%time%§a."),
    MESSAGES__AUTO_CONVERTER__STARTUP__DISABLED("Auto-conversion is disabled. Use /cec convert to manually convert supported plugins."),

    WARNING__TEMPLATE_MANAGER__MISSING_TEMPLATE("The template §e%template_name%§c is missing! Please report this to the plugin developer."),
    WARNING__TEMPLATE_MANAGER__ERROR_LOADING_TEMPLATE("An error occurred while loading the template §e%template_name%§c! Please report this to the plugin developer."),
    WARNING__TEMPLATE_MANAGER__INVALID_ARGS_NUMBER("Invalid args number for template %template_name%, must be %2. Received %args_length% arguments."),
    WARNING__FILE__DELETE_FAILURE("Failed to delete file §e%file% (%path%)§c!"),
    WARNING__FOLDER__DELETE_FAILURE("Failed to delete folder §e%folder% (%path%)§c!"),

    WARNING__CONVERTER__NEXO__TOOL__NO_BLOCKS_FOUND("No valid blocks found for tool rules in item §e%item%§c. Skipping tool rules conversion."),
    WARNING__CONVERTER__NEXO__EQUIPPABLE__UNKNOWN_SLOT("Unknown equipment slot §e%slot%§c for item §e%item%§c."),
    WARNING__CONVERTER__NEXO__TOOLTIP_STYLE__UNKNOWN_STYLE("Unknown tooltip style §e%style%§c for item §e%item%§c."),
    WARNING__CONVERTER__NEXO__USE_COOLDOWN__INVALID_SECONDS("Invalid use_cooldown seconds value §e%seconds%§c for item §e%item%§c. Defaulting to 1 second."),
    WARNING__CONVERTER__NEXO__ATTACK_RANGE__INVALID_REACH_FORMAT("Invalid reach format §e%reach%§c for item §e%item%§c. Using defaults."),
    WARNING__CONVERTER__NEXO__ATTACK_RANGE__INVALID_REACH_VALUE("Invalid reach value §e%reach%§c for item §e%item%§c. Using defaults."),
    WARNING__CONVERTER__NEXO__SWING_ANIMATION__INVALID_TYPE("Invalid type value for swing_animation in item §e%item%§c. Using default (whack)."),
    WARNING__CONVERTER__NEXO__SWING_ANIMATION__INVALID_DURATION("Invalid duration value §e%duration%§c for swing_animation in item §e%item%§c. Must be positive. Using default (6)."),
    WARNING__CONVERTER__NEXO__MODEL__PROCESS_FAILURE("Failed to process model path for item §e%item%§c. Skipping texture conversion."),
    WARNING__CONVERTER__NEXO__MODEL__NAMESPACE_FAILURE("Failed to namespace model path for item §e%item%§c. Skipping texture conversion."),
    WARNING__CONVERTER__NEXO__MODEL__PARENT_NOT_SUPPORTED("§e%parent%§c parent_model for item §e%item%§c is not supported yet. Skipping texture conversion. Please report to the developer to add support for this parent_model."),
    WARNING__CONVERTER__NEXO__MODEL__GENERATED__MISSING_TEXTURE("No texture path found for item §e%item%§c despite parent_model being §e%parent%§c. Skipping texture conversion."),
    WARNING__CONVERTER__NEXO__MODEL__CUBE_TOP__PROCESS_FAILURE("Failed to process textures for item §e%item%§c. Skipping texture conversion."),
    WARNING__CONVERTER__NEXO__MODEL__CUBE_TOP__MISSING_TEXTURE("Missing side or top texture for item §e%item%§c despite parent_model being 'block/cube_top'. Skipping texture conversion."),
    WARNING__CONVERTER__NEXO__MODEL__BOW__PROCESS_FAILURE("Failed to process bow model paths for item §e%item%§c. Skipping bow model conversion."),
    WARNING__CONVERTER__NEXO__MODEL__CROSSBOW__PROCESS_FAILURE("Failed to process crossbow model paths for item §e%item%§c. Skipping crossbow model conversion."),
    WARNING__CONVERTER__NEXO__CUSTOM_BLOCK__SAPLING_NOT_SUPPORTED("Sapling behavior conversion for custom block item §e%item%§c is not supported yet. Skipping sapling behavior."),
    WARNING__CONVERTER__NEXO__CUSTOM_BLOCK__SAPLING_NATURAL_ONLY("CraftEngine only supports naturally growing saplings. The sapling for custom block item §e%item%§c will grow naturally."),
    WARNING__CONVERTER__NEXO__CUSTOM_BLOCK__BLOCK_DATA_FAILURE("Failed to create BlockData for custom_variation §e%variation%§c for custom block item §e%item%§c. Skipping custom_variation conversion."),
    WARNING__CONVERTER__NEXO__CUSTOM_BLOCK__UNKNOWN_MINIMAL_TYPE("Unknown minimal_type §e%type%§c for custom block item §e%item%§c. Skipping minimal_type conversion."),
    WARNING__CONVERTER__NEXO__CUSTOM_BLOCK__UNKNOWN_BEST_TOOL("Unknown best_tool §e%tool%§c for custom block item §e%item%§c. Skipping best_tool conversion."),

    WARNING__FURNITURE__INVALID_SEAT_FORMAT("§cInvalid seat format for furniture item §e%item%§c, expected 3 comma-separated float values but got §e%value%§c. Defaulting to (0,0,0)."),
    WARNING__FURNITURE__UNKNOWN_DISPLAY_TRANSFORM("§cUnknown display_transform §e%transform%§c for furniture item §e%item%§c, defaulting to NONE."),
    WARNING__FURNITURE__UNKNOWN_TRACKING_ROTATION("§cUnknown tracking_rotation §e%rotation%§c for furniture item §e%item%§c, defaulting to FIXED."),
    WARNING__FURNITURE__INVALID_TRANSLATION_SIZE("§cInvalid translation size for furniture item §e%item%§c, expected 3 values but got §e%size%§c. Defaulting to (0,0,0)."),
    WARNING__FURNITURE__INVALID_SCALE_FORMAT("§cInvalid scale format for furniture item §e%item%§c, expected 3 comma-separated float values but got §e%value%§c. Defaulting to (1,1,1)."),
    WARNING__FURNITURE__INVALID_AMOUNT_FORMAT("§cInvalid amount format §e%amount%§c for furniture item §e%item%§c. Defaulting to 1."),
    WARNING__FURNITURE__INVALID_PROBABILITY_FORMAT("§cInvalid probability format §e%probability%§c for furniture item §e%item%§c. Defaulting to 1.0."),
    WARNING__FURNITURE__CUSTOM_DROP_CONDITIONS_NOT_SUPPORTED("§cCustom drop conditions (minimal_type, best_tool) for furniture item §e%item%§c are not supported yet. Skipping custom drop conditions."),
    WARNING__FURNITURE__FORTUNE_DROP_NO_LOOTS("§eFurniture item §e%item%§e has fortune-based drop enabled but no loots defined. Please define loots to use fortune-based drops."),
    WARNING__FURNITURE__INVALID_SHULKER_ENTRY("§cInvalid shulker entry §e%entry%§c for item §e%item%§c."),
    WARNING__FURNITURE__NON_NUMERIC_SHULKER_VALUES("§cNon-numeric values in shulker §e%entry%§c for item §e%item%§c."),
    WARNING__FURNITURE__INVALID_GHAST_ENTRY("§cInvalid ghast entry §e%entry%§c for item §e%item%§c."),
    WARNING__FURNITURE__NON_NUMERIC_GHAST_VALUES("§cNon-numeric values in ghast §e%entry%§c for item §e%item%§c."),
    WARNING__FURNITURE__INVALID_INTERACTION_ENTRY("§cInvalid interaction entry §e%entry%§c for item §e%item%§c."),
    WARNING__FURNITURE__NON_NUMERIC_INTERACTION_VALUES("§cNon-numeric values in interaction §e%entry%§c for item §e%item%§c."),
    WARNING__FURNITURE__INVALID_BARRIER_ENTRY("§cInvalid barrier entry §e%entry%§c for item §e%item%§c, expected 3 comma-separated values."),
    WARNING__FURNITURE__INVALID_BARRIER_RANGE("§cInvalid range §e%range%§c in barrier entry §e%entry%§c."),
    WARNING__FURNITURE__NON_NUMERIC_BARRIER_RANGE_BOUNDS("§cNon-numeric range bounds §e%range%§c in barrier entry §e%entry%§c."),
    WARNING__FURNITURE__NON_NUMERIC_BARRIER_VALUE("§cNon-numeric value §e%value%§c in barrier entry §e%entry%§c."),


    WARNING__CONVERTER__IA__FURNITURE__UNKNOWN_DISPLAY_TRANSFORM("§cUnknown furniture display transform type §e%transform%§c for item §e%item%§c."),

    WARNING__CONVERTER__IA__ITEMS__NO_SECTION("No 'items' section found in: §e%file%§c"),
    WARNING__CONVERTER__IA__ITEMS__SKIPPED_NO_SECTION("Skipped item (no section): §e%item%§c in file: §e%file%§c"),

    WARNING__CONVERTER__IA__IMAGES__NONE_FOUND("No ItemsAdder font images found to convert"),

    WARNING__CONVERTER__IA__LANGUAGES__NONE_FOUND("No ItemsAdder language files found to convert"),

    WARNING__CONVERTER__IA__SOUNDS__NO_SECTION("No 'sounds' section found in: §e%file%§c"),
    WARNING__CONVERTER__IA__SOUNDS__SKIPPED_NO_SECTION("Skipped sound (no section): §e%sound%§c in file: §e%file%§c"),

    WARNING__CONVERTER__IA__RECIPES__NO_SECTION("No 'recipes' section found in: §e%file%§c"),
    WARNING__CONVERTER__IA__RECIPES__SKIPPED_UNKNOWN_TYPE("Skipped recipe (unknown type): §e%type%§c for recipe: §e%recipe%§c in file: §e%file%§c"),
    WARNING__CONVERTER__IA__RECIPES__SKIPPED_NO_SECTION("Skipped recipe (no section): §e%recipe%§c in file: §e%file%§c"),
    WARNING__CONVERTER__IA__RECIPES__ANVIL_REPAIR_NOT_IMPLEMENTED("Anvil Repair recipe conversion not implemented yet for recipe: §e%recipe%§c"),
    WARNING__CONVERTER__IA__RECIPES__UNSUPPORTED_TYPE("Unsupported recipe type: §e%type%§c for recipe: §e%recipe%§c"),
    WARNING__CONVERTER__IA__RECIPES__UNKNOWN_MACHINE_TYPE("Unknown machine type: §e%machine%§c for recipe: §e%recipe%§c"),
    WARNING__CONVERTER__IA__RECIPES__SMITHING_MISSING_BASE("Missing required 'base' for smithing recipe: §e%recipe%§c in file: §e%file%§c"),
    WARNING__CONVERTER__IA__RECIPES__UNKNOWN_ITEM_REFERENCE("Unknown ItemsAdder item: §e%item%§c for recipe: §e%recipe%§c in file: §e%file%§c"),
    WARNING__CONVERTER__IA__RECIPES__ITEM_REFERENCE_CONVERSION_FAILURE("Could not convert item reference: §e%item%§c for recipe: §e%recipe%§c in file: §e%file%§c"),


    ERROR__CONVERTER__IA__CONTENTS_FOLDER_NOT_FOUND("ItemsAdder contents folder not found: §e%path%§c"),
    ERROR__CONVERTER__IA__OUTPUT_FOLDER_CREATION_FAILED("Failed to create output folder: §e%path%§c"),
    ERROR__CONVERTER__IA__ITEM_CONVERSION_EXCEPTION("An error occurred during ItemsAdder item conversion"),

    ERROR__CONVERTER__IA__ITEMS__CONVERSION_FAILURE("Failed to convert ItemsAdder item: §e%item%§c in file: §e%file%§c"),

    ERROR__CONVERTER__IA__LANGUAGES__COUNT_FAILURE("Failed to count entries in: §e%file%§c"),
    ERROR__CONVERTER__IA__LANGUAGES__CONVERSION_EXCEPTION("An error occurred during ItemsAdder language conversion"),
    ERROR__CONVERTER__IA__LANGUAGES__KEY_CONVERSION_FAILURE("Failed to convert ItemsAdder translation key: §e%key%§c for language: §e%lang%§c in file: §e%file%§c"),
    ERROR__CONVERTER__IA__LANGUAGES__FILE_CONVERSION_FAILURE("Failed to convert ItemsAdder language file: §e%file%§c"),

    ERROR__CONVERTER__IA__IMAGES__CONVERSION_EXCEPTION("An error occurred during ItemsAdder font image conversion"),

    ERROR__CONVERTER__IA__SOUNDS__CONVERSION_FAILURE("Failed to convert ItemsAdder sound: §e%sound%§c in file: §e%file%§c"),
    ERROR__CONVERTER__IA__SOUNDS__CONVERSION_EXCEPTION("An error occurred during ItemsAdder sound conversion"),

    ERROR__CONVERTER__IA__RECIPES__CONVERSION_EXCEPTION("An error occurred during ItemsAdder recipe conversion"),
    ERROR__CONVERTER__IA__RECIPES__CONVERSION_FAILURE("Failed to convert ItemsAdder recipe: §e%recipe%§c in file: §e%file%§c"),

    ERROR__CACHE__NULL_RESULT("Cache returned null for path: §e%path%§c"),
    ERROR__CACHE__EXCEPTION("An error occurred while accessing the cache for path: §e%path%§c. Error: §e%message%§c"),
    ERROR__MKDIR_FAILURE("Failed to create directory §e%directory% (%path%)§c!"),
    ERROR__FILE__COPY_EXCEPTION("An error occurred while copying file §e%file%§c: §e%message%§c"),
    ERROR__FILE__LOAD_FAILURE("Unable to load file '%file%': file not found or invalid YAML format"),
    ERROR__FILE_OPERATIONS__TIMEOUT("Timeout waiting for file operations to complete"),
    ERROR__FILE_OPERATIONS__FORCE_SHUTDOWN("Forcing shutdown of file operation threads"),
    ERROR__JSON__MALFORMED_AUTO_FIXED("Malformed JSON detected in §e%file%§c, auto-fixed."),
    ERROR__JSON__LOAD_FAILURE("Unable to load JSON file '%file%': file not found or invalid JSON format"),

    ERROR__PACK_CONVERSION__EXCEPTION("An error occurred during pack conversion for plugin §e%plugin%§c"),
    ERROR__CONVERTER__INVALID_GLOW_DROP_COLOR("[%converter%] §c'%color%' is not a valid glow drop color for item §e%item%§c. Allowed colors: §e%valid_colors%§c")
    ;

    private List<String> messages;
    private String message;
    private Map<String, Object> titles = new HashMap<>();
    private boolean use = true;
    private MessageType type = MessageType.TCHAT;
    private ItemStack itemStack;

    /**
     * Constructs a new Message with the specified message string.
     *
     * @param message the message string.
     */
    Message(String message) {
        this.message = message;
    }

    /**
     * Constructs a new Message with the specified title, subtitle, and timings.
     *
     * @param title     the title string.
     * @param subTitle  the subtitle string.
     * @param a         the start time in ticks.
     * @param b         the display time in ticks.
     * @param c         the end time in ticks.
     */
    Message(String title, String subTitle, int a, int b, int c) {
        this.titles.put("title", title);
        this.titles.put("subtitle", subTitle);
        this.titles.put("start", a);
        this.titles.put("time", b);
        this.titles.put("end", c);
        this.titles.put("isUse", true);
        this.type = MessageType.TITLE;
    }

    /**
     * Constructs a new Message with multiple message strings.
     *
     * @param message the array of message strings.
     */
    Message(String... message) {
        this.messages = Arrays.asList(message);
    }

    /**
     * Constructs a new Message with a specific type and multiple message strings.
     *
     * @param type    the type of the message.
     * @param message the array of message strings.
     */
    Message(MessageType type, String... message) {
        this.messages = Arrays.asList(message);
        this.type = type;
    }

    /**
     * Constructs a new Message with a specific type and a single message string.
     *
     * @param type    the type of the message.
     * @param message the message string.
     */
    Message(MessageType type, String message) {
        this.message = message;
        this.type = type;
    }

    /**
     * Constructs a new Message with a single message string and a use flag.
     *
     * @param message the message string.
     * @param use     the use flag.
     */
    Message(String message, boolean use) {
        this.message = message;
        this.use = use;
    }

    /**
     * Gets the message string.
     *
     * @return the message string.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Converts the message to a string.
     *
     * @return the message string.
     */
    public String toMsg() {
        return message;
    }



    /**
     * Gets the message string.
     *
     * @return the message string.
     */
    public String msg() {
        return message;
    }

    /**
     * Checks if the message is in use.
     *
     * @return true if the message is in use, false otherwise.
     */
    public boolean isUse() {
        return use;
    }

    /**
     * Gets the list of messages.
     *
     * @return the list of messages.
     */
    public List<String> getMessages() {
        return messages == null ? Collections.singletonList(message) : messages;
    }

    /**
     * Sets the list of messages.
     *
     * @param messages the list of messages.
     */
    public void setMessages(List<String> messages) {
        this.messages = messages;
    }

    /**
     * Checks if the message contains multiple parts.
     *
     * @return true if the message contains multiple parts, false otherwise.
     */
    public boolean isMessage() {
        return messages != null && messages.size() > 1;
    }

    /**
     * Sets the message string.
     *
     * @param message the message string.
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Gets the title string.
     *
     * @return the title string.
     */
    public String getTitle() {
        return (String) titles.get("title");
    }

    /**
     * Gets the map of titles.
     *
     * @return the map of titles.
     */
    public Map<String, Object> getTitles() {
        return titles;
    }

    /**
     * Sets the map of titles and changes the message type to TITLE.
     *
     * @param titles the map of titles.
     */
    public void setTitles(Map<String, Object> titles) {
        this.titles = titles;
        this.type = MessageType.TITLE;
    }

    /**
     * Gets the subtitle string.
     *
     * @return the subtitle string.
     */
    public String getSubTitle() {
        return (String) titles.get("subtitle");
    }

    /**
     * Checks if the message has a title.
     *
     * @return true if the message has a title, false otherwise.
     */
    public boolean isTitle() {
        return titles.containsKey("title");
    }

    /**
     * Gets the start time in ticks.
     *
     * @return the start time in ticks.
     */
    public int getStart() {
        return ((Number) titles.get("start")).intValue();
    }

    /**
     * Gets the end time in ticks.
     *
     * @return the end time in ticks.
     */
    public int getEnd() {
        return ((Number) titles.get("end")).intValue();
    }

    /**
     * Gets the display time in ticks.
     *
     * @return the display time in ticks.
     */
    public int getTime() {
        return ((Number) titles.get("time")).intValue();
    }

    /**
     * Checks if the title is in use.
     *
     * @return true if the title is in use, false otherwise.
     */
    public boolean isUseTitle() {
        return (boolean) titles.getOrDefault("isUse", "true");
    }

    /**
     * Replaces a substring in the message with another string.
     *
     * @param a the substring to replace.
     * @param b the replacement string.
     * @return the modified message string.
     */
    public String replace(String a, String b) {
        return message.replace(a, b);
    }

    /**
     * Gets the type of the message.
     *
     * @return the type of the message.
     */
    public MessageType getType() {
        return type;
    }

    /**
     * Sets the type of the message.
     *
     * @param type the type of the message.
     */
    public void setType(MessageType type) {
        this.type = type;
    }

    /**
     * Gets the item stack associated with the message.
     *
     * @return the item stack.
     */
    public ItemStack getItemStack() {
        return itemStack;
    }

    /**
     * Sets the item stack associated with the message.
     *
     * @param itemStack the item stack.
     */
    public void setItemStack(ItemStack itemStack) {
        this.itemStack = itemStack;
    }
}
