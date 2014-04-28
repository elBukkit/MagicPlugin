package com.elmakers.mine.bukkit.spell.builtin;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.spell.TargetingSpell;
import com.elmakers.mine.bukkit.utility.InventoryUtils;
import com.elmakers.mine.bukkit.utility.Target;

public class GotoSpell extends TargetingSpell
{
	LivingEntity targetEntity = null;
	int playerIndex = 0;
	private Color effectColor = null;
	
	@Override
	public SpellResult onCast(ConfigurationSection parameters) 
	{
		effectColor = mage.getEffectColor();
		if (effectColor == null) {
			effectColor = Color.fromRGB(Integer.parseInt(parameters.getString("effect_color", "FF0000"), 16));
		}
		
		if (targetEntity != null && targetEntity instanceof LivingEntity)
		{
			if (!targetEntity.isValid() || targetEntity.isDead())
			{
				releaseTarget();
			} 
			
			// Check for protected Mages
			if (targetEntity != null && targetEntity instanceof Player) {
				Mage targetMage = controller.getMage((Player)targetEntity);
				// Check for protected players (admins, generally...)
				if (targetMage.isSuperProtected()) {
					releaseTarget();
				}
			}
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
			if (targetEntity instanceof Player) {
				Mage targetMage = controller.getMage((Player)targetEntity);
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
			getPlayer().teleport(targetEntity.getLocation());
			castMessage(getMessage("cast_to_player").replace("$target", getTargetName(targetEntity)));
			releaseTarget();
			return SpellResult.CAST;
		}
		
		if (targetEntity != null) {
			releaseTarget();
			return SpellResult.CANCEL;
		}

		List<String> playerNames = new ArrayList<String>(controller.getPlayerNames());
		if (playerNames.size() == 0) return SpellResult.NO_TARGET;
		
		if (playerIndex < 0) playerIndex = playerNames.size() - 1;
		if (playerIndex >= playerNames.size()) {
			playerIndex = 0;
			releaseTarget();
			return SpellResult.TARGET_SELECTED;
		}
		
		Player player = getPlayer();
		String playerName = playerNames.get(playerIndex);
		if (player != null && playerName.equals(player.getName())) {
			if (playerNames.size() == 1) return SpellResult.NO_TARGET;
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
		location.setY(location.getY() + 1);
		targetEntity.teleport(location);
		this.getCurrentTarget().setEntity(targetEntity);
		
		return true;
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
