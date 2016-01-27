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
	private ArrayList<Material> itemMaterials;
	private ArrayList<Byte> itemData;
	private ArrayList<Integer> itemAmounts;
	private ArrayList<String> itemDisplayNames;
	private HashMap<Integer, ArrayList<Integer>> itemEnchantId;
	private HashMap<Integer, ArrayList<Integer>> itemEnchantIdLevel;
	private HashMap<Integer, ArrayList<Integer>> enchantBookId;
	private HashMap<Integer, ArrayList<Integer>> enchantBookIdLevel;
	private ArrayList<Short> itemDurability;
	private ArrayList<Integer> itemPosition;

	// Sign lines
	private ArrayList<String> signLines = new ArrayList<String>();

	// Sign lines
	private String skullOwner = "";
	private BlockFace skullRotation = BlockFace.SELF;

	// optional stuff
	private ArrayList<Boolean> itemSplash;

	private ItemStack[] inv;

	boolean isDoubleChest = false;
	private DoubleChest doubleChest = null;

	public SmartArenaBlock(Block b, boolean c, boolean s) {
		this.m = b.getType();
		this.x = b.getX();
		this.y = b.getY();
		this.z = b.getZ();
		this.data = b.getData();
		this.world = b.getWorld().getName();
		if (this.m.equals(Material.SKULL)) {
			if (b.getState() instanceof Skull) {
				this.skullOwner = ((Skull) b.getState()).getOwner();
				this.skullRotation = ((Skull) b.getState()).getRotation();
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
				this.signLines.addAll(Arrays.asList(sign.getLines()));
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
		World w = Bukkit.getWorld(this.world);
		if (w == null)
			return null;
		Block b = w.getBlockAt(this.x, this.y, this.z);
		return b;
	}

	public Material getMaterial() {
		return this.m;
	}

	public Byte getData() {
		return this.data;
	}

	public void setData(byte data) {
		this.data = data;
	}

	public ItemStack[] getInventory() {
		return this.inv;
	}

	public HashMap<Integer, ItemStack> getNewInventory() {
		HashMap<Integer, ItemStack> ret = new HashMap<Integer, ItemStack>();
		for (int i = 0; i < this.itemMaterials.size(); i++) {
			ItemStack item = new ItemStack(this.itemMaterials.get(i), this.itemAmounts.get(i), this.itemData.get(i));
			item.setDurability(this.itemDurability.get(i));
			ItemMeta im = item.getItemMeta();
			im.setDisplayName(this.itemDisplayNames.get(i));

			if (this.itemEnchantId.size() > i && this.itemEnchantId.get(i) != null) {
				int count = 0;
				for (Integer ench : this.itemEnchantId.get(i)) {
					im.addEnchant(Enchantment.getById(ench), this.itemEnchantIdLevel.get(i).get(count), true);
					count++;
				}
			}

			item.setItemMeta(im);
			if (item.getType() == Material.POTION && item.getDurability() > 0) {
				Potion potion = Potion.fromDamage(item.getDurability() & 0x3F);
				if (this.itemSplash.size() > i) {
					potion.setSplash(this.itemSplash.get(i));
				}
			} else if (item.getType() == Material.ENCHANTED_BOOK) {
				ItemStack neww = new ItemStack(Material.ENCHANTED_BOOK);
				EnchantmentStorageMeta meta = (EnchantmentStorageMeta) neww.getItemMeta();
				int count = 0;
				if (this.enchantBookId.size() > i) {
					for (Integer ench : this.enchantBookId.get(i)) {
						try {
							meta.addStoredEnchant(Enchantment.getById(ench), this.enchantBookIdLevel.get(i).get(count), true);
						} catch (Exception e) {
							System.out.println("Failed applying enchantment to enchantment book at reset.");
						}
						count++;
					}
				}
				neww.setItemMeta(meta);
				item = neww;
			}
			int pos = i;
			if (i < this.itemPosition.size()) {
				pos = this.itemPosition.get(i);
			}
			ret.put(pos, item);
		}
		return ret;
	}

	public void setInventory(Inventory inventory) {
		this.inv = inventory.getContents();
		this.itemMaterials = new ArrayList<Material>();
		this.itemData = new ArrayList<Byte>();
		this.itemAmounts = new ArrayList<Integer>();
		this.itemDisplayNames = new ArrayList<String>();
		this.itemSplash = new ArrayList<Boolean>();
		this.itemPosition = new ArrayList<Integer>();
		this.itemEnchantId = new HashMap<Integer, ArrayList<Integer>>();
		this.itemEnchantIdLevel = new HashMap<Integer, ArrayList<Integer>>();
		this.enchantBookId = new HashMap<Integer, ArrayList<Integer>>();
		this.enchantBookIdLevel = new HashMap<Integer, ArrayList<Integer>>();
		this.itemDurability = new ArrayList<Short>();

		if (inventory.getHolder() instanceof DoubleChest) {
			isDoubleChest = true;
			doubleChest = (DoubleChest) inventory.getHolder();
		}

		int pos = 0;
		for (ItemStack i : inventory.getContents()) {
			if (i != null) {
				this.itemMaterials.add(i.getType());
				this.itemData.add(i.getData().getData());
				this.itemAmounts.add(i.getAmount());
				this.itemDisplayNames.add(i.getItemMeta().getDisplayName());
				this.itemDurability.add(i.getDurability());
				if (i.getType() == Material.POTION && i.getDurability() > 0) {
					Potion potion = Potion.fromDamage(i.getDurability() & 0x3F);
					this.itemSplash.add(potion.isSplash());
				} else if (i.getType() == Material.ENCHANTED_BOOK) {
					EnchantmentStorageMeta meta = (EnchantmentStorageMeta) i.getItemMeta();
					ArrayList<Integer> tempid = new ArrayList<Integer>();
					ArrayList<Integer> templv = new ArrayList<Integer>();
					for (Enchantment ench : meta.getStoredEnchants().keySet()) {
						tempid.add(ench.getId());
						templv.add(meta.getStoredEnchants().get(ench));
					}
					this.enchantBookId.put(pos, tempid);
					this.enchantBookIdLevel.put(pos, templv);
					this.itemSplash.add(false);
				} else {
					this.itemSplash.add(false);
				}
				this.itemPosition.add(pos);
				if (i.getItemMeta().getEnchants().size() > 0) {
					ArrayList<Integer> tempid = new ArrayList<Integer>();
					ArrayList<Integer> templv = new ArrayList<Integer>();
					for (Enchantment ench : i.getItemMeta().getEnchants().keySet()) {
						tempid.add(ench.getId());
						templv.add(i.getItemMeta().getEnchants().get(ench));
					}
					this.itemEnchantId.put(pos, tempid);
					this.itemEnchantIdLevel.put(pos, templv);
				} else {
					this.itemEnchantId.put(pos, new ArrayList<Integer>());
					this.itemEnchantIdLevel.put(pos, new ArrayList<Integer>());
				}
			}
			pos++;
		}
	}

	public ArrayList<String> getSignLines() {
		return this.signLines;
	}

	public boolean isDoubleChest() {
		return this.isDoubleChest;
	}

	public DoubleChest getDoubleChest() {
		return this.doubleChest;
	}

	public String getSkullOwner() {
		return this.skullOwner;
	}

	public BlockFace getSkullORotation() {
		return this.skullRotation;
	}
}