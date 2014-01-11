package com.elmakers.mine.bukkit.utilities.borrowed;

import org.bukkit.Material;

public class MaterialAndData {
	private Material material;
	private byte data;
	
	public MaterialAndData(final Material material, final  byte data) {
		this.material = material;
		this.data = data;
	}
	
	public byte getData() {
		return data;
	}
	
	public Material getMaterial() {
		return material;
	}
}
