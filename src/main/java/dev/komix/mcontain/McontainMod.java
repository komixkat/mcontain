package dev.komix.mcontain;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;

import java.util.List;

public final class McontainMod implements ModInitializer {

	public static McontainMod INSTANCE;

	public McontainConfig config;

	private java.nio.file.Path configPath;
	private int counter;
	private boolean commandsRegistered;

	@Override
	public void onInitialize() {
		INSTANCE = this;
		configPath = FabricLoader.getInstance().getConfigDir().resolve("mcontain.json");
		config = McontainConfig.load(configPath);
		config.save(configPath);
		log("loaded");
	}

	public static void log(String message) {
		System.out.println("[mcontain] " + message);
	}

	public void onServerStart(Object server) {
		log("active on this server");
	}

	public void tryRegisterCommands(Object server) {
		if (commandsRegistered) {
			return;
		}
		if (Compat.findDispatcher(server) == null) {
			return;
		}
		McontainCommands.register(this, server);
		commandsRegistered = true;
		log("commands registered");
	}

	public void onTick(Object server) {
		if (++counter % config.tick_interval != 0) {
			return;
		}
		enforce(server);
	}

	public void saveConfig() {
		config.save(configPath);
	}

	java.nio.file.Path configFile() {
		return configPath;
	}

	private void enforce(Object server) {
		Object overworld = Compat.overworld(server);
		List<Object> players = Compat.onlinePlayers(server);
		double[] gateSpawn = gateSpawn(overworld);
		long now = System.currentTimeMillis();
		boolean changed = false;
		for (Object player : players) {
			String uuid = Compat.uuidOf(player);
			McontainConfig.Sentence sentence = config.jailed.get(uuid);
			if (sentence != null) {
				McontainConfig.Jail jail = config.jails.get(sentence.jail);
				if (jail == null) {
					config.jailed.remove(uuid);
					changed = true;
					Compat.send(player, "Your jail cell no longer exists. You are free.");
					continue;
				}
				if (!"ADVENTURE".equals(Compat.currentGameMode(player))) {
					Compat.setGameMode(player, "ADVENTURE");
				}
				if (!Compat.sameLevel(player, overworld) || !inside(player, jail.x, jail.y, jail.z, jail.radius)) {
					Compat.teleportPlayer(player, overworld, jail.x, jail.y, jail.z);
				}
				if (sentence.until > 0 && now >= sentence.until) {
					config.jailed.remove(uuid);
					changed = true;
					Compat.send(player, "Your sentence is served. You are free.");
					if ("ADVENTURE".equals(Compat.currentGameMode(player))) {
						Compat.setGameMode(player, "SURVIVAL");
					}
				}
				continue;
			}
			if (!config.gate.enabled || gateSpawn == null) {
				continue;
			}
			boolean verified = config.verified.contains(Compat.nameOf(player).toLowerCase(java.util.Locale.ROOT));
			if (Compat.isOperator(player) && !verified) {
				continue;
			}
			if (!verified) {
				if (!"ADVENTURE".equals(Compat.currentGameMode(player))) {
					Compat.setGameMode(player, "ADVENTURE");
				}
				if (!Compat.sameLevel(player, overworld) || !insideGate(player, gateSpawn[0], gateSpawn[1], gateSpawn[2], config.gate.radius)) {
					Compat.teleportPlayer(player, overworld, gateSpawn[0], gateSpawn[1], gateSpawn[2]);
				}
			} else if ("ADVENTURE".equals(Compat.currentGameMode(player)) && insideGate(player, gateSpawn[0], gateSpawn[1], gateSpawn[2], config.gate.radius)) {
				Compat.setGameMode(player, "SURVIVAL");
			}
		}
		if (changed) {
			saveConfig();
		}
	}

	private double[] gateSpawn(Object overworld) {
		if (!config.gate.enabled) {
			return null;
		}
		if (config.gate.hasPos) {
			return new double[] { config.gate.x, config.gate.y, config.gate.z };
		}
		return Compat.worldSpawn(overworld);
	}

	private static boolean inside(Object player, double x, double y, double z, int radius) {
		double dx = Compat.xOf(player) - x;
		double dy = Compat.yOf(player) - y;
		double dz = Compat.zOf(player) - z;
		double r = radius + 1;
		return dx * dx + dz * dz <= (double) radius * radius && dy * dy <= r * r;
	}

	private static boolean insideGate(Object player, double x, double y, double z, int radius) {
		return inside(player, x, y, z, radius);
	}
}