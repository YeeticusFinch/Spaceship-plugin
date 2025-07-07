package com.lerdorf.spaceships;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class Travel implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /travel <planetname>");
            return true;
        }

        String planetName = args[0];
        if (CosmicObject.destinations.containsKey(planetName)) {
            Location destination = CosmicObject.destinations.get(planetName).getLocation(Bukkit.getWorld("system"));
            player.teleport(destination);
            player.sendMessage(ChatColor.GREEN + "Traveling to " + ChatColor.GOLD + planetName + ChatColor.GREEN + "...");
        } else {
            sender.sendMessage(ChatColor.RED + "Unknown destination: " + planetName);
        }

        return true;
    }
}
