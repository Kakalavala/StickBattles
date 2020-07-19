package me.kakalavala.chris.core;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import me.kakalavala.chris.assets.PlayerDataYaml;
import me.kakalavala.chris.core.Core.Prefixes;
import me.kakalavala.chris.core.NameCore.Names;
import net.md_5.bungee.api.ChatColor;

public class ItemCore {
	
	public ItemStack basicStickItem;
	public ItemStack goldenStickItem;
	public ItemStack appleItem;
	public ItemStack refreshItem;
	public ItemStack enderPearlItem;
	public ItemStack pocketSandItem;
	public ItemStack spikeStripItem;
	
	public List<ItemStack> possibleItems = new ArrayList<ItemStack>();
	
	public boolean wasGoldenStickSpawned = false;
	
	public final Map<String, Location> currentlyUsed = new HashMap<String, Location>(); // id, [x,y,z]
	
	private final Core core;
	
	public ItemCore(final Core core) {
		this.core = core;
		
		this.goldenStickItem = new ItemStack(Material.BLAZE_ROD, 1); {
			final ItemMeta m = this.goldenStickItem.getItemMeta();
			
			m.setDisplayName("§6Golden Stick§8 - §7Right-Click to use §eRecovery");
			
			this.goldenStickItem.setItemMeta(m);
		};
		
		this.appleItem = new ItemStack(Material.APPLE, 1); {
			final ItemMeta m = this.appleItem.getItemMeta();
			
			m.setDisplayName("§cApple");
			
			this.appleItem.setItemMeta(m);
		};
		
		this.refreshItem = new ItemStack(Material.SUGAR, 1); {
			final ItemMeta m = this.refreshItem.getItemMeta();
			
			m.setDisplayName("§eInstant Recovery Refresh §8- §7Right-Click to refresh §eRecovery");
			
			this.refreshItem.setItemMeta(m);
		};
		
		this.enderPearlItem = new ItemStack(Material.ENDER_PEARL, 3); {
			final ItemMeta m = this.enderPearlItem.getItemMeta();
			
			m.setDisplayName("§5Ender Pearl §8- §7Right-Click to throw");
			
			this.enderPearlItem.setItemMeta(m);
		};
		
		this.pocketSandItem = new ItemStack(Material.SPLASH_POTION, 1); {
			final PotionMeta m = (PotionMeta) this.pocketSandItem.getItemMeta();
			
			m.setDisplayName("§6Pocket Sand §8- §7Right-Click to throw");
			m.setColor(Color.ORANGE);
			
			m.addCustomEffect(new PotionEffect(PotionEffectType.SLOW, 100, 2, true), true);
			m.addCustomEffect(new PotionEffect(PotionEffectType.CONFUSION, 100, 0, true), true);
			m.addCustomEffect(new PotionEffect(PotionEffectType.BLINDNESS, 100, 0, true), true);
			
			this.pocketSandItem.setItemMeta(m);
		};
		
		this.spikeStripItem = new ItemStack(Material.LINGERING_POTION, 1); {
			final PotionMeta m = (PotionMeta) this.spikeStripItem.getItemMeta();
			
			m.setDisplayName("§7Spike Strip §8- §7Right-Click to throw");
			m.setColor(Color.GRAY);
			
			m.addCustomEffect(new PotionEffect(PotionEffectType.SLOW, 100, 1, true), true);
			m.addCustomEffect(new PotionEffect(PotionEffectType.HARM, 100, 1, true), true);
			
			this.spikeStripItem.setItemMeta(m);
		};
		
		this.possibleItems.add(this.appleItem);
		this.possibleItems.add(this.refreshItem);
		this.possibleItems.add(this.enderPearlItem);
		this.possibleItems.add(this.pocketSandItem);
		this.possibleItems.add(this.spikeStripItem);
	}
	
