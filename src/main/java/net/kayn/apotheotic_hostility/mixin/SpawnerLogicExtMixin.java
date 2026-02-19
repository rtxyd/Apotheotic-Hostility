package net.kayn.apotheotic_hostility.mixin;

import dev.shadowsoffire.apotheosis.spawn.spawner.ApothSpawnerTile;
import dev.xkmc.l2hostility.content.capability.chunk.ChunkDifficulty;
import dev.xkmc.l2hostility.content.capability.mob.MobTraitCap;
import net.kayn.apotheotic_hostility.ApotheoticHostility;
import net.kayn.apotheotic_hostility.spawner.IL2HSpawner;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "dev.shadowsoffire.apotheosis.spawn.spawner.ApothSpawnerTile$SpawnerLogicExt", remap = false)
public abstract class SpawnerLogicExtMixin {

    @Shadow(remap = false)
    public abstract net.minecraft.world.level.block.entity.BlockEntity getSpawnerBlockEntity();

    @Redirect(
            method = "serverTick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerLevel;tryAddFreshEntityWithPassengers(Lnet/minecraft/world/entity/Entity;)Z"
            ),
            remap = false
    )
    private boolean apotheotic_hostility$onTryAddFreshEntityWithPassengers(ServerLevel serverLevel, Entity entity) {
        // Call the original method (this replaces the original INVOKE)
        boolean added = serverLevel.tryAddFreshEntityWithPassengers(entity);

        // If entity was actually added, do our L2H spawner-level adjustments
        if (added && entity instanceof Mob mob) {
            try {
                // get the enclosing block entity (the ApothSpawnerTile instance)
                var be = getSpawnerBlockEntity();
                if (!(be instanceof ApothSpawnerTile)) return added; // shouldn't happen but safe
                ApothSpawnerTile tile = (ApothSpawnerTile) be;

                // only care about spawners that implement IL2HSpawner (your extension)
                if (!(tile instanceof IL2HSpawner l2hSpawner)) return added;

                int spawnerLevel = l2hSpawner.apotheotic_hostility$getLevel();
                if (spawnerLevel <= 0) return added;

                // If mob has no MobTraitCap, nothing to do
                if (!MobTraitCap.HOLDER.isProper(mob)) return added;

                // compute chunk difficulty at mob position
                var optChunk = ChunkDifficulty.at(serverLevel, mob.blockPosition());
                if (optChunk.isEmpty()) return added;

                var cap = MobTraitCap.HOLDER.get(mob);

                // initialize if needed
                if (!cap.isInitialized()) {
                    try {
                        cap.init(serverLevel, mob, optChunk.get());
                    } catch (Throwable t) {
                        ApotheoticHostility.LOGGER.warn("[SpawnerLogicExtMixin] cap.init failed for {}: {}", mob.getType(), t.getMessage());
                        return added;
                    }
                }

                // Force reinit to spawnerLevel
                try {
                    cap.reinit(mob, spawnerLevel, false);
                    cap.dropRate = dev.xkmc.l2hostility.init.data.LHConfig.COMMON.dropRateFromSpawner.get();
                    ApotheoticHostility.LOGGER.debug("[SpawnerLogicExtMixin] Applied L2H level {} to spawner mob {}", spawnerLevel, mob.getType());
                } catch (Throwable t) {
                    ApotheoticHostility.LOGGER.error("[SpawnerLogicExtMixin] error reinitializing cap for spawner mob {}", mob.getType(), t);
                }
            } catch (Throwable t) {
                // Never propagate; if this mixin fails we must not break the spawner behavior
                ApotheoticHostility.LOGGER.error("[SpawnerLogicExtMixin] unexpected error while post-processing spawned entity", t);
            }
        }

        return added;
    }
}
