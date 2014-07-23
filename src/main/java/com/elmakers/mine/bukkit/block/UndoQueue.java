package com.elmakers.mine.bukkit.block;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;

import com.elmakers.mine.bukkit.block.UndoList;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;

public class UndoQueue implements com.elmakers.mine.bukkit.api.block.UndoQueue
{
    private final Mage					owner;
    private UndoList                    head = null;
    private UndoList                    tail = null;
    private int                         size = 0;
    private int                         maxSize    = 0;

    public UndoQueue(Mage mage)
    {
        this.owner = mage;
    }

    @Override
    public void add(com.elmakers.mine.bukkit.api.block.UndoList blocks)
    {
        if (!(blocks instanceof UndoList)) return;
        UndoList addList = (UndoList)blocks;
        if (maxSize > 0 && size > maxSize)
        {
            UndoList expired = tail;
            if (expired != null)
            {
                if (expired.isScheduled())
                {
                    expired.undo();
                }
                else
                {
                    expired.commit();
                }
            }
        }

        addList.setUndoQueue(this);
        size++;
        if (head == null) {
            head = addList;
            tail = addList;
        } else {
            addList.setPrevious(head);
            head.setNext(addList);
            head = addList;
        }

        if (addList.isScheduled())
        {
            owner.getController().scheduleUndo(addList);
        }
    }

    public void removed(UndoList list)
    {
        if (list == head) {
            head = list.getPrevious();
        }
        if (list == tail) {
            tail = list.getNext();
        }
        size--;
    }

    public int undoScheduled()
    {
        int undid = 0;
        UndoList nextList = tail;
        while (nextList != null) {
            UndoList checkList = nextList;
            nextList = nextList.getNext();
            if (checkList.isScheduled()) {
                checkList.undo();
                undid++;
            }
        }

        return undid;
    }

    public boolean isEmpty()
    {
         return head == null;
    }

    public UndoList getLast()
    {
        return head;
    }

    public UndoList getLast(Block target)
    {
        UndoList checkList = head;
        while (checkList != null)
        {
            if (checkList.contains(target))
            {
                return checkList;
            }
            checkList = checkList.getPrevious();
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
        if (head == null)
        {
            return null;
        }

        if (timeout > 0 && System.currentTimeMillis() - timeout > head.getModifiedTime())
        {
            return null;
        }

        UndoList undid = head;
        undid.undo();
        return undid;
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

        lastActionOnTarget.undo();
        return lastActionOnTarget;
    }

    public void load(ConfigurationSection node)
    {
        try {
            if (node == null) return;
            Collection<ConfigurationSection> nodeList = ConfigurationUtils.getNodeList(node, "undo");

            if (nodeList != null) {
                for (ConfigurationSection listNode : nodeList) {
                    UndoList list = new com.elmakers.mine.bukkit.block.UndoList(owner, null);
                    list.load(listNode);
                    add(list);
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
            UndoList list = tail;
            while (list != null) {
                if (maxSize > 0 && list.size() > maxSize) {
                    discarded++;
                } else {
                    MemoryConfiguration listNode = new MemoryConfiguration();
                    list.save(listNode);
                    nodeList.add(listNode.getValues(true));
                }

                list = list.getNext();
            }

            if (discarded > 0) {
                controller.getLogger().info("Not saving " + discarded + " undo batches for mage " + owner.getName() + ", over max size of " + maxSize);
            }
            node.set("undo", nodeList);
        } catch (Exception ex) {
            ex.printStackTrace();
            controller.getLogger().warning("Failed to save undo data: " + ex.getMessage());
        }
    }

    public int getSize()
    {
        return size;
    }

    public boolean commit()
    {
        UndoList nextList = tail;
        while (nextList != null) {
            UndoList list = nextList;
            nextList = nextList.getNext();

            list.commit();
        }

        head = null;
        tail = null;
        size = 0;
        return true;
    }
}
