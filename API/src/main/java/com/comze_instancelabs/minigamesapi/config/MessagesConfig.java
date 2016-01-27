package com.comze_instancelabs.minigamesapi.config;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import com.comze_instancelabs.minigamesapi.ArenaState;

public class MessagesConfig {

	private FileConfiguration messagesConfig = null;
	private File messagesFile = null;
	private JavaPlugin plugin = null;

	public MessagesConfig(JavaPlugin plugin) {
		this.plugin = plugin;
		Arrays.fill(MessagesConfig.squaresMid, (char) 0x25A0);
		Arrays.fill(MessagesConfig.squaresFull, (char) 0x2588);
		Arrays.fill(MessagesConfig.squaresMedium, (char) 0x2592);
		Arrays.fill(MessagesConfig.squaresLight, (char) 0x2591);
		for (int i = 0; i < 10; i++) {
			MessagesConfig.squares += Character.toString((char) 0x25A0);
		}
		this.init();
	}

	public static String squares = Character.toString((char) 0x25A0);

	public static char[] squaresMid = new char[10];
	public static char[] squaresFull = new char[10];
	public static char[] squaresMedium = new char[10];
	public static char[] squaresLight = new char[10];

	public void init() {
		// all signs
		this.getConfig().options().header("Contains all messages for easy translation. You can remove a mesage by setting it to nothing, like so: ''");
		HashMap<String, String> namecol = ArenaState.getAllStateNameColors();
		for (String state : namecol.keySet()) {
			String color = namecol.get(state);
			this.getConfig().addDefault("signs." + state.toLowerCase() + ".0", color + "[]");
			this.getConfig().addDefault("signs." + state.toLowerCase() + ".1", color + "<arena>");
			this.getConfig().addDefault("signs." + state.toLowerCase() + ".2", color + "<count>/<maxcount>");
			this.getConfig().addDefault("signs." + state.toLowerCase() + ".3", color + "[]");
		}

		// Arcade sign (I think that one is unused)
		this.getConfig().addDefault("signs.arcade.0", "[]");
		this.getConfig().addDefault("signs.arcade.1", "&cArcade");
		this.getConfig().addDefault("signs.arcade.2", "<count>/<maxcount>");
		this.getConfig().addDefault("signs.arcade.3", "[]");

		// Leave sign
		this.getConfig().addDefault("signs.leave.0", "");
		this.getConfig().addDefault("signs.leave.1", "&4Leave");
		this.getConfig().addDefault("signs.leave.2", "");
		this.getConfig().addDefault("signs.leave.3", "");

		// Random arena sign
		this.getConfig().addDefault("signs.random.0", "&a[]");
		this.getConfig().addDefault("signs.random.1", "&2Random");
		this.getConfig().addDefault("signs.random.2", "");
		this.getConfig().addDefault("signs.random.3", "&a[]");

		this.getConfig().addDefault("messages.no_perm", noPermission);
		this.getConfig().addDefault("messages.successfully_reloaded", successfully_reloaded);
		this.getConfig().addDefault("messages.successfully_set", successfullySet);
		this.getConfig().addDefault("messages.successfully_saved_arena", successfully_saved_arena);
		this.getConfig().addDefault("messages.arena_invalid", arenaInvalid);
		this.getConfig().addDefault("messages.failed_saving_arena", failed_saving_arena);
		this.getConfig().addDefault("messages.broadcast_players_left", broadcastPlayersLeft);
		this.getConfig().addDefault("messages.broadcast_player_joined", broadcast_player_joined);
		this.getConfig().addDefault("messages.player_died", player_died);
		this.getConfig().addDefault("messages.arena_action", arenaAction);
		this.getConfig().addDefault("messages.you_already_are_in_arena", youAreAlreadyInAnArena);
		this.getConfig().addDefault("messages.you_joined_arena", you_joined_arena);
		this.getConfig().addDefault("messages.not_in_arena", not_in_arena);
		this.getConfig().addDefault("messages.teleporting_to_arena_in", teleporting_to_arena_in);
		this.getConfig().addDefault("messages.starting_in", starting_in);
		this.getConfig().addDefault("messages.failed_removing_arena", failed_removing_arena);
		this.getConfig().addDefault("messages.successfully_removed", successfullyRemoved);
		this.getConfig().addDefault("messages.failed_removing_component", failedRemovingComponent);
		this.getConfig().addDefault("messages.joined_arena", joined_arena);
		this.getConfig().addDefault("messages.you_won", youWon);
		this.getConfig().addDefault("messages.you_lost", youLost);
		this.getConfig().addDefault("messages.you_got_a_kill", you_got_a_kill);
		this.getConfig().addDefault("messages.player_was_killed_by", player_was_killed_by);
		this.getConfig().addDefault("messages.arena_not_initialized", arena_not_initialized);
		this.getConfig().addDefault("messages.guns.attributelevel_increased", attributelevel_increased);
		this.getConfig().addDefault("messages.guns.not_enough_credits", not_enough_credits);
		this.getConfig().addDefault("messages.guns.too_many_main_guns", too_many_main_guns);
		this.getConfig().addDefault("messages.guns.successfully_set_main_gun", successfully_set_main_gun);
		this.getConfig().addDefault("messages.guns.all_guns", all_guns);
		this.getConfig().addDefault("messages.arcade_next_minigame", arcade_next_minigame);
		this.getConfig().addDefault("messages.arcade_joined_waiting", arcade_joined_waiting);
		this.getConfig().addDefault("messages.arcade_joined_spectator", arcade_joined_spectator);
		this.getConfig().addDefault("messages.arcade_new_round", arcade_new_round);
		this.getConfig().addDefault("messages.arena_disabled", arenaDisabled);
		this.getConfig().addDefault("messages.you_can_leave_with", you_can_leave_with);
		this.getConfig().addDefault("messages.no_perm_to_join_arena", noPermToJoinArena);
		this.getConfig().addDefault("messages.set_kit", set_kit);
		this.getConfig().addDefault("messages.classes_item", classes_item);
		this.getConfig().addDefault("messages.achievement_item", achievement_item);
		this.getConfig().addDefault("messages.shop_item", shop_item);
		this.getConfig().addDefault("messages.spectator_item", spectatorItem);
		this.getConfig().addDefault("messages.server_broadcast_winner", serverBroadcastWinner);
		this.getConfig().addDefault("messages.exit_item", exitItem);
		this.getConfig().addDefault("messages.successfully_bought_kit", successfully_bought_kit);
		this.getConfig().addDefault("messages.scoreboard.title", scoreboard_title);
		this.getConfig().addDefault("messages.scoreboard.lobby_title", scoreboard_lobby_title);
		this.getConfig().addDefault("messages.you_got_kicked_because_vip_joined", you_got_kicked_because_vip_joined);
		this.getConfig().addDefault("messages.powerup_spawned", powerup_spawned);
		if (!this.getConfig().isSet("config.generatedv182")) {
			this.getConfig().addDefault("messages.custom_scoreboard.line0", "Players:<playercount>");
			this.getConfig().addDefault("messages.custom_scoreboard.line1", "Spectators:<lostplayercount>");
			this.getConfig().addDefault("messages.custom_scoreboard.line2", "Alive:<playeralivecount>");
			this.getConfig().addDefault("messages.custom_scoreboard.line3", "Your Credits:<points>");
			this.getConfig().addDefault("messages.custom_scoreboard.line4", "Your Wins:<wins>");
			this.getConfig().addDefault("messages.custom_lobby_scoreboard.line0", "Players:<playercount>");
			this.getConfig().addDefault("messages.custom_lobby_scoreboard.line1", "Max Players:<maxplayercount>");
			this.getConfig().addDefault("messages.custom_lobby_scoreboard.line2", "Your Credits:<points>");
			this.getConfig().addDefault("messages.custom_lobby_scoreboard.line3", "Your Wins:<wins>");
		}
		this.getConfig().addDefault("messages.you_got_the_achievement", youGotTheAchievement);
		this.getConfig().addDefault("messages.game_started", game_started);
		this.getConfig().addDefault("messages.author_of_the_map", author_of_the_map);
		this.getConfig().addDefault("messages.description_of_the_map", description_of_the_map);
		this.getConfig().addDefault("messages.not_enough_money", not_enough_money);
		this.getConfig().addDefault("messages.possible_kits", possible_kits);
		this.getConfig().addDefault("messages.possible_shopitems", possible_shopitems);
		this.getConfig().addDefault("messages.cancelled_starting", cancelled_starting);
		this.getConfig().addDefault("messages.minigame_description", minigame_description);
		this.getConfig().addDefault("messages.successfully_bought_shopitem", successfully_bought_shopitem);
		this.getConfig().addDefault("messages.already_bought_shopitem", already_bought_shopitem);
		this.getConfig().addDefault("messages.you_received_rewards", youReceivedRewards);
		this.getConfig().addDefault("messages.you_received_rewards_2", youReceivedRewardsTwo);
		this.getConfig().addDefault("messages.you_received_rewards_3", youReceivedRewardsThree);
		this.getConfig().addDefault("messages.already_in_arena", alreadyInArena);
		this.getConfig().addDefault("messages.stop_cause_maximum_game_time", stop_cause_maximum_game_time);
		this.getConfig().addDefault("messages.compass.no_player_found", compass_no_player_found);
		this.getConfig().addDefault("messages.compass.found_player", compass_player_found);
		this.getConfig().addDefault("messages.you_got_a_participation_reward", youGotAParticipationReward);
		this.getConfig().addDefault("messages.kit_warning", kit_warning);
		if (!this.getConfig().isSet("config.generatedv1102")) {
			this.getConfig().addDefault("messages.stats.line0", "&7----- &a&lStats &7-----; ");
			this.getConfig().addDefault("messages.stats.line1", "&7Wins: &a<wins>");
			this.getConfig().addDefault("messages.stats.line2", "&7Loses: &c<loses>");
			this.getConfig().addDefault("messages.stats.line3", "&7Alltime Kills: &a<alltime_kills>");
			this.getConfig().addDefault("messages.stats.line4", "&7Alltime Deaths: &c<alltime_deaths>");
			this.getConfig().addDefault("messages.stats.line5", "&7KDR: &e<kdr>");
			this.getConfig().addDefault("messages.stats.line6", "&7Points: &e<points>");
			this.getConfig().addDefault("messages.stats.line7", " ;&7-----------------");
		}

		// save
		this.getConfig().options().copyDefaults(true);
		this.saveConfig();

		// load
		this.noPermission = ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.no_perm"));
		this.successfully_reloaded = ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.successfully_reloaded"));
		this.successfullySet = ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.successfully_set"));
		this.successfully_saved_arena = ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.successfully_saved_arena"));
		this.failed_saving_arena = ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.failed_saving_arena"));
		this.arenaInvalid = ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.arena_invalid"));
		this.player_died = ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.player_died"));
		this.broadcastPlayersLeft = ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.broadcast_players_left"));
		this.broadcast_player_joined = ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.broadcast_player_joined"));
		this.arenaAction = ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.arena_action"));
		this.youAreAlreadyInAnArena = ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.you_already_are_in_arena"));
		this.you_joined_arena = ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.you_joined_arena"));
		this.not_in_arena = ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.not_in_arena"));
		this.teleporting_to_arena_in = ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.teleporting_to_arena_in"));
		this.starting_in = ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.starting_in"));
		this.failed_removing_arena = ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.failed_removing_arena"));
		this.successfullyRemoved = ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.successfully_removed"));
		this.failedRemovingComponent = ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.failed_removing_component"));
		this.joined_arena = ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.joined_arena"));
		this.youWon = ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.you_won"));
		this.youLost = ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.you_lost"));
		this.you_got_a_kill = ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.you_got_a_kill"));
		this.player_was_killed_by = ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.player_was_killed_by"));
		this.arena_not_initialized = ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.arena_not_initialized"));
		this.arcade_next_minigame = ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.arcade_next_minigame"));
		this.arcade_new_round = ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.arcade_new_round"));
		this.arenaDisabled = ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.arena_disabled"));
		this.you_can_leave_with = ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.you_can_leave_with"));
		this.arcade_joined_waiting = ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.arcade_joined_waiting"));
		this.arcade_joined_spectator = ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.arcade_joined_spectator"));
		this.noPermToJoinArena = ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.no_perm_to_join_arena"));
		this.set_kit = ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.set_kit"));
		this.classes_item = ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.classes_item"));
		this.achievement_item = ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.achievement_item"));
		this.shop_item = ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.shop_item"));
		this.spectatorItem = ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.spectator_item"));
		this.serverBroadcastWinner = ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.server_broadcast_winner"));
		this.exitItem = ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.exit_item"));
		this.successfully_bought_kit = ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.successfully_bought_kit"));
		this.scoreboard_title = ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.scoreboard.title"));
		this.scoreboard_lobby_title = ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.scoreboard.lobby_title"));
		this.you_got_kicked_because_vip_joined = ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.you_got_kicked_because_vip_joined"));
		this.powerup_spawned = ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.powerup_spawned"));
		this.youGotTheAchievement = ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.you_got_the_achievement"));
		this.game_started = ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.game_started"));
		this.author_of_the_map = ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.author_of_the_map"));
		this.description_of_the_map = ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.description_of_the_map"));
		this.not_enough_money = ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.not_enough_money"));
		this.possible_kits = ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.possible_kits"));
		this.possible_shopitems = ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.possible_shopitems"));
		this.cancelled_starting = ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.cancelled_starting"));
		this.minigame_description = ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.minigame_description"));
		this.successfully_bought_shopitem = ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.successfully_bought_shopitem"));
		this.already_bought_shopitem = ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.already_bought_shopitem"));
		this.youReceivedRewards = ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.you_received_rewards"));
		this.youReceivedRewardsTwo = ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.you_received_rewards_2"));
		this.youReceivedRewardsThree = ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.you_received_rewards_3"));
		this.alreadyInArena = ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.already_in_arena"));
		this.stop_cause_maximum_game_time = ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.stop_cause_maximum_game_time"));
		this.compass_no_player_found = ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.compass.no_player_found"));
		this.compass_player_found = ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.compass.found_player"));
		this.youGotAParticipationReward = ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.you_got_a_participation_reward"));
		this.kit_warning = ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.kit_warning"));

		this.attributelevel_increased = ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.guns.attributelevel_increased"));
		this.not_enough_credits = ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.guns.not_enough_credits"));
		this.too_many_main_guns = ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.guns.too_many_main_guns"));
		this.successfully_set_main_gun = ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.guns.successfully_set_main_gun"));
		this.all_guns = ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.guns.all_guns"));

		this.getConfig().set("config.generatedv182", true);
		this.getConfig().set("config.generatedv1102", true);
		this.saveConfig();
	}

