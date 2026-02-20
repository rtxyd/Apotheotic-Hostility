package net.kayn.apotheotic_hostility.init.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.apotheosis.adventure.loot.RarityRegistry;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.Gem;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.GemRegistry;
import dev.xkmc.l2hostility.content.capability.mob.MobTraitCap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.kayn.apotheotic_hostility.data.GemDropRuleManager;
import net.kayn.apotheotic_hostility.data.GemDropRules;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;

import javax.annotation.Nonnull;
import java.util.Set;

public class HostilityGemLootModifier extends LootModifier {

    public static final Codec<HostilityGemLootModifier> CODEC = RecordCodecBuilder.create(inst -> codecStart(inst).apply(inst, HostilityGemLootModifier::new));

    protected HostilityGemLootModifier(LootItemCondition[] conditionsIn) {
        super(conditionsIn);
    }

    @Nonnull
    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        Entity entity = context.getParamOrNull(LootContextParams.THIS_ENTITY);

        if (!(entity instanceof LivingEntity livingEntity)) {
            return generatedLoot;
        }

        if (!MobTraitCap.HOLDER.isProper(livingEntity)) {
            return generatedLoot;
        }

        MobTraitCap cap = MobTraitCap.HOLDER.get(livingEntity);
        int mobLevel = cap.getLevel();

        GemDropRules.GemDropEntry dropRule = GemDropRuleManager.getInstance().getDropForLevel(mobLevel);

        if (dropRule == null) {
            return generatedLoot;
        }

        if (context.getRandom().nextDouble() < dropRule.getChance()) {
            try {
                LootRarity targetRarity = RarityRegistry.INSTANCE.getValue(dropRule.getTier());
                if (targetRarity == null) {
                    return generatedLoot;
                }

                Set<ResourceLocation> excludedGems = GemTraitLootModifier.INSTANCES.stream()
                        .map(m -> new ResourceLocation(m.gem))
                        .collect(java.util.stream.Collectors.toSet());

                Gem selectedGem = GemRegistry.INSTANCE.getRandomItem(
                        context.getRandom(),
                        0.0f,
                        gem -> {
                            ResourceLocation gemId = GemRegistry.INSTANCE.getKey(gem);

                            if (gemId != null && excludedGems.contains(gemId)) return false;

                            boolean withinRange = targetRarity.isAtLeast(gem.getMinRarity())
                                    && targetRarity.isAtMost(gem.getMaxRarity());
                            if (!withinRange) return false;

                            return gem.getBonuses().stream().anyMatch(bonus -> bonus.supports(targetRarity));
                        }
                );

                if (selectedGem == null) {
                    return generatedLoot;
                }

                ItemStack gemStack = GemRegistry.createGemStack(selectedGem, targetRarity);

                if (!gemStack.isEmpty()) {
                    generatedLoot.add(gemStack);
                }
            } catch (Exception ignored) {
            }
        }

        return generatedLoot;
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}