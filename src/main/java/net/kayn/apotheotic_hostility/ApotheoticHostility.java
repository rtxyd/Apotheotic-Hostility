package net.kayn.apotheotic_hostility;

import net.kayn.apotheotic_hostility.event.InitNewCodecs;
import net.kayn.apotheotic_hostility.init.ModLootModifiers;
import net.kayn.apotheotic_hostility.registry.ModCreativeTabs;
import net.kayn.apotheotic_hostility.registry.ModItems;
import net.kayn.apotheotic_hostility.spawner.HostilitySpawnerStats;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

@Mod(ApotheoticHostility.MOD_ID)
public class ApotheoticHostility {
    public static final String MOD_ID = "apotheotic_hostility";
    public static final Logger LOGGER = LogManager.getLogger();

    public ApotheoticHostility(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();

        LOGGER.info("Loading Apotheotic Hostility");

        ModLootModifiers.LOOT_MODIFIERS.register(modEventBus);

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(InitNewCodecs::init);

        ModCreativeTabs.CREATIVE_MODE_TABS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);

        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        HostilitySpawnerStats.register();
        LOGGER.info("Apotheotic Hostility common setup complete");
    }

    public static ResourceLocation id(@NotNull String path) {
        return new ResourceLocation(ApotheoticHostility.MOD_ID, path);
    }
}