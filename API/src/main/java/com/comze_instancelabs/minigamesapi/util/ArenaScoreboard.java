package com.comze_instancelabs.minigamesapi.util;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

import com.comze_instancelabs.minigamesapi.Arena;
import com.comze_instancelabs.minigamesapi.MinigamesAPI;
import com.comze_instancelabs.minigamesapi.PluginInstance;

public class ArenaScoreboard {

	private HashMap<String, Scoreboard> arenaScore = new HashMap<String, Scoreboard>();
	private HashMap<String, Objective> arenaObjective = new HashMap<String, Objective>();
	private HashMap<String, Integer> currentScore = new HashMap<String, Integer>();

	private int initialized = 0; // 0 = false; 1 = true;
	private boolean custom = false;

	private PluginInstance pli;

	private ArrayList<String> loadedCustomStrings = new ArrayList<String>();

	public ArenaScoreboard() {
		//
	}

	public ArenaScoreboard(PluginInstance pli, JavaPlugin plugin) {
		this.custom = plugin.getConfig().getBoolean("config.use_custom_scoreboard");
		this.initialized = 1;
		this.pli = pli;
		if (pli.getMessagesConfig().getConfig().isSet("messages.custom_scoreboard.")) {
			for (String configline : pli.getMessagesConfig().getConfig().getConfigurationSection("messages.custom_scoreboard.").getKeys(false)) {
				String line = ChatColor.translateAlternateColorCodes('&', pli.getMessagesConfig().getConfig().getString("messages.custom_scoreboard." + configline));
				this.loadedCustomStrings.add(line);
			}
		}
	}

