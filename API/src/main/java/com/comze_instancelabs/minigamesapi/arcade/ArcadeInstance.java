package com.comze_instancelabs.minigamesapi.arcade;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.comze_instancelabs.minigamesapi.Arena;
import com.comze_instancelabs.minigamesapi.ArenaState;
import com.comze_instancelabs.minigamesapi.MinigamesAPI;
import com.comze_instancelabs.minigamesapi.PluginInstance;
import com.comze_instancelabs.minigamesapi.util.Util;
import com.comze_instancelabs.minigamesapi.util.Validator;

public class ArcadeInstance {

	public ArrayList<PluginInstance> minigames = new ArrayList<PluginInstance>();
	int currentindex = 0;
	public ArrayList<String> players = new ArrayList<String>();
	Arena arena;
	JavaPlugin plugin;

	boolean in_a_game = false;
	Arena currentarena = null;
	boolean started;

	public ArcadeInstance(JavaPlugin plugin, ArrayList<PluginInstance> minigames, Arena arena) {
		this.minigames = minigames;
		this.arena = arena;
		this.plugin = plugin;
	}

	// TODO max 16 players!
	public void joinArcade(String playername) {
		PluginInstance pli = MinigamesAPI.getAPI().getPluginInstance(plugin);
		if (!players.contains(playername)) {
			players.add(playername);
			arena.addPlayer(playername);
		}
		Player p = Bukkit.getPlayer(playername);
		if (p == null) {
			return;
		}
		if (players.size() >= plugin.getConfig().getInt("config.arcade.min_players")) {
			boolean msg = true;
			if (!started) {
				startArcade();
			} else {
				if (currentindex < minigames.size()) {
					if (in_a_game) {
						if (currentarena != null) {
							if (p != null) {
								PluginInstance pli_ = minigames.get(currentindex);
								System.out.println(pli_.getPlugin().getName() + " " + currentarena.getInternalName() + " " + p.getName());
								if (currentarena.getArenaState() != ArenaState.INGAME && currentarena.getArenaState() != ArenaState.RESTARTING) {
									currentarena.joinPlayerLobby(playername, this, false, true);
								} else {
									msg = false;
									currentarena.spectateArcade(playername);
								}

								pli_.scoreboardManager.updateScoreboard(pli_.getPlugin(), currentarena);
							}
						}
					}
				}
			}
			if (msg) {
				p.sendMessage(MinigamesAPI.getAPI().getPluginInstance(plugin).getMessagesConfig().arcade_joined_waiting.replaceAll("<count>", "0"));
			} else {
				p.sendMessage(MinigamesAPI.getAPI().getPluginInstance(plugin).getMessagesConfig().arcade_joined_spectator);
			}
		} else {
			p.sendMessage(MinigamesAPI.getAPI().getPluginInstance(plugin).getMessagesConfig().arcade_joined_waiting.replaceAll("<count>", Integer.toString(plugin.getConfig().getInt("config.arcade.min_players") - players.size())));
		}
	}

	public void leaveArcade(final String playername) {
		this.leaveArcade(playername, true);
	}

	public void leaveArcade(final String playername, boolean endOfGame) {
		final PluginInstance pli = MinigamesAPI.getAPI().getPluginInstance(plugin);
		if (players.contains(playername)) {
			players.remove(playername);
		}
		if (arena.containsPlayer(playername)) {
			arena.removePlayer(playername);
		}
		if (minigames.get(currentindex).getArenas().size() > 0) {
			if (minigames.get(currentindex).getArenas().get(0).containsPlayer(playername)) {
				minigames.get(currentindex).getArenas().get(0).leavePlayer(playername, false, false);
			}
		}
		Bukkit.getScheduler().runTaskLater(MinigamesAPI.getAPI(), new Runnable() {
			public void run() {
				Player p = Bukkit.getPlayer(playername);
				if (p != null) {
					Util.teleportPlayerFixed(p, arena.getMainLobbyTemp());
					pli.getSpectatorManager().setSpectate(p, false);
					if (!p.isOp()) {
						p.setFlying(false);
						p.setAllowFlight(false);
					}
				}
			}
		}, 20L);
		clean();

		// This shouldn't be necessary anymore except for arcade spectators
		if (pli.containsGlobalPlayer(playername)) {
			pli.globalPlayers.remove(playername);
		}
		if (pli.containsGlobalLost(playername)) {
			pli.globalLost.remove(playername);
		}
		if (currentarena != null) {
			PluginInstance pli_ = MinigamesAPI.getAPI().pluginInstances.get(currentarena.getPlugin());
			if (pli_ != null) {
				if (pli_.containsGlobalPlayer(playername)) {
					pli_.globalPlayers.remove(playername);
				}
				if (pli_.containsGlobalLost(playername)) {
					pli_.globalLost.remove(playername);
				}
			}
		}

		Util.updateSign(plugin, arena);

		if (endOfGame) {
			if (players.size() < 2) {
				stopArcade(false);
			}
		}
	}

	int currentlobbycount = 31;
	int currenttaskid = 0;

