package net.kayn.apotheotic_hostility.mixin;

import dev.shadowsoffire.apotheosis.adventure.boss.ApothBoss;
import dev.shadowsoffire.apotheosis.adventure.boss.BossRegistry;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.apotheosis.adventure.loot.RarityRegistry;
import dev.shadowsoffire.apotheosis.util.SupportingEntity;
import dev.xkmc.l2hostility.content.capability.chunk.ChunkDifficulty;
import dev.xkmc.l2hostility.content.capability.mob.MobTraitCap;
import net.kayn.apotheotic_hostility.ApotheoticHostility;
import net.kayn.apotheotic_hostility.data.BossScalingConfig;
import net.kayn.apotheotic_hostility.data.BossScalingManager;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.ServerLevelAccessor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Mixin(value = ApothBoss.class, remap = false)
public class ApothBossMixin {

    @Final
    @Shadow @Nullable
    protected CompoundTag nbt;

    @Final
    @Shadow @Nullable
    protected SupportingEntity mount;

    @Inject(
            method = "createBoss(Lnet/minecraft/world/level/ServerLevelAccessor;Lnet/minecraft/core/BlockPos;Lnet/minecraft/util/RandomSource;FLdev/shadowsoffire/apotheosis/adventure/loot/LootRarity;)Lnet/minecraft/world/entity/Mob;",
            at = @At(
                    value = "INVOKE",
                    target = "Ldev/shadowsoffire/apotheosis/adventure/boss/ApothBoss;initBoss(Lnet/minecraft/util/RandomSource;Lnet/minecraft/world/entity/Mob;FLdev/shadowsoffire/apotheosis/adventure/loot/LootRarity;)V",
                    shift = At.Shift.BEFORE
            ),
            locals = LocalCapture.CAPTURE_FAILHARD,
            cancellable = true,
            remap = false
    )
    private void beforeInitBoss(ServerLevelAccessor world, BlockPos pos, net.minecraft.util.RandomSource random, float luck,
                                LootRarity rarity,
                                CallbackInfoReturnable<Mob> cir,
                                CompoundTag fakeNbt, Mob entity) {
        try {
            if (rarity != null) return;

            ResourceLocation bossId = null;
            for (ResourceLocation key : BossRegistry.INSTANCE.getKeys()) {
                var apoth = BossRegistry.INSTANCE.getValue(key);
                if (apoth != null && apoth.getEntity() == entity.getType()) {
                    bossId = key;
                    break;
                }
            }
            if (bossId == null) {
                ApotheoticHostility.LOGGER.debug("Could not resolve bossId for entity {}", entity.getType());
                return;
            }

            BossScalingManager manager = BossScalingManager.getInstance();
            if (!manager.hasBossConfig(bossId)) return;
            BossScalingConfig.BossScalingEntry entry = manager.getEntryForBoss(bossId);
            if (entry == null) return;

            Map<ResourceLocation, Integer> reqs = entry.getRarityRequirementMap();
            if (reqs == null || reqs.isEmpty()) {
                return;
            }

            try {
                entity.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, random.nextFloat() * 360.0F, 0.0F);
            } catch (Throwable t) {
                ApotheoticHostility.LOGGER.warn("Failed to set entity pos prior to init: {}", t.getMessage());
            }
            if (!MobTraitCap.HOLDER.isProper(entity)) {
                ApotheoticHostility.LOGGER.debug("Entity lacks MobTraitCap: {}", entity);
                return;
            }
            var cap = MobTraitCap.HOLDER.get(entity);

            if (!cap.isInitialized()) {
                Optional<ChunkDifficulty> opt = ChunkDifficulty.at(world.getLevel(), pos);
                if (opt.isEmpty()) {
                    ApotheoticHostility.LOGGER.debug("ChunkDifficulty not available at {}, skipping rarity override", pos);
                    return;
                }
                try {
                    cap.setConfigCache(entry.toEntityConfig());
                } catch (Throwable t) {
                    ApotheoticHostility.LOGGER.warn("Could not set config cache for {}: {}", bossId, t.getMessage());
                }

                try {
                    cap.init(world.getLevel(), entity, opt.get());
                    cap.dropRate = dev.xkmc.l2hostility.init.data.LHConfig.COMMON.dropRateFromSpawner.get();
                } catch (Throwable t) {
                    ApotheoticHostility.LOGGER.warn("Cap.init failed for {}: {}", bossId, t.getMessage());
                    return;
                }
            }

            int lvl = cap.getLevel();

            Optional<LootRarity> chosen = reqs.entrySet().stream()
                    .filter(e -> lvl >= e.getValue())
                    .map(Map.Entry::getKey)
                    .map(RarityRegistry.INSTANCE::getValue)
                    .filter(Objects::nonNull)
                    .max(Comparator.comparingInt(LootRarity::ordinal));

            if (chosen.isEmpty()) {
                ApotheoticHostility.LOGGER.info("Discarding boss {} (level {}) - no eligible rarity", bossId, lvl);

                // FIX: Mark entity to be discarded safely later in BossEventsMixin
                entity.getPersistentData().putBoolean("apoth_hostility.discard", true);

                // FIX: Force return and abort the rest of the target method so Mounts aren't generated!
                cir.setReturnValue(entity);
                cir.cancel();
                return;
            }

            LootRarity chosenRarity = chosen.get();
            try {
                entity.getPersistentData().putString("apoth.rarity", RarityRegistry.INSTANCE.getKey(chosenRarity).toString());
            } catch (Throwable ignored) {}

            ApotheoticHostility.LOGGER.debug("Forcing rarity {} for boss {} (level {}) at pos {}",
                    RarityRegistry.INSTANCE.getKey(chosenRarity), bossId, lvl, pos);

            ((ApothBoss) (Object) this).initBoss(random, entity, luck, chosenRarity);

            if (this.nbt != null) {
                entity.readAdditionalSaveData(this.nbt);
            }

            if (this.mount != null) {
                try {
                    Mob mountedEntity = this.mount.create((ServerLevel) world.getLevel(), pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
                    if (mountedEntity != null) {
                        entity.startRiding(mountedEntity, true);
                        entity = mountedEntity;
                    }
                } catch (Throwable t) {
                    ApotheoticHostility.LOGGER.warn("Failed to create mount for {}: {}", bossId, t.getMessage());
                }
            }

            entity.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, random.nextFloat() * 360.0F, 0.0F);

            cir.setReturnValue(entity);
            cir.cancel();

        } catch (Throwable t) {
            ApotheoticHostility.LOGGER.error("Error while choosing/forcing rarity", t);
        }
    }
}