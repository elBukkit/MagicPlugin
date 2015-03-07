package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.action.CompoundAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.BaseSpell;
import com.elmakers.mine.bukkit.utility.RandomUtils;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Arrays;
import java.util.Collection;

public class CoverAction extends CompoundAction
{
	private static final int DEFAULT_RADIUS	= 2;
    private int radius;
    private double centerProbability;
    private double outerProbability;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        super.prepare(context, parameters);
        radius = parameters.getInt("radius", DEFAULT_RADIUS);
        centerProbability = parameters.getDouble("probability", 1);
        outerProbability = parameters.getDouble("probability", 1);
        centerProbability = parameters.getDouble("center_probability", centerProbability);
        outerProbability = parameters.getDouble("outer_probability", outerProbability);
    }

	@Override
	public SpellResult perform(CastContext context) {
        Block block = context.getTargetBlock();
		if (!context.hasBuildPermission(block))
		{
			return SpellResult.INSUFFICIENT_PERMISSION;
		}

		Mage mage = context.getMage();
		int radius = (int)(mage.getRadiusMultiplier() * this.radius);
		block = context.findSpaceAbove(block);

        CastContext actionContext = createContext(context);
		if (radius < 1)
		{
            if (centerProbability >= 1 || context.getRandom().nextDouble() <= centerProbability) {
                actionContext.setTargetLocation(context.findBlockUnder(block).getLocation());
            }
			return performActions(actionContext);
		}

		SpellResult result = SpellResult.NO_ACTION;
		int y = block.getY();
		for (int dx = -radius; dx < radius; ++dx)
		{
			for (int dz = -radius; dz < radius; ++dz)
			{
				if (isInCircle(dx, dz, radius))
				{
					int x = block.getX() + dx;
					int z = block.getZ() + dz;
					Block targetBlock = context.getWorld().getBlockAt(x, y, z);
					targetBlock = context.findBlockUnder(targetBlock);
					Block coveringBlock = targetBlock.getRelative(BlockFace.UP);
					if (!context.isTransparent(targetBlock.getType()) && context.isTransparent(coveringBlock.getType()))
					{
                        double probability = centerProbability;
                        if (centerProbability != outerProbability) {
                            probability = RandomUtils.lerp(centerProbability, outerProbability, (dx * dz) / (radius * radius));
                        }

                        if (probability >= 1 || context.getRandom().nextDouble() <= probability) {
                            actionContext.setTargetLocation(targetBlock.getLocation());
                            actions.perform(actionContext);
                        }
					}
				}
			}
		}

		return result;
	}

    @Override
    public boolean requiresTarget() {
        return true;
    }

	protected boolean isInCircle(int x, int z, int R)
	{
		return ((x * x) +  (z * z) - (R * R)) <= 0;
	}

    @Override
    public void getParameterNames(Collection<String> parameters)
    {
        super.getParameterNames(parameters);
        parameters.add("radius");
        parameters.add("probability");
        parameters.add("center_probability");
        parameters.add("outer_probability");
    }

    @Override
    public void getParameterOptions(Collection<String> examples, String parameterKey)
    {
        super.getParameterOptions(examples, parameterKey);

        if (parameterKey.equals("radius")) {
            examples.addAll(Arrays.asList(BaseSpell.EXAMPLE_SIZES));
        } else if (parameterKey.equals("probability") || parameterKey.equals("center_probability") || parameterKey.equals("outer_probability")) {
            examples.addAll(Arrays.asList(BaseSpell.EXAMPLE_PERCENTAGES));
        }
    }
}
