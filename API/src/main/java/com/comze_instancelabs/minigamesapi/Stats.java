package com.comze_instancelabs.minigamesapi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Skull;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;

import com.comze_instancelabs.minigamesapi.config.StatsConfig;
import com.comze_instancelabs.minigamesapi.util.Util.ValueComparator;

public class Stats {

	// used for wins and points
	// you can get points for pretty much everything in the games,
	// but these points are just for top stats, nothing more

	private JavaPlugin plugin = null;
	private PluginInstance pli = null;

	public ArrayList<String> skullsetup = new ArrayList<String>();
	private int statsKillPoints = 2;
	private int statsWinPoints = 10;

	public Stats(PluginInstance pli, JavaPlugin plugin) {
		this.plugin = plugin;
		this.reloadVariables();
		this.pli = pli;
	}

	public void reloadVariables() {
		this.setStatsKillPoints(plugin.getConfig().getInt("config.stats.points_for_kill"));
		this.setStatsWinPoints(plugin.getConfig().getInt("config.stats.points_for_win"));
	}

	public void win(String playername, int count) {
		addWin(playername);
		addPoints(playername, count);
		Player p = Bukkit.getPlayer(playername);
		if (p != null) {
			this.pli.getSQLInstance().updateWinnerStats(p, count, true);
		} else {
			if (MinigamesAPI.debug) {
				System.out.println("Failed updating SQL Stats as the player is not online anymore!");
			}
		}
	}

	public void lose(String playername) {
		addLose(playername);
		Player p = Bukkit.getPlayer(playername);
		if (p != null) {
			this.pli.getSQLInstance().updateLoserStats(p);
		} else {
			if (MinigamesAPI.debug) {
				System.out.println("Failed updating SQL Stats as the player is not online anymore!");
			}
		}
	}

	/**
	 * Gets called on player join to ensure file stats are up to date (with mysql stats)
	 * 
	 * @param playername
	 */
	public void update(String playername) {
		if (this.plugin.getConfig().getBoolean("mysql.enabled")) {
			Player p = Bukkit.getPlayer(playername);
			String uuid = p.getUniqueId().toString();
			if (this.pli.getStatsConfig().getConfig().isSet("players." + uuid + ".wins")) {
				int wins = getWins(playername);
				int sqlwins = this.pli.getSQLInstance().getWins(p);
				setWins(playername, Math.max(wins, sqlwins));
			}
			if (this.pli.getStatsConfig().getConfig().isSet("players." + uuid + ".points")) {
				int points = getPoints(playername);
				int sqlpoints = this.pli.getSQLInstance().getPoints(p);
				setPoints(playername, Math.max(points, sqlpoints));
			}
		}
	}

	public void updateSQLKillsDeathsAfter(Player p, Arena a) {
		if (!a.getPlugin().isEnabled()) {
			System.out.println("Couldn't save Death/Kill SQL stats as the server stopped/restarted.");
			return;
		}
		// Update sql server with kills stats at the end
		if (a.getTempKillCount().containsKey(p.getName())) {
			if (MinigamesAPI.debug) {
				System.out.println(a.getTempKillCount().get(p.getName()));
			}
			this.pli.getSQLInstance().updateKillerStats(p, a.getTempKillCount().get(p.getName()));
			a.getTempKillCount().remove(p.getName());
		}
		// death stats
		if (a.getTempDeathCount().containsKey(p.getName())) {
			if (MinigamesAPI.debug) {
				System.out.println(a.getTempDeathCount().get(p.getName()));
			}
			this.pli.getSQLInstance().updateDeathStats(p, a.getTempDeathCount().get(p.getName()));
			a.getTempDeathCount().remove(p.getName());
		}
	}

	public void setWins(String playername, int count) {
		String uuid = Bukkit.getPlayer(playername).getUniqueId().toString();
		this.pli.getStatsConfig().getConfig().set("players." + uuid + ".wins", count);
		this.pli.getStatsConfig().saveConfig();
	}

	public void setPoints(String playername, int count) {
		String uuid = Bukkit.getPlayer(playername).getUniqueId().toString();
		this.pli.getStatsConfig().getConfig().set("players." + uuid + ".points", count);
		this.pli.getStatsConfig().saveConfig();
	}

	public void addWin(String playername) {
		StatsConfig config = pli.getStatsConfig();
		int temp = 0;
		String uuid = Bukkit.getPlayer(playername).getUniqueId().toString();
		if (config.getConfig().isSet("players." + uuid + ".wins")) {
			temp = config.getConfig().getInt("players." + uuid + ".wins");
		}
		temp++;
		this.pli.getArenaAchievements().setAchievementDone(playername, "first_win", false);
		if (temp >= 10) {
			this.pli.getArenaAchievements().setAchievementDone(playername, "ten_wins", false);
		}
		config.getConfig().set("players." + uuid + ".wins", temp);
		config.getConfig().set("players." + uuid + ".playername", playername);
		config.saveConfig();
	}

