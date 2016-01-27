package com.comze_instancelabs.minigamesapi.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import com.comze_instancelabs.minigamesapi.Arena;

public class ArenaEvent extends Event {
	private static final HandlerList handlers = new HandlerList();
	private Arena arena;
	private JavaPlugin plugin;

	public ArenaEvent(JavaPlugin plugin, Arena a) {
		this.arena = a;
		this.plugin = plugin;
	}

	public Arena getArena() {
		return this.arena;
	}

	public JavaPlugin getPlugin() {
		return this.plugin;
	}

	public HandlerList getHandlers() {
		return ArenaEvent.handlers;
	}

	public static HandlerList getHandlerList() {
		return ArenaEvent.handlers;
	}
}
