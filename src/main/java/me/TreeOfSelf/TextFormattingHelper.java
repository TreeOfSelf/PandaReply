package me.TreeOfSelf;

import eu.pb4.placeholders.api.ParserContext;
import eu.pb4.placeholders.api.parsers.NodeParser;
import eu.pb4.placeholders.api.parsers.ParserBuilder;
import net.minecraft.network.chat.Component;

public class TextFormattingHelper {

    private static final NodeParser PARSER = ParserBuilder.of()
            .simplifiedTextFormat()
            .requireSafe()
            .build();

    public static Component formatTextWithCustomCodes(String text) {
        if (text == null || text.isEmpty()) {
            return Component.empty();
        }

        String processedText = text.replace("<ra>", "<gr:red:yellow:green>");

        return PARSER.parseComponent(processedText, ParserContext.of());
    }
}