	public ItemStack getStick(Player ply) {
		final PlayerDataYaml pYml = new PlayerDataYaml(core, ply);
		String sN = "§bBasic Stick";
		
		if (pYml.getActiveName(ply) != -1) {
			for (final Names n : Names.values()) {
				if (n.getId() == pYml.getActiveName(ply)) {
					sN = n.getName();
					break;
				}
			}
		}
		
		return this.getRenamedStick(sN);
	}
	
	public void spawnRandomItem() {
		final String id = this.getId();
		
		final ItemStack it = this.possibleItems.get(core.rn.nextInt(this.possibleItems.size())); {
			final ItemMeta m = it.getItemMeta();
			
			m.setDisplayName(m.getDisplayName());
			m.setLore(Arrays.asList("§7" + id));
			
			it.setItemMeta(m);
		};
		
		final int locId = core.getRandomItemLocation();
		Location loc = null;
		
		if (!this.currentlyUsed.containsValue(core.getItemSpawnPoints().get(locId)))
			loc = core.getItemSpawnPoints().get(locId);
		
		if (loc != null && it != null) {
			loc.getWorld().dropItem(loc.getBlock().getLocation().add(+0.5, +1, +0.5), it).setVelocity(new Vector(0, 0, 0));
			
			this.currentlyUsed.put(id, loc);
			
			for (final UUID uuid : core.currentlyPlaying)
				Bukkit.getPlayer(uuid).playSound(loc, Sound.ENTITY_ITEMFRAME_BREAK, 10F, 1F);
			
			for (final UUID uuid : core.queue) {
				if (!core.currentlyPlaying.contains(uuid)) {
					Bukkit.getPlayer(uuid).playSound(loc, Sound.ENTITY_ITEMFRAME_BREAK, 10F, 1F);
				}
			}
		}
	}
	
	public ItemStack getRenamedStick(String name) {
		this.basicStickItem = new ItemStack(Material.STICK, 1); {
			final ItemMeta m = this.basicStickItem.getItemMeta();
			
			m.setDisplayName(ChatColor.translateAlternateColorCodes('&', name) + "§8 - §7Right-Click to use §eRecovery");
			
			this.basicStickItem.setItemMeta(m);
		};
		
		return this.basicStickItem;
	}
	
	public void spawnSpecialStick() {
		if (!this.wasGoldenStickSpawned) {
			final Location loc = core.getGoldenStickSpawn();
			
			loc.getWorld().dropItem(loc.getBlock().getLocation().add(+0.5, +1, +0.5), this.goldenStickItem).setVelocity(new Vector(0, 0, 0));
			
			this.wasGoldenStickSpawned = true;
			
			for (final UUID uuid : core.currentlyPlaying) {
				Bukkit.getPlayer(uuid).sendMessage(String.format("%s §b§lThe §6§lGolden Stick§b§l has spawned in the center!", Prefixes.NOTICE.getPrefix()));
				Bukkit.getPlayer(uuid).playSound(Bukkit.getPlayer(uuid).getLocation(), Sound.BLOCK_END_PORTAL_SPAWN, 4F, 1F);
			}
			
			for (final UUID uuid : core.queue) {
				if (!core.currentlyPlaying.contains(uuid))
					Bukkit.getPlayer(uuid).sendMessage(String.format("%s §b§lThe §6§lGolden Stick§b§l has spawned in the center!", Prefixes.NOTICE.getPrefix()));
			}
		}
	}
	
	private String getId() {
		final DateFormat day = new SimpleDateFormat("dd");
		final DateFormat month = new SimpleDateFormat("MM");
		final DateFormat year = new SimpleDateFormat("yy");
		
		final DateFormat hr = new SimpleDateFormat("HH");
		final DateFormat min = new SimpleDateFormat("mm");
		final DateFormat sec = new SimpleDateFormat("ss");
		
		final Date date = new Date();
		final String[] d = {
				day.format(date),
				month.format(date),
				year.format(date),
				hr.format(date),
				min.format(date),
				sec.format(date)
		};
		
		String id = "";
		
		for (String _d : d) id += _d;
		
		return id;
	}
}
