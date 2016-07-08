package com.elmakers.mine.bukkit.api.data;

import org.bukkit.Location;
import org.bukkit.Material;

public class BrushData {
    private Location cloneLocation;
    private Location cloneTarget;
    private Location materialTarget;
    private int mapId;
    private Material material;
    private Short materialData;
    private String schematicName;
    private double scale;
    private boolean fillWithAir;

    public Location getCloneLocation() {
        return cloneLocation;
    }

    public void setCloneLocation(Location cloneLocation) {
        this.cloneLocation = cloneLocation;
    }

    public Location getCloneTarget() {
        return cloneTarget;
    }

    public void setCloneTarget(Location cloneTarget) {
        this.cloneTarget = cloneTarget;
    }

    public Location getMaterialTarget() {
        return materialTarget;
    }

    public void setMaterialTarget(Location materialTarget) {
        this.materialTarget = materialTarget;
    }

    public int getMapId() {
        return mapId;
    }

    public void setMapId(int mapId) {
        this.mapId = mapId;
    }

    public Short getMaterialData() {
        return materialData;
    }

    public void setMaterialData(Short materialData) {
        this.materialData = materialData;
    }

    public String getSchematicName() {
        return schematicName;
    }

    public void setSchematicName(String schematicName) {
        this.schematicName = schematicName;
    }

    public double getScale() {
        return scale;
    }

    public void setScale(double scale) {
        this.scale = scale;
    }

    public boolean isFillWithAir() {
        return fillWithAir;
    }

    public void setFillWithAir(boolean fillWithAir) {
        this.fillWithAir = fillWithAir;
    }

    public Material getMaterial() {
        return material;
    }

    public void setMaterial(Material material) {
        this.material = material;
    }
}
