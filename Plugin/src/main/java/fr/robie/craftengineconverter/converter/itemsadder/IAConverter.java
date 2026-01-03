package fr.robie.craftengineconverter.converter.itemsadder;

import fr.robie.craftengineconverter.CraftEngineConverter;
import fr.robie.craftengineconverter.common.PluginNameMapper;
import fr.robie.craftengineconverter.common.configuration.Configuration;
import fr.robie.craftengineconverter.common.enums.ArmorConverter;
import fr.robie.craftengineconverter.common.enums.ConverterOptions;
import fr.robie.craftengineconverter.common.enums.Plugins;
import fr.robie.craftengineconverter.common.logger.LogType;
import fr.robie.craftengineconverter.common.logger.Logger;
import fr.robie.craftengineconverter.common.progress.BukkitProgressBar;
import fr.robie.craftengineconverter.common.records.ImageConversion;
import fr.robie.craftengineconverter.common.utils.CraftEngineImageUtils;
import fr.robie.craftengineconverter.converter.Converter;
import fr.robie.craftengineconverter.utils.ConfigFile;
import fr.robie.craftengineconverter.utils.SnakeUtils;
import fr.robie.craftengineconverter.utils.enums.ia.IARecipesTypes;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

public class IAConverter extends Converter {
    public IAConverter(CraftEngineConverter plugin) {
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
                        section,
                        namespace
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
        ConfigurationSection equipments = config.getConfigurationSection("equipments");
        if (isNotNull(equipments)){
            for (String equipmentId : equipments.getKeys(false)){
                ConfigurationSection equipmentSection = equipments.getConfigurationSection(equipmentId);
                if (isNull(equipmentSection)) continue;
                String type = equipmentSection.getString("type","");
                if (!type.equalsIgnoreCase("armor")) continue;
                String layer1 = equipmentSection.getString("layer_1");
                String layer2 = equipmentSection.getString("layer_2");
                if (!isValidString(layer1) || !isValidString(layer2)) continue;
                layer1 = cleanPath(layer1);
                layer2 = cleanPath(layer2);
                List<ArmorConverter> convertersToProcess = Configuration.armorConverterType.getComposition();
                Map<ArmorConverter, ConfigurationSection> converterSections = ArmorConverter.createArmorConverterSections(getOrCreateSection(convertedConfig, "equipments"), namespaced(equipmentId, namespace));
                String layer1FileName = namespace+"_"+equipmentId+"_"+getFileName(layer1);
                String layer2FileName = namespace+"_"+equipmentId+"_"+getFileName(layer2);
                addPackMapping(namespace, "textures/"+layer1+".png", namespace, "textures/entity/equipment/humanoid/",layer1FileName+".png");
                addPackMapping(namespace, "textures/"+layer2+".png", namespace, "textures/entity/equipment/humanoid_leggings/",layer2FileName+".png");
                for (ArmorConverter converter : convertersToProcess){
                    ConfigurationSection section = converterSections.get(converter);
                    if (isNotNull(section)){
                        String layer1Texture = converter.getTexturePath(namespace, "humanoid",layer1FileName);
                        String layer2Texture = converter.getTexturePath(namespace, "humanoid_leggings",layer2FileName);
                        ArmorConverter.addEquipmentTextures(section, "humanoid", Set.of(layer1Texture));
                        ArmorConverter.addEquipmentTextures(section, "humanoid_leggings", Set.of(layer2Texture));
                    }
                }
            }
        }
        generateCategorie(itemsIds, convertedConfig, finalFileName);
        if (this.settings.dryRunEnabled()) return;
        saveConvertedConfig(convertedConfig, configFile, itemFile, outputFolder, "items","item");
    }

    private String getFileName(@NotNull String path){
        int lastIndexOf = path.lastIndexOf("/");
        if (lastIndexOf == -1) return path;
        return path.substring(lastIndexOf + 1);
    }

    @Override
    public CompletableFuture<Void> convertEmojis(boolean async, Optional<Player> player) {
        return null;
    }

