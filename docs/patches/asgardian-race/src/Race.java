package com.eiden.iourusraces.race;

public enum Race {
    NONE(1.0f, 20.0, 1.0),
    HUMAN(1.0f, 20.0, 1.0),
    ORC(1.25f, 24.0, 0.9),
    GOBLIN(0.6f, 16.0, 1.15),
    ELF(1.1f, 18.0, 1.05),
    DWARF(0.8f, 22.0, 0.95),
    MELFORK(1.0f, 20.0, 1.1),
    DRACONIC(1.1f, 28.0, 0.9),
    ASGARDIAN(1.05f, 22.0, 0.95);

    private final float scale;
    private final double maxHealth;
    private final double speedMultiplier;

    private Race(float scale, double maxHealth, double speedMultiplier) {
        this.scale = scale;
        this.maxHealth = maxHealth;
        this.speedMultiplier = speedMultiplier;
    }

    public float getScale() {
        return this.scale;
    }

    public double getMaxHealth() {
        return this.maxHealth;
    }

    public double getSpeedMultiplier() {
        return this.speedMultiplier;
    }
}
