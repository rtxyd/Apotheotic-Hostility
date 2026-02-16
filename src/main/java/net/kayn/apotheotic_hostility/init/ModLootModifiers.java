package net.kayn.apotheotic_hostility.init;

import com.mojang.serialization.Codec;
import net.kayn.apotheotic_hostility.ApotheoticHostility;
import net.kayn.apotheotic_hostility.loot.HostilityGemLootModifier;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModLootModifiers {
    public static final DeferredRegister<Codec<? extends IGlobalLootModifier>> LOOT_MODIFIERS =
            DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, ApotheoticHostility.MOD_ID);

    public static final RegistryObject<Codec<HostilityGemLootModifier>> HOSTILITY_GEM_MODIFIER =
            LOOT_MODIFIERS.register("hostility_gem_modifier", () -> HostilityGemLootModifier.CODEC);
}