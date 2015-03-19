package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.action.CompoundAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.block.MaterialBrush;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;

public class BrushAction extends CompoundAction {
    private List<MaterialBrush> brushes = new ArrayList<MaterialBrush>();

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
    }

    protected void addBrush(Mage mage, Location location, String brushKey) {
        brushes.add(new MaterialBrush(mage, location, brushKey));
    }

    @Override
    public SpellResult perform(CastContext context) {
        if (brushes.size() == 0) {
            return performActions(context);
        }
        createActionContext(context);
        MaterialBrush brush = brushes.get(context.getRandom().nextInt(brushes.size()));
        actionContext.setBrush(brush);
        return performActions(actionContext);
    }

    @Override
    public boolean usesBrush() {
        return false;
    }
}
