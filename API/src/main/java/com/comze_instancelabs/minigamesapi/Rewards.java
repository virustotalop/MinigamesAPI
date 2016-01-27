package com.comze_instancelabs.minigamesapi;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import com.comze_instancelabs.minigamesapi.util.Util;
import com.comze_instancelabs.minigamesapi.util.Validator;

public class Rewards {

	private JavaPlugin plugin = null;

	private boolean economyrewards;
	private boolean itemrewards;
	private boolean commandrewards;
	private boolean killEconomyRewards;
	private boolean killCommandRewards;
	private boolean participationEconomyRewards;
	private boolean participationCommandRewards;

	private int economyReward = 0;
	private int killEconomyReward = 0;
	private int participationEconomyReward = 0;
	private String command = "";
	private String killCommand = "";
	private String participationCommand = "";
	private ItemStack[] items = null;

	public Rewards(JavaPlugin plugin) {
		this.plugin = plugin;
		this.reloadVariables();

		if (!MinigamesAPI.economy) {
			this.economyrewards = false;
			this.killEconomyRewards = false;
			this.participationEconomyRewards = false;
		}
	}

	public void reloadVariables() {
		this.economyrewards = plugin.getConfig().getBoolean("config.rewards.economy");
		this.itemrewards = plugin.getConfig().getBoolean("config.rewards.item_reward");
		this.commandrewards = plugin.getConfig().getBoolean("config.rewards.command_reward");
		this.killEconomyRewards = plugin.getConfig().getBoolean("config.rewards.economy_for_kills");
		this.killCommandRewards = plugin.getConfig().getBoolean("config.rewards.command_reward_for_kills");
		this.participationEconomyRewards = plugin.getConfig().getBoolean("config.rewards.economy_for_participation");
		this.participationCommandRewards = plugin.getConfig().getBoolean("config.rewards.command_reward_for_participation");

		this.economyReward = plugin.getConfig().getInt("config.rewards.economy_reward");
		this.command = plugin.getConfig().getString("config.rewards.command");
		this.items = Util.parseItems(plugin.getConfig().getString("config.rewards.item_reward_ids")).toArray(new ItemStack[0]);
		this.killEconomyReward = plugin.getConfig().getInt("config.rewards.economy_reward_for_kills");
		this.killCommand = plugin.getConfig().getString("config.rewards.command_for_kills");
		this.participationEconomyReward = plugin.getConfig().getInt("config.rewards.economy_reward_for_participation");
		this.participationCommand = plugin.getConfig().getString("config.rewards.command_for_participation");
	}

	/**
	 * Give all win rewards to players who won the game
	 * 
	 * @param arena
	 *            Arena
	 */
	public void giveRewardsToWinners(Arena arena) {
		for (String p_ : arena.getAllPlayers()) {
			giveWinReward(p_, arena);
		}
	}

	@Deprecated
	public void giveReward(String p_) {
		if (Validator.isPlayerOnline(p_)) {
			Player p = Bukkit.getPlayer(p_);

			if (economyrewards) {
				MinigamesAPI.econ.depositPlayer(p.getName(), economyReward);
			}

			MinigamesAPI.getAPI().getPluginInstance(plugin).getStatsInstance().win(p_, 10);
		}
	}

	/**
	 * Give a player a kill reward
	 * 
	 * @param p_
	 *            Playername
	 * @param reward
	 *            Amount of statistics points the player gets
	 */
	public void giveKillReward(String p_) {
		if (Validator.isPlayerOnline(p_)) {
			PluginInstance pli = MinigamesAPI.getAPI().getPluginInstance(plugin);
			Player p = Bukkit.getPlayer(p_);

			if (killEconomyRewards && MinigamesAPI.economy) {
				MinigamesAPI.econ.depositPlayer(p.getName(), killEconomyReward);
			}
			if (killCommandRewards) {
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), killCommand.replaceAll("<player>", p_));
			}

