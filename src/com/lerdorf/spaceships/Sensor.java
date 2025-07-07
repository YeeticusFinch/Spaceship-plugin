package com.lerdorf.spaceships;

import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

import net.md_5.bungee.api.ChatColor;

public class Sensor  implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		// TODO Auto-generated method stub
		
		if (args.length > 1) {
			if (args[0].equalsIgnoreCase("create")) {
				try {
					int type = Integer.parseInt(args[1]);
					if (sender instanceof Player p) {
						Block block = p.getTargetBlock((Set) null, 5);
						if (block != null) {
							
							create(block, type, p.getEyeLocation());
							//if (top) turret.addScoreboardTag("top_turret");
							//else turret.addScoreboardTag("bottom_turret");
						}
					} else {
						sender.sendMessage(ChatColor.RED + "Only players can execute this command");
					}
				} catch (Exception e) {
					sender.sendMessage(ChatColor.RED + "Transceiver type must be an integer");
				}
			}
		}
		
		return false;
	}
	
	
	public static ArmorStand create(Block block, int type, Location placeLoc) {
		boolean top = true;
		if (block.getLocation().getY() > placeLoc.getY()) {
			top = false;
		}
		boolean small = isSmall(type);
		ArmorStand transceiver = (ArmorStand)block.getWorld().spawnEntity(block.getLocation().add(0.5, top ? (small ? 0.4 : -0.23) : (small ? -1.2 : -2), 0.5), EntityType.ARMOR_STAND);
		transceiver.setGravity(false);
		transceiver.setVisible(false);
		transceiver.setMarker(true);
		ItemStack head = new ItemStack(Material.SHULKER_SHELL);
		ItemMeta meta = head.getItemMeta();
		meta.setCustomModelData(33);
		head.setItemMeta(meta);
		transceiver.getEquipment().setHelmet(head);
		transceiver.addScoreboardTag("sensor");
		transceiver.addScoreboardTag("sensor_"+type);
		transceiver.setCustomName("sensor_"+type);
		Location loc = transceiver.getLocation();
		Location temp = loc.clone().setDirection(placeLoc.clone().subtract(loc).toVector());
		loc.setYaw((int)Math.round(temp.getYaw() /90) * 90);
		//p.sendMessage("Yaw: "+ (int)Math.round(temp.getYaw() /90) * 90);
		transceiver.teleport(loc);
		transceiver.addScoreboardTag("attached");
		MainPlugin.attached.add(transceiver);
		Vector offset = block.getLocation().add(0.5,0.5,0.5).subtract(transceiver.getLocation()).toVector();
		transceiver.addScoreboardTag("attach_to:" + offset.getX() + "," + offset.getY() + "," + offset.getZ());
		transceiver.setMetadata("attached", new FixedMetadataValue(MainPlugin.instance, block.getLocation().add(0.5,0.5,0.5).subtract(transceiver.getLocation()).toVector()));
		transceiver.setSmall(small);
		return transceiver;
	}
	
	public static void update(Entity e) {
		//Bukkit.broadcastMessage("Updating sensor");
		if (e instanceof LivingEntity le) {
			int type = getType(e);
			if (type == 1 || type == 2) {
				//Bukkit.broadcastMessage("Sensor type " + type);
				Location loc = le.getEyeLocation().add(0, type == 1 ? 1 : 1.4, 0);
				if (MainPlugin.inVoid(loc)) {
					//Bukkit.broadcastMessage("In void");
					ItemStack head = le.getEquipment().getHelmet();
					ItemMeta meta = head.getItemMeta();
					meta.setCustomModelData(33);
					head.setItemMeta(meta);
					le.getEquipment().setHelmet(head);
				} else {
					//Bukkit.broadcastMessage("In atmosphere");
					ItemStack head = le.getEquipment().getHelmet();
					ItemMeta meta = head.getItemMeta();
					meta.setCustomModelData(34);
					head.setItemMeta(meta);
					le.getEquipment().setHelmet(head);
				}
			}
		}
	}
	
	public static boolean isSmall(int type) {
		switch (type) {
		case 1:
			return true;
		case 2:
			return false;
		}
		return false;
	}
	
	public static int getType(Entity e) {
		for (String tag : e.getScoreboardTags()) {
			if (tag.contains("sensor_")) {
				return Integer.parseInt(tag.substring(tag.indexOf('_')+1));
			}
		}
		return -1;
	}
	
}