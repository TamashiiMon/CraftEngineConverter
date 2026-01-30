package fr.robie.craftengineconverter.command;

import fr.robie.craftengineconverter.CraftEngineConverter;
import fr.robie.craftengineconverter.api.BlockHistory;
import fr.robie.craftengineconverter.api.database.StorageManager;
import fr.robie.craftengineconverter.common.builder.TimerBuilder;
import fr.robie.craftengineconverter.common.format.Message;
import fr.robie.craftengineconverter.common.logger.Logger;
import fr.robie.craftengineconverter.common.permission.Permission;
import fr.robie.craftengineconverter.utils.command.CommandType;
import fr.robie.craftengineconverter.utils.command.VCommand;
import net.momirealms.craftengine.bukkit.api.CraftEngineBlocks;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class CraftEngineConverterCommandWorldConverterRestore extends VCommand {

    public CraftEngineConverterCommandWorldConverterRestore(CraftEngineConverter plugin) {
        super(plugin);
        this.setPermission(Permission.COMMAND_WORLDCONVERTER_RESTORE);
        this.setDescription(Message.COMMAND__WORLD_CONVERTER__RESTORE__DESCRIPTION);
        this.addSubCommand("restore");
        this.addFlag("--confirm");
    }

    @Override
    protected CommandType perform(CraftEngineConverter plugin) {
        StorageManager dataBaseManager = this.plugin.getStorageManager();

        if (!dataBaseManager.isEnabled()) {
            message(plugin, sender, Message.COMMAND__WORLD_CONVERTER__RESTORE__DATABASE_DISABLED);
            return CommandType.SUCCESS;
        }

        boolean confirm = this.containFlag("--confirm");

        long activeConversions = dataBaseManager.getActiveConversions();

        if (activeConversions == 0) {
            message(plugin, sender, Message.COMMAND__WORLD_CONVERTER__RESTORE__ALL__CONFIRM,
                    "blocks", 0);
            return CommandType.SUCCESS;
        }

        if (!confirm) {
            message(plugin, sender, Message.COMMAND__WORLD_CONVERTER__RESTORE__ALL__CONFIRM,
                    "blocks", activeConversions);
            return CommandType.SUCCESS;
        }

        message(plugin, sender, Message.COMMAND__WORLD_CONVERTER__RESTORE__ALL__START,
                "blocks", activeConversions);

        long startTime = System.currentTimeMillis();
        AtomicInteger restoredCount = new AtomicInteger(0);
        AtomicInteger totalCount = new AtomicInteger(0);

        // Process all worlds
        for (World world : Bukkit.getWorlds()) {
            String worldName = world.getName();

            // Get all chunks in the world
            for (org.bukkit.Chunk chunk : world.getLoadedChunks()) {
                int chunkX = chunk.getX();
                int chunkZ = chunk.getZ();

                // Get all block history for this chunk
                List<BlockHistory> chunkHistory = dataBaseManager.getChunkHistory(worldName, chunkX, chunkZ);

                for (BlockHistory history : chunkHistory) {
                    totalCount.incrementAndGet();

                    Location location = new Location(
                            world,
                            history.block_x(),
                            history.block_y(),
                            history.block_z()
                    );

                    try {
                        restoreBlock(location, history);
                        dataBaseManager.markBlockAsReverted(worldName, history.block_x(), history.block_y(), history.block_z());
                        restoredCount.incrementAndGet();
                    } catch (Exception e) {
                        Logger.showException("Failed to restore block at " + location, e);
                    }
                }
            }
        }

        long endTime = System.currentTimeMillis();

        message(plugin, sender, Message.COMMAND__WORLD_CONVERTER__RESTORE__ALL__COMPLETE,
                "restored", restoredCount.get(),
                "total", totalCount.get(),
                "time", TimerBuilder.formatTimeAuto(endTime - startTime));

        return CommandType.SUCCESS;
    }

    /**
     * Restores a block to its original state.
     *
     * @param location The location of the block
     * @param history The block history containing the original block data
     */
    private void restoreBlock(Location location, BlockHistory history) {
        Block block = location.getBlock();

        // Remove CraftEngine block if present
        if (CraftEngineBlocks.isCustomBlock(block)) {
            // Set to air first to clear the CraftEngine block
            CraftEngineBlocks.remove(block);

        }

        // Parse and restore the original block data
        try {
            org.bukkit.block.data.BlockData blockData = Bukkit.createBlockData(history.original_block());
            block.setBlockData(blockData, false);
        } catch (Exception e) {
            Logger.showException("Failed to parse block data: " + history.original_block(), e);
            // Fallback: try to extract just the material name
            try {
                String materialName = history.original_block().split("\\[")[0];
                Material material = Material.matchMaterial(materialName);
                if (material != null) {
                    block.setType(material, false);
                }
            } catch (Exception ex) {
                Logger.showException("Failed to restore block, setting to AIR", ex);
                block.setType(Material.AIR, false);
            }
        }
    }
}