			pli.getStatsInstance().addPoints(p_, pli.getStatsInstance().getStatsKillPoints());
			pli.getStatsInstance().addKill(p_);
			pli.getSQLInstance().updateWinnerStats(p, pli.getStatsInstance().getStatsKillPoints(), false);
		}
	}

	@Deprecated
	public void giveKillReward(String p_, int reward) {
		this.giveKillReward(p_);
	}

	/**
	 * Gives a player an achievement reward
	 * 
	 * @param p_
	 *            Playername
	 * @param econ
	 *            Whether economy rewards are enabled
	 * @param command
	 *            Whether command rewards are enabled
	 * @param money_reward
	 *            Amount of money to reward if economy rewards are enabled
	 * @param cmd
	 *            Command to execute if command rewards are enabled
	 */
	public void giveAchievementReward(String p_, boolean econ, boolean command, int money_reward, String cmd) {
		if (Validator.isPlayerOnline(p_)) {
			Player p = Bukkit.getPlayer(p_);

			if (econ && MinigamesAPI.economy) {
				MinigamesAPI.econ.depositPlayer(p.getName(), money_reward);
			}
			if (command) {
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replaceAll("<player>", p_));
			}
		}
	}

	public void giveWinReward(String p_, Arena a) {
		giveWinReward(p_, a, 1);
	}

	public void giveWinReward(String p_, Arena a, int global_multiplier) {
		giveWinReward(p_, a, a.getAllPlayers(), global_multiplier);
	}

	/**
	 * Gives all rewards to a player who won and sends reward messages/win broadcasts
	 * 
	 * @param p_
	 *            Playername
	 * @param a
	 *            Arena
	 * @param players
	 *            Optional array of players to send win broadcast to
	 * @param global_multiplier
	 *            Money reward multiplier (default: 1)
	 */
	public void giveWinReward(String p_, Arena a, ArrayList<String> players, int global_multiplier) {
		if (Validator.isPlayerOnline(p_)) {
			PluginInstance pli = MinigamesAPI.getAPI().getPluginInstance(plugin);
			final Player p = Bukkit.getPlayer(p_);
			if (!pli.getGlobalLost().containsKey(p_)) {
				String received_rewards_msg = pli.getMessagesConfig().youReceivedRewards;
				if (this.economyrewards && MinigamesAPI.economy) {
					int multiplier = global_multiplier;
					if (pli.getShopHandler().hasItemBought(p_, "coin_boost2_solo")) {
						multiplier = 2;
					}
					if (pli.getShopHandler().hasItemBought(p_, "coin_boost3_solo")) {
						multiplier = 3;
					}
					MinigamesAPI.econ.depositPlayer(p.getName(), this.economyReward * multiplier);
					received_rewards_msg = received_rewards_msg.replaceAll("<economyreward>", Integer.toString(this.economyReward * multiplier) + " " + MinigamesAPI.econ.currencyNamePlural());
				} else {
					received_rewards_msg = received_rewards_msg.replaceAll("<economyreward>", "");
				}
				if (this.itemrewards) {
					p.getInventory().addItem(this.items);
					p.updateInventory();
					String items_str = "";
					for (ItemStack i : this.items) {
						items_str += Integer.toString(i.getAmount()) + " " + Character.toUpperCase(i.getType().toString().charAt(0)) + i.getType().toString().toLowerCase().substring(1) + ", ";
					}
					if (items_str.length() > 2) {
						items_str = items_str.substring(0, items_str.length() - 2);
					}
					if (this.economyrewards && MinigamesAPI.economy) {
						received_rewards_msg += pli.getMessagesConfig().youReceivedRewardsTwo;
						received_rewards_msg += pli.getMessagesConfig().youReceivedRewardsThree.replaceAll("<itemreward>", items_str);
					} else {
						received_rewards_msg += pli.getMessagesConfig().youReceivedRewardsThree.replaceAll("<itemreward>", items_str);
					}
				} else {
					received_rewards_msg += pli.getMessagesConfig().youReceivedRewardsThree.replaceAll("<itemreward>", "");
				}
				if (this.commandrewards) {
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(), this.command.replaceAll("<player>", p_));
				}

				pli.getStatsInstance().win(p_, pli.getStatsInstance().getStatsWinPoints());

				try {
					if (this.plugin.getConfig().getBoolean("config.broadcast_win")) {
						String msgs[] = pli.getMessagesConfig().serverBroadcastWinner.replaceAll("<player>", p_).replaceAll("<arena>", a.getInternalName()).split(";");
						for (String msg : msgs) {
							Bukkit.getServer().broadcastMessage(msg);
						}
					} else {
						String msgs[] = pli.getMessagesConfig().serverBroadcastWinner.replaceAll("<player>", p_).replaceAll("<arena>", a.getInternalName()).split(";");
						for (String playername : players) {
							if (Validator.isPlayerOnline(playername)) {
								Bukkit.getPlayer(playername).sendMessage(msgs);
							}
						}
					}
				} catch (Exception e) {
					System.out.println("Could not find arena for broadcast. " + e.getMessage());
				}

				Util.sendMessage(plugin, p, pli.getMessagesConfig().youWon);
				Util.sendMessage(plugin, p, received_rewards_msg);
				if (this.plugin.getConfig().getBoolean("config.effects.1_8_titles") && MinigamesAPI.getAPI().version.startsWith("v1_8")) {
					Effects.playTitle(p, pli.getMessagesConfig().youWon, 0);
				}

				// Participation Rewards
				if (this.participationEconomyRewards) {
					MinigamesAPI.econ.depositPlayer(p.getName(), this.participationEconomyReward);
					Util.sendMessage(plugin, p, pli.getMessagesConfig().youGotAParticipationReward.replaceAll("<economyreward>", Integer.toString(this.participationEconomyReward) + " " + MinigamesAPI.econ.currencyNamePlural()));
				}
				if (this.participationCommandRewards) {
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replaceAll("<player>", p_));
				}

				if (this.plugin.getConfig().getBoolean("config.spawn_fireworks_for_winners")) {
					Bukkit.getScheduler().runTaskLater(this.plugin, new Runnable() {
						public void run() {
							Util.spawnFirework(p);
						}
					}, 20L);
				}
			} else {
				Util.sendMessage(this.plugin, p, pli.getMessagesConfig().youLost);
				if (this.plugin.getConfig().getBoolean("config.effects.1_8_titles") && MinigamesAPI.getAPI().version.startsWith("v1_8")) {
					Effects.playTitle(p, pli.getMessagesConfig().youLost, 0);
				}
				MinigamesAPI.getAPI().getPluginInstance(this.plugin).getStatsInstance().lose(p_);
			}
		}
	}

}
