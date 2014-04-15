package com.elmakers.mine.bukkit.effects;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.meta.FireworkMeta;

import com.elmakers.mine.bukkit.api.magic.NMSUtils;

public class EffectUtils extends NMSUtils {
	
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
	
	public static void playEffect(Location location, ParticleType particleType, float xOffset, float yOffset, float zOffset, float effectSpeed, int particleCount) {
		playEffect(location, particleType, "", xOffset, yOffset, zOffset, effectSpeed, particleCount);
	}
	
	public static void playEffect(Location location, ParticleType particleType, float effectSpeed, int particleCount) {
		playEffect(location, particleType, "", 0, 0, 0, effectSpeed, particleCount);
	}
	
	public static void playEffect(Location location, ParticleType particleType, String subtype, float xOffset, float yOffset, float zOffset, float effectSpeed, int particleCount) {
		try {
			Object packet = null;
			if (class_PacketPlayOutWorldParticles != null) {
				Constructor<?> packetConstructor = class_PacketPlayOutWorldParticles.getConstructor();
				packet = packetConstructor.newInstance();			
			} else {
				// Fall back to previous versions
				Constructor<?> packetConstructor = class_Packet63WorldParticles.getConstructor();
				packet = packetConstructor.newInstance();						
			}
			for (Field field : packet.getClass().getDeclaredFields())
	        {
                field.setAccessible(true);
                String fieldName = field.getName();
                if (fieldName.equals("a")) {
                	field.set(packet, particleType.getParticleName(subtype));
                } else if (fieldName.equals("b")) {
                	field.setFloat(packet, (float)location.getX());
                } else if (fieldName.equals("c")) {
                	field.setFloat(packet, (float)location.getY());
                } else if (fieldName.equals("d")) {
                	field.setFloat(packet, (float)location.getZ());
                } else if (fieldName.equals("e")) {
                	field.setFloat(packet, xOffset);
                } else if (fieldName.equals("f")) {
                	field.setFloat(packet, yOffset);
                } else if (fieldName.equals("g")) {
                	field.setFloat(packet, zOffset);
                } else if (fieldName.equals("h")) {
                	field.setFloat(packet, effectSpeed);
                } else if (fieldName.equals("i")) {
                	field.setInt(packet, particleCount);
                }
	        }
			sendPacket(location, null, packet);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
