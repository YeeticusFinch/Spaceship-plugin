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

public class Transceiver implements CommandExecutor {

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
		//boolean top = true;
		//if (block.getLocation().getY() > p.getEyeLocation().getY()) {
		//	top = false;
		//}
		boolean small = isSmall(type);
		ArmorStand transceiver = (ArmorStand)block.getWorld().spawnEntity(block.getLocation().add(0.5, (small ? 0.15 : -0.5), 0.5), EntityType.ARMOR_STAND);
		transceiver.setGravity(false);
		transceiver.setVisible(false);
		transceiver.setMarker(true);
		ItemStack head = new ItemStack(Material.BEACON);
		ItemMeta meta = head.getItemMeta();
		//meta.setCustomModelData(type);
		//head.setItemMeta(meta);
		transceiver.getEquipment().setHelmet(head);
		transceiver.addScoreboardTag("transceiver");
		transceiver.addScoreboardTag("zoom_1");
		transceiver.addScoreboardTag("transceiver"+type);
		transceiver.setCustomName("transceiver_"+type);
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
	
	public static boolean isSmall(int type) {
		switch (type) {
		case 1:
			return true;
		case 2:
			return false;
		}
		return false;
	}

	public static float getZoom(Entity e) {
		for (String tag : e.getScoreboardTags()) {
			if (tag.contains("zoom_")) {
				return Float.parseFloat(tag.substring(tag.indexOf('_')+1));
			}
		}
		return -1;
	}
	
	public static void multZoom(Entity e, float m) {
		String zoomTag = null;
		for (String tag : e.getScoreboardTags()) {
			if (tag.contains("zoom_")) {
				zoomTag = tag;
				break;
			}
		}
		e.removeScoreboardTag(zoomTag);
		float zoom = Float.parseFloat(zoomTag.substring(zoomTag.indexOf('_')+1));
		zoom *= m;
		if (zoom < 0.001 || zoom > 1000000)
			zoom = 1;
		e.addScoreboardTag("zoom_"+zoom);
	}
	
	public static int getType(Entity e) {
		for (String tag : e.getScoreboardTags()) {
			if (tag.contains("transceiver_")) {
				return Integer.parseInt(tag.substring(tag.indexOf('_')+1));
			}
		}
		return -1;
	}
	
}
