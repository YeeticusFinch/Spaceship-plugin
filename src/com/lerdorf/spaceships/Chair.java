package com.lerdorf.spaceships;

import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
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
import org.bukkit.plugin.Plugin;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import net.countercraft.movecraft.CruiseDirection;
import net.countercraft.movecraft.MovecraftRotation;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.events.CraftRotateEvent;
import net.countercraft.movecraft.events.CraftSinkEvent;
import net.countercraft.movecraft.events.CraftTranslateEvent;
import net.countercraft.movecraft.util.hitboxes.HitBox;
import net.md_5.bungee.api.ChatColor;

public class Chair implements CommandExecutor {

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
							boolean top = true;
							//if (block.getLocation().getY() > p.getEyeLocation().getY()) {
							//	top = false;
							//}
							boolean small = isSmall(type);
							ArmorStand chair = (ArmorStand)block.getWorld().spawnEntity(block.getLocation().add(0.5, top ? 1.5 : (small ? -1.4 : -2.5), 0.5), EntityType.ARMOR_STAND);
							chair.setGravity(false);
							chair.setVisible(false);
							chair.setMarker(true);
							ItemStack head = new ItemStack(Material.GOLD_INGOT);
							ItemMeta meta = head.getItemMeta();
							meta.setCustomModelData(type);
							head.setItemMeta(meta);
							chair.getEquipment().setHelmet(head);
							chair.addScoreboardTag("chair");
							chair.addScoreboardTag("rideable");
							chair.addScoreboardTag("chair_"+type);
							chair.setCustomName("chair_"+type);
							Location loc = chair.getLocation();
							Location temp = loc.clone().setDirection(p.getLocation().subtract(loc).toVector());
							if (type == 1 || type == 2)
								loc.setYaw((int)Math.round(temp.getYaw() /90) * 90);
							else if (type == 3)
								loc.setYaw((int)Math.round(temp.getYaw() /45) * 45);
							//p.sendMessage("Yaw: "+ (int)Math.round(temp.getYaw() /90) * 90);
							chair.teleport(loc);
							chair.addScoreboardTag("attached");
							MainPlugin.attached.add(chair);
							Vector offset = block.getLocation().add(0.5,0.5,0.5).subtract(chair.getLocation()).toVector();
							chair.addScoreboardTag("attach_to:" + offset.getX() + "," + offset.getY() + "," + offset.getZ());
							chair.setMetadata("attached", new FixedMetadataValue(MainPlugin.instance, block.getLocation().add(0.5,0.5,0.5).subtract(chair.getLocation()).toVector()));
							chair.setSmall(small);
							
