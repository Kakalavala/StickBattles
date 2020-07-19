package me.kakalavala.chris.commands;

import java.text.DecimalFormat;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import me.kakalavala.chris.assets.PlayerDataYaml;
import me.kakalavala.chris.core.Core;
import me.kakalavala.chris.core.Core.Prefixes;
import me.kakalavala.chris.core.NameCore.Names;

public class Command_StickBattles implements CommandExecutor {
	
	private Core core;
	
	private final String exp = "§ePosition §7- §bPlayer §8(§aWins§7/§cLosses §7- §6Kills§7/§4Deaths§8) [§2Win % §7- §6K/D§8]";
	
	public Command_StickBattles(Core core) {
		this.core = core;
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String command, String[] args) {
		final boolean isPlayer = (sender instanceof Player);
		
		command = command.toLowerCase();
		
		if (args.length == 0) {
			this.sendHelpMessage(sender, command);
			return false;
		} else {
			if (args.length == 1) {
				switch (args[0].toLowerCase()) {
					case "join":
						if (isPlayer) return joinGame((Player) sender);
						else {
							this.sendConsoleErrorMessage(sender);
							return false;
						}
					case "leave":
						if (isPlayer) return leaveGame((Player) sender);
						else {
							this.sendConsoleErrorMessage(sender);
							return false;
						}
					case "leaderboard":
						return this.showLeaderboard(sender);
					case "stats":
						if (isPlayer) return showOwnStats((Player) sender);
						else {
							this.sendConsoleErrorMessage(sender);
							return false;
						}
					case "debug":
						if (sender.hasPermission("stickbattles.command.debug")) {
							this.debugCommand(sender);
							return true;
						} else return false;
					case "selectname":
						if (isPlayer) {
							((Player) sender).openInventory(core.nameCore.getNewNameInventory((Player) sender));
							return true;
						} else {
							this.sendConsoleErrorMessage(sender);
							return false;
						}
					case "endgame":
						if (sender.hasPermission("stickbattles.command.endgame")) {
							core.endGame();
							
							for (final UUID uuid : core.currentlyPlaying)
								core.removePlayerFromGameOrQueue(Bukkit.getPlayer(uuid));
							
							for (final UUID uuid : core.queue)
								core.removePlayerFromGameOrQueue(Bukkit.getPlayer(uuid));
							
							sender.sendMessage(String.format("%s §cEnded the game, if one was in progress.", Prefixes.GAME.getPrefix()));
							return true;
						} else return false;
					default:
						this.sendHelpMessage(sender, command);
						return false;
				}
			} else if (args.length == 2) {
				if (args[0].equalsIgnoreCase("stats")) {
					return this.showOtherStats(sender, args[1]);
				} else if (args[0].equalsIgnoreCase("selectname")) {
					if (!isPlayer) {
						this.sendConsoleErrorMessage(sender);
						return false;
					}
					
					final Player ply = (Player) sender;
					
					try {
						final PlayerDataYaml pYml = new PlayerDataYaml(core, ply);
						final int id = Integer.parseInt(args[1]);
						boolean wasValid = false;
						
						for (final Names n : Names.values()) {
							if (id == n.getId()) {
								if (pYml.getWonNames(ply).contains(id)) {
									wasValid = true;
									pYml.setActiveName(id);
									ply.playSound(ply.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 5F, 1F);
									ply.sendMessage(String.format("%s §7Now using \"%s§7\"", Prefixes.ITEM.getPrefix(), n.getName()));
									
									if (core.currentlyPlaying.contains(ply.getUniqueId())) {
										if (ply.getInventory().getItem(0).getType().equals(Material.STICK))
											ply.getInventory().setItem(0, core.itemCore.getRenamedStick(n.getName()));
									}
									
									break;
								} else {
									wasValid = true;
									core.sendPluginMessage(ply, "§cYou haven't unlocked that stick name yet!");
									break;
								}
							}
						}
						
						if (!wasValid) {
							core.sendPluginMessage(ply, "§cThat ID was invalid.");
							return false;
						}
						
					} catch (Exception e) {
						if (args[1].equalsIgnoreCase("reset")) {
							final PlayerDataYaml pYml = new PlayerDataYaml(core, ply);
							
							pYml.setActiveName(-1);
							ply.playSound(ply.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 5F, 1F);
							ply.sendMessage(String.format("%s §cStick Name was reset.", Prefixes.ITEM.getPrefix()));
							
							if (core.currentlyPlaying.contains(ply.getUniqueId())) {
								if (ply.getInventory().getItem(0).getType().equals(Material.STICK))
									ply.getInventory().setItem(0, core.itemCore.getRenamedStick("§bBasic Stick"));
							}
							
							return true;
						} else {
							core.sendPluginMessage(ply, "§cThat ID was invalid.");
							return false;
						}
					}
				} else if (args[0].equalsIgnoreCase("whitelist")) {
					switch (args[1].toLowerCase()) {
						case "status":
							core.getWhitelistStatus(sender);
							return true;
						case "enable":
							core.setWhitelistEnabled(true);
							return true;
						case "disable":
							core.setWhitelistEnabled(false);
							return true;
					}
				} else if (args[0].equalsIgnoreCase("ban")) {
					this.banPlayer(sender, args[1]);
					return true;
				} else if (args[0].equalsIgnoreCase("unban")) {
					this.unbanPlayer(sender, args[1]);
					return true;
				} else {
					this.sendHelpMessage(sender, command);
					return false;
				}
			} else if (args.length == 3) {
				if (args[0].equalsIgnoreCase("whitelist")) {
					switch (args[1].toLowerCase()) {
						case "add":
							this.whitelistPlayer(sender, args[2]);
							return true;
						case "remove":
							this.unwhitelistPlayer(sender, args[2]);
							return true;
					}
				} else {
					this.sendHelpMessage(sender, command);
					return false;
				}
			} else {
				this.sendHelpMessage(sender, command);
				return false;
			}
		}
		
		return false;
	}
	
