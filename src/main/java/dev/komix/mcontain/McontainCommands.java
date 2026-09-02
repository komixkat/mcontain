package dev.komix.mcontain;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;

import java.util.Locale;
import java.util.Map;

public final class McontainCommands {

	private McontainCommands() {
	}

	public static void register(McontainMod mod, Object server) {
		Object commands = Compat.invoke(server, "getCommands");
		CommandDispatcher dispatcher = (CommandDispatcher) Compat.invoke(commands, "getDispatcher");
		if (dispatcher == null) {
			dispatcher = (CommandDispatcher) Compat.invoke(commands, "getCommands");
		}
		if (dispatcher == null) {
			return;
		}

		LiteralArgumentBuilder<CommandSourceStack> root = LiteralArgumentBuilder.<CommandSourceStack>literal("mcontain")
				.requires(src -> Compat.hasPermission(src));

		root.then(LiteralArgumentBuilder.<CommandSourceStack>literal("gate")
				.then(LiteralArgumentBuilder.<CommandSourceStack>literal("set")
						.executes(ctx -> gateSet(mod, ctx, 0))
						.then(RequiredArgumentBuilder.<CommandSourceStack, Integer>argument("radius", IntegerArgumentType.integer(1, 128))
								.executes(ctx -> gateSet(mod, ctx, IntegerArgumentType.getInteger(ctx, "radius")))))
				.then(LiteralArgumentBuilder.<CommandSourceStack>literal("unset")
						.executes(ctx -> gateUnset(mod, ctx)))
				.then(LiteralArgumentBuilder.<CommandSourceStack>literal("verify")
						.then(RequiredArgumentBuilder.<CommandSourceStack, String>argument("player", StringArgumentType.word())
								.executes(ctx -> verify(mod, ctx, true))))
				.then(LiteralArgumentBuilder.<CommandSourceStack>literal("unverify")
						.then(RequiredArgumentBuilder.<CommandSourceStack, String>argument("player", StringArgumentType.word())
								.executes(ctx -> verify(mod, ctx, false)))));

		root.then(LiteralArgumentBuilder.<CommandSourceStack>literal("jail")
				.then(LiteralArgumentBuilder.<CommandSourceStack>literal("set")
						.then(RequiredArgumentBuilder.<CommandSourceStack, String>argument("name", StringArgumentType.word())
								.executes(ctx -> jailSet(mod, ctx, mod.config.default_radius))
								.then(RequiredArgumentBuilder.<CommandSourceStack, Integer>argument("radius", IntegerArgumentType.integer(1, 128))
										.executes(ctx -> jailSet(mod, ctx, IntegerArgumentType.getInteger(ctx, "radius"))))))
				.then(LiteralArgumentBuilder.<CommandSourceStack>literal("list")
						.executes(ctx -> jailList(mod, ctx)))
				.then(LiteralArgumentBuilder.<CommandSourceStack>literal("clear")
						.executes(ctx -> jailClear(mod, ctx)))
				.then(RequiredArgumentBuilder.<CommandSourceStack, String>argument("name", StringArgumentType.word())
						.then(RequiredArgumentBuilder.<CommandSourceStack, String>argument("player", StringArgumentType.word())
								.executes(ctx -> jail(mod, ctx, 0))
								.then(RequiredArgumentBuilder.<CommandSourceStack, Integer>argument("minutes", IntegerArgumentType.integer(1, 525600))
										.executes(ctx -> jail(mod, ctx, IntegerArgumentType.getInteger(ctx, "minutes")))))));

		root.then(LiteralArgumentBuilder.<CommandSourceStack>literal("unjail")
				.then(RequiredArgumentBuilder.<CommandSourceStack, String>argument("player", StringArgumentType.word())
						.executes(ctx -> unjail(mod, ctx))));
		root.then(LiteralArgumentBuilder.<CommandSourceStack>literal("reload")
				.executes(ctx -> reload(mod, ctx)));
		root.then(LiteralArgumentBuilder.<CommandSourceStack>literal("status")
				.executes(ctx -> status(mod, ctx)));

		dispatcher.register(root);
	}

