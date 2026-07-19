package com.elmakers.mine.bukkit.utility;

public record IntVector(int x, int y, int z) {
    public static final IntVector ZERO = new IntVector(0, 0, 0);

    public boolean isZero() {
        return x == 0 && y == 0 && z == 0;
    }

    public IntVector add(IntVector other) {
        if (other.isZero()) return this;
        return new IntVector(this.x + other.x, this.y + other.y, this.z + other.z);
    }

    public IntVector addY(int y) {
        if (y == 0) return this;
        return new IntVector(this.x, this.y + y, this.z);
    }
}
