package fr.robie.craftengineconverter.loader;

import fr.robie.craftengineconverter.common.CraftEngineConverterPlugin;
import fr.robie.craftengineconverter.common.Manageable;
import fr.robie.craftengineconverter.common.ObjectUtils;
import fr.robie.craftengineconverter.common.configuration.Configuration;
import fr.robie.craftengineconverter.common.enums.Languages;
import fr.robie.craftengineconverter.common.format.Message;
import fr.robie.craftengineconverter.common.format.MessageType;
import fr.robie.craftengineconverter.common.logger.LogType;
import fr.robie.craftengineconverter.common.logger.Logger;
import fr.robie.craftengineconverter.utils.SnakeUtils;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.util.*;

public class MessageLoader extends ObjectUtils implements Manageable {
    private static final String TRANSLATIONS_PATH = "translations/";
    private static final String MESSAGES_FILE = "/messages.yml";
    private static final String VERSION_KEY = "version";

    private final CraftEngineConverterPlugin plugin;
    private final int version = 0;

    public MessageLoader(CraftEngineConverterPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void reload() {
        for (Languages lang : Languages.values()) {
            String path = getLanguagePath(lang);
            File file = new File(this.plugin.getDataFolder(), path);

            try {
                ensureFileExists(file, path);
                updateLanguageFile(file, path, lang);
            } catch (Exception e) {
                Logger.showException("Failed to process language file: " + path, e);
            }
        }
        loadLanguage(Configuration.language);
    }

    private void ensureFileExists(File file, String path) {
        if (!file.exists()) {
            this.plugin.saveResource(path, false);
        }
    }

    private void updateLanguageFile(File file, String path, Languages lang) throws Exception {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        Object fileVersion = config.get(VERSION_KEY);

        if (!(fileVersion instanceof Integer intVersion) || intVersion < this.version) {
            config.set(VERSION_KEY, this.version);
            if (updateMissingKeys(config, path, lang)) {
                config.save(file);
                Logger.info("Language file updated for language: " + lang.name(), LogType.INFO);
            }
        }
    }

    private boolean updateMissingKeys(YamlConfiguration config, String path, Languages lang) throws Exception {
        boolean updated = false;
        try (InputStream inputStream = this.plugin.getResource(path)) {
            if (inputStream == null) {
                Logger.info("Language file not found in resources: " + path);
                return updated;
            }

            try (SnakeUtils reader = new SnakeUtils(inputStream)) {
                for (Message message : Message.values()) {
                    String key = enumNameToKey(message.name());

                    if (!config.contains(key)) {
                        if (reader.contains(key)) {
                            config.set(key,reader.getObject(key));
                        } else {
                            Logger.info("Missing message key for language " + lang.name() + ": " + key+ ". Please report this. Using default message.",LogType.WARNING);
                            MessageType type = message.getType();
                            if (type != MessageType.TCHAT) config.set(key + ".type", type.name());
                            if (type == MessageType.TITLE){
                                config.set(key + ".title", message.getTitle());
                                config.set(key + ".subtitle", message.getSubTitle());
                                config.set(key + ".fade-in", message.getStart());
                                config.set(key + ".show-time", message.getTime());
                                config.set(key + ".fade-out", message.getEnd());
                            } else {
                                if (message.isMessage()) {
                                    config.set(key + (type != MessageType.TCHAT ? ".messages" : ""), message.getMessages());
                                } else {
                                    config.set(key + (type != MessageType.TCHAT ? ".message" : ""), message.getMessage());
                                }
                            }
                        }
                        updated = true;
                    }
                }
            }
        }
        return updated;
    }

    public void loadLanguage(Languages language) {
        File file = new File(this.plugin.getDataFolder(), getLanguagePath(language));

        if (!file.exists()) {
            Logger.info("Language file not found: " + file.getPath(), LogType.WARNING);
            return;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        Set<String> keys = config.getKeys(true);
        List<Message> loadedMessages = new ArrayList<>();

        for (String key : keys) {
            if (VERSION_KEY.equals(key) || config.isConfigurationSection(key)) continue;

            try {
                Message message = parseMessage(key);
                loadMessageContent(message, config, key);
                loadedMessages.add(message);
            } catch (IllegalArgumentException e) {
                Logger.info("Unknown message key: " + key, LogType.WARNING);
            } catch (Exception e) {
                Logger.showException("Failed to load message: " + key, e);
            }
        }

        validateLoadedMessages(loadedMessages, language);
    }

    private Message parseMessage(String key) {
        return Message.valueOf(keyToEnumName(key));
    }

    private void loadMessageContent(Message message, YamlConfiguration config, String key) {
        if (config.contains(key + ".type")) {
            loadTypedMessage(message, config, key);
        } else {
            loadSimpleMessage(message, config, key);
        }
    }

    private void loadTypedMessage(Message message, YamlConfiguration config, String key) {
        MessageType messageType = MessageType.valueOf(
                config.getString(key + ".type", "TCHAT").toUpperCase()
        );
        message.setType(messageType);

        switch (messageType) {
            case ACTION, TCHAT_AND_ACTION -> loadActionMessage(message, config, key);
            case CENTER, TCHAT, WITHOUT_PREFIX -> loadTextMessage(message, config, key);
            case TITLE -> loadTitleMessage(message, config, key);
        }
    }

    private void loadActionMessage(Message message, YamlConfiguration config, String key) {
        message.setMessage(config.getString(key + ".message"));
    }

    private void loadTextMessage(Message message, YamlConfiguration config, String key) {
        List<String> messages = config.getStringList(key + ".messages");
        if (messages.isEmpty()) {
            message.setMessage(config.getString(key + ".message"));
        } else {
            message.setMessages(messages);
        }
    }

    private void loadTitleMessage(Message message, YamlConfiguration config, String key) {
        Map<String, Object> titles = new HashMap<>();
        titles.put("title", config.getString(key + ".title"));
        titles.put("subtitle", config.getString(key + ".subtitle"));
        titles.put("start", config.getInt(key + ".fade-in", 10));
        titles.put("time", config.getInt(key + ".show-time", 70));
        titles.put("end", config.getInt(key + ".fade-out", 20));
        titles.put("isUse", true);
        message.setTitles(titles);
    }

    private void loadSimpleMessage(Message message, YamlConfiguration config, String key) {
        message.setType(MessageType.TCHAT);
        List<String> messages = config.getStringList(key);
        if (messages.isEmpty()) {
            message.setMessage(config.getString(key));
        } else {
            message.setMessages(messages);
        }
    }

    private void validateLoadedMessages(List<Message> loadedMessages, Languages language) {
        if (loadedMessages.size() != Message.values().length) {
            Logger.info(String.format(
                    "Loaded messages (%d) do not match expected count (%d) for language %s",
                    loadedMessages.size(), Message.values().length, language.name()
            ), LogType.WARNING);
        }
    }

    private String getLanguagePath(Languages language) {
        return TRANSLATIONS_PATH + language.name().toLowerCase() + MESSAGES_FILE;
    }

    private String keyToEnumName(String key) {
        return key.toUpperCase().replace(".", "__").replace("-", "_");
    }

    private String enumNameToKey(String enumName) {
        return enumName.toLowerCase().replace("__", ".").replace("_", "-");
    }
}