package me.kakalavala.chris.core.api.menu;

import org.bukkit.inventory.Inventory;

import me.kakalavala.chris.core.Core;

public class PageRegistry {
	
	private Core core;
	
	public PageRegistry(Core core) {
		this.core = core;
	}
	
	public void registerPage(final int page, final Inventory inv) {
		core.registeredPages.put(page, inv);
	}
	
	public Inventory getPage(final int page) {
		return core.registeredPages.get(page);
	}
	
	public int getPageFromInventory(final Inventory inv) {
		int page = -1;
		
		for (final int pg : core.registeredPages.keySet()) {
			if (core.registeredPages.get(pg) == inv)
				page = pg;
		}
		
		return page;
	}

}
