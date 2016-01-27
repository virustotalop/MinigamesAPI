package com.comze_instancelabs.minigamesapi.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class StatsGlobalConfig {

    private FileConfiguration statsConfig = null;
    private File statsFile = null;
    private JavaPlugin plugin = null;
    
    public StatsGlobalConfig(JavaPlugin plugin, boolean custom){
    	this.plugin = plugin;
    	if(!custom){
    		this.getConfig().options().header("Used for saving user statistics. Example user stats:");
        	this.getConfig().addDefault("players.3c8c41ff-51f5-4b7a-8c2b-44df0beba03b.wins", 1);
        	this.getConfig().addDefault("players.3c8c41ff-51f5-4b7a-8c2b-44df0beba03b.loses", 1);
        	this.getConfig().addDefault("players.3c8c41ff-51f5-4b7a-8c2b-44df0beba03b.points", 10);
        	this.getConfig().addDefault("players.3c8c41ff-51f5-4b7a-8c2b-44df0beba03b.playername", "InstanceLabs");
    	}
    	this.getConfig().options().copyDefaults(true);
    	this.saveConfig();
    }
    
    public FileConfiguration getConfig() {
        if (this.statsConfig == null) {
            reloadConfig();
        }
        return this.statsConfig;
    }
    
    public void saveConfig() {
        if (this.statsConfig == null || this.statsFile == null) {
            return;
        }
        try {
            getConfig().save(statsFile);
        } catch (IOException ex) {
            
        }
    }
    
    public void reloadConfig() {
        if (this.statsFile == null) {
        	this.statsFile = new File(plugin.getDataFolder(), "global_stats.yml");
        }
        this.statsConfig = YamlConfiguration.loadConfiguration(this.statsFile);

        InputStream defConfigStream = this.plugin.getResource("global_stats.yml");
        if (defConfigStream != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
            this.statsConfig.setDefaults(defConfig);
        }
    }
    
}
