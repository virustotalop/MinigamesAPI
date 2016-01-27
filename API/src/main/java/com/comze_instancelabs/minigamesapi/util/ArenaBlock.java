package com.comze_instancelabs.minigamesapi.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.Potion;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;

public class ArenaBlock implements Serializable {
    private static final long serialVersionUID = -1894759842709524780L;

    private int x, y, z;
    private String world;
    private Material m;
    private byte data;
    private ArrayList<Material> itemMaterial;
    private ArrayList<Byte> itemData;
    private ArrayList<Integer> itemAmounts;
    private ArrayList<String> itemDisplayNames;

    // optional stuff
    private ArrayList<Boolean> itemSplash;

    private ItemStack[] inv;

    public ArenaBlock(Block b, boolean c) {
    	this.m = b.getType();
    	this.x = b.getX();
        this.y = b.getY();
        this.z = b.getZ();
        this.data = b.getData();
        this.world = b.getWorld().getName();
        if (c) {
        	this.inv = ((Chest) b.getState()).getInventory().getContents();
            this.itemMaterial = new ArrayList<Material>();
            this.itemData = new ArrayList<Byte>();
            this.itemAmounts = new ArrayList<Integer>();
            this.itemDisplayNames = new ArrayList<String>();
            this.itemSplash = new ArrayList<Boolean>();

            for (ItemStack i : ((Chest) b.getState()).getInventory().getContents()) {
                if (i != null) {
                	this.itemMaterial.add(i.getType());
                    this.itemData.add(i.getData().getData());
                    this.itemAmounts.add(i.getAmount());
                    this.itemDisplayNames.add(i.getItemMeta().getDisplayName());
                    if (i.getType() == Material.POTION && i.getDurability() > 0 && i.getData().getData() > 0) {
                        Potion potion = Potion.fromDamage(i.getDurability() & 0x3F);
                        this.itemSplash.add(potion.isSplash());
                    } else {
                    	this.itemSplash.add(false);
                    }
                }
            }
        }
    }

    public ArenaBlock(Location l) {
    	this.m = Material.AIR;
    	this.x = l.getBlockX();
    	this.y = l.getBlockY();
    	this.z = l.getBlockZ();
    	this.world = l.getWorld().getName();
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

    public ItemStack[] getInventory() {
        return this.inv;
    }

    public ArrayList<ItemStack> getNewInventory() {
        int c = 0;
        ArrayList<ItemStack> ret = new ArrayList<ItemStack>();
        for (int i = 0; i < this.itemMaterial.size(); i++) {
            ItemStack item = new ItemStack(itemMaterial.get(i), this.itemAmounts.get(i), this.itemData.get(i));
            ItemMeta im = item.getItemMeta();
            im.setDisplayName(this.itemDisplayNames.get(i));
            item.setItemMeta(im);
            if (item.getType() == Material.POTION && item.getDurability() > 0) {
                Potion potion = Potion.fromDamage(item.getDurability() & 0x3F);
                potion.setSplash(this.itemSplash.get(i));
                item = potion.toItemStack(this.itemAmounts.get(i));
            }
            ret.add(item);
        }
        return ret;
    }

    public static ItemStack getEnchantmentBook(Map<Enchantment, Integer> t) {
        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK, 1);
        ItemMeta meta = book.getItemMeta();
        int i = 0;
        for (Enchantment e : t.keySet()) {
            meta.addEnchant(e, t.get(e), true);
        }
        book.setItemMeta(meta);
        return book;
    }

}