package net.kayn.apotheotic_hostility.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class GemDropRules {

    public static final Codec<GemDropRules> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            GemDropEntry.CODEC.listOf().fieldOf("gem_drops").forGetter(r -> r.gemDrops)
    ).apply(inst, GemDropRules::new));

    private final List<GemDropEntry> gemDrops;

    public GemDropRules(List<GemDropEntry> gemDrops) {
        this.gemDrops = gemDrops;
    }

    public List<GemDropEntry> getGemDrops() {
        return gemDrops;
    }

    public static class GemDropEntry {

        public static final Codec<GemDropEntry> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                Codec.INT.fieldOf("min_level").forGetter(r -> r.minLevel),
                ResourceLocation.CODEC.fieldOf("tier").forGetter(r -> r.tier),
                Codec.DOUBLE.fieldOf("chance").forGetter(r -> r.chance)
        ).apply(inst, GemDropEntry::new));

        private final int minLevel;
        private final ResourceLocation tier;
        private final double chance;

        public GemDropEntry(int minLevel, ResourceLocation tier, double chance) {
            this.minLevel = minLevel;
            this.tier = tier;
            this.chance = chance;
        }

        public int getMinLevel() {
            return minLevel;
        }

        public ResourceLocation getTier() {
            return tier;
        }

        public double getChance() {
            return chance;
        }
    }
}