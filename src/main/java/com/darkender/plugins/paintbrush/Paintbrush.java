package com.darkender.plugins.paintbrush;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.*;

public class Paintbrush extends JavaPlugin implements Listener
{
    private static NamespacedKey paintbrushKey;
    private static List<String> colors;
    private static Map<String, List<Material>> cycles;
    
    @Override
    public void onEnable()
    {
        paintbrushKey = new NamespacedKey(this, "paintbrush");
        colors = Arrays.asList("white", "orange", "magenta", "light_blue", "yellow",
                "lime", "pink", "gray", "light_gray", "cyan", "purple", "blue", "brown",
                "green", "red", "black");
        cycles = new HashMap<>();
        for(Material material : Material.values())
        {
            String key = material.getKey().getKey();
            for(String color : colors)
            {
                if(key.startsWith(color + "_"))
                {
                    String name = key.substring(color.length() + 1);
                    if(!cycles.containsKey(name))
                    {
                        cycles.put(name, new ArrayList<>());
                    }
                    cycles.get(name).add(material);
                }
            }
        }
        
        Iterator<Map.Entry<String, List<Material>>> entryIterator = cycles.entrySet().iterator();
        while(entryIterator.hasNext())
        {
            Map.Entry<String, List<Material>> entry = entryIterator.next();
            if(entry.getValue().size() == 1)
            {
                entryIterator.remove();
                continue;
            }
            
            entry.getValue().sort((first, second) ->
            {
                String firstKey = first.getKey().getKey();
                String colorFirst = firstKey.substring(0, firstKey.length() - entry.getKey().length() - 1);
                String secondKey = second.getKey().getKey();
                String colorSecond = secondKey.substring(0, secondKey.length() - entry.getKey().length() - 1);
                
                return colors.indexOf(colorFirst) - colors.indexOf(colorSecond);
            });
        }
        
        PaintbrushCommand paintbrushCommand = new PaintbrushCommand();
        getCommand("paintbrush").setExecutor(paintbrushCommand);
        getCommand("paintbrush").setTabCompleter(paintbrushCommand);
        
        getServer().getPluginManager().registerEvents(this, this);
    }
    
    private static String getBaseName(String key)
    {
        for(String color : colors)
        {
            if(key.startsWith(color + "_"))
            {
                return key.substring(color.length() + 1);
            }
        }
        return null;
    }
    
    public static List<Material> getCycle(Material material)
    {
        String baseName = getBaseName(material.getKey().getKey());
        if(baseName == null || !cycles.containsKey(baseName))
        {
            return null;
        }
        return cycles.get(baseName);
    }
    
    public static boolean cycleBlock(Block block, int amount)
    {
        List<Material> cycle = getCycle(block.getType());
        if(cycle == null)
        {
            return false;
        }
        
        int newPos = cycle.indexOf(block.getType()) + amount;
        while(newPos >= cycle.size())
        {
            newPos -= cycle.size();
        }
        while(newPos < 0)
        {
            newPos += cycle.size();
        }
        block.setType(cycle.get(newPos));
        return true;
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
    
    public static boolean isPaintbrush(ItemStack item)
    {
        if(item == null || !item.hasItemMeta())
        {
            return false;
        }
        return item.getItemMeta().getPersistentDataContainer().has(paintbrushKey, PersistentDataType.BYTE);
    }
    
    private RayTraceResult raytraceFor(Player player)
    {
        Location rayStart = player.getEyeLocation();
        return rayStart.getWorld().rayTraceBlocks(rayStart, player.getEyeLocation().getDirection(),
                25, FluidCollisionMode.NEVER, true);
    }
    
    public static Location getHandScreenLocation(Location loc, boolean offhand)
    {
        Location spawnFrom = loc.clone();
        org.bukkit.util.Vector normal2D = spawnFrom.getDirection().clone().setY(0).normalize()
                .rotateAroundY((offhand ? 1 : -1) * (Math.PI / 2))
                .multiply(0.40).setY(-0.35);
        spawnFrom.add(normal2D);
        spawnFrom.add(loc.getDirection().clone().multiply(-0.3));
        return spawnFrom;
    }
    
    public static void displayParticles(Location from, Block to, boolean positive)
    {
        Location center = to.getLocation().add(0.5, 0.5, 0.5);
        double distance = from.distance(center);
        org.bukkit.util.Vector direction = center.toVector().subtract(from.toVector()).normalize();
        Vector step = direction.multiply(0.3);
        
        double distanceProgress = 0.0;
        Location current = from.clone();
        while(distanceProgress < distance)
        {
            current.getWorld().spawnParticle(Particle.REDSTONE, current, 0, new Particle.DustOptions(Color.GRAY, 0.5F));
            distanceProgress += 0.3;
            current.add(step);
        }
        
        for(int x = to.getX(); x <= to.getX() + 1; x++)
        {
            for(int y = to.getY(); y <= to.getY() + 1; y++)
            {
                for(int z = to.getZ(); z <= to.getZ() + 1; z++)
                {
                    to.getWorld().spawnParticle(Particle.REDSTONE,
                            new Location(to.getWorld(), x, y, z), 0,
                            new Particle.DustOptions(positive ? Color.LIME : Color.RED, 1.2F));
                }
            }
        }
    }
    
    private boolean cycleBlockFromPlayer(Player player, Block block, int amount, boolean offhand)
    {
        if(!cycleBlock(block, amount))
        {
            return false;
        }
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP,
                SoundCategory.MASTER, 1.0F, amount > 0 ? 1.0F : 0.6F);
        displayParticles(getHandScreenLocation(player.getEyeLocation(), offhand), block, amount > 0);
        return true;
    }
    
    @EventHandler
    private void onPlayerInteract(PlayerInteractEvent event)
    {
        if(isPaintbrush(event.getItem()) && event.getPlayer().hasPermission("paintbrush.use"))
        {
            event.setCancelled(true);
            int amount = (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) ? 1 : -1;
            if(event.getClickedBlock() != null)
            {
                cycleBlockFromPlayer(event.getPlayer(), event.getClickedBlock(), amount, event.getHand() == EquipmentSlot.OFF_HAND);
            }
            else
            {
                RayTraceResult trace = raytraceFor(event.getPlayer());
                if(trace != null && trace.getHitBlock() != null)
                {
                    cycleBlockFromPlayer(event.getPlayer(), trace.getHitBlock(), amount, event.getHand() == EquipmentSlot.OFF_HAND);
                }
            }
        }
    }
    
    @EventHandler(ignoreCancelled = true)
    public void onPlayerItemHeld(PlayerItemHeldEvent event)
    {
        if(event.getNewSlot() != event.getPreviousSlot() &&
                isPaintbrush(event.getPlayer().getInventory().getItem(event.getPreviousSlot())) &&
                event.getPlayer().hasPermission("paintbrush.use"))
        {
            RayTraceResult trace = raytraceFor(event.getPlayer());
            if(trace != null && trace.getHitBlock() != null)
            {
                int amount = event.getNewSlot() - event.getPreviousSlot();
                // Scrolling past bounds
                if((event.getPreviousSlot() == 0 && event.getNewSlot() > 4) || (event.getPreviousSlot() == 8 && event.getNewSlot() < 4))
                {
                    amount = amount > 0 ? -1 : 1;
                }
                if(cycleBlockFromPlayer(event.getPlayer(), trace.getHitBlock(), amount, false))
                {
                    event.setCancelled(true);
                }
            }
        }
    }
}
