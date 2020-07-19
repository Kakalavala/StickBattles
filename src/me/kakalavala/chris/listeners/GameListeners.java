package me.kakalavala.chris.listeners;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import me.kakalavala.chris.assets.PlayerDataYaml;
import me.kakalavala.chris.core.Core;
import me.kakalavala.chris.core.Core.Prefixes;
import me.kakalavala.chris.core.NameCore.Names;
import me.kakalavala.chris.core.PlayerAutoRespawnEvent;
import me.kakalavala.chris.core.PlayerPreAutoRespawnEvent;

public class GameListeners implements Listener {
	
	private Core core;
	
	private UUID justDied = null;
	
	private final List<UUID> justGroundPounded = new ArrayList<UUID>();
	
	/**
	 * <VICTUM, LAST_DAMAGER>
	 */
	private final Map<UUID, UUID> damageHolder = new HashMap<UUID, UUID>(); // VICTUM, LAST_DAMAGER
	
	/**
	 * <DAMAGER, STICK USED NAME>
	 */
	private final Map<UUID, String> stickHolder = new HashMap<UUID, String>(); // DAMAGER, STICK USED NAME
	
	private final Map<UUID, Integer> groundpounding = new HashMap<UUID, Integer>();
	
	public GameListeners(final Core core) {
		this.core = core;
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerDisconnect(final PlayerQuitEvent e) {
		final Player ply = e.getPlayer();
		
		ply.removePotionEffect(PotionEffectType.SPEED);
		ply.removePotionEffect(PotionEffectType.JUMP);
		ply.removePotionEffect(PotionEffectType.NIGHT_VISION);
		ply.removePotionEffect(PotionEffectType.GLOWING);
		
		if (core.currentlyPlaying.contains(ply.getUniqueId())) {
			final PlayerDataYaml pYml = new PlayerDataYaml(core, ply);
			
			pYml.setLosses(pYml.getLosses(ply) + 1);
			pYml.setDeaths(pYml.getDeaths(ply) + 1);
		}
		
		core.removePlayerFromGameOrQueue(ply);
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void stopWorldChange(final PlayerChangedWorldEvent e) {
		final Player ply = e.getPlayer();
		final World from = e.getFrom();
		
		if (ply.getWorld().equals(core.getGameWorldName()) && !from.getName().equals(core.getGameWorldName())) {
			ply.teleport(from.getSpawnLocation());
			ply.sendMessage("§cYou cannot /back into this world.");
			return;
		}
		
		if (from.getName().equals(core.getGameWorldName())) {
			if (core.currentlyPlaying.contains(ply.getUniqueId())) {
				
				// was that the player who just died?
				if (this.justDied == ply.getUniqueId())
					return;
				
				final PlayerDataYaml pYml = new PlayerDataYaml(core, ply);
				
				// were they in a game?
				// if no -> set them as if they were (they'll be moved back to lobby after game is over anyways && re-added to queue)
				// if yes -> do nothing
				if (!core.currentlyPlaying.contains(ply.getUniqueId()))
					core.currentlyPlaying.add(ply.getUniqueId());
				
				// were they alive in the game?
				// if yes -> set them as dead, they forfitted the game.
				// if no -> do nothing
				if (core.alive.contains(ply.getUniqueId()))
					core.alive.remove(ply.getUniqueId());
				
				// did they have stocks?
				// if yes -> make it 0
				// if no -> also make it 0
				if (core.stock.containsKey(ply.getUniqueId()))
					core.stock.remove(ply.getUniqueId());
				
				pYml.setLosses(pYml.getLosses(ply) + 1);
				pYml.setDeaths(pYml.getDeaths(ply) + 1);
				
				core.sendPluginMessage(ply, "§cBy leaving the world, you forfitted the game.");
				
				core.removePlayerFromGameOrQueue(ply);
			} else {
				if (core.queue.contains(ply.getUniqueId())) {
					core.removePlayerQueueDontTeleport(ply);
				} else return;
			}
		} else return;
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onDeathEvent(final PlayerDeathEvent e) {
		final Player ply = (Player) e.getEntity();

		if (core.alive.contains(ply.getUniqueId())) {
			final int stocksLeft = core.stock.get(ply.getUniqueId());
			
			if (stocksLeft == 0) {
				justDied = ply.getUniqueId();
				e.setKeepInventory(true);
				
				Entity dmgr = ply;
				
				if (this.damageHolder.containsKey(ply.getUniqueId()))
					dmgr = Bukkit.getPlayer(this.damageHolder.get(ply.getUniqueId()));
				
				String nm = this.stickHolder.get(dmgr.getUniqueId());
				
				for (final UUID uuid : this.justGroundPounded) {
					Bukkit.broadcastMessage("fuck me sideways: " + Bukkit.getPlayer(uuid).getName());
				}
				
				if (this.justGroundPounded.contains(ply.getUniqueId())) {
					nm = "§7Ground Pound";
					this.justGroundPounded.remove(ply.getUniqueId());
				}
				
				if (dmgr != ply) {
					final PlayerDataYaml dYml = new PlayerDataYaml(core, Bukkit.getPlayer(this.damageHolder.get(ply.getUniqueId())));
					final PlayerDataYaml pYml = new PlayerDataYaml(core, ply);
					
					dYml.setKills(dYml.getKills((Player) dmgr) + 1);
					pYml.setDeaths(pYml.getDeaths(ply) + 1);
					core.kills.put(dmgr.getUniqueId(), core.kills.get(dmgr.getUniqueId()) + 1);
				}
				
				for (final UUID uuid : core.currentlyPlaying) {
					if (dmgr != ply)
						Bukkit.getPlayer(uuid).sendMessage(String.format("%s §c%s §7got §6%s §7using §8[%s§8]", Prefixes.KO.getPrefix(), dmgr.getName(), ply.getName(), nm));
					else Bukkit.getPlayer(uuid).sendMessage(String.format("%s §6%s §7knocked themself out§7!", Prefixes.KO.getPrefix(), ply.getName()));
					
					Bukkit.getPlayer(uuid).sendMessage(String.format("%s §6%s§8 is out!", Prefixes.OUT.getPrefix(), ply.getName()));
				}
					
				for (final UUID uuid : core.queue) {
					if (!core.currentlyPlaying.contains(uuid)) {
						if (dmgr != ply)
							Bukkit.getPlayer(uuid).sendMessage(String.format("%s §c%s §7got §6%s §7using §8[%s§8]", Prefixes.KO.getPrefix(), dmgr.getName(), ply.getName(), nm));
						else Bukkit.getPlayer(uuid).sendMessage(String.format("%s §6%s §7knocked themself out§7!", Prefixes.KO.getPrefix(), ply.getName()));
						
						Bukkit.getPlayer(uuid).sendMessage(String.format("%s §6%s§8 is out!", Prefixes.OUT.getPrefix(), ply.getName()));
					}
				}
				
				this.damageHolder.remove(ply.getUniqueId());
				return;
			}
			
			if (stocksLeft > 0) {
				core.stock.remove(ply.getUniqueId());
				core.stock.put(ply.getUniqueId(), stocksLeft - 1);
				justDied = ply.getUniqueId();
				e.setKeepInventory(true);
				
				Entity dmgr = ply;
				
				if (this.damageHolder.containsKey(ply.getUniqueId()))
					dmgr = Bukkit.getPlayer(this.damageHolder.get(ply.getUniqueId()));
				
				String nm = this.stickHolder.get(dmgr.getUniqueId());
				
				if (dmgr != ply) {
					final PlayerDataYaml dYml = new PlayerDataYaml(core, Bukkit.getPlayer(this.damageHolder.get(ply.getUniqueId())));
					final PlayerDataYaml pYml = new PlayerDataYaml(core, ply);
					
					dYml.setKills(dYml.getKills((Player) dmgr) + 1);
					pYml.setDeaths(pYml.getDeaths(ply) + 1);
					core.kills.put(dmgr.getUniqueId(), core.kills.get(dmgr.getUniqueId()) + 1);
				}
				
				for (final UUID uuid : core.currentlyPlaying) {
					if (dmgr != ply)
						Bukkit.getPlayer(uuid).sendMessage(String.format("%s §c%s §7got §6%s §7using §8[%s§8]", Prefixes.KO.getPrefix(), dmgr.getName(), ply.getName(), nm));
					else Bukkit.getPlayer(uuid).sendMessage(String.format("%s §6%s §7knocked themself out!", Prefixes.KO.getPrefix(), ply.getName()));
				}
					
				for (final UUID uuid : core.queue) {
					if (!core.currentlyPlaying.contains(uuid)) {
						if (dmgr != ply)
							Bukkit.getPlayer(uuid).sendMessage(String.format("%s §c%s §7got §6%s §7using §8[%s§8]", Prefixes.KO.getPrefix(), dmgr.getName(), ply.getName(), nm));
						else Bukkit.getPlayer(uuid).sendMessage(String.format("%s §6%s §7knocked themself out§7!", Prefixes.KO.getPrefix(), ply.getName()));
					}
				}
				
				this.damageHolder.remove(ply.getUniqueId());
			}
			
			e.setDeathMessage("");
			
			// AUTO-RESPAWN
			// WRITTEN BY Shadey-GV
			final PlayerPreAutoRespawnEvent ppare = new PlayerPreAutoRespawnEvent(ply, ply.getLocation());
			
			Bukkit.getPluginManager().callEvent(ppare);
			if (ppare.isCancelled())
				return;
			Bukkit.getScheduler().scheduleSyncDelayedTask(core, new Runnable() {
				@Override
				public void run() {
					ply.spigot().respawn();
					
					final Location respawnLoc = ply.getLocation();
					Bukkit.getPluginManager().callEvent(new PlayerAutoRespawnEvent(ply, ply.getLocation(), respawnLoc));
				}
			}, 1L);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onMoveBoundsDetection(final PlayerMoveEvent e) {
		try {
			final Player ply = e.getPlayer();
			
			if (core.frozen.contains(ply.getUniqueId()))
				e.setCancelled(true);
			
			final int x = ply.getLocation().getBlockX();
			final int y = ply.getLocation().getBlockY();
			final int z = ply.getLocation().getBlockZ();
			
			if (core.alive.contains(ply.getUniqueId())) {
				if (ply.getWorld().getBlockAt(ply.getLocation()).getType().equals(Material.STATIONARY_WATER)) {
					ply.setHealth(0D);
				}
				
				// is the player at the minimum distance to ground pound?
				if (y >= 168 && !ply.isOnGround()) {
					if (!this.groundpounding.containsKey(ply.getUniqueId()))
						this.groundpounding.put(ply.getUniqueId(), 1);
					
					if (y >= 164) {
						if (this.groundpounding.get(ply.getUniqueId()) < 2)
							this.groundpounding.put(ply.getUniqueId(), 2);
					}
					
					if (y >= 180) {
						if (this.groundpounding.get(ply.getUniqueId()) < 3)
							this.groundpounding.put(ply.getUniqueId(), 3);
					}
					
					// this is the max
					if (y >= 186) {
						this.groundpounding.put(ply.getUniqueId(), 4);
					}
				}
				
				if (this.groundpounding.containsKey(ply.getUniqueId()) && ply.isOnGround()) {
					if (ply.getNearbyEntities(6, 3, 6).size() == 0) {
						ply.playSound(ply.getLocation(), Sound.ENTITY_ARMORSTAND_BREAK, 6F, 0F);
					}
					
					for (final Entity ent : ply.getNearbyEntities(6, 3, 6)) {
						if (ent instanceof Player) {
							final Player vic = (Player) ent;
						       
					        Location playerCenterLocation = ply.getEyeLocation();
					        Location playerToThrowLocation = vic.getEyeLocation();
					       
					        double _x = playerToThrowLocation.getX() - playerCenterLocation.getX();
					        double _y = playerToThrowLocation.getY() - playerCenterLocation.getY();
					        double _z = playerToThrowLocation.getZ() - playerCenterLocation.getZ();
					       
					        Vector throwVector = new Vector(_x, _y, _z);
					       
					        throwVector.normalize();
					        throwVector.multiply(1.5D);
					        throwVector.setY(1.0D);
					       
					        vic.setVelocity(throwVector);
					        
					        this.damageHolder.put(vic.getUniqueId(), ply.getUniqueId());
					        this.stickHolder.put(ply.getUniqueId(), "§7Ground Pound");
					        this.justGroundPounded.add(vic.getUniqueId());
					        
					        try {
					        	vic.setHealth(vic.getHealth() - this.groundpounding.get(ply.getUniqueId()));
					        } catch (Exception exc) {
					        	vic.setHealth(0);
					        }
					        
					        ply.playSound(ply.getLocation(), Sound.ENTITY_ZOMBIE_BREAK_DOOR_WOOD, 1F, 0F);
							vic.playSound(ply.getLocation(), Sound.ENTITY_ZOMBIE_BREAK_DOOR_WOOD, 1F, 0F);
						}
					}
					
					this.groundpounding.remove(ply.getUniqueId());
				}
				
				if (x >= core.getArenaBounds()[0].getBlockX() || x <= core.getArenaBounds()[1].getBlockX()
				|| y <= core.getArenaBounds()[1].getBlockY() || y == 256
				|| z >= core.getArenaBounds()[0].getBlockZ() || z <= core.getArenaBounds()[1].getBlockZ()) {
					final int stocksLeft = core.stock.get(ply.getUniqueId());
					
					if (stocksLeft > 0) {
						core.stock.remove(ply.getUniqueId());
						core.stock.put(ply.getUniqueId(), stocksLeft - 1);
					}
					
					core.safeCooldown.put(ply.getUniqueId(), 3000L);
					core.abiCore.cooldown.put(ply.getUniqueId(), 0L);
					
					final Location spawnLoc = core.getSpawnPoints().get(core.usedSpawnPoints.get(ply.getUniqueId()));
					
					ply.teleport(spawnLoc);
					
					final Location loc = ply.getLocation();
					
					loc.setYaw(core.getCenteredYaw(ply));
					
					ply.teleport(loc);
					
					ply.getInventory().setItem(0, core.itemCore.getStick(ply));
					ply.setHealth(20);
					ply.setExp(1F);
					ply.setLevel(0);
					
					Entity dmgr = ply;
					
					if (this.damageHolder.containsKey(ply.getUniqueId()))
						dmgr = Bukkit.getPlayer(this.damageHolder.get(ply.getUniqueId()));
					
					String nm = this.stickHolder.get(dmgr.getUniqueId());
					
					if (dmgr != ply) {
						final PlayerDataYaml dYml = new PlayerDataYaml(core, Bukkit.getPlayer(this.damageHolder.get(ply.getUniqueId())));
						final PlayerDataYaml pYml = new PlayerDataYaml(core, ply);
						
						
						dYml.setKills(dYml.getKills((Player) dmgr) + 1);
						pYml.setDeaths(pYml.getDeaths(ply) + 1);
						core.kills.put(dmgr.getUniqueId(), core.kills.get(dmgr.getUniqueId()) + 1);
					}
					
					for (final UUID uuid : core.currentlyPlaying) {
						if (dmgr != ply)
							Bukkit.getPlayer(uuid).sendMessage(String.format("%s §c%s §7got §6%s §7using §8[%s§8]", Prefixes.KO.getPrefix(), dmgr.getName(), ply.getName(), nm));
						else Bukkit.getPlayer(uuid).sendMessage(String.format("%s §6%s §7knocked themself out!", Prefixes.KO.getPrefix(), ply.getName()));
					}
						
					for (final UUID uuid : core.queue) {
						if (!core.currentlyPlaying.contains(uuid)) {
							if (dmgr != ply)
								Bukkit.getPlayer(uuid).sendMessage(String.format("%s §c%s §7got §6%s §7using §8[%s§8]", Prefixes.KO.getPrefix(), dmgr.getName(), ply.getName(), nm));
							else Bukkit.getPlayer(uuid).sendMessage(String.format("%s §6%s §7knocked themself out§7!", Prefixes.KO.getPrefix(), ply.getName()));
						}
					}
					
					this.damageHolder.remove(ply.getUniqueId());
				}
			}
		} catch (Exception exc) {}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onRespawn(final PlayerRespawnEvent e) {
		final Player ply = e.getPlayer();
		final PlayerDataYaml pYml = new PlayerDataYaml(core, ply);
		
		if (ply.getUniqueId() == this.justDied) {
			if (core.stock.get(ply.getUniqueId()) == 0) {
				for (final UUID uuid : core.currentlyPlaying) {
					Bukkit.getPlayer(uuid).sendMessage(String.format("%s §6%s§8 is out!", Prefixes.OUT.getPrefix(), ply.getName()));
				}
					
				for (final UUID uuid : core.queue) {
					if (!core.currentlyPlaying.contains(uuid)) {
						Bukkit.getPlayer(uuid).sendMessage(String.format("%s §6%s§8 is out!", Prefixes.OUT.getPrefix(), ply.getName()));
					}
				}
			}
			
			if (core.stock.get(ply.getUniqueId()) > 0) {
				core.safeCooldown.put(ply.getUniqueId(), 3000L);
				core.abiCore.cooldown.put(ply.getUniqueId(), 0L);
				
				ply.getInventory().setItem(0, core.itemCore.getStick(ply));
				
				final Location loc = core.getSpawnPoints().get(core.usedSpawnPoints.get(ply.getUniqueId()));
				
				loc.setYaw(core.getCenteredYaw(ply));
				
				e.setRespawnLocation(loc);
			} else {
				// spectate
				if (core.alive.size() >= 1) {
					if (core.alive.contains(ply.getUniqueId()))
						core.alive.remove(ply.getUniqueId());
					
					if (!core.dead.contains(ply.getUniqueId()))
						core.dead.add(ply.getUniqueId());
					
					pYml.setLosses(pYml.getLosses(ply) + 1);
					
					final Entity spTar = core.getRandomSpectateTarget();
					
					ply.setGameMode(GameMode.SPECTATOR);
					e.setRespawnLocation(spTar.getLocation());
				}
			}
		}
	}
	
	/**
	 * When a player picks up an [game] item
	 */
	@EventHandler(priority = EventPriority.HIGH)
	public void onPickUp(final EntityPickupItemEvent e) {
		if (!(e.getEntity() instanceof Player)) return;
		else {
			final Player ply = (Player) e.getEntity();
			final ItemStack item = e.getItem().getItemStack();
			
			if (core.alive.contains(ply.getUniqueId())) {
				if (item.equals(core.itemCore.goldenStickItem)) {
					ply.getInventory().setItem(0, core.itemCore.goldenStickItem);
					
					e.getItem().remove();
					e.getItem().setItemStack(new ItemStack(Material.AIR, 0));
					e.setCancelled(true);
					
					for (final UUID uuid : core.currentlyPlaying) {
						Bukkit.getPlayer(uuid).playSound(Bukkit.getPlayer(uuid).getLocation(), Sound.BLOCK_NOTE_PLING, 6F, 0F);
						Bukkit.getPlayer(uuid).sendMessage(String.format("%s §9§lThe §6§lGolden Stick§9§l was picked up!", Prefixes.NOTICE.getPrefix()));
					}
					
					for (final UUID uuid : core.queue) {
						if (!core.currentlyPlaying.contains(uuid)) {
							Bukkit.getPlayer(uuid).playSound(Bukkit.getPlayer(uuid).getLocation(), Sound.BLOCK_NOTE_PLING, 6F, 0F);
							Bukkit.getPlayer(uuid).sendMessage(String.format("%s §9§lThe §6§lGolden Stick§b§l was picked up!", Prefixes.NOTICE.getPrefix()));
						}
					}
				} else {
					final Inventory inv = ply.getInventory();
					int takenSlots = 0;

					for (int i = 0; i != 9; i += 1) {
						if (inv.getItem(i) != null)
							takenSlots += 1;
					}
					
					if (item.getItemMeta().getDisplayName().equals(core.itemCore.appleItem.getItemMeta().getDisplayName())) {
						takenSlots -= 1;
						
						e.getItem().remove();
						e.getItem().setItemStack(new ItemStack(Material.AIR, 0));
						e.setCancelled(true);
						
						core.itemCore.currentlyUsed.remove(ChatColor.stripColor(item.getItemMeta().getLore().get(0)));
						
						try {
							ply.setHealth(ply.getHealth() + 4D);
						} catch (IllegalArgumentException exc) {
							ply.setHealth(20D);
						}
						
						ply.playSound(ply.getLocation(), Sound.ENTITY_PLAYER_BURP, 5F, 2F);
					} else {
						if (takenSlots >= 9) {
							e.setCancelled(true);
							return;
						}
						
						core.itemCore.currentlyUsed.remove(ChatColor.stripColor(item.getItemMeta().getLore().get(0)));
						return;
					}
				}
			} else return;
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onAttack(final EntityDamageByEntityEvent e) {
		if (e.getEntityType().equals(EntityType.PLAYER)) {
			if (e.getDamager().getType().equals(EntityType.PLAYER)) {
				if (e.getCause().equals(DamageCause.ENTITY_ATTACK)) {
					final Player ply = (Player) e.getEntity();
					
					if (core.safeCooldown.containsKey(ply.getUniqueId())) {
						if (core.safeCooldown.get(ply.getUniqueId()) > 0L) {
							e.setCancelled(true);
							return;
						}
					}
					
					if (core.alive.contains(ply.getUniqueId())) {
						if (e.getDamage() == 0D) return;
						
						final Player dmgr = (Player) e.getDamager();
						final PlayerDataYaml dYml = new PlayerDataYaml(core, dmgr);
						
						this.damageHolder.put(ply.getUniqueId(), dmgr.getUniqueId());
						
						if (dmgr.getInventory().getItemInMainHand().getType().equals(Material.STICK)) {
							this.stickHolder.put(dmgr.getUniqueId(), "§bBasic Stick");
							
							for (final Names n : Names.values()) {
								if (n.getId() == dYml.getActiveName(dmgr)) {
									this.stickHolder.put(dmgr.getUniqueId(), n.getName());
									break;
								}
							}
							
						} else if (dmgr.getInventory().getItemInMainHand().getType().equals(Material.BLAZE_ROD))
							this.stickHolder.put(dmgr.getUniqueId(), "§6Golden Stick");
						else this.stickHolder.put(dmgr.getUniqueId(), "§7Fists");
						
						final double d_xDir = Math.round(dmgr.getLocation().getDirection().normalize().getX());
						final double d_zDir = Math.round(dmgr.getLocation().getDirection().normalize().getZ());
						
						if (dmgr.getInventory().getItemInMainHand().getType().equals(Material.STICK)) {
							final double k = Math.floor(1 + (20 - ply.getHealth()) / 2);
							
							ply.setVelocity(new Vector((k * d_xDir), 0.2, (k * d_zDir)));
							
							return;
						} else if (dmgr.getInventory().getItemInMainHand().getType().equals(Material.BLAZE_ROD)) {
							core.itemCore.wasGoldenStickSpawned = false;
							dmgr.getInventory().setItem(0, core.itemCore.getStick(dmgr));
							e.setDamage(10D);
							ply.setVelocity(new Vector((100 * d_xDir), 60, (100 * d_zDir)));
							return;
						}
					} else return;
				} else return;
			} else return;
		} else return;
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onRightClick(final PlayerInteractEvent e) {
		final Player ply = e.getPlayer();
		
		try {
			if (core.alive.contains(ply.getUniqueId())) {
				if (e.getAction().equals(Action.RIGHT_CLICK_AIR) || e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
					if (ply.getInventory().getItemInMainHand().getType().equals(Material.STICK) || ply.getInventory().getItemInMainHand().getType().equals(Material.BLAZE_ROD))
						core.abiCore.newRecovery(ply);
					else {
						if (ply.getInventory().getItemInMainHand().getItemMeta().getDisplayName().equals(core.itemCore.refreshItem.getItemMeta().getDisplayName())) {
							if (core.abiCore.cooldown.get(ply.getUniqueId()) == 0 || core.abiCore.cooldown.get(ply.getUniqueId()) == null)
								return;
							
							core.abiCore.cooldown.put(ply.getUniqueId(), 0L);
							ply.setExp(1F);
							ply.getInventory().getItemInMainHand().setAmount(0);
							ply.playSound(ply.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 6F, 1F);
						} else return;
					}
				} else return;
			} else return;
		} catch (Exception exc) {}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onItemDespawn(final ItemDespawnEvent e) {
		final Location loc = e.getLocation();
		final int x = loc.getBlockX();
		final int y = loc.getBlockY();
		final int z = loc.getBlockZ();
		
		if (core.isGameStarted) {
			if (x >= core.getArenaBounds()[0].getBlockX() || x <= core.getArenaBounds()[1].getBlockX()
					|| y <= core.getArenaBounds()[1].getBlockY() || y == 256
					|| z >= core.getArenaBounds()[0].getBlockZ() || z <= core.getArenaBounds()[1].getBlockZ()) {
				e.setCancelled(true);
			} else return;
		} else return;
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void inventoryInteract(final InventoryInteractEvent e) {
		final Player ply = (Player) e.getWhoClicked();
		final Inventory inv = e.getInventory();
		
		if (core.currentlyPlaying.contains(ply.getUniqueId())) {
			if (inv.equals(core.nameCore.getNewNameInventory(ply))) {
				
				e.setResult(Result.DENY);
				e.setCancelled(true);
			} else return;
		} else return;
	}
}
