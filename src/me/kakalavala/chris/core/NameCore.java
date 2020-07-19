package me.kakalavala.chris.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.kakalavala.chris.assets.PlayerDataYaml;
import me.kakalavala.chris.core.api.menu.MenuCreator;
import me.kakalavala.chris.core.api.menu.MenuCreator.ColourableColour;
import me.kakalavala.chris.core.api.menu.MenuCreator.InventorySize;
import me.kakalavala.chris.core.api.menu.PageRegistry;

public class NameCore {
	
	public String invName = "§4Stick Name Selector §8§l(§6§l%s§7§l/§c§l%s§8§l)";
	
	public ItemStack rareGlass;
	public ItemStack uncommonGlass;
	public ItemStack commonGlass;
	public ItemStack normalGlass;
	
	public ItemStack testerStick;
	public ItemStack resetBtn;
	public ItemStack prevBtn;
	public ItemStack nextBtn;
	public ItemStack curPage;
	public ItemStack closeBtn;
	public ItemStack footerItem;
	
	public final int TOTAL_PAGES = 1;
	
	private final Core core;
	
	public NameCore(final Core core) {
		this.core = core;
		
		this.rareGlass = new ItemStack(Material.STAINED_GLASS_PANE, 1, ColourableColour.RED.getData()); {
			final ItemMeta m = this.rareGlass.getItemMeta();
			
			m.setDisplayName("§cRare");
			
			this.rareGlass.setItemMeta(m);
		};
		
		this.uncommonGlass = new ItemStack(Material.STAINED_GLASS_PANE, 1, ColourableColour.MAGENTA.getData()); {
			final ItemMeta m = this.uncommonGlass.getItemMeta();
			
			m.setDisplayName("§dUncommon");
			
			this.uncommonGlass.setItemMeta(m);
		};
		
		this.commonGlass = new ItemStack(Material.STAINED_GLASS_PANE, 1, ColourableColour.GREEN.getData()); {
			final ItemMeta m = this.commonGlass.getItemMeta();
			
			m.setDisplayName("§aCommon");
			
			this.commonGlass.setItemMeta(m);
		};
		
		this.normalGlass = new ItemStack(Material.STAINED_GLASS_PANE, 1, ColourableColour.BLUE.getData()); {
			final ItemMeta m = this.normalGlass.getItemMeta();
			
			m.setDisplayName("§bNormal");
			
			this.normalGlass.setItemMeta(m);
		};
		
		this.testerStick = new ItemStack(Material.EMPTY_MAP, 1); {
			final ItemMeta m = this.testerStick.getItemMeta();
			
			m.setDisplayName(Names.THE_BUG_CRUSHER.getName());
			m.setLore(Arrays.asList("§7ID: " + Names.THE_BUG_CRUSHER.id));
			
			this.testerStick.setItemMeta(m);
		};
		
		this.resetBtn = new ItemStack(Material.NAME_TAG, 1); {
			final ItemMeta m = this.resetBtn.getItemMeta();
			
			m.setDisplayName("§c§lReset Selected Name");
			
			this.resetBtn.setItemMeta(m);
		};
		
		this.prevBtn = new ItemStack(Material.ARROW, 1); {
			final ItemMeta m = this.prevBtn.getItemMeta();
			
			m.setDisplayName("§4§l<< §c§lPrev Page");
			
			this.prevBtn.setItemMeta(m);
		};
		
		this.nextBtn = new ItemStack(Material.ARROW, 1); {
			final ItemMeta m = this.nextBtn.getItemMeta();
			
			m.setDisplayName("§a§lNext Page §2§l>>");
			
			this.nextBtn.setItemMeta(m);
		};
		
		this.closeBtn = new ItemStack(Material.BARRIER, 1); {
			final ItemMeta m = this.closeBtn.getItemMeta();
			
			m.setDisplayName("§4§lClose");
			
			this.closeBtn.setItemMeta(m);
		};
		
		this.footerItem = new ItemStack(Material.STAINED_GLASS_PANE, 1, ColourableColour.BLACK.getData()); {
			final ItemMeta m = this.footerItem.getItemMeta();
			
			m.setDisplayName("§7");
			
			this.footerItem.setItemMeta(m);
		};
	}
	
