package net.kayn.apotheotic_hostility.spawner;

import dev.shadowsoffire.apotheosis.spawn.modifiers.SpawnerStats;

public class HostilitySpawnerStats {

    public static void register() {
        SpawnerStats.REGISTRY.put(LevelStat.INSTANCE.getId(), LevelStat.INSTANCE);
    }
}