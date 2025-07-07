package com.lerdorf.spaceships;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.AbstractArrow.PickupStatus;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDismountEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import org.bukkit.event.player.*;

import net.countercraft.movecraft.MovecraftRotation;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.events.CraftPilotEvent;
import net.countercraft.movecraft.events.CraftRotateEvent;
import net.countercraft.movecraft.events.CraftSinkEvent;
import net.countercraft.movecraft.events.CraftTeleportEntityEvent;
import net.countercraft.movecraft.events.CraftTranslateEvent;
import net.countercraft.movecraft.util.MathUtils;
import net.countercraft.movecraft.util.hitboxes.HitBox;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class MainPlugin extends JavaPlugin implements Listener  {
	
	public static ArrayList<Entity> attached = new ArrayList<>();
	
	public static HashMap<Player, Craft> passenger = new HashMap<>();
	
	public static HashMap<Block, Integer> blockTags = new HashMap<Block, Integer>();
	public static List<Location> physicsImmune = new ArrayList<Location>();
	public static HashMap<Location[], Material> asyncFill = new HashMap<Location[], Material>();

	public static MainPlugin instance = null;

	public static final Material tempMat = Material.STRUCTURE_BLOCK;
	
	public static int c = 0;
	
	public static HashMap<Player, Integer> air = new HashMap<>();
	
	public static float taurus = 0;
	
	public Spacemap map;
	
	@Override
	public void onEnable() {
		System.out.println("Starting Spaceships");
		
		getServer().getPluginManager().registerEvents(this, this);
		this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
		instance = this;
		
		this.getCommand("turret").setExecutor(new Turret());
		this.getCommand("chair").setExecutor(new Chair());
		this.getCommand("radar").setExecutor(new Radar());
		this.getCommand("transceiver").setExecutor(new Transceiver());
		this.getCommand("spaceitem").setExecutor(new SpaceItem());
		this.getCommand("taurus").setExecutor(new Taurus());
	    this.getCommand("travel").setExecutor(new Travel());
	    this.getCommand("travel").setTabCompleter(new TravelTabCompleter());
	    this.getCommand("sensor").setExecutor(new Sensor());
		
		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
			  @Override
			  public void run() {
				  try {
					  if (c == 0) {
						  map = new Spacemap();
						  //Bukkit.broadcastMessage("Checking for attached things");
						  fixItems();
					  }
					  c++;
					  bulletUpdate();
					  asyncFillUpdate();
					  if (air.size() > 0) {
						  for (Player p : air.keySet())
							  p.setRemainingAir(air.get(p));
					  }
					  if (Spacemap.particles.size() > 0) {
						  Spacemap.render();
					  }
					  if (c % 2 == 0) {
						  
						  if (c%4 == 0) {
							  
							  if (taurus > 0) {
								  Player player = Taurus.player;
								  Location centerLoc = Taurus.centerLoc;
								  centerLoc.setPitch(0);
								  centerLoc.setYaw(taurus);
								  
								  player.teleport(centerLoc.clone().add(centerLoc.getDirection().multiply(Taurus.radius)));
								  
								  Bukkit.getScheduler().runTaskLater(MainPlugin.instance, () -> {
									  player.performCommand(Taurus.worldEdit);
							      }, 2L); // Delay of 1 ticks
								  
								  taurus-= Taurus.angleStep;
							  }
						  }
						  
						  if (c % 10 == 0) {
							  if (Bukkit.getOnlinePlayers().size() > 0) {
								  
								  for (Player p : Bukkit.getOnlinePlayers()) {
									  if (inVoid(p.getEyeLocation()) && !(p.getGameMode() == GameMode.CREATIVE || p.getGameMode() == GameMode.SPECTATOR) && !(p.isInsideVehicle() && p.getVehicle().getScoreboardTags().contains("Insulating"))) {
										  if (wearingSpaceSuit(p)) {
											  if (c % 80 == 0) {
												  if (!air.containsKey(p) || air.get(p) > 0) {
													  p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("You are in void"));
													  air.put(p, air.containsKey(p) ? air.get(p)-Math.max(p.getMaximumAir()/10, 1) : p.getRemainingAir());
												  } else {
													  p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.RED + "You are out of air"));
													  p.damage(3);
													  p.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 6*20, 5));
													  p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 1*20, 5));
												  }
											  }
										  } else {
											  if (c % 40 == 0) {
												  p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.RED + "You are not wearing a spacesuit"));
											  }
											  p.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 3*20, 5));
											  p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 5*20, 5));
											  p.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 8*20, 5));
											  p.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 3*20, 5));
										  }
									  } else {
										  if (air.containsKey(p)) {
											  air.put(p, air.get(p) + Math.max(p.getMaximumAir()/10, 1));
											  if (air.get(p) >= p.getMaximumAir())
												  air.remove(p);
										  }
									  }
								  }
							  }
						  }
						  if (attached.size() > 0) {
							  //Bukkit.broadcastMessage("Size > 0");
							  ArrayList<Entity> remove = new ArrayList<>();
							  boolean clearedMaps = false;
							  for (Entity e : attached) {
								  if (e == null)
									  remove.add(e);
								  boolean loaded = e.getLocation().getChunk().isLoaded() && e.getWorld().getPlayers().size() > 0 && Bukkit.getOnlinePlayers().size() > 0;
								  //Bukkit.broadcastMessage("Checking attached " + e);
								  if (loaded && !e.isValid())
									  remove.add(e);
								  else {
									  if (e.getScoreboardTags().contains("turret") && e instanceof ArmorStand turret && turret.getPassengers().size() > 0) {
										  if (turret.getPassengers().get(0) instanceof LivingEntity le) {
											  turret.setRotation(le.getEyeLocation().getYaw(), le.getEyeLocation().getPitch());
											  turret.setHeadPose(new EulerAngle(le.getEyeLocation().getPitch()*Math.PI/180, 0, 0));
										  }
									  } /*else if (e.getScoreboardTags().contains("chair_1") && e instanceof ArmorStand chair && chair.getPassengers().size() > 0) {
										  if (chair.getPassengers().get(0) instanceof LivingEntity le) {
											  chair.setRotation(le.getEyeLocation().getYaw(), 0);
											  //chair.setHeadPose(new EulerAngle(le.getEyeLocation().getPitch()*Math.PI/180, 0, 0));
										  }
									  }*/
									  if (c % 10 == 0) {
										  if (e.getScoreboardTags().contains("transceiver")) {
											  if (!clearedMaps) {
												  map.particles.clear();
											  }
											  map.updateMap(((LivingEntity)e).getEyeLocation(), Transceiver.getZoom(e));
										  } else if (e.getScoreboardTags().contains("sensor")) {
											  Sensor.update(e);
										  }
									  }
									  if (c % 30 == 0) {
										  if (e.hasMetadata("attached")) {
											  //Bukkit.broadcastMessage(e + " has metadata");
											  Vector offset = (Vector)e.getMetadata("attached").get(0).value();
											  Location loc = e.getLocation().add(offset);
											  Block block = loc.getBlock();
											  
											  //loc.getWorld().spawnParticle(Particle.CRIT, loc, 1, 0);
											  //Bukkit.broadcastMessage("Found block " + block.getType().name());
											  if (block.isPassable()) {
												  if (e.getScoreboardTags().contains("missing_block")) {
													  for (Player p : e.getWorld().getPlayers()) {
														  p.playSound(e.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1.5f, 1.5f);
													  }
													  //e.remove();
													  Bukkit.broadcastMessage("Removing " + e.getName());
													  remove.add(e);
													  e.getWorld().spawnParticle(Particle.EXPLOSION, e instanceof LivingEntity le ? le.getEyeLocation() : e.getLocation(), 1);
													  //Bukkit.broadcastMessage("Breaking attached thing");
												  } else {
													  e.addScoreboardTag("missing_block");
													  for (Player p : e.getWorld().getPlayers()) {
														  p.playSound(e.getLocation(), Sound.ENTITY_ITEM_BREAK, 1, 1);
													  }
												  }
											  } else {
												  if (e.getScoreboardTags().contains("missing_block")) {
													  e.removeScoreboardTag("missing_block");
												  }
											  }
										  }
									  }
								  }
							  }
							  
							  for (Entity e : remove) {
								  attached.remove(e);
								  e.remove();
							  }
						  }
					  }
						//System.out.println("Main loop");
			    	} catch (Exception e) {
			    		e.printStackTrace();
			    	}
			  }

			
			}, 0L, 1L);
	}
	
	public static void fixItems() {
		for (World world : Bukkit.getWorlds()) {
			//Bukkit.broadcastMessage("Checking " + world.getName());
			for (Entity e : world.getEntities()) {
				//Bukkit.broadcastMessage("Checking " + e.getName());
				if (e instanceof LivingEntity le && e.getScoreboardTags().contains("attached")) {
					//Bukkit.broadcastMessage("Found " + e.getName());
					fixItem(le);
				}
			}
		}
	}
	
	public static void fixItem(LivingEntity e) {
		attached.add(e);
		
		 for (String tag : e.getScoreboardTags()) {
			  if (tag.contains("attach_to:")) {
				  Vector offset = new Vector(Double.parseDouble(tag.substring(tag.indexOf(':')+1, tag.indexOf(','))), Double.parseDouble(tag.substring(tag.indexOf(',')+1, tag.indexOf(',', tag.indexOf(',')+1))), Double.parseDouble(tag.substring(tag.indexOf(',', tag.indexOf(',')+1)+1)));
				  e.setMetadata("attached", new FixedMetadataValue(instance, offset));
			  }
		 }
	}
	
	@EventHandler
	public void onChunkLoad(ChunkLoadEvent event) {
		Chunk chunk = event.getChunk();
		for (Entity e : chunk.getEntities()) {
			if (e instanceof LivingEntity le && e.getScoreboardTags().contains("attached") && !attached.contains(e)) {
				fixItem(le);
			}
		}
	}
	
	@EventHandler
	public void onPlayerItemHeld(PlayerItemHeldEvent event) {
		Player player = event.getPlayer();
		//if (inVoid(player.getLocation())) {
			boolean yeeted = false;
			Block target = player.getTargetBlock(null, 10);
			
			boolean scrollingMap = false;
			Entity map = null;

			//event.getPlayer().sendMessage("Scrolling");
			for (Entity e : attached) {
				if (e.getScoreboardTags().contains("transceiver") && e.hasMetadata("attached")) {
					  //Bukkit.broadcastMessage(e + " has metadata");
					  Vector offset = (Vector)e.getMetadata("attached").get(0).value();
					  Location loc = e.getLocation().add(offset);
					  Block block = loc.getBlock();
					  if (block != null && block.getLocation().distance(target.getLocation()) < 0.5) {
						  scrollingMap = true;
						  map = e;
						  break;
					  }
				}
			}
			
			if (scrollingMap && map != null) {
				//event.getPlayer().sendMessage("Scrolling on map");
				double scrollAmount = Math.pow(1.6, scroll(event.getPreviousSlot(), event.getNewSlot()));
				Transceiver.multZoom(map, (float)scrollAmount);
				this.map.updateMap(((LivingEntity)map).getEyeLocation(), Transceiver.getZoom(map));
				event.getPlayer().sendMessage("Set map zoom to " + ((int)(Transceiver.getZoom(map)*100))/100.0 + "x");
			}
			/*
			for (SpecialBlock b : blocks) {
				if (b != null && b.checkMaterial(target.getType()) && b.compareLocation(target.getLocation())) {
					b.scroll(event.getPreviousSlot(), event.getNewSlot(), player);
					yeeted = true;
				}
			}
			if (yeeted == false) {
				for (Spaceship s : ships) {
					if (s != null && s.blocks != null && s.blocks.length > 0)
						for (SpecialBlock b : s.blocks)
							if (b != null && b.checkMaterial(target.getType()) && b.compareLocation(target.getLocation()))
								b.scroll(event.getPreviousSlot(), event.getNewSlot(), player);
				}
			}
			*/
		//}
	}
	
	public static int scroll(int prev, int next) {
		
			if (prev == 8 && next < 5) prev = -1;
			else if (prev == 0  && next > 4) prev = 9;
			return next-prev;
			//zoom *= Math.pow(1.6,(next-prev));
			//player.sendMessage("Set map zoom to " + ((int)(zoom*100))/100.0 + "x");

	}
	
	private boolean wearingSpaceSuit(Player p) {
		// TODO Auto-generated method stub
		
		for (ItemStack item : p.getEquipment().getArmorContents()) {
			if (item != null && item.hasItemMeta() && item.getItemMeta().hasLore() && item.getItemMeta().getLore().contains("Spacesuit")) {
				continue;
			} else {
				return false;
			}
		}
		
		return true;
	}
	
	@EventHandler
	public void onSplashPotion(PotionSplashEvent event) {
		if (event.getPotion() != null && event.getPotion().getItem() != null && event.getPotion().getItem().hasItemMeta() && event.getPotion().getItem().getItemMeta().hasDisplayName()) {
			Location loc = event.getPotion().getLocation();
			if (event.getHitBlock() != null) {
				loc.add(event.getHitBlockFace().getDirection().multiply(0.5));
			}
			if (event.getPotion().getItem().getItemMeta().getDisplayName().contains(ChatColor.AQUA + "Air Bottle")) {
				/*
				int a = countVacuum(loc, 30);
				Bukkit.broadcastMessage("Filling " + a + " blocks");
				if (a >= 500) {
					replace(loc, 2000, tempMat, Material.VOID_AIR, 0, Particle.ENCHANTED_HIT);
				} else
					replace(loc, 300, tempMat, Material.CAVE_AIR, 0, Particle.CLOUD);
            	Bukkit.getScheduler().runTaskLater(this, () -> {
            		physicsImmune.clear();
		        }, 2L); // Delay of 2 ticks
		        */
				if (airSource(800, loc))
				{
					// success
					replace(loc, 800, tempMat, Material.CAVE_AIR, 0, Particle.CLOUD);
					if (event.getEntity().getShooter() instanceof Player p) {
						p.sendMessage(ChatColor.GREEN + "Filled the area with air");
					}
				} else {
					// fail
					replace(loc, 2000, tempMat, Material.AIR, 0, Particle.ENCHANTED_HIT);
					if (event.getEntity().getShooter() instanceof Player p) {
						p.sendMessage(ChatColor.RED + "Area is too big");
					}
				}
				Bukkit.getScheduler().runTaskLater(this, () -> {
		    		physicsImmune.clear();
		        }, 2L); // Delay of 2 ticks
			} else if (event.getPotion().getItem().getItemMeta().getDisplayName().contains(ChatColor.DARK_PURPLE + "Void Bottle")) {
				if (!inVoid(loc)) {
					Item item = (Item)loc.getWorld().spawnEntity(loc, EntityType.ITEM);
					item.setItemStack(SpaceItem.airBottle());
				}
				voidSource(loc, 200);
				//loc.getBlock().setType(Material.VOID_AIR);
			} 
		}
	}
	
	public void voidSource(Location loc, int size) {
		Material oldMat = loc.getBlock().getType();
		int a = countAir(loc, size);
    	//player.sendMessage("Regaining " + a + " cubic meters of air");
		if (a > 0 && a <= size*7)
			replace(loc, size*100, tempMat, Material.VOID_AIR, 0, Particle.ENCHANTED_HIT);
		else
			replace(loc, size*20, tempMat, oldMat, 0, Particle.CRIT);
    	Bukkit.getScheduler().runTaskLater(this, () -> {
    		physicsImmune.clear();
        }, 2L); // Delay of 2 ticks
	}
	
	@EventHandler
	public void onEntityDismount(EntityDismountEvent event) {
		//Bukkit.broadcastMessage("Dismounting");
		if (event.getEntity() instanceof Player player && !player.getScoreboardTags().contains("MovingRide")) {
			if (player.getScoreboardTags().contains("MovingRide")) {
				player.removeScoreboardTag("MovingRide");
			} else {
				if (player.hasMetadata("dismountoffset")) {
					Location loc = event.getDismounted().getLocation().add((Vector)player.getMetadata("dismountoffset").get(0).value());
					//Bukkit.broadcastMessage("Dismounting to " + loc);
					
					Bukkit.getScheduler().runTaskLater(this, () -> {
	
						player.teleport(loc);
			        }, 2L); // Delay of 2 ticks
					player.removeMetadata("dismountoffset", instance);
				}
				if (player.hasMetadata("dismountinv")) {
					player.getInventory().setContents((ItemStack[])player.getMetadata("dismountinv").get(0).value());
					player.removeMetadata("dismountinv", instance);
				}
			}
		}
	}
	
	@EventHandler
	public void onDropItem(PlayerDropItemEvent event) {
		if (event.getPlayer().isInsideVehicle() && event.getPlayer().getVehicle().getScoreboardTags().contains("turret")) {
			event.setCancelled(true);
		}
	}
	
	public boolean rightClickBlock(Player player, Block clickedBlock) {
		if (player.isInsideVehicle() && player.getVehicle().getScoreboardTags().contains("turret")) {
			return true;
		}
		for (Entity e : clickedBlock.getWorld().getNearbyEntities(clickedBlock.getLocation(), 3, 3, 3)) {
    		if (e.getScoreboardTags().contains("attached")) {

    	        //Bukkit.broadcastMessage("Checking attached entity");
    			if (e.hasMetadata("attached")) {

    		          //Bukkit.broadcastMessage("Right click metadata");
					  //Bukkit.broadcastMessage(e + " has metadata");
					  Vector offset = (Vector)e.getMetadata("attached").get(0).value();
					  Location loc = e.getLocation().add(offset);
					  Block block = loc.getBlock();
					  if (block.getLocation().distance(clickedBlock.getLocation()) < 1) {
						  rightClickAttached(player, e);
						  return true;
					  }
    			}
    		}
    	}
		return false;
	}
	
	public boolean useItem(Player player, ItemStack item) {
		if (player.isInsideVehicle()) {
        	if (player.getVehicle().getScoreboardTags().contains("turret")) {
	        	if (item != null && !player.hasCooldown(item.getType())) {
	        		int cooldown = Turret.fire(item, (LivingEntity) player.getVehicle());
	        		if (cooldown > 0)
	        			player.setCooldown(item.getType(), cooldown);
	        		return true;
	        	}
        	} else if (player.getVehicle().getScoreboardTags().contains("chair")) {
        		if (item != null && !player.hasCooldown(item.getType())) {
        			//passenger.get(player).translate(null, c, c, c);;
        			int cooldown = Chair.fire(item, (LivingEntity) player.getVehicle(), player);
        			//player.sendMessage("Cooldown: " + cooldown);
	        		if (cooldown > 0)
	        			player.setCooldown(item.getType(), cooldown);
	        		return true;
        		}
        	}
        } 
		if (item.getType() == Material.SHULKER_SHELL) {
			ItemStack headItem = player.getEquipment().getHelmet();
			if (headItem != null)
				headItem = headItem.clone();
			player.getEquipment().setHelmet(item);
			player.getEquipment().setItemInMainHand(headItem);
			return true;
		}
		return false;
	}
	
	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) {
	    Block placedBlock = event.getBlockPlaced();
	    Player player = event.getPlayer();

	    boolean cancelEvent = rightClickBlock(player, event.getBlockAgainst());
	    if (cancelEvent)
	    	event.setCancelled(true);
	    
	    // Player placed a block
	    //player.sendMessage("You placed a " + placedBlock.getType().toString());
	}
	
	@EventHandler
    public void onPlayerRightClick(PlayerInteractEvent event) {
        Action action = event.getAction();
        //Bukkit.broadcastMessage("Right click");
        boolean cancelEvent = false;
        if (event.getItem() != null)
        {
        	cancelEvent = useItem(event.getPlayer(), event.getItem());
        	if (cancelEvent) {
        		event.setCancelled(true);
        		return;
        	}
        }
        if (action == Action.RIGHT_CLICK_BLOCK) {
            //Bukkit.broadcastMessage("Right click block");
        	Block clickedBlock = event.getClickedBlock();
        	rightClickBlock(event.getPlayer(), clickedBlock);
        }
        
        
    }
	
	public static boolean validTarget(Entity e) {
		return e instanceof LivingEntity le && le.getType() != EntityType.ARMOR_STAND && le.getType() != EntityType.PAINTING && le.getType() != EntityType.ITEM_FRAME && le.getType() != EntityType.GLOW_ITEM_FRAME && !le.getScoreboardTags().contains("invulnerable") && !le.getScoreboardTags().contains("Invulnerable");
	}
	
	public void rightClickAttached(Player player, Entity e) {
		//Bukkit.broadcastMessage("Right click attached block");
		if (e.getScoreboardTags().contains("rideable")) {
			if (e.getScoreboardTags().contains("turret")) {
				player.setMetadata("dismountoffset", new FixedMetadataValue(MainPlugin.instance, player.getLocation().subtract(e.getLocation()).toVector()));
				player.setMetadata("dismountinv", new FixedMetadataValue(MainPlugin.instance, player.getInventory().getContents()));
				ItemStack[] armor = player.getEquipment().getArmorContents().clone();
				player.getInventory().clear();
				ItemStack[] turretItems = Turret.getItems(Turret.getType(e));
				if (turretItems != null)
					for (ItemStack item : turretItems)
						player.getInventory().addItem(item);
				player.getEquipment().setArmorContents(armor);
				player.setRotation(e.getLocation().getYaw(), e.getLocation().getPitch());
			}
			else if (e.getScoreboardTags().contains("chair")) {
				player.setMetadata("dismountoffset", new FixedMetadataValue(MainPlugin.instance, player.getLocation().subtract(e.getLocation()).toVector()));
				player.setMetadata("dismountinv", new FixedMetadataValue(MainPlugin.instance, player.getInventory().getContents()));
				ItemStack[] armor = player.getEquipment().getArmorContents().clone();
				player.getInventory().clear();
				ItemStack[] chairItems = Chair.getItems(Chair.getType(e));
				if (chairItems != null)
					for (ItemStack item : chairItems)
						player.getInventory().addItem(item);
				player.getEquipment().setArmorContents(armor);
			}
			

			e.addPassenger(player);
		}
		
	}
	
	public void entityTP(Entity e, Location loc) {
		Player rider = null;
		if (e.getPassengers().size() > 0) {
			if (e.getPassengers().get(0) instanceof Player player) {
				rider = player;
			}
		}
		if (rider != null) {
			rider.addScoreboardTag("MovingRide");
			rider.leaveVehicle();
			if (e.getScoreboardTags().contains("turret"))
				loc.setDirection(e.getLocation().getDirection());
		}
		e.teleport(loc);
		if (rider != null) {
			final Player player = rider;
			e.addPassenger(rider);
			Bukkit.getScheduler().runTaskLater(this, () -> {
				player.removeScoreboardTag("MovingRide");
	        }, 1L); // Delay of 1 tick
		}
	}
	
	@EventHandler
	public void onCraftPilot(CraftPilotEvent event) {
		BoundingBox bb = hbtobb2(event.getCraft().getHitBox());
		for (Entity e : event.getCraft().getWorld().getNearbyEntities(bb)) {
			if (e instanceof Player p) {
				passenger.put(p, event.getCraft());
			}
		}
	}
	
	@EventHandler
	public void onCraftTranslate(CraftTranslateEvent event) {
		World world = event.getCraft().getWorld();
		BoundingBox oldBB = hbtobb2(event.getOldHitBox());
		BoundingBox newBB = hbtobb2(event.getNewHitBox());
		Collection<Entity> entities = world.getNearbyEntities(oldBB);
		for (Entity e : entities) {
			if (moveable(e)) {
				Vector offset = e.getLocation().toVector().subtract(oldBB.getCenter());
				Location newLoc = newBB.getCenter().add(offset).toLocation(world);
				newLoc.setDirection(e.getLocation().getDirection());
				entityTP(e, newLoc);
				//e.teleport(newLoc);
			}
		}
	}
	
	@EventHandler
	public void onCraftSink(CraftSinkEvent event) {
		
	}
	
	@EventHandler
	public void onCraftRotate(CraftRotateEvent event) {
		World world = event.getCraft().getWorld();
		MovecraftRotation rotation = event.getRotation();
		boolean clockwise = rotation.equals(MovecraftRotation.CLOCKWISE);
		BoundingBox oldBB = hbtobb2(event.getOldHitBox());
		BoundingBox newBB = hbtobb2(event.getNewHitBox());
		Location originPoint = event.getOriginPoint().toBukkit(world);
		
		originPoint.setX(originPoint.getBlockX()+0.5);
		originPoint.setZ(originPoint.getBlockZ()+0.5);
		Craft craft = event.getCraft();
		
		Location midpoint = new Location(
                world,
                (oldBB.getMaxX() + oldBB.getMinX())/2.0,
                (oldBB.getMaxY() + oldBB.getMinY())/2.0,
                (oldBB.getMaxZ() + oldBB.getMinZ())/2.0);
		
		
		Collection<Entity> entities = world.getNearbyEntities(oldBB);
		for (Entity e : entities) {
			if (moveable(e)) {
				Location adjustedPLoc = e.getLocation().subtract(originPoint);
				
				double[] rotatedCoords = MathUtils.rotateVecNoRound(rotation,
	                    adjustedPLoc.getX(), adjustedPLoc.getZ());
	            float newYaw = rotation == MovecraftRotation.CLOCKWISE ? 90F : -90F;

	            CraftTeleportEntityEvent ctee = new CraftTeleportEntityEvent(craft, e);
	            Bukkit.getServer().getPluginManager().callEvent(ctee);
	            
	            if (ctee.isCancelled())
	            	continue;
	            
	            Location newLoc = new Location(world, rotatedCoords[0] + originPoint.getX(), e.getLocation().getY(), rotatedCoords[1] + originPoint.getZ());
	            newLoc.setYaw(e.getLocation().getYaw() + newYaw);
	            if (e.getPassengers().size() > 0) {
	            	if (e.getPassengers().get(0) instanceof Player p) {
	        			if (p.hasMetadata("dismountoffset")) {
	        				Vector offset = (Vector)p.getMetadata("dismountoffset").get(0).value();
	        				//Location oLoc = e.getLocation().add(offset).subtract(originPoint);
	        				double[] rotatedOffset = MathUtils.rotateVecNoRound(rotation, offset.getX(), offset.getZ());
	        				//Location rLoc = originPoint.clone().add(rotatedOffset[0], offset.getY(), rotatedOffset[1]);
	        				Vector newOffset = new Vector(rotatedOffset[0], offset.getY(), rotatedOffset[1]);
	        				p.setMetadata("dismountoffset", new FixedMetadataValue(MainPlugin.instance, newOffset));
	        			}
	            	}
	            }
	            //e.teleport(newLoc);
	            entityTP(e, newLoc);
			}
		}
	}
	
	@EventHandler
	public void onBlockPhysics(final BlockPhysicsEvent event) {
		if (inVoid(event.getBlock().getLocation()) && !physicsImmune.contains(event.getSourceBlock().getLocation()) && event.getSourceBlock().getLocation().equals(event.getBlock().getLocation())) {
			//getServer().broadcastMessage("Block: " + locStr(event.getBlock()) + " Source
			// Block: " + locStr(event.getSourceBlock()) + " Changed Type: " +
			// event.getChangedType().name());
			// getServer().broadcastMessage("BlockData: " +
			// event.getBlock().getBlockData());
			//Bukkit.broadcastMessage("ChangedType: " + event.getChangedType().name());
			//Bukkit.broadcastMessage("SourceBlock: " + event.getSourceBlock().getType().name() + " " + event.getSourceBlock().getLocation());
			//Bukkit.broadcastMessage("Block: " + event.getBlock().getType().name() + " " + event.getBlock().getLocation());
			
			/*
			if (isAir(event.getSourceBlock())) {
				if (touching(event.getSourceBlock(), Material.AIR) || touching(event.getSourceBlock(), Material.VOID_AIR)) {
					setVoidAir(event.getSourceBlock());
					if (isCaveAir(event.getSourceBlock().getLocation().clone().add(1, 0, 0).getBlock())) {
						setVoidAir(event.getSourceBlock().getLocation().clone().add(1, 0, 0).getBlock());
					}
					if (isCaveAir(event.getSourceBlock().getLocation().clone().add(0, 1, 0).getBlock())) {
						setVoidAir(event.getSourceBlock().getLocation().clone().add(0, 1, 0).getBlock());
					}
					if (isCaveAir(event.getSourceBlock().getLocation().clone().add(0, 0, 1).getBlock())) {
						setVoidAir(event.getSourceBlock().getLocation().clone().add(0, 0, 1).getBlock());
					}
					if (isCaveAir(event.getSourceBlock().getLocation().clone().add(-1, 0, 0).getBlock())) {
						setVoidAir(event.getSourceBlock().getLocation().clone().add(-1, 0, 0).getBlock());
					}
					if (isCaveAir(event.getSourceBlock().getLocation().clone().add(0, -1, 0).getBlock())) {
						setVoidAir(event.getSourceBlock().getLocation().clone().add(0, -1, 0).getBlock());
					}
					if (isCaveAir(event.getSourceBlock().getLocation().clone().add(0, 0, -1).getBlock())) {
						setVoidAir(event.getSourceBlock().getLocation().clone().add(0, 0, -1).getBlock());
					}
					//setCaveAir(event.getSourceBlock());
					//int a = countAir(event.getBlock().getLocation(), 200);
	            	//replace(event.getBlock().getLocation(), 2000, tempMat, Material.VOID_AIR, 0, Particle.ENCHANTED_HIT);
				} else {
					setCaveAir(event.getSourceBlock());
				}
			}
			*/
			
			// b = getBlock()
			// s = getSourceBlock()
			//    b 
			//  b s b
			//    b
			
			if (isAir(event.getSourceBlock())) {
				if ((touching(event.getSourceBlock(), Material.AIR) || touching(event.getSourceBlock(), Material.VOID_AIR)) && touching(event.getBlock(), Material.CAVE_AIR)) {
					// If this block has turned into air, and it is touching both vacuum and cave air
					if (event.getSourceBlock().getLocation().add(1, 0, 0).getBlock().getType() == Material.CAVE_AIR)
						voidSource(event.getSourceBlock().getLocation().add(1, 0, 0), 200);
					if (event.getSourceBlock().getLocation().add(0, 1, 0).getBlock().getType() == Material.CAVE_AIR)
						voidSource(event.getSourceBlock().getLocation().add(0, 1, 0), 200);
					if (event.getSourceBlock().getLocation().add(0, 0, 1).getBlock().getType() == Material.CAVE_AIR)
						voidSource(event.getSourceBlock().getLocation().add(0, 0, 1), 200);
					
					if (event.getSourceBlock().getLocation().add(-1, 0, 0).getBlock().getType() == Material.CAVE_AIR)
						voidSource(event.getSourceBlock().getLocation().add(-1, 0, 0), 200);
					if (event.getSourceBlock().getLocation().add(0, -1, 0).getBlock().getType() == Material.CAVE_AIR)
						voidSource(event.getSourceBlock().getLocation().add(0, -1, 0), 200);
					if (event.getSourceBlock().getLocation().add(0, 0, -1).getBlock().getType() == Material.CAVE_AIR)
						voidSource(event.getSourceBlock().getLocation().add(0, 0, -1), 200);
					//Bukkit.broadcastMessage("There's a hole!");
				} else if (touching(event.getSourceBlock(), Material.CAVE_AIR)) {
					setCaveAir(event.getSourceBlock());
				}
			}
			
			/*
			if (isAir(event.getSourceBlock()) && !isVoidAir(event.getSourceBlock()) && isCaveAir(event.getBlock())) {
				// Source is air (not void), touching cave air block -> set source to cave air
				setCaveAir(event.getSourceBlock());
			} else if ((isAir(event.getSourceBlock()) || isCaveAir(event.getSourceBlock())) && isAir(event.getBlock())) {
				// Source is air, cave air, or void air, touching air or void air -> set source to void air
				setVoidAir(event.getSourceBlock());
			} else if ((isVoidAir(event.getSourceBlock())) && isCaveAir(event.getBlock())) {
				// Source is void air, touching cave air -> set touching to void air
				setVoidAir(event.getBlock());
			}*/

			//specialBlockUpdate();
		}
	}

	public boolean touching(Block b, Material mat) {
		if (b.getLocation().clone().add(1, 0, 0).getBlock().getType() == mat) {
			return true;
		} else if (b.getLocation().clone().add(0, 1, 0).getBlock().getType() == mat) {
			return true;
		} else if (b.getLocation().clone().add(0, 0, 1).getBlock().getType() == mat) {
			return true;
		} else if (b.getLocation().clone().add(-1, 0, 0).getBlock().getType() == mat) {
			return true;
		} else if (b.getLocation().clone().add(0, -1, 0).getBlock().getType() == mat) {
			return true;
		} else if (b.getLocation().clone().add(0, 0, -1).getBlock().getType() == mat) {
			return true;
		}
		return false;
	}
	
	public BoundingBox hbtobb(HitBox hb) {
		return new BoundingBox(hb.getMinX(), hb.getMinY(), hb.getMinZ(), hb.getMaxX(), hb.getMaxY(), hb.getMaxZ());
	}
	
	public BoundingBox hbtobb2(HitBox hb) {
		return new BoundingBox(hb.getMinX()-2, hb.getMinY()-2, hb.getMinZ()-2, hb.getMaxX()+2, hb.getMaxY()+2, hb.getMaxZ()+2);
	}
	
	public boolean moveable(Entity e) {
		return e instanceof ArmorStand;
	}
	
	public void bulletUpdate() {
		if (Bullet.bullets == null)
			return;
		ArrayList<Bullet> removeBullets = new ArrayList<Bullet>();
		for (Iterator<Bullet> ib = Bullet.bullets.iterator(); ib.hasNext(); ) {
			Bullet b = ib.next();
			b.update();
			if (b.isDead())
				removeBullets.add(b);
		}
		for (Bullet b : removeBullets) {
			Bullet.bullets.remove(b);
		}
	}
	
	public static boolean inVoid(Location loc) {
		return (loc.getBlock().getType() == Material.AIR || loc.getBlock().getType() == Material.VOID_AIR) && (loc.getWorld().getName().toLowerCase().strip().contains("system") || loc.getWorld().getName().toLowerCase().strip().contains("space") || loc.getWorld().getName().toLowerCase().strip().contains("void"));
	}

	public void setVoidAir(Block block) {
		/*
		 * try { TimeUnit.MILLISECONDS.sleep(100); } catch (InterruptedException e) { //
		 * TODO Auto-generated catch block e.printStackTrace(); }
		 */
		// getServer().broadcastMessage("Creating void air!");
		
		/*ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

		Runnable task = new Runnable() {
			public void run() {
				block.setType(Material.VOID_AIR);
				Location loc = block.getLocation();
				loc.getWorld().playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 1, 2);
				loc.getWorld().spawnParticle(Particle.CRIT_MAGIC, loc, 5);
			}
		};

		int delay = 100;
		scheduler.schedule(task, delay, TimeUnit.MILLISECONDS);
		scheduler.shutdown();*/
		
		block.setType(Material.VOID_AIR);
		Location loc = block.getLocation();
		loc.getWorld().playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 1, 2);
		loc.getWorld().spawnParticle(Particle.ENCHANTED_HIT, loc, 5);
		
		// block.setBlockData(Material.valueOf("VOID_AIR").createBlockData());
		// block.setBlockData(Bukkit.createBlockData("CraftBlockData{minecraft:void_air}"));
		// Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "execute in " +
		// loc.getWorld().getName() + " run setblock " + loc.getBlockX() + " " +
		// loc.getBlockY() + " " + loc.getBlockZ() + " void_air");
	}

	public static void setCaveAir(Block block) {
		/*
		 * try { TimeUnit.MILLISECONDS.sleep(100); } catch (InterruptedException e) { //
		 * TODO Auto-generated catch block e.printStackTrace(); }
		 */
		// getServer().broadcastMessage("Creating cave air!");
		block.setType(Material.CAVE_AIR);
		// block.setBlockData(Material.valueOf("AIR").createBlockData());
		// block.setBlockData(Bukkit.createBlockData("CraftBlockData{minecraft:cave_air}"));
		Location loc = block.getLocation();
		loc.getWorld().spawnParticle(Particle.FIREWORK, loc, 5);
		// Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "execute in " +
		// loc.getWorld().getName() + " run setblock " + loc.getBlockX() + " " +
		// loc.getBlockY() + " " + loc.getBlockZ() + " cave_air");
	}

	public void setAir(Block block) {
		/*
		 * try { TimeUnit.MILLISECONDS.sleep(100); } catch (InterruptedException e) { //
		 * TODO Auto-generated catch block e.printStackTrace(); }
		 */
		// getServer().broadcastMessage("Creating air!");
		block.setType(Material.VOID_AIR);
		// block.setBlockData(Material.valueOf("AIR").createBlockData());
		// block.setBlockData(Bukkit.createBlockData("CraftBlockData{minecraft:air}"));
		Location loc = block.getLocation();
		loc.getWorld().spawnParticle(Particle.CRIT, loc, 5);
		// Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "execute in " +
		// loc.getWorld().getName() + " run setblock " + loc.getBlockX() + " " +
		// loc.getBlockY() + " " + loc.getBlockZ() + " air");
	}
	
	public static boolean isSomeAir(Block block) {
		return block.getType() == Material.VOID_AIR ||block.getType() == Material.CAVE_AIR || block.getType() == Material.AIR;
	}

	public static boolean isAir(Block block) {
		return block.getType() == Material.AIR
				|| block.getType() == Material.VOID_AIR ;
	}

	public boolean isVoidAir(Block block) {
		return block.getType() == Material.VOID_AIR ;
	}

	public boolean isCaveAir(Block block) {
		return block.getType() == Material.CAVE_AIR ;
	}
	
	public static int failedAirSource(Location loc) {
		if (loc.getBlock().getType() == tempMat && blockTags.keySet().contains(loc.getBlock()) && blockTags.get(loc.getBlock()) == 0) {
			// Material ogMat = loc.getBlock().getType();\
			physicsImmune.add(loc.getBlock().getLocation());
			loc.getBlock().setType(Material.VOID_AIR);
			loc.getWorld().spawnParticle(Particle.ENCHANTED_HIT, loc, 1);
			blockTags.remove(loc.getBlock());
			return 1 + failedAirSource(loc.clone().add(new Vector(1, 0, 0)))
					+ failedAirSource(loc.clone().add(new Vector(-1, 0, 0)))
					+ failedAirSource(loc.clone().add(new Vector(0, 1, 0)))
					+ failedAirSource(loc.clone().add(new Vector(0, -1, 0)))
					+ failedAirSource(loc.clone().add(new Vector(0, 0, 1)))
					+ failedAirSource(loc.clone().add(new Vector(0, 0, -1)));

			// loc.getBlock().setType(ogMat);
			// System.out.println("WE GOT A FALSE");
			// return false;
		}
		return 0;
		// System.out.println("WE GOT A FALSE");
	}
	
	public static int countAir(Location loc, int l) {
		if (loc.getBlock().getType() == Material.CAVE_AIR && l > 0) {

			physicsImmune.add(loc.getBlock().getLocation());
			loc.getWorld().spawnParticle(Particle.DOLPHIN, loc, 5);
			//System.out.println(l);
			loc.getBlock().setType(tempMat);
			blockTags.put(loc.getBlock(), 0);
			int r = 1 + countAir(loc.clone().add(new Vector(1, 0, 0)), l-1)
					+ countAir(loc.clone().add(new Vector(-1, 0, 0)), l-1)
					+ countAir(loc.clone().add(new Vector(0, 1, 0)), l-1)
					+ countAir(loc.clone().add(new Vector(0, -1, 0)), l-1)
					+ countAir(loc.clone().add(new Vector(0, 0, 1)), l-1)
					+ countAir(loc.clone().add(new Vector(0, 0, -1)), l-1);

			//loc.getBlock().setType(Material.CAVE_AIR);
			return r;

		}
		//System.out.println("WE GOT A FALSE");
		return 0;
	}
	
	public static boolean airSource(int air, Location loc) {
		if (isSomeAir(loc.getBlock())) {
			if (air < 1)
				return false;
			loc.getWorld().spawnParticle(Particle.CLOUD, loc, 5);
			loc.getWorld().playSound(loc, Sound.BLOCK_FIRE_EXTINGUISH, 1, 2);
			Material ogMat = loc.getBlock().getType();
			air--;
			physicsImmune.add(loc.getBlock().getLocation());
			loc.getBlock().setType(tempMat);
			blockTags.put(loc.getBlock(), 0);
			if (airSource(air, loc.clone().add(new Vector(1, 0, 0)))
					&& airSource(air, loc.clone().add(new Vector(-1, 0, 0)))
					&& airSource(air, loc.clone().add(new Vector(0, 1, 0)))
					&& airSource(air, loc.clone().add(new Vector(0, -1, 0)))
					&& airSource(air, loc.clone().add(new Vector(0, 0, 1)))
					&& airSource(air, loc.clone().add(new Vector(0, 0, -1)))) {
				// setCaveAir(loc.getBlock());
				// loc.getBlock().setType(Material.STONE);
				return true;
			} else {
				failedAirSource(loc.clone().add(new Vector(1, 0, 0)));
				failedAirSource(loc.clone().add(new Vector(-1, 0, 0)));
				failedAirSource(loc.clone().add(new Vector(0, 1, 0)));
				failedAirSource(loc.clone().add(new Vector(0, -1, 0)));
				failedAirSource(loc.clone().add(new Vector(0, 0, 1)));
				failedAirSource(loc.clone().add(new Vector(0, 0, -1)));
			}
			loc.getBlock().setType(ogMat);
			// System.out.println("WE GOT A FALSE");
			return false;
		}
		// System.out.println("WE GOT A FALSE");
		return true;
	}
	
	public static int countVacuum(Location loc, int l) {
		if (loc.getBlock().getType() == Material.VOID_AIR && l > 0) {

			physicsImmune.add(loc.getBlock().getLocation());
			loc.getBlock().setType(tempMat);
			blockTags.put(loc.getBlock(), 0);
			int r = 1 + countVacuum(loc.clone().add(new Vector(1, 0, 0)), l-1)
					+ countVacuum(loc.clone().add(new Vector(-1, 0, 0)), l-1)
					+ countVacuum(loc.clone().add(new Vector(0, 1, 0)), l-1)
					+ countVacuum(loc.clone().add(new Vector(0, -1, 0)), l-1)
					+ countVacuum(loc.clone().add(new Vector(0, 0, 1)), l-1)
					+ countVacuum(loc.clone().add(new Vector(0, 0, -1)), l-1);

			//loc.getBlock().setType(Material.VOID_AIR);
			return r;

		} else if (loc.getBlock().getType() == Material.AIR && l > 0) {

			physicsImmune.add(loc.getBlock().getLocation());
			loc.getBlock().setType(tempMat);
			blockTags.put(loc.getBlock(), 0);
			int r = 1 + countVacuum(loc.clone().add(new Vector(1, 0, 0)), l - 1)
					+ countVacuum(loc.clone().add(new Vector(-1, 0, 0)), l - 1)
					+ countVacuum(loc.clone().add(new Vector(0, 1, 0)), l - 1)
					+ countVacuum(loc.clone().add(new Vector(0, -1, 0)), l - 1)
					+ countVacuum(loc.clone().add(new Vector(0, 0, 1)), l - 1)
					+ countVacuum(loc.clone().add(new Vector(0, 0, -1)), l - 1);

			//loc.getBlock().setType(Material.AIR);
			return r;

		}
		return 0;
		// System.out.println("WE GOT A FALSE");
	}
	
	public static int successAirSource(Location loc) {
		if (loc.getBlock().getType() == tempMat && blockTags.keySet().contains(loc.getBlock()) && blockTags.get(loc.getBlock()) == 0) {
			// Material ogMat = loc.getBlock().getType();\
			physicsImmune.add(loc.getBlock().getLocation());
			loc.getBlock().setType(Material.CAVE_AIR);
			blockTags.remove(loc.getBlock());
			return 1 + successAirSource(loc.clone().add(new Vector(1, 0, 0)))
					+ successAirSource(loc.clone().add(new Vector(-1, 0, 0)))
					+ successAirSource(loc.clone().add(new Vector(0, 1, 0)))
					+ successAirSource(loc.clone().add(new Vector(0, -1, 0)))
					+ successAirSource(loc.clone().add(new Vector(0, 0, 1)))
					+ successAirSource(loc.clone().add(new Vector(0, 0, -1)));

			// loc.getBlock().setType(ogMat);
			// System.out.println("WE GOT A FALSE");
			// return false;
		}
		return 0;
		// System.out.println("WE GOT A FALSE");
	}
	
	public static int replace(Location loc, int l, Material mat1, Material mat2) {
		if (loc.getBlock().getType() == mat1 && l > 0) {
			physicsImmune.add(loc.getBlock().getLocation());
			loc.getBlock().setType(mat2);
			return 1 + replace(loc.clone().add(new Vector(1, 0, 0)), l-1, mat1, mat2)
			+ replace(loc.clone().add(new Vector(-1, 0, 0)), l-1, mat1, mat2)
			+ replace(loc.clone().add(new Vector(0, 1, 0)), l-1, mat1, mat2)
			+ replace(loc.clone().add(new Vector(0, -1, 0)), l-1, mat1, mat2)
			+ replace(loc.clone().add(new Vector(0, 0, 1)), l-1, mat1, mat2)
			+ replace(loc.clone().add(new Vector(0, 0, -1)), l-1, mat1, mat2);
		}
		return 0;
	}
	
	public static int replace(Location loc, int l, Material mat1, Material mat2, int tag) {
		if (loc.getBlock().getType() == mat1 && l > 0 && blockTags.keySet().contains(loc.getBlock()) && blockTags.get(loc.getBlock()) == tag) {
			physicsImmune.add(loc.getBlock().getLocation());
			blockTags.remove(loc.getBlock());
			loc.getBlock().setType(mat2);
			return 1 + replace(loc.clone().add(new Vector(1, 0, 0)), l-1, mat1, mat2, tag)
			+ replace(loc.clone().add(new Vector(-1, 0, 0)), l-1, mat1, mat2, tag)
			+ replace(loc.clone().add(new Vector(0, 1, 0)), l-1, mat1, mat2, tag)
			+ replace(loc.clone().add(new Vector(0, -1, 0)), l-1, mat1, mat2, tag)
			+ replace(loc.clone().add(new Vector(0, 0, 1)), l-1, mat1, mat2, tag)
			+ replace(loc.clone().add(new Vector(0, 0, -1)), l-1, mat1, mat2, tag);
		}
		return 0;
	}
	
	public static int replace(Location loc, int l, Material mat1, Material mat2, int tag, Particle p) {
		if (loc.getBlock().getType() == mat1 && l > 0 && blockTags.keySet().contains(loc.getBlock()) && blockTags.get(loc.getBlock()) == tag) {
			physicsImmune.add(loc.getBlock().getLocation());
			loc.getWorld().spawnParticle(p, loc, 1, 0, 0, 0, 0);
			blockTags.remove(loc.getBlock());
			loc.getBlock().setType(mat2);
			return 1 + replace(loc.clone().add(new Vector(1, 0, 0)), l-1, mat1, mat2, tag, p)
			+ replace(loc.clone().add(new Vector(-1, 0, 0)), l-1, mat1, mat2, tag, p)
			+ replace(loc.clone().add(new Vector(0, 1, 0)), l-1, mat1, mat2, tag, p)
			+ replace(loc.clone().add(new Vector(0, -1, 0)), l-1, mat1, mat2, tag, p)
			+ replace(loc.clone().add(new Vector(0, 0, 1)), l-1, mat1, mat2, tag, p)
			+ replace(loc.clone().add(new Vector(0, 0, -1)), l-1, mat1, mat2, tag, p);
		}
		return 0;
	}
	
	public static void fillAsync(String world, int x1, int y1, int z1, int x2, int y2, int z2, Material mat) {

		System.out.println("Commencing asyc fill for " + (Math.abs(x1-x2)*Math.abs(y1-y2)*Math.abs(z1-z2)) + " blocks, " + Math.abs(x1-x2) + "x" + Math.abs(y1-y2) + "x" + Math.abs(z1-z2));
		World w = Bukkit.getWorld(world);
		asyncFill.put(new Location[] {new Location(w, Math.min(x1,x2), Math.min(y1,y2), Math.min(z1,z2)), new Location(w, Math.min(x1,x2), Math.min(y1,y2), Math.min(z1,z2)), new Location(w, Math.max(x1,x2), Math.max(y1,y2), Math.max(z1,z2))}, mat);
	}
	
	void asyncFillUpdate() {
		if (asyncFill.size() > 0) {
			for (Location[] key : asyncFill.keySet()) {
				while (key[1].getBlockY() <= key[2].getBlockY()) {
					if (key[1].getBlockX() <= key[2].getBlockX()) {
						key[1].getBlock().setType(asyncFill.get(key));
						key[1].add(new Vector(1, 0, 0));
					} else
						key[1] = new Location(key[1].getWorld(), key[0].getBlockX(), key[1].getBlockY()+1, key[1].getBlockZ());

				}
				if (key[1].getBlockZ() < key[2].getBlockZ()) {
					key[1] = new Location(key[1].getWorld(), key[0].getBlockX(), key[0].getBlockY(), key[1].getBlockZ()+1);
				} else {
					getServer().broadcastMessage("Async fill complete!");
					asyncFill.remove(key);
				}
				//break;
			}
		}
	}
	
}
