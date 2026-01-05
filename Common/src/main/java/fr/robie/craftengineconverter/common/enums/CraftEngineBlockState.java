package fr.robie.craftengineconverter.common.enums;

import fr.robie.craftengineconverter.common.configuration.Configuration;
import org.bukkit.block.BlockType;

import java.util.*;

public enum CraftEngineBlockState {
    /*
     * POOL HIERARCHY:
     *
     * SOLID (1338 slots) - Top-level pool
     *   ├─ MUSHROOM (189 slots) - Mushroom block pool
     *   │   ├─ RED_MUSHROOM_BLOCK (63 slots)
     *   │   ├─ BROWN_MUSHROOM_BLOCK (63 slots)
     *   │   └─ MUSHROOM_STEM (63 slots)
     *   └─ NOTE_BLOCK (1149 slots)
     *
     * TRIPWIRE (126 slots) - Tripwire pool
     *   ├─ LOWER_TRIPWIRE (63 slots)
     *   └─ HIGHER_TRIPWIRE (63 slots)
     *
     * TINTABLE_LEAVES (65 slots)
     *   └─ WATERLOGGED_TINTABLE_LEAVES (65 slots)
     *
     * NON_TINTABLE_LEAVES (234 slots)
     *   └─ WATERLOGGED_NON_TINTABLE_LEAVES (234 slots)
     *
     * LEAVES (143 slots)
     *   └─ WATERLOGGED_LEAVES (143 slots)
     *
     * Independent blocks (no shared pool):
     *   - CACTUS (15), SUGAR_CANE (15)
     *   - WEEPING_VINE (50), TWISTING_VINE (50), CAVE_VINE (100)
     *   - SAPLING (8), KELP (25), CHORUS (0)
     */

    // Leaves group (tintable variants) - 65 slots shared
    TINTABLE_LEAVES(65),
    WATERLOGGED_TINTABLE_LEAVES(65),  // Shares TINTABLE_LEAVES pool

    // Leaves group (non-tintable variants) - 234 slots shared
    NON_TINTABLE_LEAVES(234),
    WATERLOGGED_NON_TINTABLE_LEAVES(234),  // Shares NON_TINTABLE_LEAVES pool

    // Leaves group (generic) - 143 slots shared
    LEAVES(143),
    WATERLOGGED_LEAVES(143),  // Shares LEAVES pool

    // Tripwire group - 126 slots shared (63*2)
    LOWER_TRIPWIRE(63),
    HIGHER_TRIPWIRE(63),
    TRIPWIRE(126),  // Pool for both tripwire types

    // Mushroom blocks group (individual blocks have their own limits)
    MUSHROOM_STEM(63),
    RED_MUSHROOM_BLOCK(63),
    BROWN_MUSHROOM_BLOCK(63),
    // MUSHROOM is the shared pool (189 = 63*3), contains draw from this pool
    MUSHROOM(189),

    // Universal fallback and composite
    NOTE_BLOCK(1149),
    SOLID(1338),

    // Plant group (tall plants)
    CACTUS(15),
    SUGAR_CANE(15),

    // Vines group
    WEEPING_VINE(50),
    TWISTING_VINE(50),
    CAVE_VINE(100),

    // Other plants
    SAPLING(8),
    KELP(25),
    CHORUS(0);

    private final Map<Plugins, Integer> pluginLimits = new HashMap<>();
    private final List<CraftEngineBlockState> contains = new ArrayList<>();
    private final List<CraftEngineBlockState> equivalents = new ArrayList<>();
    private final Set<BlockType> blocks = new HashSet<>();
    private final int limit;
    private int start = 0;
    private CraftEngineBlockState parent = null;  // Reference to parent pool

