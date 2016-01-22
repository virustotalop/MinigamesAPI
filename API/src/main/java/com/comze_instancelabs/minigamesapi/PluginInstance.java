package com.comze_instancelabs.minigamesapi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.bukkit.plugin.java.JavaPlugin;

import com.comze_instancelabs.minigamesapi.achievements.ArenaAchievements;
import com.comze_instancelabs.minigamesapi.config.AchievementsConfig;
import com.comze_instancelabs.minigamesapi.config.ArenasConfig;
import com.comze_instancelabs.minigamesapi.config.ClassesConfig;
import com.comze_instancelabs.minigamesapi.config.GunsConfig;
import com.comze_instancelabs.minigamesapi.config.HologramsConfig;
import com.comze_instancelabs.minigamesapi.config.MessagesConfig;
import com.comze_instancelabs.minigamesapi.config.ShopConfig;
import com.comze_instancelabs.minigamesapi.config.StatsConfig;
import com.comze_instancelabs.minigamesapi.guns.Gun;
import com.comze_instancelabs.minigamesapi.sql.MainSQL;
import com.comze_instancelabs.minigamesapi.statsholograms.Holograms;
import com.comze_instancelabs.minigamesapi.util.AClass;
import com.comze_instancelabs.minigamesapi.util.ArenaLobbyScoreboard;
import com.comze_instancelabs.minigamesapi.util.ArenaScoreboard;
import com.comze_instancelabs.minigamesapi.util.Util;
import com.comze_instancelabs.minigamesapi.util.Validator;

public class PluginInstance {

	public HashMap<String, Arena> globalPlayers = new HashMap<String, Arena>();
	public HashMap<String, Arena> globalLost = new HashMap<String, Arena>();
	public HashMap<String, Arena> global_arcade_spectator = new HashMap<String, Arena>();

	private ArenaListener arenalistener = null;
	private ArenasConfig arenasconfig = null;
	private ClassesConfig classesconfig = null;
	private MessagesConfig messagesconfig = null;
	private StatsConfig statsconfig = null;
	private GunsConfig gunsconfig = null;
	private AchievementsConfig achievementsconfig = null;
	private ShopConfig shopconfig = null;
	private HologramsConfig hologramsconfig = null;
	private JavaPlugin plugin = null;
	private ArrayList<Arena> arenas = new ArrayList<Arena>();
	private HashMap<String, AClass> pclass = new HashMap<String, AClass>();
	private LinkedHashMap<String, AClass> aclasses = new LinkedHashMap<String, AClass>();
	private HashMap<String, Gun> guns = new HashMap<String, Gun>();
	private Rewards rew = null;
	private MainSQL sql = null;
	private Stats stats = null;
	private Classes classes = null;
	private Shop shop = null;
	private SpectatorManager spectatormanager = null;
	private ArenaAchievements achievements = null;
	private Holograms holograms = null;
	private boolean achievement_gui_enabled = false;

	public ArenaScoreboard scoreboardManager;
	public ArenaLobbyScoreboard scoreboardLobbyManager;
	public ArenaSetup arenaSetup = new ArenaSetup();

	int lobby_countdown = 30;
	int ingame_countdown = 10;

	boolean spectator_move_y_lock = true;
	boolean use_xp_bar_level = true;
	boolean blood_effects = true;
	boolean deadInFakeBedEffects = true;
	boolean spectatorMode1_8 = true;
	boolean damage_identifier_effects = true;
	public boolean color_background_wool_of_signs;
	boolean last_man_standing = true;
	boolean old_reset = false;
	public boolean show_classes_without_usage_permission = true;
	public boolean chat_enabled = true;

	public HashMap<String, ArrayList<String>> cached_sign_states = new HashMap<String, ArrayList<String>>();

	public PluginInstance(JavaPlugin plugin, ArenasConfig arenasconfig, MessagesConfig messagesconfig, ClassesConfig classesconfig, StatsConfig statsconfig, ArrayList<Arena> arenas) {
		this.arenasconfig = arenasconfig;
		this.messagesconfig = messagesconfig;
		this.classesconfig = classesconfig;
		this.statsconfig = statsconfig;
		this.gunsconfig = new GunsConfig(plugin, false);
		this.achievementsconfig = new AchievementsConfig(plugin);
		this.shopconfig = new ShopConfig(plugin, false);
		this.hologramsconfig = new HologramsConfig(plugin, false);
		this.arenas = arenas;
		this.plugin = plugin;
		rew = new Rewards(plugin);
		stats = new Stats(this, plugin);
		sql = new MainSQL(plugin, true);
		classes = new Classes(this, plugin);
		shop = new Shop(this, plugin);
		spectatormanager = new SpectatorManager(plugin);
		achievements = new ArenaAchievements(this, plugin);
		holograms = new Holograms(this);
		scoreboardManager = new ArenaScoreboard(this, plugin);
		scoreboardLobbyManager = new ArenaLobbyScoreboard(this, plugin);
		reloadVariables();
	}

