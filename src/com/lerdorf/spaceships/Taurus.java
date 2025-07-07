package com.lerdorf.spaceships;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Taurus implements CommandExecutor {

	public static Player player;
	public static String worldEdit;
	public static int radius;
	public static float angleStep;
	public static Location centerLoc;
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		// TODO Auto-generated method stub
		
		if (args.length > 1) {
			int rad = Integer.parseInt(args[0]);
			float step = Integer.parseInt(args[1]);
			String we = "";
			for (int i = 2; i < args.length; i++) {
				we += args[i] + " ";
			}
			if (sender instanceof Player p) {
				this.player = p;
				this.angleStep = (float)(180/Math.PI) * step/((float)rad);
				worldEdit = we;
				radius = rad;
				centerLoc = p.getLocation();
				centerLoc.setYaw(0);
				centerLoc.setPitch(0);
				MainPlugin.taurus = 360;
			}
			
		}
		
		return false;
		
	}
	
}
