package net.kayn.fallen_traits;

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
    public static final String MOD_ID = "fallen_traits";
    private static final Logger LOGGER = LogManager.getLogger();

    public ApotheoticHostility(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();

        LOGGER.info("Loading Fallen Traits");

        modEventBus.addListener(this::commonSetup);

        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
    }

    public static ResourceLocation id(@NotNull String path) {
        return new ResourceLocation(ApotheoticHostility.MOD_ID, path);
    }
}