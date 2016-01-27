package com.comze_instancelabs.minigamesapi.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class GunsConfig {

	private FileConfiguration arenaConfig = null;
	private File arenaFile = null;
	private JavaPlugin plugin = null;
	
	// TODO add bullet option
	
	public GunsConfig(JavaPlugin plugin, boolean custom) {
		this.plugin = plugin;
		if (!custom) {
			this.getConfig().options().header("Be aware that this config is barely used in any minigame. \nUsed for saving gun classes. Default:");
			this.getConfig().addDefault("config.guns.pistol.name", "Pistol");
			this.getConfig().addDefault("config.guns.pistol.items", "256#DAMAGE_ALL:1#KNOCKBACK*1");
			this.getConfig().addDefault("config.guns.pistol.icon", "256#DAMAGE_ALL:1#KNOCKBACK*1");
			this.getConfig().addDefault("config.guns.pistol.lore", "The Pistol.");
			this.getConfig().addDefault("config.guns.pistol.speed", 1D);
			this.getConfig().addDefault("config.guns.pistol.durability", 50);
			this.getConfig().addDefault("config.guns.pistol.shoot_amount", 1);
			this.getConfig().addDefault("config.guns.pistol.knockback_multiplier", 1.1D);
			this.getConfig().addDefault("config.guns.pistol.permission_node", "minigames.guns.pistol");

			this.getConfig().addDefault("config.guns.sniper.name", "Sniper");
			this.getConfig().addDefault("config.guns.sniper.items", "292#DAMAGE_ALL:1#KNOCKBACK*1");
			this.getConfig().addDefault("config.guns.sniper.icon", "292#DAMAGE_ALL:1#KNOCKBACK*1");
			this.getConfig().addDefault("config.guns.sniper.lore", "The Sniper.");
			this.getConfig().addDefault("config.guns.sniper.speed", 0.5D);
			this.getConfig().addDefault("config.guns.sniper.durability", 10);
			this.getConfig().addDefault("config.guns.sniper.shoot_amount", 1);
			this.getConfig().addDefault("config.guns.sniper.knockback_multiplier", 3D);
			this.getConfig().addDefault("config.guns.sniper.permission_node", "minigames.guns.sniper");

			this.getConfig().addDefault("config.guns.grenade.name", "Grenade Launcher");
			this.getConfig().addDefault("config.guns.grenade.items", "257#DAMAGE_ALL:1#KNOCKBACK*1");
			this.getConfig().addDefault("config.guns.grenade.icon", "257#DAMAGE_ALL:1#KNOCKBACK*1");
			this.getConfig().addDefault("config.guns.grenade.lore", "The Grenade Launcher.");
			this.getConfig().addDefault("config.guns.grenade.speed", 0.1D);
			this.getConfig().addDefault("config.guns.grenade.durability", 10);
			this.getConfig().addDefault("config.guns.grenade.shoot_amount", 1);
			this.getConfig().addDefault("config.guns.grenade.knockback_multiplier", 2.5D);
			this.getConfig().addDefault("config.guns.grenade.permission_node", "minigames.guns.grenade");

			this.getConfig().addDefault("config.guns.freeze.name", "Freeze Gun");
			this.getConfig().addDefault("config.guns.freeze.items", "258#DAMAGE_ALL:1#KNOCKBACK*1");
			this.getConfig().addDefault("config.guns.freeze.icon", "258#DAMAGE_ALL:1#KNOCKBACK*1");
			this.getConfig().addDefault("config.guns.freeze.lore", "The Freeze Gun.");
			this.getConfig().addDefault("config.guns.freeze.speed", 0.8D);
			this.getConfig().addDefault("config.guns.freeze.durability", 5);
			this.getConfig().addDefault("config.guns.freeze.shoot_amount", 1);
			this.getConfig().addDefault("config.guns.freeze.knockback_multiplier", 0.5D);
			this.getConfig().addDefault("config.guns.freeze.permission_node", "minigames.guns.freeze");
		}
		this.getConfig().options().copyDefaults(true);
		this.saveConfig();
	}

	public FileConfiguration getConfig() {
		if (this.arenaConfig == null) {
			reloadConfig();
		}
		return this.arenaConfig;
	}

	public void saveConfig() {
		if (this.arenaConfig == null || arenaFile == null) {
			return;
		}
		try {
			getConfig().save(arenaFile);
		} catch (IOException ex) {

		}
	}

	public void reloadConfig() {
		if (this.arenaFile == null) {
			this.arenaFile = new File(plugin.getDataFolder(), "guns.yml");
		}
		this.arenaConfig = YamlConfiguration.loadConfiguration(this.arenaFile);

		InputStream defConfigStream = this.plugin.getResource("guns.yml");
		if (defConfigStream != null) {
			YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
			this.arenaConfig.setDefaults(defConfig);
		}
	}

}
