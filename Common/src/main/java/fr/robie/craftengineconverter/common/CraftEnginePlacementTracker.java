package fr.robie.craftengineconverter.common;

import net.momirealms.craftengine.bukkit.api.CraftEngineBlocks;
import net.momirealms.craftengine.bukkit.api.CraftEngineFurniture;
import net.momirealms.craftengine.core.util.Key;
import org.bukkit.Location;

public class CraftEnginePlacementTracker {

    private static class Statistics {
        int converted = 0;
        int failed = 0;

        void incrementConverted() {
            converted++;
        }

        void incrementFailed() {
            failed++;
        }

        int getTotal() {
            return converted + failed;
        }

        double getSuccessRate() {
            int total = getTotal();
            return total > 0 ? (converted * 100.0) / total : 0.0;
        }
    }

    private final Statistics blocks = new Statistics();
    private final Statistics furniture = new Statistics();

    public void placeBlock(String itemId, Location location) {
        if (CraftEngineBlocks.place(location, Key.of(itemId), false)) {
            blocks.incrementConverted();
        } else {
            blocks.incrementFailed();
        }
    }

    public void placeFurniture(String itemId, Location location) {
        if (CraftEngineFurniture.place(location, Key.of(itemId)) != null) {
            furniture.incrementConverted();
        } else {
            furniture.incrementFailed();
        }
    }

    // Blocks statistics
    public int getBlocksConverted() {
        return blocks.converted;
    }

    public int getBlocksFailed() {
        return blocks.failed;
    }

    public int getTotalBlocks() {
        return blocks.getTotal();
    }

    public double getBlocksSuccessRate() {
        return blocks.getSuccessRate();
    }

    // Furniture statistics
    public int getFurnitureConverted() {
        return furniture.converted;
    }

    public int getFurnitureFailed() {
        return furniture.failed;
    }

    public int getTotalFurniture() {
        return furniture.getTotal();
    }

    public double getFurnitureSuccessRate() {
        return furniture.getSuccessRate();
    }

    // Global statistics
    public int getTotalConverted() {
        return blocks.converted + furniture.converted;
    }

    public int getTotalFailed() {
        return blocks.failed + furniture.failed;
    }

    public int getGrandTotal() {
        return getTotalConverted() + getTotalFailed();
    }

    public double getOverallSuccessRate() {
        int total = getGrandTotal();
        return total > 0 ? (getTotalConverted() * 100.0) / total : 0.0;
    }

    public void reset() {
        blocks.converted = 0;
        blocks.failed = 0;
        furniture.converted = 0;
        furniture.failed = 0;
    }

    @Override
    public String toString() {
        return String.format(
                "Conversion Statistics:%n" +
                        "  Blocks: %d converted, %d failed (%.1f%% success)%n" +
                        "  Furniture: %d converted, %d failed (%.1f%% success)%n" +
                        "  Total: %d converted, %d failed (%.1f%% success)",
                blocks.converted, blocks.failed, blocks.getSuccessRate(),
                furniture.converted, furniture.failed, furniture.getSuccessRate(),
                getTotalConverted(), getTotalFailed(), getOverallSuccessRate()
        );
    }
}