	public void addLose(String playername) {
		StatsConfig config = this.pli.getStatsConfig();
		int temp = 0;
		String uuid = Bukkit.getPlayer(playername).getUniqueId().toString();
		if (config.getConfig().isSet("players." + uuid + ".loses")) {
			temp = config.getConfig().getInt("players." + uuid + ".loses");
		}
		config.getConfig().set("players." + uuid + ".loses", temp + 1);
		config.getConfig().set("players." + uuid + ".playername", playername);
		config.saveConfig();
	}

	public void addKill(String playername) {
		StatsConfig config = this.pli.getStatsConfig();
		int temp = 0;
		String uuid = Bukkit.getPlayer(playername).getUniqueId().toString();
		if (config.getConfig().isSet("players." + uuid + ".kills")) {
			temp = config.getConfig().getInt("players." + uuid + ".kills");
		}
		temp++;
		config.getConfig().set("players." + uuid + ".kills", temp);
		config.getConfig().set("players." + uuid + ".playername", playername);
		config.saveConfig();
		this.pli.getArenaAchievements().setAchievementDone(playername, "first_blood", false);
		if (temp >= 10 && temp < 100) {
			this.pli.getArenaAchievements().setAchievementDone(playername, "ten_kills", false);
		} else if (temp >= 100) {
			this.pli.getArenaAchievements().setAchievementDone(playername, "hundred_kills", false);
		}
		// Moved to Rewards.java:257
		// pli.getSQLInstance().updateKillerStats(Bukkit.getPlayer(playername));
	}

	public void addDeath(String playername) {
		StatsConfig config = this.pli.getStatsConfig();
		int temp = 0;
		String uuid = Bukkit.getPlayer(playername).getUniqueId().toString();
		if (config.getConfig().isSet("players." + uuid + ".deaths")) {
			temp = config.getConfig().getInt("players." + uuid + ".deaths");
		}
		temp++;
		config.getConfig().set("players." + uuid + ".deaths", temp);
		config.getConfig().set("players." + uuid + ".playername", playername);
		config.saveConfig();
		// Moved to Rewards.java:265
		// pli.getSQLInstance().updateDeathStats(Bukkit.getPlayer(playername));
	}

	public void addPoints(String playername, int count) {
		StatsConfig config = this.pli.getStatsConfig();
		int temp = 0;
		String uuid = Bukkit.getPlayer(playername).getUniqueId().toString();
		if (config.getConfig().isSet("players." + uuid + ".points")) {
			temp = config.getConfig().getInt("players." + uuid + ".points");
		}
		int temp_ = 0;
		if (MinigamesAPI.getAPI().statsglobal.getConfig().isSet("players." + uuid + ".points")) {
			temp_ = MinigamesAPI.getAPI().statsglobal.getConfig().getInt("players." + uuid + ".points");
		} else {
			temp_ = temp;
		}
		MinigamesAPI.getAPI().statsglobal.getConfig().set("players." + uuid + ".points", temp_ + count);
		MinigamesAPI.getAPI().statsglobal.saveConfig();
		config.getConfig().set("players." + uuid + ".points", temp + count);
		config.saveConfig();
	}

	public int getPoints(String playername) {
		FileConfiguration config = this.pli.getStatsConfig().getConfig();
		String uuid = Bukkit.getPlayer(playername).getUniqueId().toString();
		if (config.isSet("players." + uuid + ".points")) {
			int points = config.getInt("players." + uuid + ".points");
			return points;
		}
		return 0;
	}

	public int getWins(String playername) {
		FileConfiguration config = this.pli.getStatsConfig().getConfig();
		String uuid = Bukkit.getPlayer(playername).getUniqueId().toString();
		if (config.isSet("players." + uuid + ".wins")) {
			return config.getInt("players." + uuid + ".wins");
		}
		return 0;
	}

	public int getLoses(String playername) {
		FileConfiguration config = this.pli.getStatsConfig().getConfig();
		String uuid = Bukkit.getPlayer(playername).getUniqueId().toString();
		if (config.isSet("players." + uuid + ".loses")) {
			return config.getInt("players." + uuid + ".loses");
		}
		return 0;
	}

	public int getKills(String playername) {
		FileConfiguration config = this.pli.getStatsConfig().getConfig();
		String uuid = Bukkit.getPlayer(playername).getUniqueId().toString();
		if (config.isSet("players." + uuid + ".kills")) {
			return config.getInt("players." + uuid + ".kills");
		}
		return 0;
	}