	public PluginInstance(JavaPlugin plugin, ArenasConfig arenasconfig, MessagesConfig messagesconfig, ClassesConfig classesconfig, StatsConfig statsconfig) {
		this(plugin, arenasconfig, messagesconfig, classesconfig, statsconfig, new ArrayList<Arena>());
	}

	public void reloadVariables() {
		lobby_countdown = plugin.getConfig().getInt("config.countdowns.lobby_countdown") + 1;
		ingame_countdown = plugin.getConfig().getInt("config.countdowns.ingame_countdown") + 1;
		spectator_move_y_lock = plugin.getConfig().getBoolean("config.spectator.spectator_move_y_lock");
		use_xp_bar_level = plugin.getConfig().getBoolean("config.use_xp_bar_level");
		blood_effects = plugin.getConfig().getBoolean("config.effects.blood");
		damage_identifier_effects = plugin.getConfig().getBoolean("config.effects.damage_identifier_holograms");
		deadInFakeBedEffects = plugin.getConfig().getBoolean("config.effects.dead_in_fake_bed");
		color_background_wool_of_signs = plugin.getConfig().getBoolean("config.color_background_wool_of_signs");
		spectatorMode1_8 = plugin.getConfig().getBoolean("config.effects.1_8_spectator_mode");
		last_man_standing = plugin.getConfig().getBoolean("config.last_man_standing_wins");
		old_reset = plugin.getConfig().getBoolean("config.use_old_reset_method");
		show_classes_without_usage_permission = plugin.getConfig().getBoolean("config.show_classes_without_usage_permission");
		chat_enabled = plugin.getConfig().getBoolean("config.chat_enabled");

		// Cache sign configuration
		for (String state : ArenaState.getAllStateNames()) {
			this.cached_sign_states.put(state, new ArrayList<String>(Arrays.asList(this.messagesconfig.getConfig().getString("signs." + state.toLowerCase() + ".0"), this.messagesconfig.getConfig().getString("signs." + state.toLowerCase() + ".1"), this.messagesconfig.getConfig().getString("signs." + state.toLowerCase() + ".2"), this.messagesconfig.getConfig().getString("signs." + state.toLowerCase() + ".3"))));
		}

	}

	public JavaPlugin getPlugin() {
		return plugin;
	}

	public HashMap<String, AClass> getAClasses() {
		return this.aclasses;
	}

	public HashMap<String, AClass> getPClasses() {
		return this.pclass;
	}

	public void addAClass(String name, AClass a) {
		this.aclasses.put(name, a);
	}

	public void setPClass(String player, AClass a) {
		this.pclass.put(player, a);
	}

	public HashMap<String, Gun> getAllGuns() {
		return this.guns;
	}

	public void addGun(String name, Gun g) {
		this.guns.put(name, g);
	}

	public ArenasConfig getArenasConfig() {
		return arenasconfig;
	}

	public MessagesConfig getMessagesConfig() {
		return messagesconfig;
	}

	public ClassesConfig getClassesConfig() {
		return classesconfig;
	}

	public StatsConfig getStatsConfig() {
		return statsconfig;
	}

	public GunsConfig getGunsConfig() {
		return gunsconfig;
	}

	public AchievementsConfig getAchievementsConfig() {
		return achievementsconfig;
	}

	public ShopConfig getShopConfig() {
		return shopconfig;
	}

	public void setShopConfig(ShopConfig shopconfig) {
		this.shopconfig = shopconfig;
	}

	public HologramsConfig getHologramsConfig() {
		return hologramsconfig;
	}

	public HashMap<String, Arena> getGlobalPlayers() {
		return globalPlayers;
	}

	public void setGlobalPlayers(HashMap<String, Arena> globalPlayers) {
		this.globalPlayers = globalPlayers;
	}

