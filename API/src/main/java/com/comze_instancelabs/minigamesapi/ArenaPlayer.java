package com.comze_instancelabs.minigamesapi;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.comze_instancelabs.minigamesapi.util.AClass;

public class ArenaPlayer {

	private String playername;
	private ItemStack[] inv;
	private ItemStack[] armorInv;
	private GameMode originalGamemode = GameMode.SURVIVAL;
	private int originalXpLvl = 0;
	private boolean noreward = false;
	private Arena currentArena;
	private AClass currentClass;

	private static HashMap<String, ArenaPlayer> players = new HashMap<String, ArenaPlayer>();

	public static ArenaPlayer getPlayerInstance(String playername) {
		if (!players.containsKey(playername)) {
			return new ArenaPlayer(playername);
		} else {
			return players.get(playername);
		}
	}

	public ArenaPlayer(String playername) {
		this.playername = playername;
		players.put(playername, this);
	}

	public Player getPlayer() {
		return Bukkit.getPlayer(playername);
	}

	public void setInventories(ItemStack[] inv, ItemStack[] armorInv) {
		this.inv = inv;
		this.armorInv = armorInv;
	}

	public ItemStack[] getInventory() {
		return this.inv;
	}

	public ItemStack[] getArmorInventory() {
		return this.armorInv;
	}

	public GameMode getOriginalGamemode() {
		return originalGamemode;
	}

	public void setOriginalGamemode(GameMode original_gamemode) {
		this.originalGamemode = original_gamemode;
	}

	public int getOriginalXplvl() {
		return originalXpLvl;
	}

	public void setOriginalXplvl(int original_xplvl) {
		this.originalXpLvl = original_xplvl;
	}

	public boolean isNoReward() {
		return noreward;
	}

	public void setNoReward(boolean noreward) {
		this.noreward = noreward;
	}

	public Arena getCurrentArena() {
		return this.currentArena;
	}

	public void setCurrentArena(Arena currentArena) {
		this.currentArena = currentArena;
	}

	public AClass getCurrentClass() {
		return this.currentClass;
	}

	public void setCurrentClass(AClass currentClass) {
		this.currentClass = currentClass;
	}

}
