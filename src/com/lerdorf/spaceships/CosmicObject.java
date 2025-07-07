package com.lerdorf.spaceships;

import java.util.HashMap;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.World;

public class CosmicObject {

	public static HashMap<String, CosmicObject> destinations = new HashMap<>();
	
	public double x, y, z = 0;
	public double radius;
	public String name;
	public String type;
	public Color color;
	
	public CosmicObject() {
		
	}
	
	public CosmicObject(double x, double y, double z, double radius, String name, String type, Color color) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.radius = radius;
		this.name = name;
		this.type = type;
		this.color = color;
		destinations.put(name, this);
	}
	
	public Location getLocation(World world) {
		return new Location(world, x, y, z);
	}
	
	public double dist(Location loc) {
		return loc.distance(getLocation(loc.getWorld()));
	}
	
}
