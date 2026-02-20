package net.kayn.apotheotic_hostility.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.kayn.apotheotic_hostility.ApotheoticHostility;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AffixEquipmentRuleManager extends SimpleJsonResourceReloadListener {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final String DIRECTORY = "affix_equipment";

    private static AffixEquipmentRuleManager INSTANCE;
    private final List<AffixEquipmentRules.AffixEquipmentEntry> rules = new ArrayList<>();

    public AffixEquipmentRuleManager() {
        super(GSON, DIRECTORY);
    }

    public static AffixEquipmentRuleManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new AffixEquipmentRuleManager();
        }
        return INSTANCE;
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> objectIn, ResourceManager resourceManager, ProfilerFiller profiler) {
        rules.clear();

        objectIn.forEach((id, json) -> {
            try {
                AffixEquipmentRules affixRules = AffixEquipmentRules.CODEC.parse(JsonOps.INSTANCE, json)
                        .getOrThrow(false, ApotheoticHostility.LOGGER::error);

                rules.addAll(affixRules.getEntries());

                ApotheoticHostility.LOGGER.info("Loaded {} affix equipment rules from {}",
                        affixRules.getEntries().size(), id);
            } catch (Exception e) {
                ApotheoticHostility.LOGGER.error("Failed to parse affix equipment rules from {}", id, e);
            }
        });

        rules.sort((a, b) -> Integer.compare(b.getMinLevel(), a.getMinLevel()));

        ApotheoticHostility.LOGGER.info("Total affix equipment rules loaded: {}", rules.size());
    }

    public AffixEquipmentRules.AffixEquipmentEntry getRuleForLevel(int mobLevel) {
        for (AffixEquipmentRules.AffixEquipmentEntry rule : rules) {
            if (mobLevel >= rule.getMinLevel() && rule.testConditions()) {
                return rule;
            }
        }
        return null;
    }

    public List<AffixEquipmentRules.AffixEquipmentEntry> getAllRules() {
        return new ArrayList<>(rules);
    }
}