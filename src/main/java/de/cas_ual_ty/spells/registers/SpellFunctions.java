package de.cas_ual_ty.spells.registers;

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

    public static void register(IEventBus modEventBus)
    {
        modEventBus.addListener(SpellFunctions::newDataPackRegistry);
    }

    private static void newDataPackRegistry(DataPackRegistryEvent.NewRegistry event)
    {
        // unsynced - functions are pure server-side execution helpers, never referenced client-side
        event.dataPackRegistry(REGISTRY_KEY, SpellsCodecs.SPELL_FUNCTION_CONTENTS);
    }
}