    @Override
    public CompletableFuture<Void> convertImages(boolean async, Optional<Player> player) {
        return executeTask(async, () -> convertImagesSync(player));
    }

    private void convertImagesSync(Optional<Player> optionalPlayer){
        File inputFolder = new File("plugins/"+this.converterName+"/contents");
        File outputBase = new File(this.plugin.getDataFolder(), "converted/" + converterName + "/CraftEngine/resources/craftengineconverter/configuration/images");
        if (!inputFolder.exists() || !inputFolder.isDirectory()) {
            Logger.debug("ItemsAdder contents folder not found: " + inputFolder.getAbsolutePath());
            return;
        }

        Queue<ConfigFile> toConvert = new LinkedList<>();
        int totalFontImage = populateQueueIA(inputFolder, inputFolder, toConvert, "font_images");

        if (toConvert.isEmpty()) {
            Logger.debug("No ItemsAdder font images found to convert");
            return;
        }

        BukkitProgressBar progressBar = createProgressBar(optionalPlayer, totalFontImage,
                "Converting ItemsAdder font images", "images", ConverterOptions.IMAGES);
        progressBar.start();

        try {
            while (!toConvert.isEmpty()) {
                ConfigFile configFile = toConvert.poll();
                convertFontImageFile(configFile, outputBase, progressBar);
            }
        } catch (Exception e) {
            Logger.showException("An error occurred during ItemsAdder font image conversion", e);
        } finally {
            progressBar.stop();
        }
    }

    private void convertFontImageFile(ConfigFile configFile, File outputBase, BukkitProgressBar progressBar) {
        File sourceFile = configFile.sourceFile();
        YamlConfiguration config = configFile.config();
        ConfigurationSection fontImagesSection = config.getConfigurationSection("font_images");
        if (isNull(fontImagesSection)) return;
        YamlConfiguration convertedConfig = new YamlConfiguration();
        ConfigurationSection ceImagesSection = convertedConfig.createSection("images");

        String finalFileName = sourceFile.getName().replace(".yml","");
        String namespace = config.getString("info.namespace", finalFileName);

        int convertedImages = 0;

        for (String imageId : fontImagesSection.getKeys(false)){
            ConfigurationSection imageSection = fontImagesSection.getConfigurationSection(imageId);
            if (isNull(imageSection)){
                progressBar.increment();
                continue;
            }

            String finalImageId = namespace + ":" + imageId;
            ConfigurationSection ceImageSection = ceImagesSection.createSection(finalImageId);
            String path = imageSection.getString("path");
            if (isValidString(path)){
                ceImageSection.set("file", namespaced(path, namespace));
            }

            int scaleRatio = imageSection.getInt("scale_ratio", 0);
            int yPosition = imageSection.getInt("y_position", 0);

            if (scaleRatio != 0){
                ceImageSection.set("height", scaleRatio);
            }
            if (yPosition != 0){
                ceImageSection.set("ascent", yPosition);
            }

            CraftEngineImageUtils.register(imageId, new ImageConversion(finalImageId, 0,0));
            convertedImages++;
            progressBar.increment();
        }
        if (this.settings.dryRunEnabled()) return;
        if (convertedImages > 0){
            saveConvertedConfig(convertedConfig, configFile, sourceFile, outputBase, "images","image");
        }
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
        return executeTask(async, () -> convertSoundsSync(player));
    }

    private void convertSoundsSync(Optional<Player> optionalPlayer){
        File inputFolder = new File("plugins/"+this.converterName+"/contents");
        File outputFolder = new File(this.plugin.getDataFolder(), "converted/"+converterName+"/CraftEngine/resources/craftengineconverter/configuration/sounds");
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

        Queue<ConfigFile> toConvert = new LinkedList<>();
        int totalSounds = populateQueueIA(inputFolder, inputFolder, toConvert, "sounds");

        if (toConvert.isEmpty()) {
            return;
        }

        BukkitProgressBar progressBar = createProgressBar(optionalPlayer, totalSounds, "Converting ItemsAdder sounds", "sounds", ConverterOptions.SOUNDS);
        progressBar.start();

        try {
            while (!toConvert.isEmpty()) {
                ConfigFile configFile = toConvert.poll();
                convertSoundsFile(configFile, outputFolder, progressBar);
            }
        } catch (Exception e) {
            Logger.showException("An error occurred during ItemsAdder sound conversion", e);
        } finally {
            progressBar.stop();
        }
    }

