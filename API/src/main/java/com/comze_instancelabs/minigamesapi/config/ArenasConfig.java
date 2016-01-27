package com.comze_instancelabs.minigamesapi.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class ArenasConfig {

	private FileConfiguration arenaConfig = null;
	private File arenaFile = null;
	private JavaPlugin plugin = null;

	public ArenasConfig(JavaPlugin plugin) {
		this.plugin = plugin;
		this.getConfig().options().header("Used for saving arena details.");
		// this.getConfig().options().copyDefaults(true);
		this.saveConfig();
	}

	public FileConfiguration getConfig() {
		if (this.arenaConfig == null) {
			reloadConfig();
		}
		return this.arenaConfig;
	}

	public void saveConfig() {
		if (this.arenaConfig == null ||this. arenaFile == null) {
			return;
		}
		try {
			getConfig().save(this.arenaFile);
		} catch (IOException ex) {

		}
	}

	public void reloadConfig() {
		if (this.arenaFile == null) {
			this.arenaFile = new File(plugin.getDataFolder(), "arenas.yml");
		}
		this.arenaConfig = YamlConfiguration.loadConfiguration(arenaFile);

		InputStream defConfigStream = plugin.getResource("arenas.yml");
		if (defConfigStream != null) {
			YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
			this.arenaConfig.setDefaults(defConfig);
		}
	}

}
