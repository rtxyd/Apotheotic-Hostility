package net.kayn.apotheotic_hostility.data;

public class MobLevelContext {
    private static final ThreadLocal<Integer> LEVEL = ThreadLocal.withInitial(() -> -1);

    public static void set(int level) {
        LEVEL.set(level);
    }

    public static int get() {
        return LEVEL.get();
    }

    public static void clear() {
        LEVEL.set(-1);
    }
}