    private void convertSoundsFile(ConfigFile configFile, File outputFolder, BukkitProgressBar progressBar) {
        String fileName = configFile.sourceFile().getName();
        File soundFile = configFile.sourceFile();
        YamlConfiguration config = configFile.config();

        YamlConfiguration convertedConfig = new YamlConfiguration();
        String finalFileName = fileName.replace(".yml","");
        String namespace = config.getString("info.namespace", finalFileName);
        ConfigurationSection sounds = convertedConfig.createSection("sounds");
        ConfigurationSection originalSounds = config.getConfigurationSection("sounds");
        if (isNull(originalSounds)) {
            Logger.debug("[ItemsAdderConverter] No 'sounds' section found in: " + fileName);
            return;
        }

        for (String soundId : originalSounds.getKeys(false)){
            ConfigurationSection section = originalSounds.getConfigurationSection(soundId);
            if (isNull(section)){
                Logger.debug("[ItemsAdderConverter] Skipped sound (no section): " + soundId + " in file: " + fileName);
                progressBar.increment();
                continue;
            }
            String finalSoundId = namespace + ":" + soundId;
            try {
                ConfigurationSection ceSoundSection = sounds.createSection(finalSoundId);
                String path = section.getString("path");
                if (isValidString(path)){
                    ceSoundSection.set("sounds", List.of(cleanPath(path)));
                }
                String subtitle = section.getString("settings.subtitle");
                if (isValidString(subtitle)){
                    ceSoundSection.set("subtitle", subtitle);
                }
                ConfigurationSection jukeboxSection = section.getConfigurationSection("jukebox");
                if (isNotNull(jukeboxSection)){
                    ConfigurationSection ceJukeBoxSection = getOrCreateSection(convertedConfig, "jukebox-songs");
                    ConfigurationSection ceJukeBoxSoundSection = ceJukeBoxSection.createSection(finalSoundId);
                    ceJukeBoxSoundSection.set("sound", finalSoundId);
                    String description = jukeboxSection.getString("description");
                    if (isValidString(description)){
                        ceJukeBoxSoundSection.set("description", description);
                    }
                }
            } catch (Exception e) {
                Logger.showException("Failed to convert ItemsAdder sound: " + soundId + " in file: " + fileName, e);
            }
            progressBar.increment();
        }
        if (this.settings.dryRunEnabled()) return;
        saveConvertedConfig(convertedConfig, configFile, soundFile, outputFolder, "sounds","sound");
    }

    @Override
    public CompletableFuture<Void> convertRecipes(boolean async, Optional<Player> player) {
        return executeTask(async, () -> convertRecipesSync(player));
    }

    private void convertRecipesSync(Optional<Player> optionalPlayer){
        File inputFolder = new File("plugins/"+this.converterName+"/contents");
        File outputFolder = new File(this.plugin.getDataFolder(), "converted/"+converterName+"/CraftEngine/resources/craftengineconverter/configuration/recipes");
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

        Queue<ConfigFile> toConvert = new LinkedList<>();
        populateQueueIA(inputFolder, inputFolder, toConvert, "recipes");
        if (toConvert.isEmpty()) {
            return;
        }

        int totalRecipes = 0;
        for (ConfigFile configFile : toConvert) {
            YamlConfiguration config = configFile.config();
            ConfigurationSection recipesSection = config.getConfigurationSection("recipes");
            if (isNull(recipesSection)) continue;
            for (String craftingType : recipesSection.getKeys(false)){
                ConfigurationSection craftingSection = recipesSection.getConfigurationSection(craftingType);
                if (isNull(craftingSection)) continue;
                totalRecipes += craftingSection.getKeys(false).size();
            }
        }

        BukkitProgressBar progressBar = createProgressBar(optionalPlayer, totalRecipes, "Converting ItemsAdder recipes", "recipes", ConverterOptions.RECIPES);
        progressBar.start();

        try {
            while (!toConvert.isEmpty()) {
                ConfigFile configFile = toConvert.poll();
                convertRecipesFile(configFile, outputFolder, progressBar);
            }
        } catch (Exception e) {
            Logger.showException("An error occurred during ItemsAdder recipe conversion", e);
        } finally {
            progressBar.stop();
        }
    }

