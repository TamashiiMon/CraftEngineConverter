package fr.robie.craftengineconverter.common.converter;

import fr.robie.craftengineconverter.common.CraftEngineConverterPlugin;
import fr.robie.craftengineconverter.common.enums.Plugins;
import net.momirealms.craftengine.bukkit.api.CraftEngineFurniture;
import net.momirealms.craftengine.core.util.Key;
import org.bukkit.Location;

import java.util.Set;

public abstract class FurnitureConverter extends ObjectConverter {
    public FurnitureConverter(CraftEngineConverterPlugin plugin, Plugins pluginType) {
        super(plugin, pluginType);
    }

    protected void executeFurnitureConversion(Location entityLoc, Set<Location> processed, ConversionCounter counter){
        for (int[] offset : ADJACENT_OFFSETS){
            if (counter.hasReachedLimit()) return;
            Location adjacentLoc = this.getExactEntityLocation(entityLoc.clone().add(offset[0], offset[1], offset[2])).add(0, -0.5, 0);
            if (!adjacentLoc.isChunkLoaded() || !processed.add(adjacentLoc)) continue;
            if (!this.isFurnitureAt(adjacentLoc)) continue;
            String newName = this.getNewNameForFurniture(adjacentLoc);
            if (newName == null || !isRegistered(newName)) continue;
            if (this.removeFurnitureAt(adjacentLoc)){
                this.placeFurniture(newName, adjacentLoc);
                counter.increment();
                executeFurnitureConversion(adjacentLoc, processed, counter);
            }
        }
    }

    @Override
    public boolean isRegistered(String itemId){
        return CraftEngineFurniture.byId(Key.from(itemId)) != null;
    }

    public abstract Location getExactEntityLocation(Location location);

    public abstract boolean isFurnitureAt(Location location);

    public abstract String getNewNameForFurniture(Location location);

    public abstract boolean removeFurnitureAt(Location location);

    public void placeFurniture(String itemId, Location location) {
        this.plugin.getPlacementTracker().placeFurniture(itemId, location);
    }
}
