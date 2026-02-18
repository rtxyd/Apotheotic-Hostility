package net.kayn.apotheotic_hostility.mixin;

import dev.shadowsoffire.apotheosis.adventure.boss.ApothBoss;
import dev.shadowsoffire.apotheosis.adventure.boss.BossRegistry;
import dev.xkmc.l2hostility.content.capability.chunk.ChunkDifficulty;
import dev.xkmc.l2hostility.content.capability.mob.MobTraitCap;
import net.kayn.apotheotic_hostility.ApotheoticHostility;
import net.kayn.apotheotic_hostility.data.BossScalingManager;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.ServerLevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(value = ApothBoss.class, remap = false)
public class ApothBossMixin {

    @Inject(
            method = "createBoss(Lnet/minecraft/world/level/ServerLevelAccessor;Lnet/minecraft/core/BlockPos;Lnet/minecraft/util/RandomSource;FLdev/shadowsoffire/apotheosis/adventure/loot/LootRarity;)Lnet/minecraft/world/entity/Mob;",
            at = @At("RETURN"),
            remap = false
    )
    private void afterCreateBoss(ServerLevelAccessor world, BlockPos pos, RandomSource random, float luck,
                                 dev.shadowsoffire.apotheosis.adventure.loot.LootRarity rarity,
                                 CallbackInfoReturnable<Mob> cir) {
        Mob entity = cir.getReturnValue();
        if (entity == null) return;

        ResourceLocation bossId = null;
        for (ResourceLocation key : BossRegistry.INSTANCE.getKeys()) {
            var boss = BossRegistry.INSTANCE.getValue(key);
            if (boss != null && boss == (Object) this) {
                bossId = key;
                break;
            }
        }
        if (bossId == null) return;

        BossScalingManager manager = BossScalingManager.getInstance();
        if (!manager.hasBossConfig(bossId)) return;

        if (!MobTraitCap.HOLDER.isProper(entity)) return;

        MobTraitCap cap = MobTraitCap.HOLDER.get(entity);
        var config = manager.getEntryForBoss(bossId).toEntityConfig();

        cap.setConfigCache(config);

        if (!cap.isInitialized()) {
            var opt = ChunkDifficulty.at(entity.level(), entity.blockPosition());
            if (opt.isPresent()) {
                cap.init(entity.level(), entity, opt.get());
                cap.dropRate = dev.xkmc.l2hostility.init.data.LHConfig.COMMON.dropRateFromSpawner.get();
            }
            return;
        }

        try {
            int curLv = cap.getLevel();
            boolean ok = cap.reinit(entity, curLv, false);
            cap.dropRate = dev.xkmc.l2hostility.init.data.LHConfig.COMMON.dropRateFromSpawner.get();
            ApotheoticHostility.LOGGER.debug("[BossScaling] reinit for {} ({}) returned {}", entity, bossId, ok);
        } catch (Throwable t) {
            ApotheoticHostility.LOGGER.error("[BossScaling] error while reinitializing MobTraitCap for boss {}", entity, t);
        }
    }
}