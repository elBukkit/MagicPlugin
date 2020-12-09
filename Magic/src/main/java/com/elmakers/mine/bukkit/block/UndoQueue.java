package com.elmakers.mine.bukkit.block;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;

import org.bukkit.block.Block;

import com.elmakers.mine.bukkit.api.data.UndoData;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.spell.Spell;

public class UndoQueue implements com.elmakers.mine.bukkit.api.block.UndoQueue
{
    private final WeakReference<Mage>   owner;
    private final MageController        controller;
    private UndoList                    head = null;
    private UndoList                    tail = null;
    private int                         scheduledSize = 0;
    private int                         size = 0;
    private int                         maxSize    = 0;

    public UndoQueue(Mage mage)
    {
        this.owner = new WeakReference<>(mage);
        this.controller = mage.getController();
    }

    @Nullable
    protected Mage getOwner() {
        return owner == null ? null : owner.get();
    }

    @Override
    public void add(com.elmakers.mine.bukkit.api.block.UndoList blocks)
    {
        if (!(blocks instanceof UndoList)) return;
        UndoList addList = (UndoList)blocks;
        if (addList.hasUndoQueue()) {
            return;
        }
        Mage owner = getOwner();
        addList.setMage(owner);

        int iterations = 100;
        while (owner != null && !owner.isLoading() && maxSize > 0 && size > maxSize && iterations-- > 0)
        {
            UndoList expired = tail;
            if (expired != null)
            {
                if (expired.isScheduled())
                {
                    expired.undoScheduled();
                }
                else
                {
                    expired.commit();
                }
            }
        }

        addList.setUndoQueue(this);
        if (addList.isScheduled()) {
            scheduledSize++;
        }
        size++;
        if (head == null) {
            head = addList;
            tail = addList;
        } else {
            addList.setPrevious(head);
            head.setNext(addList);
            head = addList;
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
        if (list.isScheduled()) {
            scheduledSize--;
        }
        size--;
    }

    @Override
    public boolean hasScheduled() {
        return scheduledSize > 0;
    }

    public int undoScheduled(String spellKey)
    {
        int undid = 0;
        UndoList nextList = tail;
        while (nextList != null) {
            UndoList checkList = nextList;
            nextList = nextList.getNext();
            if (checkList.isScheduled()) {
                Spell spell = checkList.getSpell();
                if (spellKey == null || spell == null || (checkList.hasBeenScheduled() && spell.getSpellKey().getBaseKey().equals(spellKey))) {
                    checkList.undoScheduled(true);
                    undid++;
                }
            }
        }

        return undid;
    }

    @Override
    public int undoScheduled()
    {
        return undoScheduled(null);
    }

    @Override
    public boolean isEmpty()
    {
         return head == null;
    }

    @Nullable
    @Override
    public UndoList getLast()
    {
        return head;
    }

    @Nullable
    @Override
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

    @Nullable
    public UndoList undo()
    {
        return undoRecent(0);
    }

    @Nullable
    public UndoList undo(Block target)
    {
        return undoRecent(target, 0);
    }

    /**
     * Undo a recent construction performed by this Mage.
     *
     * <p>This will restore anything changed by the last-cast
     * construction spell, and remove that construction from
     * the Mage's UndoQueue.
     *
     * <p>It will skip undoing if the UndoList is older than
     * the specified timeout.
     *
     * @param timeout How long ago the UndoList may have been modified
     * @return The UndoList that was undone, or null if none.
     */
    @Nullable
    @Override
    public UndoList undoRecent(int timeout)
    {
        return undoRecent(timeout, null);
    }

    @Nullable
    @Override
    public UndoList undoRecent(int timeout, String spellKey)
    {
        UndoList undo = head;
        while (undo != null)
        {
            if (timeout > 0 && System.currentTimeMillis() - timeout > undo.getModifiedTime())
            {
                return null;
            }

            if (spellKey != null) {
                Spell spell = undo.getSpell();
                if (spell == null || !spell.getSpellKey().getBaseKey().equalsIgnoreCase(spellKey)) {
                    undo = head.getNext();
                    continue;
                }
            }

            undo.undo();
            return undo;
        }
        return null;
    }

    /**
     * Undo a recent construction performed by this Mage against the
     * given Block
     *
     * <p>This will restore anything changed by the last-cast
     * construction spell by this Mage that targeted the specific Block,
     * even if it was not the most recent Spell cast by that Mage.
     *
     * <p>It will skip undoing if the UndoList is older than
     * the specified timeout.
     *
     * @param block The block to check for modifications.
     * @param timeout How long ago the UndoList may have been modified
     * @return The UndoList that was undone, or null if the Mage has no constructions for the given Block.
     */
    @Nullable
    @Override
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

    @Override
    public void load(UndoData data)
    {
        try {
            if (data == null) return;
            List<com.elmakers.mine.bukkit.api.block.UndoList> undoList = data.getBlockList();
            if (undoList != null) {
                for (com.elmakers.mine.bukkit.api.block.UndoList list : undoList) {
                    add(list);
                    if (list.isScheduled() && list.hasChanges())
                    {
                        controller.scheduleUndo(list);
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            controller.getLogger().warning("Failed to load undo data: " + ex.getMessage());
        }
    }

    @Override
    public void save(UndoData data)
    {
        int maxSize = controller.getMaxUndoPersistSize();
        try {
            int discarded = 0;
            List<com.elmakers.mine.bukkit.api.block.UndoList> undoList = data.getBlockList();
            UndoList list = tail;
            while (list != null) {
                if (maxSize > 0 && list.size() > maxSize) {
                    discarded++;
                } else if (list.size() > 0) {
                    undoList.add(list);
                }

                list = list.getNext();
            }

            if (discarded > 0) {
                Mage owner = getOwner();
                String name = owner == null ? "(Offline)" : owner.getName();
                controller.getLogger().info("Not saving " + discarded + " undo batches for mage " + name + ", over max size of " + maxSize);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            controller.getLogger().warning("Failed to save undo data: " + ex.getMessage());
        }
    }

    @Override
    public int getSize()
    {
        return size;
    }

    @Override
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

    @Override
    public Collection<com.elmakers.mine.bukkit.api.block.UndoList> getAll() {
        List<com.elmakers.mine.bukkit.api.block.UndoList> list = new ArrayList<>();
        UndoList current = tail;
        while (current != null) {
            list.add(current);
            current = current.getPrevious();
        }
        return list;
    }

    public void skippedUndo(com.elmakers.mine.bukkit.api.block.UndoList undoList) {
        scheduledSize--;
    }
}
