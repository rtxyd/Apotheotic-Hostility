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

public class GemDropRuleManager extends SimpleJsonResourceReloadListener {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final String DIRECTORY = "gem_drops";

    private static GemDropRuleManager INSTANCE;
    private final List<GemDropRules.GemDropEntry> rules = new ArrayList<>();

    public GemDropRuleManager() {
        super(GSON, DIRECTORY);
    }

    public static GemDropRuleManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new GemDropRuleManager();
        }
        return INSTANCE;
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> objectIn, ResourceManager resourceManager, ProfilerFiller profiler) {
        rules.clear();

        objectIn.forEach((id, json) -> {
            try {
                GemDropRules gemDropRules = GemDropRules.CODEC.parse(JsonOps.INSTANCE, json)
                        .getOrThrow(false, ApotheoticHostility.LOGGER::error);

                rules.addAll(gemDropRules.getGemDrops());

                ApotheoticHostility.LOGGER.info("Loaded {} gem drop rules from {}",
                        gemDropRules.getGemDrops().size(), id);

                for (GemDropRules.GemDropEntry rule : gemDropRules.getGemDrops()) {
                    ApotheoticHostility.LOGGER.debug("  - {} at level {}+ ({}% chance)",
                            rule.getTier(), rule.getMinLevel(), rule.getChance() * 100);
                }
            } catch (Exception e) {
                ApotheoticHostility.LOGGER.error("Failed to parse gem drop rules from {}", id, e);
            }
        });
        rules.sort((a, b) -> Integer.compare(b.getMinLevel(), a.getMinLevel()));

        ApotheoticHostility.LOGGER.info("Total gem drop rules loaded: {}", rules.size());
    }
    public GemDropRules.GemDropEntry getDropForLevel(int mobLevel) {
        for (GemDropRules.GemDropEntry rule : rules) {
            if (mobLevel >= rule.getMinLevel()) {
                return rule;
            }
        }
        return null;
    }

    public List<GemDropRules.GemDropEntry> getAllRules() {
        return new ArrayList<>(rules);
    }
}