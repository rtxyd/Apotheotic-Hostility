package net.kayn.apotheotic_hostility.event;

import dev.shadowsoffire.apotheosis.adventure.boss.BossRegistry;
import dev.xkmc.l2hostility.content.capability.chunk.ChunkDifficulty;
import dev.xkmc.l2hostility.content.capability.mob.MobTraitCap;
import dev.xkmc.l2hostility.init.data.LHConfig;
import net.kayn.apotheotic_hostility.data.BossScalingManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class BossScalingEventHandler {

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onBossJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) return;
        if (!(event.getEntity() instanceof Mob)) return;
        Mob mob = (Mob) event.getEntity();

        if (!mob.getPersistentData().getBoolean("apoth.boss")) return;

        ResourceLocation bossId = null;
        for (ResourceLocation key : BossRegistry.INSTANCE.getKeys()) {
            var apothBoss = BossRegistry.INSTANCE.getValue(key);
            if (apothBoss != null && apothBoss.getEntity() == mob.getType()) {
                bossId = key;
                break;
            }
        }

        if (bossId == null) return;

        BossScalingManager manager = BossScalingManager.getInstance();
        if (!manager.hasBossConfig(bossId)) return;

        initMob(mob, manager.getEntryForBoss(bossId).toEntityConfig());
    }

    private static void initMob(Mob mob, dev.xkmc.l2hostility.content.config.EntityConfig.Config config) {
        if (MobTraitCap.HOLDER.isProper(mob)) {
            MobTraitCap cap = MobTraitCap.HOLDER.get(mob);
            if (!mob.level().isClientSide() && !cap.isInitialized()) {
                var opt = ChunkDifficulty.at(mob.level(), mob.blockPosition());
                if (opt.isPresent()) {
                    cap.setConfigCache(config);
                    cap.init(mob.level(), mob, opt.get());
                    cap.dropRate = LHConfig.COMMON.dropRateFromSpawner.get();
                }
            }
        }
    }
}