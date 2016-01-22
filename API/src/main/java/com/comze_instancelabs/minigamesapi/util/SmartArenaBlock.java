package com.comze_instancelabs.minigamesapi.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.Dispenser;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.Dropper;
import org.bukkit.block.Sign;
import org.bukkit.block.Skull;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.Potion;

public class SmartArenaBlock implements Serializable {
	private static final long serialVersionUID = -1894759842709524780L;

	private int x, y, z;
	private String world;
	private Material m;
	private byte data;
	private ArrayList<Material> item_mats;
	private ArrayList<Byte> item_data;
	private ArrayList<Integer> item_amounts;
	private ArrayList<String> item_displaynames;
	private HashMap<Integer, ArrayList<Integer>> item_enchid;
	private HashMap<Integer, ArrayList<Integer>> item_enchid_lv;
	private HashMap<Integer, ArrayList<Integer>> enchbook_id;
	private HashMap<Integer, ArrayList<Integer>> enchbook_id_lv;
	private ArrayList<Short> item_durability;
	private ArrayList<Integer> item_pos;

	// Sign lines
	private ArrayList<String> sign_lines = new ArrayList<String>();

	// Sign lines
	private String skull_owner = "";
	private BlockFace skull_rotation = BlockFace.SELF;

	// optional stuff
	private ArrayList<Boolean> item_splash;

	private ItemStack[] inv;

	boolean isDoubleChest = false;
	DoubleChest doubleChest = null;

	public SmartArenaBlock(Block b, boolean c, boolean s) {
		this.m = b.getType();
		this.x = b.getX();
		this.y = b.getY();
		this.z = b.getZ();
		this.data = b.getData();
		this.world = b.getWorld().getName();
		if (this.m.equals(Material.SKULL)) {
			if (b.getState() instanceof Skull) {
				this.skull_owner = ((Skull) b.getState()).getOwner();
				this.skull_rotation = ((Skull) b.getState()).getRotation();
			}
		}
		if (this.m.equals(Material.DROPPER)) {
			if (b.getState() instanceof Dropper) {
				setInventory(((Dropper) b.getState()).getInventory());
			}
		}
		if (this.m.equals(Material.DISPENSER)) {
			if (b.getState() instanceof Dispenser) {
				setInventory(((Dispenser) b.getState()).getInventory());
			}
		}
		if (s) {
			Sign sign = (Sign) b.getState();
			if (sign != null) {
				sign_lines.addAll(Arrays.asList(sign.getLines()));
			}
		} else if (c) {
			Chest chest = (Chest) b.getState();
			setInventory(chest.getInventory());
		}
	}

	public SmartArenaBlock(Location l, Material m, byte data) {
		this.m = m;
		this.x = l.getBlockX();
		this.y = l.getBlockY();
		this.z = l.getBlockZ();
		this.world = l.getWorld().getName();
		this.data = data;
	}

	public Block getBlock() {
		World w = Bukkit.getWorld(world);
		if (w == null)
			return null;
		Block b = w.getBlockAt(x, y, z);
		return b;
	}

	public Material getMaterial() {
		return m;
	}

	public Byte getData() {
		return data;
	}

	public void setData(byte data) {
		this.data = data;
	}

	public ItemStack[] getInventory() {
		return inv;
	}