	private void sendConsoleErrorMessage(CommandSender sender) {
		sender.sendMessage(String.format("%s §cOnly players can preform that command.", Prefixes.GAME.getPrefix()));
		return;
	}
	
	private void sendHelpMessage(CommandSender sender, String command) {
		sender.sendMessage(String.format("%s §b/%s join", Prefixes.GAME.getPrefix(), command));
		sender.sendMessage(String.format("%s §b/%s leave", Prefixes.GAME.getPrefix(), command));
		sender.sendMessage(String.format("%s §b/%s leaderboard", Prefixes.GAME.getPrefix(), command));
		sender.sendMessage(String.format("%s §b/%s stats §7[player]", Prefixes.GAME.getPrefix(), command));
		sender.sendMessage(String.format("%s §b/%s selectname §7[id/reset]", Prefixes.GAME.getPrefix(), command));
		if (sender.hasPermission("stickbattles.command.endgame")) core.sendPluginMessage(sender, String.format("§c/%s endgame", command));
		if (sender.hasPermission("stickbattles.command.whitelist")) core.sendPluginMessage(sender, String.format("§c/%s whitelist <add|remove|status|enable|disable> <player>", command));
		if (sender.hasPermission("stickbattles.command.blacklist")) core.sendPluginMessage(sender, String.format("§c/%s <ban|unban> <player>", command));
	}
	
	public boolean joinGame(Player ply) {
		return core.addPlayerToQueue(ply);
	}

	public boolean leaveGame(Player ply) {
		return core.removePlayerFromGameOrQueue(ply);
	}
	
	public boolean showLeaderboard(CommandSender sender) {
		this.notReadyYet(sender);
		return true;
	}
	
	public boolean showOwnStats(Player ply) {
		final PlayerDataYaml pYml = new PlayerDataYaml(core, ply);
		final DecimalFormat f = new DecimalFormat("##.##");
		final String stats = String.format("§e#%s §8- §b%s §8(§a%s§7/§c%s §7- §6%s§7/§4%s§8) [§2%s%% §7- §6%s§8]",
				0, ply.getName(), (int) pYml.getWins(ply), (int) pYml.getLosses(ply), (int) pYml.getKills(ply), (int) pYml.getDeaths(ply), f.format(pYml.getWinPercentage(ply)), pYml.getKD(ply));
		
		ply.sendMessage(exp);
		this.drawBar(ply);
		ply.sendMessage(stats);
		return true;
	}
	
