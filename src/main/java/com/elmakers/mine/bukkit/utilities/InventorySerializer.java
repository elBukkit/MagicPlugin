package com.elmakers.mine.bukkit.utilities;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.math.BigInteger;

import net.minecraft.server.v1_6_R3.NBTBase;
import net.minecraft.server.v1_6_R3.NBTTagCompound;
import net.minecraft.server.v1_6_R3.NBTTagList;

import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_6_R3.inventory.CraftInventoryCustom;
import org.bukkit.craftbukkit.v1_6_R3.inventory.CraftItemStack;
import org.bukkit.inventory.Inventory;
 
public class InventorySerializer 
{
    public static String inventoryToString(final Inventory inventory) {
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        final DataOutputStream dataOutput = new DataOutputStream(outputStream);
        final NBTTagList itemList = new NBTTagList();
        for (int i = 0; i < inventory.getSize(); i++) {
            final NBTTagCompound outputObject = new NBTTagCompound();
            net.minecraft.server.v1_6_R3.ItemStack craft = null;
            final org.bukkit.inventory.ItemStack is = inventory.getItem(i);
            if (is != null) {
                craft = CraftItemStack.asNMSCopy(is);
            } else {
                craft = null;
            }
            if (craft != null) {
                craft.save(outputObject);
            }
            itemList.add(outputObject);
        }
        NBTBase.a(itemList, dataOutput);
        return new BigInteger(1, outputStream.toByteArray()).toString(32);
    }
 
    public static Inventory stringToInventory(final String data, final String name) {
        final ByteArrayInputStream inputStream = new ByteArrayInputStream(new BigInteger(data, 32).toByteArray());
        final NBTTagList itemList = (NBTTagList) NBTBase.a(new DataInputStream(inputStream));
        final Inventory inventory = new CraftInventoryCustom(null, itemList.size(), ChatColor.translateAlternateColorCodes('&', name));
        for (int i = 0; i < itemList.size(); i++) {
            final NBTTagCompound inputObject = (NBTTagCompound) itemList.get(i);
            if (!inputObject.isEmpty()) {
                inventory.setItem(i, CraftItemStack.asBukkitCopy(net.minecraft.server.v1_6_R3.ItemStack.createStack(inputObject)));
            }
        }
        return inventory;
    }
}