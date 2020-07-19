package me.kakalavala.chris.core;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Difficulty;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;

import me.kakalavala.chris.assets.PlayerDataYaml;
import me.kakalavala.chris.assets.PlayerInventoryYaml;
import me.kakalavala.chris.commands.Command_StickBattles;
import me.kakalavala.chris.core.NameCore.Names;
import me.kakalavala.chris.core.api.menu.MenuListeners;
import me.kakalavala.chris.listeners.GameListeners;
import me.kakalavala.chris.listeners.PreventionListeners;
import net.ess3.api.MaxMoneyException;

public class Core extends JavaPlugin {
	
	public final PluginDescriptionFile pf = this.getDescription();
	public final PluginManager pm = Bukkit.getPluginManager();
	
	public final Logger log = Logger.getLogger(pf.getName());
	public final Random rn = new Random();
	public final ScoreboardAPI sb = new ScoreboardAPI();
	public final ItemCore itemCore = new ItemCore(this);
	public final AbilityCore abiCore = new AbilityCore(this);
	public final NameCore nameCore = new NameCore(this);
	
	public final List<UUID> currentlyPlaying = new ArrayList<UUID>();
	public final List<UUID> alive = new ArrayList<UUID>();
	public final List<UUID> dead = new ArrayList<UUID>();
	public final List<UUID> queue = new ArrayList<UUID>();
	public final Map<UUID, Integer> stock = new HashMap<UUID, Integer>();
	public final Map<UUID, Integer> usedSpawnPoints = new HashMap<UUID, Integer>();
	public final List<UUID> spectating = new ArrayList<UUID>();
	public final Map<UUID, Integer> kills = new HashMap<UUID, Integer>();
	public final List<UUID> frozen = new ArrayList<UUID>();
	public final Map<UUID, Long> safeCooldown = new HashMap<UUID, Long>(); 
	public final Map<UUID, Integer> currentPageHolder = new HashMap<UUID, Integer>(); // Player, Current Page
	public final Map<Integer, Inventory> registeredPages = new HashMap<Integer, Inventory>();
	
	public boolean gameJustStarted = false;
	public boolean isGameStarted = false;
	public boolean haveNotified = false;
	public boolean didGameJustEnd = false;
	
	public Essentials ess;
	
	public enum Prefixes {
		GAME("§8[§b§lStick§c§lBattles§8]"),
		KO("§8[§4§lK.O.§8]"),
		NOTICE("§8[§6§lNOTICE§8]"),
		ITEM("§8[§6§lITEM§8]"),
		WINNER("§8[§a§lWINNER§8]"),
		OUT("§8[§c§lOUT§8]")
		;
		
		String pfx;
		
		Prefixes(final String pfx) {
			this.pfx = pfx + "§r";
		}
		
		public String getPrefix() {
			return this.pfx;
		}
	}
	
	/*
	private void _DEBUG() {
		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
			@Override
			public void run() {
				final String[] debug = {
						"\n\n",
						" ----- [ CURRENTLYPLAYING ] -----",
						String.format("currentlyPlaying - size: %s", currentlyPlaying.size()),
						String.format("currentlyPlaying: %s", getArrayContents(currentlyPlaying)),
						" ----- [ QUEUE ] -----",
						String.format("queue - size: %s", queue.size()),
						String.format("queue: %s", getArrayContents(queue)),
						" ----- [ ALIVE ] ------",
						String.format("alive - size: %s", alive.size()),
						String.format("alive: %s", getArrayContents(alive)),
						" ----- [ DEAD ] ------",
						String.format("dead - size: %s", dead.size()),
						String.format("dead: %s", getArrayContents(dead)),
						" ----- [ GAME INFO ] ------",
						String.format("isGameStarted = %s", isGameStarted),
						String.format("MINIMUM_REQUIREMENT = %s", getMinimumRequirement()),
						String.format("wasGoldenStickSpawned = %s", itemCore.wasGoldenStickSpawned),
						" ----- [ SPECTATING ] -----",
						String.format("spectating - size: %s", spectating.size()),
						String.format("spectating: %s", getArrayContents(spectating)),
				};
				
				for (String s : debug)
					log.info(s);
				
				log.info(" ----- [ USED LOCATIONS ] -----");
				log.info("currentlyUsed - size: " + itemCore.currentlyUsed.size());
				
				for (final String nm : itemCore.currentlyUsed.keySet()) {
					final Location loc = itemCore.currentlyUsed.get(nm);
					
					log.info(String.format("%s: %s", nm, locationToString("(%s,%s,%s)", loc)));
				}
				
				log.info(" ----- [ SPAWNPOINTS ] -----");
				
				for (final UUID uuid : usedSpawnPoints.keySet()) {
					
					log.info(String.format("%s: %s", Bukkit.getPlayer(uuid).getName(), locationToString("(%s,%s,%s)", getSpawnPoints().get(usedSpawnPoints.get(uuid)))));
				}
			}
		}, 100L, 100L);
	}
	*/
	
