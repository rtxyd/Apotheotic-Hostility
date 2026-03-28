package net.kayn.apotheotic_hostility.mixin;

import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.apotheosis.adventure.loot.RarityRegistry;
import net.kayn.apotheotic_hostility.data.MobLevelContext;
import net.kayn.apotheotic_hostility.data.UniversalBossLevelConfig;
import net.minecraft.util.RandomSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(targets = "net.kayn.fallen_gems_affixes.adventure.boss.UniversalBossConfig", remap = false)
public class UniversalBossConfigMixin {

    @Inject(
            method = "rollRarity",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private void filterRarityByLevel(RandomSource rand, CallbackInfoReturnable<LootRarity> cir) {
        int mobLevel = MobLevelContext.get();
        if (mobLevel < 0) return;

        Map<String, Integer> minLevels = UniversalBossLevelConfig.getAllMinLevels();
        if (minLevels.isEmpty()) return;

        String eligibleRarityKey = null;
        int highestMinLevel = -1;

        for (Map.Entry<String, Integer> entry : minLevels.entrySet()) {
            if (mobLevel >= entry.getValue() && entry.getValue() > highestMinLevel) {
                highestMinLevel = entry.getValue();
                eligibleRarityKey = entry.getKey();
            }
        }

        if (eligibleRarityKey == null) {
            cir.setReturnValue(null);
            return;
        }

        try {
            LootRarity rarity = RarityRegistry.byLegacyId(eligibleRarityKey).get();
            cir.setReturnValue(rarity);
        } catch (Exception e) {
            cir.setReturnValue(null);
        }
    }
}