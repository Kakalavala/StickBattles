package me.kakalavala.chris.assets;

import java.io.File;
import java.util.List;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.kakalavala.chris.core.Core;

public class PlayerInventoryYaml {
	
	public File plyInvYml;
	public FileConfiguration plyInvCfg;
	
	private Core core;
	
	public PlayerInventoryYaml(Core core, Player ply) {
		this.core = core;
		this.plyInvYml = new File(core.getDataFolder() + "/playerdata/inventories/" + ply.getUniqueId().toString() + ".yml");
		this.plyInvCfg = YamlConfiguration.loadConfiguration(this.plyInvYml);
	}
	
	public void saveInv() {
		try {
			this.plyInvCfg.save(this.plyInvYml);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void deleteInventory(Player ply) {
		this.plyInvYml.delete();
	}
	
	public void storeInventory(Player ply) {
		if (this.plyInvYml.exists()) {
			core.log.warning(String.format("Inventory for [%s] already exists!", ply.getUniqueId().toString()));
			return;
		} else {
			this.plyInvCfg.set("inventory.armor", ply.getInventory().getArmorContents());
			this.plyInvCfg.set("inventory.content", ply.getInventory().getContents());
			this.plyInvCfg.set("inventory.level", ply.getLevel());
			this.plyInvCfg.set("inventory.exp", ply.getExp());
			this.plyInvCfg.set("inventory.health", ply.getHealth());
			this.plyInvCfg.set("inventory.hunger", ply.getFoodLevel());
			this.saveInv();
		}
	}
	
	@SuppressWarnings("unchecked")
	public void restoreInventory(Player ply) {
		if (!this.plyInvYml.exists()) return;
		else {
			ItemStack[] content = ((List<ItemStack>) this.plyInvCfg.get("inventory.armor")).toArray(new ItemStack[0]);
	        
	        ply.getInventory().setArmorContents(content);
	        
	        content = ((List<ItemStack>) this.plyInvCfg.get("inventory.content")).toArray(new ItemStack[0]);
	        
	        ply.getInventory().setContents(content);
	        
	        ply.setHealth(this.plyInvCfg.getDouble("inventory.health"));
	        ply.setLevel(this.plyInvCfg.getInt("inventory.level"));
	        ply.setExp(Float.parseFloat(this.plyInvCfg.get("inventory.exp").toString()));
	        ply.setFoodLevel(this.plyInvCfg.getInt("inventory.hunger"));
		}
	}

}
