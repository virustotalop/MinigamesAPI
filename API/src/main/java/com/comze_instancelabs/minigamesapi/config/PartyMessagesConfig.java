package com.comze_instancelabs.minigamesapi.config;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class PartyMessagesConfig {

	private FileConfiguration messagesConfig = null;
	private File messagesFile = null;
	private JavaPlugin plugin = null;

	public PartyMessagesConfig(JavaPlugin plugin) {
		this.plugin = plugin;
		this.init();
	}

	public void init() {
		this.getConfig().addDefault("messages.cannot_invite_yourself", cannotInviteYourself);
		this.getConfig().addDefault("messages.player_not_online", playerNotOnline);
		this.getConfig().addDefault("messages.you_invited", youInvited);
		this.getConfig().addDefault("messages.you_were_invited", youWereInvited);
		this.getConfig().addDefault("messages.not_invited_to_any_party", notInvitedToAnyParty);
		this.getConfig().addDefault("messages.not_invited_to_players_party", notInvitedToPlayersParty);
		this.getConfig().addDefault("messages.player_not_in_party", playerNotInParty);
		this.getConfig().addDefault("messages.you_joined_party", youJoinedParty);
		this.getConfig().addDefault("messages.player_joined_party", playerJoinedParty);
		this.getConfig().addDefault("messages.you_left_party", youLeftParty);
		this.getConfig().addDefault("messages.player_left_party", playerLeftParty);
		this.getConfig().addDefault("messages.party_disbanded", partyDisbanded);
		this.getConfig().addDefault("messages.party_too_big_to_join", partyTooBigToJoin);

		// save
		this.getConfig().options().copyDefaults(true);
		this.saveConfig();

		// load
		this.cannotInviteYourself = ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.cannot_invite_yourself"));
		this.playerNotOnline = ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.player_not_online"));
		this.youInvited = ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.you_invited"));
		this.youWereInvited = ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.you_were_invited"));
		this.notInvitedToAnyParty = ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.not_invited_to_any_party"));
		this.notInvitedToPlayersParty = ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.not_invited_to_players_party"));
		this.playerNotInParty = ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.player_not_in_party"));
		this.youJoinedParty = ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.you_joined_party"));
		this.playerJoinedParty = ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.player_joined_party"));
		this.youLeftParty = ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.you_left_party"));
		this.playerLeftParty = ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.player_left_party"));
		this.partyDisbanded = ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.party_disbanded"));
		this.partyTooBigToJoin = ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.party_too_big_to_join"));

	}

	public String cannotInviteYourself = "&cYou cannot invite yourself!";
	public String playerNotOnline = "&4<player> &cis not online!";
	public String youInvited = "&aYou invited &2<player>&a!";
	public String youWereInvited = "&2<player> &ainvited you to join his/her party! Type &2/party accept <player> &ato accept.";
	public String notInvitedToAnyParty = "&cYou are not invited to any party.";
	public String notInvitedToPlayersParty = "&cYou are not invited to the party of &4<player>&c.";
	public String playerNotInParty = "&4<player> &cis not in your party.";
	public String youJoinedParty = "&7You joined the party of &8<player>&7.";
	public String playerJoinedParty = "&2<player> &ajoined the party.";
	public String youLeftParty = "&7You left the party of &8<player>&7.";
	public String playerLeftParty = "&4<player> &cleft the party.";
	public String partyDisbanded = "&cThe party was disbanded.";
	public String partyTooBigToJoin = "&cYour party is too big to join this arena.";

	public FileConfiguration getConfig() {
		if (this.messagesConfig == null) {
			reloadConfig();
		}
		return this.messagesConfig;
	}

	public void saveConfig() {
		if (this.messagesConfig == null || this.messagesFile == null) {
			return;
		}
		try {
			getConfig().save(this.messagesFile);
		} catch (IOException ex) {

		}
	}

	public void reloadConfig() {
		if (this.messagesFile == null) {
			this.messagesFile = new File(plugin.getDataFolder(), "partymessages.yml");
		}
		this.messagesConfig = YamlConfiguration.loadConfiguration(this.messagesFile);

		InputStream defConfigStream = this.plugin.getResource("partymessages.yml");
		if (defConfigStream != null) {
			YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
			this.messagesConfig.setDefaults(defConfig);
		}
	}

}
