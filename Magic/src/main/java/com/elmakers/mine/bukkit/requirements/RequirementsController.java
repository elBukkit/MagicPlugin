package com.elmakers.mine.bukkit.requirements;

import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.CasterProperties;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.magic.ProgressionPath;
import com.elmakers.mine.bukkit.api.requirements.Requirement;
import com.elmakers.mine.bukkit.api.requirements.RequirementsProcessor;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.api.wand.Wand;
import com.elmakers.mine.bukkit.api.wand.WandUpgradePath;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;

public class RequirementsController implements RequirementsProcessor {
    
    private class MagicRequirement {
        public @Nullable String permissionNode = null;
        public @Nullable String requiredPath = null;
        public @Nullable String requiredTemplate = null;
        public @Nullable Set<String> requiredTemplates = null;
        public @Nullable String requiresCompletedPath = null;
        public @Nullable String exactPath = null;
        public @Nullable String mageClass = null;
        public boolean requireWand = false;
        
        public MagicRequirement(Requirement requirement) {
            ConfigurationSection configuration = requirement.getConfiguration();

            permissionNode = configuration.getString("permission");
            requiredPath = configuration.getString("path");
            exactPath = configuration.getString("path_exact");
            requiresCompletedPath = configuration.getString("path_end");
            requiredTemplate = configuration.getString("wand");
            requireWand = configuration.getBoolean("holding_wand");
            mageClass = configuration.getString("class");

            if (configuration.contains("wands")) {
                requiredTemplates = new HashSet<>(ConfigurationUtils.getStringList(configuration, "wands"));
            }

            if (requiresCompletedPath != null) {
                requiredPath = requiresCompletedPath;
                exactPath = requiresCompletedPath;
            }
            
            if (requiredTemplate != null || requiredTemplates != null) {
                requireWand = true;
            }
        }
        
    }

    @Override
    public boolean checkRequirement(@Nonnull CastContext context, @Nonnull Requirement requirement) {
        Mage mage = context.getMage();
        MageController controller = mage.getController();
        Player player = mage.getPlayer();
        MagicRequirement check = new MagicRequirement(requirement);
        
        if (check.permissionNode != null && (player == null || !player.hasPermission(check.permissionNode))) {
            return false;
        }
        Wand wand = context.getWand();
        if (wand == null && check.requireWand) {
            return false;
        }

        if (check.requiredTemplate != null) {
            String template = wand.getTemplateKey();
            if (template == null || !template.equals(check.requiredTemplate)) {
                return false;
            }
        }

        if (check.requiredTemplates != null) {
            String template = wand.getTemplateKey();
            if (template == null || !check.requiredTemplates.contains(template)) {
                return false;
            }
        }

        CasterProperties checkProperties = context.getActiveProperties();
        ProgressionPath path = checkProperties.getPath();

        if (check.mageClass != null && !check.mageClass.isEmpty()) {
            if (!mage.hasClassUnlocked(check.mageClass)) {
                return false;
            }
        }

        if (check.requiredPath != null || check.exactPath != null) {
            if (path == null) {
                return false;
            }

            if (check.requiredPath != null && !path.hasPath(check.requiredPath)) {
                return false;
            }
            if (check.exactPath != null && !check.exactPath.equals(path.getKey())) {
                return false;
            }
            if (check.requiresCompletedPath != null) {
                boolean hasPathCompleted = false;
                if (path.hasPath(check.requiresCompletedPath)) {
                    if (path.getKey().equals(check.requiresCompletedPath)) {
                        hasPathCompleted = !path.canProgress(checkProperties);
                    } else {
                        hasPathCompleted = true;
                    }
                }

                if (!hasPathCompleted) {
                    return false;
                }
            }
        }
        
        return true;
    }

    protected String getMessage(CastContext context, String key) {
        return context.getMessage(key, getDefaultMessage(context, key));
    }

    protected String getDefaultMessage(CastContext context, String key) {
        // This borrows messages from shops
        // TODO: Divorce this in some way that doesn't break peoples' localizations.
        return context.getController().getMessages().get("shops." + key);
    }

    @Nullable
    @Override
    public String getRequirementDescription(@Nonnull CastContext context, @Nonnull Requirement requirement) {
        Mage mage = context.getMage();
        MageController controller = mage.getController();
        Player player = mage.getPlayer();
        MagicRequirement check = new MagicRequirement(requirement);

        if (check.permissionNode != null && (player == null || !player.hasPermission(check.permissionNode))) {
            return context.getMessage(SpellResult.INSUFFICIENT_PERMISSION.name().toLowerCase());
        }
        Wand wand = context.getWand();
        if (wand == null && check.requireWand) {
            return getMessage(context, "no_wand");
        }
        
        if (check.requiredTemplate != null) {
            String template = wand.getTemplateKey();
            if (template == null || !template.equals(check.requiredTemplate)) {
                return getMessage(context, "no_template").replace("$wand", wand.getName());
            }
        }

        if (check.requiredTemplates != null) {
            String template = wand.getTemplateKey();
            if (template == null || check.requiredTemplates.contains(template)) {
                return getMessage(context, "no_template").replace("$wand", wand.getName());
            }
        }
        
        if (check.mageClass != null && !check.mageClass.isEmpty()) {
            if (mage.hasClassUnlocked(check.mageClass)) {
                return getMessage(context, "no_class").replace("$class", check.mageClass);
            }
        }
        
        CasterProperties checkProperties = context.getActiveProperties();
        ProgressionPath path = checkProperties.getPath();

        if (check.requiredPath != null || check.exactPath != null) {
            if (path == null) {
                return getMessage(context, "no_path");
            }

            if (check.requiredPath != null && !path.hasPath(check.requiredPath)) {
                WandUpgradePath requiresPath = controller.getPath(check.requiredPath);
                String pathName = check.requiredPath;
                if (requiresPath != null) {
                    pathName = requiresPath.getName();
                } else {
                    context.getLogger().warning("Invalid path specified in requirement " + check.requiredPath);
                }
                return getMessage(context, "no_required_path").replace("$path", pathName);
            }
            if (check.exactPath != null && !check.exactPath.equals(path.getKey())) {
                WandUpgradePath requiresPath = controller.getPath(check.exactPath);
                String pathName = check.exactPath;
                if (requiresPath != null) {
                    pathName = requiresPath.getName();
                } else {
                    context.getLogger().warning("Invalid path specified in requirement: " + check.exactPath);
                }
                return getMessage(context, "no_path_exact").replace("$path", pathName);
            }
            if (check.requiresCompletedPath != null) {
                boolean hasPathCompleted = false;
                if (path.hasPath(check.requiresCompletedPath)) {
                    if (path.getKey().equals(check.requiresCompletedPath)) {
                        hasPathCompleted = !path.canProgress(checkProperties);
                    } else {
                        hasPathCompleted = true;
                    }
                }

                if (!hasPathCompleted) {
                    WandUpgradePath requiresPath = controller.getPath(check.requiresCompletedPath);
                    String pathName = check.requiresCompletedPath;
                    if (requiresPath != null) {
                        pathName = requiresPath.getName();
                    } else {
                        context.getLogger().warning("Invalid path specified in requirement: " + check.exactPath);
                    }

                    return getMessage(context, "no_path_end").replace("$path", pathName);
                }
            }
        }
        
        return null;
    }
}
