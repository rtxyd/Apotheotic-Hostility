package net.kayn.apotheotic_hostility.mixin;

import dev.xkmc.l2hostility.compat.jei.GLMRecipeCategory;
import dev.xkmc.l2hostility.compat.jei.JEICompat;
import mezz.jei.api.registration.IRecipeRegistration;
import net.kayn.apotheotic_hostility.init.loot.GemTraitLootModifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;

@Mixin(value = JEICompat.class, remap = false)
public class JEICompatMixin {

    @Shadow
    public GLMRecipeCategory LOOT;

    @Inject(method = "registerRecipes", at = @At("TAIL"), remap = false)
    public void addGemTraitRecipes(IRecipeRegistration registration, CallbackInfo ci) {
        if (!GemTraitLootModifier.INSTANCES.isEmpty()) {
            registration.addRecipes(LOOT.getRecipeType(), new ArrayList<>(GemTraitLootModifier.INSTANCES));
        }
    }
}