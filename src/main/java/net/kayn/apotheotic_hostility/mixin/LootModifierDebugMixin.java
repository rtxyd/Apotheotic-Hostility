package net.kayn.apotheotic_hostility.mixin;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraftforge.common.loot.LootModifier;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = LootModifier.class, remap = false)
public class LootModifierDebugMixin {
    @Inject(method = "apply", at = @At("RETURN"), cancellable = false, remap = false)
    private void debugLootModifier(ObjectArrayList<ItemStack> generatedLoot, LootContext context, CallbackInfoReturnable<ObjectArrayList<ItemStack>> cir) {
        ObjectArrayList<ItemStack> result = cir.getReturnValue();
        if (result.stream().anyMatch(s -> s.getItem() == ForgeRegistries.ITEMS.getValue(new ResourceLocation("apotheosis", "gem")))) {
            System.out.println("GEM DROPPED BY: " + this.getClass().getName());
        }
    }
}