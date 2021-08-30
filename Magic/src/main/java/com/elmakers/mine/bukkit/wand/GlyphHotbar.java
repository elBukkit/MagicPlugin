package com.elmakers.mine.bukkit.wand;

import org.bukkit.Color;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.magic.Messages;
import com.elmakers.mine.bukkit.api.spell.CastingCost;
import com.elmakers.mine.bukkit.api.spell.MageSpell;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import com.elmakers.mine.bukkit.utility.TextUtils;

public class GlyphHotbar {
    private final Wand wand;
    private boolean skipEmpty;
    private int hotbarSlotWidth;
    private int hotbarActiveSlotWidth;
    private int iconWidth;
    private int slotSpacingWidth;
    private int barWidth;
    private int barSteps;
    private int flashTime;
    private int collapsedWidth;
    private int collapsedFinalSpacing;
    private int collapsedMaxMessageLength;
    private WandDisplayMode barMode;
    private int barSlotPadding;
    private boolean showCooldown;
    private String barTemplate;
    private String hotbarPrefix;

    protected String extraMessage;
    protected long lastExtraMessage;
    protected int extraMessageDelay;
    protected int extraAnimationTime;
    protected long lastInsufficientResource;
    protected long lastInsufficientCharges;
    protected long lastCooldown;
    protected Spell lastCooldownSpell;

    public GlyphHotbar(Wand wand) {
        this.wand = wand;
    }

    public void load(ConfigurationSection configuration) {
        if (configuration == null) {
            configuration = ConfigurationUtils.newConfigurationSection();
        }

        skipEmpty = configuration.getBoolean("skip_empty", true);
        hotbarSlotWidth = configuration.getInt("slot_width", 22);
        hotbarActiveSlotWidth = configuration.getInt("active_slot_width", 22);
        iconWidth = configuration.getInt("icon_width", 16);
        slotSpacingWidth = configuration.getInt("slot_spacing", -1);
        barWidth = configuration.getInt("bar_width", 128);
        barSteps = configuration.getInt("bar_steps", 32);
        barSlotPadding = configuration.getInt("bar_slot_padding", 1);
        flashTime = configuration.getInt("flash_duration", 300);
        extraMessageDelay = configuration.getInt("extra_display_time", 2000);
        extraAnimationTime = configuration.getInt("extra_animate_time", 500);
        collapsedWidth = configuration.getInt("collapsed_slot_width", 6);
        collapsedFinalSpacing = configuration.getInt("collapsed_spacing", 12);
        showCooldown = configuration.getBoolean("show_cooldown", true);
        collapsedMaxMessageLength = configuration.getInt("collapsed_message_max_length", 20);
        hotbarPrefix = configuration.getString("hotbar_prefix", "&f");
        barTemplate = configuration.getString("bar_template", "`{\"text\": \"$glyph\", \"color\":\"#$color\"}`");

        barMode = WandDisplayMode.parse(wand.getController(), configuration, "bar_mode", WandDisplayMode.MANA);
    }

