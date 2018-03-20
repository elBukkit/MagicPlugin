package com.elmakers.mine.bukkit.integration.skript;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Checker;
import ch.njol.util.Kleenean;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.wand.Wand;
import com.elmakers.mine.bukkit.magic.MagicPlugin;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;

@Name("Has Magic Item")
@Description("Checks whether a player has a specific magc item or wand.")
@Examples({"player has \"emerald_sword\"", "player has \"lightsaber\" in offhand", "player does not have \"battle\"", "player is wearing \"magneticleggings\""})
public class CondHasItem extends Condition {
    private Expression<Entity> entities;
    private Expression<String> itemKeys;
    private boolean offhand;
    private boolean armor;

    public static void register() {
		Skript.registerCondition(CondHasItem.class,
                "[%entities%] ha(s|ve) [wand] [%-strings%] in [main] hand",
			"[%entities%] [(is|are)] holding [wand] [%-strings%] [in main hand]",
			"[%entities%] ha(s|ve) [wand] [%-strings%] in off[(-| )]hand",
			"[%entities%] [(is|are)] holding [wand] [%-strings%] in off[(-| )]hand",
			"[%entities%] (ha(s|ve) not|do[es]n't have) [wand] [%-strings%] in [main] hand",
			"[%entities%] [(is|are)] wearing [wand] [%-strings%]",
			"[%entities%] (is not|isn't) holding [wand] [%-strings%] [in main hand]",
			"[%entities%] (ha(s|ve) not|do[es]n't have) [wand] [%-strings%] in off[(-| )]hand",
			"[%entities%] (is not|isn't) holding [wand] [%-strings%] in off[(-| )]hand",
			"[%entities%] (is not|isn't) wearing [wand] [%-strings%]"
		);
    }

    @SuppressWarnings({"unchecked"})
	@Override
	public boolean init(final Expression<?>[] vars, final int matchedPattern, final Kleenean isDelayed, final SkriptParser.ParseResult parseResult) {
		entities = (Expression<Entity>) vars[0];
		itemKeys = (Expression<String>) vars[1];
		offhand = (matchedPattern == 2 || matchedPattern == 3 || matchedPattern == 7 || matchedPattern == 8);
		armor = (matchedPattern == 5 || matchedPattern == 9);
		setNegated(matchedPattern >= 6);
		return true;
	}

	@Override
	public boolean check(final Event e) {
		return entities.check(e, new Checker<Entity>() {
			@Override
			public boolean check(final Entity entity) {
			    final MageController controller = MagicPlugin.getAPI().getController();
			    final Mage mage = controller.getRegisteredMage(entity);
			    if (mage == null) {
			        return false;
                }
                final Wand wand = offhand ? mage.getOffhandWand() : mage.getActiveWand();
			    if (itemKeys == null) {
			    	return (wand != null) != isNegated();
				}
				final LivingEntity living = mage.getLivingEntity();
			    final ItemStack item = wand == null && living != null ?
					(offhand ? living.getEquipment().getItemInOffHand() : living.getEquipment().getItemInMainHand())
					: null;
				return itemKeys.check(e, new Checker<String>() {
					@Override
					public boolean check(final String targetKey) {
						if (armor) {
							if (living == null) return false;
							for (ItemStack armorItem : living.getEquipment().getArmorContents()) {
								if (armorItem == null) continue;
								String key = controller.getWandKey(armorItem);
								if (key != null && key.equalsIgnoreCase(targetKey)) return true;
								key = controller.getItemKey(armorItem);
								if (key != null && key.equalsIgnoreCase(targetKey)) return true;
							}
							return false;
						}

						if (wand != null && wand.getTemplateKey().equalsIgnoreCase(targetKey)) {
							return true;
						}
						if (item != null) {
							String itemKey = controller.getItemKey(item);
							if (itemKey != null && itemKey.equalsIgnoreCase(targetKey)) {
								return true;
							}
						}
						return false;
					}
				}, isNegated());
			}
		});
	}

	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
        String itemTypes = itemKeys == null ? "" : itemKeys.toString();
		return entities.toString(e, debug) + (entities.isSingle() ? " has" : " have") + (isNegated() ? " not" : "") + (armor ? " wearing " : " ") + itemTypes;
	}
}
