package dev.komix.mcontain;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class McontainConfig {

	public static final class Gate {
		public boolean enabled = true;
		public int radius = 16;
		public String world = "minecraft:overworld";
	}

	public static final class Jail {
		public String world = "minecraft:overworld";
		public double x;
		public double y;
		public double z;
		public int radius = 2;
	}

	public static final class Sentence {
		public String jail;
		public long until;
	}

	public Gate gate = new Gate();
	public java.util.Map<String, Jail> jails = new java.util.LinkedHashMap<>();
	public java.util.Map<String, Sentence> jailed = new java.util.HashMap<>();
	public java.util.List<String> verified = new java.util.ArrayList<>();
	public int default_radius = 2;
	public int tick_interval = 20;

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	public static McontainConfig load(Path file) {
		if (Files.isRegularFile(file)) {
			try {
				byte[] bytes = Files.readAllBytes(file);
				String text = new String(bytes, StandardCharsets.UTF_8);
				McontainConfig cfg = GSON.fromJson(text, McontainConfig.class);
				if (cfg != null) {
					defaults(cfg);
					return cfg;
				}
			} catch (Exception e) {
				McontainMod.log("corrupt config, resetting: " + e.getMessage());
			}
		}
		McontainConfig cfg = new McontainConfig();
		defaults(cfg);
		return cfg;
	}

	private static void defaults(McontainConfig cfg) {
		if (cfg.gate == null) {
			cfg.gate = new Gate();
		}
		if (cfg.jails == null) {
			cfg.jails = new java.util.LinkedHashMap<>();
		}
		if (cfg.jailed == null) {
			cfg.jailed = new java.util.HashMap<>();
		}
		if (cfg.verified == null) {
			cfg.verified = new java.util.ArrayList<>();
		}
		if (cfg.tick_interval <= 0) {
			cfg.tick_interval = 20;
		}
		if (cfg.gate.radius <= 0) {
			cfg.gate.radius = 16;
		}
		if (cfg.default_radius <= 0) {
			cfg.default_radius = 2;
		}
	}

	public synchronized void save(Path file) {
		try {
			Path parent = file.getParent();
			if (parent != null) {
				Files.createDirectories(parent);
			}
			Files.write(file, GSON.toJson(this).getBytes(StandardCharsets.UTF_8));
		} catch (IOException e) {
			McontainMod.log("could not save config: " + e.getMessage());
		}
	}
}