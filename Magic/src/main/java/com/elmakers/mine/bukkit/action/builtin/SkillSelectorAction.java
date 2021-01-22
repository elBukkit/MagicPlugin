package com.elmakers.mine.bukkit.action.builtin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.action.GUIAction;
import com.elmakers.mine.bukkit.api.block.MaterialAndData;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageClass;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.magic.Messages;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellKey;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.api.spell.SpellTemplate;
import com.elmakers.mine.bukkit.heroes.HeroesManager;
import com.elmakers.mine.bukkit.magic.MagicController;
import com.elmakers.mine.bukkit.spell.BaseSpell;
import com.elmakers.mine.bukkit.utility.CompatibilityUtils;
import com.elmakers.mine.bukkit.utility.InventoryUtils;
import com.elmakers.mine.bukkit.wand.Wand;

public class SkillSelectorAction extends BaseSpellAction implements GUIAction {
    private int page;
    private List<SkillDescription> allSkills = new ArrayList<>();
    private ConfigurationSection skillsConfig;
    private String classKey;
    private int inventoryLimit = 0;
    private int extraSlots = 0;
    private String inventoryTitle;
    private CastContext context;

    @Override
    public void processParameters(CastContext context, ConfigurationSection parameters) {
        super.processParameters(context, parameters);
        this.page = parameters.getInt("page", 1);
    }

    @Override
    public SpellResult perform(CastContext context) {
        this.context = context;

        MageController apiController = context.getController();
        Player player = context.getMage().getPlayer();
        if (player == null) {
            return SpellResult.PLAYER_REQUIRED;
        }
        if (!(apiController instanceof MagicController)) return SpellResult.FAIL;
        MagicController controller = (MagicController) apiController;
        Messages messages = controller.getMessages();
        HeroesManager heroes = controller.getHeroes();
        allSkills.clear();
        if (controller.useHeroesSkills() && heroes != null) {
            @Nonnull String classString = heroes.getClassName(player);
            @Nonnull String class2String = heroes.getSecondaryClassName(player);
            String messageKey = !class2String.isEmpty() ? "skills.inventory_title_secondary" : "skills.inventory_title";
            inventoryTitle = controller.getMessages().get(messageKey, "Skills ($page/$pages)");
            inventoryTitle = inventoryTitle
                    .replace("$class2", class2String)
                    .replace("$class", classString);

            List<String> heroesSkills = heroes.getSkillList(player, true, true);
            for (String heroesSkill : heroesSkills) {
                allSkills.add(new SkillDescription(heroes, controller, player, heroesSkill));
            }
        } else {
            inventoryTitle = messages.get("skills.inventory_title");
        }

        if (controller.usePermissionSkills()) {
            boolean bypassHidden = player.hasPermission("Magic.bypass_hidden");
            Collection<SpellTemplate> spells = controller.getSpellTemplates(bypassHidden);
            for (SpellTemplate spell : spells) {
                SpellKey key = spell.getSpellKey();
                if (key.isVariant()) continue;
                if (key.getBaseKey().startsWith("heroes*")) continue;
                if (!spell.hasCastPermission(player)) continue;
                allSkills.add(new SkillDescription(spell));
            }
        } else {
            Mage mage = controller.getMage(player);
            MageClass activeClass = mage.getActiveClass();
            if (activeClass != null) {
                // gather spells in player's inventory to avoid showing
                Set<String> hasSpells = new HashSet<>();
                for (ItemStack item : player.getInventory().getContents()) {
                    String spellKey = controller.getSpell(item);
                    if (spellKey != null) {
                        hasSpells.add(spellKey);
                    }
                }

                classKey = activeClass.getKey();
                skillsConfig = activeClass.getProperty("skills", ConfigurationSection.class);
                inventoryLimit = activeClass.getProperty("skills.skill_limit", 0);
                Collection<String> spells = activeClass.getSpells();
                for (String spellKey : spells) {
                    if (hasSpells.contains(spellKey)) {
                        extraSlots++;
                        continue;
                    }
                    SpellTemplate spell = controller.getSpellTemplate(spellKey);
                    if (spell != null) {
                        allSkills.add(new SkillDescription(spell));
                    }
                }
            }
        }

        if (allSkills.size() == 0 && extraSlots == 0) {
            player.sendMessage(messages.get("skills.none"));
            return SpellResult.NO_ACTION;
        }

        Collections.sort(allSkills);
        openInventory();

        return SpellResult.CAST;
    }

