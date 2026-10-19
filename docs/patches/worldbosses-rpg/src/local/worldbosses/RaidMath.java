package local.worldbosses;

import java.util.ArrayDeque;

/** Shared arithmetic; no Minecraft dependency. */
public final class RaidMath {
    private RaidMath() {}
    public static int players(int count) { return Math.max(1, Math.min(8, count)); }
    public static double health(double base, int count) { return base * (1 + .15 * (players(count) - 1)); }
    public static double regeneration(double base, int count) { return base * (1 + .10 * (players(count) - 1)); }
    public static double armor(double base, int count) { return Math.min(30, regeneration(base, count)); }
    public static final class Budget {
        private final float limit;
        public Budget(){this(12);}
        public Budget(float limit){this.limit=limit;}
        private record Hit(long tick, float amount) {}
        private final ArrayDeque<Hit> hits = new ArrayDeque<>();
        public float take(long tick, float requested) {
            while (!hits.isEmpty() && tick - hits.peekFirst().tick >= 20) hits.removeFirst();
            if (!Float.isFinite(requested) || requested <= 0) return 0;
            float used = 0;
            for (Hit hit : hits) used += hit.amount;
            float allowed = Math.max(0, Math.min(requested, limit - used));
            if (allowed > 0) hits.addLast(new Hit(tick, allowed));
            return allowed;
        }
    }
}
