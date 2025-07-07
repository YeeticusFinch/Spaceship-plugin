package com.lerdorf.spaceships;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ArmorMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;
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

public class SpaceItem implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		// TODO Auto-generated method stub
		
		if (args.length > 1) {
			if (args[0].equalsIgnoreCase("fix")) {
				MainPlugin.fixItems();
				return true;
			}
			if (args[0].equalsIgnoreCase("give")) {
				try {
					if (sender instanceof Player p) {
						if (args[1].contains("air")) {
							p.getInventory().addItem(airBottle());
						} else if (args[1].contains("void")) {
							p.getInventory().addItem(voidBottle());
						} else if (args[1].contains("spacesuit")) {
							ItemStack[] items = spacesuit();
							for (ItemStack item : items) {
								p.getInventory().addItem(item);
							}
						} else if (args[1].contains("armored_spacesuit")) {
							ItemStack[] items = armoredSpacesuit();
							for (ItemStack item : items) {
								if (item != null)
									p.getInventory().addItem(item);
							}
						}
					} else {
						sender.sendMessage(ChatColor.RED + "Only players can execute this command");
					}
				} catch (Exception e) {
					sender.sendMessage(ChatColor.RED + "Something went wrong");
					sender.sendMessage(ChatColor.YELLOW + e.getLocalizedMessage());
				}
			}
		}
		
		return false;
	}
	
	private ItemStack[] spacesuit() {
		ItemStack[] items = new ItemStack[4];
		{
			ItemStack helmet = new ItemStack(Material.SHULKER_SHELL);
			ItemMeta meta = helmet.getItemMeta();
			List<String> lore = new ArrayList<String>();
			lore.add("Spacesuit");
			meta.setLore(lore);
			meta.setCustomModelData(26);
			meta.setDisplayName(ChatColor.GREEN + "Spacesuit Helmet");
			meta = setAttribute(meta, 1, Attribute.GENERIC_ARMOR, "generic.armor", new EquipmentSlot[] {EquipmentSlot.HEAD});
			meta = setAttribute(meta, -0.01f, Attribute.GENERIC_MOVEMENT_SPEED, "generic.movementSpeed", new EquipmentSlot[] {EquipmentSlot.HEAD});
			meta = setAttribute(meta, -0.1f, Attribute.GENERIC_FLYING_SPEED, "generic.flyingSpeed", new EquipmentSlot[] {EquipmentSlot.HEAD});
			meta = setAttribute(meta, -0.1f, Attribute.GENERIC_ATTACK_SPEED, "generic.attacjSpeed", new EquipmentSlot[] {EquipmentSlot.HEAD});
			//LeatherArmorMeta lMeta = (LeatherArmorMeta) meta;
			//lMeta.setColor(Color.WHITE);
			//((ArmorMeta) lMeta).setTrim(new ArmorTrim(TrimMaterial.NETHERITE, TrimPattern.SILENCE));
			helmet.setItemMeta(meta);
			items[0] = helmet;
		}
		{
			ItemStack chestplate = new ItemStack(Material.LEATHER_CHESTPLATE);
			ItemMeta meta = chestplate.getItemMeta();
			List<String> lore = new ArrayList<String>();
			lore.add("Spacesuit");
			meta.setLore(lore);
			meta.setCustomModelData(26);
			meta.setDisplayName(ChatColor.GREEN + "Spacesuit Chestplate");
			meta = setAttribute(meta, 2, Attribute.GENERIC_ARMOR, "generic.armor", new EquipmentSlot[] {EquipmentSlot.CHEST});
			meta = setAttribute(meta, -0.01f, Attribute.GENERIC_MOVEMENT_SPEED, "generic.movementSpeed", new EquipmentSlot[] {EquipmentSlot.CHEST});
			meta = setAttribute(meta, -0.1f, Attribute.GENERIC_FLYING_SPEED, "generic.flyingSpeed", new EquipmentSlot[] {EquipmentSlot.CHEST});
			meta = setAttribute(meta, -0.1f, Attribute.GENERIC_ATTACK_SPEED, "generic.attacjSpeed", new EquipmentSlot[] {EquipmentSlot.CHEST});
			LeatherArmorMeta lMeta = (LeatherArmorMeta) meta;
			lMeta.setColor(Color.WHITE);
			((ArmorMeta) lMeta).setTrim(new ArmorTrim(TrimMaterial.NETHERITE, TrimPattern.SILENCE));
			chestplate.setItemMeta(lMeta);
			items[1] = chestplate;
		}
		{
			ItemStack leggings = new ItemStack(Material.LEATHER_LEGGINGS);
			ItemMeta meta = leggings.getItemMeta();
			List<String> lore = new ArrayList<String>();
			lore.add("Spacesuit");
			meta.setLore(lore);
			meta.setCustomModelData(26);
			meta.setDisplayName(ChatColor.GREEN + "Spacesuit Leggings");
			meta = setAttribute(meta, 2, Attribute.GENERIC_ARMOR, "generic.armor", new EquipmentSlot[] {EquipmentSlot.LEGS});
			meta = setAttribute(meta, -0.01f, Attribute.GENERIC_MOVEMENT_SPEED, "generic.movementSpeed", new EquipmentSlot[] {EquipmentSlot.LEGS});
			meta = setAttribute(meta, -0.1f, Attribute.GENERIC_FLYING_SPEED, "generic.flyingSpeed", new EquipmentSlot[] {EquipmentSlot.LEGS});
			meta = setAttribute(meta, -0.1f, Attribute.GENERIC_ATTACK_SPEED, "generic.attacjSpeed", new EquipmentSlot[] {EquipmentSlot.LEGS});
			LeatherArmorMeta lMeta = (LeatherArmorMeta) meta;
			lMeta.setColor(Color.WHITE);
			((ArmorMeta) lMeta).setTrim(new ArmorTrim(TrimMaterial.NETHERITE, TrimPattern.SILENCE));
			leggings.setItemMeta(lMeta);
			items[2] = leggings;
		}
		{
			ItemStack boots = new ItemStack(Material.LEATHER_BOOTS);
			ItemMeta meta = boots.getItemMeta();
			List<String> lore = new ArrayList<String>();
			lore.add("Spacesuit");
			meta.setLore(lore);
			meta.setCustomModelData(26);
			meta.setDisplayName(ChatColor.GREEN + "Spacesuit Boots");
			meta = setAttribute(meta, 1, Attribute.GENERIC_ARMOR, "generic.armor", new EquipmentSlot[] {EquipmentSlot.FEET});
			meta = setAttribute(meta, -0.01f, Attribute.GENERIC_MOVEMENT_SPEED, "generic.movementSpeed", new EquipmentSlot[] {EquipmentSlot.FEET});
			meta = setAttribute(meta, -0.1f, Attribute.GENERIC_FLYING_SPEED, "generic.flyingSpeed", new EquipmentSlot[] {EquipmentSlot.FEET});
			meta = setAttribute(meta, -0.1f, Attribute.GENERIC_ATTACK_SPEED, "generic.attacjSpeed", new EquipmentSlot[] {EquipmentSlot.FEET});
			LeatherArmorMeta lMeta = (LeatherArmorMeta) meta;
			lMeta.setColor(Color.WHITE);
			((ArmorMeta) lMeta).setTrim(new ArmorTrim(TrimMaterial.NETHERITE, TrimPattern.SILENCE));
			boots.setItemMeta(lMeta);
			items[3] = boots;
		}
		return items;
	}
	
	private ItemStack[] armoredSpacesuit() {
		ItemStack[] items = new ItemStack[4];
		{
			ItemStack helmet = new ItemStack(Material.SHULKER_SHELL);
			ItemMeta meta = helmet.getItemMeta();
			List<String> lore = new ArrayList<String>();
			lore.add("Spacesuit");
			meta.setLore(lore);
			meta.setCustomModelData(26);
			meta.setDisplayName(ChatColor.GREEN + "Spacesuit Helmet");
			meta = setAttribute(meta, 3, Attribute.GENERIC_ARMOR, "generic.armor", new EquipmentSlot[] {EquipmentSlot.HEAD});
			meta = setAttribute(meta, -0.015f, Attribute.GENERIC_MOVEMENT_SPEED, "generic.movementSpeed", new EquipmentSlot[] {EquipmentSlot.HEAD});
			meta = setAttribute(meta, -0.15f, Attribute.GENERIC_FLYING_SPEED, "generic.flyingSpeed", new EquipmentSlot[] {EquipmentSlot.HEAD});
			meta = setAttribute(meta, -0.1f, Attribute.GENERIC_ATTACK_SPEED, "generic.attacjSpeed", new EquipmentSlot[] {EquipmentSlot.HEAD});
			//ArmorMeta lMeta = (ArmorMeta) meta;
			//lMeta.setTrim(new ArmorTrim(TrimMaterial.NETHERITE, TrimPattern.SILENCE));
			helmet.setItemMeta(meta);
			items[0] = helmet;
		}
		{
			ItemStack chestplate = new ItemStack(Material.IRON_CHESTPLATE);
			ItemMeta meta = chestplate.getItemMeta();
			List<String> lore = new ArrayList<String>();
			lore.add("Spacesuit");
			meta.setLore(lore);
			meta.setCustomModelData(26);
			meta.setDisplayName(ChatColor.GREEN + "Spacesuit Chestplate");
			meta = setAttribute(meta, 5, Attribute.GENERIC_ARMOR, "generic.armor", new EquipmentSlot[] {EquipmentSlot.CHEST});
			meta = setAttribute(meta, -0.015f, Attribute.GENERIC_MOVEMENT_SPEED, "generic.movementSpeed", new EquipmentSlot[] {EquipmentSlot.CHEST});
			meta = setAttribute(meta, -0.15f, Attribute.GENERIC_FLYING_SPEED, "generic.flyingSpeed", new EquipmentSlot[] {EquipmentSlot.CHEST});
			meta = setAttribute(meta, -0.1f, Attribute.GENERIC_ATTACK_SPEED, "generic.attacjSpeed", new EquipmentSlot[] {EquipmentSlot.CHEST});
			ArmorMeta lMeta = (ArmorMeta) meta;
			lMeta.setTrim(new ArmorTrim(TrimMaterial.NETHERITE, TrimPattern.SILENCE));
			chestplate.setItemMeta(lMeta);
			items[1] = chestplate;
		}
		{
			ItemStack leggings = new ItemStack(Material.IRON_LEGGINGS);
			ItemMeta meta = leggings.getItemMeta();
			List<String> lore = new ArrayList<String>();
			lore.add("Spacesuit");
			meta.setLore(lore);
			meta.setCustomModelData(26);
			meta.setDisplayName(ChatColor.GREEN + "Spacesuit Leggings");
			meta = setAttribute(meta, 4, Attribute.GENERIC_ARMOR, "generic.armor", new EquipmentSlot[] {EquipmentSlot.LEGS});
			meta = setAttribute(meta, -0.015f, Attribute.GENERIC_MOVEMENT_SPEED, "generic.movementSpeed", new EquipmentSlot[] {EquipmentSlot.LEGS});
			meta = setAttribute(meta, -0.15f, Attribute.GENERIC_FLYING_SPEED, "generic.flyingSpeed", new EquipmentSlot[] {EquipmentSlot.LEGS});
			meta = setAttribute(meta, -0.1f, Attribute.GENERIC_ATTACK_SPEED, "generic.attacjSpeed", new EquipmentSlot[] {EquipmentSlot.LEGS});
			ArmorMeta lMeta = (ArmorMeta) meta;
			lMeta.setTrim(new ArmorTrim(TrimMaterial.NETHERITE, TrimPattern.SILENCE));
			leggings.setItemMeta(lMeta);
			items[2] = leggings;
		}
		{
			ItemStack boots = new ItemStack(Material.IRON_BOOTS);
			ItemMeta meta = boots.getItemMeta();
			List<String> lore = new ArrayList<String>();
			lore.add("Spacesuit");
			meta.setLore(lore);
			meta.setCustomModelData(26);
			meta.setDisplayName(ChatColor.GREEN + "Spacesuit Boots");
			meta = setAttribute(meta, 2, Attribute.GENERIC_ARMOR, "generic.armor", new EquipmentSlot[] {EquipmentSlot.FEET});
			meta = setAttribute(meta, -0.015f, Attribute.GENERIC_MOVEMENT_SPEED, "generic.movementSpeed", new EquipmentSlot[] {EquipmentSlot.FEET});
			meta = setAttribute(meta, -0.15f, Attribute.GENERIC_FLYING_SPEED, "generic.flyingSpeed", new EquipmentSlot[] {EquipmentSlot.FEET});
			meta = setAttribute(meta, -0.1f, Attribute.GENERIC_ATTACK_SPEED, "generic.attacjSpeed", new EquipmentSlot[] {EquipmentSlot.FEET});
			ArmorMeta lMeta = (ArmorMeta) meta;
			lMeta.setTrim(new ArmorTrim(TrimMaterial.NETHERITE, TrimPattern.SILENCE));
			boots.setItemMeta(lMeta);
			items[3] = boots;
		}
		return items;
	}

	public static ItemStack airBottle() {
		ItemStack item = new ItemStack(Material.SPLASH_POTION);
		PotionMeta meta = (PotionMeta) item.getItemMeta();
		meta.setMaxStackSize(2);
		meta.setColor(Color.WHITE);
		meta.setDisplayName(ChatColor.AQUA + "Air Bottle");
		meta.setEnchantmentGlintOverride(false);
		List<String> lore = new ArrayList<>();
		lore.add(ChatColor.WHITE + "Air source");
		meta.setLore(lore);
		//meta = setAttribute(meta, -0.5, Attribute.GENERIC_JUMP_STRENGTH, "generic.jumpStrength", new EquipmentSlot[] {EquipmentSlot.})
		item.setItemMeta(meta);
		return item;
	}
	
	public static ItemStack voidBottle() {
		ItemStack item = new ItemStack(Material.SPLASH_POTION);
		PotionMeta meta = (PotionMeta) item.getItemMeta();
		meta.setMaxStackSize(2);
		meta.setColor(Color.BLACK);
		meta.setDisplayName(ChatColor.DARK_PURPLE + "Void Bottle");
		meta.setEnchantmentGlintOverride(false);
		List<String> lore = new ArrayList<>();
		lore.add(ChatColor.WHITE + "Void source");
		meta.setLore(lore);
		//meta = setAttribute(meta, -0.5, Attribute.GENERIC_JUMP_STRENGTH, "generic.jumpStrength", new EquipmentSlot[] {EquipmentSlot.})
		item.setItemMeta(meta);
		return item;
	}
	
	public static ItemMeta setAttribute(ItemMeta meta, double amount, Attribute attribute, String attrStr, EquipmentSlot slots[]) {
		if (meta != null) {
        	//double damage = meta.getAttributeModifiers().t
        	
        	
        	if (meta.getAttributeModifiers(attribute) != null) {

           	 // Remove existing attack damage modifiers
               meta.getAttributeModifiers(attribute).forEach(modifier -> 
                   meta.removeAttributeModifier(attribute, modifier)
               );
        	}
        	
            
            for (EquipmentSlot slot : slots) {
	            AttributeModifier modifier = new AttributeModifier(
	                UUID.randomUUID(),
	                attrStr,
	                amount,
	                AttributeModifier.Operation.ADD_NUMBER,
	                slot
	            );
	
	            meta.addAttributeModifier(attribute, modifier);
            }
		}
		return meta;
	}
}