	public Rewards getRewardsInstance() {
		return rew;
	}

	public void setRewardsInstance(Rewards r) {
		rew = r;
	}

	public MainSQL getSQLInstance() {
		return sql;
	}

	public Stats getStatsInstance() {
		return stats;
	}

	public ArenaListener getArenaListener() {
		return this.arenalistener;
	}

	public void setArenaListener(ArenaListener al) {
		this.arenalistener = al;
	}

	public Classes getClassesHandler() {
		return this.classes;
	}

	public void setClassesHandler(Classes c) {
		this.classes = c;
	}

	public Shop getShopHandler() {
		return this.shop;
	}

	public SpectatorManager getSpectatorManager() {
		return this.spectatormanager;
	}

	public void setSpectatorManager(SpectatorManager s) {
		this.spectatormanager = s;
	}

	public ArenaAchievements getArenaAchievements() {
		return this.achievements;
	}

	public Holograms getHologramsHandler() {
		return this.holograms;
	}

	public int getIngameCountdown() {
		return this.ingame_countdown;
	}

	public int getLobbyCountdown() {
		return this.lobby_countdown;
	}

	public ArrayList<Arena> getArenas() {
		return arenas;
	}

	public void clearArenas() {
		arenas.clear();
	}

	public ArrayList<Arena> addArena(Arena arena) {
		arenas.add(arena);
		return getArenas();
	}

	public Arena getArenaByName(String arenaname) {
		for (Arena a : getArenas()) {
			if (a.getInternalName().equalsIgnoreCase(arenaname)) {
				return a;
			}
		}
		return null;
	}

	public Arena removeArenaByName(String arenaname) {
		Arena torem = null;
		for (Arena a : getArenas()) {
			if (a.getInternalName().equalsIgnoreCase(arenaname)) {
				torem = a;
			}
		}
		if (torem != null) {
			removeArena(torem);
		}
		return null;
	}

	public boolean removeArena(Arena arena) {
		if (arenas.contains(arena)) {
			arenas.remove(arena);
			return true;
		}
		return false;
	}

	public void addLoadedArenas(ArrayList<Arena> arenas) {
		this.arenas = arenas;
	}

	public boolean isAchievementGuiEnabled() {
		return achievement_gui_enabled;
	}

	public void setAchievementGuiEnabled(boolean achievement_gui_enabled) {
		this.achievement_gui_enabled = achievement_gui_enabled;
	}

	public void reloadAllArenas() {
		for (Arena a : this.getArenas()) {
			if (a != null) {
				String arenaname = a.getInternalName();
				ArenaSetup s = this.arenaSetup;
				a.init(Util.getSignLocationFromArena(plugin, arenaname), Util.getAllSpawns(plugin, arenaname), Util.getMainLobby(plugin), Util.getComponentForArena(plugin, arenaname, "lobby"), s.getPlayerCount(plugin, arenaname, true), s.getPlayerCount(plugin, arenaname, false), s.getArenaVIP(plugin, arenaname));
				if (a.isSuccessfullyInit()) {
					Util.updateSign(plugin, a);
				}
			}
		}
	}

	public void reloadArena(String arenaname) {
		if (Validator.isArenaValid(plugin, arenaname)) {
			Arena a = this.getArenaByName(arenaname);
			if (a != null) {
				ArenaSetup s = this.arenaSetup;
				a.init(Util.getSignLocationFromArena(plugin, arenaname), Util.getAllSpawns(plugin, arenaname), Util.getMainLobby(plugin), Util.getComponentForArena(plugin, arenaname, "lobby"), s.getPlayerCount(plugin, arenaname, true), s.getPlayerCount(plugin, arenaname, false), s.getArenaVIP(plugin, arenaname));
			}
		}
	}

	public boolean containsGlobalPlayer(String playername) {
		return this.globalPlayers.containsKey(playername);
	}

	public boolean containsGlobalLost(String playername) {
		return this.globalLost.containsKey(playername);
	}

	public Arena getArenaByGlobalPlayer(String playername) {
		if (containsGlobalPlayer(playername)) {
			return this.globalPlayers.get(playername);
		} else {
			return null;
		}
	}

	public HashMap<String, Arena> getGlobalLost() {
		return globalLost;
	}

	public void setGlobalLost(HashMap<String, Arena> globalLost) {
		this.globalLost = globalLost;
	}

}
