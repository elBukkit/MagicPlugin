package com.elmakers.mine.bukkit.spell.builtin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MaterialSet;
import com.elmakers.mine.bukkit.api.magic.MaterialSetManager;
import com.elmakers.mine.bukkit.api.magic.Messages;
import com.elmakers.mine.bukkit.api.spell.Spell;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.block.MaterialAndData;
import com.elmakers.mine.bukkit.utility.TextUtils;
import com.elmakers.mine.bukkit.utility.AscendingPair;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import com.elmakers.mine.bukkit.utility.RandomUtils;
import com.elmakers.mine.bukkit.utility.WeightedPair;
import org.bukkit.configuration.MemoryConfiguration;

public class AnimateSpell extends SimulateSpell 
{
	private static Random random = new Random();
	private LinkedList<WeightedPair<Integer>> levelWeights = null;

	public final static String[] ANIMATE_PARAMETERS = {
		"animate", "sim_check_destructible", "seed_radius", "restricted", "obworld", "btarget"
	};
	
    @Override
	public SpellResult onCast(ConfigurationSection parameters) 
	{
		if (parameters.getString("animate", null) != null)
		{
			return super.onCast(parameters);
		}
		
		final Block targetBlock = getTargetBlock();
		if (targetBlock == null) 
		{
			return SpellResult.NO_TARGET;
		}
		if (!hasBuildPermission(targetBlock)) 
		{
			return SpellResult.INSUFFICIENT_PERMISSION;
		}
		int seedRadius = parameters.getInt("seed_radius", 0);
		MaterialAndData targetMaterial = new MaterialAndData(targetBlock);

		List<String> materials = ConfigurationUtils.getStringList(parameters, "materials");
		if (seedRadius > 0 && materials != null && !materials.isEmpty()) {
			targetMaterial = new MaterialAndData(RandomUtils.getRandom(materials));
		} else if (parameters.contains("material")) {
            targetMaterial = ConfigurationUtils.getMaterialAndData(parameters,
                    "material", targetMaterial);
            if (targetMaterial.isValid()) {
                addDestructible(targetMaterial);
            }
		}

        if (!mage.isSuperPowered() && seedRadius == 0) {
            MaterialSetManager materialSets = controller.getMaterialSetManager();
            MaterialSet restricted = materialSets.getMaterialSet("restricted");
            if (restricted != null && restricted.testMaterialAndData(targetMaterial)) {
                return SpellResult.FAIL;
            }

            if (parameters.contains("restricted")) {
                String customRestricted = parameters.getString("restricted");
                if (customRestricted != null && !customRestricted.equals("restricted")) {
                    restricted = materialSets.fromConfigEmpty(customRestricted);
                    if (restricted.testMaterialAndData(targetMaterial)) {
                        return SpellResult.FAIL;
                    }
                }
            }
        }

		if (!isDestructible(targetBlock)) {
			return SpellResult.INSUFFICIENT_PERMISSION;
		}

		registerForUndo(targetBlock);

		if (seedRadius > 0) {
			for (int dx = -seedRadius; dx < seedRadius; dx++) {
				for (int dz = -seedRadius; dz < seedRadius; dz++) {
					for (int dy = -seedRadius; dy < seedRadius; dy++) {
						Block seedBlock = targetBlock.getRelative(dx, dy, dz);
						if (isDestructible(seedBlock)) {
							registerForUndo(seedBlock);
							targetMaterial.modify(seedBlock);
						}
					}
				}
			}
		}
		
		// Look for randomized levels
		int level = 0;
		if (parameters.contains("level")) {
			level = parameters.getInt("level", level);
		}
		else if (levelWeights != null) {
			level = RandomUtils.weightedRandom(levelWeights);
		}
		
		boolean simCheckDestructible = parameters.getBoolean("sim_check_destructible", true);
		simCheckDestructible = parameters.getBoolean("scd", simCheckDestructible);

		final ConfigurationSection automataParameters = new MemoryConfiguration();
		automataParameters.set("target", "self");
		automataParameters.set("cooldown", 0);
		automataParameters.set("m", targetMaterial.getKey());
		automataParameters.set("cd",  (simCheckDestructible ? true : false));
		automataParameters.set("level", level);
		String automataName = parameters.getString("name", "Automata");

        Messages messages = controller.getMessages();
		String automataType = parameters.getString("message_type", "evil");
		List<String> prefixes = messages.getAll("automata." + automataType + ".prefixes");
		List<String> suffixes = messages.getAll("automata." + automataType + ".suffixes");
		
		automataName = prefixes.get(random.nextInt(prefixes.size()))
				+ " " + automataName + " " + suffixes.get(random.nextInt(suffixes.size()));

		if (level > 1) 
		{
			automataName += " " + escapeLevel(messages, "automata.level", level);
		}

		String message = getMessage("cast_broadcast").replace("$name", automataName);
		if (message.length() > 0) {
			controller.sendToMages(message, targetBlock.getLocation());	
		}

		automataParameters.set("animate", automataName);
		String automataId = UUID.randomUUID().toString();
		final Mage mage = controller.getAutomaton(automataId, automataName);
		mage.setLocation(targetBlock.getLocation());
		mage.setQuiet(true);
		mage.addTag(getKey());
		final Spell spell = mage.getSpell(getKey());
        Bukkit.getScheduler().runTaskLater(controller.getPlugin(), new Runnable() {
            @Override
            public void run() {
                spell.cast(automataParameters);
            }
        }, 1);

        registerForUndo();
		return SpellResult.CAST;
	}

	@Override
	public void getParameterOptions(Collection<String> examples, String parameterKey)
	{
		super.getParameterOptions(examples, parameterKey);
		
		if (parameterKey.equals("animate") || parameterKey.equals("sim_check_destructible")) {
			examples.addAll(Arrays.asList(EXAMPLE_BOOLEANS));
		}
	}

	@Override
	public void getParameters(Collection<String> parameters)
	{
		super.getParameters(parameters);
		parameters.addAll(Arrays.asList(ANIMATE_PARAMETERS));
	}
	
	@Override
	protected void loadTemplate(ConfigurationSection template)
	{
		super.loadTemplate(template);
		
		if (template.contains("levels")) {
			ConfigurationSection levelTemplate = template.getConfigurationSection("levels");
			Collection<String> levelKeys = levelTemplate.getKeys(false);
			
			List<AscendingPair<Float>> levels = new ArrayList<>();
			
			for (String levelString : levelKeys) {
				int level =  Integer.parseInt(levelString);
				double weight = levelTemplate.getDouble(levelString);
				levels.add(new AscendingPair<>(level, (float)weight));
			}
			
			RandomUtils.extrapolateFloatList(levels);
			
			levelWeights = new LinkedList<>();
			float threshold = 0;
			for (AscendingPair<Float> level : levels) {
				float weight = level.getValue();
				int levelIndex = (int)level.getIndex();
				threshold += weight;
				levelWeights.add(new WeightedPair<>(threshold, weight, levelIndex));
			}
		} else {
			levelWeights = null;
		}
	}
	
	protected static String escapeLevel(Messages messages, String templateName, int level)
	{
		String templateString = messages.get(templateName);
		if (templateString.contains("$roman")) {
			return templateString.replace("$roman", TextUtils.roman(level));
		}
		return templateString.replace("$amount", Integer.toString(level));
	}
}
