package com.lerdorf.spaceships;

import org.bukkit.Color;
import org.bukkit.util.Vector;

public class Moon extends CosmicObject {
	
	public Planet planet;
	
	Vector orbitX = null;
	Vector orbitZ = null;
	
	public Moon(double x, double y, double z, double radius, String name, String type, Color color) {
		super(x, y, z, radius, name, type, color);
		this.orbitX = new Vector(1, 0, 0);
		this.orbitZ = new Vector(0, 0, 1);
	}
	
	
	public Moon(double x, double y, double z, double radius, String name, String type, Color color, Vector orbitX, Vector orbitZ) {
		super(x, y, z, radius, name, type, color);
		this.orbitX = orbitX.normalize();
		this.orbitZ = orbitZ.normalize();
	}
	
	public void setPlanet(Planet p) {
		this.planet = p;
		Vector planetPos = p.getPos();
		Vector dir = (new Vector(x-planetPos.getX(), y-planetPos.getY(), z-planetPos.getZ())).normalize();
		Vector hRight = dir.clone().rotateAroundAxis(new Vector(0, 1, 0), 90);
		hRight = hRight.setY(0);
		hRight = hRight.normalize();
		Vector pUp = dir.clone().rotateAroundAxis(hRight, 90);
		if (pUp.getY() < 0)
			pUp = pUp.multiply(-1);
		pUp = pUp.normalize();
		Vector pRight = dir.clone().rotateAroundAxis(pUp, 90);
		if (Math.abs(pRight.getX()) > 0.1) {
			orbitX = pRight;
			orbitZ = dir;
		} else {
			orbitX = dir;
			orbitZ = pRight;
		}
	}
}