	public String noPermission = "&cYou don't have permission.";
	public String successfully_reloaded = "&aSuccessfully reloaded all configs.";
	public String successfullySet = "&aSuccessfully set &3<component>&a.";
	public String successfully_saved_arena = "&aSuccessfully saved &3<arena>&a.";
	public String failed_saving_arena = "&cFailed to save &3<arena>&c.";
	public String failed_removing_arena = "&cFailed to remove &3<arena>&c.";
	public String arenaInvalid = "&3<arena> &cappears to be invalid.";
	public String broadcastPlayersLeft = "&eThere are &4<count> &eplayers left!";
	public String broadcast_player_joined = "&2<player> &ajoined the arena! (<count>/<maxcount>)";
	public String player_died = "&c<player> died.";
	public String arenaAction = "&aYou <action> arena &3<arena>&a!";
	public String you_joined_arena = "&aYou joined arena &3<arena>&a!";
	public String youAreAlreadyInAnArena = "&aYou already seem to be in arena &3<arena>&a!";
	public String arena_not_initialized = "&cThe arena appears to be not initialized, did you save the arena?";
	public String not_in_arena = "&cYou don't seem to be in an arena right now.";
	public String teleporting_to_arena_in = "&7Teleporting to arena in <count>.";
	public String starting_in = "&aStarting in <count>!";
	public String successfullyRemoved = "&cSuccessfully removed &3<component>&c!";
	public String failedRemovingComponent = "&cFailed removing &3<component>&c. <cause>.";
	public String joined_arena = "&aYou joined &3<arena>&a.";
	public String youWon = "&aYou &2won &athe game!";
	public String youLost = "&cYou &4lost &cthe game.";
	public String you_got_a_kill = "&aYou killed &2<player>!";
	public String player_was_killed_by = "&4<player> &cwas killed by &4<killer>&c!";
	public String attributelevel_increased = "&aThe <attribute> level was increased successfully!";
	public String not_enough_credits = "&cThe max level of 3 was reached or you don't have enough credits. Needed: <credits>";
	public String too_many_main_guns = "&cYou already have 2 main guns, remove one first.";
	public String successfully_set_main_gun = "&aSuccessfully set a main gun (of a maximum of two).";
	public String arcade_next_minigame = "&6Next Arcade game: &4<minigame>&6!";
	public String arenaDisabled = "&cThe arena is disabled thus you can't join.";
	public String all_guns = "&aYour current main guns: &2<guns>";
	public String you_can_leave_with = "&cYou can leave with <cmd> or /l!";
	public String arcade_joined_spectator = "&6You joined Arcade as a spectator! You'll be able to play in the next minigame.";
	public String arcade_joined_waiting = "&6You joined Arcade! Waiting for <count> more players to start.";
	public String arcade_new_round = "&6Next Arcade round in <count>!";
	public String noPermToJoinArena = "&cYou don't have permission (arenas.<arena>) to join this arena as it's vip!";
	public String set_kit = "&aSuccessfully set &2<kit>&a!";
	public String classes_item = "&4Classes";
	public String achievement_item = "&4Achievements";
	public String shop_item = "&4Shop";
	public String spectatorItem = "&4Players";
	public String serverBroadcastWinner = "&2<player> &awon the game on &2<arena>&a!";
	public String exitItem = "&4Leave the game";
	public String successfully_bought_kit = "&aSuccessfully bought &2<kit> &afor &2<money>&a.";
	public String scoreboard_title = "&4<arena>";
	public String scoreboard_lobby_title = "&4[<arena>]";
	public String you_got_kicked_because_vip_joined = "&cYou got kicked out of the game because a vip joined!";
	public String powerup_spawned = "&2A Powerup spawned!";
	public String youGotTheAchievement = "&3You got the achievement &b<achievement>&3!";
	public String game_started = "&2The game has started!";
	public String author_of_the_map = "&3You are playing on the map &b<arena> &3by &b<author>&3!";
	public String description_of_the_map = "<description>";
	public String not_enough_money = "&cYou don't have enough money.";
	public String possible_kits = "&aPossible kits: &2";
	public String possible_shopitems = "&aPossible shop items: &2";
	public String cancelled_starting = "&cThe starting countdown was cancelled because there's only one player left in the arena.";
	public String minigame_description = "";
	public String successfully_bought_shopitem = "&aSuccessfully bought &2<shopitem> &afor &2<money>&a.";
	public String already_bought_shopitem = "&aYou already had &2<shopitem>&a.";
	public String youReceivedRewards = "&aYou received a reward of &2<economyreward>";
	public String youReceivedRewardsTwo = " &aand ";
	public String youReceivedRewardsThree = "&2<itemreward>&a!";
	public String alreadyInArena = "&cYou are already in an arena.";
	public String stop_cause_maximum_game_time = "&cThe game is stopping in 5 seconds because the maximum game time was reached.";
	public String compass_no_player_found = "&cNo near players found!";
	public String compass_player_found = "&aThe compass is tracking &3<player> &anow. Distance: <distance>";
	public String youGotAParticipationReward = "&aYou received &2<economyreward> &afor participating!";
	public String kit_warning = "&7Be aware that you'll only get the &8last &7kit you bought even if you buy all of them.";

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
			getConfig().save(messagesFile);
		} catch (IOException ex) {

		}
	}

	public void reloadConfig() {
		if (this.messagesFile == null) {
			this.messagesFile = new File(plugin.getDataFolder(), "messages.yml");
		}
		this.messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);

		InputStream defConfigStream = plugin.getResource("messages.yml");
		if (defConfigStream != null) {
			YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
			this.messagesConfig.setDefaults(defConfig);
		}
	}

}
