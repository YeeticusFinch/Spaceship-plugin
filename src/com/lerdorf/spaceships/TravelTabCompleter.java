package com.lerdorf.spaceships;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TravelTabCompleter implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> suggestions = new ArrayList<>();
            for (String planet : CosmicObject.destinations.keySet()) {
                if (planet.toLowerCase().startsWith(args[0].toLowerCase())) {
                    suggestions.add(planet);
                }
            }
            return suggestions;
        }
        return new ArrayList<>();
    }
}

