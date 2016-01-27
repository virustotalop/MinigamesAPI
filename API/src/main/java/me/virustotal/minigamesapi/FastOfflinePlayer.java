package me.virustotal.minigamesapi;

import java.util.Map;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class FastOfflinePlayer implements OfflinePlayer {
	
	/* Fast offline player to be used in 1.7 scoreboards.
	 * OfflinePlayers query every user now due to the uuid update.
	 * Only the name is needed for the scoreboard.
	 */
	
	private String name;
	public FastOfflinePlayer(Player player)
	{
		this.name = player.getName();	
	}
	
	public FastOfflinePlayer(String name)
	{
		this.name = name;
	}
	
	@Override
	public boolean isOp() 
	{
		return false;
	}

	@Override
	public void setOp(boolean arg0) {}

	@Override
	public Map<String, Object> serialize() 
	{
		return null;
	}

	@Override
	public Location getBedSpawnLocation() 
	{
		return null;
	}

	@Override
	public long getFirstPlayed() 
	{
		return 0;
	}

	@Override
	public long getLastPlayed() 
	{
		return 0;
	}

	@Override
	public String getName() 
	{
		return this.name;
	}

	@Override
	public Player getPlayer() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public UUID getUniqueId() 
	{
		return UUID.randomUUID();
	}

	@Override
	public boolean hasPlayedBefore() 
	{
		return false;
	}

	@Override
	public boolean isBanned() 
	{
		return false;
	}

	@Override
	public boolean isOnline() 
	{
		return false;
	}

	@Override
	public boolean isWhitelisted() 
	{
		return false;
	}

	@Override
	public void setBanned(boolean arg0) {}

	@Override
	public void setWhitelisted(boolean arg0) {}

}
