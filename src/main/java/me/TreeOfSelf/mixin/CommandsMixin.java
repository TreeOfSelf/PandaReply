package me.TreeOfSelf.mixin;

import com.mojang.brigadier.ParseResults;
import me.TreeOfSelf.PandaReply;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Commands.class)
public class CommandsMixin {

    @Inject(method = "performCommand", at = @At("HEAD"))
    private void onPerformCommand(ParseResults<CommandSourceStack> parseResults, String string, CallbackInfo ci) {
        CommandSourceStack source = parseResults.getContext().getSource();

        if (source.getEntity() instanceof ServerPlayer sender) {
            String cmd = string.trim().toLowerCase();

            if (cmd.startsWith("msg ") || cmd.startsWith("tell ") || cmd.startsWith("w ")) {

                String[] parts = string.split(" ", 3);
                if (parts.length >= 2) {
                    String targetName = parts[1];
                    PandaReply.trackSentMessage(sender, targetName);

                    ServerPlayer target = source.getServer().getPlayerList().getPlayerByName(targetName);
                    if (target != null) {
                        PandaReply.trackReceivedMessage(target, sender.getGameProfile().name());
                    }
                }
            }
        }
    }
}