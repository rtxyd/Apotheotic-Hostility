package net.kayn.apotheotic_hostility.event;

import dev.shadowsoffire.apotheosis.adventure.affix.AffixHelper;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import net.kayn.apotheotic_hostility.adventure.affix.DifficultyLevelAffix;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = "apotheotic_hostility", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class AffixEquipmentEvents {

    private static final ResourceLocation ADD_LEVEL_RL =
            new ResourceLocation("l2hostility", "extra_difficulty");
    private static final String MODIFIER_NAME = "apotheotic_hostility:difficulty_level";

    @SubscribeEvent
    public static void onEquipmentChange(LivingEquipmentChangeEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.level().isClientSide()) return;

        Attribute addLevel = ForgeRegistries.ATTRIBUTES.getValue(ADD_LEVEL_RL);
        if (addLevel == null) return;

        AttributeInstance inst = player.getAttribute(addLevel);
        if (inst == null) return;

        inst.getModifiers().stream()
                .filter(m -> m.getName().equals(MODIFIER_NAME))
                .toList()
                .forEach(inst::removeModifier);

        final int[] totalBonus = {0};
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack stack = player.getItemBySlot(slot);
            if (stack.isEmpty()) continue;

            AffixHelper.streamAffixes(stack).forEach(affixInst -> {
                if (affixInst.affix().get() instanceof DifficultyLevelAffix dla) {
                    LootRarity rarity = affixInst.rarity().get();
                    float level = affixInst.level();
                    DifficultyLevelAffix.LevelData data = dla.values.get(rarity);
                    if (data != null) {
                        totalBonus[0] += data.bonus().getInt(level); // accumulate
                    }
                }
            });
        }

        if (totalBonus[0] > 0) {
            inst.addTransientModifier(new AttributeModifier(
                    UUID.nameUUIDFromBytes(MODIFIER_NAME.getBytes()),
                    MODIFIER_NAME,
                    totalBonus[0],
                    AttributeModifier.Operation.ADDITION
            ));
        }
    }
}