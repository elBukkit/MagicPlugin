package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.block.MaterialBrush;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.BaseSpell;
import com.elmakers.mine.bukkit.action.CompoundAction;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.Collection;

public class IterateAction extends CompoundAction
{
	private int	DEFAULT_SIZE = 16;
    private boolean incrementData;
    private int radius;
    private int size;
    private boolean reverse;
    private boolean requireBlock;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters)
    {
        super.prepare(context, parameters);
        incrementData = parameters.getBoolean("increment_data", false);
        radius = parameters.getInt("radius", 0);
        size = parameters.getInt("size", DEFAULT_SIZE);
        reverse = parameters.getBoolean("reverse", false);
        requireBlock = parameters.getBoolean("require_block", false);
    }

	@SuppressWarnings("deprecation")
	@Override
    public SpellResult perform(CastContext context)
    {
        Mage mage = context.getMage();
		int size = (int)(mage.getConstructionMultiplier() * (float)this.size);
        //int radius = (int)(this.radius * mage.getRadiusMultiplier());
        Location startLocation = context.getEyeLocation();
        Location targetLocation = context.getTargetLocation();

		int iterateBlocks = (int)startLocation.distance(targetLocation);
        if (iterateBlocks <= 0) return SpellResult.NO_TARGET;
		iterateBlocks = Math.min(iterateBlocks, size);

		Vector targetLoc = new Vector(targetLocation.getX(), targetLocation.getY(), targetLocation.getZ());
		Vector playerLoc = startLocation.toVector();

		Vector aim = null;
		if (reverse) {
			aim = playerLoc;
			aim.subtract(targetLoc);
			aim.normalize();
		} else {
			aim = targetLoc;
			aim.subtract(playerLoc);
			aim.normalize();
			
			targetLoc = playerLoc;

			// Move out a bit for safety!
			targetLoc.add(aim);
			targetLoc.add(aim);
		}

		MaterialBrush buildWith = context.getBrush();
		buildWith.setTarget(targetLocation);
		buildWith.update(mage, targetLocation);
        CastContext actionContext = createContext(context);
        SpellResult result = SpellResult.NO_ACTION;
		for (int dr = 0; dr <= radius; dr++) {
			int spokes = 1;
			// TODO: Handle radius > 1 algorithmically....
			if (dr > 0) {
				// 8, 16, 24, 32
				spokes = dr * 8;
			}
			for (int dspoke = 0; dspoke < spokes; dspoke++) {
				Vector currentLoc = targetLoc.clone();
				if (dr > 0) {
					// Arbitrary axis rotation would be better, but... math is hard! :P
					// TODO: Arbitrary axis rotation.
					double q = (double)dspoke * Math.PI * 2 / spokes;
					if (aim.getY() > 0.7) {
						Vector axis = new Vector(1, 0 ,0);
						Vector perp = aim.clone().crossProduct(axis).multiply(dr);

						double x = perp.getZ() * Math.sin(q) - perp.getX() * Math.cos(q);
						double z = perp.getZ() * Math.cos(q) - perp.getX() * Math.sin(q);
						perp.setX(x);
						perp.setZ(z);
						
						currentLoc.add(perp);
					} else if (aim.getX() > 0.7) {
						Vector axis = new Vector(0, 1 ,0);
						Vector perp = aim.clone().crossProduct(axis).multiply(dr);
						
						double y = perp.getZ() * Math.sin(q) - perp.getY() * Math.cos(q);
						double z = perp.getZ() * Math.cos(q) - perp.getY() * Math.sin(q);
						perp.setY(y);
						perp.setZ(z);

						currentLoc.add(perp);
					} else {
						Vector axis = new Vector(0, 1 ,0);
						Vector perp = aim.clone().crossProduct(axis).multiply(dr);
						
						double y = perp.getX() * Math.sin(q) - perp.getY() * Math.cos(q);
						double x = perp.getX() * Math.cos(q) - perp.getY() * Math.sin(q);
						perp.setY(y);
						perp.setX(x);

						currentLoc.add(perp);
					}
				}
				for (int i = 0; i < iterateBlocks; i++)
				{
					Block currentTarget = targetLocation.getWorld().getBlockAt(currentLoc.getBlockX(), currentLoc.getBlockY(), currentLoc.getBlockZ());
					if (!context.isTargetable(currentTarget.getType()) && context.isDestructible(currentTarget) && context.hasBuildPermission(currentTarget))
					{
						buildWith.update(mage, currentTarget.getLocation());
		
						if (incrementData) {
							short data = i > 15 ? 15 : (short)i;
							buildWith.setData(data);
						}

                        if (requireBlock) {
                            Block lowerBlock = currentTarget.getRelative(BlockFace.DOWN);
                            if (lowerBlock.getType() == Material.AIR || lowerBlock.getType() == buildWith.getMaterial()) {
                                currentLoc.add(aim);
                                continue;
                            }
                        }
                        actionContext.setTargetLocation(currentTarget.getLocation());
                        result = result.min(performActions(actionContext));
						if (dr == 0 && usesBrush()) {
                            Location effectLocation = currentTarget.getLocation();
                            effectLocation.add(0.5f, 0.5f, 0.5f);
							Material material = buildWith.getMaterial();
							// Kinda hacky.
							// TODO: Customize with effects system
							if (material == Material.AIR) {
                                effectLocation.getWorld().playEffect(effectLocation, Effect.STEP_SOUND, Material.OBSIDIAN.getId());
							} else {
								effectLocation.getWorld().playEffect(effectLocation, Effect.STEP_SOUND, material.getId());
							}
						}
					}					
					currentLoc.add(aim);
				}
			}
		}

		return result;
	}

    @Override
    public boolean requiresTarget() {
        return true;
    }

    @Override
    public void getParameterNames(Collection<String> parameters) {
        super.getParameterNames(parameters);
        parameters.add("radius");
        parameters.add("size");
        parameters.add("increment_data");
        parameters.add("require_block");
        parameters.add("reverse");
    }

    @Override
    public void getParameterOptions(Collection<String> examples, String parameterKey) {
        if (parameterKey.equals("increment_data") || parameterKey.equals("reverse") || parameterKey.equals("require_block")) {
            examples.addAll(Arrays.asList((BaseSpell.EXAMPLE_BOOLEANS)));
        } else if (parameterKey.equals("radius") || parameterKey.equals("size")) {
            examples.addAll(Arrays.asList((BaseSpell.EXAMPLE_SIZES)));
        } else {
            super.getParameterOptions(examples, parameterKey);
        }
    }
}
