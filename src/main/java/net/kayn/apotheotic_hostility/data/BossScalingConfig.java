package net.kayn.apotheotic_hostility.data;

import dev.xkmc.l2hostility.content.config.EntityConfig;
import dev.xkmc.l2hostility.content.config.WorldDifficultyConfig;
import dev.xkmc.l2hostility.content.traits.base.MobTrait;
import dev.xkmc.l2hostility.init.registrate.LHTraits;
import net.kayn.apotheotic_hostility.ApotheoticHostility;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class BossScalingConfig {

    public List<BossScalingEntry> list;

    public static class BossScalingEntry {
        public double attackScale, healthScale;

        public List<String> blacklist;

        public List<String> bosses;

        public DifficultyConfig difficulty;

        public int maxLevel, maxTraitCount;

        public List<TraitEntry> traits;

        public List<RarityRequirement> rarity_requirements;

        public EntityConfig.Config toEntityConfig() {
            WorldDifficultyConfig.DifficultyConfig diffConfig = new WorldDifficultyConfig.DifficultyConfig(
                    difficulty.min, difficulty.base, difficulty.variation, difficulty.scale,
                    difficulty.apply_chance, difficulty.trait_chance, difficulty.suppression
            );

            EntityConfig.Config config = new EntityConfig.Config(List.of(), diffConfig);
            config.maxLevel = maxLevel;
            config.maxTraitCount = maxTraitCount;
            config.healthScale = healthScale;
            config.attackScale = attackScale;

            if (traits != null && !traits.isEmpty()) {
                List<EntityConfig.TraitBase> resolved = traits.stream().map(t -> {
                    if (t == null || t.trait == null) {
                        ApotheoticHostility.LOGGER.warn("[BossScaling] null trait entry found in boss config");
                        return null;
                    }
                    ResourceLocation rl;
                    try {
                        rl = new ResourceLocation(t.trait);
                    } catch (Exception ex) {
                        ApotheoticHostility.LOGGER.warn("[BossScaling] invalid trait resource location '{}'", t.trait);
                        return null;
                    }
                    MobTrait traitObj = LHTraits.TRAITS.get().getValue(rl);
                    if (traitObj == null) {
                        ApotheoticHostility.LOGGER.warn("[BossScaling] trait id {} did not resolve to a MobTrait", rl);
                        return null;
                    }
                    return EntityConfig.trait(traitObj, t.free, t.min);
                }).filter(Objects::nonNull).collect(Collectors.toList());

                if (!resolved.isEmpty()) config.trait(resolved);
            }

            if (blacklist != null && !blacklist.isEmpty()) {
                MobTrait[] resolvedBlack = blacklist.stream().map(s -> {
                    try {
                        return LHTraits.TRAITS.get().getValue(new ResourceLocation(s));
                    } catch (Exception ex) {
                        ApotheoticHostility.LOGGER.warn("[BossScaling] invalid blacklist id '{}'", s);
                        return null;
                    }
                }).filter(Objects::nonNull).toArray(MobTrait[]::new);

                if (resolvedBlack.length > 0) config.blacklist(resolvedBlack);
            }

            return config;
        }

        public Map<ResourceLocation, Integer> getRarityRequirementMap() {
            if (rarity_requirements == null || rarity_requirements.isEmpty()) {
                return new HashMap<>();
            }

            Map<ResourceLocation, Integer> map = new HashMap<>();
            for (RarityRequirement req : rarity_requirements) {
                try {
                    ResourceLocation rarityId = new ResourceLocation(req.rarity);
                    map.put(rarityId, req.minSpawnLevel);
                } catch (Exception ex) {
                    ApotheoticHostility.LOGGER.warn("[BossScaling] invalid rarity id '{}'", req.rarity);
                }
            }
            return map;
        }
    }

    public static class DifficultyConfig {
        public double apply_chance, scale, suppression, trait_chance, variation;
        public int base, min;
    }

    public static class TraitEntry {
        public boolean cap;
        public int free, min;
        public String trait;
    }

    public static class RarityRequirement {
        public String rarity;
        public int minSpawnLevel;
    }
}