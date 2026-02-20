package fr.robie.craftengineconverter.converter;

import fr.robie.craftengineconverter.CraftEngineConverter;
import fr.robie.craftengineconverter.common.cache.FileCacheEntry;
import fr.robie.craftengineconverter.common.configuration.Configuration;
import fr.robie.craftengineconverter.common.configuration.ConverterSettings;
import fr.robie.craftengineconverter.common.enums.ConverterOptions;
import fr.robie.craftengineconverter.common.enums.Plugins;
import fr.robie.craftengineconverter.common.format.Message;
import fr.robie.craftengineconverter.common.logger.LogType;
import fr.robie.craftengineconverter.common.logger.Logger;
import fr.robie.craftengineconverter.common.manager.FileCacheManager;
import fr.robie.craftengineconverter.common.progress.BukkitProgressBar;
import fr.robie.craftengineconverter.converter.settings.BasicConverterSettings;
import fr.robie.craftengineconverter.utils.ConfigFile;
import fr.robie.craftengineconverter.utils.YamlUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public abstract class Converter extends YamlUtils {
    protected final CraftEngineConverter plugin;
    protected final Plugins pluginType;
    protected final String converterName;
    protected final ConverterSettings settings;
    protected final Map<String, List<PackMapping>> packMappings = new HashMap<>();

    public Converter(CraftEngineConverter plugin, String converterName, Plugins pluginType) {
        super(plugin);
        this.plugin = plugin;
        this.converterName = converterName;
        this.pluginType = pluginType;
        this.settings = new BasicConverterSettings();
    }

    public CompletableFuture<Void> convertAll(Optional<Player> player) {
        return this.plugin.getFoliaCompatibilityManager().runAsyncComplatable(() -> {
            convertItems(false, player);
            convertEmojis(false, player);
            convertImages(false, player);
            convertLanguages(false, player);
            convertSounds(false, player);
            convertRecipes(false, player);
            convertPack(false, player);
        });
    }

    public abstract CompletableFuture<Void> convertItems(boolean async, Optional<Player> player);

    public abstract CompletableFuture<Void> convertEmojis(boolean async, Optional<Player> player);

    public abstract CompletableFuture<Void> convertImages(boolean async, Optional<Player> player);

    public abstract CompletableFuture<Void> convertLanguages(boolean async, Optional<Player> player);

    public abstract CompletableFuture<Void> convertSounds(boolean async, Optional<Player> player);

    public abstract CompletableFuture<Void> convertRecipes(boolean async, Optional<Player> player);

    public abstract CompletableFuture<Void> convertPack(boolean async, Optional<Player> player);

    public String getName() {
        return this.converterName;
    }

    public Plugins getPluginType() {
        return this.pluginType;
    }

    @Contract("-> this")
    public ConverterSettings getSettings() {
        return this.settings;
    }

    protected CompletableFuture<Void> executeTask(boolean async, Runnable task) {
        if (async) {
            return this.plugin.getFoliaCompatibilityManager().runAsyncComplatable(task);
        } else {
            task.run();
            return CompletableFuture.completedFuture(null);
        }
    }

    public void addPackMapping(@NotNull String namespaceSource, @NotNull String originalPath, @NotNull String namespaceTarget, @NotNull String targetPath, @Nullable String newName){
        PackMapping mapping = new PackMapping(namespaceSource, originalPath, namespaceTarget, targetPath, newName);
        this.packMappings.computeIfAbsent(namespaceSource, k -> new ArrayList<>()).add(mapping);
    }

    public void addPackMapping(@NotNull String namespaceSource, @NotNull String originalPath, @NotNull String namespaceTarget, @NotNull String targetPath){
        addPackMapping(namespaceSource, originalPath, namespaceTarget, targetPath, null);
    }

    protected void populateQueue(File baseDir, File currentDir, Queue<ConfigFile> toConvert) {
        File[] files = currentDir.listFiles();
        if (files == null) return;

        for (File itemFile : files) {
            if (itemFile.isDirectory()) {
                populateQueue(baseDir, itemFile, toConvert);
                continue;
            }

            String fileName = itemFile.getName();
            if (!fileName.endsWith(".yml")) {
                continue;
            }


            Optional<FileCacheEntry<YamlConfiguration>> entry = FileCacheManager.getYamlCache().getEntryFile(itemFile.toPath());
            if (entry.isPresent()) {
                FileCacheEntry<YamlConfiguration> fileCacheEntry = entry.get();
                toConvert.add(new ConfigFile(itemFile, baseDir, fileCacheEntry.getData()));
            } else {
                Logger.info(Message.ERROR__FILE__LOAD_FAILURE, LogType.ERROR, "file", fileName);
            }
        }
    }

    protected BukkitProgressBar createProgressBar(Optional<Player> optionalPlayer, int totalSteps, String prefix, String suffix, ConverterOptions options) {
        BukkitProgressBar.Builder builder = new BukkitProgressBar.Builder(totalSteps);
        if (optionalPlayer.isPresent()) {
            builder.player(optionalPlayer.get());
            builder.showBar(false);
        }
        return builder.prefix(prefix).suffix(suffix).options(options).build(this.plugin);
    }

    protected int countFilesInDirectory(File directory) {
        if (!directory.exists() || !directory.isDirectory()) {
            return 0;
        }

        int count = 0;
        File[] files = directory.listFiles();
        if (files == null) return 0;

        for (File file : files) {
            if (file.isDirectory()) {
                count += countFilesInDirectory(file);
            } else if (file.isFile()) {
                count++;
            }
        }

        return count;
    }

    protected void copyAssetsFolder(File assetsFolder, File outputAssetsFolder, String packName,
                                  BukkitProgressBar progress, ExecutorService executor,
                                  CountDownLatch latch, AtomicReference<Exception> errorRef,
                                  boolean useMultiThread) {
        if (!assetsFolder.exists() || !assetsFolder.isDirectory()) {
            Logger.debug("Assets folder not found for pack '" + packName + "' at: " + assetsFolder.getAbsolutePath());
            return;
        }

        try {
            copyDirectory(assetsFolder, outputAssetsFolder, assetsFolder, progress, executor, latch, errorRef, useMultiThread);
        } catch (IOException e) {
            Logger.info("Failed to copy assets from " + packName + " pack: " + e.getMessage(), LogType.ERROR);
            errorRef.compareAndSet(null, e);
        }
    }

    protected void copyDirectory(File source, File destination, File assetsRoot,
                                 BukkitProgressBar progress, ExecutorService executor,
                                 CountDownLatch latch, AtomicReference<Exception> errorRef,
                                 boolean useMultiThread) throws IOException {
        if (!this.settings.dryRunEnabled() && !destination.exists() && !destination.mkdirs()) {
            Logger.debug(Message.ERROR__MKDIR_FAILURE, LogType.ERROR, "directory", destination.getName(), "path", destination.getAbsolutePath());
            return;
        }

        File[] files = source.listFiles();
        if (files == null) return;

        for (File file : files) {
            Path relativePath = assetsRoot.toPath().relativize(file.toPath());
            String relativePathStr = relativePath.toString().replace("\\", "/");

            String[] parts = relativePathStr.split("/", 2);
            String namespace = parts[0];
            String pathInNamespace = parts.length > 1 ? parts[1] : "";

            String fullPath = namespace + ":" + pathInNamespace;

            if (Configuration.isPathBlacklisted(fullPath)) {
                if (file.isFile()) progress.increment();
                continue;
            }

            if (file.isFile()) {
                String fullPathWithFile = namespace + ":" + pathInNamespace + "/" + file.getName();
                if (Configuration.isPathBlacklisted(fullPathWithFile)) {
                    progress.increment();
                    continue;
                }
            }

            List<PackMapping> resolvedMappings = resolveAllPackMappings(namespace, pathInNamespace);

            if (!resolvedMappings.isEmpty()) {
                for (PackMapping resolvedMapping : resolvedMappings) {
                    String mappedFullPath = resolvedMapping.namespaceTarget() + "/" + resolvedMapping.targetPath();

                    File targetFile;
                    if (file.isFile()) {
                        String fileName = resolvedMapping.newName() != null ? resolvedMapping.newName() : file.getName();
                        targetFile = new File(destination, mappedFullPath + "/" + fileName);
                    } else {
                        targetFile = new File(destination, mappedFullPath);
                    }

                    if (file.isDirectory()) {
                        if (!this.settings.dryRunEnabled() && !targetFile.exists() && !targetFile.mkdirs()) {
                            Logger.debug(Message.ERROR__MKDIR_FAILURE, LogType.ERROR, "directory", targetFile.getName(), "path", targetFile.getAbsolutePath());
                        }
                        copyDirectoryContents(file, targetFile, progress, executor, latch, errorRef, useMultiThread);
                    } else {
                        copyFileWithProgress(progress, executor, latch, errorRef, useMultiThread, file, targetFile);
                    }
                }
            } else {
                File targetFile = new File(destination, relativePathStr);
                if (file.isDirectory()) {
                    if (!this.settings.dryRunEnabled() && !targetFile.exists() && !targetFile.mkdirs()) {
                        Logger.debug(Message.ERROR__MKDIR_FAILURE, LogType.ERROR, "directory", targetFile.getName(), "path", targetFile.getAbsolutePath());
                    }
                    copyDirectory(file, destination, assetsRoot, progress, executor, latch, errorRef, useMultiThread);
                } else {
                    copyFileWithProgress(progress, executor, latch, errorRef, useMultiThread, file, targetFile);
                }
            }
        }
    }

    public List<PackMapping> resolveAllPackMappings(@NotNull String namespaceSource, @NotNull String originalPath) {
        List<PackMapping> mappings = this.packMappings.get(namespaceSource);
        if (mappings == null) return Collections.emptyList();

        int bestMatchLength = -1;
        List<PackMapping> bestMatches = new ArrayList<>();

        for (PackMapping mapping : mappings) {
            if (mapping.matches(originalPath)) {
                int matchLength = mapping.originalPath().length();
                if (matchLength > bestMatchLength) {
                    bestMatchLength = matchLength;
                    bestMatches.clear();
                    bestMatches.add(mapping);
                } else if (matchLength == bestMatchLength) {
                    bestMatches.add(mapping);
                }
            }
        }

        return bestMatches.stream()
                .map(m -> new PackMapping(namespaceSource, originalPath, m.namespaceTarget(), m.apply(originalPath), m.newName()))
                .collect(Collectors.toList());
    }

    private void copyDirectoryContents(File source, File destination, BukkitProgressBar progress,
                                       ExecutorService executor, CountDownLatch latch,
                                       AtomicReference<Exception> errorRef, boolean useMultiThread) throws IOException {
        if (!this.settings.dryRunEnabled() && !destination.exists() && !destination.mkdirs()) {
            Logger.debug(Message.ERROR__MKDIR_FAILURE, LogType.ERROR, "directory", destination.getName(), "path", destination.getAbsolutePath());
            return;
        }

        File[] files = source.listFiles();
        if (files == null) return;

        for (File file : files) {
            File targetFile = new File(destination, file.getName());

            if (file.isDirectory()) {
                copyDirectoryContents(file, targetFile, progress, executor, latch, errorRef, useMultiThread);
            } else {
                copyFileWithProgress(progress, executor, latch, errorRef, useMultiThread, file, targetFile);
            }
        }
    }

    private void copyFileWithProgress(BukkitProgressBar progress, ExecutorService executor, CountDownLatch latch, AtomicReference<Exception> errorRef, boolean useMultiThread, File file, File targetFile) throws IOException {
        if (useMultiThread) {
            executor.submit(() -> {
                try {
                    latch.await();
                    if (!this.settings.dryRunEnabled() && !targetFile.getParentFile().exists()
                            && !targetFile.getParentFile().mkdirs()) {
                        Logger.debug(Message.ERROR__MKDIR_FAILURE, LogType.ERROR, "directory", targetFile.getParentFile().getName(), "path", targetFile.getParentFile().getAbsolutePath());
                    }
                    copyFile(file, targetFile);
                    progress.increment();
                } catch (Exception e) {
                    Logger.debug(Message.ERROR__FILE__COPY_EXCEPTION, LogType.ERROR, "file", file.getAbsolutePath(), "message", e.getMessage());
                    errorRef.compareAndSet(null, e);
                }
            });
        } else {
            if (!this.settings.dryRunEnabled() && !targetFile.getParentFile().exists()
                    && !targetFile.getParentFile().mkdirs()) {
                Logger.debug("Failed to create parent directory for file: " + targetFile.getAbsolutePath(), LogType.ERROR);
            }
            copyFile(file, targetFile);
            progress.increment();
        }
    }

    private void copyFile(File source, File destination) throws IOException {
        if (this.settings.dryRunEnabled()) return;
        Files.copy(
                source.toPath(),
                destination.toPath(),
                StandardCopyOption.REPLACE_EXISTING
        );
    }

    protected void generateCategorie(List<String> itemsIds, YamlConfiguration config, String fileName) {
        if (itemsIds.isEmpty()) return;
        ConfigurationSection categoriesSection = config.createSection("categories");
        ConfigurationSection categorySection = categoriesSection.createSection(itemsIds.getFirst());
        categorySection.set("name", (Configuration.disableDefaultItalic? "<!i>":"") + "Category "+fileName);
        categorySection.set("icon", itemsIds.getFirst());
        categorySection.set("list", itemsIds);
    }

    protected void saveConvertedConfig(YamlConfiguration convertedConfig, ConfigFile configFile, File baseFile, File outputFolder, String directoryName, String type) {
        try {
            Path relativePath = configFile.baseDir().toPath().relativize(baseFile.toPath());
            File outputFile = new File(outputFolder, relativePath.toString());

            if (!outputFile.getParentFile().exists()) {
                if (!outputFile.getParentFile().mkdirs()) {
                    Logger.debug(Message.ERROR__MKDIR_FAILURE,LogType.ERROR, "directory", outputFile.getParentFile().getName(),
                            "path", outputFile.getParentFile().getAbsolutePath());
                }
            }

            convertedConfig.save(outputFile);
        } catch (IOException e) {
            Logger.showException("Failed to save converted "+type+" file: " + baseFile.getName(), e);
        }
    }

    protected void deleteDirectory(File directory) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else if (!file.delete()){
                    Logger.debug(Message.WARNING__FILE__DELETE_FAILURE, LogType.ERROR, "file", file.getName(), "path", file.getAbsolutePath());
                }
            }
        }
        if (!directory.delete()){
            Logger.debug(Message.WARNING__FOLDER__DELETE_FAILURE, LogType.ERROR, "folder", directory.getName(), "path", directory.getAbsolutePath());
        }
    }

    public record PackMapping(String namespaceSource, String originalPath, String namespaceTarget, String targetPath, String newName){
        public boolean matches(String path) {
            if (originalPath.contains("*")) {
                String regex = originalPath.replace("*", ".*");
                return path.matches(regex);
            } else {
                return path.equals(originalPath) || path.startsWith(originalPath + "/");
            }
        }

        public String apply(String path) {
            if (originalPath.contains("*")) {
                String regex = originalPath.replace("*", "(.*)");
                String matched = path.replaceFirst(regex, "$1");
                if (targetPath.contains("$1")) {
                    return targetPath.replace("$1", matched);
                } else {
                    return targetPath + "/" + matched;
                }
            } else {
                if (path.equals(originalPath)) {
                    return targetPath;
                } else if (path.startsWith(originalPath + "/")) {
                    String remainder = path.substring(originalPath.length() + 1);
                    return targetPath + "/" + remainder;
                }
            }
            return path;
        }
    }
}
