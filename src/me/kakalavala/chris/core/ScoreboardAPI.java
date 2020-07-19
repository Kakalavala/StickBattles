package me.kakalavala.chris.core;

import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

@SuppressWarnings("deprecation")
public class ScoreboardAPI {
	
	public Scoreboard bd;
	public boolean wasLoaded = false;
	
	public final String[] teamNames = {
			"sb-red", "sb-gold", "sb-yellow", "sb-green"
	};
	
	public void registerScoreboard() {
		this.bd = Bukkit.getScoreboardManager().getMainScoreboard();
	}
	
	public void addTeams() {
		try {
			for (final String tn : this.teamNames) {
				if (!this.bd.getTeams().contains(tn)) {
					if (tn.equalsIgnoreCase("sb-red")) {
						final Team t = this.bd.registerNewTeam(tn);
						t.setColor((tn.equalsIgnoreCase("sb-red") ? ChatColor.RED : (tn.equalsIgnoreCase("sb-gold") ? ChatColor.GOLD : (tn.equalsIgnoreCase("sb-yellow") ? ChatColor.YELLOW : ChatColor.GREEN))));
					}
				}
			}
		} catch (Exception exc) {}
	}
	
	public void addPlayer(final UUID uuid, final String teamName) {
		this.bd.getTeam(teamName).addPlayer(Bukkit.getPlayer(uuid));
		Bukkit.getPlayer(uuid).setScoreboard(this.bd);
	}
	
	public void addPlayers(final List<UUID> plys) {
		for (final UUID uuid : plys) {
			this.bd.getTeam(this.teamNames[3]).addPlayer(Bukkit.getPlayer(uuid));
			Bukkit.getPlayer(uuid).setScoreboard(this.bd);
		}
	}
	
	public void removePlayer(final UUID uuid) {
		try {
			for (final String tn : this.teamNames) {
				if (this.bd.getTeam(tn).getPlayers().contains(Bukkit.getPlayer(uuid))) {
					this.bd.getTeam(tn).removePlayer(Bukkit.getPlayer(uuid));
				}
			}
			
			Bukkit.getPlayer(uuid).setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
		} catch (Exception exc) {}
	}
	
	public void removePlayers(final List<UUID> plys) {
		try {
			for (final UUID uuid : plys) {
				for (final String tn : this.teamNames) {
					if (this.bd.getTeam(tn).getPlayers().contains(Bukkit.getPlayer(uuid))) {
						this.bd.getTeam(tn).removePlayer(Bukkit.getPlayer(uuid));
					}
				}
				
				Bukkit.getPlayer(uuid).setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
			}
		} catch (Exception exc) {}
	}
	
	public void removeTeams() {
		for (final String tn : this.teamNames) {
			if (this.bd.getTeams().contains(tn))
				this.bd.getTeam(tn).unregister();
		}
	}

}
