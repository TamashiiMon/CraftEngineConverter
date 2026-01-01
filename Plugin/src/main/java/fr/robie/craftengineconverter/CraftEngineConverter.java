package fr.robie.craftengineconverter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fr.robie.craftengineconverter.api.packet.PacketLoader;
import fr.robie.craftengineconverter.command.CraftEngineConverterCommand;
import fr.robie.craftengineconverter.common.CraftEngineConverterPlugin;
import fr.robie.craftengineconverter.common.builder.TimerBuilder;
import fr.robie.craftengineconverter.common.configuration.Configuration;
import fr.robie.craftengineconverter.common.enums.Plugins;
import fr.robie.craftengineconverter.common.format.ClassicMeta;
import fr.robie.craftengineconverter.common.format.ComponentMeta;
import fr.robie.craftengineconverter.common.format.Message;
import fr.robie.craftengineconverter.common.format.MessageFormatter;
import fr.robie.craftengineconverter.common.logger.LogType;
import fr.robie.craftengineconverter.common.logger.Logger;
import fr.robie.craftengineconverter.common.manager.FoliaCompatibilityManager;
import fr.robie.craftengineconverter.common.tag.ITagResolver;
import fr.robie.craftengineconverter.converter.Converter;
import fr.robie.craftengineconverter.converter.itemsadder.ItemsAdderConverter;
import fr.robie.craftengineconverter.converter.nexo.NexoConverter;
import fr.robie.craftengineconverter.hooks.nexo.NexoBlockConverter;
import fr.robie.craftengineconverter.hooks.nexo.NexoFurnitureConverter;
import fr.robie.craftengineconverter.hooks.packetevent.PacketEventHook;
import fr.robie.craftengineconverter.loader.MessageLoader;
import fr.robie.craftengineconverter.utils.TagResolver;
import fr.robie.craftengineconverter.utils.command.CommandManager;
import fr.robie.craftengineconverter.utils.manager.InternalTemplateManager;
import org.bstats.bukkit.Metrics;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

public final class CraftEngineConverter extends CraftEngineConverterPlugin {
    private static CraftEngineConverter INSTANCE;

    private static final int BSTAT_PLUGIN_ID = 28612;

    private final Map<String, Converter> converterMap = new HashMap<>();

    private final FoliaCompatibilityManager foliaCompatibilityManager = new FoliaCompatibilityManager(this);
    private final CommandManager commandManager = new CommandManager(this);
    private final Gson gson = getGsonBuilder().create();
    private final InternalTemplateManager templateManager = new InternalTemplateManager(this);
    private final ITagResolver tagResolver = new TagResolver();
    private final MessageLoader messageLoader = new MessageLoader(this);
    private MessageFormatter messageFormatter = new ClassicMeta();
    private Metrics metrics;
    private PacketLoader packetLoader;

    public CraftEngineConverter() {
        new Logger(this.foliaCompatibilityManager.isPaper() ? this.getPluginMeta().getVersion() :this.getDescription().getFullName());
    }

    @Override
    public void onLoad() {
        this.reloadConfig();
        if (Plugins.PACKET_EVENTS.isPresent()){
            Logger.info("[Hook] PacketEvents", LogType.SUCCESS);
            if (Configuration.packetEventsFormatting) {
                this.packetLoader = new PacketEventHook(this);
            }
        }
        if (this.packetLoader != null){
            this.packetLoader.onLoad();
        }
    }

