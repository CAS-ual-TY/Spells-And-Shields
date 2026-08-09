package de.cas_ual_ty.spells.registers;

import de.cas_ual_ty.spells.animation.PlayerAnimation;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.LevelAccessor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;

import static de.cas_ual_ty.spells.SpellsAndShields.MOD_ID;

public class PlayerAnimations
{
    public static final ResourceKey<Registry<PlayerAnimation>> REGISTRY_KEY = ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(MOD_ID, "player_animations"));

    public static Registry<PlayerAnimation> getRegistry(LevelAccessor level)
    {
        return getRegistry(level.registryAccess());
    }

    public static Registry<PlayerAnimation> getRegistry(RegistryAccess access)
    {
        return access.registryOrThrow(REGISTRY_KEY);
    }

    public static void register(IEventBus modEventBus)
    {
        modEventBus.addListener(PlayerAnimations::newDataPackRegistry);
    }

    private static void newDataPackRegistry(DataPackRegistryEvent.NewRegistry event)
    {
        // synced - clients need the full keyframe data to actually render an animation, unlike SpellFunction
        event.dataPackRegistry(REGISTRY_KEY, PlayerAnimation.CODEC, PlayerAnimation.CODEC);
    }
}
