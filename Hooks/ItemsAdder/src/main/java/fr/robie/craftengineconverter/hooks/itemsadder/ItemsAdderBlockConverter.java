package fr.robie.craftengineconverter.hooks.itemsadder;

import dev.lone.itemsadder.api.CustomBlock;
import dev.lone.itemsadder.api.Events.CustomBlockInteractEvent;
import fr.robie.craftengineconverter.common.CraftEngineConverterPlugin;
import fr.robie.craftengineconverter.common.configuration.Configuration;
import fr.robie.craftengineconverter.common.converter.BlockConverter;
import fr.robie.craftengineconverter.common.enums.Plugins;
import fr.robie.craftengineconverter.common.permission.Permission;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.HashSet;
import java.util.Set;

public class ItemsAdderBlockConverter extends BlockConverter implements Listener {
    public ItemsAdderBlockConverter(CraftEngineConverterPlugin plugin){
        super(plugin, Plugins.ITEMS_ADDER);
    }

    @EventHandler
    public void onItemsAdderBlockInteract(CustomBlockInteractEvent event){
        if (!Configuration.itemsAdderEnableBlockInteractionConversion || !event.getPlayer().hasPermission(Permission.ITEMSADDER_BLOCK_INTERACT_CONVERSION.asPermission())) return;

        String namespacedID = event.getNamespacedID();
        String newName = this.getNewName(namespacedID);

        if (newName == null || !isRegistered(newName)){
            return;
        }

        Block block = event.getBlockClicked();
        Location location = block.getLocation();

        if (!removeBlockAt(location)){
            return;
        }

        this.placeBlock(newName, location);
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
        return CustomBlock.byAlreadyPlaced(location.getBlock()) != null;
    }

    @Override
    public String getNewNameForCustomBlock(Location location) {
        CustomBlock customBlock = CustomBlock.byAlreadyPlaced(location.getBlock());
        if (customBlock == null){
            return null;
        }
        return this.getNewName(customBlock.getNamespacedID());
    }

    @Override
    public boolean removeBlockAt(Location location) {
        return CustomBlock.remove(location);
    }
}
