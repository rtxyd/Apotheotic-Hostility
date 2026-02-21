package net.kayn.apotheotic_hostility.mixin;

import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import dev.shadowsoffire.apotheosis.adventure.socket.SocketHelper;
import dev.xkmc.l2hostility.content.traits.legendary.DispellTrait;
import net.kayn.apotheotic_hostility.adventure.socket.gem.bonus.DispellResistBonus;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.item.ItemStack;

@Mixin(value = DispellTrait.class, remap = false)
public class DispellTraitMixin {

    @ModifyVariable(
            method = "postHurtImpl",
            at = @At(value = "INVOKE", target = "Ljava/util/List;isEmpty()Z"),
            ordinal = 0,
            remap = false
    )
    private List<ItemStack> filterDispellResistItems(List<ItemStack> list, int level, LivingEntity attacker, LivingEntity target) {
        List<ItemStack> filtered = new ArrayList<>(list);
        filtered.removeIf(stack -> {
            LootCategory cat = LootCategory.forItem(stack);
            return SocketHelper.getGems(stack).stream()
                    .anyMatch(gemInst -> {
                        if (!gemInst.gem().isBound() || !gemInst.rarity().isBound()) return false;
                        return gemInst.gem().get()
                                .getBonus(cat, gemInst.rarity().get())
                                .filter(b -> b instanceof DispellResistBonus)
                                .isPresent();
                    });
        });
        return filtered;
    }
}