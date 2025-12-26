package fr.robie.craftengineconverter.hooks.nexo;

import com.nexomc.nexo.api.NexoBlocks;
import com.nexomc.nexo.api.events.custom_block.NexoBlockInteractEvent;
import com.nexomc.nexo.mechanics.custom_block.CustomBlockMechanic;
import fr.robie.craftengineconverter.common.CraftEngineConverterPlugin;
import fr.robie.craftengineconverter.common.configuration.Configuration;
import fr.robie.craftengineconverter.common.converter.BlockConverter;
import fr.robie.craftengineconverter.common.enums.Plugins;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.HashSet;
import java.util.Set;

public class NexoBlockConverter extends BlockConverter implements Listener {

    public NexoBlockConverter(CraftEngineConverterPlugin plugin) {
        super(plugin, Plugins.NEXO);
    }

    @EventHandler
    public void onNexoBlockInteract(NexoBlockInteractEvent event) {
        if (!Configuration.nexoEnableBlockInteractionConversion) return;
        String itemID = event.getMechanic().getItemID();
        String newName = this.nameMapper.getNewName(Plugins.NEXO, itemID);

        if (newName == null) {
            return;
        }

        Block block = event.getBlock();
        Location location = block.getLocation();

        if (!removeBlockAt(location)) {
            return;
        }

        this.plugin.getPlacementTracker().placeBlock(newName, location);
        event.setCancelled(true);

        if (Configuration.allowBlockConversionPropagation && Configuration.maxBlockConversionPropagationDepth > 1) {
            Set<Location> processed = new HashSet<>();
            processed.add(location);
            ConversionCounter counter = new ConversionCounter(Configuration.maxBlockConversionPropagationDepth - 1);
            executeBlockConversion(block.getLocation(), processed, counter);
        }
    }


    @Override
    public boolean isCustomBlockAt(Location location) {
        return NexoBlocks.customBlockMechanic(location) != null;
    }

    @Override
    public String getNewNameForCustomBlock(Location location) {
        CustomBlockMechanic mechanic = NexoBlocks.customBlockMechanic(location);
        if (mechanic == null) {
            return null;
        }
        return this.nameMapper.getNewName(Plugins.NEXO, mechanic.getItemID());
    }

    @Override
    public boolean removeBlockAt(Location location) {
        return NexoBlocks.remove(location);
    }

    @Override
    public void placeBlock(String itemId, Location location) {
        this.plugin.getPlacementTracker().placeBlock(itemId, location);
    }
}