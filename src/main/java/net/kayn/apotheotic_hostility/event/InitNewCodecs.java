package net.kayn.apotheotic_hostility.event;

import dev.shadowsoffire.apotheosis.adventure.affix.AffixRegistry;
import net.kayn.apotheotic_hostility.adventure.affix.DifficultyLevelAffix;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public class InitNewCodecs {
    @SubscribeEvent
    public static void init(FMLCommonSetupEvent e) {
        e.enqueueWork(() -> AffixRegistry.INSTANCE.registerCodec(new ResourceLocation("apotheotic_hostility", "difficulty_level"), DifficultyLevelAffix.CODEC));
    }
}