    private static class SkillDescription implements Comparable<SkillDescription> {
        public String heroesSkill;
        public SpellTemplate spell;
        private int skillLevel = 0;

        public SkillDescription(SpellTemplate spell) {
            this.spell = spell;
        }

        public SkillDescription(HeroesManager heroes, MageController controller, Player player, String heroesSkill) {
            this.heroesSkill = heroesSkill;
            this.spell = controller.getSpellTemplate(getSpellKey());
            skillLevel = heroes.getSkillLevel(player, heroesSkill);
        }

        @Nullable
        public String getSkillKey() {
            String key = getSpellKey();
            if (key != null) {
                key = "skill:" + key;
            }
            return key;
        }

        @Nullable
        public String getSpellKey() {
            if (heroesSkill != null) {
                return "heroes*" + heroesSkill;
            }
            if (spell != null) {
                return spell.getKey();
            }
            return null;
        }

        public String getSpellName() {
            if (spell != null) return spell.getName();
            return heroesSkill;
        }

        public boolean isHeroes() {
            return heroesSkill != null;
        }

        @Override
        public int compareTo(SkillDescription other) {
            if (heroesSkill != null && skillLevel != other.skillLevel) {
                return Integer.compare(skillLevel, other.skillLevel);
            }
            return getSpellName().compareTo(other.getSpellName());
        }
    }

    public SkillSelectorAction() {
    }

    protected void openInventory() {
        MageController apiController = context.getController();
        if (!(apiController instanceof MagicController)) return;
        MagicController controller = (MagicController) apiController;
        HeroesManager heroes = controller.getHeroes();
        int inventorySize = 9 * controller.getSkillInventoryRows();
        float numSlots = extraSlots + allSkills.size();
        int numPages = (int)Math.ceil(numSlots / inventorySize);
        if (page < 1) page = numPages;
        else if (page > numPages) page = 1;
        Mage mage = context.getMage();
        Player player = mage.getPlayer();
        int pageIndex = page - 1;
        int startIndex = pageIndex * inventorySize;
        int maxIndex = (pageIndex + 1) * inventorySize - 1;

        List<SkillDescription> skills = new ArrayList<>();
        for (int i = startIndex; i <= maxIndex && i < allSkills.size(); i++) {
            skills.add(allSkills.get(i));
        }
        if (skills.size() == 0 && extraSlots == 0)
        {
            String messageTemplate = controller.getMessages().get("skills.none_on_page", "$page");
            player.sendMessage(messageTemplate.replace("$page", Integer.toString(page)));
            return;
        }
        int invSize = (int)Math.ceil(numSlots / 9.0f) * 9;
        invSize = Math.min(inventorySize, invSize);
        String title = inventoryTitle;
        title = title
                .replace("$pages", Integer.toString(numPages))
                .replace("$page", Integer.toString(page));
        Inventory displayInventory = CompatibilityUtils.createInventory(null, invSize, title);
        for (SkillDescription skill : skills)
        {
            ItemStack skillItem = controller.getAPI().createItem(skill.getSkillKey(), mage);
            if (skillItem == null) continue;
            InventoryUtils.configureSkillItem(skillItem, classKey, skillsConfig);
            if (skill.isHeroes() && heroes != null && !heroes.canUseSkill(player, skill.heroesSkill))
            {
                String nameTemplate = controller.getMessages().get("skills.item_name_unavailable", "$skill");
                String spellName = skill.spell != null ? skill.spell.getName() : skill.heroesSkill;
                CompatibilityUtils.setDisplayName(skillItem, nameTemplate.replace("$skill", spellName));
                InventoryUtils.setMetaBoolean(skillItem, "unavailable", true);
                if (skill.spell != null) {
                    MaterialAndData disabledIcon = skill.spell.getDisabledIcon();
                    if (disabledIcon != null) {
                        disabledIcon.applyToItem(skillItem);
                    } else {
                        String disabledIconURL = skill.spell.getDisabledIconURL();
                        if (disabledIconURL != null && !disabledIconURL.isEmpty()) {
                            InventoryUtils.setSkullURL(skillItem, disabledIconURL);
                        }
                    }
                }
            }
            displayInventory.addItem(skillItem);
        }

        mage.deactivateGUI();
        mage.activateGUI(this, displayInventory);
    }