    static {
        // Setup parent-child relationships for shared pools

        // Mushroom blocks share the MUSHROOM pool (189 = 63*3)
        RED_MUSHROOM_BLOCK.parent = MUSHROOM;
        BROWN_MUSHROOM_BLOCK.parent = MUSHROOM;
        MUSHROOM_STEM.parent = MUSHROOM;

        // MUSHROOM and NOTE_BLOCK share the SOLID pool (1338 = 189 + 1149)
        MUSHROOM.parent = SOLID;
        NOTE_BLOCK.parent = SOLID;

        // Waterlogged leaves share their non-waterlogged counterpart's pool
        WATERLOGGED_TINTABLE_LEAVES.parent = TINTABLE_LEAVES;
        WATERLOGGED_NON_TINTABLE_LEAVES.parent = NON_TINTABLE_LEAVES;
        WATERLOGGED_LEAVES.parent = LEAVES;

        // Tripwire variants share pools
        LOWER_TRIPWIRE.parent = TRIPWIRE;  // TRIPWIRE pool = 126 (63*2)
        HIGHER_TRIPWIRE.parent = TRIPWIRE;

        TRIPWIRE.contains.add(LOWER_TRIPWIRE);
        TRIPWIRE.contains.add(HIGHER_TRIPWIRE);

        MUSHROOM.contains.add(RED_MUSHROOM_BLOCK);
        MUSHROOM.contains.add(BROWN_MUSHROOM_BLOCK);
        MUSHROOM.contains.add(MUSHROOM_STEM);

        SOLID.contains.add(MUSHROOM);
        SOLID.contains.add(NOTE_BLOCK);

        TINTABLE_LEAVES.equivalents.add(WATERLOGGED_TINTABLE_LEAVES);
        TINTABLE_LEAVES.equivalents.add(LEAVES);
        WATERLOGGED_TINTABLE_LEAVES.equivalents.add(TINTABLE_LEAVES);
        WATERLOGGED_TINTABLE_LEAVES.equivalents.add(WATERLOGGED_LEAVES);

        NON_TINTABLE_LEAVES.equivalents.add(WATERLOGGED_NON_TINTABLE_LEAVES);
        NON_TINTABLE_LEAVES.equivalents.add(LEAVES);
        WATERLOGGED_NON_TINTABLE_LEAVES.equivalents.add(NON_TINTABLE_LEAVES);
        WATERLOGGED_NON_TINTABLE_LEAVES.equivalents.add(WATERLOGGED_LEAVES);

        LEAVES.equivalents.add(WATERLOGGED_LEAVES);
        LEAVES.equivalents.add(TINTABLE_LEAVES);
        LEAVES.equivalents.add(NON_TINTABLE_LEAVES);
        WATERLOGGED_LEAVES.equivalents.add(LEAVES);
        WATERLOGGED_LEAVES.equivalents.add(WATERLOGGED_TINTABLE_LEAVES);
        WATERLOGGED_LEAVES.equivalents.add(WATERLOGGED_NON_TINTABLE_LEAVES);

        LOWER_TRIPWIRE.equivalents.add(HIGHER_TRIPWIRE);
        LOWER_TRIPWIRE.equivalents.add(TRIPWIRE);
        HIGHER_TRIPWIRE.equivalents.add(LOWER_TRIPWIRE);
        HIGHER_TRIPWIRE.equivalents.add(TRIPWIRE);
        TRIPWIRE.equivalents.add(LOWER_TRIPWIRE);
        TRIPWIRE.equivalents.add(HIGHER_TRIPWIRE);

        RED_MUSHROOM_BLOCK.equivalents.add(BROWN_MUSHROOM_BLOCK);
        RED_MUSHROOM_BLOCK.equivalents.add(MUSHROOM_STEM);
        RED_MUSHROOM_BLOCK.equivalents.add(NOTE_BLOCK);
        BROWN_MUSHROOM_BLOCK.equivalents.add(RED_MUSHROOM_BLOCK);
        BROWN_MUSHROOM_BLOCK.equivalents.add(MUSHROOM_STEM);
        BROWN_MUSHROOM_BLOCK.equivalents.add(NOTE_BLOCK);
        MUSHROOM_STEM.equivalents.add(RED_MUSHROOM_BLOCK);
        MUSHROOM_STEM.equivalents.add(BROWN_MUSHROOM_BLOCK);
        MUSHROOM_STEM.equivalents.add(NOTE_BLOCK);

        NOTE_BLOCK.equivalents.add(RED_MUSHROOM_BLOCK);
        NOTE_BLOCK.equivalents.add(BROWN_MUSHROOM_BLOCK);
        NOTE_BLOCK.equivalents.add(MUSHROOM_STEM);

        WEEPING_VINE.equivalents.add(TWISTING_VINE);
        WEEPING_VINE.equivalents.add(CAVE_VINE);
        TWISTING_VINE.equivalents.add(WEEPING_VINE);
        TWISTING_VINE.equivalents.add(CAVE_VINE);
        CAVE_VINE.equivalents.add(WEEPING_VINE);
        CAVE_VINE.equivalents.add(TWISTING_VINE);

        CACTUS.equivalents.add(SUGAR_CANE);
        SUGAR_CANE.equivalents.add(CACTUS);

        SAPLING.equivalents.add(LEAVES); // Transparent fallback
        KELP.equivalents.add(NOTE_BLOCK);
        CHORUS.equivalents.add(LEAVES); // Transparent fallback

        Set<BlockType> leaves = Set.of(BlockType.OAK_LEAVES, BlockType.SPRUCE_LEAVES, BlockType.BIRCH_LEAVES,
                BlockType.JUNGLE_LEAVES, BlockType.ACACIA_LEAVES, BlockType.DARK_OAK_LEAVES,
                BlockType.MANGROVE_LEAVES, BlockType.CHERRY_LEAVES, BlockType.PALE_OAK_LEAVES,
                BlockType.AZALEA_LEAVES, BlockType.FLOWERING_AZALEA_LEAVES);
        TINTABLE_LEAVES.addAllBlocks(leaves);
        WATERLOGGED_TINTABLE_LEAVES.addAllBlocks(leaves);
        NON_TINTABLE_LEAVES.addAllBlocks(leaves);
        WATERLOGGED_NON_TINTABLE_LEAVES.addAllBlocks(leaves);
        LEAVES.addAllBlocks(leaves);
        WATERLOGGED_LEAVES.addAllBlocks(leaves);

        LOWER_TRIPWIRE.addBlock(BlockType.TRIPWIRE);
        HIGHER_TRIPWIRE.addBlock(BlockType.TRIPWIRE);
        TRIPWIRE.addBlock(BlockType.TRIPWIRE);

        MUSHROOM_STEM.addBlock(BlockType.MUSHROOM_STEM);
        RED_MUSHROOM_BLOCK.addBlock(BlockType.RED_MUSHROOM_BLOCK);
        BROWN_MUSHROOM_BLOCK.addBlock(BlockType.BROWN_MUSHROOM_BLOCK);
        MUSHROOM.addAllBlocks(Set.of(BlockType.MUSHROOM_STEM, BlockType.RED_MUSHROOM_BLOCK, BlockType.BROWN_MUSHROOM_BLOCK));

        NOTE_BLOCK.addBlock(BlockType.NOTE_BLOCK);
        SOLID.addAllBlocks(Set.of(BlockType.MUSHROOM_STEM, BlockType.RED_MUSHROOM_BLOCK, BlockType.BROWN_MUSHROOM_BLOCK, BlockType.NOTE_BLOCK));

        CACTUS.addBlock(BlockType.CACTUS);
        SUGAR_CANE.addBlock(BlockType.SUGAR_CANE);
        WEEPING_VINE.addBlock(BlockType.WEEPING_VINES);
        TWISTING_VINE.addBlock(BlockType.TWISTING_VINES);
        CAVE_VINE.addBlock(BlockType.CAVE_VINES);
        SAPLING.addAllBlocks(Set.of(BlockType.OAK_SAPLING, BlockType.SPRUCE_SAPLING, BlockType.BIRCH_SAPLING,
                BlockType.JUNGLE_SAPLING, BlockType.ACACIA_SAPLING, BlockType.DARK_OAK_SAPLING,
                BlockType.CHERRY_SAPLING, BlockType.PALE_OAK_SAPLING));
        KELP.addBlock(BlockType.KELP);
        CHORUS.addBlock(BlockType.CHORUS_PLANT);
    }

