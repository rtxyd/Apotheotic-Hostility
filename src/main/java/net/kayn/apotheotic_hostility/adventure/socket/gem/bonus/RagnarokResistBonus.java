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

public class RagnarokResistBonus extends GemBonus {

    public static final Codec<RagnarokResistBonus> CODEC = RecordCodecBuilder.create(inst -> inst.group(gemClass()).apply(inst, RagnarokResistBonus::new));

    public RagnarokResistBonus(GemClass gemClass) {
        super(new ResourceLocation("apotheotic_hostility", "ragnarok_resist"), gemClass);
    }

    @Override
    public Component getSocketBonusTooltip(ItemStack gem, LootRarity rarity) {
        return Component.translatable("gem_bonus.apotheotic_hostility.ragnarok_resist.desc").withStyle(ChatFormatting.YELLOW);
    }

    @Override
    public RagnarokResistBonus validate() {
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