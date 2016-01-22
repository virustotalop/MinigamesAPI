package com.comze_instancelabs.minigamesapi.achievements;

public class AAchievement {

	private String name;
	private boolean done;
	private String playerName;

	public AAchievement(String name, String playerName, boolean done) {
		this.name = name;
		this.playerName = playerName;
		this.done = done;
	}

	public boolean isDone() {
		return done;
	}

	public void setDone(boolean t) {
		this.done = t;
	}

	public String getAchievementNameRaw() {
		return name;
	}
	
	public String getPlayerName() {
		return this.playerName;
	}
}