	public void onEnable() {
		this.getConfig().options().copyDefaults(true);
		this.saveConfig();
		
		this.registerCommands();
		this.registerListeners();
		
		this.ess = (Essentials) pm.getPlugin("Essentials");
		
		this.worldCheck();
		
		if (!this.sb.wasLoaded) {
			this.sb.registerScoreboard();
			this.sb.wasLoaded = true;
			this.sb.addTeams();
		}
		
		 ActionBar.plugin = this;
		 ActionBar.nmsver = Bukkit.getServer().getClass().getPackage().getName();
		 ActionBar.nmsver = ActionBar.nmsver.substring(ActionBar.nmsver.lastIndexOf(".") + 1);

	     if (ActionBar.nmsver.equalsIgnoreCase("v1_8_") && ActionBar.nmsver.equalsIgnoreCase("v1_9_") && ActionBar.nmsver.equalsIgnoreCase("v1_10_") && ActionBar.nmsver.equalsIgnoreCase("v1_11_") && ActionBar.nmsver.equalsIgnoreCase("v1_12_") || ActionBar.nmsver.startsWith("v1_7_")) {
	    	 ActionBar.useOldMethods = true;
	     }
		
	     /**
	      * Start Check Thread
	      * INTERVAL: 20 Seconds
	      */
		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
			@Override
			public void run() {
				gameStartCheck();
			}
		}, 0L, 200L);
		
		/**
		 * Golden Stick Spawning Thread
		 * INTERVAL: 30 Seconds
		 */
		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
			@Override
			public void run() {
				if (isGameStarted)
					spawnGoldenStickMaybe();
				
			}
		}, 0L, 600L);
		
		/**
		 * Item Spawning Thread
		 * INTERVAL: 15 Seconds
		 */
		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
			@Override
			public void run() {
				if (isGameStarted) {
					itemCore.spawnRandomItem();
				}
			}
		}, 0L, 300L);
		
		/**
		 * Main Game Thread
		 * INTERVAL: 1 Second
		 */
		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
			@Override
			public void run() {
				for (final UUID uuid : currentPageHolder.keySet()) {
					if (currentPageHolder.get(uuid) == registeredPages.size() + 1)
						currentPageHolder.put(uuid, registeredPages.size());
				}
				
				if (!stock.isEmpty())
					stockCheck();
				
				if (sb.wasLoaded)
					sb.addTeams();
				
				gameCheck();
				
				for (final UUID uuid : abiCore.cooldown.keySet()) {
					long timeLeft = abiCore.cooldown.get(uuid);
						
					if (timeLeft == 0 && !abiCore.wasTold.contains(uuid) && !dead.contains(uuid)) {
						abiCore.wasTold.add(uuid);
						Bukkit.getPlayer(uuid).playSound(Bukkit.getPlayer(uuid).getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 6F, 1F);
						Bukkit.getPlayer(uuid).sendMessage(String.format("%s §a§lYou can use your §e§lRecovery §a§lagain!", Prefixes.NOTICE.getPrefix()));
					}
						
					if (!(timeLeft <= 0)) {
						try {
							Bukkit.getPlayer(uuid).setExp(Bukkit.getPlayer(uuid).getExp() + 0.1F);
						} catch (Exception exc) {
							Bukkit.getPlayer(uuid).setExp(1);
						}
						
						timeLeft -= 1000;
						abiCore.cooldown.put(uuid, timeLeft);
					}
				}
				
				for (final UUID uuid : safeCooldown.keySet()) {
					long timeLeft = safeCooldown.get(uuid);
					
					if (timeLeft == 0 && !dead.contains(uuid))
						safeCooldown.put(uuid, 0L);
					
					if (!(timeLeft <= 0)) {
						timeLeft -= 1000;
						safeCooldown.put(uuid, timeLeft);
					}
				}
				
			}
		}, 0L, 20L);
		
		//this._DEBUG();
	}
	
	public void onDisable() {
		for (final UUID uuid : this.currentlyPlaying) {
			final Player ply = Bukkit.getPlayer(uuid);
			
			this.removeAllPotionEffects(ply);
			
			if (!this.queue.contains(uuid)) {
				this.queue.add(uuid);
			}
		}
		
		this.currentlyPlaying.clear();
		
		this.sb.removePlayers(this.queue);
		
		for (final UUID uuid : this.queue) {
			final Player ply = Bukkit.getPlayer(uuid);
			final PlayerInventoryYaml pInvYml = new PlayerInventoryYaml(this, ply);
			
			ply.setGameMode(this.getLobbyGameMode());
			ply.teleport(this.lobbyLocation());
			
			pInvYml.restoreInventory(ply);
			pInvYml.deleteInventory(ply);
			
			// tell players just incase they're somehow still online
			ply.sendMessage("§4Plugin disabled. You've been removed from the game/queue.");
		}
		
		this.sb.removeTeams();
		this.removeItemsFromStage();
		
		this.alive.clear();
		this.dead.clear();
		this.stock.clear();
		this.queue.clear();
		this.usedSpawnPoints.clear();
		this.spectating.clear();
		
		this.isGameStarted = false;
		
		this.getServer().getServicesManager().unregisterAll(this);
		Bukkit.getScheduler().cancelTasks(this);
	}
	
	private void registerCommands() {
		this.getCommand("stickbattles").setExecutor(new Command_StickBattles(this));
	}
	
	private void registerListeners() {
		pm.registerEvents(new GameListeners(this), this);
		pm.registerEvents(new PreventionListeners(this), this);
		pm.registerEvents(new MenuListeners(this), this);
	}
	
	/**
	 * Display a countdown to a list of Players (UUID of players)
	 * @param duration = Best to make equal to the number of titles OR subtitles
	 * @param titles = List of titles
	 * @param subtitles = List of subtitles
	 * @param plys = Players who will be affected
	 * @param playSounds = Whether players will receive sounds or not
	 * @param freezePlayers = Whether players will be frozen or not
	 */
	public void performCountdown(int duration, List<String> titles, List<String> subtitles, List<UUID> plys, boolean playSounds, boolean freezePlayers) {
		final List<Integer> t = Arrays.asList(duration * 1000);
		
		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
			@Override
			public void run() {
				boolean ignoreFreeze = false;
				
				if (t.get(0) == 0)
					return;
				
				if (t.get(0) == 1000) {
					ignoreFreeze = true;
					frozen.clear();
				}
				
				String title = "";
				String subtitle = "";
				
				try {
					title = titles.get((t.get(0) / 1000) - 1);
				} catch (Exception exc) {
					title = "";
				}
				
				try {
					subtitle = subtitles.get((t.get(0) / 1000) - 1);
				} catch (Exception exc) {
					subtitle = "";
				}
				
				for (final UUID uuid : plys) {
					Bukkit.getPlayer(uuid).sendTitle(title, subtitle, 10, 20, 10);
					
					if (playSounds) {
						final float pitch = (t.get(0) <= 1000) ? 1F : 0F;
						Bukkit.getPlayer(uuid).playSound(Bukkit.getPlayer(uuid).getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 6F, pitch);
					}
					
					if (freezePlayers && !ignoreFreeze)
						if (!frozen.contains(uuid)) frozen.add(uuid);
				}
				
				t.set(0, t.get(0) - 1000);
			}
		}, 0L, 20L);
	}
	
	public void playSounds(int intervalBetween, List<UUID> plys, List<Sound> snds, List<Float> pitchs) {
		final List<Integer> interval = Arrays.asList(intervalBetween * 1000);

		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
			@Override
			public void run() {
				if (interval.get(0) == 0)
					return;
				
				for (final UUID uuid : plys) {
					Bukkit.getPlayer(uuid).playSound(Bukkit.getPlayer(uuid).getLocation(), snds.get((interval.get(0) / 1000) - 1), 6F, pitchs.get((interval.get(0) / 1000) - 1));
				}
				
				interval.set(0, interval.get(0) - 1000);
			}
		}, 0L, 20L);
	}
	
	public void spawnGoldenStickMaybe() {
		final double rd = Math.random() * 100;
		
		if (rd >= 80) {
			this.itemCore.spawnSpecialStick();
			return;
		}
	}
	
	public void gameCheck() {
		if (this.isGameStarted && (this.alive.isEmpty() && this.currentlyPlaying.isEmpty()))
			this.isGameStarted = false;
		
		if (!this.isGameStarted && this.queue.size() >= this.getMinimumRequirement() && !this.haveNotified) {
			this.haveNotified = true;
			
			for (final UUID uuid : this.queue) {
				this.sendPluginMessage(Bukkit.getPlayer(uuid), "§9The game will begin shortly...");
				Bukkit.getPlayer(uuid).playSound(Bukkit.getPlayer(uuid).getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 6F, 2F);
			}
		}
		
		String _col = "§a";
		
		final ActionBar ab = new ActionBar();
		
		for (final UUID uuid : this.alive) {
			final Player ply = Bukkit.getPlayer(uuid);
			final User usr = this.ess.getUser(ply);
			final double hp = ply.getHealth();
			
			usr.setGodModeEnabled(false);
			ply.setFlying(false);
			
			ply.setGameMode(GameMode.ADVENTURE);
			ply.setFoodLevel(20);
			
			ply.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 9999999, this.getSpeedLevel() - 1), true);
			ply.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 9999999, this.getJumpLevel() - 1), true);
			ply.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 9999999, this.getGlowingLevel() - 1), true);
			
			if (hp <= 20.0D && hp >= 15.0D) {
				this.sb.addPlayer(uuid, "sb-green");
				_col = "§a";
				ab.sendActionBar(ply, String.format("%s§lLives: §b§l%s", _col, this.stock.get(uuid)));
			} else if (hp <= 15.0D && hp >= 10.0D) {
				this.sb.addPlayer(uuid, "sb-yellow");
				_col = "§e";
				ab.sendActionBar(ply, String.format("%s§lLives: §b§l%s", _col, this.stock.get(uuid)));
			} else if (hp <= 10.0D && hp >= 5.0D) {
				this.sb.addPlayer(uuid, "sb-gold");
				_col = "§6";
				ab.sendActionBar(ply, String.format("%s§lLives: §b§l%s", _col, this.stock.get(uuid)));
			} else if (hp <= 5.0D && hp >= 1.0D) {
				this.sb.addPlayer(uuid, "sb-red");
				_col = "§c";
				ab.sendActionBar(ply, String.format("%s§lLives: §b§l%s", _col, this.stock.get(uuid)));
			}
		}
		
		for (final UUID uuid : this.dead) {
			final Player ply = Bukkit.getPlayer(uuid);
			
			ply.setGameMode(GameMode.SPECTATOR);
			
			this.removeAllPotionEffects(ply);
			
			ab.sendActionBar(ply, "§4§l You are dead.");
			
			if (!this.currentlyPlaying.contains(uuid))
				this.currentlyPlaying.add(uuid);
		}
	}
	
	public void stockCheck() {
		if (this.isGameStarted) {
			for (final UUID uuid : this.stock.keySet()) {
				final int stocksLeft = this.stock.get(uuid);
				
				if (stocksLeft <= 0 && !this.dead.contains(uuid)) {
					this.alive.remove(uuid);
					this.dead.add(uuid);
					
					for (final UUID u1 : this.currentlyPlaying) {
						Bukkit.getPlayer(u1).sendMessage(String.format("%s §6%s§8 is out!", Prefixes.OUT.getPrefix(), Bukkit.getPlayer(uuid).getName()));
					}
					
					for (final UUID u2 : this.queue) {
						if (!this.currentlyPlaying.contains(u2)) {
							Bukkit.getPlayer(u2).sendMessage(String.format("%s §6%s§8 is out!", Prefixes.OUT.getPrefix(), Bukkit.getPlayer(uuid).getName()));
						}
					}
				}
			}
		}
	}
	
	public void gameStartCheck() {
		if (this.isGameStarted) {
			this.haveNotified = false;
			
			if (!this.alive.isEmpty() && this.alive.size() <= 1) {
				// game has a winner (THE GAME IS OVER)
				if (this.alive.get(0) != null) {
					final Player winner = Bukkit.getPlayer(this.alive.get(0));
					final PlayerDataYaml pYml = new PlayerDataYaml(this, winner);
					
					pYml.setWins(pYml.getWins(winner) + 1);
					
					for (final UUID uuid : this.currentlyPlaying) {
						Bukkit.getPlayer(uuid).sendTitle("§a§lWINNER", String.format("§b%s §2has won with §c%s HP §2remaining!", winner.getName(), Math.round(winner.getHealth())), 10, 70, 20);
						Bukkit.getPlayer(uuid).sendMessage(String.format("%s §b%s §2has won with §c%s HP §2remaining!", Prefixes.WINNER.getPrefix(), winner.getName(), Math.round(winner.getHealth())));
					}
					
					// send same message to ppl not currentlyPlaying and only in queue, but only if they're not somehow in currentlyPlaying
					for (final UUID uuid : this.queue) {
						if (!this.currentlyPlaying.contains(uuid)) {
							Bukkit.getPlayer(uuid).sendTitle("§a§lWINNER", String.format("§b%s §2has won with §c%s HP §2remaining!", winner.getName(), Math.round(winner.getHealth())), 10, 70, 20);
							Bukkit.getPlayer(uuid).sendMessage(String.format("%s §b%s §2has won with §c%s HP §2remaining!", Prefixes.WINNER.getPrefix(), winner.getName(), Math.round(winner.getHealth())));
						}
					}
					
					this.awardDrops(winner);
					this.endGame();
				} else {
					for (final UUID uuid : this.currentlyPlaying) {
						this.sendPluginMessage(Bukkit.getPlayer(uuid), "§cThere was no winner.");
					}
					
					for (final UUID uuid : this.queue) {
						if (!this.currentlyPlaying.contains(uuid)) {
							this.sendPluginMessage(Bukkit.getPlayer(uuid), "§cThere was no winner.");
						}
					}
					
					this.awardDrops(null);
					this.endGame();
				}
			}
			
			if (this.alive.size() == 0 && this.dead.size() != 0){
				// game is going but no one is alive (no winner)
				for (final UUID uuid : this.currentlyPlaying) {
					this.sendPluginMessage(Bukkit.getPlayer(uuid), "§cThere was no winner.");
				}
				
				for (final UUID uuid : this.queue) {
					if (!this.currentlyPlaying.contains(uuid)) {
						this.sendPluginMessage(Bukkit.getPlayer(uuid), "§cThere was no winner.");
					}
				}
				
				this.awardDrops(null);
				this.endGame();
			}
			// game already in progress
			
			if (!this.didGameJustEnd) {
				for (final UUID uuid : this.queue) {
					Bukkit.getPlayer(uuid).sendMessage("§cGame already in progress.");
					
					if (!this.spectating.contains(uuid))
						this.spectating.add(uuid);
				}
			}
		} else {
			// game not started
			if (this.queue.size() >= this.getMinimumRequirement()) {
				this.startGame();
				return;
			}
		}
	}
	
	public void startGame() {
		this.didGameJustEnd = false;
		this.gameJustStarted = true;
		
		new BukkitRunnable() {
			@Override
			public void run() {
				gameJustStarted = false;
			}
		}.runTaskLater(this, 200L);
		
		// make sure the game is safely ended first
		if (!this.currentlyPlaying.isEmpty()) {
			for (final UUID uuid : this.currentlyPlaying) {
				this.removePlayerFromGameOrQueue(Bukkit.getPlayer(uuid));
			}
		}
		
		this.spectating.clear();
		this.currentlyPlaying.clear();
		this.alive.clear();
		this.dead.clear();
		this.stock.clear();
		this.usedSpawnPoints.clear();
		this.abiCore.cooldown.clear();
		
		for (final UUID uuid : this.queue)
			this.currentlyPlaying.add(uuid);
		
		this.queue.clear();
		
		for (final UUID uuid : this.currentlyPlaying) {
			this.alive.add(uuid);
			this.stock.put(uuid, this.getStartingStock());
			Bukkit.getPlayer(uuid).setHealth(20D);
			Bukkit.getPlayer(uuid).setExp(1F);
			this.assignRandomSpawnPoints(uuid); // give user's their random spawn point
		}
		
		// teleport them to said random spawn point
		// give them the game items
		for (final UUID uuid : this.usedSpawnPoints.keySet()) {
			final int locId = this.usedSpawnPoints.get(uuid);
			final Player ply = Bukkit.getPlayer(uuid);
			final Location spawnLoc = this.getSpawnPoints().get(locId);
			
			this.kills.put(ply.getUniqueId(), 0);
			
			ply.teleport(spawnLoc);
			
			final Location loc = ply.getLocation();
			
			loc.setYaw(this.getCenteredYaw(ply));
			
			ply.teleport(loc);
			
			ply.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 9999999, this.getSpeedLevel()), true);
			ply.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 9999999, this.getJumpLevel()), true);
			ply.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 9999999, this.getGlowingLevel()), true);
			
			ply.getInventory().setItem(0, this.itemCore.getStick(ply));
		}
		
		this.isGameStarted = true;
		this.performCountdown(4, Arrays.asList("§4§lGO!", "§c§l1", "§6§l2", "§a§l3"), Arrays.asList("§6Knock your Opponents off the Stage!"), this.currentlyPlaying, true, true);
		this.performCountdown(4, Arrays.asList("§4§lGO!", "§c§l1", "§6§l2", "§a§l3"), Arrays.asList("§6Knock your Opponents off the Stage!"), this.queue, true, false);
	}
	
	public void endGame() {
		this.isGameStarted = false;
		this.didGameJustEnd = true;
		this.haveNotified = false;
		
		for (final UUID uuid : this.currentlyPlaying) {
			final Player ply = Bukkit.getPlayer(uuid);
			
			this.removeAllPotionEffects(ply);
			
			if (!this.queue.contains(uuid))
				this.queue.add(uuid);
		}
		
		for (final UUID uuid : this.kills.keySet()) {
			final Player ply = Bukkit.getPlayer(uuid);
			
			if (this.kills.get(uuid) != 0)
				this.sendPluginMessage(ply, String.format("§7You had §c%s Kill%s§7 that game!", this.kills.get(ply.getUniqueId()), (this.kills.get(ply.getUniqueId()) > 1) ? "s" : ""));
		}
		
		for (final Entity ent : Bukkit.getWorld(this.getGameWorldName()).getEntities()) {
			if (ent instanceof EnderPearl)
				ent.remove();
		}
		
		this.sb.removePlayers(this.queue);
		
		this.alive.clear();
		this.dead.clear();
		this.usedSpawnPoints.clear();
		this.stock.clear();
		this.currentlyPlaying.clear();
		this.abiCore.cooldown.clear();
		this.spectating.clear();
		this.kills.clear();
		this.frozen.clear();
		this.itemCore.currentlyUsed.clear();
		
		this.removeItemsFromStage();
		
		for (final UUID uuid : this.queue) {
			final Player ply = Bukkit.getPlayer(uuid);
			
			ply.setHealth(20D);
			ply.setFoodLevel(20);
			ply.getInventory().clear();
			ply.setExp(0F);
			ply.setLevel(0);
			ply.setGameMode(this.getLobbyGameMode());
			ply.teleport(this.lobbyLocation());
			
			this.sendPluginMessage(ply, String.format("§aYou've been re-added to the queue! §8(§b%s§7/§9%s§8)", queue.size(), this.getMinimumRequirement()));
		}
	}
	
	public void awardDrops(final Player winner) {
		final Map<UUID, Integer> payOuts = new HashMap<UUID, Integer>();
		
		for (final UUID uuid : this.currentlyPlaying) {
			int winB = 0;
			
			if (this.kills.get(uuid) == null)
				this.kills.put(uuid, 0);
			
			if ((Bukkit.getPlayer(uuid) == winner))
				winB = this.getWinnerBonusReward();
			
			payOuts.put(uuid, (winB + this.getBaseReward() + (this.kills.get(uuid) * this.getKillBonusReward())));
			
			final PlayerDataYaml pYml = new PlayerDataYaml(this, Bukkit.getPlayer(uuid));
			final Names wonItem = this.nameCore.getRandomName();
			
			if (wonItem != null && !pYml.getWonNames(Bukkit.getPlayer(uuid)).contains(wonItem.getId())) {
				pYml.addWonName(Bukkit.getPlayer(uuid), wonItem.getId());
				Bukkit.broadcastMessage(String.format("%s §b%s §7found %s§7!", Prefixes.ITEM.getPrefix(), Bukkit.getPlayer(uuid).getName(), wonItem.getName()));
			}
		}
		
		for (final UUID uuid : this.queue) {
			if (!this.currentlyPlaying.contains(uuid)) {
				final PlayerDataYaml pYml = new PlayerDataYaml(this, Bukkit.getPlayer(uuid));
				final Names wonItem = this.nameCore.getRandomName();
				
				if (wonItem != null && !pYml.getWonNames(Bukkit.getPlayer(uuid)).contains(wonItem.getId()))
					Bukkit.broadcastMessage(String.format("%s §b%s §7found %s§7!", Prefixes.ITEM.getPrefix(), Bukkit.getPlayer(uuid).getName(), wonItem.getName()));
			}
		}
		
		for (final UUID uuid : payOuts.keySet()) {
			final User usr = this.ess.getUser(uuid);
			
			try {
				usr.giveMoney(BigDecimal.valueOf(payOuts.get(uuid)));
			} catch (MaxMoneyException e) {}
		}
	}
	
	public void removeItemsFromStage() {
		final World gameWorld = Bukkit.getWorld(this.getGameWorldName());
		
		for (final Entity ent : gameWorld.getEntities()) {
			if (ent instanceof Item) {
				
				if (this.isInArenaBounds(ent))
					ent.remove();
				else return;
			}
		}
	}
	
	public void removeAllPotionEffects(final Player ply) {
		try {
			for (final PotionEffect p : ply.getActivePotionEffects())
				ply.removePotionEffect(p.getType());
		} catch (Exception exc) {}
	}
	
	private void worldCheck() {
		if (Bukkit.getWorld(this.getGameWorldName()) == null) {
			final WorldCreator creator = new WorldCreator(this.getGameWorldName());
			
			creator.generator(new ChunkGenerator() {
				@Override
				public byte[] generate(World world, Random random, int x, int z) {
					return new byte[32768]; //Empty byte array
				}
			});
			
			Bukkit.createWorld(creator);
			
			final World w = Bukkit.getWorld(this.getGameWorldName());
			
			w.setAutoSave(false);
			w.setDifficulty(Difficulty.PEACEFUL);
			w.setPVP(true);
			w.setGameRuleValue("naturalRegeneration", "false");
			w.setGameRuleValue("doWeatherCycle", "false"); // probably already is false tbh
			w.setGameRuleValue("doEntityDrops", "false"); // eh not really
			w.setGameRuleValue("doMobSpawning", "false");
			w.setGameRuleValue("doDaylightCycle", "false");
			w.setTime(0L);
		}
	}
	
	public void sendPluginMessage(final Player ply, final String msg) {
		ply.sendMessage(String.format("%s %s", Prefixes.GAME.getPrefix(), ChatColor.translateAlternateColorCodes('&', msg)));
	}
	
	public void sendPluginMessage(final CommandSender sender, final String msg) {
		sender.sendMessage(String.format("%s %s", Prefixes.GAME.getPrefix(), ChatColor.translateAlternateColorCodes('&', msg)));
	}
	
	public boolean addPlayerToQueue(final Player ply) {
		if (this.isWhitelistEnabled()) {
			if (!this.getWhitelist().contains(ply.getUniqueId())) {
				this.sendPluginMessage(ply, "§c§lYou're not whitelisted!");
				return false;
			}
		}
		
		if (this.getBlacklist().contains(ply.getUniqueId())) {
			this.sendPluginMessage(ply, "§4§lYou are banned from playing.");
			return false;
		}
		
		if (!this.currentlyPlaying.contains(ply.getUniqueId()) && !this.queue.contains(ply.getUniqueId())) {
			final PlayerInventoryYaml pInvYml = new PlayerInventoryYaml(this, ply);
			
			for (final UUID uuid : this.queue)
				this.sendPluginMessage(Bukkit.getPlayer(uuid), String.format("§2%s §ahas joined the queue! §8(§b%s§7/§9%s§8)", ply.getName(), queue.size() + 1, this.getMinimumRequirement()));
			
			queue.add(ply.getUniqueId());
			
			pInvYml.storeInventory(ply);
			
			this.removeAllPotionEffects(ply);
			
			ply.getInventory().setContents(new ItemStack[] { new ItemStack(Material.AIR) });
			ply.setExp(0F);
			ply.setLevel(0);
			ply.setHealth(20D);
			ply.setFoodLevel(20);
			
			ply.setGameMode(this.getLobbyGameMode());
			ply.teleport(this.lobbyLocation());
			
			this.sendPluginMessage(ply, String.format("§aYou've been added to the queue! §8(§b%s§7/§9%s§8)", queue.size(), this.getMinimumRequirement()));
			return true;
		} else {
			this.sendPluginMessage(ply, "§cYou're already in the queue!");
			return false;
		}
	}
	
	private void removePlayerFromStorage(Player ply) {
		this.currentlyPlaying.remove(ply.getUniqueId());
		this.alive.remove(ply.getUniqueId());
		this.dead.remove(ply.getUniqueId());
		this.queue.remove(ply.getUniqueId());
		this.usedSpawnPoints.remove(ply.getUniqueId());
		this.stock.remove(ply.getUniqueId());
		this.abiCore.cooldown.remove(ply.getUniqueId());
		this.abiCore.wasTold.remove(ply.getUniqueId());
		this.sb.removePlayer(ply.getUniqueId());
		this.spectating.remove(ply.getUniqueId());
		this.kills.remove(ply.getUniqueId());
		this.frozen.remove(ply.getUniqueId());
	}
	
	public void removePlayerQueueDontTeleport(Player ply) {
		final PlayerInventoryYaml pInvYml = new PlayerInventoryYaml(this, ply);
		
		pInvYml.restoreInventory(ply);
		pInvYml.deleteInventory(ply);
		
		this.removeAllPotionEffects(ply);
		
		ply.setGameMode(GameMode.SURVIVAL);
		
		this.removePlayerFromStorage(ply);
		
		for (final Entity ent : Bukkit.getWorld(this.getGameWorldName()).getEntities()) {
			if (ent instanceof EnderPearl) {
				final Player shooter = (Player) ((EnderPearl) ent).getShooter();
				
				if (shooter == ply)
					ent.remove();
			}
		}
		
		this.sendPluginMessage(ply, "§2You have been removed from the queue.");
		
		for (final UUID uuid : this.queue)
			this.sendPluginMessage(Bukkit.getPlayer(uuid), String.format("§4%s §chas left the queue! §8(§b%s§7/§9%s§8)", ply.getName(), queue.size(), this.getMinimumRequirement()));
	}
	
	public boolean removePlayerFromGameOrQueue(Player ply) {
		final PlayerInventoryYaml pInvYml = new PlayerInventoryYaml(this, ply);
		final PlayerDataYaml pYml = new PlayerDataYaml(this, ply);
		
		for (final Entity ent : Bukkit.getWorld(this.getGameWorldName()).getEntities()) {
			if (ent instanceof EnderPearl) {
				final Player shooter = (Player) ((EnderPearl) ent).getShooter();
				
				if (shooter == ply)
					ent.remove();
			}
		}
		
		if (this.queue.contains(ply.getUniqueId())) { // isn't in a game, but is queued for a game
			// restore their inventory
			pInvYml.restoreInventory(ply);
			pInvYml.deleteInventory(ply);
			
			this.removeAllPotionEffects(ply);
			
			// safely move them out of the arena
			ply.setGameMode(this.getLobbyGameMode());
			ply.teleport(this.lobbyLocation());
			
			this.removePlayerFromStorage(ply);
			
			this.sendPluginMessage(ply, "§2You have been removed from the queue.");
			
			for (final UUID uuid : this.queue)
				this.sendPluginMessage(Bukkit.getPlayer(uuid), String.format("§4%s §chas left the queue! §8(§b%s§7/§9%s§8)", ply.getName(), queue.size(), this.getMinimumRequirement()));
			
			return true;
		} else {
			// is currently in a game
			if (this.currentlyPlaying.contains(ply.getUniqueId())) {
				if (this.alive.contains(ply.getUniqueId())) {
					// player was still alive
					pYml.setLosses(pYml.getLosses(ply) + 1);
					this.sendPluginMessage(ply, "§cBy leaving during the game, you forfitted the game.");
					
					// restore their inventory
					pInvYml.restoreInventory(ply);
					pInvYml.deleteInventory(ply);
					
					this.removeAllPotionEffects(ply);
					
					// safely move them out of the arena
					ply.setGameMode(this.getLobbyGameMode());
					ply.teleport(this.lobbyLocation());
					
					this.removePlayerFromStorage(ply);
					
					for (final UUID uuid : this.currentlyPlaying) {
						this.sendPluginMessage(Bukkit.getPlayer(uuid), String.format("§4%s§c left the game!", ply.getName()));
					}
					
					for (final UUID uuid : this.queue) {
						if (!this.currentlyPlaying.contains(uuid)) {
							this.sendPluginMessage(Bukkit.getPlayer(uuid), String.format("§4%s§c left the game!", ply.getName()));
						}
					}
					
					this.sendPluginMessage(ply, "§2You've been removed from the game.");
					return true;
				} else {
					// player was already dead, spectating the game
					pYml.setLosses(pYml.getLosses(ply) + 1); // player already lost the game, so just add the loss
					
					// restore their inventory
					pInvYml.restoreInventory(ply);
					pInvYml.deleteInventory(ply);
					
					this.removeAllPotionEffects(ply);
					
					// safely move them out of the arena
					ply.setGameMode(this.getLobbyGameMode());
					ply.teleport(this.lobbyLocation());
					
					this.removePlayerFromStorage(ply);
					
					this.sendPluginMessage(ply, "§2You've been safely removed from the game.");
					return true;
				}
			} else {
				// not in queue or game
				this.sendPluginMessage(ply, "§cYou're not in a game!");
				return false;
			}
		}
	}
	
	public long toSeconds(final long time) {
		return TimeUnit.MILLISECONDS.toSeconds(time);
	}
	
	public double getKnockbackLevel(final Player ply) {
		return Math.floor(1 + (20 - ply.getHealth()) / 2);
	}
	
	public Vector getKnockbackVector(final Player ply) {
		final double xDir = Math.round(ply.getLocation().getDirection().normalize().getX());
		final double zDir = Math.round(ply.getLocation().getDirection().normalize().getZ());
		final double k = this.getKnockbackLevel(ply);
		
		return new Vector(-(k * xDir), 0.2, -(k * zDir));
	}
	
	public Entity getRandomSpectateTarget() {
		return Bukkit.getPlayer(this.alive.get(rn.nextInt(this.alive.size())));
	}
	
	public Location getRandomLocation() {
		final int x = rn.nextInt((this.getArenaBounds()[0].getBlockX() - this.getArenaBounds()[1].getBlockX()) + 1) + this.getArenaBounds()[1].getBlockX();
		final int z = rn.nextInt((this.getArenaBounds()[0].getBlockZ() - this.getArenaBounds()[1].getBlockZ()) + 1) + this.getArenaBounds()[1].getBlockZ();
		
		return new Location(Bukkit.getWorld(this.getGameWorldName()), x, this.getConfig().getInt("arena.fall-start-y"), z);
	}
	
	public int getRandomItemLocation() {
		return rn.nextInt(this.getItemSpawnPoints().size());
	}
	
	public void assignRandomSpawnPoints(UUID uuid) {
		while (!this.usedSpawnPoints.containsKey(uuid)) {
			final int rnInd = rn.nextInt(this.getSpawnPoints().size());
			
			if (!this.usedSpawnPoints.containsValue(rnInd)) {
				this.usedSpawnPoints.put(uuid, rnInd);
				break;
			}
		}
	}
	
	public boolean isInArenaBounds(final Entity ent) {
		final Location loc = ent.getLocation();
		final int x = loc.getBlockX();
		final int y = loc.getBlockY();
		final int z = loc.getBlockZ();
		
		if (x >= this.getArenaBounds()[0].getBlockX() || x <= this.getArenaBounds()[1].getBlockX()
				|| y >= this.getArenaBounds()[1].getBlockY() || y > 256
				|| z >= this.getArenaBounds()[0].getBlockZ() || z <= this.getArenaBounds()[1].getBlockZ())
			return true;
		else return false;
	}
	
	public Location[] getArenaBounds() {
		this.reloadConfig();
		return new Location[] { this.getLocFromString(this.getConfig().getString("arena.bounds.pos1")), this.getLocFromString(this.getConfig().getString("arena.bounds.pos2")) };
	}
	
	public List<Location> getSpawnPoints() {
		this.reloadConfig();
		final List<Location> spawnPoints = new ArrayList<Location>();
		
		for (final String c : this.getConfig().getStringList("arena.spawn-points")) {
			if (!spawnPoints.contains(this.getLocFromString(c)))
				spawnPoints.add(this.getLocFromString(c));
		}
		
		return spawnPoints;
	}
	
	public List<Location> getItemSpawnPoints() {
		this.reloadConfig();
		final List<Location> itemPoints = new ArrayList<Location>();
		
		for (final String c : this.getConfig().getStringList("arena.item-points")) {
			if (!itemPoints.contains(this.getLocFromString(c)))
				itemPoints.add(this.getLocFromString(c));
		}
		
		return itemPoints;
	}
	
	public Location getGoldenStickSpawn() {
		this.reloadConfig();
		return this.getLocFromString(this.getConfig().getString("arena.golden-stick-spawn-point"));
	}
	
	public GameMode getLobbyGameMode() {
		this.reloadConfig();
		
		try {
			return GameMode.valueOf(this.getConfig().getString("lobby.gamemode").toUpperCase());
		} catch (Exception e) {
			log.warning("[lobby.gamemode] " + this.getConfig().getString("lobby.gamemode") + " is not a valid GameMode!");
			return GameMode.SURVIVAL;
		}
	}
	
	public Location getLobbySpawnPoint() {
		this.reloadConfig();
		return this.getLocFromString(this.getConfig().getString("lobby.spawn-point"));
	}
	
	public String getGameWorldName() {
		this.reloadConfig();
		return this.getConfig().getString("game-settings.world-name");
	}
	
	public int getSpeedLevel() {
		this.reloadConfig();
		return this.getConfig().getInt("game-settings.speed-lvl");
	}
	
	public int getJumpLevel() {
		this.reloadConfig();
		return this.getConfig().getInt("game-settings.jump-lvl");
	}
	
	public int getGlowingLevel() {
		this.reloadConfig();
		return this.getConfig().getInt("game-settings.glowing-lvl");
	}
	
	public int getNightVisionLevel() {
		this.reloadConfig();
		return this.getConfig().getInt("game-settings.nightvision-lvl");
	}
	
	public int getStartingStock() {
		this.reloadConfig();
		return this.getConfig().getInt("game-settings.stocks");
	}
	
	public int getMinimumRequirement() {
		this.reloadConfig();
		return this.getConfig().getInt("game-settings.minimum-players");
	}
	
	public int getBaseReward() {
		this.reloadConfig();
		return this.getConfig().getInt("game-settings.win-rewards.base");
	}
	
	public int getKillBonusReward() {
		this.reloadConfig();
		return this.getConfig().getInt("game-settings.win-rewards.kill");
	}
	
	public int getWinnerBonusReward() {
		this.reloadConfig();
		return this.getConfig().getInt("game-settings.win-rewards.winner");
	}
	
	public Location getArenaCenter() {
		this.reloadConfig();
		return this.getLocFromString(this.getConfig().getString("arena.center"));
	}
	
	public boolean isWhitelistEnabled() {
		this.reloadConfig();
		return this.getConfig().getBoolean("game-settings.useWhitelist");
	}
	
	public void setWhitelistEnabled(final boolean enabled) {
		this.getConfig().set("game-settings.useWhitelist", enabled);
		this.saveConfig();
	}
	
	public void getWhitelistStatus(final CommandSender sender) {
		this.sendPluginMessage(sender, "§3Whitelist Status: " + (this.isWhitelistEnabled() ? "§aEnabled" : "§cDisabled"));
	}
	
	public List<UUID> getWhitelist() {
		this.reloadConfig();
		
		final List<UUID> uuids = new ArrayList<UUID>();
		
		for (final String u : this.getConfig().getStringList("whitelist"))
			uuids.add(UUID.fromString(u));
		
		return uuids;
	}
	
	public List<UUID> getBlacklist() {
		this.reloadConfig();
		
		final List<UUID> uuids = new ArrayList<UUID>();
		
		for (final String u : this.getConfig().getStringList("blacklist"))
			uuids.add(UUID.fromString(u));
		
		return uuids;
	}
	
	public void addToWhitelist(final CommandSender sender, final UUID uuid) {
		if (this.getWhitelist().contains(uuid)) {
			this.sendPluginMessage(sender, String.format("§c%s §4is already whitelisted!", Bukkit.getOfflinePlayer(uuid).getName()));
			return;
		} else {
			final List<String> wl = this.getConfig().getStringList("whitelist");
			
			wl.add(uuid.toString());
			
			this.getConfig().set("whitelist", wl);
			this.saveConfig();
			
			this.sendPluginMessage(sender, String.format("§2Added §a%s §2to the whitelist!", Bukkit.getOfflinePlayer(uuid).getName()));
			return;
		}
	}
	
	public void removeFromWhitelist(final CommandSender sender, final UUID uuid) {
		if (!this.getWhitelist().contains(uuid)) {
			this.sendPluginMessage(sender, String.format("§c%s §4isn't whitelisted!", Bukkit.getOfflinePlayer(uuid).getName()));
			return;
		} else {
			final List<String> wl = this.getConfig().getStringList("whitelist");
			
			wl.remove(uuid.toString());
			
			this.getConfig().set("whitelist", wl);
			this.saveConfig();
			
			this.sendPluginMessage(sender, String.format("§9Removed §3%s §9from the whitelist!", Bukkit.getOfflinePlayer(uuid).getName()));
			return;
		}
	}
	
	public void addToBlacklist(final CommandSender sender, final UUID uuid) {
		if (this.getBlacklist().contains(uuid)) {
			this.sendPluginMessage(sender, String.format("§c%s §4is already blacklisted!", Bukkit.getOfflinePlayer(uuid).getName()));
			return;
		} else {
			final List<String> bl = this.getConfig().getStringList("blacklist");
			
			bl.add(uuid.toString());
			
			this.getConfig().set("blacklist", bl);
			this.saveConfig();
			
			this.sendPluginMessage(sender, String.format("§4Banned §c%s§4!", Bukkit.getOfflinePlayer(uuid).getName()));
			return;
		}
	}
	
	public void removeFromBlacklist(final CommandSender sender, final UUID uuid) {
		if (!this.getBlacklist().contains(uuid)) {
			this.sendPluginMessage(sender, String.format("§c%s §4isn't banned!", Bukkit.getOfflinePlayer(uuid).getName()));
			return;
		} else {
			final List<String> bl = this.getConfig().getStringList("blacklist");
			
			bl.remove(uuid.toString());
			
			this.getConfig().set("blacklist", bl);
			this.saveConfig();
			
			this.sendPluginMessage(sender, String.format("§9Unbanned §3%s§9!", Bukkit.getOfflinePlayer(uuid).getName()));
			return;
		}
	}
	
	public int[] getListFromLocation(final Location loc) {
		return new int[] { loc.getBlockX(), loc.getBlockY(), loc.getBlockZ() };
	}
	
	public Location getLocFromString(final String coords) {
		final String name = this.getGameWorldName();
		final double x = Integer.parseInt(coords.substring(coords.indexOf("x:") + 2, coords.indexOf(",y:")));
		final double y = Integer.parseInt(coords.substring(coords.indexOf("y:") + 2, coords.indexOf(",z:")));
		final double z = Integer.parseInt(coords.substring(coords.indexOf("z:") + 2, coords.length()));
	    
	    return new Location(Bukkit.getWorld(name), x, y, z);
	}
	
	private Location lobbyLocation() {
		final String name = this.getGameWorldName();
		final double x = this.getLobbySpawnPoint().getBlockX() + 0.5;
		final double y = this.getLobbySpawnPoint().getBlockY();
		final double z = this.getLobbySpawnPoint().getBlockZ() + 0.5;
		
	    return new Location(Bukkit.getWorld(name), x, y, z, 179, 2);
	}
	
	public String locationToString(final String format, final Location loc) {
		if (loc != null)
			return String.format(format, loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
		else return "null";
	}
	
	public String getArrayContents(List<?> list) {
		String conts = "";
		
		for (Object item : list)
			conts += item.toString() + ", ";
		
		return conts;
	}
    
    public float getCenteredYaw(final Player ply) {
    	return this.getAngle(new Vector(ply.getLocation().getX(), 0, ply.getLocation().getZ()), this.getArenaCenter().toVector());
    }
    
    public float getAngle(Vector pnt1, Vector pnt2) {
        final double dx = pnt2.getX() - pnt1.getX();
        final double dz = pnt2.getZ() - pnt1.getZ();
        
        float angle = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90;
        
        if (angle < 0)
            angle += 360.0F;
        
        return angle;
    }


}
