package net.kayn.apotheotic_hostility.mixin;

import dev.shadowsoffire.apotheosis.adventure.affix.Affix;
import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import dev.shadowsoffire.apotheosis.adventure.loot.LootController;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.apotheosis.adventure.loot.RarityRegistry;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import dev.xkmc.l2hostility.content.capability.mob.MobTraitCap;
import dev.xkmc.l2hostility.content.logic.ItemPopulator;
import net.kayn.apotheotic_hostility.ApotheoticHostility;
import net.kayn.apotheotic_hostility.data.AffixEquipmentRuleManager;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collections;
import java.util.List;

@Mixin(value = ItemPopulator.class, remap = false)
public class ItemPopulatorMixin {

    @Inject(
            method = "postFill",
            at = @At("RETURN"),
            remap = false
    )
    private static void afterPostFill(MobTraitCap cap, LivingEntity le, CallbackInfo ci) {
        System.out.println("POPULATOR MIXIN FIRED, LEVEL: " + cap.getLevel() + " NOAI: " + (le instanceof Mob mob && mob.isNoAi()));
        try {
            if (le.getPersistentData().getBoolean("apoth.boss")) return;
            if (cap.getLevel() <= 0) return;
            if (le instanceof Mob mob && mob.isNoAi()) return;

            int mobLevel = cap.getLevel();
            var rule = AffixEquipmentRuleManager.getInstance().getRuleForLevel(mobLevel);

            if (rule == null) return;

            RandomSource rand = le.getRandom();

            if (rand.nextDouble() >= rule.getChance()) return;

            LootRarity targetRarity = RarityRegistry.INSTANCE.getValue(rule.getRarity());
            if (targetRarity == null) return;

            for (EquipmentSlot slot : EquipmentSlot.values()) {
                ItemStack stack = le.getItemBySlot(slot);

                if (stack.isEmpty()) continue;

                if (stack.hasTag() && stack.getTag().getBoolean("apoth_boss")) continue;

                LootCategory cat = LootCategory.forItem(stack);
                if (cat.isNone()) continue;

                boolean canAffix = true;
                for (LootRarity.LootRule lootRule : targetRarity.getRules()) {
                    if (lootRule.type().needsValidation()) {
                        List<DynamicHolder<? extends Affix>> candidates =
                                LootController.getAvailableAffixes(stack, targetRarity, Collections.emptySet(), lootRule.type());
                        if (candidates.isEmpty()) {
                            canAffix = false;
                            break;
                        }
                    }
                }
                if (!canAffix) continue;

                try {
                    ItemStack affixed = LootController.createLootItem(stack, cat, targetRarity, rand);
                    le.setItemSlot(slot, affixed);

                    ApotheoticHostility.LOGGER.debug(
                            "Applied {} affixes to {} in slot {}",
                            targetRarity, stack.getItem(), slot
                    );
                } catch (Exception e) {
                    ApotheoticHostility.LOGGER.error(
                            "Failed to apply affixes to {} for mob level {}",
                            stack.getItem(), mobLevel, e
                    );
                }
            }

        } catch (Throwable t) {
            ApotheoticHostility.LOGGER.error("Error applying affixed equipment", t);
        }
    }
}