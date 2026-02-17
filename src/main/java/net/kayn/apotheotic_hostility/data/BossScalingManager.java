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

import java.util.HashMap;
import java.util.Map;

public class BossScalingManager extends SimpleJsonResourceReloadListener {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final String DIRECTORY = "boss_config/bosses";

    private static BossScalingManager INSTANCE;

    private final Map<ResourceLocation, BossScalingConfig.BossScalingEntry> bossConfigs = new HashMap<>();

    public BossScalingManager() {
        super(GSON, DIRECTORY);
    }

    public static BossScalingManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new BossScalingManager();
        }
        return INSTANCE;
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> objectIn, ResourceManager resourceManager, ProfilerFiller profiler) {
        bossConfigs.clear();

        objectIn.forEach((id, json) -> {
            try {
                BossScalingConfig config = BossScalingConfig.CODEC.parse(JsonOps.INSTANCE, json)
                        .getOrThrow(false, ApotheoticHostility.LOGGER::error);

                for (BossScalingConfig.BossScalingEntry entry : config.getList()) {
                    for (ResourceLocation bossId : entry.bosses) {
                        bossConfigs.put(bossId, entry);

                        ApotheoticHostility.LOGGER.debug(
                                "Registered boss scaling for {} (base: {}, min: {})",
                                bossId,
                                entry.difficulty.base,
                                entry.difficulty.min
                        );
                    }
                }

                ApotheoticHostility.LOGGER.info("Loaded boss scaling config from {}", id);
            } catch (Exception e) {
                ApotheoticHostility.LOGGER.error("Failed to parse boss scaling config from {}", id, e);
            }
        });

        ApotheoticHostility.LOGGER.info("Total boss scaling entries loaded: {}", bossConfigs.size());
    }

    public BossScalingConfig.BossScalingEntry getEntryForBoss(ResourceLocation bossId) {
        return bossConfigs.get(bossId);
    }

    public boolean hasBossConfig(ResourceLocation bossId) {
        return bossConfigs.containsKey(bossId);
    }
}