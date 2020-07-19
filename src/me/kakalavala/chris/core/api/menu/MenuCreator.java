package me.kakalavala.chris.core.api.menu;

import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import me.kakalavala.chris.core.Core;

public class MenuCreator {
	
	// <Slot #, ItemStack>
	private Map<Integer, ItemStack> header;
	private Map<Integer, ItemStack> footerBtns;
	
	private Inventory menu;
	private InventorySize size;
	private ItemStack footerItem;
	
	private int totalPages;
	
	private Core core;

	public enum InventorySize {
		SMALL(9),
		MEDIUM(18),
		LARGE(27),
		VERY_LARGE(54)
		;
		
		int size;
		
		InventorySize(int size) {
			this.size = size;
		}
		
		public int getSize() {
			return this.size;
		}
	}
	
	public enum ColourableColour {
		WHITE(0), ORANGE(1), MAGENTA(2),
		BLUE(3), YELLOW(4), GREEN(5), PINK(6),
		DARK_GREY(7), LIGHT_GREY(8), CYAN(9),
		PURPLE(10), DARK_BLUE(11), BROWN(12),
		DARK_GREEN(13), RED(14), BLACK(15);
		
		byte data;
		
		ColourableColour(int data) {
			this.data = (byte) data;
		}
		
		public byte getData() {
			return this.data;
		}
	}
	
	public MenuCreator(Core core, InventorySize size, String title, int totalPages, Map<Integer, ItemStack> header, Map<Integer, ItemStack> footerBtns, ItemStack footerItem, Map<Integer, ItemStack> items) {
		this.core = core;
		
		this.menu = Bukkit.createInventory(null, size.getSize(), title);
		
		this.header = header;
		this.footerBtns = footerBtns;
		this.size = size;
		this.footerItem = footerItem;
		this.totalPages = totalPages;
		
		this.addHeaderItems();
		
		for (final int slot : items.keySet())
			this.menu.setItem(slot, items.get(slot));
		
		this.addFooter();
	}
	
	public Inventory getInventory() {
		return this.menu;
	}
	
	public int getTotalPages() {
		return this.totalPages;
	}
	
	public void setCurrentPage(final UUID uuid, int page) {
		if (page <= this.getTotalPages())
			core.currentPageHolder.put(uuid, page);
		else core.currentPageHolder.put(uuid, this.getTotalPages());
	}
	
	public int getCurrentPage(final UUID uuid) {
		if (core.currentPageHolder.containsKey(uuid))
			return core.currentPageHolder.get(uuid);
		else return 0;
	}
	
	private void addHeaderItems() {
		if (this.size != InventorySize.SMALL) {
			for (final int slot : this.header.keySet())
				this.menu.setItem(slot, this.header.get(slot));
		}
	}
	
	private void addFooter() {
		if (this.size != InventorySize.SMALL) {
			for (final int slot : this.footerBtns.keySet())
				this.menu.setItem(slot, this.footerBtns.get(slot));
			
			for (int i = this.size.getSize() - 9; i < this.size.getSize(); i += 1) {
				if (this.menu.getItem(i) == null)
					this.menu.setItem(i, this.footerItem);
			}
		} 
	}

}
