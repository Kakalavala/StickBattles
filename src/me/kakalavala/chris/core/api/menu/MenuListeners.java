package me.kakalavala.chris.core.api.menu;

import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import me.kakalavala.chris.assets.PlayerDataYaml;
import me.kakalavala.chris.core.Core;
import me.kakalavala.chris.core.Core.Prefixes;
import me.kakalavala.chris.core.NameCore.Names;

public class MenuListeners implements Listener {
	
	private Core core;
	
	public MenuListeners(Core core) {
		this.core = core;
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void stopInventoryInteraction(final InventoryClickEvent e) {
		final Player ply = (Player) e.getWhoClicked();
		final UUID uuid = ply.getUniqueId();
		final Inventory inv = e.getInventory();
		final PlayerDataYaml pYml = new PlayerDataYaml(core, ply);
		
		try {
			if (inv.getTitle().equals(core.nameCore.getNewNameInventory(ply).getTitle())) {
				final ItemStack clicked = e.getCurrentItem();
				
				if (clicked.getType().equals(Material.PAPER) || clicked.getType().equals(Material.EMPTY_MAP)) {
					core.currentPageHolder.remove(uuid);
					
					for (final Names n : Names.values()) {
						if (n.getName().equalsIgnoreCase(clicked.getItemMeta().getDisplayName())){
							if (!pYml.getWonNames(ply).contains(n.getId())) {
								ply.playSound(ply.getLocation(), Sound.BLOCK_ANVIL_PLACE, 1F, 1F);
								core.sendPluginMessage(ply, "§cYou haven't unlocked that stick name yet!");
								e.setResult(Result.DENY);
								e.setCancelled(true);
								return;
							}
							
							pYml.setActiveName(n.getId());
							ply.playSound(ply.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 5F, 1F);
							ply.closeInventory();
							ply.sendMessage(String.format("%s §7Now using \"%s§7\"", Prefixes.ITEM.getPrefix(), n.getName()));
							
							if (core.currentlyPlaying.contains(ply.getUniqueId())) {
								if (ply.getInventory().getItem(0).getType().equals(Material.STICK))
									ply.getInventory().setItem(0, core.itemCore.getRenamedStick(n.getName()));
							}
							
							break;
						}
					}
				} else {
					switch (clicked.getType()) {
						case BARRIER:
							core.currentPageHolder.remove(uuid);
							ply.closeInventory();
							break;
						case NAME_TAG:
							core.currentPageHolder.remove(uuid);
							
							pYml.setActiveName(-1);
							ply.playSound(ply.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 5F, 1F);
							ply.sendMessage(String.format("%s §cStick Name was reset.", Prefixes.ITEM.getPrefix()));
							
							if (core.currentlyPlaying.contains(ply.getUniqueId())) {
								if (ply.getInventory().getItem(0).getType().equals(Material.STICK))
									ply.getInventory().setItem(0, core.itemCore.getRenamedStick("§bBasic Stick"));
							}
							
							ply.closeInventory();
							break;
						case ARROW:
							if (clicked.getItemMeta().getDisplayName().contains("Prev Page")) {
								core.currentPageHolder.put(uuid, core.currentPageHolder.get(uuid) - 1);
								ply.closeInventory();
								ply.openInventory(core.nameCore.getNewNameInventory(ply));
							} else {
								core.currentPageHolder.put(uuid, core.currentPageHolder.get(uuid) + 1);
								ply.closeInventory();
								ply.openInventory(core.nameCore.getNewNameInventory(ply));
							}
							
							break;
						default:
							break;
					}
				}
				
				e.setResult(Result.DENY);
				e.setCancelled(true);
			}
		} catch (Exception exc) {}
	}

}
