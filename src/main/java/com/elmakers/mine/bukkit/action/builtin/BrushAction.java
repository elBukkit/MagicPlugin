package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.action.CompoundAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.block.MaterialBrush;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;

public class BrushAction extends CompoundAction {
    private List<MaterialBrush> brushes = new ArrayList<MaterialBrush>();
    private boolean sample = false;
    private boolean performed = false;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        super.prepare(context, parameters);
        brushes.clear();

        Mage mage = context.getMage();
        Location location = context.getLocation();
        String materialKey = parameters.getString("brush", null);
        if (materialKey != null) {
            addBrush(mage, location, materialKey);
        }
        List<String> brushList = parameters.getStringList("brushes");
        if (brushList != null) {
            for (String brushKey : brushList) {
                addBrush(mage, location, brushKey);
            }
        }
        sample = parameters.getBoolean("sample");
    }

    protected void addBrush(Mage mage, Location location, String brushKey) {
        brushes.add(new MaterialBrush(mage, location, brushKey));
    }

    @Override
    public void reset(CastContext context) {
        super.reset(context);
        performed = false;
    }

    @Override
    public SpellResult perform(CastContext context) {
        if (brushes.size() == 0 && !sample) {
            return super.perform(context);
        }

        // Don't re-assign the brush if we are re-running pending actions
        if (performed) {
            return super.perform(context);
        }

        createActionContext(context);
        if (sample)
        {
            Block targetBlock = context.getTargetBlock();
            if (targetBlock != null)
            {
                Mage mage = context.getMage();
                MaterialBrush brush = new MaterialBrush(mage, targetBlock);
                actionContext.setBrush(brush);
            }
        }
        else
        {
            MaterialBrush brush = brushes.get(context.getRandom().nextInt(brushes.size()));
            brush.clearCloneTarget();
            actionContext.setBrush(brush);
        }
        return super.perform(actionContext);
    }

    @Override
    public boolean usesBrush() {
        return false;
    }
}
