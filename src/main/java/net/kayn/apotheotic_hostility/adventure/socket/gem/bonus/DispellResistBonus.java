package net.kayn.apotheotic_hostility.adventure.socket.gem.bonus;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.apotheosis.adventure.loot.RarityRegistry;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.GemClass;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.bonus.GemBonus;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class DispellResistBonus extends GemBonus {

    public static final Codec<DispellResistBonus> CODEC = RecordCodecBuilder.create(inst -> inst
            .group(gemClass())
            .apply(inst, DispellResistBonus::new));

    public DispellResistBonus(GemClass gemClass) {
        super(new ResourceLocation("apotheotic_hostility", "dispell_resist"), gemClass);
    }

    @Override
    public Component getSocketBonusTooltip(ItemStack gem, LootRarity rarity) {
        return Component.translatable("gem_bonus.apotheotic_hostility.dispell_resist.desc")
                .withStyle(ChatFormatting.YELLOW);
    }

    @Override
    public DispellResistBonus validate() {
        return this;
    }

    @Override
    public boolean supports(LootRarity rarity) {
        ResourceLocation key = RarityRegistry.INSTANCE.getKey(rarity);
        return key != null && key.getPath().equals("ancient");
    }

    @Override
    public int getNumberOfUUIDs() {
        return 0;
    }

    @Override
    public Codec<? extends GemBonus> getCodec() {
        return CODEC;
    }
}