	public ItemStack getCurrentPageBook(final UUID uuid) {
		this.curPage = new ItemStack(Material.BOOK, 1); {
			final ItemMeta m = this.curPage.getItemMeta();
			
			m.setDisplayName("§9§lPage §6§l" + core.currentPageHolder.get(uuid) + "§8§l/§c§l" + this.TOTAL_PAGES);
			m.addEnchant(Enchantment.LUCK, 10, true);
			m.addItemFlags(ItemFlag.HIDE_ENCHANTS);
			
			this.curPage.setItemMeta(m);
		};
		
		return this.curPage;
	}
	
	public enum Names {
		THE_BUG_CRUSHER(-2, "Legendary", "§lThe Bug Crusher"),
		THE_BACKHAND(0, "Rare", "The Backhand"),
		TEACHERS_RULER(1, "Uncommon", "Teacher's Ruler"),
		THE_MAILBOX_SMASHER(2, "Common", "The Mailbox Smasher"),
		A_LIVING_STICKBUG(3, "Common", "A Living Stickbug"),
		PEGLEG(4, "Common", "Pegleg"),
		BACKSCRATCHER(5, "Normal", "Backscratcher"),
		LIVE_ACTION_ROLEPLAY_FOAM_STICK(6, "Normal", "Live-Action Roleplay Foam Stick"),
		THE_BROOMSTICK(7, "Normal", "The Broomstick"),
		BASEBALL_BAT(8, "Normal", "Baseball Bat"),
		THE_BAN_HAMMER(9, "Rare", "The Ban Hammer"),
		SW0RD(10, "Uncommon", "SwoOoO00OoOord"),
		VERY_EFFECTIVE_AXE(11, "Common", "\"Very Effective\" Axe"),
		FROZEN_CORN_DOG(12, "Common", "Frozen Corn Dog"),
		IMPORTANT_FOUNDATION_ROD(13, "Common", "Important Foundation Rod"),
		SELFIE_STICK(14, "Normal", "Selfie Stick"),
		PITCHFORK(15, "Normal", "Pitchfork"),
		POPSICLE(16, "Normal", "Popsicle"),
		WALKING_STICK(17, "Normal", "Walking Stick"),
		UNICORN_HORN(18, "Rare", "Unicorn Horn"),
		VIBRATING_STICK(19, "Uncommon", "Stick of Never-Ending Vibrations"),
		GRANDPAS_CANE(20, "Common", "Grandpa's Cane"),
		NIGHTSTICK(21, "Common", "Nightstick"),
		KNEE_BREAKER(22, "Common", "Knee-Breaker"),
		LEAD_PIPE(23, "Normal", "Lead Pipe"),
		SPORK(24, "Normal", "Spork"),
		FLASHLIGHT(25, "Normal", "Flashlight"),
		CROWBAR(26, "Normal", "Crowbar")
		;
		
		int id;
		String rarity;
		String name;
		
		Names(int id, String rarity, String name) {
			this.id = id;
			this.rarity = rarity;
			
			switch (rarity.toLowerCase()) {
				case "legendary":
					name = "§6" + name;
					break;
				case "rare":
					name = "§c" + name;
					break;
				case "uncommon":
					name = "§d" + name;
					break;
				case "common":
					name = "§a" + name;
					break;
				default:
					name = "§b" + name;
					break;
			}
			
			this.name = name;
		}
		
		public String getName() {
			return this.name;
		}
		
		public int getId() {
			return this.id;
		}
		
		public String getRarity() {
			return this.rarity;
		}
		
	}
	
	public List<Names> getNames(String rarity) {
		final List<Names> names = new ArrayList<Names>();
		
		for (final Names n : Names.values()) {
			if (n.getRarity().equalsIgnoreCase(rarity))
				names.add(n);
		}
		
		return names;
	}
	
