package com.elmakers.mine.bukkit.magic.command;

import java.util.Collection;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.npc.MagicNPC;
import com.elmakers.mine.bukkit.effect.NPCTargetingContext;
import com.elmakers.mine.bukkit.magic.MagicController;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import com.elmakers.mine.bukkit.utility.Target;
import com.elmakers.mine.bukkit.utility.Targeting;

public class NPCSelectionManager extends SelectionManager<MagicNPC> {
    private final Targeting targeting;
    private static final int selectRange = 32;

    public NPCSelectionManager(MagicController controller) {
        super(controller);
        targeting = new Targeting();
        ConfigurationSection targetingParameters = ConfigurationUtils.newConfigurationSection();
        targetingParameters.set("range", selectRange);
        targetingParameters.set("target", "other_entity");
        targetingParameters.set("ignore_blocks", true);
        targeting.processParameters(targetingParameters);
    }

    @Override
    @Nonnull
    public Collection<MagicNPC> getAll() {
        return controller.getNPCs();
    }

    @Override
    @Nonnull
    protected String getTypeNamePlural() {
        return "NPCs";
    }

    @Override
    protected void showListItem(CommandSender sender, MagicNPC npc, ListType listType) {
        int duration;
        switch (listType) {
            case INACTIVE:
                return;
            case TARGET:
                duration = 5;
                break;
            case SELECTED:
                duration = 4;
                break;
            default:
                duration = 3;
                break;
        }
        highlight(npc, duration);
    }

    public void highlight(MagicNPC npc) {
        highlight(npc, 5);
    }

    public void highlight(MagicNPC npc, int duration) {
        Entity entity = npc.getEntity();
        if (entity != null && entity instanceof LivingEntity) {
            LivingEntity li = (LivingEntity)entity;
            li.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, duration * 20, 1, true, false));
        }
    }

    @Override
    @Nullable
    protected MagicNPC getTarget(CommandSender sender, List<MagicNPC> sorted) {
        Mage mage = controller.getRegisteredMage(sender);
        if (mage == null) return null;
        MagicNPC npc = null;
        NPCTargetingContext context = new NPCTargetingContext(mage);
        targeting.reset();
        Target target = targeting.target(context, selectRange);
        if (target != null && target.hasEntity()) {
            npc = controller.getNPC(target.getEntity());
        }
        return npc;
    }
}
