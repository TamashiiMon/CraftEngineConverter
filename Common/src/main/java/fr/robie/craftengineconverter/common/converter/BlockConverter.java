package fr.robie.craftengineconverter.common.converter;

import fr.robie.craftengineconverter.common.CraftEngineConverterPlugin;
import fr.robie.craftengineconverter.common.enums.Plugins;
import org.bukkit.Location;

import java.util.Set;

public abstract class BlockConverter extends ObjectConverter {
    public BlockConverter(CraftEngineConverterPlugin plugin, Plugins pluginType) {
        super(plugin, pluginType);
    }

    protected void executeBlockConversion(Location blockLoc, Set<Location> processed, ConversionCounter counter) {
        for (int[] offset : ADJACENT_OFFSETS) {
            if (counter.hasReachedLimit()) {
                return;
            }

            Location adjacentLoc = blockLoc.clone().add(offset[0], offset[1], offset[2]);

            if (!processed.add(adjacentLoc)) {
                continue;
            }

            if (!this.isCustomBlockAt(adjacentLoc)) {
                continue;
            }

            String newName = this.getNewNameForCustomBlock(adjacentLoc);
            if (newName == null) {
                continue;
            }

            if (this.removeBlockAt(adjacentLoc)) {
                this.placeBlock(newName, adjacentLoc);
                counter.increment();
                executeBlockConversion(adjacentLoc, processed, counter);
            }
        }
    }

    public abstract boolean isCustomBlockAt(Location location);

    public abstract String getNewNameForCustomBlock(Location location);

    public abstract boolean removeBlockAt(Location location);

    public abstract void placeBlock(String itemId, Location location);


}
