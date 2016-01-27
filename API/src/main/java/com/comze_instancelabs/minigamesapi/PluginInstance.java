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

	private HashMap<String, Arena> globalPlayers = new HashMap<String, Arena>();
	private HashMap<String, Arena> globalLost = new HashMap<String, Arena>();
	private HashMap<String, Arena> globalArcadeSpectator = new HashMap<String, Arena>();

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
	private boolean achievementGuiEnabled = false;

	private  ArenaScoreboard scoreboardManager;
	private  ArenaLobbyScoreboard scoreboardLobbyManager;
	private  ArenaSetup arenaSetup = new ArenaSetup();

	private int lobbyCountdown = 30;
	private int ingameCountdown = 10;

	private boolean spectatorMoveYLocked = true;
	private boolean useXpBarLevel = true;
	private boolean bloodEffects = true;
	private boolean deadInFakeBedEffects = true;
	private boolean spectatorMode1_8 = true;
	private boolean damageIdentifierEffects = true;
	private boolean colorBackgroundWoolOfSigns;
	private boolean lastManStanding = true;
	private boolean oldReset = false;
	private boolean showClassesWithoutUsagePermission = true;
	private boolean chatEnabled = true;

	public HashMap<String, ArrayList<String>> cachedSignStates = new HashMap<String, ArrayList<String>>();

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
		this.rew = new Rewards(plugin);
		this.stats = new Stats(this, plugin);
		this.sql = new MainSQL(plugin, true);
		this.classes = new Classes(this, plugin);
		this.shop = new Shop(this, plugin);
		this.spectatormanager = new SpectatorManager(plugin);
		this.achievements = new ArenaAchievements(this, plugin);
		this.holograms = new Holograms(this);
		this.setScoreboardManager(new ArenaScoreboard(this, plugin));
		this.setScoreboardLobbyManager(new ArenaLobbyScoreboard(this, plugin));
		this.reloadVariables();
	}

	public PluginInstance(JavaPlugin plugin, ArenasConfig arenasconfig, MessagesConfig messagesconfig, ClassesConfig classesconfig, StatsConfig statsconfig) {
		this(plugin, arenasconfig, messagesconfig, classesconfig, statsconfig, new ArrayList<Arena>());
	}

	public void reloadVariables() {
		this.lobbyCountdown = plugin.getConfig().getInt("config.countdowns.lobby_countdown") + 1;
		this.ingameCountdown = plugin.getConfig().getInt("config.countdowns.ingame_countdown") + 1;
		this.setSpectatorMoveYLocked(plugin.getConfig().getBoolean("config.spectator.spectator_move_y_lock"));
		this.setUseXpBarLevel(plugin.getConfig().getBoolean("config.use_xp_bar_level"));
		this.setBloodEffects(plugin.getConfig().getBoolean("config.effects.blood"));
		this.setDamageIdentifierEffects(plugin.getConfig().getBoolean("config.effects.damage_identifier_holograms"));
		this.setDeadInFakeBedEffects(plugin.getConfig().getBoolean("config.effects.dead_in_fake_bed"));
		this.setColorBackgroundWoolOfSigns(plugin.getConfig().getBoolean("config.color_background_wool_of_signs"));
		this.setSpectatorMode1_8(plugin.getConfig().getBoolean("config.effects.1_8_spectator_mode"));
		this.setLastManStanding(plugin.getConfig().getBoolean("config.last_man_standing_wins"));
		this.setOldReset(plugin.getConfig().getBoolean("config.use_old_reset_method"));
		this.setShowClassesWithoutUsagePermission(plugin.getConfig().getBoolean("config.show_classes_without_usage_permission"));
		this.setChatEnabled(plugin.getConfig().getBoolean("config.chat_enabled"));

		// Cache sign configuration
		for (String state : ArenaState.getAllStateNames()) {
			this.cachedSignStates.put(state, new ArrayList<String>(Arrays.asList(this.messagesconfig.getConfig().getString("signs." + state.toLowerCase() + ".0"), this.messagesconfig.getConfig().getString("signs." + state.toLowerCase() + ".1"), this.messagesconfig.getConfig().getString("signs." + state.toLowerCase() + ".2"), this.messagesconfig.getConfig().getString("signs." + state.toLowerCase() + ".3"))));
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
		return this.arenasconfig;
	}

	public MessagesConfig getMessagesConfig() {
		return this.messagesconfig;
	}

	public ClassesConfig getClassesConfig() {
		return this.classesconfig;
	}

	public StatsConfig getStatsConfig() {
		return this.statsconfig;
	}

	public GunsConfig getGunsConfig() {
		return this.gunsconfig;
	}

	public AchievementsConfig getAchievementsConfig() {
		return this.achievementsconfig;
	}

	public ShopConfig getShopConfig() {
		return this.shopconfig;
	}

	public void setShopConfig(ShopConfig shopconfig) {
		this.shopconfig = shopconfig;
	}

	public HologramsConfig getHologramsConfig() {
		return this.hologramsconfig;
	}

	public HashMap<String, Arena> getGlobalPlayers() {
		return this.globalPlayers;
	}

	public void setGlobalPlayers(HashMap<String, Arena> globalPlayers) {
		this.globalPlayers = globalPlayers;
	}

	public Rewards getRewardsInstance() {
		return this.rew;
	}

	public void setRewardsInstance(Rewards r) {
		this.rew = r;
	}

	public MainSQL getSQLInstance() {
		return this.sql;
	}

	public Stats getStatsInstance() {
		return this.stats;
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
		return this.ingameCountdown;
	}

	public int getLobbyCountdown() {
		return this.lobbyCountdown;
	}

	public ArrayList<Arena> getArenas() {
		return this.arenas;
	}

	public void clearArenas() {
		this.arenas.clear();
	}

	public ArrayList<Arena> addArena(Arena arena) {
		this.arenas.add(arena);
		return this.getArenas();
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
		if (this.arenas.contains(arena)) {
			this.arenas.remove(arena);
			return true;
		}
		return false;
	}

	public void addLoadedArenas(ArrayList<Arena> arenas) {
		this.arenas = arenas;
	}

	public boolean isAchievementGuiEnabled() {
		return this.achievementGuiEnabled;
	}

	public void setAchievementGuiEnabled(boolean achievement_gui_enabled) {
		this.achievementGuiEnabled = achievement_gui_enabled;
	}

	public void reloadAllArenas() {
		for (Arena a : this.getArenas()) {
			if (a != null) {
				String arenaname = a.getInternalName();
				ArenaSetup s = this.getArenaSetup();
				a.init(Util.getSignLocationFromArena(this.plugin, arenaname), Util.getAllSpawns(this.plugin, arenaname), Util.getMainLobby(this.plugin), Util.getComponentForArena(plugin, arenaname, "lobby"), s.getPlayerCount(plugin, arenaname, true), s.getPlayerCount(plugin, arenaname, false), s.getArenaVIP(plugin, arenaname));
				if (a.isSuccessfullyInit()) {
					Util.updateSign(this.plugin, a);
				}
			}
		}
	}

	public void reloadArena(String arenaname) {
		if (Validator.isArenaValid(this.plugin, arenaname)) {
			Arena a = this.getArenaByName(arenaname);
			if (a != null) {
				ArenaSetup s = this.getArenaSetup();
				a.init(Util.getSignLocationFromArena(this.plugin, arenaname), Util.getAllSpawns(this.plugin, arenaname), Util.getMainLobby(this.plugin), Util.getComponentForArena(plugin, arenaname, "lobby"), s.getPlayerCount(plugin, arenaname, true), s.getPlayerCount(plugin, arenaname, false), s.getArenaVIP(plugin, arenaname));
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
		return this.globalLost;
	}

	public void setGlobalLost(HashMap<String, Arena> globalLost) {
		this.globalLost = globalLost;
	}

	public HashMap<String, Arena> getGlobalArcadeSpectator() {
		return globalArcadeSpectator;
	}

	public void setGlobalArcadeSpectator(HashMap<String, Arena> globalArcadeSpectator) {
		this.globalArcadeSpectator = globalArcadeSpectator;
	}

	public ArenaScoreboard getScoreboardManager() {
		return this.scoreboardManager;
	}

	public void setScoreboardManager(ArenaScoreboard scoreboardManager) {
		this.scoreboardManager = scoreboardManager;
	}

	public ArenaLobbyScoreboard getScoreboardLobbyManager() {
		return this.scoreboardLobbyManager;
	}

	public void setScoreboardLobbyManager(ArenaLobbyScoreboard scoreboardLobbyManager) {
		this.scoreboardLobbyManager = scoreboardLobbyManager;
	}

	public ArenaSetup getArenaSetup() {
		return arenaSetup;
	}

	public void setArenaSetup(ArenaSetup arenaSetup) {
		this.arenaSetup = arenaSetup;
	}

	public boolean isLastManStanding() {
		return lastManStanding;
	}

	public void setLastManStanding(boolean lastManStanding) {
		this.lastManStanding = lastManStanding;
	}

	public boolean isDeadInFakeBedEffects() {
		return deadInFakeBedEffects;
	}

	public void setDeadInFakeBedEffects(boolean deadInFakeBedEffects) {
		this.deadInFakeBedEffects = deadInFakeBedEffects;
	}

	public boolean isSpectatorMode1_8() {
		return this.spectatorMode1_8;
	}

	public void setSpectatorMode1_8(boolean spectatorMode1_8) {
		this.spectatorMode1_8 = spectatorMode1_8;
	}

	public boolean isUseXpBarLevel() {
		return this.useXpBarLevel;
	}

	public void setUseXpBarLevel(boolean useXpBarLevel) {
		this.useXpBarLevel = useXpBarLevel;
	}

	public boolean isOldReset() {
		return oldReset;
	}

	public void setOldReset(boolean oldReset) {
		this.oldReset = oldReset;
	}

	public boolean isChatEnabled() {
		return chatEnabled;
	}

	public void setChatEnabled(boolean chatEnabled) {
		this.chatEnabled = chatEnabled;
	}

	public boolean isDamageIdentifierEffects() {
		return damageIdentifierEffects;
	}

	public void setDamageIdentifierEffects(boolean damageIdentifierEffects) {
		this.damageIdentifierEffects = damageIdentifierEffects;
	}

	public boolean isBloodEffects() {
		return bloodEffects;
	}

	public void setBloodEffects(boolean bloodEffects) {
		this.bloodEffects = bloodEffects;
	}

	public boolean isSpectatorMoveYLocked() {
		return spectatorMoveYLocked;
	}

	public void setSpectatorMoveYLocked(boolean spectatorMoveYLocked) {
		this.spectatorMoveYLocked = spectatorMoveYLocked;
	}

	public boolean isShowClassesWithoutUsagePermission() {
		return this.showClassesWithoutUsagePermission;
	}

	public void setShowClassesWithoutUsagePermission(boolean showClassesWithoutUsagePermission) {
		this.showClassesWithoutUsagePermission = showClassesWithoutUsagePermission;
	}

	public boolean isColorBackgroundWoolOfSigns() {
		return this.colorBackgroundWoolOfSigns;
	}

	public void setColorBackgroundWoolOfSigns(boolean colorBackgroundWoolOfSigns) {
		this.colorBackgroundWoolOfSigns = colorBackgroundWoolOfSigns;
	}

}
