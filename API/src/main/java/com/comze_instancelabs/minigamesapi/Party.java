package com.comze_instancelabs.minigamesapi;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class Party {

	private String owner;
	private ArrayList<String> players = new ArrayList<String>();

	public Party(String owner) {
		this.owner = owner;
	}

	public String getOwner() {
		return owner;
	}

	public ArrayList<String> getPlayers() {
		return players;
	}

	public void addPlayer(String p) {
		if (!this.players.contains(p)) {
			this.players.add(p);
		}
		Bukkit.getPlayer(p).sendMessage(MinigamesAPI.getAPI().partymessages.youJoinedParty.replaceAll("<player>", this.getOwner()));
		tellAll(MinigamesAPI.getAPI().partymessages.playerJoinedParty.replaceAll("<player>", p));
	}

	public boolean removePlayer(String p) {
		if (this.players.contains(p)) {
			this.players.remove(p);
			Player p___ = Bukkit.getPlayer(p);
			if (p___ != null) {
				p___.sendMessage(MinigamesAPI.getAPI().partymessages.youLeftParty.replaceAll("<player>", this.getOwner()));
			}
			tellAll(MinigamesAPI.getAPI().partymessages.playerLeftParty.replaceAll("<player>", p));
			return true;
		}
		return false;
	}

	public boolean containsPlayer(String p) {
		return players.contains(p);
	}

	public void disband() {
		tellAll(MinigamesAPI.getAPI().partymessages.partyDisbanded);
		if (MinigamesAPI.getAPI().globalParty.containsKey(owner)) {
			this.players.clear();
			MinigamesAPI.getAPI().globalParty.remove(owner);
		}
	}

	private void tellAll(String msg) {
		for (String p_ : this.getPlayers()) {
			Player p__ = Bukkit.getPlayer(p_);
			if (p__ != null) {
				p__.sendMessage(msg);
			}
		}
		Player p___ = Bukkit.getPlayer(this.getOwner());
		if (p___ != null) {
			p___.sendMessage(msg);
		}
	}

}