	public boolean showOtherStats(CommandSender sender, String usr) {
		try {
			final Player ply = Bukkit.getPlayer(usr);
			final PlayerDataYaml pYml = new PlayerDataYaml(core, ply);
			final DecimalFormat f = new DecimalFormat("##.##");
			final String stats = String.format("§e#%s §8- §b%s §8(§a%s§7/§c%s §7- §6%s§7/§4%s§8) [§2%s%% §7- §6%s§8]",
					0, ply.getName(), (int) pYml.getWins(ply), (int) pYml.getLosses(ply), (int) pYml.getKills(ply), (int) pYml.getDeaths(ply), f.format(pYml.getWinPercentage(ply)), f.format(pYml.getKD(ply)));
			
			sender.sendMessage(stats);
			return true;
		} catch (Exception e) {
			OfflinePlayer ply = null;
			
			for (OfflinePlayer p : Bukkit.getOfflinePlayers()) {
				if (p.getName().equalsIgnoreCase(usr)) {
					ply = p;
					break;
				}
			}
			
			final PlayerDataYaml pYml = new PlayerDataYaml(core, ply);
			final DecimalFormat f = new DecimalFormat("##.##");
			final String stats = String.format("§e#%s §8- §b%s §8(§a%s§7/§c%s §7- §6%s§7/§4%s§8) [§2%s%% §7- §6%s§8]",
					0, ply.getName(), (int) pYml.getWins(ply), (int) pYml.getLosses(ply), (int) pYml.getKills(ply), (int) pYml.getDeaths(ply), f.format(pYml.getWinPercentage(ply)), f.format(pYml.getKD(ply)));

			sender.sendMessage(stats);
			return true;
		}
	}
	
	public void banPlayer(final CommandSender sender, String usr) {
		try {
			final Player ply = Bukkit.getPlayer(usr);
			core.addToBlacklist(sender, ply.getUniqueId());
		} catch (Exception e) {
			OfflinePlayer ply = null;
			
			for (final OfflinePlayer p : Bukkit.getOfflinePlayers()) {
				if (p.getName().equalsIgnoreCase(usr)) {
					ply = p;
					break;
				}
			}
			
			core.addToBlacklist(sender, ply.getUniqueId());
		}
	}
	
	public void unbanPlayer(final CommandSender sender, String usr) {
		try {
			final Player ply = Bukkit.getPlayer(usr);
			core.removeFromBlacklist(sender, ply.getUniqueId());
		} catch (Exception e) {
			OfflinePlayer ply = null;
			
			for (final OfflinePlayer p : Bukkit.getOfflinePlayers()) {
				if (p.getName().equalsIgnoreCase(usr)) {
					ply = p;
					break;
				}
			}
				
			core.removeFromBlacklist(sender, ply.getUniqueId());
		}
	}
	
	public void whitelistPlayer(final CommandSender sender, String usr) {
		try {
			final Player ply = Bukkit.getPlayer(usr);
			core.addToWhitelist(sender, ply.getUniqueId());
		} catch (Exception e) {
			OfflinePlayer ply = null;
			
			for (final OfflinePlayer p : Bukkit.getOfflinePlayers()) {
				if (p.getName().equalsIgnoreCase(usr)) {
					ply = p;
					break;
				}
			}
			
			core.addToWhitelist(sender, ply.getUniqueId());
		}
	}
	
	public void unwhitelistPlayer(final CommandSender sender, String usr) {
		try {
			final Player ply = Bukkit.getPlayer(usr);
			core.removeFromWhitelist(sender, ply.getUniqueId());
		} catch (Exception e) {
			OfflinePlayer ply = null;
			
			for (final OfflinePlayer p : Bukkit.getOfflinePlayers()) {
				if (p.getName().equalsIgnoreCase(usr)) {
					ply = p;
					break;
				}
			}
				
			core.removeFromWhitelist(sender, ply.getUniqueId());
		}
	}
	
	public void debugCommand(final CommandSender sender) {
		final Player ply = (Player) sender;
		
		for (final Entity ent : ply.getNearbyEntities(4, 1, 4)) {
			if (ent instanceof Player) {
				final Player vic = (Player) ent;
			       
		        Location playerCenterLocation = ply.getEyeLocation();
		        Location playerToThrowLocation = vic.getEyeLocation();
		       
		        double x = playerToThrowLocation.getX() - playerCenterLocation.getX();
		        double y = playerToThrowLocation.getY() - playerCenterLocation.getY();
		        double z = playerToThrowLocation.getZ() - playerCenterLocation.getZ();
		       
		        Vector throwVector = new Vector(x, y, z);
		       
		        throwVector.normalize();
		        throwVector.multiply(1.5D);
		        throwVector.setY(1.0D);
		       
		        vic.setVelocity(throwVector);
				vic.playSound(((Player) sender).getLocation(), Sound.ENTITY_ZOMBIE_BREAK_DOOR_WOOD, 1F, 0F);
			}
		}
	}
	
	private void drawBar(final Player ply) {
		String bar = "§8";
		
		for (int i = 0; i < 51; i += 1)
			bar += "-";
		
		ply.sendMessage(bar);
	}
	
	private void notReadyYet(final CommandSender sender) {
		core.sendPluginMessage(sender, "§cThat feature isn't ready yet!");
		return;
	}
	
}
