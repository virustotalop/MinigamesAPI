package com.comze_instancelabs.minigamesapi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import com.comze_instancelabs.minigamesapi.arcade.ArcadeInstance;
import com.comze_instancelabs.minigamesapi.events.ArenaStartEvent;
import com.comze_instancelabs.minigamesapi.events.ArenaStartedEvent;
import com.comze_instancelabs.minigamesapi.events.ArenaStopEvent;
import com.comze_instancelabs.minigamesapi.events.PlayerJoinLobbyEvent;
import com.comze_instancelabs.minigamesapi.events.PlayerLeaveArenaEvent;
import com.comze_instancelabs.minigamesapi.util.BungeeUtil;
import com.comze_instancelabs.minigamesapi.util.Cuboid;
import com.comze_instancelabs.minigamesapi.util.IconMenu;
import com.comze_instancelabs.minigamesapi.util.Util;
import com.comze_instancelabs.minigamesapi.util.Validator;

public class Arena {

	private PluginInstance pli;
	private ArcadeInstance ai;
	
	private JavaPlugin plugin;
	
	private boolean isArcadeMain = false;
	
	private boolean isSuccessfullyInitialized = false;
	
	private ArrayList<Location> spawns = new ArrayList<Location>();
	
	private HashMap<String, Location> pSpawnLoc = new HashMap<String, Location>();
	
	private HashMap<String, String> lastdamager = new HashMap<String, String>();
	
	private HashMap<String, Integer> tempKillCount = new HashMap<String, Integer>();
	
	private HashMap<String, Integer> tempDeathCount = new HashMap<String, Integer>();
	
	private Location mainLobby;
	private Location waitingLobby;
	
	private Location specSpawn;
	private Location signLoc;
	
	private int maxPlayers;
	private int minPlayers;
	
	private boolean vipArena;
	
	//private String permissionNode; -> Not used
	
	private ArrayList<String> players = new ArrayList<String>();
	private ArrayList<String> tempPlayers = new ArrayList<String>();
	
	private ArenaType type = ArenaType.DEFAULT;
	
	private ArenaState currentstate = ArenaState.JOIN;
	
	private String name = "mainarena";
	
	private String displayname = "mainarena";

	private Arena currentarena;
	
	private boolean started = false;
	private boolean startedIngameCountdown = false;
	private boolean showArenascoreboard = true;
	private boolean alwaysPvP = false;
	private SmartReset sr = null;
	
	
	private Cuboid boundaries;
	private Cuboid lobbyBoundaries;
	private Cuboid specBoundaries;
	
	private boolean tempCountdown = true;
	private boolean skipJoinLobby = false;
	
	private int currentspawn = 0;
	private int globalCoinMultiplier = 1;
	
	private BukkitTask maximumGameTime;
	
	private ArrayList<ItemStack> globalDrops = new ArrayList<ItemStack>();

	/**
	 * Creates a normal singlespawn arena
	 * 
	 * @param plugin
	 *            JavaPlugin the arena belongs to
	 * @param name
	 *            name of the arena
	 */
	public Arena(JavaPlugin plugin, String name) 
	{
		currentarena = this;
		this.plugin = plugin;
		this.name = name;
		sr = new SmartReset(this);
		this.pli = MinigamesAPI.getAPI().getPluginInstance(plugin);
	}

	/**
	 * Creates an arena of given arenatype
	 * 
	 * @param name
	 *            name of the arena
	 * @param type
	 *            arena type
	 */
	public Arena(JavaPlugin plugin, String name, ArenaType type) {
		this(plugin, name);
		this.type = type;
	}

	// This is for loading existing arenas
	public void init(Location signloc, ArrayList<Location> spawns, Location mainlobby, Location waitinglobby, int max_players, int min_players, boolean viparena) {
		this.signLoc = signloc;
		this.spawns = spawns;
		this.mainLobby = mainlobby;
		this.waitingLobby = waitinglobby;
		this.vipArena = viparena;
		this.minPlayers = min_players;
		this.maxPlayers = max_players;
		this.showArenascoreboard = pli.getArenaSetup().getShowScoreboard(plugin, this.getInternalName());
		isSuccessfullyInitialized = true;
		if (Util.isComponentForArenaValid(plugin, this.getInternalName(), "bounds.low") && Util.isComponentForArenaValid(plugin, this.getInternalName(), "bounds.high")) {
			try {
				Location lowBoundary = Util.getComponentForArena(plugin, this.getInternalName(), "bounds.low");
				Location highBoundary = Util.getComponentForArena(plugin, this.getInternalName(), "bounds.high");
				if (lowBoundary != null && highBoundary != null) {
					this.setBoundaries(new Cuboid(lowBoundary, highBoundary));
				} else {
					plugin.getServer().getConsoleSender().sendMessage(ChatColor.RED + "The boundaries of an arena appear to be invalid (missing world?), please fix! Arena: " + this.getInternalName());
				}
			} catch (Exception e) {
				plugin.getServer().getConsoleSender().sendMessage(ChatColor.RED + "Failed to save arenas as you forgot to set boundaries or they could not be found. This will lead to errors later, please fix your setup. " + e.getMessage());
				isSuccessfullyInitialized = false;
			}
		}
		if (Util.isComponentForArenaValid(plugin, this.getInternalName(), "lobbybounds.bounds.low") && Util.isComponentForArenaValid(this.plugin, this.getInternalName(), "lobbybounds.bounds.high")) {
			try {
				this.setLobbyBoundaries(new Cuboid(Util.getComponentForArena(this.plugin, this.getInternalName(), "lobbybounds.bounds.low"), Util.getComponentForArena(plugin, this.getInternalName(), "lobbybounds.bounds.high")));
			} catch (Exception e) {
				isSuccessfullyInitialized = false;
			}
		}
		if (Util.isComponentForArenaValid(plugin, this.getInternalName(), "specbounds.bounds.low") && Util.isComponentForArenaValid(this.plugin, this.getInternalName(), "specbounds.bounds.high")) {
			try {
				this.setSpecBoundaries(new Cuboid(Util.getComponentForArena(this.plugin, this.getInternalName(), "specbounds.bounds.low"), Util.getComponentForArena(this.plugin, this.getInternalName(), "specbounds.bounds.high")));
			} catch (Exception e) {
				isSuccessfullyInitialized = false;
			}
		}

		if (Util.isComponentForArenaValid(plugin, this.getInternalName(), "specspawn")) {
			this.specSpawn = Util.getComponentForArena(this.plugin, this.getInternalName(), "specspawn");
		}

		String path = "arenas." + name + ".displayname";
		if (pli.getArenasConfig().getConfig().isSet(path)) {
			this.displayname = ChatColor.translateAlternateColorCodes('&', pli.getArenasConfig().getConfig().getString("arenas." + name + ".displayname"));
		} else {
			pli.getArenasConfig().getConfig().set(path, name);
			pli.getArenasConfig().saveConfig();
			this.displayname = name;
		}

	}

