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

public class Radar implements CommandExecutor {

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
					sender.sendMessage(ChatColor.RED + "Chair type must be an integer");
				}
			}
		}
		
		return false;
	}
	
	public static ArmorStand create(Block block, int type, Location placeLoc) {
		boolean top = true;
		//if (block.getLocation().getY() > p.getEyeLocation().getY()) {
		//	top = false;
		//}
		boolean small = isSmall(type);
		ArmorStand radar = (ArmorStand)block.getWorld().spawnEntity(block.getLocation().add(0.5, top ? 1 : (small ? -1.4 : -2.5), 0.5), EntityType.ARMOR_STAND);
		radar.setGravity(false);
		radar.setVisible(false);
		radar.setMarker(true);
		ItemStack head = new ItemStack(Material.DIAMOND);
		ItemMeta meta = head.getItemMeta();
		meta.setCustomModelData(type);
		head.setItemMeta(meta);
		radar.getEquipment().setHelmet(head);
		radar.addScoreboardTag("radar");
		radar.addScoreboardTag("radar"+type);
		radar.setCustomName("radar_"+type);
		Location loc = radar.getLocation();
		Location temp = loc.clone().setDirection(placeLoc.clone().subtract(loc).toVector());
		loc.setYaw((int)Math.round(temp.getYaw() /90) * 90);
		//p.sendMessage("Yaw: "+ (int)Math.round(temp.getYaw() /90) * 90);
		radar.teleport(loc);
		radar.addScoreboardTag("attached");
		MainPlugin.attached.add(radar);
		Vector offset = block.getLocation().add(0.5,0.5,0.5).subtract(radar.getLocation()).toVector();
		radar.addScoreboardTag("attach_to:" + offset.getX() + "," + offset.getY() + "," + offset.getZ());
		radar.setMetadata("attached", new FixedMetadataValue(MainPlugin.instance, block.getLocation().add(0.5,0.5,0.5).subtract(radar.getLocation()).toVector()));
		radar.setSmall(small);
		return radar;
	}
	
	public static boolean isSmall(int type) {
		switch (type) {
		}
		return false;
	}
	
	public static int getType(Entity e) {
		for (String tag : e.getScoreboardTags()) {
			if (tag.contains("radar_")) {
				return Integer.parseInt(tag.substring(tag.indexOf('_')+1));
			}
		}
		return -1;
	}
	
}
