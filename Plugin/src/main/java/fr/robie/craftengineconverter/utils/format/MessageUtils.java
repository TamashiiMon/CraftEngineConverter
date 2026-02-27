package fr.robie.craftengineconverter.utils.format;

import fr.robie.craftengineconverter.CraftEngineConverter;
import fr.robie.craftengineconverter.common.format.Message;
import fr.robie.craftengineconverter.common.format.MessageFormatter;
import fr.robie.craftengineconverter.common.format.TextFormatter;
import fr.robie.craftengineconverter.common.utils.enums.DefaultFontInfo;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

public abstract class MessageUtils extends TextFormatter {
    private final static int CENTER_PX = 154;

    protected void messageWO(CraftEngineConverter plugin, CommandSender sender, Message message, Object... args) {
        plugin.getMessageFormatter().sendMessage(sender, parseText(message, args));
    }

    protected void messageWO(CraftEngineConverter plugin, CommandSender player, String message, Object... args) {
        plugin.getMessageFormatter().sendMessage(player, parseText(message, args));
    }
    protected void message(CraftEngineConverter plugin, CommandSender sender, String message, Object... args) {
        plugin.getMessageFormatter().sendMessage(sender, Message.COMMAND__PREFIX.msg() + parseText(message, args));
    }
    protected void message(CraftEngineConverter plugin, CommandSender sender, Message message, Object... args) {
        MessageFormatter messageFormatter = plugin.getMessageFormatter();
        if (sender instanceof ConsoleCommandSender) {
            if (!message.getMessages().isEmpty()) {
                message.getMessages().forEach(msg -> messageFormatter.sendMessage(sender, Message.COMMAND__PREFIX.msg() + parseText(msg, args)));
            } else {
                messageFormatter.sendMessage(sender, Message.COMMAND__PREFIX.msg() + parseText(message, args));
            }
        } else {
            Player player = (Player) sender;
            switch (message.getType()) {
                case CENTER:
                    if (!message.getMessages().isEmpty()) {
                        message.getMessages().forEach(msg -> messageFormatter.sendMessage(sender, this.getCenteredMessage(parseText(msg, args))));
                    } else {
                        messageFormatter.sendMessage(sender, this.getCenteredMessage(parseText(message, args)));
                    }
                    break;
                case ACTION:
                    messageFormatter.sendAction(player, parseText(message, args));
                    break;
                case TCHAT:
                    if (!message.getMessages().isEmpty()) {
                        message.getMessages().forEach(msg -> messageFormatter.sendMessage(sender, Message.COMMAND__PREFIX.msg() + parseText(msg, args)));
                    } else {
                        messageFormatter.sendMessage(sender, Message.COMMAND__PREFIX.msg() + parseText(message, args));
                    }
                    break;
                default:
                    break;
            }
        }


    }

    protected String getCenteredMessage(String message) {
        if (message == null || message.isEmpty()) {
            return "";
        }
        message = ChatColor.translateAlternateColorCodes('&', message);

        int messagePxSize = 0;
        boolean previousCode = false;
        boolean isBold = false;

        for (char c : message.toCharArray()) {
            if (c == '§') {
                previousCode = true;
            } else if (previousCode) {
                previousCode = false;
                isBold = c == 'l' || c == 'L';
            } else {
                DefaultFontInfo dFI = DefaultFontInfo.getDefaultFontInfo(c);
                messagePxSize += isBold ? dFI.getBoldLength() : dFI.getLength();
                messagePxSize++;
            }
        }

        int halvedMessageSize = messagePxSize / 2;
        int toCompensate = CENTER_PX - halvedMessageSize;
        int spaceLength = DefaultFontInfo.SPACE.getLength() + 1;
        int compensated = 0;
        StringBuilder sb = new StringBuilder();
        while (compensated < toCompensate) {
            sb.append(" ");
            compensated += spaceLength;
        }
        return sb + message;
    }
}
