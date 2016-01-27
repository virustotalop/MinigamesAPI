package com.comze_instancelabs.minigamesapi.util;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

import com.comze_instancelabs.minigamesapi.Arena;
import com.comze_instancelabs.minigamesapi.MinigamesAPI;
import com.comze_instancelabs.minigamesapi.PluginInstance;

public class ArenaLobbyScoreboard {

	
	/* Should have just made a base scoreboard object and then extended and overrode methods
	 * 
	 * 
	 */
	
	private HashMap<String, Scoreboard> ascore = new HashMap<String, Scoreboard>();
	private HashMap<String, Objective> aobjective = new HashMap<String, Objective>();

	private int initialized = 0; // 0 = false; 1 = true;
	private boolean custom = false;

	private PluginInstance pli;

	private ArrayList<String> loadedCustomStrings = new ArrayList<String>();

	public ArenaLobbyScoreboard(PluginInstance pli, JavaPlugin plugin) {
		this.custom = plugin.getConfig().getBoolean("config.use_custom_scoreboard");
		this.initialized = 1;
		this.pli = pli;
		if (pli.getMessagesConfig().getConfig().isSet("messages.custom_lobby_scoreboard.")) {
			for (String configline : pli.getMessagesConfig().getConfig().getConfigurationSection("messages.custom_lobby_scoreboard.").getKeys(false)) {
				String line = ChatColor.translateAlternateColorCodes('&', pli.getMessagesConfig().getConfig().getString("messages.custom_lobby_scoreboard." + configline));
				this.loadedCustomStrings.add(line);
			}
		}
	}

	public void updateScoreboard(final JavaPlugin plugin, final Arena arena) {
		if (!arena.getShowScoreboard()) {
			return;
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
					if (!ascore.containsKey(playername)) {
						ascore.put(playername, Bukkit.getScoreboardManager().getNewScoreboard());
					}
					if (!aobjective.containsKey(playername)) {
						aobjective.put(playername, ascore.get(playername).registerNewObjective(playername, "dummy"));
						aobjective.get(playername).setDisplaySlot(DisplaySlot.SIDEBAR);
						aobjective.get(playername).setDisplayName(pli.getMessagesConfig().scoreboard_lobby_title.replaceAll("<arena>", arena.getInternalName()));
					}

					try {
						if (loadedCustomStrings.size() < 1) {
							return;
						}
						for (String line : loadedCustomStrings) {
							String[] line_arr = line.split(":");
							String line_ = line_arr[0];
							String score_identifier = line_arr[line_arr.length - 1];
							if (line_arr.length > 2) {
								line_ += ":" + line_arr[1];
							}
							int score = 0;
							if (score_identifier.equalsIgnoreCase("<playercount>")) {
								score = arena.getAllPlayers().size();
							} else if (score_identifier.equalsIgnoreCase("<maxplayercount>")) {
								score = arena.getMaxPlayers();
							} else if (score_identifier.equalsIgnoreCase("<points>")) {
								score = pli.getStatsInstance().getPoints(playername);
							} else if (score_identifier.equalsIgnoreCase("<wins>")) {
								score = pli.getStatsInstance().getWins(playername);
							} else if (score_identifier.equalsIgnoreCase("<money>")) {
								score = (int) MinigamesAPI.econ.getBalance(playername);
							}
							if (line_.length() < 15) {
								// ascore.get(arena.getInternalName()).resetScores(Bukkit.getOfflinePlayer(ChatColor.GREEN + line_));
								Util.getScore(aobjective.get(playername), ChatColor.GREEN + line_).setScore(score);
							} else {
								// ascore.get(arena.getInternalName()).resetScores(Bukkit.getOfflinePlayer(ChatColor.GREEN + line_.substring(0,
								// Math.min(line_.length() - 3, 13))));
								Util.getScore(aobjective.get(playername), ChatColor.GREEN + line_.substring(0, Math.min(line_.length() - 3, 13))).setScore(score);
							}
						}
						p.setScoreboard(ascore.get(playername));
					} catch (Exception e) {
						System.out.println("Failed to set custom scoreboard: ");
						e.printStackTrace();
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
		// TODO
	}
}
