package com.elmakers.mine.bukkit.block;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;

import com.elmakers.mine.bukkit.api.block.UndoList;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;

public class UndoQueue implements com.elmakers.mine.bukkit.api.block.UndoQueue
{
    private final Mage					owner;
    private final LinkedList<UndoList> 	changeQueue = new LinkedList<UndoList>();
    private final Set<UndoList> 		scheduledBlocks = new HashSet<UndoList>();
    private int                         maxSize    = 0;

    public UndoQueue(Mage mage)
    {
        this.owner = mage;
    }

    @Override
    public void add(UndoList blocks)
    {
        if (maxSize > 0 && changeQueue.size() > maxSize)
        {
            UndoList expired = changeQueue.removeFirst();
            expired.commit();
        }
        changeQueue.add(blocks);
    }

    public void scheduleCleanup(UndoList blocks)
    {
        scheduledBlocks.add(blocks);
        blocks.scheduleCleanup();
    }

    public void undoScheduled()
    {
        if (scheduledBlocks.size() == 0) return;
        for (UndoList list : scheduledBlocks) {
            list.undoScheduled();
        }
        scheduledBlocks.clear();
    }

    public boolean isEmpty()
    {
         return scheduledBlocks.isEmpty() && changeQueue.isEmpty();
    }

    public void removeScheduledCleanup(UndoList blockList)
    {
        scheduledBlocks.remove(blockList);
    }

    public UndoList getLast()
    {
        if (changeQueue.isEmpty())
        {
            return null;
        }
        return changeQueue.getLast();
    }

    public UndoList getLast(Block target)
    {
        if (changeQueue.size() == 0)
        {
            return null;
        }
        for (UndoList blocks : changeQueue)
        {
            if (blocks.contains(target))
            {
                return blocks;
            }
        }
        return null;
    }

    public void setMaxSize(int size)
    {
        maxSize = size;
    }

    public UndoList undo()
    {
        return undoRecent(0);
    }

    public UndoList undo(Block target)
    {
        return undoRecent(target, 0);
    }

    /**
     * Undo a recent construction performed by this Mage.
     *
     * This will restore anything changed by the last-cast
     * construction spell, and remove that construction from
     * the Mage's UndoQueue.
     *
     * It will skip undoing if the UndoList is older than
     * the specified timeout.
     *
     * @param timeout How long ago the UndoList may have been modified
     * @return The UndoList that was undone, or null if none.
     */
    public UndoList undoRecent(int timeout)
    {
        if (changeQueue.size() == 0)
        {
            return null;
        }

        UndoList blocks = changeQueue.getLast();
        if (timeout > 0 && System.currentTimeMillis() - timeout > blocks.getModifiedTime())
        {
            return null;
        }

        changeQueue.removeLast();
        if (blocks.undo()) {
            return blocks;
        }

        changeQueue.add(blocks);
        return null;
    }

    /**
     * Undo a recent construction performed by this Mage against the
     * given Block
     *
     * This will restore anything changed by the last-cast
     * construction spell by this Mage that targeted the specific Block,
     * even if it was not the most recent Spell cast by that Mage.
     *
     * It will skip undoing if the UndoList is older than
     * the specified timeout.
     *
     * @param block The block to check for modifications.
     * @param timeout How long ago the UndoList may have been modified
     * @return The UndoList that was undone, or null if the Mage has no constructions for the given Block.
     */
    public UndoList undoRecent(Block block, int timeout)
    {
        UndoList lastActionOnTarget = getLast(block);

        if (lastActionOnTarget == null)
        {
            return null;
        }

        if (timeout > 0 && System.currentTimeMillis() - timeout > lastActionOnTarget.getModifiedTime())
        {
            return null;
        }

        if (lastActionOnTarget.undo()) {
            changeQueue.remove(lastActionOnTarget);
            return lastActionOnTarget;
        }

        return null;
    }

    public void load(ConfigurationSection node)
    {
        try {
            if (node == null) return;
            Collection<ConfigurationSection> nodeList = ConfigurationUtils.getNodeList(node, "undo");
            if (nodeList != null) {
                for (ConfigurationSection listNode : nodeList) {
                    UndoList list = new com.elmakers.mine.bukkit.block.UndoList(owner);
                    list.load(listNode);
                    changeQueue.add(list);
                }
            }
            nodeList = ConfigurationUtils.getNodeList(node, "scheduled");
            if (nodeList != null) {
                for (ConfigurationSection listNode : nodeList) {
                    UndoList list = new com.elmakers.mine.bukkit.block.UndoList(owner);
                    list.load(listNode);
                    scheduleCleanup(list);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            owner.getController().getLogger().warning("Failed to load undo data: " + ex.getMessage());
        }
    }

    public void save(ConfigurationSection node)
    {
        MageController controller = owner.getController();
        int maxSize = controller.getMaxUndoPersistSize();
        try {
            int discarded = 0;
            List<Map<String, Object>> nodeList = new ArrayList<Map<String, Object>>();
            for (UndoList list : changeQueue) {
                if (maxSize > 0 && list.size() > maxSize) {
                    discarded++;
                    continue;
                }
                MemoryConfiguration listNode = new MemoryConfiguration();
                list.save(listNode);
                nodeList.add(listNode.getValues(true));
            }
            if (discarded > 0) {
                controller.getLogger().info("Not saving " + discarded + " undo batches for mage " + owner.getName() + ", over max size of " + maxSize);
            }
            node.set("undo", nodeList);
            nodeList = new ArrayList<Map<String, Object>>();
            for (UndoList list : scheduledBlocks) {
                MemoryConfiguration listNode = new MemoryConfiguration();
                list.save(listNode);
                nodeList.add(listNode.getValues(true));
            }
            node.set("scheduled", nodeList);
        } catch (Exception ex) {
            ex.printStackTrace();
            controller.getLogger().warning("Failed to save undo data: " + ex.getMessage());
        }
    }

    public int getSize()
    {
        return changeQueue.size();
    }

    public boolean commit()
    {
        if (changeQueue.size() == 0) return false;
        for (UndoList list : changeQueue) {
            list.commit();
        }
        changeQueue.clear();
        return true;
    }
}
