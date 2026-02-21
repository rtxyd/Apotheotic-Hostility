package net.kayn.apotheotic_hostility.init.loot;

import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.xkmc.l2hostility.compat.jei.ITraitLootRecipe;
import dev.xkmc.l2hostility.content.capability.mob.MobTraitCap;
import dev.xkmc.l2hostility.content.capability.player.PlayerDifficulty;
import dev.xkmc.l2hostility.content.item.curio.core.CurseCurioItem;
import dev.xkmc.l2hostility.content.traits.base.MobTrait;
import dev.xkmc.l2hostility.content.traits.legendary.LegendaryTrait;
import dev.xkmc.l2hostility.init.data.LHConfig;
import dev.xkmc.l2hostility.init.data.LHTagGen;
import dev.xkmc.l2hostility.init.loot.MobCapLootCondition;
import dev.xkmc.l2hostility.init.loot.PlayerHasItemCondition;
import dev.xkmc.l2hostility.init.loot.TraitLootCondition;
import dev.xkmc.l2hostility.init.registrate.LHTraits;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.kayn.apotheotic_hostility.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class GemTraitLootModifier extends LootModifier implements ITraitLootRecipe {

    public static final List<GemTraitLootModifier> INSTANCES = new ArrayList<>();

    public static final Supplier<Codec<GemTraitLootModifier>> CODEC = Suppliers.memoize(() ->
            RecordCodecBuilder.create(inst -> codecStart(inst).and(inst.group(
                    LHTraits.TRAITS.get().getCodec().optionalFieldOf("trait")
                            .forGetter(e -> Optional.ofNullable(e.trait)),
                    Codec.STRING.fieldOf("gem").forGetter(m -> m.gem),
                    Codec.STRING.fieldOf("rarity").forGetter(m -> m.rarity),
                    Codec.DOUBLE.fieldOf("chance").forGetter(m -> m.chance),
                    Codec.DOUBLE.optionalFieldOf("rankBonus", 0.0).forGetter(m -> m.rankBonus),
                    Codec.INT.optionalFieldOf("min_traits", 0).forGetter(m -> m.minTraits),
                    Codec.INT.optionalFieldOf("min_legendary_traits", 0).forGetter(m -> m.minLegendaryTraits)
            )).apply(inst, GemTraitLootModifier::new)));


    @Nullable
    public final MobTrait trait;
    public final String gem;
    public final String rarity;
    public final double chance;
    public final double rankBonus;
    public final int minTraits;
    public final int minLegendaryTraits;

    private GemTraitLootModifier(LootItemCondition[] conditionsIn, Optional<MobTrait> trait,
                                 String gem, String rarity, double chance, double rankBonus,
                                 int minTraits, int minLegendaryTraits) {
        super(conditionsIn);
        this.trait = trait.orElse(null);
        this.gem = gem;
        this.rarity = rarity;
        this.chance = chance;
        this.rankBonus = rankBonus;
        this.minTraits = minTraits;
        this.minLegendaryTraits = minLegendaryTraits;
        INSTANCES.add(this);
    }

    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> list, LootContext context) {
        if (!(context.getParam(LootContextParams.THIS_ENTITY) instanceof LivingEntity le)) {
            return list;
        }
        if (le.getType().is(LHTagGen.NO_DROP)) {
            return list;
        }
        if (!MobTraitCap.HOLDER.isProper(le)) {
            return list;
        }

        MobTraitCap cap = MobTraitCap.HOLDER.get(le);

        if (minTraits > 0 && cap.traits.size() < minTraits) return list;

        if (minLegendaryTraits > 0) {
            long count = cap.traits.keySet().stream()
                    .filter(t -> t instanceof LegendaryTrait)
                    .count();
            if (count < minLegendaryTraits) return list;
        }

        double factor = 1.0;

        if (context.hasParam(LootContextParams.LAST_DAMAGE_PLAYER)) {
            Player player = context.getParam(LootContextParams.LAST_DAMAGE_PLAYER);

            boolean hasCharm = CuriosApi.getCuriosInventory(player).resolve()
                    .flatMap(inv -> inv.findFirstCurio(ModItems.GEM_LOOTING_CHARM.get()))
                    .isPresent();
            if (!hasCharm) return list;

            var pl = PlayerDifficulty.HOLDER.get(player);

            var curios = CurseCurioItem.getFromPlayer(player);

            for (var stack : curios) {
                var item = stack.item();
                double mult;
                try {
                    mult = item.getLootFactor(stack.stack(), pl, cap);
                } catch (Throwable t) {
                    mult = 0.0;
                }

                if (!Double.isFinite(mult) || mult <= 0.0) {
                    mult = 1.0;
                }

                factor *= mult;
            }
        } else {
            return list;
        }

        int lv = trait == null ? 0 : cap.getTraitLevel(trait);
        double rate = chance + lv * rankBonus;

        if (context.getRandom().nextDouble() < rate * factor) {
            ItemStack gemStack = createGem();
            if (!gemStack.isEmpty()) list.add(gemStack);
        }

        return list;
    }

    private ItemStack createGem() {
        Item gemItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation("apotheosis", "gem"));
        if (gemItem == null) return ItemStack.EMPTY;
        ItemStack stack = new ItemStack(gemItem);
        CompoundTag affixData = new CompoundTag();
        affixData.putString("rarity", rarity);
        CompoundTag nbt = new CompoundTag();
        nbt.put("affix_data", affixData);
        nbt.putString("gem", gem);
        stack.setTag(nbt);
        return stack;
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return CODEC.get();
    }

    public LootItemCondition[] getConditions() {
        return conditions;
    }

    @Override
    public List<ItemStack> getResults() {
        return List.of(createGem());
    }

    @Override
    public List<ItemStack> getCurioRequired() {
        List<ItemStack> ans = new ArrayList<>();
        if (LHConfig.COMMON.disableHostilityLootCurioRequirement.get()) return ans;
        for (var c : getConditions()) {
            if (c instanceof PlayerHasItemCondition item) {
                ans.add(item.item.getDefaultInstance());
            }
        }
        return ans;
    }

    @Override
    public List<ItemStack> getInputs() {
        // Specific traits from conditions take priority
        Set<MobTrait> traitSet = new LinkedHashSet<>();
        for (var c : getConditions()) {
            if (c instanceof TraitLootCondition cl) {
                traitSet.add(cl.trait);
            }
        }
        if (!traitSet.isEmpty()) {
            return traitSet.stream()
                    .map(t -> t.asItem().getDefaultInstance())
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
        }

        // Cycle through legendary traits if min_legendary_traits is set
        if (minLegendaryTraits > 0) {
            return LHTraits.TRAITS.get().getValues().stream()
                    .filter(t -> t instanceof LegendaryTrait && !t.isBanned())
                    .map(t -> t.asItem().getDefaultInstance())
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
        }

        // Cycle through all traits if min_traits is set
        if (minTraits > 0) {
            return LHTraits.TRAITS.get().getValues().stream()
                    .filter(t -> !t.isBanned())
                    .map(t -> t.asItem().getDefaultInstance())
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
        }

        return List.of();
    }

    @Override
    public void addTooltip(List<Component> list) {
        int minLevel = 0;
        List<TraitLootCondition> traitConditions = new ArrayList<>();

        for (var c : getConditions()) {
            if (c instanceof MobCapLootCondition cl) minLevel = cl.minLevel;
            if (c instanceof TraitLootCondition cl) traitConditions.add(cl);
        }

        if (minLevel > 0) {
            list.add(Component.translatable("l2hostility.jei.min_level",
                            Component.literal(minLevel + "").withStyle(ChatFormatting.AQUA))
                    .withStyle(ChatFormatting.LIGHT_PURPLE));
        }

        if (minTraits > 0) {
            list.add(Component.translatable("apotheotic_hostility.jei.min_traits",
                            Component.literal(minTraits + "").withStyle(ChatFormatting.AQUA))
                    .withStyle(ChatFormatting.LIGHT_PURPLE));
        }

        if (minLegendaryTraits > 0) {
            list.add(Component.translatable("apotheotic_hostility.jei.min_legendary_traits",
                            Component.literal(minLegendaryTraits + "").withStyle(ChatFormatting.AQUA))
                    .withStyle(ChatFormatting.LIGHT_PURPLE));
        }

        // Show each required trait from conditions
        for (var cl : traitConditions) {
            int cmin = Math.max(cl.minLevel, 1);
            int cmax = Math.min(cl.maxLevel, cl.trait.getMaxLevel());
            String str = cmax == cmin ? cmin + "" :
                    cmax >= cl.trait.getMaxLevel() ? cmin + "+" : cmin + "-" + cmax;
            list.add(Component.translatable("l2hostility.jei.other_trait",
                            cl.trait.getDesc().withStyle(ChatFormatting.GOLD),
                            Component.literal(str).withStyle(ChatFormatting.AQUA))
                    .withStyle(ChatFormatting.RED));
        }

        // Drop chance
        if (trait != null) {
            int min = 1;
            int max = trait.getMaxLevel();
            for (var c : getConditions()) {
                if (c instanceof TraitLootCondition cl && cl.trait == trait) {
                    min = Math.max(min, cl.minLevel);
                    max = Math.min(max, cl.maxLevel);
                }
            }
            for (int lv = min; lv <= max; lv++) {
                list.add(Component.translatable("l2hostility.jei.loot_chance",
                                Component.literal(Math.round((chance + rankBonus * lv) * 100) + "%")
                                        .withStyle(ChatFormatting.AQUA),
                                trait.getDesc().withStyle(ChatFormatting.GOLD),
                                Component.literal(lv + "").withStyle(ChatFormatting.AQUA))
                        .withStyle(ChatFormatting.GRAY));
            }
        }

        // Required curio
        for (var c : getConditions()) {
            if (c instanceof PlayerHasItemCondition cl) {
                list.add(Component.translatable("l2hostility.jei.required",
                                cl.item.getDescription().copy().withStyle(ChatFormatting.LIGHT_PURPLE))
                        .withStyle(ChatFormatting.YELLOW));
            }
        }
    }
}