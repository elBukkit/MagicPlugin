package com.elmakers.mine.bukkit.integration.valhalla;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;

import me.athlaeos.valhallammo.skills.perk_rewards.PerkReward;
import me.athlaeos.valhallammo.skills.perk_rewards.PerkRewardArgumentType;

public class RecipePerkReward extends PerkReward {
    private final MageController controller;
    private final List<NamespacedKey> recipeKeys = new ArrayList<>();

    public RecipePerkReward(MageController controller, String name) {
        super(name);
        this.controller = controller;
    }

    @Override
    public void apply(Player player) {
        for (NamespacedKey recipeKey : recipeKeys) {
            player.discoverRecipe(recipeKey);
        }
    }

    @Override
    public void remove(Player player) {
        for (NamespacedKey recipeKey : recipeKeys) {
            player.undiscoverRecipe(recipeKey);
        }
    }

    @Override
    public void parseArgument(Object argument) {
        List<String> keyList = ConfigurationUtils.getStringList(argument);
        for (String key : keyList) {
            if (key.contains(":")) {
                recipeKeys.add(NamespacedKey.fromString(key));
            } else {
                recipeKeys.add(new NamespacedKey(controller.getPlugin(), key.toLowerCase(Locale.ROOT)));
            }
        }
    }

    @Override
    public String rewardPlaceholder() {
        return recipeKeys.size() > 1 ? "Learn " + recipeKeys.size() + " crafting recipes" : "Learn a crafting recipe";
    }

    @Override
    public PerkRewardArgumentType getRequiredType() {
        return PerkRewardArgumentType.STRING_LIST;
    }
}