	// This is for loading existing arenas
	@Deprecated
	public Arena initArena(Location signloc, ArrayList<Location> spawn, Location mainlobby, Location waitinglobby, int max_players, int min_players, boolean viparena) {
		this.init(signloc, spawn, mainlobby, waitinglobby, max_players, min_players, viparena);
		return this;
	}

	public Arena getArena() {
		return this;
	}

	public void setBoundaries(Cuboid boundaries) {
		this.boundaries = boundaries;
	}

	public SmartReset getSmartReset() {
		return this.sr;
	}

	public boolean getShowScoreboard() {
		return this.showArenascoreboard;
	}

	public boolean getAlwaysPvP() {
		return this.alwaysPvP;
	}

	public void setAlwaysPvP(boolean t) {
		this.alwaysPvP = t;
	}

	public Location getSignLocation() {
		return this.signLoc;
	}

	public void setSignLocation(Location l) {
		this.signLoc = l;
	}

	public ArrayList<Location> getSpawns() {
		return this.spawns;
	}

	public Cuboid getBoundaries() {
		return this.boundaries;
	}

	public Cuboid getLobbyBoundaries() {
		return this.lobbyBoundaries;
	}

	public Cuboid getSpecBoundaries() {
		return this.specBoundaries;
	}

	public String getInternalName() {
		return name;
	}

	public String getDisplayName() {
		return displayname;
	}

	/**
	 * Please use getInternalName() for the internal name and getDisplayName() for the optional displayname
	 * 
	 * @return Internal name of arena (same as getInternalName())
	 */
	@Deprecated
	public String getName() {
		return name;
	}

	public int getMaxPlayers() {
		return this.maxPlayers;
	}
	
	public Location getMainLobby() {
		return this.mainLobby;
	}

	public int getMinPlayers() {
		return this.minPlayers;
	}

	public void setMinPlayers(int i) {
		this.minPlayers = i;
	}

	public void setMaxPlayers(int i) {
		this.maxPlayers = i;
	}

	public boolean isVIPArena() {
		return this.vipArena;
	}

	public void setVIPArena(boolean t) {
		this.vipArena = t;
	}

	public Location getWaitingLobby() {
		return this.waitingLobby;
	}
	
	public ArrayList<String> getAllPlayers() {
		return this.players;
	}
	
	public HashMap<String, Location> getpSpawnLoc() {
		return pSpawnLoc;
	}

	public void setLobbyBoundaries(Cuboid lobbyBoundaries) {
		this.lobbyBoundaries = lobbyBoundaries;
	}

	public void setSpecBoundaries(Cuboid specBoundaries) {
		this.specBoundaries = specBoundaries;
	}

	public void setPlayerSpawnLoc(HashMap<String, Location> pSpawnLoc) {
		this.pSpawnLoc = pSpawnLoc;
	}

	public boolean containsPlayer(String playername) {
		return players.contains(playername);
	}

	/**
	 * Please do not use this function to add players
	 * 
	 * @param playername
	 * @return
	 */
	@Deprecated
	public boolean addPlayer(String playername) {
		return players.add(playername);
	}

	/**
	 * Please do not use this function to remove players
	 * 
	 * @param playername
	 * @return
	 */
	@Deprecated
	public boolean removePlayer(String playername) {
		return players.remove(playername);
	}

	public ArenaState getArenaState() {
		return this.currentstate;
	}

	public void setArenaState(ArenaState s) {
		this.currentstate = s;
	}

	public ArenaType getArenaType() {
		return this.type;
	}