							//if (top) turret.addScoreboardTag("top_turret");
							//else turret.addScoreboardTag("bottom_turret");
						}
					} else {
						sender.sendMessage(ChatColor.RED + "Only players can execute this command");
					}
				} catch (Exception e) {
					sender.sendMessage(ChatColor.RED + "Chair type must be an integer");
				}
			}
		}
		
		return false;
	}
	
	public boolean isSmall(int type) {
		switch (type) {
		}
		return false;
	}
	
	public static int getType(Entity e) {
		for (String tag : e.getScoreboardTags()) {
			if (tag.contains("chair_")) {
				return Integer.parseInt(tag.substring(tag.indexOf('_')+1));
			}
		}
		return -1;
	}
	
	public static ItemStack[] getItems(int type) {
		switch (type) {
			case 1: // Basic Pilot Seat
			{
				ItemStack item1 = new ItemStack(Material.LEVER);
				ItemMeta meta = item1.getItemMeta();
				meta.setDisplayName(ChatColor.GREEN + "Move");
				meta.setEnchantmentGlintOverride(true);
				item1.setItemMeta(meta);
				
				ItemStack item2 = new ItemStack(Material.REPEATER);
			    meta = item2.getItemMeta();
				meta.setDisplayName(ChatColor.GREEN + "Cruise");
				meta.setEnchantmentGlintOverride(true);
				item2.setItemMeta(meta);
				
				ItemStack item3 = new ItemStack(Material.PRISMARINE_SHARD);
			    meta = item3.getItemMeta();
				meta.setDisplayName(ChatColor.GREEN + "Accelerate");
				meta.setEnchantmentGlintOverride(true);
				item3.setItemMeta(meta);
				
				ItemStack item4 = new ItemStack(Material.PRISMARINE_CRYSTALS);
			    meta = item4.getItemMeta();
				meta.setDisplayName(ChatColor.GREEN + "Descelerate");
				meta.setEnchantmentGlintOverride(true);
				item4.setItemMeta(meta);
				
				return new ItemStack[] {item1, item2, item3, item4 };
			}
			case 2: // Fighter Chair
			{
				
			}
		}
		return null;
	}
	
	public static HashMap<LivingEntity, Integer> barrelNum = new HashMap<>(); 

	public static int fire(ItemStack item, LivingEntity chair, Player player) {
		// TODO Auto-generated method stub
		if (item != null && item.hasItemMeta()) {
			int type = getType(chair);
			String itemName = item.getItemMeta().getDisplayName();
			Location loc = null;
			int cooldown = 5;
			if (itemName.contains(ChatColor.GREEN + "Move")) {
				if (MainPlugin.passenger.containsKey(player)) {
					Craft craft = MainPlugin.passenger.get(player);
					Vector dir = player.getLocation().getDirection().normalize();
					if (dir.getX() > 0.6)	
						dir.setX(1);
					else if (dir.getX() < -0.6)
						dir.setX(-1);
					if (dir.getY() > 0.6)
						dir.setY(1);
					else if (dir.getY() < -0.6)
						dir.setY(-1);
					if (dir.getZ() > 0.6)
						dir.setZ(1);
					else if (dir.getZ() < -0.6)
						dir.setZ(-1);
					dir.dot(chair.getLocation().getDirection().multiply(craft.getSpeed()/2));
					craft.translate(craft.getWorld(), dir.getBlockX(), dir.getBlockY(), dir.getBlockZ());
				} else {
					player.sendMessage(ChatColor.RED + "You must right click the sign before you can pilot the ship");
				}
				cooldown = 10;
				return cooldown;
			} else if (itemName.contains(ChatColor.GREEN + "Cruise")) {
				if (MainPlugin.passenger.containsKey(player)) {
					Craft craft = MainPlugin.passenger.get(player);
					Vector dir = player.getLocation().getDirection().normalize();
					//craft.translate(craft.getWorld(), dir.getBlockX(), dir.getBlockY(), dir.getBlockZ());
					boolean cruising = craft.getCruising();
					if (cruising) {
						craft.setCruising(false);
						craft.setCurrentGear(1);
					} else {
						//craft.setCruiseDirection(CruiseDirection.);
						craft.setCruising(true);
						craft.setCurrentGear(1);
						
						Vector cruiseDir = chair.getLocation().getDirection();
						if (Math.abs(cruiseDir.getX()) > Math.abs(cruiseDir.getZ())) {
							if (cruiseDir.getX() > 0) {
								craft.setCruiseDirection(CruiseDirection.EAST);
							} else {
								craft.setCruiseDirection(CruiseDirection.WEST);
							}
						} else {
							if (cruiseDir.getZ() > 0) {
								craft.setCruiseDirection(CruiseDirection.SOUTH);
							} else {
								craft.setCruiseDirection(CruiseDirection.NORTH);
							}
						}
						
					}
				} else {
					player.sendMessage(ChatColor.RED + "You must right click the sign before you can pilot the ship");
				}
				cooldown = 10;
				return cooldown;
			} else if (itemName.contains(ChatColor.GREEN + "Accelerate")) {
				if (MainPlugin.passenger.containsKey(player)) {
					Craft craft = MainPlugin.passenger.get(player);
					if (craft.getCruising()) {
						craft.setCurrentGear(craft.getCurrentGear()+1);
						player.sendMessage(ChatColor.GREEN + "Set gear to " + craft.getCurrentGear());
					}
				} else {
					player.sendMessage(ChatColor.RED + "You must right click the sign before you can pilot the ship");
				}
				cooldown = 120;
			} else if (itemName.contains(ChatColor.GREEN + "Descelerate")) {
				if (MainPlugin.passenger.containsKey(player)) {
					Craft craft = MainPlugin.passenger.get(player);
					if (craft.getCruising()) {
						craft.setCurrentGear(Math.max(craft.getCurrentGear()-1, 1));
						player.sendMessage(ChatColor.YELLOW + "Set gear to " + craft.getCurrentGear());
					}
				} else {
					player.sendMessage(ChatColor.RED + "You must right click the sign before you can pilot the ship");
				}
				cooldown = 40;
				return cooldown;
			}
			
			
			return cooldown;
		}
		return 20;
	}
}