    private void convertRecipesFile(ConfigFile configFile, File outputFolder, BukkitProgressBar progressBar) {
        String fileName = configFile.sourceFile().getName();
        File recipeFile = configFile.sourceFile();
        YamlConfiguration config = configFile.config();

        YamlConfiguration convertedConfig = new YamlConfiguration();
        String finalFileName = fileName.replace(".yml","");
        String namespace = config.getString("info.namespace", finalFileName);
        ConfigurationSection recipes = convertedConfig.createSection("recipes");
        ConfigurationSection originalRecipes = config.getConfigurationSection("recipes");

        if (isNull(originalRecipes)) {
            Logger.debug("[ItemsAdderConverter] No 'recipes' section found in: " + fileName);
            return;
        }

        for (String craftingType : originalRecipes.getKeys(false)){
            ConfigurationSection craftingSection = originalRecipes.getConfigurationSection(craftingType);
            if (isNull(craftingSection)) continue;

            for (String recipeId : craftingSection.getKeys(false)){
                IARecipesTypes iaRecipesType;
                try {
                    iaRecipesType = IARecipesTypes.valueOf(craftingType.toUpperCase());
                } catch (IllegalArgumentException e) {
                    Logger.debug("[ItemsAdderConverter] Skipped recipe (unknown type): " + craftingType + " for recipe: " + recipeId + " in file: " + fileName);
                    progressBar.increment();
                    continue;
                }

                ConfigurationSection recipeSection = craftingSection.getConfigurationSection(recipeId);
                if (isNull(recipeSection)){
                    Logger.debug("[ItemsAdderConverter] Skipped recipe (no section): " + recipeId + " in file: " + fileName);
                    progressBar.increment();
                    continue;
                }

                String baseRecipeId = namespace + ":" + recipeId;
                try {
                    convertRecipe(iaRecipesType, recipeSection, recipes, baseRecipeId, recipeId, fileName);
                } catch (Exception e) {
                    Logger.showException("Failed to convert ItemsAdder recipe: " + recipeId + " in file: " + fileName, e);
                }
                progressBar.increment();
            }
        }

        if (this.settings.dryRunEnabled()) return;
        saveConvertedConfig(convertedConfig, configFile, recipeFile, outputFolder, "recipes","recipe");
    }

    private void convertRecipe(IARecipesTypes type, ConfigurationSection iaRecipe,
                               ConfigurationSection recipesSection, String baseRecipeId,
                               String recipeId, String fileName) {
        switch (type) {
            case CRAFTING_TABLE -> {
                ConfigurationSection ceRecipe = recipesSection.createSection(baseRecipeId);
                convertCraftingTableRecipe(iaRecipe, ceRecipe, recipeId, fileName);
            }
            case COOKING -> convertCookingRecipes(iaRecipe, recipesSection, baseRecipeId, recipeId, fileName);
            case ANVIL_REPAIR -> //TODO: Implement Anvil Repair conversion
                Logger.debug("[ItemsAdderConverter] Anvil Repair recipe conversion not implemented yet for recipe: " + recipeId);
            case SMITHING -> {
                ConfigurationSection ceRecipe = recipesSection.createSection(baseRecipeId);
                convertSmithingRecipe(iaRecipe, ceRecipe, recipeId, fileName);
            }
            default -> Logger.debug("[ItemsAdderConverter] Unsupported recipe type: " + type + " for recipe: " + recipeId);
        }
    }

