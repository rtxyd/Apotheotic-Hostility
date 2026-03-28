package net.kayn.apotheotic_hostility.mixin;

import dev.shadowsoffire.apotheosis.adventure.gen.ItemFrameGemsProcessor;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.GemRegistry;
import dev.shadowsoffire.placebo.reload.WeightedDynamicRegistry;
import net.kayn.apotheotic_hostility.init.loot.GemTraitLootModifier;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;
import java.util.stream.Collectors;

@Mixin(value = ItemFrameGemsProcessor.class, remap = false)
public class ItemFrameGemsProcessorMixin {
    @Inject(
            method = "writeEntityNBT",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private void filterTraitGems(ServerLevel world, BlockPos pos, RandomSource rand, CompoundTag nbt, StructurePlaceSettings settings, CallbackInfo ci) {
        Set<ResourceLocation> excluded = GemTraitLootModifier.INSTANCES.stream()
                .map(m -> new ResourceLocation(m.gem))
                .collect(Collectors.toSet());
        if (excluded.isEmpty()) return;

        ci.cancel();

        for (int attempts = 0; attempts < 20; attempts++) {
            ItemStack stack = GemRegistry.createRandomGemStack(rand, world, 0, WeightedDynamicRegistry.IDimensional.matches(world));
            if (stack.isEmpty()) return;

            CompoundTag tag = stack.getTag();
            if (tag == null) { nbt.put("Item", stack.serializeNBT()); return; }

            ResourceLocation gemId = ResourceLocation.tryParse(tag.getString("gem"));
            if (gemId != null && excluded.contains(gemId)) continue;

            nbt.put("Item", stack.serializeNBT());
            nbt.putInt("TileX", pos.getX());
            nbt.putInt("TileY", pos.getY());
            nbt.putInt("TileZ", pos.getZ());
            return;
        }
    }
}
