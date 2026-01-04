package me.TreeOfSelf;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import me.TreeOfSelf.TextFormattingHelper;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PandaReply implements ModInitializer {
	public static final String MOD_ID = "panda-reply";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private static final Map<UUID, String> lastSentTo = new HashMap<>();
	private static final Map<UUID, String> lastReceivedFrom = new HashMap<>();
	private static Config config;
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
	private static final File CONFIG_FILE = new File("config/PandaReply.json");

	@Override
	public void onInitialize() {

		loadConfig();

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(Commands.literal("r")
					.then(Commands.argument("message", StringArgumentType.greedyString())
							.executes(this::executeReply)));

			dispatcher.register(Commands.literal("reply")
					.then(Commands.argument("message", StringArgumentType.greedyString())
							.executes(this::executeReply)));
		});

		ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
			lastSentTo.clear();
			lastReceivedFrom.clear();
		});

		LOGGER.info("PandaReply started!");
	}

	private int executeReply(CommandContext<CommandSourceStack> context) {
		if (!(context.getSource().getEntity() instanceof ServerPlayer player)) {
			return 0;
		}

		String message = StringArgumentType.getString(context, "message");
		UUID playerUUID = player.getUUID();

		String targetPlayer;

		if (config.replyToLastReceived) {
			targetPlayer = lastReceivedFrom.get(playerUUID);
			if (targetPlayer == null) {
				player.sendSystemMessage(TextFormattingHelper.formatTextWithCustomCodes(config.noReceivedMessage));
				return 0;
			}
		} else {
			targetPlayer = lastSentTo.get(playerUUID);
			if (targetPlayer == null) {
				player.sendSystemMessage(TextFormattingHelper.formatTextWithCustomCodes(config.noSentMessage));
				return 0;
			}
		}

		context.getSource().getServer().getCommands().performPrefixedCommand(
				context.getSource(), "msg " + targetPlayer + " " + message
		);

		lastSentTo.put(playerUUID, targetPlayer);

		return 1;
	}

	public static void trackSentMessage(ServerPlayer sender, String recipient) {
		lastSentTo.put(sender.getUUID(), recipient);
	}

	public static void trackReceivedMessage(ServerPlayer receiver, String sender) {
		lastReceivedFrom.put(receiver.getUUID(), sender);
	}

	private static void loadConfig() {
		if (!CONFIG_FILE.getParentFile().exists()) {
			CONFIG_FILE.getParentFile().mkdirs();
		}

		if (CONFIG_FILE.exists()) {
			try (FileReader reader = new FileReader(CONFIG_FILE)) {
				config = GSON.fromJson(reader, Config.class);
			} catch (IOException e) {
				config = new Config();
				saveConfig();
			}
		} else {
			config = new Config();
			saveConfig();
		}
	}

	private static void saveConfig() {
		try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
			GSON.toJson(config, writer);
		} catch (IOException e) {
		}
	}

	public static class Config {
		public boolean replyToLastReceived = false;
		public String noSentMessage = "<red><bold>You haven't messaged anyone yet!";
		public String noReceivedMessage = "<red><bold>No one has messaged you yet!";
	}
}