    CraftEngineBlockState(int limit) {
        this.limit = limit;
    }

    /**
     * Set the starting index for this CraftEngineBlockState.
     * When a child node's start is set, it reserves slots in the parent pool.
     * For example, if MUSHROOM.start = 189 and MUSHROOM is part of SOLID,
     * this reserves 189 slots in SOLID for MUSHROOM.
     * @param start The starting index (must be >= 0)
     */
    public void setStart(int start) {
        if (start < 0) {
            throw new IllegalArgumentException("Start must be >= 0");
        }

        int oldStart = this.start;
        this.start = start;

        if (this.parent != null) {
            int deltaStart = start - oldStart;
            if (deltaStart > 0) {
                int newParentStart = this.parent.start + deltaStart;
                this.parent.setStart(newParentStart);
            }
        }
    }

    /**
     * Check if this CraftEngineBlockState has reached its limit.
     * Available slots = limit - start. If start >= limit, no slots are available.
     * @param plugin The plugin to check
     * @return true if the limit is reached, false otherwise
     */
    private boolean hasReachLimit(Plugins plugin) {
        if (this.limit != -1) {
            int availableSlots = this.limit - this.start;
            if (availableSlots <= 0) {
                return true;
            }

            int myCurrentIndex = this.pluginLimits.getOrDefault(plugin, this.start);
            if (myCurrentIndex >= this.limit) {
                return true;
            }
        }

        CraftEngineBlockState target = getCountTarget();

        if (target == this) {
            return false;
        }

        if (target.limit == -1) {
            return false;
        }

        int currentCount = target.getCurrentCount(plugin);

        return currentCount >= target.limit;
    }