	private static int gateSet(McontainMod mod, CommandContext<CommandSourceStack> ctx, int radius) {
		Object source = ctx.getSource();
		Object player = Compat.commandPlayer(source);
		if (player == null) {
			McontainMod.log("gate set requires a player");
			return -1;
		}
		mod.config.gate.x = Compat.xOf(player);
		mod.config.gate.y = Compat.yOf(player);
		mod.config.gate.z = Compat.zOf(player);
		mod.config.gate.hasPos = true;
		mod.config.gate.enabled = true;
		if (radius > 0) {
			mod.config.gate.radius = radius;
		}
		mod.saveConfig();
		Compat.send(player, "Gate set at " + Math.round(mod.config.gate.x) + ", "
				+ Math.round(mod.config.gate.y) + ", " + Math.round(mod.config.gate.z)
				+ " with radius " + mod.config.gate.radius);
		return 1;
	}

	private static int gateUnset(McontainMod mod, CommandContext<CommandSourceStack> ctx) {
		mod.config.gate.hasPos = false;
		mod.config.gate.enabled = false;
		mod.saveConfig();
		feedback(mod, ctx.getSource(), "Gate disabled. It will follow world spawn when re-enabled.");
		return 1;
	}

	private static int verify(McontainMod mod, CommandContext<CommandSourceStack> ctx, boolean add) {
		String name = StringArgumentType.getString(ctx, "player").toLowerCase(Locale.ROOT);
		boolean changed;
		if (add) {
			changed = !mod.config.verified.contains(name) && mod.config.verified.add(name);
		} else {
			changed = mod.config.verified.remove(name);
		}
		if (changed) {
			mod.saveConfig();
		}
		feedback(mod, ctx.getSource(), (add ? "Verified " : "Unverified ") + name);
		return 1;
	}

	private static int jailSet(McontainMod mod, CommandContext<CommandSourceStack> ctx, int radius) {
		Object source = ctx.getSource();
		Object player = Compat.commandPlayer(source);
		if (player == null) {
			McontainMod.log("jail set requires a player");
			return -1;
		}
		String name = StringArgumentType.getString(ctx, "name").toLowerCase(Locale.ROOT);
		McontainConfig.Jail jail = new McontainConfig.Jail();
		jail.x = Compat.xOf(player);
		jail.y = Compat.yOf(player);
		jail.z = Compat.zOf(player);
		jail.radius = radius;
		mod.config.jails.put(name, jail);
		mod.saveConfig();
		Compat.send(player, "Jail '" + name + "' set at " + Math.round(jail.x) + ", "
				+ Math.round(jail.y) + ", " + Math.round(jail.z) + " with radius " + radius);
		return 1;
	}

	private static int jail(McontainMod mod, CommandContext<CommandSourceStack> ctx, int minutes) {
		String jailName = StringArgumentType.getString(ctx, "name").toLowerCase(Locale.ROOT);
		McontainConfig.Jail jail = mod.config.jails.get(jailName);
		if (jail == null) {
			feedback(mod, ctx.getSource(), "No jail named '" + jailName + "'");
			return -1;
		}
		Object server = Compat.invoke(ctx.getSource(), "getServer");
		Object target = findPlayer(server, StringArgumentType.getString(ctx, "player"));
		if (target == null) {
			feedback(mod, ctx.getSource(), "Player not found");
			return -1;
		}
		McontainConfig.Sentence sentence = new McontainConfig.Sentence();
		sentence.jail = jailName;
		sentence.until = minutes > 0 ? System.currentTimeMillis() + minutes * 60_000L : 0L;
		mod.config.jailed.put(Compat.uuidOf(target), sentence);
		mod.saveConfig();
		Object overworld = Compat.overworld(server);
		Compat.setGameMode(target, "ADVENTURE");
		Compat.teleportPlayer(target, overworld, jail.x, jail.y, jail.z);
		Compat.send(target, minutes > 0
				? "You have been jailed in '" + jailName + "' for " + minutes + " minutes."
				: "You have been jailed in '" + jailName + "'.");
		feedback(mod, ctx.getSource(), "Jailed " + Compat.nameOf(target) + " in '" + jailName + "'");
		return 1;
	}

