package com.elmakers.mine.bukkit.action.builtin;

import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.magic.MagicController;
import com.elmakers.mine.bukkit.protection.PreciousStonesManager;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;

public class CreateFieldAction extends ModifyBlockAction {
    private Material fieldType;
    private String rent;
    private String rentPeriod;
    private BlockFace rentSignDirection;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters)
    {
        super.prepare(context, parameters);
        fieldType = ConfigurationUtils.getMaterial(parameters, "field_type");
        rent = parameters.getString("field_rent", "");
        rentPeriod = parameters.getString("field_rent_period", "");
        String facingString = parameters.getString("field_rent_sign_direction", "north");
        try {
            rentSignDirection = BlockFace.valueOf(facingString.toUpperCase());
        } catch (Exception ex) {
            context.getLogger().log(Level.WARNING, "Invalid rent sign direction: " + facingString);
        }
    }

    @Override
    public SpellResult perform(CastContext context) {
        SpellResult result = super.perform(context);
        if (!result.isSuccess() || fieldType != context.getTargetBlock().getType()) {
            return result;
        }

        MageController apiController = context.getController();
        if (!(apiController instanceof MagicController)) {
            return SpellResult.FAIL;
        }
        MagicController controller = (MagicController)apiController;
        PreciousStonesManager preciousStones = controller.getPreciousStones();
        context.getMage().sendDebugMessage(ChatColor.GRAY + "Placing field", 7);
        if (!preciousStones.createField(context.getTargetLocation(), context.getMage().getPlayer())) {
            context.getMage().sendDebugMessage(ChatColor.RED + "Could not place field", 2);
            return SpellResult.NO_TARGET;
        }
        if (!rent.isEmpty() && !rentPeriod.isEmpty()) {
            if (!preciousStones.rentField(context.getTargetLocation().getBlock().getRelative(BlockFace.UP).getLocation(),
                    context.getMage().getPlayer(), rent, rentPeriod, rentSignDirection)) {
                context.getMage().sendDebugMessage(ChatColor.RED + "Could not rent field", 2);
            }
        }
        return SpellResult.CAST;
    }
}
