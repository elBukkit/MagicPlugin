# TODO

NEW SPELLS:


OTHER STUFF:

 - Maybe data-drive blink's special list of things it will try to target through?
 - Allow adding materials with data e.g. /wand add material stained_glass_blue
   - support above in config files, too- ideally even for spell icons.
   - Related to above, refactor so spell materials don't matter, use nbt data.
 - managed to shoot myself with sniper :(
 - Maybe make wands regenerate while you're not holding them (timer-based)?
 - Use firework effects directly
 - Add sound effects (breaking on wand break... spell-specific)
 - Add other visual effects
 - Add server commands to create or upgrade wands

 - vortex type spells.. singularity, black hole, tornado - fun ideas there.
 - Persist player spell data

 - invincibility doesn't protect against magic or lightning?
 - somehow broke ALL my wands trading with a villager.. ? (.. changing to creative?)
 - can't recurse with colored clay? (hm, prolly 1.7 thing)
 - .. update docs, .. make new video
 - .. Factions support .. ?

DONE:

 - Show material names in lore since they don't always show up in the display name
 - force is wonky
 - Async mass block updates?
 - The skull_item seems to not play well with InventoryUtils :(
 - XP system broken without mana regen?	
 - Light spell- using client-side lighting hacks?
 - PotionEffectSpell: slow, haste, etc - also use in frost spell
 - Add pyramid construction type, use for blast and blob (makes steps)
 - fix SHELL at large radii - maybe keep current effect though? Looks cool.
 - Maybe change mana system to scale XP to one bar?
 - Cooldowns stopped working? Check sniper spell...
 * Allow combining wands on an anvil - Sort of... I think I need some Bukkit support here. Or, maybe it's not possible. :(
 - Add duration to spells for auto-deactivate after X ms
 - Add disarm spell
 - Need to drop armor on death with wand inventory open (!)
 - Test dieing while the wand inventory is open
 - make sure when a spell is removed that a wand still has an active spell
 - Add materials to random wands, always add a material when needed by a spell
 - add power property to wands and support in spells that can
 - randomizing shouldn't change active spell or material
 - Make sure max XP is always at least the highest-costing spell on a wand
 - Target through doors?
 * ^ Wand override for building material list? (Nah)
 - allow alter on entities (wolves, horses, cats, etc)
 - zombie/skeleton horses! Different cat breeds / dog collars?
 - materials don't store inventory locations
 - Fix XP system, use a mana system when wand has XP Regen, store accumulated XP, etc
 - grenades/explosion that do no block damage
 - remove chests from destructible (+ build able?) list
 - Make sure bedrock is not destructible
 - Decrease levitate flight speed?
 - add "break blocks" parameter to BoomSpell
 - fix health regeneration with max health (test with some plugin?)
 * Tried this, didn't work... waiting on forum confirmation, but sounds like a bust:
  - Look into using player portal event for the portal spell instead of move event