	private static int unjail(McontainMod mod, CommandContext<CommandSourceStack> ctx) {
		Object server = Compat.invoke(ctx.getSource(), "getServer");
		Object target = findPlayer(server, StringArgumentType.getString(ctx, "player"));
		boolean removed = target != null && mod.config.jailed.remove(Compat.uuidOf(target)) != null;
		if (removed) {
			if ("ADVENTURE".equals(Compat.currentGameMode(target))) {
				Compat.setGameMode(target, "SURVIVAL");
			}
			Compat.send(target, "You have been unjailed.");
			mod.saveConfig();
		}
		feedback(mod, ctx.getSource(), removed ? "Unjailed." : "No current sentence for that player");
		return 1;
	}

	private static int jailList(McontainMod mod, CommandContext<CommandSourceStack> ctx) {
		Object source = ctx.getSource();
		Object player = Compat.commandPlayer(source);
		if (player == null) {
			return -1;
		}
		if (mod.config.jails.isEmpty()) {
			Compat.send(player, "No jail cells defined.");
		} else {
			Compat.send(player, "Cells: " + String.join(", ", mod.config.jails.keySet()));
		}
		if (mod.config.jailed.isEmpty()) {
			Compat.send(player, "No one is currently jailed.");
		} else {
			for (Map.Entry<String, McontainConfig.Sentence> entry : mod.config.jailed.entrySet()) {
				Compat.send(player, entry.getKey() + " is in '" + entry.getValue().jail + "'" + remaining(entry.getValue().until));
			}
		}
		return 1;
	}

	private static int jailClear(McontainMod mod, CommandContext<CommandSourceStack> ctx) {
		Object server = Compat.invoke(ctx.getSource(), "getServer");
		for (Object target : Compat.onlinePlayers(server)) {
			if (mod.config.jailed.remove(Compat.uuidOf(target)) != null) {
				if ("ADVENTURE".equals(Compat.currentGameMode(target))) {
					Compat.setGameMode(target, "SURVIVAL");
				}
				Compat.send(target, "You have been released.");
			}
		}
		mod.config.jailed.clear();
		mod.saveConfig();
		feedback(mod, ctx.getSource(), "All sentences cleared. Cells kept.");
		return 1;
	}

	private static int reload(McontainMod mod, CommandContext<CommandSourceStack> ctx) {
		mod.config = McontainConfig.load(mod.configFile());
		mod.saveConfig();
		feedback(mod, ctx.getSource(), "Config reloaded.");
		return 1;
	}

	private static int status(McontainMod mod, CommandContext<CommandSourceStack> ctx) {
		Object source = ctx.getSource();
		Object player = Compat.commandPlayer(source);
		if (player == null) {
			return -1;
		}
		String gateState = mod.config.gate.enabled
				? "enabled" + (mod.config.gate.hasPos
						? " at " + Math.round(mod.config.gate.x) + ", " + Math.round(mod.config.gate.y) + ", "
							+ Math.round(mod.config.gate.z)
						: " at world spawn")
				: "disabled";
		Compat.send(player, "Gate: " + gateState + ", radius " + mod.config.gate.radius);
		Compat.send(player, "Verified: " + mod.config.verified.size() + ", jailed: " + mod.config.jailed.size());
		return 1;
	}

	private static String remaining(long until) {
		if (until <= 0) {
			return " (permanent)";
		}
		long minutes = Math.max(0, (until - System.currentTimeMillis()) / 60_000L);
		return " (" + minutes + " min left)";
	}

	private static Object findPlayer(Object server, String name) {
		Object playerList = Compat.invoke(server, "getPlayerList");
		Object target = Compat.invoke(playerList, "getPlayerByName", name);
		if (target == null && server != null) {
			for (Object p : Compat.onlinePlayers(server)) {
				if (Compat.nameOf(p).equalsIgnoreCase(name)) {
					return p;
				}
			}
		}
		return target;
	}

	private static void feedback(McontainMod mod, Object source, String message) {
		Object operator = Compat.commandPlayer(source);
		if (operator != null) {
			Compat.send(operator, message);
		} else {
			Object server = Compat.invoke(source, "getServer");
			Compat.broadcast(server, message);
		}
	}
}