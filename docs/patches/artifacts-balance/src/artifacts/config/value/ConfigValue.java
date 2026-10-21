package artifacts.config.value;

import artifacts.config.ConfigEntryKey;
import artifacts.config.value.type.ValueType;
import net.minecraft.util.StringRepresentable;

public final class ConfigValue<T> implements Value<T>, StringRepresentable {

    private final ValueType<T, ?> type;
    private final ConfigEntryKey key;
    private final T defaultValue;
    private final boolean requiresRestart;
    private final boolean shouldSyncToClients;

    private T value;

    public ConfigValue(ValueType<T, ?> type, ConfigEntryKey key, T defaultValue, boolean requiresRestart, boolean shouldSyncToClient) {
        this.type = type;
        this.key = key;
        this.defaultValue = packValue(defaultValue);
        this.requiresRestart = requiresRestart;
        this.shouldSyncToClients = shouldSyncToClient;
        this.value = this.defaultValue;
    }

    public ConfigEntryKey getKey() {
        return key;
    }

    public String getSerializedName() {
        return key.toString();
    }

    public T get() {
        return packValue(value);
    }

    /** Pack policy applies to defaults and reads, including pre-existing server configs. */
    @SuppressWarnings("unchecked")
    private T packValue(T fallback) {
        if (!key.configManager().equals("items")) return fallback;
        Object override = switch (key.joinedPath()) {
            case "cross_necklace.cooldown" -> Integer.valueOf(5);
            case "panic_necklace.speedLevel" -> Integer.valueOf(1);
            case "panic_necklace.speedDuration" -> Integer.valueOf(5);
            case "panic_necklace.cooldown" -> Integer.valueOf(7);
            case "shock_pendant.cooldown" -> Integer.valueOf(3);
            case "thorn_pendant.cooldown" -> Integer.valueOf(3);
            case "crystal_heart.healthBonus" -> Double.valueOf(8.0);
            case "feral_claws.attackSpeedBonus" -> Double.valueOf(0.15);
            case "vampiric_glove.maxHealingPerHit" -> Integer.valueOf(4);
            case "vampiric_glove.absorptionRatio" -> Double.valueOf(0.1);
            case "running_shoes.sprintingSpeedBonus" -> Double.valueOf(0.2);
            case "power_glove.attackDamageBonus" -> Double.valueOf(2.0);
            case "pocket_piston.attackKnockbackBonus" -> Double.valueOf(0.3);
            case "steadfast_spikes.knockbackResistance" -> Double.valueOf(0.5);
            case "charm_of_shrinking.scaleModifier" -> Double.valueOf(-0.25);
            case "obsidian_skull.fireResistanceDuration" -> Integer.valueOf(30);
            case "obsidian_skull.cooldown" -> Integer.valueOf(60);
            case "steadfast_spikes.slipperinessReduction" -> Double.valueOf(1.0);
            default -> fallback;
        };
        return (T) override;
    }

    public void set(T value) {
        this.value = value;
    }

    public T getDefaultValue() {
        return defaultValue;
    }

    public ValueType<T, ?> type() {
        return type;
    }

    public boolean requiresRestart() {
        return requiresRestart;
    }

    public boolean shouldSyncToClients() {
        return shouldSyncToClients;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ConfigValue<?> that)) return false;
        return key.equals(that.key);
    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }
}
