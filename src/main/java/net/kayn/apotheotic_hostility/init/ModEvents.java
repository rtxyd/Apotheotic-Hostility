package net.kayn.apotheotic_hostility.init;

import net.kayn.apotheotic_hostility.data.BossScalingManager;
import net.kayn.apotheotic_hostility.data.GemDropRuleManager;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class ModEvents {

    @SubscribeEvent
    public static void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(GemDropRuleManager.getInstance());
        event.addListener(BossScalingManager.getInstance());
    }
}