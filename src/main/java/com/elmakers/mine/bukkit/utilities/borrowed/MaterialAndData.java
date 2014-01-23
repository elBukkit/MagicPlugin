package com.elmakers.mine.bukkit.utilities.borrowed;

import org.bukkit.Material;

public class MaterialAndData {
	protected Material material;
	protected byte data;
	
	public MaterialAndData(final Material material) {
		this.material = material;
		this.data = 0;
	}
	
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
	
	public String getKey() {
		String materialKey = material.name().toLowerCase();
		if (data != 0) {
			materialKey += ":" + data;
		}
		
		return materialKey;
	}
}
