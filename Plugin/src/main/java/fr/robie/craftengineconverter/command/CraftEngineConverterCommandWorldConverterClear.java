package fr.robie.craftengineconverter.command;

import fr.robie.craftengineconverter.CraftEngineConverter;
import fr.robie.craftengineconverter.common.builder.TimerBuilder;
import fr.robie.craftengineconverter.common.format.Message;
import fr.robie.craftengineconverter.common.permission.Permission;
import fr.robie.craftengineconverter.listener.WorldConverterManager;
import fr.robie.craftengineconverter.utils.command.CommandType;
import fr.robie.craftengineconverter.utils.command.VCommand;

public class CraftEngineConverterCommandWorldConverterClear extends VCommand {
    public CraftEngineConverterCommandWorldConverterClear(CraftEngineConverter plugin) {
        super(plugin);
        this.setPermission(Permission.COMMAND_WORLDCONVERTER_CLEAR);
        this.setDescription(Message.COMMAND__WORLD_CONVERTER__CLEAR__DESCRIPTION);
        this.addSubCommand("clear");
    }

    @Override
    protected CommandType perform(CraftEngineConverter plugin) {
        long startTime = System.currentTimeMillis();
        WorldConverterManager worldConverterManager = plugin.getWorldConverterManager();

        int clearedChunks = worldConverterManager.getProcessedChunksCount();
        worldConverterManager.clearProcessedChunks();

        long endTime = System.currentTimeMillis();

        message(plugin, sender, Message.COMMAND__WORLD_CONVERTER__CLEAR__COMPLETE,
                "chunks", clearedChunks,
                "time", TimerBuilder.formatTimeAuto(endTime - startTime));

        return CommandType.SUCCESS;
    }
}