	public void startArcade() {
		if (started) {
			return;
		}
		started = true;
		Collections.shuffle(minigames);

		currentlobbycount = plugin.getConfig().getInt("config.arcade.lobby_countdown") + 1;
		final ArcadeInstance ai = this;
		final PluginInstance pli = MinigamesAPI.getAPI().getPluginInstance(plugin);

		currenttaskid = Bukkit.getScheduler().runTaskTimer(MinigamesAPI.getAPI(), new Runnable() {
			public void run() {
				currentlobbycount--;
				if (currentlobbycount == 60 || currentlobbycount == 30 || currentlobbycount == 15 || currentlobbycount == 10 || currentlobbycount < 6) {
					for (String p_ : ai.players) {
						if (Validator.isPlayerOnline(p_)) {
							Player p = Bukkit.getPlayer(p_);
							p.sendMessage(pli.getMessagesConfig().starting_in.replaceAll("<count>", Integer.toString(currentlobbycount)));
						}
					}
				}
				if (currentlobbycount < 1) {
					currentindex--;
					ai.nextMinigame();
					try {
						Bukkit.getScheduler().cancelTask(currenttaskid);
					} catch (Exception e) {
					}
				}
			}
		}, 5L, 20).getTaskId();
	}

	public void stopArcade(boolean stopOfGame) {
		try {
			Bukkit.getScheduler().cancelTask(currenttaskid);
		} catch (Exception e) {

		}
		final ArrayList<String> temp = new ArrayList<String>(players);
		for (String p_ : temp) {
			this.leaveArcade(p_, false);
		}
		players.clear();
		started = false;
		in_a_game = false;
		currentarena = null;
		this.currentindex = 0;

		HashSet hs = new HashSet();
		hs.addAll(temp);
		temp.clear();
		temp.addAll(hs);
		final ArcadeInstance ai = this;
		if (stopOfGame && plugin.getConfig().getBoolean("config.arcade.infinite_mode.enabled")) {
			if (temp.size() > 1) {
				for (String p_ : temp) {
					Util.sendMessage(plugin, Bukkit.getPlayer(p_), MinigamesAPI.getAPI().getPluginInstance(plugin).getMessagesConfig().arcade_new_round.replaceAll("<count>", Integer.toString(plugin.getConfig().getInt("config.arcade.infinite_mode.seconds_to_new_round"))));
				}
				Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
					public void run() {
						for (String p_ : temp) {
							if (!players.contains(p_)) {
								players.add(p_);
							}
						}
						ai.startArcade();
					}
				}, Math.max(40L, 20L * plugin.getConfig().getInt("config.arcade.infinite_mode.seconds_to_new_round")));
			}
		}
	}

	public void stopArcade() {
		this.stopArcade(false);
	}

	public void stopCurrentMinigame() {
		if (currentindex < minigames.size()) {
			PluginInstance mg = minigames.get(currentindex);
			if (mg.getArenas().size() > 0) {
				if (mg.getPlugin().getConfig().getBoolean("config.arcade.arena_to_prefer.enabled")) {
					String arenaname = mg.getPlugin().getConfig().getString("config.arcade.arena_to_prefer.arena");
					Arena a = mg.getArenaByName(arenaname);
					if (a != null) {
						a.stop();
					}
				} else {
					minigames.get(currentindex).getArenas().get(0).stop();
				}
			}
		}
	}

	public void nextMinigame() {
		nextMinigame(30L);
	}

	public void nextMinigame(long delay) {
		in_a_game = false;

		if (currentindex < minigames.size() - 1) {
			currentindex++;
		} else {
			arena.stop();
			// stopArcade();
			return;
		}
		// System.out.println(delay + " " + currentindex);
		final ArcadeInstance ai = this;
		Bukkit.getScheduler().runTaskLater(MinigamesAPI.getAPI(), new Runnable() {
			public void run() {
				ArrayList<String> temp = new ArrayList<String>(players);

				PluginInstance mg = minigames.get(currentindex);
				if (mg.getPlugin().getConfig().getBoolean("config.arcade.enabled")) {
					Arena a = null;
					if (mg.getPlugin().getConfig().getBoolean("config.arcade.arena_to_prefer.enabled")) {
						String arenaname = mg.getPlugin().getConfig().getString("config.arcade.arena_to_prefer.arena");
						a = mg.getArenaByName(arenaname);
						if (a == null) {
							for (Arena a_ : mg.getArenas()) {
								if (a_.getArenaState() == ArenaState.JOIN || a_.getArenaState() == ArenaState.STARTING) {
									a = a_;
									break;
								}
							}
						}
					} else {
						for (Arena a_ : mg.getArenas()) {
							if (a_.getArenaState() == ArenaState.JOIN || a_.getArenaState() == ArenaState.STARTING) {
								a = a_;
								break;
							}
						}
					}
					if (a != null) {
						in_a_game = true;
						currentarena = a;
						PluginInstance pli = MinigamesAPI.getAPI().getPluginInstance(plugin);
						for (String p_ : temp) {
							if (Validator.isPlayerOnline(p_)) {
								String minigame = mg.getArenaListener().getName();
								if (!a.containsPlayer(p_)) {
									Bukkit.getPlayer(p_).sendMessage(mg.getMessagesConfig().arcade_next_minigame.replaceAll("<minigame>", Character.toUpperCase(minigame.charAt(0)) + minigame.substring(1)));
									a.joinPlayerLobby(p_, ai, plugin.getConfig().getBoolean("config.arcade.show_each_lobby_countdown"), false);
								}
								pli.getSpectatorManager().setSpectate(Bukkit.getPlayer(p_), false);
							}
						}
					} else {
						nextMinigame(5L);
					}
				} else {
					nextMinigame(5L);
				}
			}
		}, delay);
	}

	public void clean() {
		ArrayList<String> rem = new ArrayList<String>();
		for (String p_ : this.players) {
			if (!Validator.isPlayerOnline(p_)) {
				rem.add(p_);
			}
		}
		for (String r : rem) {
			if (players.contains(r)) {
				players.remove(r);
			}
		}
	}

}