    public String getGlyphs() {
        MageController controller = wand.getController();
        Messages messages = controller.getMessages();
        WandInventory hotbar = wand.getActiveHotbar();
        Mage mage = wand.getMage();
        if (hotbar == null || mage == null) return "";

        // Animation when showing extra message
        int hotbarActivePaddingLeft = (hotbarActiveSlotWidth - hotbarSlotWidth) / 2;
        int iconPaddingLeft = (hotbarSlotWidth - iconWidth) / 2;
        int iconPaddingRight = (hotbarSlotWidth - iconWidth) - iconPaddingLeft;
        int collapseSpace = 0;
        int finalSpace = 0;
        long now = System.currentTimeMillis();
        boolean hasExtraMessage = extraMessage != null && extraAnimationTime > 0;
        if (hasExtraMessage) {
            int useCollapsedWidth = collapsedWidth;
            int messageLength = extraMessage.length();
            if (messageLength < collapsedMaxMessageLength) {
                int widthDelta = hotbarSlotWidth - collapsedWidth;
                useCollapsedWidth = hotbarSlotWidth - (int)Math.ceil((double)widthDelta * messageLength / collapsedMaxMessageLength);
            }
            if (now < lastExtraMessage + extraAnimationTime) {
                collapseSpace = (int)Math.ceil((hotbarSlotWidth - useCollapsedWidth) * (now - lastExtraMessage) / extraAnimationTime);
                finalSpace = (int)Math.ceil(collapsedFinalSpacing * (now - lastExtraMessage) / extraAnimationTime);
            } else {
                collapseSpace = hotbarSlotWidth - useCollapsedWidth;
                finalSpace = collapsedFinalSpacing;
            }
        }
        String collapseReverse = messages.getSpace(-collapseSpace);
        String finalPadding = messages.getSpace(finalSpace);

        // Icon width + 1 pixel padding, to reverse back over the icon (for applying cooldown)
        String iconReverse = messages.getSpace(-(iconWidth + 1));

        // Hotbar slot border + 1 pixel padding, to reverse back over the hotbar slot
        String hotbarSlotReverse = messages.getSpace(-(hotbarSlotWidth + 1));

        // Padding between icon and the slot border on either side
        String hotbarIconPaddingLeft = messages.getSpace(iconPaddingLeft);
        String hotbarIconPaddingRight = messages.getSpace(iconPaddingRight);

        // Amount to reverse back past a hotbar slot background start before placing
        // The active slot overlay
        // Generally the active slot overlay is larger than the slot, so we have to back up
        // farther and then go forward farther as well
        // Also need to add in one extra pixel of space as usual
        String hotbarActiveSlotStart = messages.getSpace(-hotbarActivePaddingLeft);

        // How far to move back after adding the active overlay, to the beginner of the hotbar slot background
        String hotbarActiveSlotEnd = messages.getSpace(-(1 + hotbarActiveSlotWidth + hotbarActivePaddingLeft));

        // Configurable space between each slot
        String slotSpacing = messages.getSpace(slotSpacingWidth);
        String glyphs = "";

        // Create the hotbar
        int hotbarSlots = 0;
        String hotbarSlot = messages.get("glyphs.hotbar.hotbar_slot");
        String hotbarSlotActive = messages.get("glyphs.hotbar.hotbar_slot_active");
        String emptyIcon = messages.get("glyphs.icons.empty");
        for (ItemStack hotbarItem : hotbar.items) {
            String icon;
            Spell spell = wand.getSpell(Wand.getSpell(hotbarItem));
            String spellKey = null;
            if (spell == null) {
                if (skipEmpty) continue;
                icon = emptyIcon;
            } else {
                icon = spell.getGlyph();
                spellKey = spell.getSpellKey().getBaseKey();
            }

            // Add hotbar slot background
            glyphs += hotbarSlot;
            glyphs += hotbarSlotReverse;

            // Add active overlay
            String activeSpell = wand.getBaseActiveSpell();
            if (spellKey != null && activeSpell != null && spellKey.equals(activeSpell) && !hotbarSlotActive.isEmpty()) {
                glyphs += hotbarActiveSlotStart;
                glyphs += hotbarSlotActive;
                glyphs += hotbarActiveSlotEnd;
            }

            // Add icon with padding
            glyphs += hotbarIconPaddingLeft;
            glyphs += icon;

            // Add cooldown/disabled indicators
            if (showCooldown) {
                if (flashTime > 0 && now < lastCooldown + flashTime && lastCooldownSpell != null && lastCooldownSpell.getSpellKey().equals(spell.getSpellKey())) {
                    String cooldownIcon = messages.get("glyphs.cooldown.wait", "");
                    if (!cooldownIcon.isEmpty()) {
                        glyphs += iconReverse;
                        glyphs += cooldownIcon;
                    }
                } else {
                    int cooldownLevel = 0;
                    Long timeToCast = spell != null && spell instanceof MageSpell ? ((MageSpell)spell).getTimeToCast() : null;
                    Long maxTimeToCast = spell != null && spell instanceof MageSpell ? ((MageSpell)spell).getMaxTimeToCast() : null;

                    if (timeToCast == null || maxTimeToCast == null) {
                        cooldownLevel = 16;
                    } else if (maxTimeToCast > 0) {
                        cooldownLevel = (int)Math.ceil(16.0 * timeToCast / maxTimeToCast);
                    }
                    if (cooldownLevel > 0) {
                        String cooldownIcon = messages.get("glyphs.cooldown." + cooldownLevel, "");
                        if (!cooldownIcon.isEmpty()) {
                            glyphs += iconReverse;
                            glyphs += cooldownIcon;
                        }
                    }
                }
            }

            // Final icon padding to align to the slot frame
            glyphs += hotbarIconPaddingRight;

            // Animation if collapses
            if (collapseSpace != 0) {
                glyphs += collapseReverse;
            }

            // Add space in between each slot
            glyphs += slotSpacing;
            hotbarSlots++;
        }

        // Create the mana bar
        if (barWidth > 0 && !hasExtraMessage && barMode != WandDisplayMode.NONE) {
            // The farthest-left element needs to tbe left first one.
            // So we will need to choose at the end between the bar and the hotbar.
            String hotbarPart = hotbarPrefix + glyphs;
            String barPart = "";

            int barProgress = (int)Math.floor(barMode.getProgress(wand) * barSteps);
            String barGlyph = messages.get("glyphs.bar." + barProgress);
            Color wandColor = wand.getEffectColor();
            if (wandColor == null) {
                wandColor = Color.WHITE;
            }
            barPart += barTemplate.replace("$glyph", barGlyph).replace("$color", TextUtils.toHexString(wandColor.asRGB()));

            // Currently treating charges the same as mana
            if (flashTime > 0 && (now < lastInsufficientResource + flashTime || now < lastInsufficientCharges + flashTime)) {
                barPart += messages.getSpace(-(this.barWidth + 1));
                barPart += messages.get("glyphs.bar.insufficient");
            }

            // Why does this need this barSlotPadding fudge factor?
            int hotbarWidth = hotbarSlots * (hotbarSlotWidth + slotSpacingWidth + barSlotPadding);

            // If this bar is longer than the hotbar, put the bar first
            if (barWidth > hotbarWidth) {
                glyphs = barPart;
                // Add padding to center the hotbar within this bar
                int barPaddingLeft = (barWidth - hotbarWidth) / 2;
                // Back up to the beginning of the bar, plus the padding
                glyphs += messages.getSpace(-(1 + barWidth - barPaddingLeft));
                glyphs += hotbarPart;
                // End even with bar
                int barPaddingRight = (barWidth - hotbarWidth) - barPaddingLeft;
                glyphs += messages.getSpace(barPaddingRight);
            } else {
                // Add padding to center this bar within the hotbar
                int barPaddingLeft = (hotbarWidth - barWidth) / 2;
                // Back up to the beginning of the hotbar, plus the padding
                glyphs += messages.getSpace(-(1 + hotbarWidth - barPaddingLeft));
                glyphs += barPart;
                // End even with hotbar
                int barPaddingRight = (hotbarWidth - barWidth) - barPaddingLeft;
                glyphs += messages.getSpace(barPaddingRight);
            }
        }

        if (finalSpace != 0) {
            glyphs += finalPadding;
        }
        return glyphs;
    }