    private void convertCraftingTableRecipe(ConfigurationSection iaRecipe, ConfigurationSection ceRecipe,
                                            String recipeId, String fileName) {
        boolean shapeless = iaRecipe.getBoolean("shapeless", false);

        if (shapeless) {
            ceRecipe.set("type", "shapeless");

            ConfigurationSection ingredients = iaRecipe.getConfigurationSection("ingredients");
            if (isNotNull(ingredients)) {
                List<String> ceIngredients = new ArrayList<>();
                for (String key : ingredients.getKeys(false)) {
                    String ingredientName = ingredients.getString(key);
                    String convertedIngredient = convertItemReference(ingredientName, recipeId, fileName);
                    if (isValidString(convertedIngredient)) {
                        ceIngredients.add(convertedIngredient);
                    }
                }
                ceRecipe.set("ingredients", ceIngredients);
            }
        } else {
            ceRecipe.set("type", "shaped");
            ceRecipe.set("pattern", iaRecipe.getStringList("pattern"));

            ConfigurationSection ingredients = iaRecipe.getConfigurationSection("ingredients");
            if (isNotNull(ingredients)) {
                Map<String, String> ceIngredients = new HashMap<>();
                for (String key : ingredients.getKeys(false)) {
                    String ingredientName = ingredients.getString(key);
                    String convertedIngredient = convertItemReference(ingredientName, recipeId, fileName);
                    if (isValidString(convertedIngredient)) {
                        ceIngredients.put(key, convertedIngredient);
                    }
                }
                ceRecipe.set("ingredients", ceIngredients);
            }
        }

        convertRecipeResult(iaRecipe, ceRecipe, recipeId, fileName);
    }

    private void convertCookingRecipes(ConfigurationSection iaRecipe, ConfigurationSection recipesSection,
                                       String baseRecipeId, String recipeId, String fileName) {
        List<String> machines = iaRecipe.getStringList("machines");

        if (machines.isEmpty()) {
            machines = List.of("FURNACE");
        }

        for (int i = 0; i < machines.size(); i++) {
            String machine = machines.get(i);
            String cookingType = getCookingTypeFromMachine(machine);

            if (cookingType == null) {
                Logger.debug("[ItemsAdderConverter] Unknown machine type: " + machine + " for recipe: " + recipeId);
                continue;
            }

            String finalRecipeId = machines.size() > 1 ? baseRecipeId + "_" + (i + 1) : baseRecipeId;

            ConfigurationSection ceRecipe = recipesSection.createSection(finalRecipeId);
            convertSingleCookingRecipe(iaRecipe, ceRecipe, cookingType, recipeId, fileName);
        }
    }

    private void convertSingleCookingRecipe(ConfigurationSection iaRecipe, ConfigurationSection ceRecipe,
                                            String cookingType, String recipeId, String fileName) {
        ceRecipe.set("type", cookingType);

        ConfigurationSection ingredientSection = iaRecipe.getConfigurationSection("ingredient");
        if (isNotNull(ingredientSection)) {
            String ingredientItem = ingredientSection.getString("item");
            String convertedIngredient = convertItemReference(ingredientItem, recipeId, fileName);
            if (isValidString(convertedIngredient)) {
                ceRecipe.set("ingredient", convertedIngredient);
            }
        }

        double exp = iaRecipe.getDouble("exp", 0.0);
        if (exp > 0) {
            ceRecipe.set("experience", exp);
        }

        int cookTime = iaRecipe.getInt("cook_time", 200);
        ceRecipe.set("time", cookTime);

        ConfigurationSection resultSection = iaRecipe.getConfigurationSection("result");
        if (isNotNull(resultSection)) {
            ConfigurationSection ceResultSection = ceRecipe.createSection("result");

            String resultItem = resultSection.getString("item");
            String convertedResult = convertItemReference(resultItem, recipeId, fileName);
            if (isValidString(convertedResult)) {
                ceResultSection.set("id", convertedResult);
            }

            int amount = resultSection.getInt("amount", 1);
            ceResultSection.set("count", amount);
        }

        ceRecipe.set("category", "misc");
    }

