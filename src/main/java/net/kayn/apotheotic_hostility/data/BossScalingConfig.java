package net.kayn.apotheotic_hostility.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.xkmc.l2hostility.content.config.EntityConfig;
import dev.xkmc.l2hostility.content.traits.base.MobTrait;
import dev.xkmc.l2hostility.init.registrate.LHTraits;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.List;

public class BossScalingConfig {

    public static final Codec<BossScalingConfig> CODEC = RecordCodecBuilder.create(inst -> inst.group(BossScalingEntry.CODEC.listOf().fieldOf("list").forGetter(c -> c.list)).apply(inst, BossScalingConfig::new));

    private final List<BossScalingEntry> list;

    public BossScalingConfig(List<BossScalingEntry> list) {
        this.list = list;
    }

    public List<BossScalingEntry> getList() {
        return list;
    }

    public static class BossScalingEntry {

        public static final Codec<BossScalingEntry> CODEC = RecordCodecBuilder.create(inst -> inst.group(Codec.DOUBLE.fieldOf("attackScale").forGetter(e -> e.attackScale), ResourceLocation.CODEC.listOf().fieldOf("blacklist").forGetter(e -> e.blacklist), DifficultyConfig.CODEC.fieldOf("difficulty").forGetter(e -> e.difficulty), ResourceLocation.CODEC.listOf().fieldOf("bosses").forGetter(e -> e.bosses), Codec.DOUBLE.fieldOf("healthScale").forGetter(e -> e.healthScale), Codec.INT.fieldOf("maxLevel").forGetter(e -> e.maxLevel), Codec.INT.fieldOf("maxTraitCount").forGetter(e -> e.maxTraitCount), Codec.INT.fieldOf("minSpawnLevel").forGetter(e -> e.minSpawnLevel), TraitEntry.CODEC.listOf().fieldOf("traits").forGetter(e -> e.traits)).apply(inst, BossScalingEntry::new));

        public final double attackScale;
        public final List<ResourceLocation> blacklist;
        public final DifficultyConfig difficulty;
        public final List<ResourceLocation> bosses;
        public final double healthScale;
        public final int maxLevel;
        public final int maxTraitCount;
        public final int minSpawnLevel;
        public final List<TraitEntry> traits;

        public BossScalingEntry(double attackScale, List<ResourceLocation> blacklist, DifficultyConfig difficulty, List<ResourceLocation> bosses, double healthScale, int maxLevel, int maxTraitCount, int minSpawnLevel, List<TraitEntry> traits) {
            this.attackScale = attackScale;
            this.blacklist = blacklist;
            this.difficulty = difficulty;
            this.bosses = bosses;
            this.healthScale = healthScale;
            this.maxLevel = maxLevel;
            this.maxTraitCount = maxTraitCount;
            this.minSpawnLevel = minSpawnLevel;
            this.traits = traits;
        }

        public EntityConfig.Config toEntityConfig() {
            EntityConfig.Config config = EntityConfig.entity(minSpawnLevel, difficulty.base, difficulty.variation, difficulty.scale, healthScale, attackScale, List.of());

            config.minSpawnLevel = minSpawnLevel;
            config.maxLevel = maxLevel;
            config.maxTraitCount = maxTraitCount;

            config.trait(traits.stream().map(t -> EntityConfig.trait(LHTraits.TRAITS.get().getValue(t.trait), t.free, t.min)).filter(t -> t.trait() != null).toList());

            config.blacklist(blacklist.stream().map(id -> LHTraits.TRAITS.get().getValue(id)).filter(java.util.Objects::nonNull).toArray(MobTrait[]::new));

            return config;
        }
    }

    public static class DifficultyConfig {

        public static final Codec<DifficultyConfig> CODEC = RecordCodecBuilder.create(inst -> inst.group(Codec.DOUBLE.fieldOf("apply_chance").forGetter(d -> d.applyChance), Codec.INT.fieldOf("base").forGetter(d -> d.base), Codec.INT.fieldOf("min").forGetter(d -> d.min), Codec.DOUBLE.fieldOf("scale").forGetter(d -> d.scale), Codec.DOUBLE.fieldOf("suppression").forGetter(d -> d.suppression), Codec.DOUBLE.fieldOf("trait_chance").forGetter(d -> d.traitChance), Codec.DOUBLE.fieldOf("variation").forGetter(d -> d.variation)).apply(inst, DifficultyConfig::new));

        public final double applyChance;
        public final int base;
        public final int min;
        public final double scale;
        public final double suppression;
        public final double traitChance;
        public final double variation;

        public DifficultyConfig(double applyChance, int base, int min, double scale, double suppression, double traitChance, double variation) {
            this.applyChance = applyChance;
            this.base = base;
            this.min = min;
            this.scale = scale;
            this.suppression = suppression;
            this.traitChance = traitChance;
            this.variation = variation;
        }
    }

    public static class TraitEntry {

        public static final Codec<TraitEntry> CODEC = RecordCodecBuilder.create(inst -> inst.group(Codec.BOOL.fieldOf("cap").forGetter(t -> t.cap), Codec.INT.fieldOf("free").forGetter(t -> t.free), Codec.INT.fieldOf("min").forGetter(t -> t.min), ResourceLocation.CODEC.fieldOf("trait").forGetter(t -> t.trait)).apply(inst, TraitEntry::new));

        public final boolean cap;
        public final int free;
        public final int min;
        public final ResourceLocation trait;

        public TraitEntry(boolean cap, int free, int min, ResourceLocation trait) {
            this.cap = cap;
            this.free = free;
            this.min = min;
            this.trait = trait;
        }
    }
}