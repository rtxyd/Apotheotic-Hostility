package net.kayn.apotheotic_hostility.compat;

import dev.xkmc.l2hostility.content.config.SpecialConfigCondition;
import dev.xkmc.l2serial.serialization.SerialClass;
import net.minecraft.resources.ResourceLocation;

@SerialClass
public class ApothBossCondition extends SpecialConfigCondition<ApothBossData> {

    @SerialClass.SerialField
    public ResourceLocation bossId;

    public ApothBossCondition() {
        super(ApothBossData.class);
    }

    public static ApothBossCondition of(ResourceLocation id) {
        ApothBossCondition cond = new ApothBossCondition();
        cond.bossId = id;
        return cond;
    }

    @Override
    public boolean test(ApothBossData context) {
        return context.id().equals(this.bossId);
    }
}
