package net.kayn.apotheotic_hostility.event;

import dev.shadowsoffire.apotheosis.adventure.boss.BossRegistry;
import dev.xkmc.l2hostility.content.capability.chunk.ChunkDifficulty;
import dev.xkmc.l2hostility.content.capability.mob.MobTraitCap;
import dev.xkmc.l2hostility.init.data.LHConfig;
import net.kayn.apotheotic_hostility.ApotheoticHostility;
import net.kayn.apotheotic_hostility.data.BossScalingConfig;
import net.kayn.apotheotic_hostility.data.BossScalingManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;

@Mod.EventBusSubscriber(modid = ApotheoticHostility.MOD_ID)
public class BossScalingEventHandler {

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onBossJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) return;
        if (!(event.getEntity() instanceof Mob mob)) return;
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
        var entry = manager.getEntryForBoss(bossId);
        if (entry == null) return;

        if (!MobTraitCap.HOLDER.isProper(mob)) return;
        var cap = MobTraitCap.HOLDER.get(mob);

        try {
            cap.setConfigCache(entry.toEntityConfig());
        } catch (Throwable t) {
            ApotheoticHostility.LOGGER.warn("[BossScaling] could not set config cache for {}: {}", bossId, t.getMessage());
        }
        if (!cap.isInitialized()) {
            var opt = ChunkDifficulty.at(mob.level(), mob.blockPosition());
            if (opt.isPresent()) {
                try {
                    cap.init(mob.level(), mob, opt.get());
                    cap.dropRate = LHConfig.COMMON.dropRateFromSpawner.get();
                } catch (Throwable t) {
                    ApotheoticHostility.LOGGER.warn("[BossScaling] cap.init error for {}: {}", bossId, t.getMessage());
                }
            } else {
                ApotheoticHostility.LOGGER.debug("[BossScaling] chunk difficulty not ready for {}, skipping validation", bossId);
                return;
            }
        }

        Map<ResourceLocation, Integer> reqs = entry.getRarityRequirementMap();
        if (reqs != null && !reqs.isEmpty()) {
            int level = cap.getLevel();
            boolean anyEligible = reqs.values().stream().anyMatch(min -> level >= min);
            if (!anyEligible) {
                ApotheoticHostility.LOGGER.info("[BossScaling] Discarding boss {} (level {}) - no eligible rarity", bossId, level);
                event.setCanceled(true);
                return;
            }

            String rarityStr = mob.getPersistentData().getString("apoth.rarity");
            if (rarityStr != null && !rarityStr.isEmpty()) {
                try {
                    ResourceLocation assigned = new ResourceLocation(rarityStr);
                    Integer req = reqs.get(assigned);
                    if (req != null && level < req) {
                        ApotheoticHostility.LOGGER.info("[BossScaling] Discarding boss {} with assigned rarity {} (level {} < required {})", bossId, assigned, level, req);
                        event.setCanceled(true);
                        return;
                    }
                } catch (Exception ex) {
                    ApotheoticHostility.LOGGER.warn("[BossScaling] invalid apoth.rarity stored: {}", rarityStr);
                }
            }
        }
        if (cap.shouldDiscard(mob)) {
            ApotheoticHostility.LOGGER.info("[BossScaling] Discarding boss {} due to minSpawnLevel (level {})", bossId, cap.getLevel());
            event.setCanceled(true);
        }
    }
}