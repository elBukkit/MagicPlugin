package com.elmakers.mine.bukkit.blocks;

import org.bukkit.Material;

public class MaterialBrushData extends MaterialAndData {
	protected String schematicName = "";

	public MaterialBrushData() {
		super();
	}
	
	public MaterialBrushData(final Material material, final  byte data) {
		super(material, data);
	}
	
	public MaterialBrushData(final Material material, final  byte data, final String schematicName) {
		super(material, data);
		this.schematicName = schematicName;
	}
	
	public MaterialBrushData(MaterialBrushData other) {
		updateFrom(other);
	}
	
	public void updateFrom(MaterialBrushData other) {
		super.updateFrom(other);
		schematicName = other.schematicName;
	}
	
	public MaterialBrushData(final Material material) {
		super(material);
	}
	
	public void setSchematicName(String name) {
		this.schematicName = name == null ? "" : name;
	}
	
	public String getSchematicName() {
		return schematicName;
	}
}
