package fr.robie.craftengineconverter.api;

import org.jetbrains.annotations.NotNull;

/**
 * Represents the history of a block conversion.
 *
 * @param id Unique identifier (auto-increment)
 * @param world_name Name of the world where the block is located
 * @param chunk_x X coordinate of the chunk
 * @param chunk_z Z coordinate of the chunk
 * @param block_x X coordinate of the block
 * @param block_y Y coordinate of the block
 * @param block_z Z coordinate of the block
 * @param original_block Original block type/data before conversion
 * @param converted_block CraftEngine block ID after conversion
 * @param reverted Whether the block was reverted to its original appearance
 */
public record BlockHistory(
        int id,
        @NotNull String world_name,
        int chunk_x,
        int chunk_z,
        int block_x,
        int block_y,
        int block_z,
        @NotNull String original_block,
        @NotNull String converted_block,
        boolean reverted
) {
    public BlockHistory(
            @NotNull String world_name,
            int chunk_x,
            int chunk_z,
            int block_x,
            int block_y,
            int block_z,
            @NotNull String original_block,
            @NotNull String converted_block,
            boolean reverted
    ) {
        this(0, world_name, chunk_x, chunk_z, block_x, block_y, block_z,
                original_block, converted_block, reverted);
    }
}
