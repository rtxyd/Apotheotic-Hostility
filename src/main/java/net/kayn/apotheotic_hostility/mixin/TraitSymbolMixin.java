package net.kayn.apotheotic_hostility.mixin;

import dev.xkmc.l2hostility.content.capability.mob.MobTraitCap;
import dev.xkmc.l2hostility.content.traits.base.MobTrait;
import dev.xkmc.l2hostility.init.registrate.LHTraits;
import net.kayn.apotheotic_hostility.ApotheoticHostility;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = dev.xkmc.l2hostility.content.item.traits.TraitSymbol.class, remap = false)
public class TraitSymbolMixin {

    @Inject(method = "interactLivingEntity(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;",
            at = @At("HEAD"),
            cancellable = true)
    private void onInteractHead(ItemStack stack, Player player, LivingEntity target, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        try {
            if (stack == null || stack.isEmpty()) return;
            Item item = stack.getItem();
            var id = ForgeRegistries.ITEMS.getKey(item);
            if (id == null) return;

            MobTrait trait = LHTraits.TRAITS.get().getValue(id);
            if (trait == null) return;

            if (MobTraitCap.HOLDER.isProper(target)) {
                var cap = MobTraitCap.HOLDER.get(target);
                var cfg = cap.getConfigCache(target);
                if (cfg != null && cfg.blacklist().contains(trait)) {
                    if (!player.level().isClientSide && player instanceof ServerPlayer sp) {
                        try {
                            sp.sendSystemMessage(dev.xkmc.l2hostility.init.data.LangData.MSG_ERR_DISALLOW.get()
                                    .withStyle(net.minecraft.ChatFormatting.RED), true);
                        } catch (Throwable t) {
                            sp.sendSystemMessage(Component.literal("Trait not applicable on this entity").withStyle(net.minecraft.ChatFormatting.RED), true);
                        }
                    }
                    cir.setReturnValue(InteractionResult.FAIL);
                }
            }
        } catch (Throwable t) {
            ApotheoticHostility.LOGGER.error("[TraitSymbolMixin] error in blacklist check", t);
        }
    }
}