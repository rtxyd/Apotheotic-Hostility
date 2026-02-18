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

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onBossJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) return;
        if (!(event.getEntity() instanceof Mob mob)) return;
        if (!mob.getPersistentData().getBoolean("apoth.boss")) return;

        if (!MobTraitCap.HOLDER.isProper(mob)) return;

        MobTraitCap cap = MobTraitCap.HOLDER.get(mob);

        if (!cap.isInitialized()) {
            var opt = ChunkDifficulty.at(mob.level(), mob.blockPosition());
            if (opt.isPresent()) {
                cap.init(mob.level(), mob, opt.get());
                cap.dropRate = LHConfig.COMMON.dropRateFromSpawner.get();
            }
        }

        if (cap.shouldDiscard(mob)) {
            event.setCanceled(true);
        }
    }
}