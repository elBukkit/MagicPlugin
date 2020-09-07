package com.elmakers.mine.bukkit.magic.command;

import java.util.ArrayList;
import java.util.Collection;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import com.elmakers.mine.bukkit.api.magic.MagicAPI;

public class SpellsCommandExecutor extends MagicTabExecutor {
    private final SpellsPaginator spellsPaginator;

    public SpellsCommandExecutor(MagicAPI api) {
        super(api, "spells");
        spellsPaginator = new SpellsPaginator(api.getController());
    }

    @Override
    public Collection<String> onTabComplete(CommandSender sender, String comandName, String[] args) {
        return new ArrayList<>();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!api.hasPermission(sender, getPermissionNode())) {
            sendNoPermission(sender);
            return true;
        }
        spellsPaginator.list(sender, args);
        return true;
    }
}