    private void convertSmithingRecipe(ConfigurationSection iaRecipe, ConfigurationSection ceRecipe,
                                       String recipeId, String fileName) {
        ceRecipe.set("type", "smithing_transform");

        String template = iaRecipe.getString("template");
        if (isValidString(template)) {
            String convertedTemplate = convertItemReference(template, recipeId, fileName);
            if (isValidString(convertedTemplate)) {
                ceRecipe.set("template-type", convertedTemplate);
            }
        }

        String base = iaRecipe.getString("base");
        if (isValidString(base)) {
            String convertedBase = convertItemReference(base, recipeId, fileName);
            if (isValidString(convertedBase)) {
                ceRecipe.set("base", convertedBase);
            }
        } else {
            Logger.debug("[ItemsAdderConverter] Missing required 'base' for smithing recipe: " + recipeId + " in file: " + fileName);
        }

        String addition = iaRecipe.getString("addition");
        if (isValidString(addition)) {
            String convertedAddition = convertItemReference(addition, recipeId, fileName);
            if (isValidString(convertedAddition)) {
                ceRecipe.set("addition", convertedAddition);
            }
        }

        ConfigurationSection resultSection = iaRecipe.getConfigurationSection("result");
        if (isNotNull(resultSection)) {
            ConfigurationSection ceResultSection = ceRecipe.createSection("result");

            String resultItem = resultSection.getString("item");
            String convertedResult = convertItemReference(resultItem, recipeId, fileName);
            if (isValidString(convertedResult)) {
                ceResultSection.set("id", convertedResult);
            }

            int amount = resultSection.getInt("amount", 1);
            ceResultSection.set("count", amount);
        }
    }

    private String getCookingTypeFromMachine(String machine) {
        return switch (machine.toUpperCase()) {
            case "FURNACE" -> "smelting";
            case "BLAST_FURNACE" -> "blasting";
            case "SMOKER" -> "smoking";
            case "CAMPFIRE" -> "campfire_cooking";
            default -> null;
        };
    }

    private String convertItemReference(String itemReference, String recipeId, String fileName) {
        if (!isValidString(itemReference)) {
            return null;
        }

        try {
            Material material = Material.valueOf(itemReference.toUpperCase());
            return "minecraft:" + material.name().toLowerCase();
        } catch (IllegalArgumentException ignored) {
        }

        if (itemReference.startsWith("itemsadder:")) {
            String iaItemId = itemReference.replace("itemsadder:", "");
            String mappedId = PluginNameMapper.getInstance().getNewName(Plugins.ITEMS_ADDER, iaItemId);
            if (isValidString(mappedId)) {
                return mappedId;
            } else {
                Logger.debug("[ItemsAdderConverter] Unknown ItemsAdder item: " + itemReference + " for recipe: " + recipeId + " in file: " + fileName);
                return null;
            }
        }

        String mappedId = PluginNameMapper.getInstance().getNewName(Plugins.ITEMS_ADDER, itemReference);
        if (isValidString(mappedId)) {
            return mappedId;
        }

        Logger.debug("[ItemsAdderConverter] Could not convert item reference: " + itemReference + " for recipe: " + recipeId + " in file: " + fileName);
        return itemReference;
    }

    private void convertRecipeResult(ConfigurationSection iaRecipe, ConfigurationSection ceRecipe,
                                     String recipeId, String fileName) {
        ConfigurationSection resultSection = iaRecipe.getConfigurationSection("result");
        if (isNotNull(resultSection)) {
            ConfigurationSection ceResultSection = ceRecipe.createSection("result");

            String resultItem = resultSection.getString("item");
            String convertedResult = convertItemReference(resultItem, recipeId, fileName);
            if (isValidString(convertedResult)) {
                ceResultSection.set("id", convertedResult);
            }

            int amount = resultSection.getInt("amount", 1);
            if (amount > 1) {
                ceResultSection.set("amount", amount);
            }
        }
    }

    @Override
    public CompletableFuture<Void> convertPack(boolean async, Optional<Player> player) {
        return executeTask(async, () -> convertPackSync(player));
    }

