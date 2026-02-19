package net.kayn.apotheotic_hostility.mixin;

import dev.shadowsoffire.apotheosis.adventure.boss.BossEvents;
import dev.shadowsoffire.apotheosis.adventure.boss.BossRegistry;
import dev.shadowsoffire.apotheosis.adventure.loot.RarityRegistry;
import net.kayn.apotheotic_hostility.ApotheoticHostility;
import net.kayn.apotheotic_hostility.data.BossScalingManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.event.entity.living.MobSpawnEvent.FinalizeSpawn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(value = BossEvents.class, remap = false)
public class BossEventsMixin {

    @Inject(
            method = "naturalBosses",
            at = @At(
                    value = "INVOKE",
                    target = "Ldev/shadowsoffire/apotheosis/adventure/boss/BossEvents;canSpawn(Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/world/entity/Mob;D)Z",
                    shift = At.Shift.BEFORE
            ),
            cancellable = true,
            locals = LocalCapture.CAPTURE_FAILHARD,
            remap = false
    )
    private void afterCreateBoss(FinalizeSpawn e, CallbackInfo ci,
                                 LivingEntity entity, RandomSource rand,
                                 net.minecraft.world.level.ServerLevelAccessor sLevel,
                                 ResourceLocation dimId,
                                 org.apache.commons.lang3.tuple.Pair rules,
                                 net.minecraft.world.entity.player.Player player,
                                 dev.shadowsoffire.apotheosis.adventure.boss.ApothBoss item,
                                 Mob boss) {
        try {
            if (boss == null) return;

            ResourceLocation bossId = null;
            for (ResourceLocation key : BossRegistry.INSTANCE.getKeys()) {
                if (BossRegistry.INSTANCE.getValue(key) == item) {
                    bossId = key;
                    break;
                }
            }
            if (bossId == null) return;

            BossScalingManager manager = BossScalingManager.getInstance();
            if (!manager.hasBossConfig(bossId)) return;

            var entry = manager.getEntryForBoss(bossId);
            var reqs = entry.getRarityRequirementMap();
            if (reqs == null || reqs.isEmpty()) return;

            String rarityStr = boss.getPersistentData().getString("apoth.rarity");
            if (rarityStr == null || rarityStr.isEmpty()) return;

            try {
                ResourceLocation rarityId = new ResourceLocation(rarityStr);
                Integer requiredLevel = reqs.get(rarityId);

                if (requiredLevel != null) {
                    if (!dev.xkmc.l2hostility.content.capability.mob.MobTraitCap.HOLDER.isProper(boss)) return;
                    var cap = dev.xkmc.l2hostility.content.capability.mob.MobTraitCap.HOLDER.get(boss);

                    if (cap.isInitialized()) {
                        int actualLevel = cap.getLevel();
                        if (actualLevel < requiredLevel) {
                            ApotheoticHostility.LOGGER.info(
                                    "[BossGuard] Preventing {} announcement - rarity {} requires level {}, got {}",
                                    bossId, rarityId, requiredLevel, actualLevel
                            );
                            boss.discard();
                            ci.cancel();
                            return;
                        }
                    }
                }

            } catch (Exception ex) {
                ApotheoticHostility.LOGGER.warn("[BossGuard] Error parsing rarity: {}", rarityStr);
            }

        } catch (Throwable t) {
            ApotheoticHostility.LOGGER.error("[BossGuard] Error checking boss", t);
        }
    }
}