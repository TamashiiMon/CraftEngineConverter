package fr.robie.craftengineconverter.database;

import fr.maxlego08.sarah.*;
import fr.maxlego08.sarah.database.DatabaseType;
import fr.maxlego08.sarah.logger.JULogger;
import fr.maxlego08.sarah.logger.Logger;
import fr.robie.craftengineconverter.api.BlockHistory;
import fr.robie.craftengineconverter.api.database.StorageManager;
import fr.robie.craftengineconverter.api.database.StorageType;
import fr.robie.craftengineconverter.common.CraftEngineConverterPlugin;
import fr.robie.craftengineconverter.common.logger.LogType;
import fr.robie.craftengineconverter.database.migrations.WorldBlockConverterHistorical;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.Optional;

public class DataBaseManager implements StorageManager {
    private final CraftEngineConverterPlugin plugin;
    private boolean isEnabled = true;
    private RequestHelper requestHelper;

    public DataBaseManager(CraftEngineConverterPlugin plugin) {
        this.plugin = plugin;
    }


    @Override
    public void loadDatabase() {

        MigrationManager.setMigrationTableName("cec_migrations");
        MigrationManager.registerMigration(new WorldBlockConverterHistorical());

        File file = new File(this.plugin.getDataFolder(), "database-config.yml");
        if (!file.exists()) {
            this.plugin.saveResource("database-config.yml", false);
        }
        Optional<YamlConfiguration> optionalDatabaseConfiguration = this.plugin.getFileCache().getConfiguration(file.toPath());
        if (optionalDatabaseConfiguration.isEmpty()) {
            fr.robie.craftengineconverter.common.logger.Logger.info("Cannot load database configuration file.", LogType.WARNING);
            this.isEnabled = false;
            return;
        }
        YamlConfiguration databaseConfiguration = optionalDatabaseConfiguration.get();

        String user = databaseConfiguration.getString("user", "homestead");
        String password = databaseConfiguration.getString("password", "secret");
        String host = databaseConfiguration.getString("host", "192.168.10.10");
        String dataBase = databaseConfiguration.getString("database", "homestead");
        String prefix = databaseConfiguration.getString("table-prefix", "cec_");
        int port = databaseConfiguration.getInt("port", 3306);
        boolean enableDebug = databaseConfiguration.getBoolean("debug", false);

        String storageType = databaseConfiguration.getString("storage-type", "SQLITE");
        StorageType type = StorageType.SQLITE;
        try {
            type = StorageType.valueOf(storageType.toUpperCase());
        } catch (IllegalArgumentException ignored) {
        }
        Logger logger = JULogger.from(this.plugin.getLogger());
        DatabaseConnection databaseConnection;
        switch (type){
            case MYSQL -> {
                databaseConnection = new MySqlConnection(new DatabaseConfiguration(prefix, user, password, port, host, dataBase, enableDebug, DatabaseType.MYSQL),logger);
            }
            case MARIADB -> {
                databaseConnection = new MariaDbConnection(new DatabaseConfiguration(prefix, user, password, port, host, dataBase, enableDebug, DatabaseType.MARIADB), logger);
            }
            case SQLITE -> {
                databaseConnection = new SqliteConnection(new DatabaseConfiguration(prefix, user, password, port, host, dataBase, enableDebug, DatabaseType.SQLITE), this.plugin.getDataFolder(), logger);
            }
            default -> {
                fr.robie.craftengineconverter.common.logger.Logger.info("You are not using a database.");
                this.isEnabled = false;
                return;
            }
        }

        this.requestHelper = new RequestHelper(databaseConnection, logger);

        if (!databaseConnection.isValid()) {
            fr.robie.craftengineconverter.common.logger.Logger.info("Unable to connect to database !", LogType.ERROR);
            this.isEnabled = false;
            return;
        } else {
            fr.robie.craftengineconverter.common.logger.Logger.info("The database connection is valid ! ("+ (type == StorageType.SQLITE ? "SQLITE" : databaseConnection.getDatabaseConfiguration().getHost()) +")");
        }

        MigrationManager.setDatabaseConfiguration(databaseConnection.getDatabaseConfiguration());
        MigrationManager.execute(databaseConnection, logger);
    }

