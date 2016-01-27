package com.comze_instancelabs.minigamesapi.statsholograms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.comze_instancelabs.minigamesapi.Effects;
import com.comze_instancelabs.minigamesapi.MinigamesAPI;
import com.comze_instancelabs.minigamesapi.PluginInstance;
import com.comze_instancelabs.minigamesapi.util.Util;

public class Hologram {

	private ArrayList<Integer> ids = new ArrayList<Integer>();
	private PluginInstance pli;
	private Location location;

	public Hologram(PluginInstance pli, Location l) {
		this.pli = pli;
		this.location = l;
	}

	public void send(Player p) {
		if (pli.getMessagesConfig().getConfig().isSet("messages.stats")) {

			double ydelta = 0.25D;

			int killsInt = this.pli.getStatsInstance().getKills(p.getName());
			int deathsInt = this.pli.getStatsInstance().getDeaths(p.getName());
			int moneyInt = (int) MinigamesAPI.econ.getBalance(p.getName());

			String wins = Integer.toString(pli.getStatsInstance().getWins(p.getName()));
			String loses = Integer.toString(pli.getStatsInstance().getLoses(p.getName()));
			String kills = Integer.toString(killsInt);
			String deaths = Integer.toString(deathsInt);
			String money = Integer.toString(moneyInt);
			String points = Integer.toString(pli.getStatsInstance().getPoints(p.getName()));
			String kdr = Integer.toString(Math.max(killsInt, 1) / Math.max(deathsInt, 1));
			ArrayList<String> s = new ArrayList<String>(pli.getMessagesConfig().getConfig().getConfigurationSection("messages.stats").getKeys(false));
			Collections.reverse(s);
			for (String key : s) {
				// Each line from the config gets checked for variables like <wins> or <money> and these get replaced by the values calculated above
				String msg = this.pli.getMessagesConfig().getConfig().getString("messages.stats." + key).replaceAll("<wins>", wins).replaceAll("<loses>", loses).replaceAll("<alltime_kills>", kills).replaceAll("<alltime_deaths>", deaths).replaceAll("<points>", points).replaceAll("<kdr>", kdr).replaceAll("<money>", money);
				this.ids.addAll(Effects.playHologram(p, this.location.clone().add(0D, ydelta, 0D), ChatColor.translateAlternateColorCodes('&', msg), false, false));
				ydelta += 0.25D;
			}
		}
	}

	public ArrayList<Integer> getIds() {
		return ids;
	}

}
