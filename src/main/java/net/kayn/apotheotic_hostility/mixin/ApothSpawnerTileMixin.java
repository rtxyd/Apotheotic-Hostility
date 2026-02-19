package net.kayn.apotheotic_hostility.mixin;

import dev.shadowsoffire.apotheosis.spawn.spawner.ApothSpawnerTile;
import net.kayn.apotheotic_hostility.spawner.IL2HSpawner;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ApothSpawnerTile.class, remap = false)
public abstract class ApothSpawnerTileMixin implements IL2HSpawner {

    @Unique
    private int apotheotic_hostility$level = 0;

    @Inject(
            method = "saveAdditional(Lnet/minecraft/nbt/CompoundTag;)V",
            at = @At("TAIL"),
            remap = true
    )
    private void saveL2HLevel(CompoundTag tag, CallbackInfo ci) {
        tag.putInt("apotheotic_hostility:level", this.apotheotic_hostility$level);
    }

    @Inject(
            method = "load(Lnet/minecraft/nbt/CompoundTag;)V",
            at = @At("TAIL"),
            remap = true
    )
    private void loadL2HLevel(CompoundTag tag, CallbackInfo ci) {
        this.apotheotic_hostility$level = tag.getInt("apotheotic_hostility:level");
    }

    @Override
    public int apotheotic_hostility$getLevel() {
        return this.apotheotic_hostility$level;
    }

    @Override
    public void apotheotic_hostility$setLevel(int level) {
        this.apotheotic_hostility$level = level;
    }
}