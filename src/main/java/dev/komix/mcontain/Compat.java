package dev.komix.mcontain;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class Compat {

	private static final String GAME_TYPE = "net.minecraft.world.level.GameType";
	private static final String COMPONENT = "net.minecraft.network.chat.Component";

	private Compat() {
	}

	private static Class<?> clazz(String name) {
		try {
			return Class.forName(name);
		} catch (Throwable t) {
			return null;
		}
	}

	static Object invoke(Object target, String name, Object... args) {
		if (target == null) {
			return null;
		}
		for (Method m : methodsIncludingSuper(target.getClass(), name)) {
			if (m.getParameterCount() != args.length) {
				continue;
			}
			Object r = call(m, target, args);
			if (r != null || isVoid(m)) {
				return r;
			}
		}
		return null;
	}

	private static boolean isVoid(Method m) {
		return m.getReturnType() == Void.TYPE;
	}

	private static List<Method> methodsIncludingSuper(Class<?> type, String name) {
		List<Method> out = new ArrayList<>();
		for (Class<?> c = type; c != null && c != Object.class; c = c.getSuperclass()) {
			try {
				for (Method m : c.getDeclaredMethods()) {
					if (m.getName().equals(name)) {
						out.add(m);
					}
				}
			} catch (Throwable t) {
				// continue
			}
		}
		return out;
	}

	private static Object call(Method m, Object target, Object... args) {
		try {
			m.setAccessible(true);
			return m.invoke(target, args);
		} catch (Throwable t) {
			return null;
		}
	}

	private static Object field(Object target, String name) {
		if (target == null) {
			return null;
		}
		for (Class<?> c = target.getClass(); c != null && c != Object.class; c = c.getSuperclass()) {
			try {
				Field f = c.getDeclaredField(name);
				f.setAccessible(true);
				return f.get(target);
			} catch (Throwable t) {
				// continue
			}
		}
		return null;
	}

	private static Object staticField(String className, String name) {
		Class<?> c = clazz(className);
		if (c == null) {
			return null;
		}
		try {
			Field f = c.getDeclaredField(name);
			f.setAccessible(true);
			return f.get(null);
		} catch (Throwable t) {
			return null;
		}
	}

	private static Double toDouble(Object value) {
		return value instanceof Number ? ((Number) value).doubleValue() : null;
	}

	@SuppressWarnings("unchecked")
	public static List<Object> onlinePlayers(Object server) {
		Object playerList = invoke(server, "getPlayerList");
		if (playerList == null) {
			return new ArrayList<>();
		}
		Object players = invoke(playerList, "getPlayers");
		if (players instanceof List) {
			return (List<Object>) players;
		}
		return new ArrayList<>();
	}

	public static String nameOf(Object player) {
		Object component = invoke(player, "getName");
		String name = (String) invoke(component, "getString");
		return name == null ? String.valueOf(player) : name;
	}

	public static String uuidOf(Object player) {
		Object uuid = invoke(player, "getUUID");
		if (uuid != null) {
			return uuid.toString();
		}
		Object uuidString = invoke(player, "getStringUUID");
		return uuidString == null ? "" : uuidString.toString();
	}

	public static boolean isOperator(Object player) {
		Boolean p = (Boolean) invoke(player, "hasPermissions", 4);
		if (Boolean.TRUE.equals(p)) {
			return true;
		}
		Object level = invoke(player, "getPermissionLevel");
		return level instanceof Integer && (Integer) level >= 2;
	}

	public static double xOf(Object player) {
		Double v = toDouble(invoke(player, "getX"));
		return v == null ? 0.0 : v;
	}

	public static double yOf(Object player) {
		Double v = toDouble(invoke(player, "getY"));
		return v == null ? 0.0 : v;
	}

	public static double zOf(Object player) {
		Double v = toDouble(invoke(player, "getZ"));
		return v == null ? 0.0 : v;
	}

	public static Object playerLevel(Object player) {
		Object level = field(player, "level");
		if (level != null) {
			return level;
		}
		level = invoke(player, "getLevel");
		if (level != null) {
			return level;
		}
		return invoke(player, "getCommandSenderWorld");
	}

	public static Object overworld(Object server) {
		Object key = staticField("net.minecraft.world.level.Level", "OVERWORLD");
		Object level = invoke(server, "getLevel", key);
		if (level != null) {
			return level;
		}
		Object oldKey = staticField("net.minecraft.world.level.World", "OVERWORLD");
		return invoke(server, "getWorld", oldKey);
	}

	public static boolean sameLevel(Object player, Object level) {
		return playerLevel(player) == level;
	}

	public static double[] worldSpawn(Object level) {
		Object pos = invoke(level, "getSharedSpawnPos");
		if (pos == null) {
			pos = invoke(level, "getSpawnPos");
		}
		if (pos != null) {
			double[] xyz = posXyz(pos);
			if (xyz != null) {
				return xyz;
			}
		}
		Object levelData = invoke(level, "getLevelData");
		if (levelData == null) {
			return null;
		}
		Object respawnData = invoke(levelData, "getRespawnData");
		if (respawnData == null) {
			Double x = toDouble(invoke(levelData, "getXSpawn"));
			Double y = toDouble(invoke(levelData, "getYSpawn"));
			Double z = toDouble(invoke(levelData, "getZSpawn"));
			if (x != null && y != null && z != null) {
				return new double[] { x, y, z };
			}
			return null;
		}
		Object respawnPos = invoke(respawnData, "pos");
		return posXyz(respawnPos);
	}

	private static double[] posXyz(Object pos) {
		if (pos == null) {
			return null;
		}
		Double x = toDouble(invoke(pos, "getX"));
		Double y = toDouble(invoke(pos, "getY"));
		Double z = toDouble(invoke(pos, "getZ"));
		if (x != null && y != null && z != null) {
			return new double[] { x, y, z };
		}
		x = toDouble(field(pos, "x"));
		y = toDouble(field(pos, "y"));
		z = toDouble(field(pos, "z"));
		if (x != null && y != null && z != null) {
			return new double[] { x, y, z };
		}
		return null;
	}

	public static boolean setGameMode(Object player, String wanted) {
		Class<?> gameTypeClass = clazz(GAME_TYPE);
		if (gameTypeClass == null || player == null) {
			return false;
		}
		Object value;
		try {
			@SuppressWarnings("rawtypes")
			Class rawType = gameTypeClass;
			value = Enum.valueOf(rawType, wanted);
		} catch (Throwable t) {
			return false;
		}
		if (invoke(player, "setGameMode", value) != null) {
			return true;
		}
		Object mode = field(player, "gameMode");
		if (mode == null) {
			mode = field(player, "interactionManager");
		}
		return mode != null && invoke(mode, "setGameMode", value) != null;
	}

	public static String currentGameMode(Object player) {
		Object mode = invoke(player, "gameMode");
		if (mode == null) {
			mode = field(player, "gameMode");
			mode = mode == null ? null : invoke(mode, "getGameMode");
		}
		if (mode == null) {
			Object manager = field(player, "interactionManager");
			mode = manager == null ? null : invoke(manager, "getGameMode");
		}
		String name = (String) invoke(mode, "name");
		return name == null ? "" : name;
	}

	private static Object component(String message) {
		Class<?> componentClass = clazz(COMPONENT);
		if (componentClass != null) {
			try {
				Method literal = componentClass.getMethod("literal", String.class);
				Object comp = literal.invoke(null, message);
				if (comp != null) {
					return comp;
				}
			} catch (Throwable t) {
				// continue
			}
		}
		try {
			Class<?> textClass = clazz("net.minecraft.network.chat.TextComponent");
			Constructor<?> ctor = textClass == null ? null : textClass.getConstructor(String.class);
			return ctor == null ? null : ctor.newInstance(message);
		} catch (Throwable t) {
			return null;
		}
	}

	public static void send(Object player, String message) {
		Object comp = component(message);
		if (comp == null) {
			return;
		}
		if (invoke(player, "sendSystemMessage", comp) != null) {
			return;
		}
		if (invoke(player, "sendMessage", comp, (Object) null) != null) {
			return;
		}
		invoke(player, "sendMessage", comp, UUID.randomUUID());
	}

	public static void broadcast(Object server, String message) {
		for (Object player : onlinePlayers(server)) {
			send(player, message);
		}
	}

	public static Object commandPlayer(Object source) {
		Object player = invoke(source, "getPlayer");
		if (player == null) {
			player = invoke(source, "getPlayerOrException");
		}
		return player;
	}

	public static boolean hasPermission(Object source) {
		Boolean ok = (Boolean) invoke(source, "hasPermission", 2);
		return Boolean.TRUE.equals(ok);
	}

	public static boolean teleportPlayer(Object player, Object level, double x, double y, double z) {
		if (player == null) {
			return false;
		}
		return invokeVoid(player, "teleportTo", 3, x, y, z)
				|| invokeVoid(player, "teleportTo", 6, level, x, y, z, 0.0f, 0.0f);
	}

	private static boolean invokeVoid(Object target, String name, int arity, Object... args) {
		if (target == null) {
			return false;
		}
		for (Method m : methodsIncludingSuper(target.getClass(), name)) {
			if (m.getParameterCount() != arity) {
				continue;
			}
			if (call(m, target, args) != null || isVoid(m)) {
				return true;
			}
		}
		return false;
	}
}