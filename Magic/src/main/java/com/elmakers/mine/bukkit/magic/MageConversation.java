package com.elmakers.mine.bukkit.magic;

import java.lang.ref.WeakReference;
import java.util.List;

import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.utility.CompatibilityLib;

public class MageConversation {
    private final Mage speaker;
    private final WeakReference<Player> targetPlayer;
    private final String formatString;
    private int nextLine;

    public MageConversation(Mage speaker, Player targetPlayer) {
        this.speaker = speaker;
        this.targetPlayer = new WeakReference<>(targetPlayer);
        formatString = speaker.getController().getMessages().get("npc.dialog");
    }

    /**
     * Returns true when finished
     */
    public boolean sayNextLine(List<String> dialog) {
        Player target = targetPlayer.get();
        if (target == null || nextLine >= dialog.size()) {
            return true;
        }
        String configuredLines = dialog.get(nextLine);
        if (!configuredLines.isEmpty()) {
            String[] lines = configuredLines.split("\n");
            for (String line : lines) {
                String message = formatString.replace("$line", line);
                message = message.replace("$speaker", speaker.getDisplayName())
                        .replace("$target", target.getDisplayName());
                target.sendMessage(CompatibilityLib.getCompatibilityUtils().translateColors(message));
            }
        }
        nextLine++;
        return nextLine >= dialog.size();
    }
}