    @Override
    public void deactivated() {

    }

    @Override
    public void clicked(InventoryClickEvent event) {
        InventoryAction action = event.getAction();
        if (action == InventoryAction.NOTHING) {
            int direction = event.getClick() == ClickType.LEFT ? 1 : -1;
            page = page + direction;
            openInventory();
            event.setCancelled(true);
            return;
        }

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem != null && InventoryUtils.getMetaBoolean(clickedItem, "unavailable", false)) {
            event.setCancelled(true);
            return;
        }

        Mage mage = context.getMage();
        MageController controller = mage.getController();
        Inventory inventory = mage.getInventory();
        boolean isContainerSlot = event.getSlot() == event.getRawSlot();
        boolean isDrop = action == InventoryAction.DROP_ALL_CURSOR || action == InventoryAction.DROP_ALL_SLOT
                || action == InventoryAction.DROP_ONE_CURSOR || action == InventoryAction.DROP_ONE_SLOT;

        if (!isContainerSlot && isDrop && controller.isSkill(clickedItem) && !InventoryUtils.getMetaBoolean(clickedItem, "undroppable", false)) {
            inventory.setItem(event.getSlot(), null);
            return;
        }

        if (inventoryLimit > 0) {
            boolean isHotbar = event.getAction() == InventoryAction.HOTBAR_SWAP || event.getAction() == InventoryAction.HOTBAR_MOVE_AND_READD;
            if (isHotbar) {
                ItemStack destinationItem = inventory.getItem(event.getHotbarButton());
                if (controller.isSkill(destinationItem)) return;
                isHotbar = controller.isSkill(clickedItem);
            }

            if (!isContainerSlot && !isHotbar) return;

            int skillCount = 0;
            for (int i = 0; i < inventory.getSize(); i++) {
                ItemStack item = inventory.getItem(i);
                if (controller.isSkill(item)) skillCount++;
            }
            if (skillCount >= inventoryLimit) {
                event.setCancelled(true);
            }
            return;
        }

        // We don't allow quick-casting here if there is an inventory limit.
        if (action == InventoryAction.PICKUP_HALF) {
            Spell spell = mage.getSpell(Wand.getSpell(clickedItem));
            if (spell != null) {
                spell.cast();
            }
            mage.getPlayer().closeInventory();
            event.setCancelled(true);
        }
    }

    @Override
    public void dragged(InventoryDragEvent event) {

    }

    @Override
    public void getParameterNames(Spell spell, Collection<String> parameters)
    {
        super.getParameterNames(spell, parameters);
        parameters.add("page");
    }

    @Override
    public void getParameterOptions(Spell spell, String parameterKey, Collection<String> examples)
    {
        super.getParameterOptions(spell, parameterKey, examples);

        if (parameterKey.equals("page")) {
            examples.addAll(Arrays.asList(BaseSpell.EXAMPLE_SIZES));
        }
    }
}
