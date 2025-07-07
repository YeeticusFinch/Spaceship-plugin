package com.lerdorf.spaceships;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class Spacemap {

	public CosmicObject[] objects;
	
	public static List<PRender> particles = new ArrayList<PRender>();
	
	class PRender {
		World world;
		double px;
		double py; 
		double pz;
		DustOptions dust;
		
		public PRender(World world, double px, double py, double pz, DustOptions dust) {
			this.world = world;
			this.px = px;
			this.py = py;
			this.pz = pz;
			this.dust = dust;
		}
		
		public void render() {
			world.spawnParticle(Particle.DUST, px, py, pz, 1, 0, 0, 0, 0, dust);
		}
	}
	
	public static void render() {
		if (particles.size() > 0)
			for (PRender pr : particles)
				pr.render();
	}
	
	public Spacemap() {
		objects = new CosmicObject[] {
			new Star(0, 400, 0, 200, "Sol", "Yellow Dwarf Star", Color.YELLOW),
			new Planet(1410, 400, -814, 6, "Mercury", "Planet", Color.GRAY, null),
			new Planet(-1543, 400, 2672, 15, "Venus", "Planet", Color.fromRGB(255, 255, 155), null),
			new Planet(-3022, 400, 3022, 16, "Earth", "Planet", Color.GREEN, new Moon[] { 
					new Moon(-3022, 400, 2061, 5, "Luna", "Moon", Color.GRAY) 
					}),
			new Planet(-5133, 400, 4010, 16, "Mars", "Planet", Color.ORANGE, new Moon[] { 
					new Moon(-5150, 400, 3996, 1, "Phobos", "Moon", Color.GRAY),
					new Moon(-5133, 400, 3952, 1, "Deimos", "Moon", Color.GRAY),
					}),
			new Planet(8461, 400, -8461, 2, "Ceres", "Dwarf Planet", Color.GRAY, null),
			new Planet(3862, 400, 21905, 178, "Jupiter", "Gas Giant", Color.RED, new Moon[] { 
					new Moon(4600, 405, 21152, 5, "Io", "Moon", Color.fromRGB(155, 200, 155)),
					new Moon(5535, 329, 21796, 4, "Europa", "Moon", Color.fromRGB(200, 200, 155)),
					new Moon(1809, 462, 23621, 7, "Ganymede", "Moon", Color.GRAY),
					new Moon(3863, 313, 17200, 6, "Callisto", "Moon", Color.GRAY),
					}),
			new Planet(40292, 400, -7104, 151, "Saturn", "Gas Giant", Color.fromRGB(255, 255, 155), new Moon[] { 
					new Moon(37248, 370, -7353, 13, "Titan", "Moon", Color.fromRGB(155, 155, 200)),
					new Moon(40432, 353, 1797, 4, "Iapetus", "Moon", Color.fromRGB(200, 200, 200)),
					new Moon(39323, 364, -7995, 4, "Rhea", "Moon", Color.GRAY),
					new Moon(40986, 436, -7742, 3, "Dione", "Moon", Color.GRAY),
					new Moon(39616, 323, -6824, 3, "Tethys", "Moon", Color.GRAY),
					}),
			new Planet(58588, 400, 58588, 63, "Uranus", "Gas Giant", Color.AQUA, new Moon[] { 
					new Moon(58917, 81, 58497, 3, "Ariel", "Moon", Color.fromRGB(155, 155, 200)),
					new Moon(58036, 739, 58737, 3, "Umbriel", "Moon", Color.fromRGB(100, 100, 100)),
					new Moon(57498, 382, 58580, 4, "Titania", "Moon", Color.GRAY),
					new Moon(60047, 365, 58595, 4, "Oberon", "Moon", Color.fromRGB(100, 100, 100)),
					}),
			new Planet(128571, 400, 0, 62, "Neptune", "Gas Giant", Color.BLUE, new Moon[] { 
					new Moon(128089, 758, 653, 7, "Triton", "Moon", Color.fromRGB(250, 250, 200)),
					new Moon(114791, 183, -226, 1, "Nereid", "Moon", Color.GRAY),
					}),
			new Planet(108355, 400, -129133, 3, "Pluto", "Planet", Color.fromRGB(155, 155, 155), null),
		};
	}
	
	public void updateMap(Location loc, float zoom) {
		if (objects != null && objects.length > 0) {

			World world = loc.getWorld();
			boolean playerNear = false;
			HashMap<Player, CosmicObject> nearbyPlayers = new HashMap<>();
			HashMap<Player, Double> listDiff = new HashMap<>();
			for (Player p : world.getPlayersSeeingChunk(loc.getChunk())) {
				if (p.getLocation().distance(loc) < 8) {
					playerNear = true;
					nearbyPlayers.put(p, null);
					//break;
				}
			}
			
			if (!playerNear)
				return;
			
			//CosmicObject listPlanet = null;
			//double listPlanetDiff = -1;
			//double maxD = 5.906423*Math.pow(10,8);
			double maxD = 0;
			
			for (CosmicObject o : objects)
				if (o.dist(loc) > maxD)
					maxD = o.dist(loc);
			maxD /= zoom;
			int x = loc.getBlockX();
			int y = loc.getBlockY();
			int z = loc.getBlockZ();
			double h = 1.4;
			
			for (CosmicObject o : objects) {
				
				float angle = (float)(Math.atan(((float)o.z)/((float)o.x))*180/Math.PI);
				if (o.x > 0)
					angle += 180;
				int dist = (int)Math.sqrt((((double)o.x)*o.x) + Math.pow((double)o.y-400, 2) + ((double)o.z)*o.z);
				
				// Render the trail
					Color trailColor = Color.fromARGB(100, Math.max(o.color.getRed()/2, 20), Math.max(o.color.getGreen()/2, 20), Math.max(o.color.getBlue()/2, 20));
					int a = 0;
					int step = dist > 30000 ? 1 : 3;
					for (float i = angle+step; i < angle + step * 40; i+=step) {
						a++;
						DustOptions dust = new DustOptions(trailColor, (float)Math.max(Math.max(o.radius/1000f, 0.05f) * (0.5f - 0.008f*a), 0.01f));
						
						double ox = -dist*Math.cos(i*Math.PI/180);
						double oy = o.y;
						double oz = -dist*Math.sin(i*Math.PI/180);
						
						double px = x + 0.5 + (h * (ox-x))/(maxD);
						double py = y + h*2*0.707 + 0.1 + (h * (oy-y))/(maxD);
						double pz = z + 0.5 + (h * (oz-z))/(maxD);
						//if (a < 2 && o.name.contains("Earth")) {
						//	Bukkit.broadcastMessage("Earth trail at " + (px-x) + " " + (py-y) +" "+ (pz-z) + " Size: " + Math.max((float)o.radius/1000f, 0.05f) * (0.7f - 0.008f*a));
						//}
						//double py = y + h*2*0.707 + 0.1 + (h * ((oy-y)*0.707+(oz-z)*0.707))/(maxD);
						//double pz = z + 0.5 + (h * ((oz-z)*0.707-(oy-y)*0.707))/(maxD);
						if (dist(x, y+h*2*0.707, z, px, py, pz) < 5)
							particle(world, px, py, pz, dust);
						else
							continue;
					}
					
				
				
				// Render the planet itself
				DustOptions dust = new DustOptions(o.color, (float)Math.max(o.radius/1000f, 0.05f));
				
				double px = x + 0.5 + (h * (o.x-x))/(maxD);
				double py = y + h*2*0.707 + 0.1 + (h * (o.y-y))/(maxD);
				double pz = z + 0.5 + (h * (o.z-z))/(maxD);
				//double py = y + h*2*0.707 + 0.1 + (h * ((o.y-y)*0.707+(o.z-z)*0.707))/(maxD);
				//double pz = z + 0.5 + (h * ((o.z-z)*0.707-(o.y-y)*0.707))/(maxD);
				if (dist(x, y, z, px, py, pz) < 5)
					particle(world, px, py, pz, dust);
				else
					continue;
                
				
				if (o instanceof Planet planet && planet.moons != null && planet.moons.length > 0) {
					for (Moon moon : planet.moons) {
						double mpx = x + 0.5 + (h * (moon.x-x))/(maxD);
						double mpy = y + h*2*0.707 + 0.1 + (h * (moon.y-y))/(maxD);
						double mpz = z + 0.5 + (h * (moon.z-z))/(maxD);
						
						if (dist(px, py, pz, mpx, mpy, mpz) > 0.05 && dist(x, y, z, mpx, mpy, mpz) < 5) { // If the moon is far enough away to be worth rendering
							DustOptions moonDust = new DustOptions(moon.color, (float)Math.max(moon.radius/1000f, 0.05f));
							particle(world, mpx, mpy, mpz, moonDust);
							
							int moonDist = (int)Math.sqrt(Math.pow((double)moon.x - (double)planet.x, 2) + Math.pow((double)moon.y - (double)planet.y, 2) + Math.pow((double)moon.z - (double)planet.z, 2));
			                Color moonTrailColor = Color.fromARGB(100, Math.max(moon.color.getRed()/2, 20), Math.max(moon.color.getGreen()/2, 20), Math.max(moon.color.getBlue()/2, 20));
			                int moonstep = (moonDist > 100000 ? 2 : 8);
			                for (float i = moonstep; i < 360; i+= moonstep) {
								DustOptions trailDust = new DustOptions(moonTrailColor, Math.max(Math.max((float)moon.radius/1000f, 0.05f) * 0.1f, 0.01f));
								
								Vector trailPos = moon.orbitX.clone().multiply(moonDist*Math.cos(i*Math.PI/180)).add(moon.orbitZ.clone().multiply(moonDist*Math.sin(i*Math.PI/180)));
								
								double ox = trailPos.getX() + (double)planet.x;
								double oy = trailPos.getY() + (double)planet.y;
								double oz = trailPos.getZ() + (double)planet.z;
								
								double pxt = x + 0.5 + (h * (ox-x))/(maxD);
								double pyt = y + h*2*0.707 + 0.1 + (h * (oy-y))/(maxD);
								double pzt = z + 0.5 + (h * (oz-z))/(maxD);
								//if (i < 2) {
								//	Bukkit.broadcastMessage(moon.name + " trail at " + (pxt-x) + " " + (pyt-y) +" "+ (pzt-z) + " Size: " + Math.max(Math.max((float)moon.radius/1000f, 0.05f) * (0.5f), 0.01f) + " dist: " + moonDist + " moon.x: " + moon.x + " moon.z: " + moon.z + " dist2: " + (int)Math.sqrt(Math.pow((double)moon.x - (double)planet.x, 2) + Math.pow((double)moon.y - (double)planet.y, 2) + Math.pow((double)moon.z - (double)planet.z, 2)));
								//}
								//double py = y + h*2*0.707 + 0.1 + (h * ((oy-y)*0.707+(oz-z)*0.707))/(maxD);
								//double pz = z + 0.5 + (h * ((oz-z)*0.707-(oy-y)*0.707))/(maxD);
								if (dist(x, y+h*2*0.707, z, pxt, pyt, pzt) < 5)
									particle(world, pxt, pyt, pzt, trailDust);
								else
									continue;
									
							}
							
							if (playerNear) {
			                	for (Player player : nearbyPlayers.keySet()) {
			                		Vector dir = player.getEyeLocation().getDirection();
			                    	Vector dir2 = getDir(player.getEyeLocation().getX(), player.getEyeLocation().getY(), player.getEyeLocation().getZ(), mpx, mpy, mpz );
			                    	double diff = dist( dir.getX(), dir.getY(), dir.getZ(), dir2.getX(), dir2.getY(), dir2.getZ() );
			                    	if ((nearbyPlayers.get(player) == null || diff < listDiff.get(player)) && diff < 0.5) {
			                    		nearbyPlayers.put(player, moon);
			                    		listDiff.put(player, diff);
			                    	}
			                	}
			                }
						}
					}
				}
				
				//System.out.println("dust for " + p.name + " at " + p.x + " " + p.y + " " + p.z);
                if (playerNear) {
                	for (Player player : nearbyPlayers.keySet()) {
                		Vector dir = player.getEyeLocation().getDirection();
                    	Vector dir2 = getDir(player.getEyeLocation().getX(), player.getEyeLocation().getY(), player.getEyeLocation().getZ(), px, py, pz );
                    	
                    	
                    	double diff = dist( dir.getX(), dir.getY(), dir.getZ(), dir2.getX(), dir2.getY(), dir2.getZ() );
                    	if ((nearbyPlayers.get(player) == null || diff < listDiff.get(player)) && diff < 0.5) {
                    		nearbyPlayers.put(player, o);
                    		listDiff.put(player, diff);
                    		//listPlanetDiff = diff;
                    	}
                	}
                	
                }
                
                /*
	                int dist = (int)Math.sqrt((((double)o.x)*o.x) + Math.pow((double)o.y-400, 2) + ((double)o.z)*o.z);
	                Color trailColor = Color.fromRGB(Math.max(o.color.getRed()/2, 20), Math.max(o.color.getGreen()/2, 20), Math.max(o.color.getBlue()/2, 20));
	                for (float i = 0; i < 360; i+= (dist > 100000 ? 2 : 10)) {
						DustOptions trailDust = new DustOptions(trailColor, Math.max(Math.max((float)o.radius/1000f, 0.05f) * (0.5f), 0.01f));
						
						double ox = -dist*Math.cos(i*Math.PI/180);
						double oy = o.y;
						double oz = -dist*Math.sin(i*Math.PI/180);
						
						double pxt = x + 0.5 + (h * ox-x)/(maxD);
						double pyt = y + h*2*0.707 + 0.1 + (h * (oy-y))/(maxD);
						double pzt = z + 0.5 + (h * oz-z)/(maxD);
						//if (i < 2) {
						//	Bukkit.broadcastMessage(o.name + " trail at " + (pxt-x) + " " + (pyt-y) +" "+ (pzt-z) + " Size: " + Math.max(Math.max((float)o.radius/1000f, 0.05f) * (0.5f), 0.01f) + " dist: " + dist + " o.x: " + o.x + " o.z: " + o.z + " dist2: " + Math.sqrt((((double)o.x)*o.x) + Math.pow((double)o.y-400, 2) + ((double)o.z)*o.z));
						//}
						//double py = y + h*2*0.707 + 0.1 + (h * ((oy-y)*0.707+(oz-z)*0.707))/(maxD);
						//double pz = z + 0.5 + (h * ((oz-z)*0.707-(oy-y)*0.707))/(maxD);
						if (true || dist(x, y+h*2*0.707, z, pxt, pyt, pzt) < 5)
							particle(world, pxt, pyt, pzt, trailDust);
						else
							continue;
							
					}*/
                
			}
			if (nearbyPlayers.size() > 0) {
				for (Player player : nearbyPlayers.keySet()) {
					if (nearbyPlayers.get(player) != null) {
						CosmicObject listObj = nearbyPlayers.get(player);
						String info = "";
						if (listObj.type != null) info += " [§6" + listObj.type + "§f]";
						if (listObj.name != null) info += " '§d" + listObj.name + "§f'";
						//if (listObj.id != null) info += " [§b" + listPlanet.id + "§f]";
						//if (listObj.habitable) info += " [habitable]";
						player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(info));
					}
				}
				
			}
		}
	}
	
	void particle(World world, double px, double py, double pz, DustOptions dust) {
		
		/*world.spawnParticle(Particle.DUST, px, py, pz, 0, 0, 0, 0, dust);
		
		Bukkit.getScheduler().runTaskLater(MainPlugin.instance, () -> {
    		//physicsImmune.clear();
			world.spawnParticle(Particle.DUST, px, py, pz, 0, 0, 0, 0, dust);
        }, 1L); // Delay of 1 tick
        */
		particles.add(new PRender(world, px, py, pz, dust));
        
	}
	
	Vector getDir(double x, double y, double z, double x2, double y2, double z2) {
		double mag = dist(x, y, z, x2, y2, z2);
		return new Vector((x2-x)/mag, (y2-y)/mag, (z2-z)/mag);
	}
	
	double dist(double x, double y, double z, double x2, double y2, double z2) {
		return Math.sqrt( Math.pow(x-x2,2) + Math.pow(y-y2,2) + Math.pow(z-z2,2) );
	}
}
