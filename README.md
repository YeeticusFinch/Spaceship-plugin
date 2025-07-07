# Spaceship-plugin

See a video demo here: https://youtu.be/h2YXaXAZbhc?si=ibKN7An3pg3Aj4Py

This is a remake of my [VoidShips plugin](https://github.com/YeeticusFinch/VoidShips), but it was simplified a bit, and it uses [MoveCraft](https://github.com/APDevTeam/Movecraft).
This means that ships can actually move around (and not just virtually like in VoidShips).

## Features

### Moving Ships

Just like in the VoidShips plugin, ships are built out of blocks. Rather than having all of that virtual navigation through the different solar systems and stars and planets that you never get to actually see other than through particles and text, this plugin focuses on ships traveling through the Minecraft world.

#### Isn't that just MoveCraft with extra steps?

Yes, this plugin is heavily integrated with MoveCraft, with a couple of changes and additions. This plugin adds custom block-entities, including pilot seats, radar dishes, turrets, maps, custom items, and of course, planets (with custom dimensions for each one.

### Custom Block-Entities

#### Pilot Seats

![image](https://github.com/user-attachments/assets/0abec996-473f-40c3-a3bb-d5e9a97a377c)

#### Radar (transceivers)

![image](https://github.com/user-attachments/assets/2219bdbe-64d0-4083-b7fb-2d4c18ec3013)

Radars (referred to as transceivers in the codebase) place the ship on the map, and it also shows other ships where your ship is. A ship with an active transceiver can see the locations of all other ships with active transceivers.

#### Sensors

Sensors are just redstone lamps (actually an invisible armorstand with a redstone lamp on its head). The lamp will turn on if it reads an air pressure, and it will turn off if its in a vacuum. This lets players see which parts of their ship are pressurized and safe to enter without a spacesuit.

#### Turrets

![image](https://github.com/user-attachments/assets/aa82275d-7d22-4319-b3b4-116c2b8fefb3) ![image](https://github.com/user-attachments/assets/bdc0db74-3116-43f9-8ecd-014d27ec1dce)

### Orbital Maps

Maps represent the solar system, centered on your current location. Players can use the scroll wheel to zoom in or zoom out on the map. Zooming in will increase the resolution. If you zoom in enough, you can see the moons orbiting planets. Planets are based on their Minecraft location in the Space dimension.

![image](https://github.com/user-attachments/assets/7175524f-8776-471a-a11f-029e6bd2533d)
The entire solar system, zoomed out.

![image](https://github.com/user-attachments/assets/73595687-b306-4114-a908-3a61219e0d7a)
Earth and the moon orbiting around, with Venus and Mars nearby.

![image](https://github.com/user-attachments/assets/336f0168-bfee-4247-afe8-15a1efedd0f8)
Jupiter with 4 moons orbiting around.

![image](https://github.com/user-attachments/assets/93cfdf84-a56a-4e49-8fd4-73bcee3da6f5)
Uranus with its 2 moons.

### Planets

Planets are in the Space dimension, which has a higher build-height to accommodate some of the bigger planets.

#### Mercury
![image](https://github.com/user-attachments/assets/77afdc1a-7079-4aae-98c1-9a44eea62687)
The surface of Mercury.

### Venus
![image](https://github.com/user-attachments/assets/d9f5f375-78e2-4be4-97ad-52a2344f0daf)
The surface of Venus, with floating islands above. I decided to add islands floating high in Venus's thick atmosphere.

#### The Moon
![image](https://github.com/user-attachments/assets/37084c3f-fb35-47e3-831e-3157a1d56586)
Ok, this isn't a planet, but it's orbiting one!

#### Mars

![image](https://github.com/user-attachments/assets/ec6c9772-a6e3-4a6d-a516-dd3a1284a305)
The surface of Mars.

![image](https://github.com/user-attachments/assets/e1974de2-fd0c-4f37-9c5c-2e38d72a2a09)
The moons of Mars. Unfortunately I couldn't make these to scale. That's not a full block, that's actually a player head, but it's still 10x bigger than it should be if it was to scale.

#### Saturn

![image](https://github.com/user-attachments/assets/98772b36-95ac-4f1b-82b6-bcda76303e61)
Here is Saturn, with one of the smaller moons (yes it's just one block, it's to scale).

#### Uranus

![image](https://github.com/user-attachments/assets/e38153cf-839a-42fd-83b0-6a4bd8d4ce4a) ![image](https://github.com/user-attachments/assets/3ebadba5-d15c-4ce4-a5e6-d28efdd3d7c2)

Uranus with two of its moons.

#### Neptune

![image](https://github.com/user-attachments/assets/115f81b6-e83d-4a54-8057-c2166733d860)
Neptune with one of its moons.

### Challenges

The main challenge was to make all the entities follow the Movecraft ships as they move. For linear movement that isn't so bad, just teleport all entities within the ship a displacement equal to the ship's displacement each time it moves. MoveCraft gives us move events to facilitate this. 

However, a lot of these entities can have a player sitting on them. Players can sit on the chairs, and players can sit inside turrets. 

Fun fact: an entity can't teleport if a player is sitting on it. So the solution was to use some delayed tasks to dismount the player, teleport both the player and the entity, then remount the player. I ended up making a custom teleport function that automatically does this.

The next challenge was for rotating the ship. When a ship rotates, all entities should move and rotate to their proper positions. Problem is, they have to spin around the ship's axis of rotation (which is also a MoveCraft variable). However, this isn't as simple as rotating a vector around a point. Minecraft's block coordinates are discretized, meaning that when a ship rotates, it's not as simple as rotating around a center coordinate. If your ship is 4 blocks wide, the center point is in-between 2 blocks, it doesn't fall on any block. And to make it even more annoying, coordinates fall on corners of blocks. <0, 0, 0> is the corner of a block. The center location of a block is <0.5, 0.5, 0.5>. 

TLDR to rotate entities with the ship I couldn't just rotate a vector around a point, because then an entity that is on a block will be moved to the corner of that block, and then upon the next rotation it will move to the center of another block, moving diagonally by half a block each rotation until it has left your ship entirely. To solve this, I had to come up with a function that uses floor and ceiling functions to round the coordinate numbers and then add 0.5 to keep it in the center of the original block.

## Why don't I see any releases?

Good question! That's because this project is currently split across a plugin and a datapack, and it's still a Work In Progress. Also, the Space dimension with all the planets was manually created (I haven't figured out how to get that to create itself automatically, seeing that it needs a much larger build height to fit those planets).

Also, the resourcepack is mixed in with my general server resourcepack, I haven't yet separated the parts that are unique to this project (hey I reuse a lot of assets)
