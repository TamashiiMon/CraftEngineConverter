package fr.robie.craftengineconverter.hooks.nexo;

import com.nexomc.nexo.api.NexoFurniture;
import com.nexomc.nexo.api.events.furniture.NexoFurnitureInteractEvent;
import com.nexomc.nexo.mechanics.furniture.FurnitureMechanic;
import fr.robie.craftengineconverter.common.CraftEngineConverterPlugin;
import fr.robie.craftengineconverter.common.configuration.Configuration;
import fr.robie.craftengineconverter.common.converter.FurnitureConverter;
import fr.robie.craftengineconverter.common.enums.Plugins;
import org.bukkit.Location;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class NexoFurnitureConverter extends FurnitureConverter implements Listener {

    public NexoFurnitureConverter(CraftEngineConverterPlugin plugin) {
        super(plugin, Plugins.NEXO);
    }

    @EventHandler
    public void onFurnitureInteract(NexoFurnitureInteractEvent event){
        if (!Configuration.nexoEnableFurnitureInteractionConversion) return;
        String itemID = event.getMechanic().getItemID();
        String newName = this.nameMapper.getNewName(Plugins.NEXO, itemID);
        if (newName == null) {
            return;
        }
        ItemDisplay baseEntity = event.getBaseEntity();
        NexoFurniture.remove(baseEntity);
        Location location = baseEntity.getLocation();
        this.plugin.getPlacementTracker().placeFurniture(newName, location.add(0, -0.5, 0));
        event.setCancelled(true);

        if (Configuration.allowBlockConversionPropagation && Configuration.maxBlockConversionPropagationDepth > 1) {
            Set<Location> processed = new HashSet<>();
            processed.add(location);
            ConversionCounter counter = new ConversionCounter(Configuration.maxBlockConversionPropagationDepth - 1);
            executeFurnitureConversion(location, processed, counter);
        }
    }

    @Override
    public Location getExactEntityLocation(Location location) {
        Collection<ItemDisplay> nearbyEntitiesByType = location.getNearbyEntitiesByType(ItemDisplay.class, 1);
        for (ItemDisplay entity : nearbyEntitiesByType) {
            if (NexoFurniture.isFurniture(entity)) {
                return entity.getLocation();
            }
        }
        return location; // Fallback to the original location if no furniture entity is found /!\ if the furniture is rotated it's reset it to the original rotation 45°
    }

    @Override
    public boolean isFurnitureAt(Location location) {
        return NexoFurniture.isFurniture(location);
    }

    @Override
    public String getNewNameForFurniture(Location location) {
        FurnitureMechanic furnitureMechanic = NexoFurniture.furnitureMechanic(location);
        if (furnitureMechanic == null) {
            return null;
        }
        return this.nameMapper.getNewName(Plugins.NEXO, furnitureMechanic.getItemID());
    }

    @Override
    public boolean removeFurnitureAt(Location location) {
        return NexoFurniture.remove(location);
    }

    @Override
    public void placeFurniture(String itemId, Location location) {
        this.plugin.getPlacementTracker().placeFurniture(itemId, location);
    }
}