	public void updateScoreboard(final JavaPlugin plugin, final Arena arena) {
		if (!arena.getShowScoreboard()) {
			return;
		}

		if (this.initialized != 1) {
			this.custom = plugin.getConfig().getBoolean("config.use_custom_scoreboard");
		}

		if (this.pli == null) {
			this.pli = MinigamesAPI.getAPI().getPluginInstance(plugin);
		}

		Bukkit.getScheduler().runTask(MinigamesAPI.getAPI(), new Runnable() {
			public void run() {
				for (String playername : arena.getAllPlayers()) {
					if (!Validator.isPlayerValid(plugin, playername, arena)) {
						return;
					}
					Player p = Bukkit.getPlayer(playername);
					if (!custom) {
						if (!arenaScore.containsKey(arena.getInternalName())) {
							arenaScore.put(arena.getInternalName(), Bukkit.getScoreboardManager().getNewScoreboard());
						}
						if (!arenaObjective.containsKey(arena.getInternalName())) {
							arenaObjective.put(arena.getInternalName(), arenaScore.get(arena.getInternalName()).registerNewObjective(arena.getInternalName(), "dummy"));
							arenaObjective.get(arena.getInternalName()).setDisplaySlot(DisplaySlot.SIDEBAR);
							arenaObjective.get(arena.getInternalName()).setDisplayName(pli.getMessagesConfig().scoreboard_title.replaceAll("<arena>", arena.getInternalName()));
						}
					} else {
						if (!arenaScore.containsKey(playername)) {
							arenaScore.put(playername, Bukkit.getScoreboardManager().getNewScoreboard());
						}
						if (!arenaObjective.containsKey(playername)) {
							arenaObjective.put(playername, arenaScore.get(playername).registerNewObjective(playername, "dummy"));
							arenaObjective.get(playername).setDisplaySlot(DisplaySlot.SIDEBAR);
							arenaObjective.get(playername).setDisplayName(pli.getMessagesConfig().scoreboard_title.replaceAll("<arena>", arena.getInternalName()));
						}
					}

					if (custom) {
						try {
							for (String line : loadedCustomStrings) {
								String[] line_arr = line.split(":");
								String line_ = line_arr[0];
								String score_identifier = line_arr[1];
								int score = 0;
								if (score_identifier.equalsIgnoreCase("<playercount>")) {
									score = arena.getAllPlayers().size();
								} else if (score_identifier.equalsIgnoreCase("<lostplayercount>")) {
									score = arena.getAllPlayers().size() - arena.getPlayerAlive();
								} else if (score_identifier.equalsIgnoreCase("<playeralivecount>")) {
									score = arena.getPlayerAlive();
								} else if (score_identifier.equalsIgnoreCase("<points>")) {
									score = pli.getStatsInstance().getPoints(playername);
								} else if (score_identifier.equalsIgnoreCase("<wins>")) {
									score = pli.getStatsInstance().getWins(playername);
								} else if (score_identifier.equalsIgnoreCase("<money>")) {
									score = (int) MinigamesAPI.econ.getBalance(playername);
								}
								if (line_.length() < 15) {
									Util.resetScores(arenaScore.get(playername), ChatColor.GREEN + line_);
									Util.getScore(arenaObjective.get(playername), ChatColor.GREEN + line_).setScore(score);
								} else {
									Util.resetScores(arenaScore.get(playername), ChatColor.GREEN + line_.substring(0, Math.min(line_.length() - 3, 13)));
									Util.getScore(arenaObjective.get(playername), ChatColor.GREEN + line_.substring(0, Math.min(line_.length() - 3, 13))).setScore(score);
								}
							}

							if (arenaScore.get(playername) != null) {
								p.setScoreboard(arenaScore.get(playername));
							}
						} catch (Exception e) {
							System.out.println("Failed to set custom scoreboard: ");
							e.printStackTrace();
						}
					} else {
						for (String playername_ : arena.getAllPlayers()) {
							if (!Validator.isPlayerOnline(playername_)) {
								continue;
							}
							Player p_ = Bukkit.getPlayer(playername_);
							if (!pli.getGlobalLost().containsKey(playername_)) {
								int score = 0;
								if (currentScore.containsKey(playername_)) {
									int oldscore = currentScore.get(playername_);
									if (score > oldscore) {
										currentScore.put(playername_, score);
									} else {
										score = oldscore;
									}
								} else {
									currentScore.put(playername_, score);
								}
								try {
									if (p_.getName().length() < 15) {
										Util.getScore(arenaObjective.get(arena.getInternalName()), ChatColor.GREEN + p_.getName()).setScore(0);
									} else {
										Util.getScore(arenaObjective.get(arena.getInternalName()), ChatColor.GREEN + p_.getName().substring(0, p_.getName().length() - 3)).setScore(0);
										;
									}
								} catch (Exception e) {
								}
							} else if (pli.getGlobalLost().containsKey(playername_)) {
								try {
									if (currentScore.containsKey(playername_)) {
										int score = currentScore.get(playername_);
										if (p_.getName().length() < 15) {
											Util.resetScores(arenaScore.get(arena.getInternalName()), ChatColor.GREEN + p_.getName());
											Util.getScore(arenaObjective.get(arena.getInternalName()), ChatColor.RED + p_.getName()).setScore(0);
										} else {
											Util.resetScores(arenaScore.get(arena.getInternalName()), ChatColor.GREEN + p_.getName().substring(0, p_.getName().length() - 3));
											Util.getScore(arenaObjective.get(arena.getInternalName()), ChatColor.RED + p_.getName().substring(0, p_.getName().length() - 3)).setScore(0);
										}
									}
								} catch (Exception e) {
								}
							}
						}

						if (arenaScore.get(arena.getInternalName()) != null) {
							p.setScoreboard(arenaScore.get(arena.getInternalName()));
						}
					}

				}
			}
		});
	}

	public void removeScoreboard(String arena, Player p) {
		try {
			ScoreboardManager manager = Bukkit.getScoreboardManager();
			Scoreboard sc = manager.getNewScoreboard();

			p.setScoreboard(sc);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void clearScoreboard(String arenaname) {
		if (this.arenaScore.containsKey(arenaname)) {
			try {
				Scoreboard sc = this.arenaScore.get(arenaname);
				for (OfflinePlayer player : sc.getPlayers()) {
					sc.resetScores(player);
				}
			} catch (Exception e) {
				if (MinigamesAPI.debug) {
					e.printStackTrace();
				}
			}
			this.arenaScore.remove(arenaname);
		}
		if (this.arenaObjective.containsKey(arenaname)) {
			this.arenaObjective.remove(arenaname);
		}

		// ascore.put(arenaname, Bukkit.getScoreboardManager().getNewScoreboard());
	}

	public void setCurrentScoreMap(HashMap<String, Integer> newcurrentscore) {
		this.currentScore = newcurrentscore;
	}
}