	public int getDeaths(String playername) {
		FileConfiguration config = this.pli.getStatsConfig().getConfig();
		String uuid = Bukkit.getPlayer(playername).getUniqueId().toString();
		if (config.isSet("players." + uuid + ".deaths")) {
			return config.getInt("players." + uuid + ".deaths");
		}
		return 0;
	}

	public TreeMap<String, Double> getTop(int count, boolean wins) {
		int c = 0;
		String key = "wins";
		if (!wins) {
			key = "points";
		}
		FileConfiguration config = pli.getStatsConfig().getConfig();
		HashMap<String, Double> pwins = new HashMap<String, Double>();
		if (config.isSet("players.")) {
			for (String p : config.getConfigurationSection("players.").getKeys(false)) {
				c++;
				if (c > 100) {
					break;
				}
				pwins.put(config.getString("players." + p + ".playername"), (double) config.getInt("players." + p + "." + key));
			}
		}
		ValueComparator bvc = new ValueComparator(pwins);
		TreeMap<String, Double> sorted_wins = new TreeMap<String, Double>(bvc);
		sorted_wins.putAll(pwins);
		return sorted_wins;
	}

	public TreeMap<String, Double> getTop() {
		FileConfiguration config = this.pli.getStatsConfig().getConfig();
		HashMap<String, Double> pwins = new HashMap<String, Double>();
		if (config.isSet("players.")) {
			for (String p : config.getConfigurationSection("players.").getKeys(false)) {
				pwins.put(config.getString("players." + p + ".playername"), (double) config.getInt("players." + p + ".wins"));
			}
		}
		ValueComparator bvc = new ValueComparator(pwins);
		TreeMap<String, Double> sorted_wins = new TreeMap<String, Double>(bvc);
		sorted_wins.putAll(pwins);
		return sorted_wins;
	}

	public static ItemStack giveSkull(String name) {
		ItemStack item = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
		SkullMeta skullmeta = (SkullMeta) item.getItemMeta();
		skullmeta.setDisplayName(name);
		skullmeta.setOwner(name);
		item.setItemMeta(skullmeta);
		return item;
	}

	public void saveSkull(Location t, int count) {
		FileConfiguration config = this.pli.getStatsConfig().getConfig();
		String base = "skulls." + UUID.randomUUID().toString() + ".";
		config.set(base + "world", t.getWorld().getName());
		config.set(base + "x", t.getBlockX());
		config.set(base + "y", t.getBlockY());
		config.set(base + "z", t.getBlockZ());
		config.set(base + "pos", count);
		BlockState state = t.getBlock().getState();
		if (state instanceof Skull) {
			Skull skull_ = (Skull) state;
			config.set(base + "dir", skull_.getRotation().toString());
		} else {
			config.set(base + "dir", "SELF");
		}

		this.pli.getStatsConfig().saveConfig();
	}

	/*
	 * Since Mojangs new public API this seems to be lagging hardcore (and they're disallowing more than 1 connection per minute, gg)
	 */
	public void updateSkulls() {
		TreeMap<String, Double> sorted_wins = getTop();
		FileConfiguration config = this.pli.getStatsConfig().getConfig();
		if (config.isSet("skulls.")) {
			for (String skull : config.getConfigurationSection("skulls.").getKeys(false)) {
				String base = "skulls." + skull;
				Location t = new Location(Bukkit.getWorld(config.getString(base + ".world")), config.getDouble(base + ".x"), config.getDouble(base + ".y"), config.getDouble(base + ".z"));
				t.getBlock().setData((byte) 0x1);
				BlockState state = t.getBlock().getState();

				int pos = config.getInt(base + ".pos");
				String dir = config.getString(base + ".dir");

				if (state instanceof Skull) {
					Skull theSkull = (Skull) state;
					theSkull.setRotation(BlockFace.valueOf(dir));
					theSkull.setSkullType(SkullType.PLAYER);
					System.out.println(pos + " " + sorted_wins.keySet().size());
					if (pos <= sorted_wins.keySet().size()) {
						String name = (String) sorted_wins.keySet().toArray()[pos - 1];
						theSkull.setOwner(name);
						System.out.println(name);
					}
					theSkull.update();
				}
			}
		}
	}

	public int getStatsKillPoints() {
		return statsKillPoints;
	}

	public void setStatsKillPoints(int statsKillPoints) {
		this.statsKillPoints = statsKillPoints;
	}

	public int getStatsWinPoints() {
		return statsWinPoints;
	}

	public void setStatsWinPoints(int statsWinPoints) {
		this.statsWinPoints = statsWinPoints;
	}

}