    public String getExtraMessage() {
        if (extraMessage != null) {
            long now = System.currentTimeMillis();
            if (now < lastExtraMessage + extraMessageDelay) {
                // If animating, wait for animation but don't clear the message
                if (now < lastExtraMessage + extraAnimationTime) {
                    int length = (int)Math.floor(extraMessage.length() * (now - lastExtraMessage) / extraAnimationTime);
                    return extraMessage.substring(0, length);
                }
                return extraMessage;
            }
        }
        extraMessage = null;
        return "";
    }

    public boolean handleActionBar(String message) {
        if (extraMessage == null) {
            lastExtraMessage = System.currentTimeMillis();
        }
        extraMessage = message;
        return true;
    }

    public boolean handleInsufficientResources(Spell spell, CastingCost cost) {
        lastInsufficientResource = System.currentTimeMillis();
        return true;
    }

    public boolean handleCooldown(Spell spell) {
        lastCooldown = System.currentTimeMillis();
        lastCooldownSpell = spell;
        return true;
    }

    public boolean handleInsufficientCharges(Spell spell) {
        lastInsufficientCharges = System.currentTimeMillis();
        return true;
    }

    public boolean isAnimating() {
        return extraMessage != null && System.currentTimeMillis() <= lastExtraMessage + extraAnimationTime;
    }
}
