package net.kayn.apotheotic_hostility.mixin;

import dev.shadowsoffire.apotheosis.adventure.boss.BossEvents;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = BossEvents.class, remap = false)
public class BossEventsMixin {

    @Inject(
            method = "canSpawn",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private static void checkHostilityDiscard(LevelAccessor world, Mob entity, double playerDist, CallbackInfoReturnable<Boolean> cir) {
        // Check if the entity (or its mount) was marked for discard by our ApothBossMixin
        boolean shouldDiscard = entity.getSelfAndPassengers().anyMatch(e ->
                e.getPersistentData().getBoolean("apoth_hostility.discard")
        );

        if (shouldDiscard) {
            // Returning false skips `addFreshEntityWithPassengers` AND skips the chat announcement cleanly.
            cir.setReturnValue(false);
        }
    }
}