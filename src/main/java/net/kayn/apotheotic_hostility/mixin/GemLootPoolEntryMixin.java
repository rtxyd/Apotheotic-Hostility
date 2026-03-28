package net.kayn.apotheotic_hostility.mixin;

import dev.shadowsoffire.apotheosis.adventure.AdventureConfig;
import dev.shadowsoffire.apotheosis.adventure.compat.GameStagesCompat;
import dev.shadowsoffire.apotheosis.adventure.loot.GemLootPoolEntry;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.apotheosis.adventure.loot.RarityClamp;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.Gem;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.GemRegistry;
import dev.shadowsoffire.placebo.reload.WeightedDynamicRegistry;
import net.kayn.apotheotic_hostility.init.loot.GemTraitLootModifier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Mixin(value = GemLootPoolEntry.class, remap = false)
public class GemLootPoolEntryMixin {
    @Inject(
            method = "createItemStack",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private void filterTraitGems(Consumer<ItemStack> list, LootContext ctx, CallbackInfo ci) {
        Set<ResourceLocation> excluded = GemTraitLootModifier.INSTANCES.stream()
                .map(m -> new ResourceLocation(m.gem))
                .collect(Collectors.toSet());
        if (excluded.isEmpty()) return;

        ci.cancel();

        for (int attempts = 0; attempts < 20; attempts++) {
            var player = GemLootPoolEntry.findPlayer(ctx);
            if (player == null) return;

            Gem gem = GemRegistry.INSTANCE.getRandomItem(
                    ctx.getRandom(),
                    ctx.getLuck(),
                    WeightedDynamicRegistry.IDimensional.matches(ctx.getLevel()),
                    GameStagesCompat.IStaged.matches(player)
            );
            if (gem == null) return;

            ResourceLocation gemId = GemRegistry.INSTANCE.getKey(gem);
            if (gemId != null && excluded.contains(gemId)) continue;

            RarityClamp clamp = AdventureConfig.GEM_DIM_RARITIES.get(ctx.getLevel().dimension().location());
            ItemStack stack = GemRegistry.createGemStack(gem, gem.clamp(LootRarity.random(ctx.getRandom(), ctx.getLuck(), clamp)));
            list.accept(stack);
            return;
        }
    }
}
