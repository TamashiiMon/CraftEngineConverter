package fr.robie.craftengineconverter.api.database;

import fr.robie.craftengineconverter.api.BlockHistory;

import java.util.Optional;

public interface StorageManager {

    void loadDatabase();

    void insertBlockHistory(BlockHistory blockHistory);

    void markBlockAsReverted(String worldName, int blockX, int blockY, int blockZ);

    Optional<BlockHistory> getBlockHistory(String worldName, int blockX, int blockY, int blockZ);

    boolean isBlockConverted(String worldName, int blockX, int blockY, int blockZ);

    java.util.List<BlockHistory> getChunkHistory(String worldName, int chunkX, int chunkZ);

    long getTotalConversions();

    long getActiveConversions();

    boolean isEnabled();
}
