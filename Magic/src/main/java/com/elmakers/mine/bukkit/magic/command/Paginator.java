package com.elmakers.mine.bukkit.magic.command;

import java.util.List;
import javax.annotation.Nonnull;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public abstract class Paginator<T> {
    private int rowsPerPage = 8;

    @Nonnull
    protected abstract List<T> getList(CommandSender sender);
    @Nonnull
    protected abstract String describe(T item);
    @Nonnull
    protected abstract String getTypeNamePlural();

    protected void showItems(CommandSender sender, List<T> items, int start, int end) {
        for (int i = start; i < end; i++) {
            sender.sendMessage(describe(items.get(i)));
        }
    }

    public void list(CommandSender sender, String[] args) {
        int page = 0;
        String pageNumber = "?";
        if (args.length > 0) {
            try {
                pageNumber = args[0];
                page = Integer.parseInt(args[0]) - 1;
            } catch (Exception ex) {
                sender.sendMessage(ChatColor.RED + "Invalid page number: " + ChatColor.WHITE + args[0]);
                return;
            }
        }

        List<T> sorted = getList(sender);
        if (sorted.isEmpty()) {
            sender.sendMessage(ChatColor.RED + "No " + getTypeNamePlural() + " to list");
            return;
        }
        int start = page * rowsPerPage;
        int end = start + rowsPerPage;
        int pages = (int)Math.ceil((double)sorted.size() / rowsPerPage) + 1;
        if (start < 0 || start > sorted.size()) {
            sender.sendMessage(ChatColor.RED + "Invalid page number: " + ChatColor.WHITE + pageNumber
                + ChatColor.GRAY + "/" + ChatColor.GOLD + pages);
            return;
        }
        sender.sendMessage(ChatColor.AQUA + "Total " + getTypeNamePlural() + ": " + ChatColor.DARK_AQUA + sorted.size());
        showItems(sender, sorted, start, end);
        if (sorted.size() > rowsPerPage) {
            sender.sendMessage("  " + ChatColor.GRAY + "Page " + ChatColor.YELLOW
                + (page + 1) + ChatColor.GRAY + "/" + ChatColor.GOLD + pages);
        }
    }
}
