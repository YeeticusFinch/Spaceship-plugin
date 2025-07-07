package com.lerdorf.spaceships;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.attribute.Attribute;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.Hangable;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.LightningStrike;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Bullet {

	public static List<Bullet> bullets = new ArrayList<Bullet>();
	
	public Vector velocity;
	public Location location;
	public boolean hitscan;
	public Particle particle;
	public int range;
	public boolean[] dead = new boolean[] {false};
	public float size;
	public int hitEffect;
	public LivingEntity owner;
	public Plugin plugin;
	public int damage;
	public int projectiles;
	public float spreadX;
	public float spreadY;
	
	public static final int PHYSICAL = 0;
	public static final int ELEMENTAL = 1;
	public static final int PSYCHIC = 2;
	public static final int NECROTIC = 3;
	public static final int DIVINE = 4;
	public static final int FORCE = 5;
	
	int initialRange = 100;
	
	public Vector up = null;
	public Vector right = null;
	
	public LivingEntity target;
	
	public Bullet(Vector velocity, Location location, boolean hitscan, Particle particle, int range, float size, int hitEffect, LivingEntity owner, Plugin plugin) {
		Construct(velocity, location, hitscan, particle, range, size, hitEffect, owner, plugin, -1, 1, 0, 0);
	}
	
	void Construct(Vector velocity, Location location, boolean hitscan, Particle particle, int range, float size, int hitEffect, LivingEntity owner, Plugin plugin, int damage, int projectiles, float spreadX, float spreadY) {		
		this.location = location;
		this.velocity = velocity;
		this.hitscan = hitscan;
		this.range = range;
		this.hitEffect = hitEffect;
		this.particle = particle;
		this.owner = owner;
		this.plugin = plugin;
		this.damage = damage;
		this.projectiles = projectiles;
		this.spreadX = spreadX;
		this.spreadY = spreadY;
		this.size = size;
		initialRange = range;
		bullets.add(this);
		
		right = velocity.clone().crossProduct(new Vector(0, 1, 0)).normalize();
		up = right.clone().crossProduct(velocity).normalize();
		
		dead = new boolean[projectiles];
		for (int i = 0; i < projectiles; i++)
			dead[i] = false;
		
		if (hitscan) {
			while (hitscan && !isDead()) {
				bulletStep();
			}
		}
	}
	
	public Bullet(Vector velocity, Location location, boolean hitscan, Particle particle, int range, float size, int hitEffect, LivingEntity owner, Plugin plugin, int damage) {
		Construct(velocity, location, hitscan, particle, range, size, hitEffect, owner, plugin, damage, 1, 0, 0);
	}
	
	public Bullet(Vector velocity, Location location, boolean hitscan, Particle particle, int range, float size, int hitEffect, LivingEntity owner, Plugin plugin, int damage, int projectiles, float spreadX, float spreadY) {
		Construct(velocity, location, hitscan, particle, range, size, hitEffect, owner, plugin, damage, projectiles, spreadX, spreadY);
	}
	
	public boolean isDead() {
		for (boolean b : dead) {
			if (!b)
				return false;
		}
		return true;
	}
	
	public void update() {
		if (!hitscan)
		{
			bulletStep();
		}
	}
	
	int cc = 0;
	public void bulletStep() {
		range -= 1;

		if (velocity.length() > 1) {
			for (int k = 0; k < velocity.length(); k++) {
				CheckStep(location.clone().add(velocity.clone().normalize().multiply(k)));
			}
			CheckStep(location.clone().add(velocity));
		}
		
		if (!isDead()) {
			
			location = location.add(velocity);
			//range -= 1;
			
			if (range <= 0) {
				for (int i = 0; i < projectiles; i++)
					if (!dead[i])
						hit(null, null, i, location);
			}
		}
		

		if (cc > 300 || cc > initialRange) {
			range = -1;
			for (int i = 0; i < projectiles; i++)
				dead[i] = true;
		}
		
		cc++;
	}
	
	boolean CheckStep(Location stepLoc) {
		for (int i = 0; i < projectiles; i++) {
			if (dead[i])
				continue;
			else {
				Location loc = stepLoc.clone();
				
				if (i >= 1 && projectiles > 1 && spreadX + spreadY > 0)
					loc = loc.add(right.clone().multiply(Math.cos((i-1) * 2.0*Math.PI / (projectiles-1)) * spreadX * cc)).add(up.clone().multiply(Math.sin((i-1) * 2.0*Math.PI / (projectiles-1)) * spreadY * cc));
				
				if (hitEffect == 1 || hitEffect == 2)
					colorDust(location, 1, 0.5f, 0.1f, 1, 0f, 0f, 0f, 1f);
				
				if (range < 0) {
					hit(null, null, i, stepLoc);
				}
				if (particle != null) {
					loc.getWorld().spawnParticle(particle, loc, 1, 0, 0, 0, 0);
					
				}
				CheckEntities(loc, i);
				//if (hitEffect != 25)
					CheckLocation(loc.clone(), i);
				if (dead[i]) return true;
			}
			
		}
		return false;
	}
	
	Vector proj(Vector a, Vector b) { // Returns a vector along b
		return b.multiply(a.dot(b) / b.lengthSquared());
	}
	
	Vector gramSchmidt(Vector a, Vector b) { // Returns a vector perpendicular to a in the direction of b
		return b.subtract(proj(b, a));
	}
	
	Vector normalize(Vector v) {
		return v.multiply(1/v.length());
	}
	
	public static void damage(Damageable entity, float damage, Entity source, int type) {
		
		if (entity.getScoreboardTags().contains("ElementalWard")) {
			if (type == ELEMENTAL)
				damage /= 2;
		} else if (entity.getScoreboardTags().contains("PrimordialWard")) {
			if (type == ELEMENTAL || type == NECROTIC || type == DIVINE)
				damage *= 0.2f;
		}
		if (entity.getScoreboardTags().contains("Unholy")) {
			if (type == NECROTIC)
				damage /= 2;
			else if (type == DIVINE)
				damage *= 2;
		}
		else if (entity.getScoreboardTags().contains("Construct")) {
			if (type == PSYCHIC)
				damage *= 0.1;
			else if (type == ELEMENTAL)
				damage *= 0.5;
			else if (type == PHYSICAL || type == FORCE)
				damage *= 2;
		}
		else if (entity.getScoreboardTags().contains("Hired")) {
			if (type == PSYCHIC)
				damage *= 2;
		}
		
		entity.damage(damage, source);
	}
	
	public void hit(Block block, Damageable entity, int k, Location location) {
		//if ((hitEffect == 17 || hitEffect == 15) && (block != null || entity != null)) // Go through entities or blocks
		//	dead[k] = dead[k]; // Don't stop the bullet
		//else if (block == null && (hitEffect == 6 || hitEffect == 7 || hitEffect == 9 || hitEffect == 19 || hitEffect == 26)) // Pierce through entities
		//	dead[k] = dead[k]; // Don't stop the bullet
		//else
			dead[k] = true; // Stop the bullet
		
		switch (hitEffect) {
			case 1: // Blaster Bolt
				if (entity != null && MainPlugin.validTarget(entity)) {
					damage(entity, damage != -1 ? damage : 9, owner, PHYSICAL);
					//entity.damage(damage != -1 ? damage : 9, owner);
				}
				for (Player player : Bukkit.getServer().getOnlinePlayers())
					player.playSound(location, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 3f, 0.9f);
				//location.getWorld().spawnParticle(Particle.FIREWORK, location, 1, 0, 0, 0, 0);
				colorDust(location, 1, 0.5f, 0.1f, 5, 0.2f, 0.2f, 0.2f, 4f);
				//location.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, location, 1, 0, 0, 0, 0);
				if (block != null) { // There should be a chance at breaking blocks
					if (3*Math.random()/Math.max((damage != -1 ? damage : 9), 1) < 1 / block.getType().getBlastResistance()) {
						block.breakNaturally();
					}
				}
				break;
			case 2: // Small Explosive Blaster Bolt
				if (entity != null && MainPlugin.validTarget(entity)) {
					damage(entity, damage != -1 ? damage : 12, owner, PHYSICAL);
					//entity.damage(damage != -1 ? damage : 9, owner);
				}
				colorDust(location, 1, 0.5f, 0.1f, 15, 0.4f, 0.4f, 0.4f, 10f);
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "execute in " + location.getWorld().getName().toLowerCase().trim() + " positioned " + location.getX() + " " + location.getY() + " " + location.getZ() + " run summon creeper ~ ~ ~ {Fuse:0,ExplosionRadius:2}");
				//for (Player player : Bukkit.getServer().getOnlinePlayers())
				//	player.playSound(location, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 10f, 0.5f);
				//location.getWorld().spawnParticle(Particle.EXPLOSION, location, 1, 0, 0, 0, 0);
				//location.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, location, 1, 0, 0, 0, 0);
				
				break;
		}
	}
	
	public static void colorDust(Location loc, float r, float g, float b, int count, float x, float y, float z, float speed) {
		Particle.DustOptions dust = new Particle.DustOptions(
                Color.fromRGB((int) (r * 255), (int) (g * 255), (int) (b * 255)), speed);
        loc.getWorld().spawnParticle(Particle.DUST, loc.getX(), loc.getY(), loc.getZ(), count, x, y, z, dust);
	}
	
	public static boolean intersectsBlock(Location loc, double radius) {
		if (loc.getBlock() != null && !loc.getBlock().isPassable() && !loc.getBlock().isLiquid()) {
			Vector minVector = loc.toVector().clone().subtract(new Vector(radius, radius, radius));
			Vector maxVector = loc.toVector().clone().add(new Vector(radius, radius, radius));
			BoundingBox box = loc.getBlock().getBoundingBox();
			if (box.overlaps(minVector, maxVector))
				return true;
		}
		return false;
	}
	
	public void CheckLocation(Location loc, int i) {
		if (intersectsBlock(loc, size*0.3))
				hit(loc.getBlock(), null, i, loc);
		
	}
	
	public void CheckEntities(Location loc, int i) {
		Collection<Entity> hitEntities = loc.getWorld().getNearbyEntities(loc, size*4, size*4, size*4);
		if (hitEntities.size() > 0) {
			Vector minVector = loc.toVector().clone().subtract(new Vector(size*0.6, size*0.6, size*0.6));
			Vector maxVector = loc.toVector().clone().add(new Vector(size*0.6, size*0.6, size*0.6));
			Vector minVector2 = loc.toVector().clone().add(velocity.clone().multiply(0.5)).subtract(new Vector(size*0.6, size*0.6, size*0.6));
			Vector maxVector2 = loc.toVector().clone().add(velocity.clone().multiply(0.5)).add(new Vector(size*0.6, size*0.6, size*0.6));
			for (Entity e : hitEntities) {
				
				if (e instanceof Damageable de && !e.equals(owner) && distance(e.getLocation(), owner.getLocation()) > 0.6) {
					BoundingBox box = e.getBoundingBox();
					boolean cancelled = false;
					if (box.overlaps(minVector, maxVector) || box.overlaps(minVector2, maxVector2)) {
						
						
						if (!cancelled)
							hit(null, de, i, loc);
					}
				}
			}
		}
	}
	
	public static double distance(Location a, Location b) {
		return Math.sqrt(Math.pow(a.getX() - b.getX(), 2) + Math.pow(a.getY() - b.getY(), 2) + Math.pow(a.getZ() - b.getZ(), 2));
	}
	
	public static List<Entity> getEntitiesAroundPoint(Location location, double radius) {
	    List<Entity> entities = new ArrayList<Entity>();
	    World world = location.getWorld();

	    // To find chunks we use chunk coordinates (not block coordinates!)
	    int smallX = (int)((location.getX() - radius) / 16.0D);
	    int bigX = (int)((location.getX() + radius) / 16.0D);
	    int smallZ = (int)((location.getZ() - radius) / 16.0D);
	    int bigZ = (int)((location.getZ() + radius) / 16.0D);

	    for (int x = smallX; x <= bigX; x++) {
	        for (int z = smallZ; z <= bigZ; z++) {
	            if (world.isChunkLoaded(x, z)) {
	                entities.addAll(Arrays.asList(world.getChunkAt(x, z).getEntities())); // Add all entities from this chunk to the list
	            }
	        }
	    }

	    // Remove the entities that are within the box above but not actually in the sphere we defined with the radius and location
	    // This code below could probably be replaced in Java 8 with a stream -> filter
	    Iterator<Entity> entityIterator = entities.iterator(); // Create an iterator so we can loop through the list while removing entries
	    while (entityIterator.hasNext()) {
	        if (entityIterator.next().getLocation().distanceSquared(location) > radius * radius) { // If the entity is outside of the sphere...
	            entityIterator.remove(); // Remove it
	        }
	    }
	    return entities;
	}
}