    /**
     * Get the BlockState that should be used for counting.
     * Returns parent if this BlockState is part of a shared pool, otherwise returns self.
     */
    private CraftEngineBlockState getCountTarget() {
        CraftEngineBlockState current = this;
        while (current.parent != null) {
            current = current.parent;
        }
        return current;
    }

    /**
     * Get the current count for this CraftEngineBlockState.
     * Returns the current index for this node (or aggregated from children).
     * @param plugin The plugin to check
     * @return The current count/index
     */
    private int getCurrentCount(Plugins plugin) {
        if (Configuration.limitType == LimitType.PLUGIN) {
            return getMaxIndexRecursive(this, plugin);
        } else {
            return getTotalMaxIndexRecursive(this);
        }
    }

    /**
     * Return the maximum index used for a given plugin across the node and its descendants.
     * Only counts actual increments (entries in pluginLimits), not default start values.
     */
    private int getMaxIndexRecursive(CraftEngineBlockState node, Plugins plugin) {
        int maxIndex = node.start;

        Integer actualIndex = node.pluginLimits.get(plugin);
        if (actualIndex != null) {
            maxIndex = Math.max(maxIndex, actualIndex);
        }

        for (CraftEngineBlockState child : node.contains) {
            int childMax = getMaxIndexRecursive(child, plugin);
            maxIndex = Math.max(maxIndex, childMax);
        }

        return maxIndex;
    }

    /**
     * Return the maximum index used across all plugins for the node and its descendants.
     * Only counts actual increments (entries in pluginLimits), not default start values.
     */
    private int getTotalMaxIndexRecursive(CraftEngineBlockState node) {
        int maxIndex = node.start;

        for (Integer idx : node.pluginLimits.values()) {
            if (idx != null) {
                maxIndex = Math.max(maxIndex, idx);
            }
        }

        for (CraftEngineBlockState child : node.contains) {
            int childMax = getTotalMaxIndexRecursive(child);
            maxIndex = Math.max(maxIndex, childMax);
        }

        return maxIndex;
    }

    /**
     * Increment the usage counter.
     * Stores the index on THIS node (not the root), so that each node tracks its own range.
     * @param plugin The plugin using this CraftEngineBlockState
     */
    public void increment(Plugins plugin) {
        int currentCount = this.pluginLimits.getOrDefault(plugin, this.start);
        this.pluginLimits.put(plugin, currentCount + 1);
    }

    /**
     * Decrement the usage counter.
     * Decrements THIS node's counter.
     * @param plugin The plugin using this CraftEngineBlockState
     */
    public void decrement(Plugins plugin) {
        int current = this.pluginLimits.getOrDefault(plugin, this.start);
        if (current > this.start) {
            this.pluginLimits.put(plugin, current - 1);
        } else {
            this.pluginLimits.remove(plugin);
        }
    }

    /**
     * Add an equivalent CraftEngineBlockState that can be used as a fallback when this one reaches its limit.
     * @param equivalent The equivalent CraftEngineBlockState
     */
    public void addEquivalent(CraftEngineBlockState equivalent) {
        if (!this.equivalents.contains(equivalent)) {
            this.equivalents.add(equivalent);
        }
    }

    /**
     * Get an available CraftEngineBlockState name (this or an equivalent) in lowercase.
     * Returns null if this CraftEngineBlockState and all its equivalents have reached their limit.
     * @param plugin The plugin requesting the CraftEngineBlockState
     * @return The name in lowercase of an available CraftEngineBlockState, or null if none available
     */
    public String getAvailable(Plugins plugin) {
        return getAvailableRecursive(plugin, new ArrayList<>());
    }

