package fr.robie.craftengineconverter.converter.itemsadder;

import fr.robie.craftengineconverter.CraftEngineConverter;
import fr.robie.craftengineconverter.common.PluginNameMapper;
import fr.robie.craftengineconverter.common.enums.ConverterOptions;
import fr.robie.craftengineconverter.common.enums.Plugins;
import fr.robie.craftengineconverter.common.logger.LogType;
import fr.robie.craftengineconverter.common.logger.Logger;
import fr.robie.craftengineconverter.common.progress.BukkitProgressBar;
import fr.robie.craftengineconverter.converter.Converter;
import fr.robie.craftengineconverter.utils.ConfigFile;
import fr.robie.craftengineconverter.utils.SnakeUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class ItemsAdderConverter extends Converter {
    public ItemsAdderConverter(CraftEngineConverter plugin) {
        super(plugin, "ItemsAdder");
    }

    @Override
    public CompletableFuture<Void> convertItems(boolean async, Optional<Player> player) {
        return executeTask(async, () -> convertItemsSync(player));
    }

    private void convertItemsSync(Optional<Player> player) {
        File inputFolder = new File("plugins/"+this.converterName+"/contents");
        File outputFolder = new File(this.plugin.getDataFolder(), "converted/"+converterName+"/CraftEngine/resources/craftengineconverter/configuration/items");
        if (!inputFolder.exists() || !inputFolder.isDirectory()) {
            Logger.debug("ItemsAdder contents folder not found: " + inputFolder.getAbsolutePath());
            return;
        }

        if (outputFolder.exists()){
            deleteDirectory(outputFolder);
        }

        if (!outputFolder.mkdirs()) {
            Logger.debug("Failed to create output folder: " + outputFolder.getAbsolutePath(), LogType.ERROR);
            return;
        }

        Queue<@NotNull ConfigFile> toConvert = new LinkedList<>();
        int totalItems = populateQueueIA(inputFolder, inputFolder, toConvert, "items");

        if (toConvert.isEmpty()) {
            return;
        }

        BukkitProgressBar progressBar = createProgressBar(player, totalItems, "Converting ItemsAdder items", "items", ConverterOptions.ITEMS);
        progressBar.start();

        PluginNameMapper.getInstance().clearMappingsForPlugin(Plugins.ITEMS_ADDER);
        
        try {
            processItemFiles(toConvert, outputFolder, progressBar);
        } catch (Exception e) {
            Logger.showException("An error occurred during ItemsAdder item conversion", e);
        } finally {
            progressBar.stop();
        }
    }

    private void processItemFiles(Queue<ConfigFile> toConvert, File outputFolder, BukkitProgressBar progressBar) {
        while (!toConvert.isEmpty()) {
            ConfigFile configFile = toConvert.poll();
            convertItemsFile(configFile, outputFolder, progressBar);
        }
    }

    private void convertItemsFile(ConfigFile configFile, File outputFolder, BukkitProgressBar progressBar) {
        String fileName = configFile.sourceFile().getName();
        File itemFile = configFile.sourceFile();
        YamlConfiguration config = configFile.config();

        YamlConfiguration convertedConfig = new YamlConfiguration();
        String finalFileName = fileName.replace(".yml","");
        String namespace = config.getString("info.namespace", finalFileName);
        ConfigurationSection items = convertedConfig.createSection("items");
        ConfigurationSection originalItems = config.getConfigurationSection("items");
        if (isNull(originalItems)) {
            Logger.debug("[ItemsAdderConverter] No 'items' section found in: " + fileName);
            return;
        }

        List<String> itemsIds = new ArrayList<>();
        for (String itemId : originalItems.getKeys(false)){
            ConfigurationSection section = originalItems.getConfigurationSection(itemId);
            if (isNull(section)){
                Logger.debug("[ItemsAdderConverter] Skipped item (no section): " + itemId + " in file: " + fileName);
                progressBar.increment();
                continue;
            }
            String finalItemId = namespace + ":" + itemId;
            try {
                IAItemsConverter iaItemsConverter = new IAItemsConverter(
                        finalItemId,
                        items.createSection(finalItemId),
                        this,
                        convertedConfig,
                        section
                );
                iaItemsConverter.convertItem();

                if (!iaItemsConverter.isExcludeFromInventory()){
                    itemsIds.add(finalItemId);
                }
                PluginNameMapper.getInstance().storeMapping(Plugins.ITEMS_ADDER, itemId, finalItemId);
            } catch (Exception e) {
                Logger.showException("Failed to convert ItemsAdder item: " + itemId + " in file: " + fileName, e);
            }
            progressBar.increment();
        }
        generateCategorie(itemsIds, convertedConfig, finalFileName);
        if (this.settings.dryRunEnabled()) return;
        saveConvertedConfig(convertedConfig, configFile, itemFile, outputFolder, "items","item");
    }


    @Override
    public CompletableFuture<Void> convertEmojis(boolean async, Optional<Player> player) {
        return null;
    }

    @Override
    public CompletableFuture<Void> convertImages(boolean async, Optional<Player> player) {
        return null;
    }

    @Override
    public CompletableFuture<Void> convertLanguages(boolean async, Optional<Player> player) {
        return executeTask(async, () -> convertLanguagesSync(player));
    }

    protected void convertLanguagesSync(Optional<Player> optionalPlayer){
        File inputFolder = new File("plugins/"+this.converterName+"/contents");
        File outputFolder = new File(this.plugin.getDataFolder(), "converted/"+converterName+"/CraftEngine/resources/craftengineconverter/configuration/languages/languages.yml");

        if (!inputFolder.exists() || !inputFolder.isDirectory()) {
            Logger.debug("ItemsAdder contents folder not found: " + inputFolder.getAbsolutePath());
            return;
        }

        Queue<ConfigFile> toConvert = new LinkedList<>();
        populateQueueIA(inputFolder, inputFolder, toConvert, "minecraft_lang_overwrite");

        if (toConvert.isEmpty()) {
            Logger.debug("No ItemsAdder language files found to convert");
            return;
        }

        int totalEntries = 0;
        for (ConfigFile configFile : toConvert) {
            try (SnakeUtils config = new SnakeUtils(configFile.sourceFile())) {
                SnakeUtils langSection = config.getSection("minecraft_lang_overwrite");
                if (langSection == null) continue;

                for (String translationKey : langSection.getKeys()) {
                    SnakeUtils translationSection = langSection.getSection(translationKey);
                    if (translationSection == null) continue;

                    Map<String, Object> entries = translationSection.getMap("entries");
                    List<String> languages = translationSection.getStringList("languages");

                    if (entries != null) {
                        totalEntries += entries.size() * languages.size();
                    }
                }
            } catch (Exception e) {
                Logger.debug("Failed to count entries in: " + configFile.sourceFile().getName(), LogType.ERROR);
            }
        }

        if (totalEntries == 0) {
            Logger.info("No translations found in ItemsAdder files.");
            return;
        }

        BukkitProgressBar progressBar = createProgressBar(optionalPlayer, totalEntries,
                "Converting ItemsAdder languages", "translations", ConverterOptions.LANGUAGES);
        progressBar.start();

        try {
            File tempOutputFile = File.createTempFile("craftengine_ia_languages", ".yml");
            tempOutputFile.deleteOnExit();

            try (SnakeUtils ceTranslation = SnakeUtils.createEmpty(tempOutputFile)) {
                while (!toConvert.isEmpty()) {
                    ConfigFile configFile = toConvert.poll();
                    convertLanguageFile(configFile, ceTranslation, progressBar);
                }

                if (!this.settings.dryRunEnabled()) {
                    ceTranslation.save(outputFolder);
                }
            }
        } catch (Exception e) {
            Logger.showException("An error occurred during ItemsAdder language conversion", e);
        } finally {
            progressBar.stop();
        }
    }

    private void convertLanguageFile(ConfigFile configFile, SnakeUtils ceTranslation, BukkitProgressBar progressBar){
        try (SnakeUtils toTranslate = new SnakeUtils(configFile.sourceFile())) {
            SnakeUtils minecraftLangOverwrite = toTranslate.getSection("minecraft_lang_overwrite");
            if (minecraftLangOverwrite == null) return;

            for (String translationGroup : minecraftLangOverwrite.getKeys()) {
                SnakeUtils section = minecraftLangOverwrite.getSection(translationGroup);
                if (section == null) continue;

                Map<String, Object> entries = section.getMap("entries");
                List<String> languages = section.getStringList("languages");

                if (entries == null || entries.isEmpty() || languages.isEmpty()) {
                    continue;
                }

                for (String langKey : languages) {
                    String ceLangKey = langKey.equalsIgnoreCase("ALL") ? "en" : langKey.toLowerCase();

                    for (Map.Entry<String, Object> entry : entries.entrySet()) {
                        try {
                            String translationKey = "translations\\n" + ceLangKey + "\\n" + entry.getKey();
                            ceTranslation.addData(translationKey, entry.getValue(), "\\n");
                        } catch (Exception e) {
                            Logger.debug("Failed to convert ItemsAdder translation key: " + entry.getKey()
                                    + " for language: " + ceLangKey + " in file: " + configFile.sourceFile().getName(), LogType.ERROR);
                        }
                        progressBar.increment();
                    }
                }
            }
        } catch (Exception e) {
            Logger.showException("Failed to convert ItemsAdder language file: " + configFile.sourceFile().getName(), e);
        }
    }

    @Override
    public CompletableFuture<Void> convertSounds(boolean async, Optional<Player> player) {
        return null;
    }

    @Override
    public CompletableFuture<Void> convertRecipes(boolean async, Optional<Player> player) {
        return null;
    }

    @Override
    public CompletableFuture<Void> convertPack(boolean async, Optional<Player> player) {
        return null;
    }

    protected int populateQueueIA(File baseDir, File currentDir, Queue<ConfigFile> toConvert, String requiredSectionName) {
        int totalItems = 0;
        File[] listed = currentDir.listFiles();
        if (isNull(listed)) return 0;
        for (File f : listed){
            if (f.isDirectory()) {
                if (f.getName().equals("configs")) {
                    totalItems += addAllYmlFilesRecursively(f, baseDir, toConvert, requiredSectionName);
                }
                totalItems += populateQueueIA(baseDir, f, toConvert, requiredSectionName);
            }
        }
        return totalItems;
    }

    private int addAllYmlFilesRecursively(File dir, File baseDir, Queue<ConfigFile> toConvert, String requiredSectionName) {
        int count = 0;
        File[] files = dir.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) {
                    count += addAllYmlFilesRecursively(f, baseDir, toConvert, requiredSectionName);
                } else if (f.isFile() && f.getName().endsWith(".yml")) {
                    YamlConfiguration config = YamlConfiguration.loadConfiguration(f);
                    ConfigurationSection itemsSection = config.getConfigurationSection(requiredSectionName);
                    if (itemsSection != null) {
                        toConvert.add(new ConfigFile(f, baseDir, config));
                        count += itemsSection.getKeys(false).size();
                    }
                }
            }
        }
        return count;
    }


}
