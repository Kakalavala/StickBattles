package me.kakalavala.chris.assets;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import me.kakalavala.chris.core.Core;

public class PlayerDataYaml {
	
	public File plyYml;
	public FileConfiguration plyCfg;
	
	private Core core;
	
	public PlayerDataYaml(final Core core, final Player ply) {
		this.core = core;
		
		this.plyYml = new File(core.getDataFolder() + "/playerdata/" + ply.getUniqueId().toString() + ".yml");
		this.plyCfg = YamlConfiguration.loadConfiguration(this.plyYml);
	}
	
	public PlayerDataYaml(final Core core, final OfflinePlayer ply) {
		this.core = core;
		
		this.plyYml = new File(core.getDataFolder() + "/playerdata/" + ply.getUniqueId().toString() + ".yml");
		this.plyCfg = YamlConfiguration.loadConfiguration(this.plyYml);
	}
	
	public void save() {
		try {
			this.plyCfg.save(this.plyYml);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public double getWinPercentage(Player ply) {
		final double w = this.getWins(ply);
		final double l = this.getLosses(ply);
		
		try {
			return Math.round(100 * (w / (w + l)));
		} catch (Exception e) {
			return 0;
		}
	}
	
	public double getKD(Player ply) {
		final double k = this.getKills(ply);
		final double d = this.getDeaths(ply);
		
		try {
			return Math.round(k / d);
		} catch (Exception e) {
			return 0;
		}
	}
	
	public double getWinPercentage(OfflinePlayer ply) {
		final double w = this.getWins(ply);
		final double l = this.getLosses(ply);
		
		try {
			return 100 * (w / (w + l));
		} catch (Exception e) {
			return 0;
		}
	}
	
	public double getKD(OfflinePlayer ply) {
		final double k = this.getKills(ply);
		final double d = this.getDeaths(ply);
		
		try {
			if (d != 0)
				return k / d;
			else return k;
		} catch (Exception e) {
			return 0;
		}
	}
	
	public void setKills(double kills) {
		this.plyCfg.set("kills", kills);
		this.save();
	}
	
	public void setWins(double wins) {
		this.plyCfg.set("wins", wins);
		this.save();
	}
	
	public void setDeaths(double deaths) {
		this.plyCfg.set("deaths", deaths);
		this.save();
	}
	
	public void setLosses(double losses) {
		this.plyCfg.set("losses", losses);
		this.save();
	}
	
	public void setActiveName(int id) {
		this.plyCfg.set("active-name", id);
		this.save();
	}
	
	public void addWonName(Player ply, int nameId) {
		final List<Integer> nameIds = this.getWonNames(ply);
		
		if (!nameIds.contains(nameId))
			nameIds.add(nameId);
		
		this.plyCfg.set("names-won", nameIds);
		this.save();
	}
	
	public double getDeaths(Player ply) {
		this.plyYml = new File(core.getDataFolder() + "/playerdata/" + ply.getUniqueId().toString() + ".yml");
		this.plyCfg = YamlConfiguration.loadConfiguration(this.plyYml);
		
		if (this.plyCfg.get("deaths") == null)
			return 0;
		else return this.plyCfg.getDouble("deaths");
	}
	
	public double getKills(Player ply) {
		this.plyYml = new File(core.getDataFolder() + "/playerdata/" + ply.getUniqueId().toString() + ".yml");
		this.plyCfg = YamlConfiguration.loadConfiguration(this.plyYml);
		
		if (this.plyCfg.get("kills") == null)
			return 0;
		else return this.plyCfg.getDouble("kills");
	}
	
	public double getWins(Player ply) {
		this.plyYml = new File(core.getDataFolder() + "/playerdata/" + ply.getUniqueId().toString() + ".yml");
		this.plyCfg = YamlConfiguration.loadConfiguration(this.plyYml);
		
		if (this.plyCfg.get("wins") == null)
			return 0;
		else return this.plyCfg.getDouble("wins");
	}
	
	public double getLosses(Player ply) {
		this.plyYml = new File(core.getDataFolder() + "/playerdata/" + ply.getUniqueId().toString() + ".yml");
		this.plyCfg = YamlConfiguration.loadConfiguration(this.plyYml);
		
		if (this.plyCfg.get("losses") == null)
			return 0;
		else return this.plyCfg.getDouble("losses");
	}
	
	public double getDeaths(OfflinePlayer ply) {
		this.plyYml = new File(core.getDataFolder() + "/playerdata/" + ply.getUniqueId().toString() + ".yml");
		this.plyCfg = YamlConfiguration.loadConfiguration(this.plyYml);
		
		if (this.plyCfg.get("deaths") == null)
			return 0;
		else return this.plyCfg.getDouble("deaths");
	}
	
	public double getKills(OfflinePlayer ply) {
		this.plyYml = new File(core.getDataFolder() + "/playerdata/" + ply.getUniqueId().toString() + ".yml");
		this.plyCfg = YamlConfiguration.loadConfiguration(this.plyYml);
		
		if (this.plyCfg.get("kills") == null)
			return 0;
		else return this.plyCfg.getDouble("kills");
	}
	
	public double getWins(OfflinePlayer ply) {
		this.plyYml = new File(core.getDataFolder() + "/playerdata/" + ply.getUniqueId().toString() + ".yml");
		this.plyCfg = YamlConfiguration.loadConfiguration(this.plyYml);
		
		if (this.plyCfg.get("wins") == null)
			return 0;
		else return this.plyCfg.getDouble("wins");
	}
	
	public double getLosses(OfflinePlayer ply) {
		this.plyYml = new File(core.getDataFolder() + "/playerdata/" + ply.getUniqueId().toString() + ".yml");
		this.plyCfg = YamlConfiguration.loadConfiguration(this.plyYml);
		
		if (this.plyCfg.get("losses") == null)
			return 0;
		else return this.plyCfg.getDouble("losses");
	}
	
	public int getActiveName(Player ply) {
		this.plyYml = new File(core.getDataFolder() + "/playerdata/" + ply.getUniqueId().toString() + ".yml");
		this.plyCfg = YamlConfiguration.loadConfiguration(this.plyYml);
		
		if (this.plyCfg.get("active-name") == null)
			return -1;
		else return this.plyCfg.getInt("active-name");
	}
	
	public int getActiveName(OfflinePlayer ply) {
		this.plyYml = new File(core.getDataFolder() + "/playerdata/" + ply.getUniqueId().toString() + ".yml");
		this.plyCfg = YamlConfiguration.loadConfiguration(this.plyYml);
		
		if (this.plyCfg.get("active-name") == null)
			return -1;
		else return this.plyCfg.getInt("active-name");
	}
	
	public List<Integer> getWonNames(Player ply) {
		this.plyYml = new File(core.getDataFolder() + "/playerdata/" + ply.getUniqueId().toString() + ".yml");
		this.plyCfg = YamlConfiguration.loadConfiguration(this.plyYml);
		
		if (this.plyCfg.getIntegerList("names-won") == null) {
			return Arrays.asList();
		} else return this.plyCfg.getIntegerList("names-won");
	}
	
	public List<Integer> getWonNames(OfflinePlayer ply) {
		this.plyYml = new File(core.getDataFolder() + "/playerdata/" + ply.getUniqueId().toString() + ".yml");
		this.plyCfg = YamlConfiguration.loadConfiguration(this.plyYml);
		
		if (this.plyCfg.getIntegerList("names-won") == null)
			return Arrays.asList();
		else return this.plyCfg.getIntegerList("names-won");
	}

}
