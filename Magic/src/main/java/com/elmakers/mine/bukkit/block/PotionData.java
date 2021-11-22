package com.elmakers.mine.bukkit.block;

import java.util.List;

import org.bukkit.Color;
import org.bukkit.potion.PotionEffect;

public class PotionData extends ColoredData {
    private List<PotionEffect> effects;

    public PotionData(Color color, List<PotionEffect> effects) {
        super(color);
        this.effects = effects;
    }

    @Override
    public MaterialExtraData clone() {
        return new PotionData(color, effects);
    }

    public List<PotionEffect> getEffects() {
        return effects;
    }
}