	public HashMap<Integer, ItemStack> getNewInventory() {
		HashMap<Integer, ItemStack> ret = new HashMap<Integer, ItemStack>();
		for (int i = 0; i < item_mats.size(); i++) {
			ItemStack item = new ItemStack(item_mats.get(i), item_amounts.get(i), item_data.get(i));
			item.setDurability(item_durability.get(i));
			ItemMeta im = item.getItemMeta();
			im.setDisplayName(item_displaynames.get(i));

			if (item_enchid.size() > i && item_enchid.get(i) != null) {
				int c = 0;
				for (Integer ench : item_enchid.get(i)) {
					im.addEnchant(Enchantment.getById(ench), item_enchid_lv.get(i).get(c), true);
					c++;
				}
			}

			item.setItemMeta(im);
			if (item.getType() == Material.POTION && item.getDurability() > 0) {
				Potion potion = Potion.fromDamage(item.getDurability() & 0x3F);
				if (item_splash.size() > i) {
					potion.setSplash(item_splash.get(i));
				}
			} else if (item.getType() == Material.ENCHANTED_BOOK) {
				ItemStack neww = new ItemStack(Material.ENCHANTED_BOOK);
				EnchantmentStorageMeta meta = (EnchantmentStorageMeta) neww.getItemMeta();
				int c_ = 0;
				if (enchbook_id.size() > i) {
					for (Integer ench : enchbook_id.get(i)) {
						try {
							meta.addStoredEnchant(Enchantment.getById(ench), enchbook_id_lv.get(i).get(c_), true);
						} catch (Exception e) {
							System.out.println("Failed applying enchantment to enchantment book at reset.");
						}
						c_++;
					}
				}
				neww.setItemMeta(meta);
				item = neww;
			}
			int pos = i;
			if (i < item_pos.size()) {
				pos = item_pos.get(i);
			}
			ret.put(pos, item);
		}
		return ret;
	}

	public void setInventory(Inventory inventory) {
		inv = inventory.getContents();
		item_mats = new ArrayList<Material>();
		item_data = new ArrayList<Byte>();
		item_amounts = new ArrayList<Integer>();
		item_displaynames = new ArrayList<String>();
		item_splash = new ArrayList<Boolean>();
		item_pos = new ArrayList<Integer>();
		item_enchid = new HashMap<Integer, ArrayList<Integer>>();
		item_enchid_lv = new HashMap<Integer, ArrayList<Integer>>();
		enchbook_id = new HashMap<Integer, ArrayList<Integer>>();
		enchbook_id_lv = new HashMap<Integer, ArrayList<Integer>>();
		item_durability = new ArrayList<Short>();

		if (inventory.getHolder() instanceof DoubleChest) {
			isDoubleChest = true;
			doubleChest = (DoubleChest) inventory.getHolder();
		}

		int pos = 0;
		for (ItemStack i : inventory.getContents()) {
			if (i != null) {
				item_mats.add(i.getType());
				item_data.add(i.getData().getData());
				item_amounts.add(i.getAmount());
				item_displaynames.add(i.getItemMeta().getDisplayName());
				item_durability.add(i.getDurability());
				if (i.getType() == Material.POTION && i.getDurability() > 0) {
					Potion potion = Potion.fromDamage(i.getDurability() & 0x3F);
					item_splash.add(potion.isSplash());
				} else if (i.getType() == Material.ENCHANTED_BOOK) {
					EnchantmentStorageMeta meta = (EnchantmentStorageMeta) i.getItemMeta();
					ArrayList<Integer> tempid = new ArrayList<Integer>();
					ArrayList<Integer> templv = new ArrayList<Integer>();
					for (Enchantment ench : meta.getStoredEnchants().keySet()) {
						tempid.add(ench.getId());
						templv.add(meta.getStoredEnchants().get(ench));
					}
					enchbook_id.put(pos, tempid);
					enchbook_id_lv.put(pos, templv);
					item_splash.add(false);
				} else {
					item_splash.add(false);
				}
				item_pos.add(pos);
				if (i.getItemMeta().getEnchants().size() > 0) {
					ArrayList<Integer> tempid = new ArrayList<Integer>();
					ArrayList<Integer> templv = new ArrayList<Integer>();
					for (Enchantment ench : i.getItemMeta().getEnchants().keySet()) {
						tempid.add(ench.getId());
						templv.add(i.getItemMeta().getEnchants().get(ench));
					}
					item_enchid.put(pos, tempid);
					item_enchid_lv.put(pos, templv);
				} else {
					item_enchid.put(pos, new ArrayList<Integer>());
					item_enchid_lv.put(pos, new ArrayList<Integer>());
				}
			}
			pos++;
		}
	}

	public ArrayList<String> getSignLines() {
		return this.sign_lines;
	}

	public boolean isDoubleChest() {
		return isDoubleChest;
	}

	public DoubleChest getDoubleChest() {
		return doubleChest;
	}

	public String getSkullOwner() {
		return skull_owner;
	}

	public BlockFace getSkullORotation() {
		return skull_rotation;
	}

}