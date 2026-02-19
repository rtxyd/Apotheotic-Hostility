package net.kayn.apotheotic_hostility.spawner;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.shadowsoffire.apotheosis.spawn.modifiers.SpawnerStat;
import dev.shadowsoffire.apotheosis.spawn.modifiers.StatModifier;
import dev.shadowsoffire.apotheosis.spawn.spawner.ApothSpawnerBlock;
import dev.shadowsoffire.apotheosis.spawn.spawner.ApothSpawnerTile;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public class LevelStat implements SpawnerStat<Integer> {

    public static final Codec<Integer> BOUNDS_CODEC = Codec.intRange(-1, Integer.MAX_VALUE);

    public static final LevelStat INSTANCE = new LevelStat();

    private static final String ID = "apotheotic_hostility:level";

    private final Codec<StatModifier<Integer>> modifierCodec =
            RecordCodecBuilder.create(inst -> inst
                    .group(
                            Codec.INT.fieldOf("value").forGetter(StatModifier::value),
                            BOUNDS_CODEC.fieldOf("min").forGetter(StatModifier::min),
                            BOUNDS_CODEC.fieldOf("max").forGetter(StatModifier::max))
                    .apply(inst, (value, min, max) ->
                            new StatModifier<>(this, value,
                                    min == -1 ? 0 : min,
                                    max == -1 ? Integer.MAX_VALUE : max)));

    private LevelStat() {}

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public Codec<StatModifier<Integer>> getModifierCodec() {
        return this.modifierCodec;
    }

    @Override
    public Integer getValue(ApothSpawnerTile spawner) {
        if (spawner instanceof IL2HSpawner l2h) {
            return l2h.apotheotic_hostility$getLevel();
        }
        return 0;
    }

    @Override
    public Component getTooltip(ApothSpawnerTile spawner) {
        int level = getValue(spawner);
        return ApothSpawnerBlock.concat(
                Component.translatable("stat.apotheotic_hostility.level").withStyle(ChatFormatting.GREEN),
                level
        );
    }


    @Override
    public boolean apply(Integer value, Integer min, Integer max, ApothSpawnerTile spawner) {
        if (spawner instanceof IL2HSpawner l2h) {
            int old = l2h.apotheotic_hostility$getLevel();
            int newLevel = Mth.clamp(old + value, min, max);
            l2h.apotheotic_hostility$setLevel(newLevel);
            return old != newLevel;
        }
        return false;
    }
}