	/**
	 * Joins the waiting lobby of an arena
	 * 
	 * @param playername
	 *            the playername
	 */
	public void joinPlayerLobby(String playername) {
		if (this.getArenaState() != ArenaState.JOIN && this.getArenaState() != ArenaState.STARTING) {
			// arena ingame or restarting
			return;
		}
		if (!this.pli.getArenaSetup().getArenaEnabled(this.plugin, this.getInternalName())) {
			Util.sendMessage(this.plugin, Bukkit.getPlayer(playername), this.pli.getMessagesConfig().arenaDisabled);
			return;
		}
		if (this.pli.containsGlobalPlayer(playername)) {
			Util.sendMessage(this.plugin, Bukkit.getPlayer(playername), this.pli.getMessagesConfig().alreadyInArena);
			return;
		}
		if (ai == null && this.isVIPArena()) {
			if (Validator.isPlayerOnline(playername)) {
				if (!Bukkit.getPlayer(playername).hasPermission("arenas." + this.getInternalName()) && !Bukkit.getPlayer(playername).hasPermission("arenas.*")) {
					Util.sendMessage(this.plugin, Bukkit.getPlayer(playername), this.pli.getMessagesConfig().noPermToJoinArena.replaceAll("<arena>", this.getInternalName()));
					return;
				}
			}
		}
		if (ai == null && this.getAllPlayers().size() > this.maxPlayers - 1) {
			// arena full

			// if player vip -> kick someone and continue
			MinigamesAPI.getAPI().getLogger().log(Level.INFO, playername + " is vip: " + Bukkit.getPlayer(playername).hasPermission("arenas.*"));
			if (!Bukkit.getPlayer(playername).hasPermission("arenas." + this.getInternalName()) && !Bukkit.getPlayer(playername).hasPermission("arenas.*")) {
				return;
			} else {
				// player has vip
				boolean noOneFound = true;
				ArrayList<String> temp = new ArrayList<String>(this.getAllPlayers());
				for (String p : temp) {
					if (Validator.isPlayerOnline(p)) {
						if (!Bukkit.getPlayer(p).hasPermission("arenas." + this.getInternalName()) && !Bukkit.getPlayer(p).hasPermission("arenas.*")) {
							this.leavePlayer(p, false, true);
							Bukkit.getPlayer(p).sendMessage(this.pli.getMessagesConfig().you_got_kicked_because_vip_joined);
							noOneFound = false;
							break;
						}
					}
				}
				if (noOneFound) {
					// apparently everyone is vip, can't join
					return;
				}
			}
		}

		if (MinigamesAPI.getAPI().globalParty.containsKey(playername)) {
			Party party = MinigamesAPI.getAPI().globalParty.get(playername);
			int playersize = party.getPlayers().size() + 1;
			if (this.getAllPlayers().size() + playersize > this.maxPlayers) {
				Bukkit.getPlayer(playername).sendMessage(MinigamesAPI.getAPI().partymessages.partyTooBigToJoin);
				return;
			} else {
				for (String pl : party.getPlayers()) {
					if (Validator.isPlayerOnline(pl)) {
						boolean cont = true;
						for (PluginInstance pluginInstance : MinigamesAPI.getAPI().pluginInstances.values()) {
							if (pluginInstance.containsGlobalPlayer(pl)) {
								cont = false;
							}
						}
						if (cont) {
							this.joinPlayerLobby(pl);
						}
					}
				}
			}
		}

		if (this.getAllPlayers().size() == this.maxPlayers - 1) {
			if (this.currentlobbycount > 16 && this.getArenaState() == ArenaState.STARTING) {
				this.currentlobbycount = 16;
			}
		}
		pli.getGlobalPlayers().put(playername, this);
		this.players.add(playername);

		if (Validator.isPlayerValid(plugin, playername, this)) {
			final Player p = Bukkit.getPlayer(playername);
			final ArenaPlayer ap = ArenaPlayer.getPlayerInstance(playername);
			Bukkit.getServer().getPluginManager().callEvent(new PlayerJoinLobbyEvent(p, plugin, this));
			Util.sendMessage(this.plugin, p, pli.getMessagesConfig().you_joined_arena.replaceAll("<arena>", this.getDisplayName()));
			Util.sendMessage(this.plugin, p, pli.getMessagesConfig().minigame_description);
			if (this.pli.getArenasConfig().getConfig().isSet("arenas." + this.getInternalName() + ".author")) {
				Util.sendMessage(this.plugin, p, this.pli.getMessagesConfig().author_of_the_map.replaceAll("<arena>", this.getDisplayName()).replaceAll("<author>", pli.getArenasConfig().getConfig().getString("arenas." + this.getInternalName() + ".author")));
			}
			if (this.pli.getArenasConfig().getConfig().isSet("arenas." + this.getInternalName() + ".description")) {
				Util.sendMessage(this.plugin, p, this.pli.getMessagesConfig().description_of_the_map.replaceAll("<arena>", this.getDisplayName()).replaceAll("<description>", pli.getArenasConfig().getConfig().getString("arenas." + this.getInternalName() + ".description")));
			}

			Bukkit.getScheduler().runTaskLater(this.getPlugin(), new Runnable() {
				public void run() {
					try {
						if (p != null) {
							pli.getHologramsHandler().sendAllHolograms(p);
						}
					} catch (Exception e) {
						System.out.println("Failed playing hologram: " + e.getMessage());
						if (MinigamesAPI.debug) {
							e.printStackTrace();
						}
					}
				}
			}, 10L);

			for (String p_ : this.getAllPlayers()) {
				if (Validator.isPlayerOnline(p_) && !p_.equalsIgnoreCase(p.getName())) {
					Player p__ = Bukkit.getPlayer(p_);
					int count = this.getAllPlayers().size();
					int maxcount = this.getMaxPlayers();
					Util.sendMessage(plugin, p__, pli.getMessagesConfig().broadcast_player_joined.replaceAll("<player>", p.getName()).replace("<count>", Integer.toString(count)).replace("<maxcount>", Integer.toString(maxcount)));
				}
			}
			Util.updateSign(plugin, this);

			if (ai == null && !this.isArcadeMain()) {
				this.setSkipJoinLobby(plugin.getConfig().getBoolean("config.countdowns.skip_lobby"));
			}

			final Arena a = this;
			ap.setInventories(p.getInventory().getContents(), p.getInventory().getArmorContents());
			if (this.getArenaType() == ArenaType.JUMPNRUN) {
				Util.teleportPlayerFixed(p, this.spawns.get(currentspawn));
				if (this.currentspawn < this.spawns.size() - 1) {
					this.currentspawn++;
				}
				Util.clearInv(p);
				ap.setOriginalGamemode(p.getGameMode());
				ap.setOriginalXplvl(p.getLevel());
				p.setGameMode(GameMode.SURVIVAL);
				p.setHealth(20D);
				return;
			} else {
				if (getStartedIngameCountdown()) {
					this.pli.getScoreboardLobbyManager().removeScoreboard(this.getInternalName(), p);
					Util.teleportAllPlayers(this.currentarena.getArena().getAllPlayers(), this.currentarena.getArena().getSpawns());
					p.setFoodLevel(5);
					p.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 9999999, -7)); // -5
					Bukkit.getScheduler().runTaskLater(MinigamesAPI.getAPI(), new Runnable() {
						public void run() {
							p.setWalkSpeed(0.0F);
						}
					}, 1L);
					Bukkit.getScheduler().runTaskLater(MinigamesAPI.getAPI(), new Runnable() {
						public void run() {
							Util.clearInv(p);
						}
					}, 10L);
					ap.setOriginalXplvl(p.getLevel());
					Bukkit.getScheduler().runTaskLater(MinigamesAPI.getAPI(), new Runnable() {
						public void run() {
							if (a.getArenaState() != ArenaState.INGAME) {
								Util.giveLobbyItems(plugin, p);
							}
							ap.setOriginalGamemode(p.getGameMode());
							p.setGameMode(GameMode.SURVIVAL);
						}
					}, 15L);
					this.pli.getScoreboardManager().updateScoreboard(this.plugin, this);
					return;
				} else {
					this.pli.getScoreboardLobbyManager().updateScoreboard(this.plugin, this);
					if (!getSkipJoinLobby()) {
						Util.teleportPlayerFixed(p, this.waitingLobby);
						Bukkit.getScheduler().runTaskLater(MinigamesAPI.getAPI(), new Runnable() {
							public void run() {
								p.setHealth(20D);
							}
						}, 2L);
					} else {
						Util.teleportAllPlayers(this.currentarena.getArena().getAllPlayers(), this.currentarena.getArena().getSpawns());
					}
				}
			}
			Bukkit.getScheduler().runTaskLater(MinigamesAPI.getAPI(), new Runnable() {
				public void run() {
					Util.clearInv(p);
				}
			}, 10L);
			ap.setOriginalXplvl(p.getLevel());
			Bukkit.getScheduler().runTaskLater(MinigamesAPI.getAPI(), new Runnable() {
				public void run() {
					if (a.getArenaState() != ArenaState.INGAME) {
						Util.giveLobbyItems(plugin, p);
					}
					ap.setOriginalGamemode(p.getGameMode());
					p.setGameMode(GameMode.SURVIVAL);
					p.setHealth(20D);
				}
			}, 15L);
			if (!this.getSkipJoinLobby()) {
				if (this.ai == null && this.getAllPlayers().size() > this.getMinPlayers() - 1) {
					this.startLobby(this.tempCountdown);
				} else if (ai != null) {
					this.startLobby(this.tempCountdown);
				}
			} else {
				if (this.ai == null && !this.isArcadeMain() && this.getAllPlayers().size() > this.getMinPlayers() - 1) {
					this.startLobby(false);
				}
			}
		}
	}

	/**
	 * Primarily used for ArcadeInstance to join a waiting lobby without countdown
	 * 
	 * @param playername
	 * @param countdown
	 */
	public void joinPlayerLobby(String playername, boolean countdown) {
		this.tempCountdown = countdown;
		joinPlayerLobby(playername);
	}

	/**
	 * Joins the waiting lobby of an arena
	 * 
	 * @param playername
	 *            the playername
	 * @param ai
	 *            the ArcadeInstance
	 */
	public void joinPlayerLobby(String playername, ArcadeInstance ai, boolean countdown, boolean skip_lobby) {
		this.setSkipJoinLobby(skip_lobby);
		this.ai = ai;
		joinPlayerLobby(playername, countdown); // join playerlobby without lobby countdown
	}

	/**
	 * Leaves the current arena, won't do anything if not present in any arena
	 * 
	 * @param playername
	 * @param fullLeave
	 *            Determines if player left only minigame or the server
	 */
	@Deprecated
	public void leavePlayer(final String playername, boolean fullLeave) {
		this.leavePlayerRaw(playername, fullLeave);
	}

	public void leavePlayer(final String playername, boolean fullLeave, boolean endofGame) {
		if (!endofGame) {
			ArenaPlayer ap = ArenaPlayer.getPlayerInstance(playername);
			ap.setNoReward(true);
		}

		this.leavePlayer(playername, fullLeave);

		if (!endofGame) {
			if (this.getAllPlayers().size() < 2) {
				if (this.getArenaState() != ArenaState.JOIN) {
					if (this.getArenaState() == ArenaState.STARTING && !getStartedIngameCountdown()) {
						// cancel starting
						this.setArenaState(ArenaState.JOIN);
						Util.updateSign(this.plugin, this);
						try {
							Bukkit.getScheduler().cancelTask(currenttaskid);
						} catch (Exception e) {
							;
						}
						for (String p_ : this.getAllPlayers()) {
							if (Validator.isPlayerOnline(p_)) {
								Util.sendMessage(this.plugin, Bukkit.getPlayer(p_), pli.getMessagesConfig().cancelled_starting);
							}
						}
						return;
					}
					this.stop();
				}
			}
		}
	}

	public void leavePlayerRaw(final String playername, final boolean fullLeave) {
		if (!this.containsPlayer(playername)) {
			return;
		}
		final Player p = Bukkit.getPlayer(playername);
		final ArenaPlayer ap = ArenaPlayer.getPlayerInstance(playername);
		if (p == null) {
			return;
		}
		if (p.isDead()) {
			System.out.println(p.getName() + " unexpectedly appeared dead! Sending respawn packet.");
			Effects.playRespawn(p, this.plugin);
			Bukkit.getScheduler().runTaskLater(this.plugin, new Runnable() {
				public void run() {
					leavePlayerRaw(playername, fullLeave);
				}
			}, 10L);
			return;
		}
		this.players.remove(playername);
		if (this.pli.containsGlobalPlayer(playername)) {
			this.pli.getGlobalPlayers().remove(playername);
		}
		if (fullLeave) {
			this.plugin.getConfig().set("temp.left_players." + playername + ".name", playername);
			this.plugin.getConfig().set("temp.left_players." + playername + ".plugin", plugin.getName());
			if (plugin.getConfig().getBoolean("config.reset_inventory_when_players_leave_server")) {
				for (ItemStack i : ap.getInventory()) {
					if (i != null) {
						this.plugin.getConfig().set("temp.left_players." + playername + ".items." + Integer.toString((int) Math.round(Math.random() * 10000)) + i.getType().toString(), i);
					}
				}
			}
			this.plugin.saveConfig();

			try {
				if (this.pli.getGlobalLost().containsKey(playername)) {
					this.pli.getSpectatorManager().showSpectator(p);
					this.pli.getGlobalLost().remove(playername);
				} else {
					this.pli.getSpectatorManager().showSpectators(p);
				}
				if (this.pli.getGlobalArcadeSpectator().containsKey(playername)) {
					this.pli.getGlobalArcadeSpectator().remove(playername);
				}
				if (p != null) {
					p.removePotionEffect(PotionEffectType.JUMP);
					Util.teleportPlayerFixed(p, this.mainLobby);
					p.setFireTicks(0);
					p.setFlying(false);
					if (!p.isOp()) {
						p.setAllowFlight(false);
					}
					p.setGameMode(ap.getOriginalGamemode());
					p.setLevel(ap.getOriginalXplvl());
					p.getInventory().setContents(ap.getInventory());
					p.getInventory().setArmorContents(ap.getArmorInventory());
					p.updateInventory();

					p.setWalkSpeed(0.2F);
					p.setFoodLevel(20);
					p.setHealth(20D);
					p.removePotionEffect(PotionEffectType.JUMP);
					this.pli.getSpectatorManager().setSpectate(p, false);
					this.pli.getStatsInstance().updateSQLKillsDeathsAfter(p, this);
				}
				if (this.pli.getClassesHandler().getLasticonm().containsKey(p.getName())) {
					IconMenu iconm = pli.getClassesHandler().getLasticonm().get(p.getName());
					iconm.destroy();
					this.pli.getClassesHandler().getLasticonm().remove(p.getName());
				}
			} catch (Exception e) {
				System.out.println("Failed to log out player out of arena. " + e.getMessage());
			}
			return;
		}
		Util.clearInv(p);
		p.setWalkSpeed(0.2F);
		p.setFoodLevel(20);
		p.setHealth(20D);
		p.setFireTicks(0);
		p.removePotionEffect(PotionEffectType.JUMP);
		this.pli.getSpectatorManager().setSpectate(p, false);

		Bukkit.getServer().getPluginManager().callEvent(new PlayerLeaveArenaEvent(p, plugin, this));

		for (PotionEffect effect : p.getActivePotionEffects()) {
			if (effect != null) {
				p.removePotionEffect(effect.getType());
			}
		}

		for (Entity e : p.getNearbyEntities(50D, 50D, 50D)) {
			if (e.getType() == EntityType.DROPPED_ITEM || e.getType() == EntityType.SLIME || e.getType() == EntityType.ZOMBIE || e.getType() == EntityType.SKELETON || e.getType() == EntityType.SPIDER || e.getType() == EntityType.CREEPER) {
				e.remove();
			}
		}

		// pli.global_players.remove(playername);
		if (this.pli.getGlobalArcadeSpectator().containsKey(playername)) {
			this.pli.getGlobalArcadeSpectator().remove(playername);
		}

		if (this.pli.getPClasses().containsKey(playername)) {
			this.pli.getPClasses().remove(playername);
		}

		Util.updateSign(this.plugin, this);

		Bukkit.getScheduler().runTaskLater(this.getPlugin(), new Runnable() {
			public void run() {
				try {
					if (p != null) {
						pli.getHologramsHandler().sendAllHolograms(p);
					}
				} catch (Exception e) {
					System.out.println("Failed playing hologram: " + e.getMessage());
					if (MinigamesAPI.debug) {
						e.printStackTrace();
					}
				}
			}
		}, 10L);
		
		if (this.pli.getClassesHandler().getLasticonm().containsKey(p.getName())) {
			IconMenu iconm = pli.getClassesHandler().getLasticonm().get(p.getName());
			iconm.destroy();
			this.pli.getClassesHandler().getLasticonm().remove(p.getName());
		}

		final String arenaname = this.getInternalName();
		final Arena a = this;
		final boolean started_ = started;
		Bukkit.getScheduler().runTaskLater(this.plugin, new Runnable() {
			public void run() {
				if (p != null) {
					if (ai == null || a.isArcadeMain()) {
						if (a.getMainLobby() != null) {
							Util.teleportPlayerFixed(p, a.getMainLobby());
						} else if (a.getWaitingLobby() != null) {
							Util.teleportPlayerFixed(p, a.getWaitingLobby());
						}
					}
					p.setFireTicks(0);
					p.setFlying(false);
					if (!p.isOp()) {
						p.setAllowFlight(false);
					}
					p.setGameMode(ap.getOriginalGamemode());
					p.setLevel(ap.getOriginalXplvl());
					p.getInventory().setContents(ap.getInventory());
					p.getInventory().setArmorContents(ap.getArmorInventory());
					p.updateInventory();
					p.updateInventory();

					if (started_) {
						pli.getStatsInstance().updateSQLKillsDeathsAfter(p, a);
						if (!ap.isNoReward()) {
							pli.getRewardsInstance().giveWinReward(playername, a, tempPlayers, globalCoinMultiplier);
						} else {
							ap.setNoReward(false);
						}
					}

					if (plugin.getConfig().getBoolean("config.send_stats_on_stop")) {
						Util.sendStatsMessage(pli, p);
					}

					if (pli.getGlobalLost().containsKey(playername)) {
						pli.getSpectatorManager().showSpectator(p);
						pli.getGlobalLost().remove(playername);
					} else {
						pli.getSpectatorManager().showSpectators(p);
					}

					try {
						pli.getScoreboardManager().removeScoreboard(arenaname, p);
					} catch (Exception e) {
						//
					}
				}
			}
		}, 5L);

		if (this.plugin.getConfig().getBoolean("config.bungee.teleport_all_to_server_on_stop.tp")) {
			final String server = this.plugin.getConfig().getString("config.bungee.teleport_all_to_server_on_stop.server");
			Bukkit.getScheduler().runTaskLater(this.plugin, new Runnable() {
				public void run() {
					BungeeUtil.connectToServer(MinigamesAPI.getAPI(), p.getName(), server);
				}
			}, 30L);
			return;
		}
	}

	/**
	 * Spectate the game generally (not specifically after death)
	 * 
	 * @param playername
	 *            name of the player
	 */
	public void spectateGame(String playername) {
		final Player p = Bukkit.getPlayer(playername);
		if (p == null) {
			return;
		}
		Util.clearInv(p);
		p.setAllowFlight(true);
		p.setFlying(true);
		this.pli.getSpectatorManager().hideSpectator(p, this.getAllPlayers());
		this.pli.getScoreboardManager().updateScoreboard(this.plugin, this);
		if (!pli.isLastManStanding()) {
			if (this.getPlayerAlive() < 1) {
				final Arena a = this;
				Bukkit.getScheduler().runTaskLater(this.plugin, new Runnable() {
					public void run() {
						a.stop();
					}
				}, 20L);
			} else {
				spectateRaw(p);
			}
		} else {
			if (this.getPlayerAlive() < 2) {
				final Arena a = this;
				Bukkit.getScheduler().runTaskLater(this.plugin, new Runnable() {
					public void run() {
						a.stop();
					}
				}, 20L);
			} else {
				spectateRaw(p);
			}
		}
		Bukkit.getScheduler().runTaskLater(this.plugin, new Runnable() {
			public void run() {
				Util.clearInv(p);
				Util.giveSpectatorItems(plugin, p);
			}
		}, 3L);
	}

	/**
	 * Spectate the game after death
	 * 
	 * @param playername
	 *            name of the player
	 */
	public void spectate(String playername) {
		if (Validator.isPlayerValid(this.plugin, playername, this)) {
			this.onEliminated(playername);
			final Player p = Bukkit.getPlayer(playername);
			if (p == null) {
				return;
			}

			this.pli.getGlobalLost().put(playername, this);

			this.pli.getSpectatorManager().setSpectate(p, true);
			if (!this.plugin.getConfig().getBoolean("config.spectator.spectator_after_fall_or_death")) {
				this.leavePlayer(playername, false, false);
				this.pli.getScoreboardManager().updateScoreboard(this.plugin, this);
				return;
			}
			spectateGame(playername);
		}
	}

	public void spectateRaw(final Player p) {
		if (this.pli.isDeadInFakeBedEffects()) {
			Effects.playFakeBed(this, p);
		}

		if (this.pli.isSpectatorMode1_8()) {
			Effects.sendGameModeChange(p, 3);
		}

		final Location temp = this.spawns.get(0);
		try {
			Bukkit.getScheduler().runTaskLater(this.plugin, new Runnable() {
				public void run() {
					if (specSpawn != null) {
						Util.teleportPlayerFixed(p, specSpawn);
					} else {
						Util.teleportPlayerFixed(p, temp.clone().add(0D, 30D, 0D));
					}
				}
			}, 2L);
		} catch (Exception e) {
			if (this.specSpawn != null) {
				Util.teleportPlayerFixed(p, this.specSpawn);
			} else {
				Util.teleportPlayerFixed(p, temp.clone().add(0D, 30D, 0D));
			}
		}
	}

	public void spectateArcade(String playername) {
		Player p = Bukkit.getPlayer(playername);
		this.pli.getGlobalPlayers().put(playername, this.currentarena);
		this.pli.getGlobalArcadeSpectator().put(playername, this.currentarena);
		Util.teleportPlayerFixed(p, this.currentarena.getSpawns().get(0).clone().add(0D, 30D, 0D));
		p.setAllowFlight(true);
		p.setFlying(true);
		this.pli.getSpectatorManager().setSpectate(p, true);
	}

	int currentlobbycount = 10;
	int currentingamecount = 10;
	int currenttaskid = 0;

	public void setTaskId(int id) {
		this.currenttaskid = id;
	}

	public int getTaskId() {
		return this.currenttaskid;
	}

	/**
	 * Starts the lobby countdown and the arena afterwards
	 * 
	 * You can insta-start an arena by using Arena.start();
	 */
	public void startLobby() {
		startLobby(true);
	}

	public void startLobby(final boolean countdown) {
		if (currentstate != ArenaState.JOIN) {
			return;
		}
		this.setArenaState(ArenaState.STARTING);
		Util.updateSign(this.plugin, this);
		this.currentlobbycount = this.pli.getLobbyCountdown();
		final Arena a = this;

		// skip countdown
		if (!countdown) {
			Bukkit.getScheduler().runTaskLater(this.plugin, new Runnable() {
				public void run() {
					currentarena.getArena().start(true);
				}
			}, 10L);
		}

		Sound lobbycountdown_sound_ = null;
		try {
			lobbycountdown_sound_ = Sound.valueOf(this.plugin.getConfig().getString("config.sounds.lobby_countdown"));
		} catch (Exception e) {
			;
		}
		final Sound lobbycountdown_sound = lobbycountdown_sound_;

		this.currenttaskid = Bukkit.getScheduler().runTaskTimer(MinigamesAPI.getAPI(), new Runnable() {
			public void run() {
				currentlobbycount--;
				if (currentlobbycount == 60 || currentlobbycount == 30 || currentlobbycount == 15 || currentlobbycount == 10 || currentlobbycount < 6) {
					for (String p_ : a.getAllPlayers()) {
						if (Validator.isPlayerOnline(p_)) {
							Player p = Bukkit.getPlayer(p_);
							if (countdown) {
								Util.sendMessage(plugin, p, pli.getMessagesConfig().teleporting_to_arena_in.replaceAll("<count>", Integer.toString(currentlobbycount)));
								if (lobbycountdown_sound != null) {
									p.playSound(p.getLocation(), lobbycountdown_sound, 1F, 0F);
								}
							}
						}
					}
				}
				for (String p_ : a.getAllPlayers()) {
					if (Validator.isPlayerOnline(p_)) {
						Player p = Bukkit.getPlayer(p_);
						p.setExp(1F * ((1F * currentlobbycount) / (1F * pli.getLobbyCountdown())));
						if (pli.isUseXpBarLevel()) {
							p.setLevel(currentlobbycount);
						}
					}
				}
				if (currentlobbycount < 1) {
					Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
						public void run() {
							currentarena.getArena().start(true);
						}
					}, 10L);
					try {
						Bukkit.getScheduler().cancelTask(currenttaskid);
					} catch (Exception e) {
					}
				}
			}
		}, 5L, 20).getTaskId();
	}

	/**
	 * Instantly starts the arena, teleports players and udpates the arena
	 */
	public void start(boolean tp) {
		try {
			Bukkit.getScheduler().cancelTask(this.currenttaskid);
		} catch (Exception e) {
		}
		this.currentingamecount = this.pli.getIngameCountdown();
		if (tp) {
			this.setPlayerSpawnLoc(Util.teleportAllPlayers(this.currentarena.getArena().getAllPlayers(), this.currentarena.getArena().spawns));
		}
		boolean clearinv = this.plugin.getConfig().getBoolean("config.countdowns.clearinv_while_ingamecountdown");
		for (String p_ : this.currentarena.getArena().getAllPlayers()) {
			Player p = Bukkit.getPlayer(p_);
			p.setWalkSpeed(0.0F);
			p.setFoodLevel(5);
			p.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 9999999, -7)); // -5
			this.pli.getScoreboardLobbyManager().removeScoreboard(this.getInternalName(), p);
			if (clearinv) {
				Util.clearInv(p);
			}
		}
		final Arena a = this;
		Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
			public void run() {
				pli.getScoreboardManager().updateScoreboard(plugin, a);
			}
		}, 20L);
		setStartedIngameCountdown(true);
		if (!this.plugin.getConfig().getBoolean("config.countdowns.ingame_countdown_enabled")) {
			startRaw(a);
			return;
		}

		Sound ingamecountdown_sound_ = null;
		try {
			ingamecountdown_sound_ = Sound.valueOf(plugin.getConfig().getString("config.sounds.ingame_countdown"));
		} catch (Exception e) {
			;
		}
		final Sound ingamecountdown_sound = ingamecountdown_sound_;

		this.currenttaskid = Bukkit.getScheduler().runTaskTimer(MinigamesAPI.getAPI(), new Runnable() {
			public void run() {
				currentingamecount--;
				if (currentingamecount == 60 || currentingamecount == 30 || currentingamecount == 15 || currentingamecount == 10 || currentingamecount < 6) {
					for (String p_ : a.getAllPlayers()) {
						if (Validator.isPlayerOnline(p_)) {
							Player p = Bukkit.getPlayer(p_);
							Util.sendMessage(plugin, p, pli.getMessagesConfig().starting_in.replaceAll("<count>", Integer.toString(currentingamecount)));
							if (ingamecountdown_sound != null) {
								p.playSound(p.getLocation(), ingamecountdown_sound, 1F, 0F);
							}
						}
					}
				}
				for (String p_ : a.getAllPlayers()) {
					if (Validator.isPlayerOnline(p_)) {
						Player p = Bukkit.getPlayer(p_);
						p.setExp(1F * ((1F * currentingamecount) / (1F * pli.getIngameCountdown())));
						if (pli.isUseXpBarLevel()) {
							p.setLevel(currentingamecount);
						}
					}
				}
				if (currentingamecount < 1) {
					startRaw(a);
				}
			}
		}, 5L, 20).getTaskId();

		for (final String p_ : this.getAllPlayers()) {
			if (this.pli.getShopHandler().hasItemBought(p_, "coin_boost2")) {
				this.globalCoinMultiplier = 2;
				break;
			}
			if (this.pli.getShopHandler().hasItemBought(p_, "coin_boost3")) {
				this.globalCoinMultiplier = 3;
				break;
			}
		}
	}

	public void startRaw(final Arena a) {
		this.currentarena.getArena().setArenaState(ArenaState.INGAME);
		setStartedIngameCountdown(false);
		Util.updateSign(this.plugin, a);
		Bukkit.getServer().getPluginManager().callEvent(new ArenaStartEvent(this.plugin, this));
		boolean send_game_started_msg = this.plugin.getConfig().getBoolean("config.send_game_started_msg");
		for (String p_ : a.getAllPlayers()) {
			try {
				if (!this.pli.getGlobalLost().containsKey(p_)) {
					Player p = Bukkit.getPlayer(p_);
					if (this.plugin.getConfig().getBoolean("config.auto_add_default_kit")) {
						if (!this.pli.getClassesHandler().hasClass(p_)) {
							this.pli.getClassesHandler().setClass("default", p_, false);
						}
						this.pli.getClassesHandler().getClass(p_);
					} else {
						Util.clearInv(Bukkit.getPlayer(p_));
						this.pli.getClassesHandler().getClass(p_);
					}
					if (this.plugin.getConfig().getBoolean("config.shop_enabled")) {
						this.pli.getShopHandler().giveShopItems(p);
					}
					p.setFlying(false);
					p.setAllowFlight(false);
				}
			} catch (Exception e) {
				if (MinigamesAPI.debug) {
					e.printStackTrace();
				}
				System.out.println("Failed to set class: " + e.getMessage() + " at [1] " + e.getStackTrace()[1].getLineNumber() + " [0] " + e.getStackTrace()[0].getLineNumber());
			}
			Player p = Bukkit.getPlayer(p_);
			p.setWalkSpeed(0.2F);
			p.setFoodLevel(20);
			p.removePotionEffect(PotionEffectType.JUMP);
			if (send_game_started_msg) {
				p.sendMessage(pli.getMessagesConfig().game_started);
			}
		}
		if (this.plugin.getConfig().getBoolean("config.bungee.whitelist_while_game_running")) {
			Bukkit.setWhitelist(true);
		}
		this.started = true;
		Bukkit.getServer().getPluginManager().callEvent(new ArenaStartedEvent(plugin, this));
		started();
		try {
			Bukkit.getScheduler().cancelTask(currenttaskid);
		} catch (Exception e) {
		}

		// Maximum game time:
		this.setMaximumGameTime(Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
			public void run() {
				for (String p_ : a.getAllPlayers()) {
					if (Validator.isPlayerValid(plugin, p_, a)) {
						Bukkit.getPlayer(p_).sendMessage(pli.getMessagesConfig().stop_cause_maximum_game_time);
					}
				}
				Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
					public void run() {
						a.stop();
					}
				}, 5 * 20L);
			}
		}, 20L * 60L * (long) plugin.getConfig().getDouble("config.defaults.default_max_game_time_in_minutes") - 5 * 20L));
	}

	/**
	 * Gets executed after an arena started (after ingame countdown)
	 */
	public void started() {
		System.out.println(this.getInternalName() + " started.");
	}

	private boolean tempDelayStopped = false;

	/**
	 * Stops the arena and teleports all players to the mainlobby
	 */
	public void stop() {
		Bukkit.getServer().getPluginManager().callEvent(new ArenaStopEvent(plugin, this));
		final Arena a = this;
		if (this.getMaximumGameTime() != null) {
			this.getMaximumGameTime().cancel();
		}
		this.setTempPlayers(new ArrayList<String>(players));
		if (!this.tempDelayStopped) {
			if (this.plugin.getConfig().getBoolean("config.delay.enabled")) {
				Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
					public void run() {
						tempDelayStopped = true;
						a.stop();
					}
				}, this.plugin.getConfig().getInt("config.delay.amount_seconds") * 20L);
				this.setArenaState(ArenaState.RESTARTING);
				Util.updateSign(this.plugin, this);
				if (this.plugin.getConfig().getBoolean("config.spawn_fireworks_for_winners")) {
					if (this.getAllPlayers().size() > 0) {
						Util.spawnFirework(Bukkit.getPlayer(this.getAllPlayers().get(0)));
					}
				}
				return;
			}
		}
		this.tempDelayStopped = false;

		try {
			Bukkit.getScheduler().cancelTask(this.currenttaskid);
		} catch (Exception e) {

		}

		this.setArenaState(ArenaState.RESTARTING);

		final ArrayList<String> temp = new ArrayList<String>(this.getAllPlayers());
		for (final String p : temp) {
			try {
				Bukkit.getScheduler().runTaskLater(this.plugin, new Runnable() {
					public void run() {
						if (Validator.isPlayerOnline(p)) {
							for (Entity e : Bukkit.getPlayer(p).getNearbyEntities(50, 50, 50)) {
								if (e.getType() == EntityType.DROPPED_ITEM || e.getType() == EntityType.SLIME || e.getType() == EntityType.ZOMBIE || e.getType() == EntityType.SKELETON || e.getType() == EntityType.SPIDER || e.getType() == EntityType.CREEPER) {
									e.remove();
								}
							}
						}
					}
				}, 10L);
			} catch (Exception e) {
				System.out.println("Failed clearing entities.");
			}
			leavePlayer(p, false, true);
		}

		try {
			for (ItemStack item : this.globalDrops) {
				if (item != null) {
					item.setType(Material.AIR);
				}
			}
		} catch (Exception e) {
			System.out.println("Failed clearing items: " + e.getMessage());
		}

		if (a.getArenaType() == ArenaType.REGENERATION) {
			reset();
		} else {
			a.setArenaState(ArenaState.JOIN);
			Util.updateSign(plugin, a);
		}

		Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
			public void run() {
				players.clear();
				for (IconMenu im : pli.getClassesHandler().getLasticonm().values()) {
					im.destroy();
				}
			}
		}, 10L);

		this.started = false;
		setStartedIngameCountdown(false);

		this.tempCountdown = true;
		this.skipJoinLobby = false;
		this.currentspawn = 0;

		try {
			this.pli.getScoreboardManager().clearScoreboard(this.getInternalName());
			this.pli.getScoreboardLobbyManager().clearScoreboard(this.getInternalName());
		} catch (Exception e) {
			//
		}

		/*
		 * try { pli.getStatsInstance().updateSkulls(); } catch (Exception e) {
		 * 
		 * }
		 */

		if (this.plugin.getConfig().getBoolean("config.bungee.whitelist_while_game_running")) {
			Bukkit.setWhitelist(false);
		}

		if (this.plugin.getConfig().getBoolean("config.execute_cmds_on_stop")) {
			String[] cmds = plugin.getConfig().getString("config.cmds").split(";");
			if (cmds.length > 0) {
				for (String cmd : cmds) {
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
				}
			}
		}

		if (this.plugin.getConfig().getBoolean("config.bungee.teleport_all_to_server_on_stop.tp")) {
			final String server = this.plugin.getConfig().getString("config.bungee.teleport_all_to_server_on_stop.server");
			Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
				public void run() {
					for (Player p : Bukkit.getOnlinePlayers()) {
						BungeeUtil.connectToServer(MinigamesAPI.getAPI(), p.getName(), server);
					}
				}
			}, 30L);
			return;
		}

		if (this.plugin.getConfig().getBoolean("config.execute_cmds_on_stop")) {
			String[] cmds = plugin.getConfig().getString("config.cmds_after").split(";");
			if (cmds.length > 0) {
				for (String cmd : cmds) {
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
				}
			}
		}

		if (this.ai != null) {
			Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
				public void run() {
					if (ai != null) {
						ai.nextMinigame();
						ai = null;
					}
				}
			}, 10L);
		} else {
			// Map rotation only works without Arcade
			// check if there is only one player or none left
			if (temp.size() < 2) {
				return;
			}
			if (this.plugin.getConfig().getBoolean("config.map_rotation")) {
				Bukkit.getScheduler().runTaskLater(this.plugin, new Runnable() {
					public void run() {
						a.nextArenaOnMapRotation(temp);
					}
				}, 35L);
			}
		}

	}

	/**
	 * Rebuilds an arena from file (only for arenas of REGENERATION type)
	 */
	public void reset() {
		if (this.pli.isOldReset()) {
			ArenaLogger.debug("Resetting using old method.");
			try {
				Util.loadArenaFromFileSYNC(plugin, this);
			} catch (Exception e) {
				ArenaLogger.debug("Error resetting map using old method. " + e.getMessage());
			}
		} else {
			this.sr.reset();
		}
	}

	/***
	 * Use this when someone got killed/pushed down/eliminated in some way by a player
	 * 
	 * @param playername
	 *            The player that got eliminated
	 */
	public void onEliminated(String playername) {
		if (getLastdamager().containsKey(playername)) {
			Player killer = Bukkit.getPlayer(getLastdamager().get(playername));
			if (killer != null) {
				this.pli.getStatsInstance().addDeath(playername);
				this.getTempKillCount().put(killer.getName(), this.getTempKillCount().containsKey(killer.getName()) ? this.getTempKillCount().get(killer.getName()) + 1 : 1);
				this.getTempDeathCount().put(playername, this.getTempDeathCount().containsKey(playername) ? this.getTempDeathCount().get(playername) + 1 : 1);
				this.pli.getRewardsInstance().giveKillReward(killer.getName());
				Util.sendMessage(plugin, killer, MinigamesAPI.getAPI().getPluginInstance(plugin).getMessagesConfig().you_got_a_kill.replaceAll("<player>", playername));
				for (String p_ : this.getAllPlayers()) {
					if (!p_.equalsIgnoreCase(killer.getName())) {
						if (Validator.isPlayerOnline(p_)) {
							Bukkit.getPlayer(p_).sendMessage(MinigamesAPI.getAPI().getPluginInstance(plugin).getMessagesConfig().player_was_killed_by.replaceAll("<player>", playername).replaceAll("<killer>", killer.getName()));
						}
					}
				}
			}
			getLastdamager().remove(playername);
		} else {
			pli.getStatsInstance().addDeath(playername);
		}
	}

	/**
	 * Will shuffle all arenas and join the next available arena
	 * 
	 * @param players
	 */
	public void nextArenaOnMapRotation(ArrayList<String> players) {
		ArrayList<Arena> arenas = this.pli.getArenas();
		Collections.shuffle(arenas);
		for (Arena a : arenas) {
			if (a.getArenaState() == ArenaState.JOIN && a != this) {
				System.out.println(this.plugin.getName() + ": Next arena on map rotation: " + a.getInternalName());
				for (String p_ : players) {
					if (!a.containsPlayer(p_)) {
						a.joinPlayerLobby(p_, false);
					}
				}
			}
		}
	}

	public String getPlayerCount() {
		int alive = 0;
		for (String p_ : getAllPlayers()) {
			if (this.pli.getGlobalLost().containsKey(p_)) {
				continue;
			} else {
				alive++;
			}
		}
		return Integer.toString(alive) + "/" + Integer.toString(getAllPlayers().size());
	}

	public ArrayList<String> getTempPlayers() {
		return tempPlayers;
	}

	public void setTempPlayers(ArrayList<String> tempPlayers) {
		this.tempPlayers = tempPlayers;
	}

	public int getPlayerAlive() {
		int alive = 0;
		for (String p_ : getAllPlayers()) {
			if (pli.getGlobalLost().containsKey(p_)) {
				continue;
			} else {
				alive++;
			}
		}
		return alive;
	}

	public Location getWaitingLobbyTemp() {
		return this.waitingLobby;
	}

	public Location getMainLobbyTemp() {
		return this.mainLobby;
	}

	public ArcadeInstance getArcadeInstance() {
		return ai;
	}

	public boolean isArcadeMain() {
		return isArcadeMain;
	}

	public void setArcadeMain(boolean t) {
		isArcadeMain = t;
	}

	public HashMap<String, Location> getPSpawnLocs() {
		return pSpawnLoc;
	}

	public BukkitTask getMaximumGameTime() {
		return maximumGameTime;
	}

	public void setMaximumGameTime(BukkitTask maximumGameTime) {
		this.maximumGameTime = maximumGameTime;
	}

	public JavaPlugin getPlugin() {
		return plugin;
	}

	public PluginInstance getPluginInstance() {
		return pli;
	}

	public int getCurrentIngameCountdownTime() {
		return this.currentingamecount;
	}

	public int getCurrentLobbyCountdownTime() {
		return this.currentlobbycount;
	}

	public boolean getIngameCountdownStarted() {
		return this.getStartedIngameCountdown();
	}

	public boolean isSuccessfullyInit() {
		return isSuccessfullyInitialized;
	}

	public boolean getStartedIngameCountdown() {
		return startedIngameCountdown;
	}

	public void setStartedIngameCountdown(boolean startedIngameCountdown) {
		this.startedIngameCountdown = startedIngameCountdown;
	}

	public boolean getSkipJoinLobby() {
		return skipJoinLobby;
	}

	public void setSkipJoinLobby(boolean skipJoinLobby) {
		this.skipJoinLobby = skipJoinLobby;
	}

	public ArrayList<ItemStack> getGlobalDrops() {
		return globalDrops;
	}

	public void setGlobalDrops(ArrayList<ItemStack> globalDrops) {
		this.globalDrops = globalDrops;
	}

	public HashMap<String, String> getLastdamager() {
		return lastdamager;
	}

	public void setLastdamager(HashMap<String, String> lastdamager) {
		this.lastdamager = lastdamager;
	}

	public HashMap<String, Integer> getTempKillCount() {
		return tempKillCount;
	}

	public void setTempKillCount(HashMap<String, Integer> tempKillCount) {
		this.tempKillCount = tempKillCount;
	}

	public void setSpawns(ArrayList<Location> spawns) {
		this.spawns = spawns;
	}

	public HashMap<String, Integer> getTempDeathCount() {
		return tempDeathCount;
	}

	public void setTempDeathCount(HashMap<String, Integer> tempDeathCount) {
		this.tempDeathCount = tempDeathCount;
	}

}
