package com.elmakers.mine.bukkit.spell.builtin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.UndoableSpell;
import com.elmakers.mine.bukkit.utility.InventoryUtils;
import com.elmakers.mine.bukkit.utility.Target;

public class GotoSpell extends UndoableSpell
{
	LivingEntity targetEntity = null;
	int playerIndex = 0;
	private Color effectColor = null;
	
	@Override
	public SpellResult onCast(ConfigurationSection parameters) 
	{
		Player player = mage.getPlayer();
		if (player == null) 
		{
			return SpellResult.PLAYER_REQUIRED;
		}
		effectColor = mage.getEffectColor();
		if (effectColor == null) 
		{
			effectColor = Color.fromRGB(Integer.parseInt(parameters.getString("effect_color", "FF0000"), 16));
		}
		
		if (targetEntity != null && targetEntity instanceof LivingEntity)
		{
			if (!mage.isSuperPowered() || !targetEntity.isValid() || targetEntity.isDead())
			{
				releaseTarget();
			} 
			
			// Check for protected Mages
			if (targetEntity != null && controller.isMage(targetEntity)) {
				Mage targetMage = controller.getMage(targetEntity);
				// Check for protected players (admins, generally...)
				if (targetMage.isSuperProtected()) {
					releaseTarget();
				}
			}
		}
		
		// Totally different behavior for superpowered mages...
		if (!mage.isSuperPowered())
		{
			Location location = getLocation();
			List<Target> allTargets = new LinkedList<Target>();
			List<Player> players = player.getWorld().getPlayers();
			for (Player targetPlayer : players) {
				if (targetPlayer == player) continue;
				if (targetPlayer.hasPotionEffect(PotionEffectType.INVISIBILITY)) continue;
                if (controller.isNPC(targetPlayer)) continue;
				allTargets.add(new Target(location, targetPlayer, 512, Math.PI));
			}
			if (allTargets.size() == 0) return SpellResult.NO_TARGET;
			
			registerMoved(player);
			registerForUndo();
			
			Collections.sort(allTargets);
			Entity targetEntity = allTargets.get(0).getEntity();
			getCurrentTarget().setEntity(targetEntity);
			registerModified(player);
			teleportTo(player, targetEntity);
			castMessage(getMessage("cast_to_player").replace("$target", controller.getEntityName(targetEntity)));
			
			return SpellResult.CAST;
		}
		
		if (!isLookingUp() && !isLookingDown()) {
			Target target = getTarget();
			
			if (targetEntity != null) {
				return teleportTarget(target.getLocation()) ? SpellResult.CAST : SpellResult.NO_TARGET;
			}
			
			if (!target.hasEntity() || !(target.getEntity() instanceof LivingEntity))
			{
				return SpellResult.NO_TARGET;
			}
		
			// Check for protected Mages
			if (controller.isMage(targetEntity)) {
				Mage targetMage = controller.getMage(targetEntity);
				// Check for protected players (admins, generally...)
				if (targetMage.isSuperProtected()) {
					return SpellResult.NO_TARGET;
				}
			}

			selectTarget((LivingEntity)target.getEntity());
			return SpellResult.TARGET_SELECTED;
		}
		
		if (isLookingUp() && targetEntity != null)
		{
			getCurrentTarget().setEntity(targetEntity);
			registerModified(player);
			teleportTo(player, targetEntity);
			castMessage(getMessage("cast_to_player").replace("$target", controller.getEntityName(targetEntity)));
			releaseTarget();
			registerForUndo();
			return SpellResult.CAST;
		}
		
		if (targetEntity != null) {
			releaseTarget();
			return SpellResult.CANCEL;
		}

		List<String> playerNames = new ArrayList<String>(controller.getPlayerNames());
		if (playerNames.size() == 1) return SpellResult.NO_TARGET;
		
		if (playerIndex < 0) playerIndex = playerNames.size() - 1;
		if (playerIndex >= playerNames.size()) {
			playerIndex = 0;
		}
		
		String playerName = playerNames.get(playerIndex);
		if (player != null && playerName.equals(player.getName())) {
			playerIndex = (playerIndex + 1) % playerNames.size();
			playerName = playerNames.get(playerIndex);
		}
		playerIndex++;
		
		Player targetPlayer = Bukkit.getPlayer(playerName);
		if (targetPlayer == null) return SpellResult.NO_TARGET;
		
		selectTarget(targetPlayer);
		return SpellResult.TARGET_SELECTED;
	}
	
	protected boolean teleportTarget(Location location) {
		if (targetEntity == null || location == null) return false;
		registerMoved(targetEntity);
		location.setY(location.getY() + 1);
		targetEntity.teleport(location);
		this.getCurrentTarget().setEntity(targetEntity);
		registerForUndo();
		
		return true;
	}
	
	protected void teleportTo(Entity sourceEntity, Entity targetEntity) {
		Location targetLocation = targetEntity.getLocation();
		
		// Try to place you in front of the other player, and facing them
		BlockFace targetFacing = getFacing(targetEntity.getLocation());
		Location candidate = findPlaceToStand(targetLocation.getBlock().getRelative(targetFacing).getRelative(targetFacing).getLocation(), 4, 4);
		if (candidate != null) {
			candidate.setPitch(0);
			candidate.setYaw(360 - targetLocation.getYaw());
			targetLocation = candidate;
		}

        sourceEntity.teleport(targetLocation);
	}
	
	protected void selectTarget(LivingEntity entity) {
		releaseTarget();

		targetEntity = entity;
		getCurrentTarget().setEntity(entity);

		if (effectColor != null) {
			InventoryUtils.addPotionEffect(targetEntity, effectColor);
		}
	}
	
	protected void releaseTarget() {
		if (targetEntity != null && effectColor != null) {
			InventoryUtils.clearPotionEffect(targetEntity);
		}
		targetEntity = null;
	}

	@Override
	public boolean onCancel()
	{
		if (targetEntity != null)
		{
            releaseTarget();
			return true;
		}
		
		return false;
	}
}