    /**
     * Get an available CraftEngineBlockState and increment its counter atomically.
     * Returns null if this CraftEngineBlockState and all its equivalents have reached their limit.
     * @param plugin The plugin requesting the CraftEngineBlockState
     * @return The CraftEngineBlockState instance that was incremented, or null if none available
     */
    public CraftEngineBlockState getAvailableAndIncrement(Plugins plugin) {
        CraftEngineBlockState available = getAvailableCraftEngineBlockState(plugin, new ArrayList<>());

        if (available == null) {
            return null;
        }

        available.increment(plugin);

        return available;
    }

    /**
     * Convenience helper which increments an available block state and returns its lowercase name.
     * @param plugin the plugin requesting the state
     * @return lowercase name of the chosen state, or null if none available
     */
    public String getAvailableAndIncrementName(Plugins plugin) {
        CraftEngineBlockState state = getAvailableAndIncrement(plugin);
        return state != null ? state.name().toLowerCase() : null;
    }

    /**
     * Internal recursive method to find an available CraftEngineBlockState with cycle detection.
     * @param plugin The plugin requesting the CraftEngineBlockState
     * @param visited Set of already visited CraftEngineBlockStates to prevent infinite loops
     * @return The name in lowercase of an available CraftEngineBlockState, or null if none available
     */
    private String getAvailableRecursive(Plugins plugin, List<CraftEngineBlockState> visited) {
        CraftEngineBlockState available = getAvailableCraftEngineBlockState(plugin, visited);
        return available != null ? available.name().toLowerCase() : null;
    }

    /**
     * Internal recursive method to find an available CraftEngineBlockState instance with cycle detection.
     * @param plugin The plugin requesting the CraftEngineBlockState
     * @param visited Set of already visited CraftEngineBlockStates to prevent infinite loops
     * @return The CraftEngineBlockState instance if available, or null if none available
     */
    private CraftEngineBlockState getAvailableCraftEngineBlockState(Plugins plugin, List<CraftEngineBlockState> visited) {
        if (visited.contains(this)) {
            return null;
        }
        visited.add(this);

        if (!hasReachLimit(plugin)) {
            return this;
        }

        for (CraftEngineBlockState equivalent : equivalents) {
            CraftEngineBlockState result = equivalent.getAvailableCraftEngineBlockState(plugin, visited);
            if (result != null) {
                return result;
            }
        }

        for (CraftEngineBlockState contained : contains) {
            CraftEngineBlockState result = contained.getAvailableCraftEngineBlockState(plugin, visited);
            if (result != null) {
                return result;
            }
        }

        return null;
    }

    public int getLimit() {
        return this.limit;
    }

    public int getStart() {
        return this.start;
    }

    public CraftEngineBlockState getParent() {
        return this.parent;
    }

    public boolean hasParent() {
        return this.parent != null;
    }

    /**
     * Get the number of remaining available slots.
     * Available slots = limit - current index.
     * @param plugin The plugin to check
     * @return The number of remaining slots
     */
    public int getRemainingSlots(Plugins plugin) {
        int currentIndex = this.pluginLimits.getOrDefault(plugin, this.start);
        return Math.max(0, this.limit - currentIndex);
    }

    /**
     * Get the current usage count (number of slots used from start).
     * @param plugin The plugin to check
     * @return The number of slots currently in use
     */
    public int getUsedSlots(Plugins plugin) {
        int currentIndex = this.pluginLimits.getOrDefault(plugin, this.start);
        return Math.max(0, currentIndex - this.start);
    }

    public List<CraftEngineBlockState> getContains() {
        return new ArrayList<>(contains);
    }

    public List<CraftEngineBlockState> getEquivalents() {
        return new ArrayList<>(equivalents);
    }

    public static void resetAllLimits() {
        for (CraftEngineBlockState state : CraftEngineBlockState.values()) {
            state.pluginLimits.clear();
        }
    }

    public void addAllBlocks(Set<BlockType> blockTypes) {
        this.blocks.addAll(blockTypes);
    }

    public void addBlock(BlockType blockType) {
        this.blocks.add(blockType);
    }
}

