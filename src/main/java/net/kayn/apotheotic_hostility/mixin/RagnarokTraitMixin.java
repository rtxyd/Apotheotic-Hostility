package net.kayn.apotheotic_hostility.mixin;

import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import dev.shadowsoffire.apotheosis.adventure.socket.SocketHelper;
import dev.xkmc.l2hostility.compat.curios.EntitySlotAccess;
import dev.xkmc.l2hostility.content.traits.legendary.RagnarokTrait;
import net.kayn.apotheotic_hostility.adventure.socket.gem.bonus.RagnarokResistBonus;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = RagnarokTrait.class, remap = false)
public class RagnarokTraitMixin {

    @Inject(
            method = "allowSeal",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private static void checkRagnarokResist(EntitySlotAccess access, CallbackInfoReturnable<Boolean> cir) {
        ItemStack stack = access.get();
        if (stack.isEmpty()) return;

        LootCategory cat = LootCategory.forItem(stack);

        boolean hasResist = SocketHelper.getGems(stack).stream()
                .anyMatch(gemInst -> {
                    if (!gemInst.gem().isBound() || !gemInst.rarity().isBound()) return false;
                    return gemInst.gem().get()
                            .getBonus(cat, gemInst.rarity().get())
                            .filter(b -> b instanceof RagnarokResistBonus)
                            .isPresent();
                });

        if (hasResist) {
            cir.setReturnValue(false);
        }
    }
}