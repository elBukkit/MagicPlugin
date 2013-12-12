package com.elmakers.mine.bukkit.utilities;

import java.lang.reflect.Method;

import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.meta.FireworkMeta;

public class FireworkUtils extends NMSUtils {
	
	public static void spawnFireworkEffect(Location location, FireworkEffect effect, int power) {
		World world = location.getWorld();
		
		try {
			// Initialize the Firework object
			Firework firework = world.spawn(location, Firework.class);
			FireworkMeta meta = firework.getFireworkMeta();
			meta.addEffect(effect);
			meta.setPower(power);
	        firework.setFireworkMeta(meta);
	        
	        // Use the firework entity to spawn an effect
			Object worldHandle = getHandle(world);
			Object fireworkHandle = getHandle(firework);
			Method broadcastMethod = class_World.getMethod("broadcastEntityEffect", class_Entity, Byte.TYPE);
			
			// 17 is a magic number from EntityFireworks.. I do not know what it means :\
			broadcastMethod.invoke(worldHandle,  fireworkHandle, (byte)17);
			
			// Remove the firework, we only needed it for the effec.t
			firework.remove();
		} catch (Exception ex) {
			
		}
	}
	
}
