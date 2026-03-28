package net.kayn.apotheotic_hostility.mixin;

import dev.shadowsoffire.apotheosis.adventure.AdventureEvents;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = AdventureEvents.class, remap = false)
public class AdventureEventsMixin {

    @Inject(method = "dropsHigh", at = @At("HEAD"), cancellable = true)
    private void disableGemDrops(LivingDropsEvent e, CallbackInfo ci) {
        ci.cancel();
    }
}
