package com.elmakers.mine.bukkit.block.magic;

import java.util.Arrays;
import javax.annotation.Nonnull;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.entity.EntityData;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import com.elmakers.mine.bukkit.utility.SpellUtils;

public class InteractCaster {
    private final MagicBlockTemplate template;
    @Nonnull
    private final String spellKey;
    protected final ConfigurationSection spellParameters;
    protected EntityData.SourceType spellSource;
    protected EntityData.TargetType spellTarget;

    public InteractCaster(MagicBlockTemplate template, MageController controller, ConfigurationSection configuration) {
        this.template = template;
        String spellKeyAndParameters = configuration.getString("spell");
        ConfigurationSection parameters = configuration.getConfigurationSection("parameters");
        spellParameters = parameters == null ? configuration.createSection("parameters") : parameters;

        if (spellKeyAndParameters.contains(" ")) {
            String[] split = StringUtils.split(spellKeyAndParameters, ' ');
            spellKey = split[0];
            String[] parameterPieces = Arrays.copyOfRange(split, 1, split.length);
            ConfigurationUtils.addParameters(parameterPieces, spellParameters);
        } else {
            spellKey = spellKeyAndParameters;
        }
        String sourceType = configuration.getString("interact_spell_source", "PLAYER");
        if (sourceType.equalsIgnoreCase("OPPED_PLAYER")) {
            controller.getLogger().warning("Invalid spell source type: " + sourceType);
            sourceType = "PLAYER";
        }
        try {
            spellSource = EntityData.SourceType.valueOf(sourceType.toUpperCase());
        } catch (Exception ex) {
            controller.getLogger().warning("Invalid block interact source type: " + sourceType);
            spellSource = EntityData.SourceType.PLAYER;
        }
        String targetType = configuration.getString("interact_spell_target", "BLOCK");
        try {
            spellTarget = EntityData.TargetType.valueOf(targetType.toUpperCase());
        } catch (Exception ex) {
            controller.getLogger().warning("Invalid block interact target type: " + targetType);
            spellTarget = EntityData.TargetType.BLOCK;
        }
    }

    private String getName() {
        return template == null ? "anonymous" : template.getKey();
    }

    public boolean onInteract(Mage magicBlock, Location location, Player player) {
        MageController controller = magicBlock.getController();
        magicBlock.setLocation(location);
        ConfigurationSection parameters = spellParameters;
        Mage mage = SpellUtils.getCastSource(spellSource, player, null, magicBlock, controller, " magic block " + getName());
        SpellUtils.prepareParameters(spellTarget, parameters, player, null, magicBlock, controller, " magic block " + getName());
        String interactSpell = spellKey;
        if (!interactSpell.contains("|")) {
            int level = mage.getActiveProperties().getSpellLevel(interactSpell);
            if (level > 1) {
                interactSpell += "|" + level;
            }
        }

        return controller.cast(mage, interactSpell, parameters, player);
    }
}
