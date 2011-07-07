package com.elmakers.mine.bukkit.utilities;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;

import com.elmakers.mine.bukkit.persistence.dao.MaterialList;

public class CSVParser
{
    public List<Integer> parseIntegers(String csvList)
    {
        List<Integer> ints = new ArrayList<Integer>();

        String[] intStrings = csvList.split(",");
        for (String s : intStrings)
        {
            try
            {
                int thisInt = Integer.parseInt(s.trim());
                ints.add(thisInt);
            }
            catch (NumberFormatException ex)
            {

            }
        }
        return ints;
    }
    
    public List<String> parseStrings(String csvList)
    {
        List<String> strings = new ArrayList<String>();

        String[] stringArray = csvList.split(",");
        for (String s : stringArray)
        {
            strings.add(s);
        }
        return strings;
    }

    public void parseMaterials(MaterialList materials, String csvList)
    {
        String[] matIds = csvList.split(",");
        for (String matId : matIds)
        {
            try
            {
                int typeId = Integer.parseInt(matId.trim());
                materials.add(Material.getMaterial(typeId));
            }
            catch (NumberFormatException ex)
            {

            }
        }
    }

    public MaterialList parseMaterials(String csvList)
    {
        MaterialList materials = new MaterialList();
        parseMaterials(materials, csvList);
        return materials;

    }

    public MaterialList parseMaterials(String listId, String csvList)
    {
        MaterialList materials = new MaterialList(listId);
        parseMaterials(materials, csvList);
        return materials;
    }
}
