package net.kayn.apotheotic_hostility.mixin;

import dev.xkmc.l2hostility.content.capability.mob.MobTraitCap;
import net.kayn.apotheotic_hostility.data.MobLevelContext;
import net.kayn.apotheotic_hostility.data.UniversalBossLevelConfig;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.fml.ModList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(targets = "net.kayn.fallen_gems_affixes.adventure.boss.UniversalBossEventHandler", remap = false)
public class UniversalBossEventHandlerMixin {

    @Inject(
            method = "onEntityJoin",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private static void checkMinLevel(MobSpawnEvent.FinalizeSpawn event, CallbackInfo ci) {
        if (!ModList.get().isLoaded("fallen_gems_affixes")) return;
        if (!(event.getEntity() instanceof Mob)) return;
        Mob mob = event.getEntity();
        if (!MobTraitCap.HOLDER.isProper(mob)) return;

        int mobLevel = MobTraitCap.HOLDER.get(mob).getLevel();
        Map<String, Integer> minLevels = UniversalBossLevelConfig.getAllMinLevels();
        if (minLevels.isEmpty()) return;

        int lowestMinLevel = minLevels.values().stream().min(Integer::compare).orElse(0);
        if (mobLevel < lowestMinLevel) {
            MobLevelContext.clear();
            ci.cancel();
            return;
        }

        MobLevelContext.set(mobLevel);
    }

    @Inject(
            method = "onEntityJoin",
            at = @At("RETURN"),
            remap = false
    )
    private static void clearLevel(MobSpawnEvent.FinalizeSpawn event, CallbackInfo ci) {
        MobLevelContext.clear();
    }
}