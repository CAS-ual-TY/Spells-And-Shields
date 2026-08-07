package de.cas_ual_ty.spells.registers;

import de.cas_ual_ty.spells.SpellsAndShields;
import de.cas_ual_ty.spells.spell.SpellFunction;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.LevelAccessor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;

import static de.cas_ual_ty.spells.SpellsAndShields.MOD_ID;

public class SpellFunctions
{
    public static final ResourceKey<Registry<SpellFunction>> REGISTRY_KEY = ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(MOD_ID, "spell_functions"));

    public static Registry<SpellFunction> getRegistry(LevelAccessor level)
    {
        return getRegistry(level.registryAccess());
    }

    public static Registry<SpellFunction> getRegistry(RegistryAccess access)
    {
        return access.registryOrThrow(REGISTRY_KEY);
    }

    public static final ResourceLocation CHECK_MANA_COST = rl("check_mana_cost");
    public static final ResourceLocation CHECK_ITEM_COST = rl("check_item_cost");
    public static final ResourceLocation CHECK_COOLDOWN_COST = rl("check_cooldown_cost");
    public static final ResourceLocation CHECK_MANA_AND_ITEM_COST = rl("check_mana_and_item_cost");
    public static final ResourceLocation CHECK_MANA_AND_COOLDOWN_COST = rl("check_mana_and_cooldown_cost");
    public static final ResourceLocation CHECK_ITEM_AND_COOLDOWN_COST = rl("check_item_and_cooldown_cost");
    public static final ResourceLocation CHECK_MANA_AND_ITEM_AND_COOLDOWN_COST = rl("check_mana_and_item_and_cooldown_cost");

    public static final ResourceLocation HAS_MANA_COST = rl("has_mana_cost");
    public static final ResourceLocation BURN_MANA_COST = rl("burn_mana_cost");
    public static final ResourceLocation HAS_ITEM_COST = rl("has_item_cost");
    public static final ResourceLocation CONSUME_ITEM_COST = rl("consume_item_cost");
    public static final ResourceLocation HAS_COOLDOWN_COST = rl("has_cooldown_cost");
    public static final ResourceLocation SET_COOLDOWN_COST = rl("set_cooldown_cost");

    public static void register(IEventBus modEventBus)
    {
        modEventBus.addListener(SpellFunctions::newDataPackRegistry);
    }

    private static void newDataPackRegistry(DataPackRegistryEvent.NewRegistry event)
    {
        // unsynced - functions are pure server-side execution helpers, never referenced client-side
        event.dataPackRegistry(REGISTRY_KEY, SpellsCodecs.SPELL_FUNCTION_CONTENTS);
    }

    private static ResourceLocation rl(String path)
    {
        return ResourceLocation.fromNamespaceAndPath(SpellsAndShields.MOD_ID, path);
    }
}