    @Override
    public void insertBlockHistory(BlockHistory blockHistory){
        if (!this.isEnabled) return;
        this.requestHelper.insert("world_block_converter_historical", schema -> {
            schema.autoIncrement("id");
            schema.string("world_name", blockHistory.world_name());
            schema.bigInt("chunk_x", blockHistory.chunk_x());
            schema.bigInt("chunk_z", blockHistory.chunk_z());
            schema.bigInt("block_x", blockHistory.block_x());
            schema.bigInt("block_y", blockHistory.block_y());
            schema.bigInt("block_z", blockHistory.block_z());
            schema.string("original_block", blockHistory.original_block());
            schema.string("converted_block", blockHistory.converted_block());
            schema.bool("reverted", blockHistory.reverted());
        });
    }

    /**
     * Marks a block as reverted in the history.
     *
     * @param worldName The world name
     * @param blockX The block X coordinate
     * @param blockY The block Y coordinate
     * @param blockZ The block Z coordinate
     */
    @Override
    public void markBlockAsReverted(String worldName, int blockX, int blockY, int blockZ) {
        if (!this.isEnabled) return;
        
        this.requestHelper.update("world_block_converter_historical", schema -> {
            schema.bool("reverted", true);
            schema.where("world_name", worldName);
            schema.where("block_x", blockX);
            schema.where("block_y", blockY);
            schema.where("block_z", blockZ);
            schema.where("reverted", false);
        });
    }

    /**
     * Gets the history of a specific block.
     *
     * @param worldName The world name
     * @param blockX The block X coordinate
     * @param blockY The block Y coordinate
     * @param blockZ The block Z coordinate
     * @return Optional containing the most recent BlockHistory if found
     */
    @Override
    public Optional<BlockHistory> getBlockHistory(String worldName, int blockX, int blockY, int blockZ) {
        if (!this.isEnabled) return Optional.empty();
        
        var results = this.requestHelper.select("world_block_converter_historical", BlockHistory.class, table -> {
            table.where("world_name", worldName);
            table.where("block_x", blockX);
            table.where("block_y", blockY);
            table.where("block_z", blockZ);
            table.orderByDesc("created_at");
        });
        
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    /**
     * Checks if a block has been converted but not reverted.
     *
     * @param worldName The world name
     * @param blockX The block X coordinate
     * @param blockY The block Y coordinate
     * @param blockZ The block Z coordinate
     * @return true if the block is converted and not reverted
     */
    @Override
    public boolean isBlockConverted(String worldName, int blockX, int blockY, int blockZ) {
        if (!this.isEnabled) return false;
        
        var results = this.requestHelper.select("world_block_converter_historical", BlockHistory.class, table -> {
            table.where("world_name", worldName);
            table.where("block_x", blockX);
            table.where("block_y", blockY);
            table.where("block_z", blockZ);
            table.where("reverted", false);
            table.orderByDesc("created_at");
        });
        
        return !results.isEmpty();
    }

    /**
     * Gets all non-reverted block conversions for a specific chunk.
     *
     * @param worldName The world name
     * @param chunkX The chunk X coordinate
     * @param chunkZ The chunk Z coordinate
     * @return List of BlockHistory records for the chunk
     */
    @Override
    public java.util.List<BlockHistory> getChunkHistory(String worldName, int chunkX, int chunkZ) {
        if (!this.isEnabled) return java.util.Collections.emptyList();
        
        return this.requestHelper.select("world_block_converter_historical", BlockHistory.class, table -> {
            table.where("world_name", worldName);
            table.where("chunk_x", chunkX);
            table.where("chunk_z", chunkZ);
            table.where("reverted", false);
        });
    }

    /**
     * Counts the total number of converted blocks.
     *
     * @return Total number of converted blocks
     */
    @Override
    public long getTotalConversions() {
        if (!this.isEnabled) return 0;
        
        return this.requestHelper.count("world_block_converter_historical", table -> {});
    }

    /**
     * Counts the number of non-reverted conversions.
     *
     * @return Number of active conversions
     */
    @Override
    public long getActiveConversions() {
        if (!this.isEnabled) return 0;
        
        return this.requestHelper.count("world_block_converter_historical", table -> {
            table.where("reverted", false);
        });
    }

    @Override
    public boolean isEnabled() {
        return this.isEnabled;
    }

    public RequestHelper getRequestHelper() {
        return this.requestHelper;
    }
}
