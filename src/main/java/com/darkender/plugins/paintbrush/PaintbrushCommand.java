package com.darkender.plugins.paintbrush;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class PaintbrushCommand implements CommandExecutor, TabCompleter
{
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        if(!(sender instanceof Player))
        {
            sender.sendMessage(ChatColor.RED + "This command can only be used by a player");
            return true;
        }
        
        Player player = (Player) sender;
        if(!player.hasPermission("paintbrush.get"))
        {
            player.sendMessage(ChatColor.RED + "You do not have permission to get a paintbrush");
            return true;
        }
        
        player.getInventory().addItem(Paintbrush.generatePaintbrush());
        player.sendMessage(ChatColor.GREEN + "Got a paintbrush!");
        return true;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args)
    {
        return Collections.emptyList();
    }
}
