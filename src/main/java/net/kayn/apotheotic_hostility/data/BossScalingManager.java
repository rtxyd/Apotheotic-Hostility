package net.kayn.apotheotic_hostility.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
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
                BossScalingConfig config = GSON.fromJson(json, BossScalingConfig.class);

                if (config != null && config.list != null) {
                    for (BossScalingConfig.BossScalingEntry entry : config.list) {
                        for (String bossIdStr : entry.bosses) {
                            bossConfigs.put(new ResourceLocation(bossIdStr), entry);
                        }
                    }
                }
            } catch (Exception e) {
                ApotheoticHostility.LOGGER.error("Failed to parse boss scaling config from {}", id, e);
            }
        });
    }

    public BossScalingConfig.BossScalingEntry getEntryForBoss(ResourceLocation bossId) {
        return bossConfigs.get(bossId);
    }

    public boolean hasBossConfig(ResourceLocation bossId) {
        return bossConfigs.containsKey(bossId);
    }
}