package net.kayn.apotheotic_hostility.data;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.kayn.apotheotic_hostility.ApotheoticHostility;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.HashMap;
import java.util.Map;

public class UniversalBossLevelConfig extends SimpleJsonResourceReloadListener {

    private static final Gson GSON = new Gson();
    public static final UniversalBossLevelConfig INSTANCE = new UniversalBossLevelConfig();
    private static final Map<String, Integer> RARITY_MIN_LEVELS = new HashMap<>();

    public UniversalBossLevelConfig() {
        super(GSON, "universal_boss");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> objects, ResourceManager manager, ProfilerFiller profiler) {
        RARITY_MIN_LEVELS.clear();
        for (JsonElement element : objects.values()) {
            JsonObject json = element.getAsJsonObject();
            if (!json.has("tier_chances")) continue;
            JsonObject tiers = json.getAsJsonObject("tier_chances");
            for (Map.Entry<String, JsonElement> entry : tiers.entrySet()) {
                JsonObject tierObj = entry.getValue().getAsJsonObject();
                int minLevel = tierObj.has("min_level") ? tierObj.get("min_level").getAsInt() : 0;
                RARITY_MIN_LEVELS.put(entry.getKey(), minLevel);
            }
            break;
        }
        ApotheoticHostility.LOGGER.info("universal_boss rarity min levels loaded: {}", RARITY_MIN_LEVELS);
    }

    public static int getMinLevelForRarity(String rarity) {
        return RARITY_MIN_LEVELS.getOrDefault(rarity, 0);
    }

    public static Map<String, Integer> getAllMinLevels() {
        return RARITY_MIN_LEVELS;
    }
}