package net.kayn.apotheotic_hostility.registry;

import net.kayn.apotheotic_hostility.ApotheoticHostility;
import net.kayn.apotheotic_hostility.content.item.GemLootingCharm;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, ApotheoticHostility.MOD_ID);

    public static final RegistryObject<GemLootingCharm> GEM_LOOTING_CHARM = ITEMS.register("gem_looting_charm",
            () -> new GemLootingCharm(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));
}