package fr.robie.craftengineconverter.command;

import fr.robie.craftengineconverter.CraftEngineConverter;
import fr.robie.craftengineconverter.common.builder.TimerBuilder;
import fr.robie.craftengineconverter.common.configuration.ConverterSettings;
import fr.robie.craftengineconverter.common.enums.ConverterOptions;
import fr.robie.craftengineconverter.common.enums.CraftEngineBlockState;
import fr.robie.craftengineconverter.common.format.Message;
import fr.robie.craftengineconverter.common.permission.Permission;
import fr.robie.craftengineconverter.converter.Converter;
import fr.robie.craftengineconverter.utils.command.CommandType;
import fr.robie.craftengineconverter.utils.command.VCommand;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

public class CraftEngineConverterCommandConvert extends VCommand {
    private final List<CompletableFuture<Void>> conversionTasks = new ArrayList<>();

    public CraftEngineConverterCommandConvert(CraftEngineConverter plugin) {
        super(plugin);
        this.setPermission(Permission.COMMAND_CONVERT);
        this.setDescription(Message.COMMAND__CONVERTER__DESCRIPTION);
        this.addSubCommand("convert");
        this.addOptionalArg("plugin",(sender,args)-> this.plugin.getConverterNames());
        this.addOptionalArg("type", (sender,args)-> {
            Set<String> options = new HashSet<>();
            for (ConverterOptions o : ConverterOptions.values()) {
                options.add(o.name());
            }
            return options;
        });
        this.addFlag("--dryrun");
        this.addFlag("--force");
        this.addFlag("--threads", Integer.class, 1);
    }

    @Override
    protected CommandType perform(CraftEngineConverter plugin) {
        boolean forceConversion = this.containFlag("--force");
        if (!this.conversionTasks.isEmpty() && !forceConversion){
            message(plugin,sender, Message.COMMAND__CONVERTER__ALREADY_RUNNING);
            return CommandType.SUCCESS;
        }

        if (forceConversion){
            message(plugin, sender, Message.COMMAND__CONVERTER__FORCE_STOPPING);
            this.disableAllConversions();
        }

        String targetPlugin = this.argAsString(0);
        ConverterOptions converterOption = argAsEnum(1, ConverterOptions.class, ConverterOptions.ALL);
        boolean dryRun = this.containFlag("--dryrun");
        if (dryRun){
            message(plugin,sender, Message.COMMAND__CONVERTER__DRY_RUN_NOTE);
        }
        int threads = this.getFlagValueAsInteger("--threads");
        if (threads < 1){
            threads = 1;
        } else if (threads > Runtime.getRuntime().availableProcessors()){
            int availableProcessors = Runtime.getRuntime().availableProcessors();
            threads = availableProcessors;
            message(plugin,sender, Message.COMMAND__CONVERTER__THREADS__ERROR_TOO_MANY, "max", availableProcessors);
        }
        message(plugin, sender, Message.COMMAND__CONVERTER__THREADS__INFO, "threads", threads);
        CraftEngineBlockState.resetAllLimits();
        if (targetPlugin == null){
            long startTime = System.currentTimeMillis();
            message(plugin,sender, Message.COMMAND__CONVERTER__START__ALL);
            Collection<Converter> converters = plugin.getConverters();
            AtomicInteger counter = new AtomicInteger(converters.size());
            for (Converter converter : converters){
                ConverterSettings converterSettings = converter.getSettings();
                converterSettings.createBackup();
                CompletableFuture<Void> voidCompletableFuture = processConverter(converter, converterOption, Optional.ofNullable(this.player), dryRun, threads);
                voidCompletableFuture.thenRun(() -> {
                    int remaining = counter.decrementAndGet();
                    if (remaining == 0) {
                        long endTime = System.currentTimeMillis();
                        message(plugin,sender, Message.COMMAND__CONVERTER__COMPLETE__ALL, "time", TimerBuilder.formatTimeAuto(endTime-startTime));
                        converterSettings.restoreBackup();
                    }
                    this.conversionTasks.remove(voidCompletableFuture);
                });
                this.conversionTasks.add(voidCompletableFuture);
            }
        } else {
            Optional<Converter> optionalConverter = plugin.getConverter(targetPlugin);
            if (optionalConverter.isPresent()){
                long startTime = System.currentTimeMillis();
                message(plugin,sender, Message.COMMAND__CONVERTER__START__SINGLE, "plugin", targetPlugin);
                Converter converter = optionalConverter.get();
                ConverterSettings converterSettings = converter.getSettings();
                converterSettings.createBackup();
                CompletableFuture<Void> voidCompletableFuture = processConverter(converter, converterOption, Optional.ofNullable(this.player), dryRun, threads);
                voidCompletableFuture.thenRun(() -> {
                    long endTime = System.currentTimeMillis();
                    message(plugin,sender, Message.COMMAND__CONVERTER__COMPLETE__SINGLE, "plugin", targetPlugin, "time", TimerBuilder.formatTimeAuto(endTime-startTime));
                    converterSettings.restoreBackup();
                    this.conversionTasks.remove(voidCompletableFuture);
                });
                this.conversionTasks.add(voidCompletableFuture);
            } else {
                message(plugin,sender, Message.COMMAND__CONVERTER__NOT_SUPPORTED, "plugin", targetPlugin);
            }
        }
        return CommandType.SUCCESS;
    }

    private CompletableFuture<Void> processConverter(Converter converter, ConverterOptions converterOption, Optional<Player> player, boolean dryRun, int threads) {
        ConverterSettings converterSettings = converter.getSettings();
        converterSettings.setDryRunEnabled(dryRun);
        converterSettings.setThreadCount(threads);
        return switch (converterOption){
            case ALL -> converter.convertAll(player);
            case ITEMS -> converter.convertItems(true, player);
            case PACKS -> converter.convertPack(true, player);
            case EMOJIS -> converter.convertEmojis(true, player);
            case IMAGES -> converter.convertImages(true, player);
            case LANGUAGES -> converter.convertLanguages(true, player);
            case RECIPES -> converter.convertRecipes(true, player);
            case SOUNDS -> converter.convertSounds(true, player);
        };
    }

    private void disableAllConversions() {
        if (!this.conversionTasks.isEmpty()) {
            for (CompletableFuture<Void> task : new ArrayList<>(this.conversionTasks)) {
                task.cancel(true);
            }
            this.conversionTasks.clear();
        }
    }

    @Override
    public void onDisable() {
        this.disableAllConversions();
    }
}
