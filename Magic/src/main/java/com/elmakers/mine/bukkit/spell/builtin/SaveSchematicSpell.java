package com.elmakers.mine.bukkit.spell.builtin;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.api.magic.MaterialSet;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.api.spell.TargetType;
import com.elmakers.mine.bukkit.batch.SaveSchematicBatch;
import com.elmakers.mine.bukkit.spell.TargetingSpell;

public class SaveSchematicSpell extends TargetingSpell {
    private static final int DEFAULT_MAX_DIMENSION = 128;
    private Block targetBlock = null;
    private String filename = "(Not Saved)";

    @Override
    public SpellResult onCast(ConfigurationSection parameters) {
        if (getTargetType() != TargetType.SELECT) {
            mage.sendMessage(ChatColor.RED + "This spell requires target: select");
            return SpellResult.NO_TARGET;
        }
        Block targetBlock = getTargetBlock();
        if (parameters.getBoolean("select_self", true) && isLookingDown()) {
            targetBlock = mage.getLocation().getBlock().getRelative(BlockFace.DOWN);
        }
        if (targetBlock == null) {
            return SpellResult.NO_TARGET;
        }

        if (targetLocation2 != null) {
            this.targetBlock = targetLocation2.getBlock();
        }

        MaterialSet ignoreMaterials = null;
        String ignoreMaterialsConfig = parameters.getString("ignore");
        if (ignoreMaterialsConfig != null && !ignoreMaterialsConfig.isEmpty()) {
            ignoreMaterials = controller.getMaterialSetManager().fromConfig(ignoreMaterialsConfig);
        }

        if (this.targetBlock != null) {
            Location secondLocation = this.targetBlock.getLocation();
            SaveSchematicBatch batch = new SaveSchematicBatch(this, secondLocation, targetBlock.getLocation(), ignoreMaterials);

            int maxDimension = parameters.getInt("max_dimension", DEFAULT_MAX_DIMENSION);
            maxDimension = parameters.getInt("md", maxDimension);
            maxDimension = (int) (mage.getConstructionMultiplier() * maxDimension);

            if (!batch.checkDimension(maxDimension)) {
                return SpellResult.FAIL;
            }
            boolean success = mage.addBatch(batch);
            return success ? SpellResult.PENDING : SpellResult.FAIL;
        } else {
            setSelectedLocation(targetBlock.getLocation());
            this.targetBlock = targetBlock;
            activate();
            return SpellResult.TARGET_SELECTED;
        }
    }

    @Override
    protected void onFinalizeCast(SpellResult result) {
        if (result != SpellResult.TARGET_SELECTED) {
            deactivate(false, true, false);
        }
    }

    @Override
    protected boolean isBatched() {
        return true;
    }

    @Override
    public boolean onCancelSelection() {
        return targetBlock != null;
    }

    @Override
    public void onDeactivate() {
        targetBlock = null;
        setSelectedLocation(null);
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    @Override
    public String getMessage(String messageKey, String def) {
        String message = super.getMessage(messageKey, def);
        message = message.replace("$filename", filename);
        return message;
    }
}
