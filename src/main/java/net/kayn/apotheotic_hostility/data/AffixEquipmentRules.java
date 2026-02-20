package net.kayn.apotheotic_hostility.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.kayn.apotheotic_hostility.ApotheoticHostility;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.ModList;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class AffixEquipmentRules {

    public static final Codec<AffixEquipmentRules> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            AffixEquipmentEntry.CODEC.listOf().fieldOf("affix_equipment").forGetter(r -> r.entries)
    ).apply(inst, AffixEquipmentRules::new));

    private final List<AffixEquipmentEntry> entries;

    public AffixEquipmentRules(List<AffixEquipmentEntry> entries) {
        this.entries = entries;
    }

    public List<AffixEquipmentEntry> getEntries() {
        return entries;
    }

    public static class AffixEquipmentEntry {

        public static final Codec<AffixEquipmentEntry> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                Codec.INT.fieldOf("min_level").forGetter(r -> r.minLevel),
                ResourceLocation.CODEC.fieldOf("rarity").forGetter(r -> r.rarity),
                Codec.DOUBLE.fieldOf("chance").forGetter(r -> r.chance),
                Condition.CODEC.listOf().optionalFieldOf("conditions", Collections.emptyList()).forGetter(r -> r.conditions)
        ).apply(inst, AffixEquipmentEntry::new));

        private final int minLevel;
        private final ResourceLocation rarity;
        private final double chance;
        private final List<Condition> conditions;

        public AffixEquipmentEntry(int minLevel, ResourceLocation rarity, double chance, List<Condition> conditions) {
            this.minLevel = minLevel;
            this.rarity = rarity;
            this.chance = chance;
            this.conditions = conditions;
        }

        public int getMinLevel() {
            return minLevel;
        }

        public ResourceLocation getRarity() {
            return rarity;
        }

        public double getChance() {
            return chance;
        }

        public boolean testConditions() {
            if (conditions == null || conditions.isEmpty()) return true;
            for (Condition c : conditions) {
                if (!c.test()) return false;
            }
            return true;
        }
    }

    public static final class Condition {

        public static final Codec<Condition> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                Codec.STRING.fieldOf("type").forGetter(c -> c.type),
                Codec.STRING.optionalFieldOf("modid").forGetter(c -> Optional.ofNullable(c.modid))
        ).apply(inst, (type, optModid) -> new Condition(type, optModid.orElse(null))));

        private final String type;
        private final @Nullable String modid;

        public Condition(String type, @Nullable String modid) {
            this.type = type;
            this.modid = modid;
        }

        public boolean test() {
            try {
                if (type != null && type.toLowerCase().contains("mod_loaded")) {
                    if (modid == null || modid.isBlank()) return false;
                    return ModList.get().isLoaded(modid);
                }

                ApotheoticHostility.LOGGER.debug("[AffixCondition] Unknown condition type '{}', failing safely.", this.type);
                return false;
            } catch (Throwable t) {
                ApotheoticHostility.LOGGER.warn("[AffixCondition] Error evaluating condition '{}': {}", type, t.getMessage());
                return false;
            }
        }
    }
}