package net.kayn.apotheotic_hostility.mixin;

import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.mixin.connect.IMixinConnector;

public class FGAMixinConnector implements IMixinConnector {

    @Override
    public void connect() {
        boolean isFGAExist = getClass().getClassLoader().getResource(
                "net/kayn/fallen_gems_affixes/FallenGemsAffixes.class") != null;
        if (isFGAExist) {
            Mixins.addConfiguration("apotheotic_hostility.fga.mixins.json");
        }
    }
}