    @Override
    public void onEnable() {
        INSTANCE = this;

        long startTime = System.currentTimeMillis();
        Logger.info(Message.MESSAGE__PLUGIN__STARTUP);
        if (!Plugins.CRAFTENGINE.isPresent()){
            Logger.info("CraftEngine plugin not found ! Disabling CraftEngineConverter ...");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        if (!this.getDataFolder().exists() && !this.getDataFolder().mkdirs()){
            Logger.info("Unable to create plugin folder ! Disabling CraftEngineConverter ...",LogType.ERROR);
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        if (this.foliaCompatibilityManager.isPaper()){
            messageFormatter = new ComponentMeta();
        }

        this.reloadMessages();

        this.templateManager.loadTemplates();

        this.commandManager.registerCommand("craftengineconverter",new CraftEngineConverterCommand(this),"cengineconverter","cec");

        this.commandManager.validCommands();
        registerConverter(new NexoConverter(this));
        registerConverter(new ItemsAdderConverter(this));

        this.tagResolver.initTagProcessors();

        this.metrics = new Metrics(this, BSTAT_PLUGIN_ID);

        if (this.packetLoader != null){
            this.packetLoader.onEnable();
        }

        if (Configuration.autoConvertOnStartup) {
            Logger.info(Message.MESSAGES__AUTO_CONVERTER__STARTUP__START);
            long startTimeAutoConverter = System.currentTimeMillis();
            Collection<Converter> values = this.converterMap.values();
            AtomicInteger counter = new AtomicInteger(values.size());
            for (Converter converter : values) {
                CompletableFuture<Void> voidCompletableFuture = converter.convertAll(Optional.empty());
                voidCompletableFuture.thenAccept(voidCompletableFuture1 -> {
                    int remaining = counter.decrementAndGet();
                    if (remaining == 0) {
                        Logger.info(Message.MESSAGES__AUTO_CONVERTER__STARTUP__COMPLETE, "time", TimerBuilder.formatTimeAuto(System.currentTimeMillis() - startTimeAutoConverter));
                    }
                });
            }
        } else {
            Logger.info(Message.MESSAGES__AUTO_CONVERTER__STARTUP__DISABLED);
        }

        if (Plugins.NEXO.isEnabled() && Configuration.nexoEnableHook){
            this.registerListener(new NexoBlockConverter(this));
            this.registerListener(new NexoFurnitureConverter(this));

        }

        Logger.info(Message.MESSAGE__PLUGIN__STARTUP__COMPLETE, "time", TimerBuilder.formatTimeAuto(System.currentTimeMillis() - startTime));
    }

    @Override
    public void onDisable() {
        long startTime = System.currentTimeMillis();
        Logger.info(Message.MESSAGE__PLUGIN__SHUTDOWN);

        if (this.packetLoader != null){
            this.packetLoader.onDisable();
        }

        this.metrics.shutdown();

        if (this.placementTracker != null){
            Logger.info("Conversion stats :");
            Logger.info("Total blocks converted : " + this.placementTracker.getBlocksConverted() + " (Failed : " + this.placementTracker.getBlocksFailed() + ", Success rate : " + String.format("%.2f", this.placementTracker.getBlocksSuccessRate()) + "%)");
            Logger.info("Total furniture converted : " + this.placementTracker.getFurnitureConverted() + " (Failed : " + this.placementTracker.getFurnitureFailed() + ", Success rate : " + String.format("%.2f", this.placementTracker.getFurnitureSuccessRate()) + "%)");
            Logger.info("Grand total converted : " + this.placementTracker.getTotalConverted() + " (Failed : " + this.placementTracker.getTotalFailed() + ", Overall success rate : " + String.format("%.2f", this.placementTracker.getOverallSuccessRate()) + "%)");
        }

        Logger.info(Message.MESSAGE__PLUGIN__SHUTDOWN__COMPLETE, "time", TimerBuilder.formatTimeAuto(System.currentTimeMillis() - startTime));
    }

    public void reloadMessages(){
        this.messageLoader.reload();
    }

    private void registerListener(@NotNull Listener listener){
        this.getServer().getPluginManager().registerEvents(listener,this);
    }

    public CommandManager getCommandManager() {
        return this.commandManager;
    }

    @Override
    public MessageFormatter getMessageFormatter() {
        return this.messageFormatter;
    }

    @Override
    public ITagResolver getTagResolver() {
        return this.tagResolver;
    }

    @Override
    public FoliaCompatibilityManager getFoliaCompatibilityManager() {
        return foliaCompatibilityManager;
    }

    public void registerConverter(Converter converter) {
        this.converterMap.put(converter.getName().toLowerCase(), converter);
    }

    public Optional<Converter> getConverter(String name) {
        return Optional.ofNullable(this.converterMap.get(name.toLowerCase()));
    }

    public Set<String> getConverterNames() {
        return this.converterMap.keySet();
    }

    public Collection<Converter> getConverters() {
        return Collections.unmodifiableCollection(this.converterMap.values());
    }

    public Gson getGson() {
        return this.gson;
    }

    private GsonBuilder getGsonBuilder() {
        return new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().serializeNulls()
                .excludeFieldsWithModifiers(Modifier.TRANSIENT, Modifier.VOLATILE);
    }

    public void reloadConfig(){
        this.saveDefaultConfig();
        File configFile = new File(this.getDataFolder(), "config.yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        Configuration.getInstance().load(config, configFile);
    }

    public static CraftEngineConverter getInstance() {
        return INSTANCE;
    }
}
