package com.lerdorf.spaceships;

import org.bukkit.Color;
import org.bukkit.util.Vector;

public class Planet extends CosmicObject {
	Moon[] moons;
	
	public Planet(double x, double y, double z, double radius, String name, String type, Color color, Moon[] moons) {
		super(x, y, z, radius, name, type, color);
		this.moons = moons;
		if (moons != null)
			for (Moon m : this.moons)
				m.setPlanet(this);
	}
	
	public Vector getPos() {
		return new Vector(x, y, z);
	}
}
