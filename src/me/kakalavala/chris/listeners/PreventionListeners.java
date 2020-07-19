package me.kakalavala.chris.listeners;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;

import me.kakalavala.chris.core.Core;

public class PreventionListeners implements Listener {
	
	private Core core;
	
	public PreventionListeners(Core core) {
		this.core = core;
	}
	
	/**
	 * Prevent the player from swapping items to their offhand
	 */
	@EventHandler(priority = EventPriority.HIGHEST)
	public void stopInventoryMoving(final PlayerSwapHandItemsEvent e) {
		final Player ply = e.getPlayer();
		final UUID uuid = ply.getUniqueId();
		
		if (core.currentlyPlaying.contains(uuid)) {
			e.setCancelled(true);
			return;
		} else return;
	}
	
	/**
	 * Stop player from moving items in their inventory
	 */
	@EventHandler(priority = EventPriority.HIGHEST)
	public void stopInventoryInteraction(final InventoryClickEvent e) {
		final Player ply = (Player) e.getWhoClicked();
			
		try {
			if (core.queue.contains(ply.getUniqueId()) || core.currentlyPlaying.contains(ply.getUniqueId())) {
				e.setResult(Result.DENY);
				e.setCancelled(true);
			} else return;
		} catch (Exception exc) {}
	}
	
	/**
	 * Prevent players from dropping their items.
	 */
	@EventHandler(priority = EventPriority.HIGHEST)
	public void stopDroppingItems(final PlayerDropItemEvent e) {
		final Player ply = e.getPlayer();
		
		if (core.queue.contains(ply.getUniqueId()) || core.currentlyPlaying.contains(ply.getUniqueId())) {
			e.setCancelled(true);
		} else return;
	}
	
	/**
	 * Prevent players from breaking or building
	 */
	@EventHandler(priority = EventPriority.HIGHEST)
	public void preventBuildOrBreak(final PlayerInteractEvent e) {
		final Player ply = e.getPlayer();
		
		if (core.queue.contains(ply.getUniqueId()) || core.currentlyPlaying.contains(ply.getUniqueId())) {
			if (e.getAction().equals(Action.RIGHT_CLICK_BLOCK) || e.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
				e.setCancelled(true);
				return;
			} else return;
		} else return;
	}
	
	/**
	 * Basically just "naturalRegeneration" = false
	 */
	@EventHandler(priority = EventPriority.HIGH)
	public void onRegen(final EntityRegainHealthEvent e) {
		if (e.getEntity() instanceof Player) {
			final Player ply = (Player) e.getEntity();
			
			if (core.currentlyPlaying.contains(ply.getUniqueId())) {
				e.setCancelled(true);
				return;
			} else return;
		} else return;
	}
	
	/**
	 * Remove fall damage from the game
	 */
	@EventHandler(priority = EventPriority.HIGH)
	public void stopFallDamage(final EntityDamageEvent e) {
		if (e.getEntity() instanceof Player) {
			final Player ply = (Player) e.getEntity();
			final DamageCause cause = e.getCause();
			
			// is the player playing?
			if (core.currentlyPlaying.contains(ply.getUniqueId())) {
				// is it fall damage?
				if (cause.equals(DamageCause.FALL)) {
					e.setCancelled(true);
					return;
				} else return;
			} else if (core.queue.contains(ply.getUniqueId())) {
				e.setCancelled(true);
				return;
			} else return;
		} else return;
	}
	
	/**
	 * Basically just "doMobSpawning" = false
	 * But only in the arena bounds
	 */
	@EventHandler(priority = EventPriority.HIGHEST)
	public void stopMobSpawning(final EntitySpawnEvent e) {
		final Entity ent = e.getEntity();
		
		final List<EntityType> allowed = Arrays.asList(
				EntityType.PLAYER,
				EntityType.DROPPED_ITEM,
				EntityType.AREA_EFFECT_CLOUD,
				EntityType.LINGERING_POTION,
				EntityType.SPLASH_POTION,
				EntityType.ARMOR_STAND
		);
		
		if (ent.getWorld().getName().equals(core.getGameWorldName()) && !allowed.contains(ent.getType())) {
			e.setCancelled(true);
			return;
		}
		
	}

}
