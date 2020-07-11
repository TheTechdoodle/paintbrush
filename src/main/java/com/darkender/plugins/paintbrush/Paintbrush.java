package com.darkender.plugins.paintbrush;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;

public class Paintbrush extends JavaPlugin implements Listener
{
    private static NamespacedKey paintbrushKey;
    
    @Override
    public void onEnable()
    {
        paintbrushKey = new NamespacedKey(this, "paintbrush");
        
        PaintbrushCommand paintbrushCommand = new PaintbrushCommand();
        getCommand("paintbrush").setExecutor(paintbrushCommand);
        getCommand("paintbrush").setTabCompleter(paintbrushCommand);
    }
    
    public static ItemStack generatePaintbrush()
    {
        ItemStack paintbrush = new ItemStack(Material.PAPER, 1);
        ItemMeta meta = paintbrush.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + "Paintbrush");
        meta.addEnchant(Enchantment.DURABILITY, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        meta.setLore(Arrays.asList(
                ChatColor.DARK_AQUA + "Right Click",
                ChatColor.BLUE + " \u2022 Cycle color forwards",
                ChatColor.DARK_AQUA + "Left Click",
                ChatColor.BLUE + " \u2022 Cycle color backwards",
                ChatColor.DARK_AQUA + "Scroll Wheel",
                ChatColor.BLUE + " \u2022 Cycle color forwards/backwards"
        ));
        meta.getPersistentDataContainer().set(paintbrushKey, PersistentDataType.BYTE, (byte) 1);
        paintbrush.setItemMeta(meta);
        return paintbrush;
    }
}
