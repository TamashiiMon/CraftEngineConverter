package fr.robie.craftengineconverter.listener;

import fr.robie.craftengineconverter.CraftEngineConverter;
import fr.robie.craftengineconverter.common.BlockStatesMapper;
import fr.robie.craftengineconverter.common.CraftEnginePlacementTracker;
import fr.robie.craftengineconverter.common.converter.WorldConverter;
import fr.robie.craftengineconverter.common.enums.Plugins;
import fr.robie.craftengineconverter.common.logger.Logger;
import fr.robie.craftengineconverter.common.manager.FoliaCompatibilityManager;
import fr.robie.craftengineconverter.common.progress.BukkitProgressBar;
import fr.robie.craftengineconverter.common.records.ChunkPosition;
import net.momirealms.craftengine.bukkit.api.CraftEngineBlocks;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class WorldConverterManager implements Listener {
    private final Set<WorldConverter> converters = new HashSet<>();
    private final Set<ChunkPosition> processedChunks = new HashSet<>();
    private final CraftEnginePlacementTracker placementTracker;
    private final FoliaCompatibilityManager foliaCompatibilityManager;
    private final List<CompletableFuture<Void>> conversionTasks = new ArrayList<>();

    public WorldConverterManager(CraftEngineConverter plugin) {
        this.placementTracker = plugin.getPlacementTracker();
        this.foliaCompatibilityManager = plugin.getFoliaCompatibilityManager();
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event){
        Chunk chunk = event.getChunk();
        processChunk(chunk, null);
    }

    private void processChunk(@NotNull Chunk chunk, @Nullable BukkitProgressBar progressBar) {
        int x = chunk.getX();
        int z = chunk.getZ();
        String worldName = chunk.getWorld().getName();
        ChunkPosition position = new ChunkPosition(worldName, x, z);
        if (processedChunks.contains(position) || !chunk.isLoaded()) {
            if (progressBar != null) {
                progressBar.increment();
            }
            return;
        }
        processedChunks.add(position);

        @NotNull Entity[] entities = chunk.getEntities();
        for (Entity entity : entities) {
            if (entity instanceof ItemDisplay itemDisplay){
                for (WorldConverter converter : this.converters){
                    if (converter.applyItemDisplayConversion(position, itemDisplay)){
                        break;
                    }
                }
            }
        }

        record BlockData(Location location, org.bukkit.block.data.BlockData blockData) {}
        record BlockConversion(Location location, String ceEquivalent) {}

        List<BlockData> blocksToCheck = new ArrayList<>();
        World world = chunk.getWorld();
        int minHeight = world.getMinHeight();
        int maxHeight = world.getMaxHeight();

        for (int cx = 0; cx < 16; cx++){
            for (int cy = minHeight; cy < maxHeight; cy++){
                for (int cz = 0; cz < 16; cz++){
                    Block block = chunk.getBlock(cx, cy, cz);
                    if (CraftEngineBlocks.isCustomBlock(block)) continue;

                    blocksToCheck.add(new BlockData(
                        block.getLocation().clone(),
                        block.getBlockData().clone()
                    ));
                }
            }
        }

        if (blocksToCheck.isEmpty()) {
            if (progressBar != null) {
                progressBar.increment();
            }
            return;
        }

        CompletableFuture<Void> completableFuture = this.foliaCompatibilityManager.runAsyncComplatable(() -> {
            List<BlockConversion> conversions = new ArrayList<>();
            BlockStatesMapper blockStatesMapper = BlockStatesMapper.getInstance();

            for (BlockData blockData : blocksToCheck) {
                for (var worldConverter : this.converters){
                    Plugins plugin = worldConverter.getPlugin();
                    String ceEquivalent = blockStatesMapper.getCeEquivalent(plugin, blockData.blockData());
                    if (ceEquivalent != null) {
                        conversions.add(new BlockConversion(blockData.location(), ceEquivalent));
                        break;
                    }
                }
            }

            if (!conversions.isEmpty()) {
                final int BATCH_SIZE = 50;

                for (int i = 0; i < conversions.size(); i += BATCH_SIZE) {
                    final int end = Math.min(i + BATCH_SIZE, conversions.size());
                    final List<BlockConversion> batch = conversions.subList(i, end);
                    final long tickDelay = i / BATCH_SIZE;

                    this.foliaCompatibilityManager.runLater(() -> {
                        for (BlockConversion conversion : batch) {
                            try {
                                this.placementTracker.placeBlock(conversion.ceEquivalent(), conversion.location());
                            } catch (Exception e) {
                                Logger.showException("error placing converted block at " + conversion.location(), e);
                            }
                        }
                    }, tickDelay);
                }
            }
        });

        this.conversionTasks.add(completableFuture);
        completableFuture.whenComplete((res, ex) -> {
            this.conversionTasks.remove(completableFuture);
            if (progressBar != null) {
                progressBar.increment();
            }
            if (ex != null) {
                Logger.showException("[WorldConverterManager] Error during chunk conversion at " + position, ex);
            }
        });
    }

    /**
     * Execute chunk conversion with custom throttling.
     *
     * @param chunksPerTick Number of chunks to process per tick (recommended: 5-20)
     * @param progressBar
     */
    public void executeChunckWithThrottling(int chunksPerTick, BukkitProgressBar progressBar) {
        List<World> worlds = Bukkit.getServer().getWorlds();
        List<Chunk> allChunks = new ArrayList<>();

        for (var world : worlds){
            allChunks.addAll(List.of(world.getLoadedChunks()));
        }

        for (int i = 0; i < allChunks.size(); i += chunksPerTick) {
            final int batchEnd = Math.min(i + chunksPerTick, allChunks.size());
            final List<Chunk> batch = allChunks.subList(i, batchEnd);

            long tickDelay = (i / chunksPerTick);

            this.foliaCompatibilityManager.runLater(() -> {
                for (Chunk chunk : batch) {
                    processChunk(chunk, progressBar);
                }
            }, tickDelay);
        }
    }

    public void registerConverter(WorldConverter converter){
        this.converters.add(converter);
    }

    public void clearProcessedChunks() {
        this.processedChunks.clear();
    }

    /**
     * Get the number of processed chunks
     */
    public int getProcessedChunksCount() {
        return this.processedChunks.size();
    }

    /**
     * Get conversion statistics
     */
    public CraftEnginePlacementTracker getPlacementTracker() {
        return this.placementTracker;
    }

    /**
     * Wait for all conversion tasks to complete
     * @return CompletableFuture that completes when all tasks are done
     */
    public CompletableFuture<Void> awaitAllConversions() {
        if (this.conversionTasks.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }

        CompletableFuture<?>[] tasksArray = this.conversionTasks.toArray(new CompletableFuture[0]);

        return CompletableFuture.allOf(tasksArray)
                .exceptionally(ex -> {
                    Logger.showException("Some conversion tasks failed", ex);
                    return null;
                });
    }

    /**
     * Get the number of pending conversion tasks
     */
    public int getPendingTasksCount() {
        return this.conversionTasks.size();
    }

    /**
     * Cancel all pending conversion tasks
     */
    public void cancelAllConversions() {
        for (CompletableFuture<Void> task : this.conversionTasks) {
            task.cancel(true);
        }
        this.conversionTasks.clear();
    }
}
