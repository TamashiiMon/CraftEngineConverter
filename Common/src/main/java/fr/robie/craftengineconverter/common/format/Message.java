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

    COMMAND__WORLD_CONVERTER__RESTORE__DESCRIPTION("Restores converted blocks to their original state."),
    COMMAND__WORLD_CONVERTER__RESTORE__START("§aStarting block restoration..."),
    COMMAND__WORLD_CONVERTER__RESTORE__SINGLE__SUCCESS("§aRestored block at §e%x%§a, §e%y%§a, §e%z%§a to original state."),
    COMMAND__WORLD_CONVERTER__RESTORE__SINGLE__NOT_FOUND("§cNo conversion history found for block at §e%x%§a, §e%y%§a, §e%z%§a."),
    COMMAND__WORLD_CONVERTER__RESTORE__SINGLE__ALREADY_REVERTED("§eBlock at §e%x%§a, §e%y%§a, §e%z%§a was already reverted."),
    COMMAND__WORLD_CONVERTER__RESTORE__ALL__CONFIRM("§eThis will restore §c%blocks%§e converted blocks. Use §a--confirm§e to proceed."),
    COMMAND__WORLD_CONVERTER__RESTORE__ALL__START("§aStarting restoration of §e%blocks%§a blocks..."),
    COMMAND__WORLD_CONVERTER__RESTORE__ALL__COMPLETE("§aRestored §e%restored%§a/§e%total%§a blocks in §c%time%§a."),
    COMMAND__WORLD_CONVERTER__RESTORE__DATABASE_DISABLED("§cDatabase is not enabled. Block restoration requires database history."),


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


    ERROR__MKDIR_FAILURE("Failed to create directory §e%directory% (%path%)§c!"),
    ERROR__FILE__COPY_EXCEPTION("An error occurred while copying file §e%file%§c: §e%message%§c"),
    ERROR__FILE__LOAD_FAILURE("Unable to load file '%file%': file not found or invalid YAML format"),
    ERROR__FILE_OPERATIONS__TIMEOUT("Timeout waiting for file operations to complete"),
    ERROR__FILE_OPERATIONS__FORCE_SHUTDOWN("Forcing shutdown of file operation threads"),

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