    private void convertPackSync(Optional<Player> optionalPlayer){
        ExecutorService executor = null;
        try {
            File inputFolder = new File("plugins/"+this.converterName+"/contents");
            File outputPackFile = new File(this.plugin.getDataFolder(), "converted/"+converterName+"/CraftEngine/resources/craftengineconverter/resourcepack");

            if (!inputFolder.exists() || !inputFolder.isDirectory()) {
                Logger.debug("ItemsAdder contents folder not found: " + inputFolder.getAbsolutePath());
                return;
            }

            if (outputPackFile.exists()){
                deleteDirectory(outputPackFile);
            }

            if (!outputPackFile.mkdirs()) {
                Logger.debug("Failed to create output folder: " + outputPackFile.getAbsolutePath(), LogType.ERROR);
                return;
            }

            int totalFiles = 0;
            List<String> blacklistedNamespacesList = List.of(".vscode", "_iainternal");
            List<String> validMinecraftFolders = List.of("atlases","blockstates","equipment","font","items","lang","models","particles","post_effect","shaders","texts","textures","waypoint_style");
            File[] listed = inputFolder.listFiles();
            if (isNull(listed)) return;
            for (File f : listed){
                if (f.isDirectory() && !blacklistedNamespacesList.contains(f.getName().toLowerCase())){
                    File[] listedFiles = f.listFiles();
                    if (isNull(listedFiles)) continue;
                    for (File subFile : listedFiles){
                        if (subFile.isDirectory() && validMinecraftFolders.contains(subFile.getName().toLowerCase())){
                            totalFiles += countFilesInDirectory(subFile);
                        } else if (subFile.getName().equalsIgnoreCase("resourcepack")){
                            File assetsDir = new File(subFile, "assets");
                            totalFiles += countFilesInDirectory(assetsDir);
                        }
                    }
                }
            }
            BukkitProgressBar progressBar = createProgressBar(optionalPlayer, totalFiles,
                    "Converting ItemsAdder resource pack", "pack", ConverterOptions.PACKS);

            progressBar.start();

            int threadCount = Math.max(1, this.getSettings().threadCount());
            boolean useMultiThread = threadCount > 1;

            if (useMultiThread) {
                executor = Executors.newFixedThreadPool(threadCount);
            }
            CountDownLatch latch = new CountDownLatch(1);
            AtomicReference<Exception> errorRef = new AtomicReference<>();

            try {
                File outputAssetsFolder = new File(outputPackFile, "assets");
                File contentsFolder = listed.length > 0 ? listed[0].getParentFile() : null;

                for (File namespaceDir : listed){
                    if (namespaceDir.isDirectory() && !blacklistedNamespacesList.contains(namespaceDir.getName().toLowerCase())){
                        File[] namespaceFiles = namespaceDir.listFiles();
                        if (isNull(namespaceFiles)) continue;
                        for (File f : namespaceFiles){
                            String folderName = f.getName().toLowerCase();
                            if (f.isDirectory() && validMinecraftFolders.contains(folderName)){
                                File assetsRoot = contentsFolder != null ? contentsFolder : namespaceDir.getParentFile();
                                copyDirectory(f, outputAssetsFolder, assetsRoot, progressBar, executor, latch, errorRef, useMultiThread);
                            } else if (folderName.equals("resourcepack")){
                                File assetsDir = new File(f, "assets");
                                copyAssetsFolder(assetsDir, outputAssetsFolder, folderName, progressBar, executor, latch, errorRef,useMultiThread);
                            }
                        }
                    }
                }

                if (useMultiThread){
                    latch.countDown();
                    executor.shutdown();
                    if (!executor.awaitTermination(1, TimeUnit.HOURS)) {
                        Logger.debug("Timeout while waiting for resource pack conversion tasks to finish", LogType.ERROR);
                    }
                }

                if (errorRef.get() != null) {
                    throw errorRef.get();
                }

            } finally {
                progressBar.stop();
                if (executor != null && !executor.isShutdown()){
                    executor.shutdown();
                }
            }


        } catch (Exception e) {
            Logger.showException("An error occurred during ItemsAdder pack conversion", e);
        } finally {
            if (isNotNull(executor) && !executor.isShutdown()){
                executor.shutdown();
            }
        }
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
