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

import net.countercraft.movecraft.MovecraftRotation;
import net.countercraft.movecraft.events.CraftRotateEvent;
import net.countercraft.movecraft.events.CraftSinkEvent;
import net.countercraft.movecraft.events.CraftTranslateEvent;
import net.countercraft.movecraft.util.hitboxes.HitBox;
import net.md_5.bungee.api.ChatColor;

public class Turret implements CommandExecutor {

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
					sender.sendMessage(ChatColor.RED + "Turret type must be an integer");
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
		ArmorStand turret = (ArmorStand)block.getWorld().spawnEntity(block.getLocation().add(0.5, top ? 0.5 : (small ? -1.4 : -2.5), 0.5), EntityType.ARMOR_STAND);
		turret.setGravity(false);
		turret.setVisible(false);
		turret.setMarker(true);
		ItemStack head = new ItemStack(Material.IRON_INGOT);
		ItemMeta meta = head.getItemMeta();
		meta.setCustomModelData(type);
		head.setItemMeta(meta);
		turret.getEquipment().setHelmet(head);
		turret.addScoreboardTag("turret");
		turret.addScoreboardTag("rideable");
		turret.addScoreboardTag("Insulating");
		turret.addScoreboardTag("turret_"+type);
		turret.setCustomName("turret_"+type);
		Location loc = turret.getLocation();
		Location eyeLoc = turret.getEyeLocation();
		eyeLoc.setDirection(placeLoc.subtract(loc).toVector());
		loc.setDirection(eyeLoc.getDirection());
		turret.teleport(loc);
		turret.addScoreboardTag("attached");
		MainPlugin.attached.add(turret);
		Vector offset = block.getLocation().add(0.5,0.5,0.5).subtract(turret.getLocation()).toVector();
		turret.addScoreboardTag("attach_to:" + offset.getX() + "," + offset.getY() + "," + offset.getZ());
		turret.setMetadata("attached", new FixedMetadataValue(MainPlugin.instance, block.getLocation().add(0.5,0.5,0.5).subtract(turret.getLocation()).toVector()));
		turret.setSmall(small);
		return turret;
	}
	
	public static boolean isSmall(int type) {
		switch (type) {
		case 1:
		case 2:
			return true;
		}
		return false;
	}
	
	public static int getType(Entity e) {
		for (String tag : e.getScoreboardTags()) {
			if (tag.contains("turret_")) {
				return Integer.parseInt(tag.substring(tag.indexOf('_')+1));
			}
		}
		return -1;
	}
	
	public static ItemStack[] getItems(int type) {
		switch (type) {
			case 1: // Basic turret
			{
				ItemStack item1 = new ItemStack(Material.CRIMSON_BUTTON);
				ItemMeta meta = item1.getItemMeta();
				meta.setDisplayName(ChatColor.YELLOW + "10MW Blaster");
				item1.setItemMeta(meta);
				return new ItemStack[] {item1};
			}
			case 2: // Basic double barrel turret
			{
				ItemStack item1 = new ItemStack(Material.CRIMSON_BUTTON);
				ItemMeta meta = item1.getItemMeta();
				meta.setDisplayName(ChatColor.YELLOW + "10MW Blaster");
				item1.setItemMeta(meta);
				ItemStack item2 = new ItemStack(Material.DARK_OAK_BUTTON);
				meta = item2.getItemMeta();
				meta.setDisplayName(ChatColor.YELLOW + "50MW Blaster");
				meta.setEnchantmentGlintOverride(true);
				item2.setItemMeta(meta);
				return new ItemStack[] {item1, item2};
			}
		}
		return null;
	}
	
	public static HashMap<LivingEntity, Integer> barrelNum = new HashMap<>(); 

	public static int fire(ItemStack item, LivingEntity turret) {
		// TODO Auto-generated method stub
		if (item != null && item.hasItemMeta()) {
			int type = getType(turret);
			String itemName = item.getItemMeta().getDisplayName();
			Vector velocity = null;
			Location loc = null;
			boolean hitscan = false;
			Particle particle = null;
			int range = 0;
			float size = 0;
			int hitEffect = 0;
			int damage = -1;
			int projectiles = 1;
			float spreadX = 0;
			float spreadY = 0;
			int cooldown = 5;
			if (itemName.contains(ChatColor.YELLOW + "10MW Blaster")) {
				// Bullet(Vector velocity, Location location, boolean hitscan, Particle particle, int range, float size, int hitEffect, LivingEntity owner, Plugin plugin, int damage, int projectiles, float spreadX, float spreadY)
				velocity = turret.getLocation().getDirection().multiply(3);
				loc = turret.getEyeLocation().add(0, 0.8, 0).add(turret.getLocation().getDirection());
				hitscan = false;
				particle = null;
				range = 100;
				size = 0.4f;
				hitEffect = 1;
				damage = 12;
				cooldown = 10;
				for (Player p : loc.getWorld().getPlayers()) {
					p.playSound(loc, Sound.ENTITY_BLAZE_HURT, 1, 1.1f);
					p.playSound(loc, Sound.BLOCK_DISPENSER_LAUNCH, 1, 1.3f);
				}
			} else if (itemName.contains(ChatColor.YELLOW + "50MW Blaster")) {
				// Bullet(Vector velocity, Location location, boolean hitscan, Particle particle, int range, float size, int hitEffect, LivingEntity owner, Plugin plugin, int damage, int projectiles, float spreadX, float spreadY)
				velocity = turret.getLocation().getDirection().multiply(3);
				loc = turret.getEyeLocation().add(0, 0.8, 0).add(turret.getLocation().getDirection());
				hitscan = false;
				particle = Particle.FLAME;
				range = 100;
				size = 0.4f;
				hitEffect = 2;
				damage = 16;
				cooldown = 20;
				for (Player p : loc.getWorld().getPlayers()) {
					p.playSound(loc, Sound.ENTITY_BLAZE_HURT, 1, 0.8f);
					p.playSound(loc, Sound.ENTITY_BLAZE_SHOOT, 1, 1.6f);
				}
			}
			
			if (velocity != null) {
				switch (type) {
					case 1: // Basic turret
					{
						Bullet b = new Bullet(velocity, loc.clone(), hitscan, particle, range, 0.4f, hitEffect, turret, MainPlugin.instance, damage, projectiles, spreadX, spreadY);
						return (int)(cooldown * 1.5);
					}
					case 2: // Basic double barrel turret
					{
						Vector dir = loc.getDirection();
						Vector right = dir.clone().crossProduct(new Vector(0, 1, 0)).normalize();
						if (barrelNum.containsKey(turret) && barrelNum.get(turret) == 1) {
							Bullet b = new Bullet(velocity, loc.clone().subtract(right.clone().multiply(0.75)), hitscan, particle, range, 0.4f, hitEffect, turret, MainPlugin.instance, damage, projectiles, spreadX, spreadY);
							barrelNum.put(turret, 0);
						} else {
							Bullet b = new Bullet(velocity, loc.clone().add(right.clone().multiply(0.75)), hitscan, particle, range, 0.4f, hitEffect, turret, MainPlugin.instance, damage, projectiles, spreadX, spreadY);
							barrelNum.put(turret, 1);
						}
						
						return (int)(cooldown * 0.5);
					}
				}
			}
		}
		return 0;
	}
}
