package net.kayn.apotheotic_hostility.adventure.affix;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.shadowsoffire.apotheosis.adventure.affix.Affix;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixType;
import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.placebo.util.StepFunction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class DifficultyLevelAffix extends Affix {

    private static final UUID[] SLOT_UUIDS = new UUID[]{UUID.fromString("a1b2c3d4-0001-0000-0000-000000000000"), UUID.fromString("a1b2c3d4-0002-0000-0000-000000000000"), UUID.fromString("a1b2c3d4-0003-0000-0000-000000000000"), UUID.fromString("a1b2c3d4-0004-0000-0000-000000000000"), UUID.fromString("a1b2c3d4-0005-0000-0000-000000000000"), UUID.fromString("a1b2c3d4-0006-0000-0000-000000000000"),};

    public static final Codec<DifficultyLevelAffix> CODEC = RecordCodecBuilder.create(inst -> inst
            .group(LootRarity.mapCodec(LevelData.CODEC).fieldOf("values").forGetter(a -> a.values),
                    LootCategory.SET_CODEC.fieldOf("types").forGetter(a -> a.types)).apply(inst, DifficultyLevelAffix::new));

    public final Map<LootRarity, LevelData> values;
    protected final Set<LootCategory> types;

    public DifficultyLevelAffix(Map<LootRarity, LevelData> values, Set<LootCategory> types) {
        super(AffixType.ABILITY);
        this.values = values;
        this.types = types;
    }

    @Override
    public Codec<? extends Affix> getCodec() {
        return CODEC;
    }

    @Override
    public MutableComponent getDescription(ItemStack stack, LootRarity rarity, float level) {
        LevelData data = this.values.get(rarity);
        if (data == null) return Component.empty();
        int bonus = data.bonus().getInt(level);
        return Component.translatable("affix.apotheotic_hostility.difficulty_level.desc", bonus);
    }

    @Override
    public Component getAugmentingText(ItemStack stack, LootRarity rarity, float level) {
        LevelData data = this.values.get(rarity);
        if (data == null) return Component.empty();
        int current = data.bonus().getInt(level);
        int min = data.bonus().getInt(0);
        int max = data.bonus().getInt(1);
        MutableComponent comp = Component.translatable("affix.apotheotic_hostility.difficulty_level.desc", current);
        if (min != max) {
            comp.append(Affix.valueBounds(Component.literal(String.valueOf(min)), Component.literal(String.valueOf(max))));
        }
        return comp;
    }

    @Override
    public boolean canApplyTo(ItemStack stack, LootCategory cat, LootRarity rarity) {
        if (cat == null || cat.isNone()) return false;
        return (this.types.isEmpty() || this.types.contains(cat)) && this.values.containsKey(rarity);
    }

    public record LevelData(StepFunction bonus) {
        public static final Codec<LevelData> CODEC = RecordCodecBuilder.create(inst -> inst.group(StepFunction.CODEC.optionalFieldOf("bonus", StepFunction.constant(10)).forGetter(LevelData::bonus)).apply(inst, LevelData::new));
    }
}