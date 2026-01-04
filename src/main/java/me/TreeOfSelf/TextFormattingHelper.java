package me.TreeOfSelf;

import eu.pb4.placeholders.api.TextParserUtils;
import net.minecraft.network.chat.Component;

public class TextFormattingHelper {

    public static Component formatTextWithCustomCodes(String text) {
        if (text == null || text.isEmpty()) {
            return Component.empty();
        }

        String processedText = text.replace("<ra>", "<gr:red:yellow:green>");

        return TextParserUtils.formatTextSafe(processedText);
    }
}