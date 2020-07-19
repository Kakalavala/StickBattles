package me.kakalavala.chris.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import me.kakalavala.chris.core.Core.Prefixes;

public class AbilityCore {
	
	private Core core;
	
	public final long COOLDOWN_TIME = 10000; // in milliseconds
	
	public final Map<UUID, Long> cooldown = new HashMap<UUID, Long>(); // USER, TIME UNTIL THEY CAN USE IT AGAIN
	public final List<UUID> wasTold = new ArrayList<UUID>();
	
	public AbilityCore(Core core) {
		this.core = core;
	}
	
	private void sendCooldownMessage(Player ply) {
		ply.sendMessage(String.format("%s §c§lYou cannot use that yet!", Prefixes.NOTICE.getPrefix()));
		ply.playSound(ply.getLocation(), Sound.BLOCK_ANVIL_PLACE, SoundCategory.PLAYERS, 6F, 0F);
	}
	
	public void newRecovery(Player ply) {
		final UUID uuid = ply.getUniqueId();
		
		if (core.frozen.contains(uuid)) {
			this.sendCooldownMessage(ply);
			return;
		} else {
			long cd = 0;
			
			if (cooldown.containsKey(ply.getUniqueId()))
				cd = cooldown.get(ply.getUniqueId());
			
			if (cd == 0) {
				wasTold.remove(uuid);
				cooldown.put(uuid, this.COOLDOWN_TIME);
				
				ply.setLevel(0);
				ply.setExp(0F);
				
				final double xDir = Math.round(ply.getLocation().getDirection().getX());
				double yDir = Math.round(ply.getLocation().getDirection().getY());
				final double zDir = Math.round(ply.getLocation().getDirection().getZ());
				
				if (yDir == 0)
					yDir = 0.1;
				
				//ply.sendMessage(String.format("§8[§c%s§7 / §a%s §7/ §9%s§8]", xDir, yDir, zDir));
				
				ply.setVelocity(new Vector((xDir * 10), (yDir * 10), (zDir * 10)));
				ply.playSound(ply.getLocation(), Sound.ENTITY_FIREWORK_LAUNCH, 6F, 1F);
				
				this.playSmokeTrail(ply);
			} else {
				this.sendCooldownMessage(ply);
				return;
			}
		}
	}

	/**
	 * @deprecated Use newRecovery(Player ply);
	 */
	public void recovery(Player ply) {
		if (core.frozen.contains(ply.getUniqueId())) {
			this.sendCooldownMessage(ply);
			return;
		} else {
			long cd = 0;
			
			if (cooldown.containsKey(ply.getUniqueId()))
				cd = cooldown.get(ply.getUniqueId());
				
			if (cd == 0) {
				wasTold.remove(ply.getUniqueId());
				cooldown.remove(ply.getUniqueId());
				cooldown.put(ply.getUniqueId(), this.COOLDOWN_TIME);
				
				ply.setLevel(0);
				ply.setExp(0F);
				
				final Location loc = ply.getLocation();
				
				final int[] m1 = core.getListFromLocation(core.getArenaBounds()[1]);
				final int[] m2 = core.getListFromLocation(core.getArenaBounds()[0]);
				
				final int x = loc.getBlockX();
				final int z = loc.getBlockZ();
				
				int nX = 0;
				int nZ = 0;
				
				final int diffX_m1 = Math.abs(m1[0] - x);
				final int diffZ_m1 = Math.abs(m1[2] - z);
				final int diffX_m2 = Math.abs(m2[0] - x);
				final int diffZ_m2 = Math.abs(m2[2] - z);
				
				if (diffX_m1 != diffX_m2)
					nX = (diffX_m1 > diffX_m2) ? -x : x;
				else nX = 0;
				if (diffZ_m1 != diffZ_m2)
					nZ = (diffZ_m1 > diffZ_m2) ? z : -z;
				else nZ = 0;
				
				ply.setVelocity(new Vector(nX, 10, nZ));
				ply.playSound(ply.getLocation(), Sound.ENTITY_FIREWORK_LAUNCH, 6F, 1F);
				this.playSmokeTrail(ply);
			} else {
				this.sendCooldownMessage(ply);
				return;
			}
		}
	}
	
	private void playSmokeTrail(Player ply) {
		final Location loc = ply.getLocation();
		ply.getWorld().playEffect(loc, Effect.SMOKE, 10);
	}

}