	public Inventory getNewNameInventory(final Player ply) {
		final Map<Integer, ItemStack> hdr = new HashMap<Integer, ItemStack>();
		final Map<Integer, ItemStack> ftrBtns = new HashMap<Integer, ItemStack>();
		
		final UUID uuid = ply.getUniqueId();
		
		final ItemStack[] hdrItems = {
			core.nameCore.rareGlass,
			core.nameCore.uncommonGlass,
			core.nameCore.commonGlass,
			core.nameCore.commonGlass,
			core.nameCore.commonGlass,
			core.nameCore.normalGlass,
			core.nameCore.normalGlass,
			core.nameCore.normalGlass,
			core.nameCore.normalGlass
		};
		
		final PlayerDataYaml pYml = new PlayerDataYaml(core, ply);
		int max = Names.values().length - 1;
		
		if (pYml.getWonNames(ply).contains(-2)) {
			max += 1;
			ftrBtns.put(46, core.nameCore.testerStick);
		}
		
		for (int i = 0; i < hdrItems.length; i += 1)
			hdr.put(i, hdrItems[i]);
		
		ftrBtns.put(45, core.nameCore.resetBtn);
		
		// if player doesnt have a current page, or if the current page is 0 or 1
		// set the page to the first page
		if (!core.currentPageHolder.containsKey(uuid) || core.currentPageHolder.get(uuid) == 0)
			core.currentPageHolder.put(uuid, 1);
		
		if (this.TOTAL_PAGES > 1) {
			// are they on the last page?
			if (core.currentPageHolder.get(uuid) == this.TOTAL_PAGES) {
				// add the previous button, but not the next button
				ftrBtns.put(48, this.prevBtn);
			} else if (core.currentPageHolder.get(uuid) == 1) {
				// they're on the first page
				ftrBtns.put(50, this.nextBtn);
			} else {
				// they're not on the last page or the first page
				// so add both the previous and the next
				ftrBtns.put(48, this.prevBtn);
				ftrBtns.put(50, this.nextBtn);
			}
			
			ftrBtns.put(49, this.getCurrentPageBook(uuid));
		}
		
		ftrBtns.put(53, this.closeBtn);
		
		final Map<Integer, ItemStack> names = new HashMap<Integer, ItemStack>();
		
		for (final int nId : pYml.getWonNames(ply)) {
			for (int i = 1; i < Names.values().length; i += 1) {
				if (Names.values()[i].getId() == nId && nId >= 0) {
				
					final ItemStack it = new ItemStack(Material.PAPER, 1); {
						final ItemMeta m = it.getItemMeta();
						
						if (nId == pYml.getActiveName(ply)) {
							m.addEnchant(Enchantment.LUCK, 10, true);
							m.addItemFlags(ItemFlag.HIDE_ENCHANTS);
						}
						
						m.setDisplayName(Names.values()[i].getName());
						m.setLore(Arrays.asList("§7ID: " + Names.values()[i].getId()));
						
						it.setItemMeta(m);
					};
					
					names.put(9 + (i - 1), it);
				}
			}
		}
		
		if (pYml.getActiveName(ply) == -2) {
			final ItemStack mp = new ItemStack(Material.EMPTY_MAP, 1); {
				final ItemMeta m = mp.getItemMeta();
				
				m.setDisplayName(this.testerStick.getItemMeta().getDisplayName());
				m.setLore(this.testerStick.getItemMeta().getLore());
				m.addEnchant(Enchantment.LUCK, 10, true);
				m.addItemFlags(ItemFlag.HIDE_ENCHANTS);
				
				mp.setItemMeta(m);
			};
			
			ftrBtns.put(46, mp);
		}
		
		final MenuCreator page1 = new MenuCreator(core, InventorySize.VERY_LARGE, String.format(this.invName, pYml.getWonNames(ply).size(), max), this.TOTAL_PAGES, hdr, ftrBtns, this.footerItem, names);
		
		final PageRegistry pr = new PageRegistry(core);
		
		pr.registerPage(1, page1.getInventory());
		
		return pr.getPage(core.currentPageHolder.get(uuid));
	}
	 
	public Names getRandomName() {
		final double rd = Math.floor(Math.random() * 100);
		
		if (rd >= 95)
			return this.getNames("rare").get(core.rn.nextInt(this.getNames("rare").size()));
		else if (rd >= 85)
			return this.getNames("uncommon").get(core.rn.nextInt(this.getNames("uncommon").size()));
		else if (rd >= 75)
			return this.getNames("common").get(core.rn.nextInt(this.getNames("common").size()));
		else if (rd >= 60)
			return this.getNames("normal").get(core.rn.nextInt(this.getNames("normal").size()));
		else return null;
	}
}
