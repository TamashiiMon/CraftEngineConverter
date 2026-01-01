package fr.robie.craftengineconverter.common.format;

import fr.robie.craftengineconverter.common.utils.ObjectUtils;
import org.jetbrains.annotations.NotNull;

public abstract class TextFormatter extends ObjectUtils {

    protected String parseText(Message message, @NotNull Object... args) throws IllegalArgumentException {
        return parseText(message.getMessage(), args);
    }

    protected String parseText(String message, @NotNull Object... args) throws IllegalArgumentException {
        if (args.length % 2 != 0) {
            throw new IllegalArgumentException("Number of invalid arguments. Arguments must be in pairs.");
        }

        for (int i = 0; i < args.length; i += 2) {
            message = message.replace("%"+ args[i] +"%", args[i + 1].toString());
        }
        return message;
    }
}
