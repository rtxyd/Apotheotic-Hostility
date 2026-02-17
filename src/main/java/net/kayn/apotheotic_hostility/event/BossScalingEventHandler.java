package net.kayn.apotheotic_hostility.event;

import dev.shadowsoffire.apotheosis.adventure.boss.BossRegistry;
import dev.xkmc.l2hostility.content.capability.chunk.ChunkDifficulty;
import dev.xkmc.l2hostility.content.capability.mob.MobTraitCap;
import dev.xkmc.l2hostility.content.config.EntityConfig;
import dev.xkmc.l2hostility.init.data.LHConfig;
import net.kayn.apotheotic_hostility.data.BossScalingConfig;
import net.kayn.apotheotic_hostility.data.BossScalingManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class BossScalingEventHandler {

    @SubscribeEvent
    public static void onEntityJoinWorld(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) return;
        if (!(event.getEntity() instanceof Mob mob)) return;
        if (!mob.getPersistentData().contains("apoth.boss")) return;

        ResourceLocation bossId = getBossId(mob);
        if (bossId == null) return;

        BossScalingManager manager = BossScalingManager.getInstance();
        if (!manager.hasBossConfig(bossId)) return;

        applyScaling(mob, manager.getEntryForBoss(bossId));
    }

    private static ResourceLocation getBossId(Mob mob) {
        for (ResourceLocation key : BossRegistry.INSTANCE.getKeys()) {
            var boss = BossRegistry.INSTANCE.getValue(key);
            if (boss != null && boss.getEntity() == mob.getType()) {
                return key;
            }
        }
        return null;
    }

    private static void applyScaling(Mob mob, BossScalingConfig.BossScalingEntry entry) {
        if (!MobTraitCap.HOLDER.isProper(mob)) return;

        MobTraitCap cap = MobTraitCap.HOLDER.get(mob);
        if (cap.isInitialized()) return;

        var chunkDifficulty = ChunkDifficulty.at(mob.level(), mob.blockPosition());
        if (chunkDifficulty.isEmpty()) return;

        EntityConfig.Config config = entry.toEntityConfig();
        cap.setConfigCache(config);
        cap.init(mob.level(), mob, chunkDifficulty.get());
        cap.dropRate = LHConfig.COMMON.dropRateFromSpawner.get();
    }
}