package de.cas_ual_ty.spells.registers;

import net.minecraft.resources.ResourceLocation;

import static de.cas_ual_ty.spells.SpellsAndShields.MOD_ID;

/**
 * Player Animator animation ids - not a Minecraft registry, unlike most other constant-holder classes in this
 * package. These just name the client-only assets Player Animator itself loads from
 * {@code assets/<namespace>/player_animations/*.json} (keyed by the animation's own declared {@code name}, not
 * its filename), see {@link de.cas_ual_ty.spells.compat.playeranimator.PlayerAnimatorHooks}. Kept here purely so
 * datagen/spell code references a named constant instead of a raw string.
 */
public class PlayerAnimations
{
    public static final ResourceLocation STAB = rl("stab");
    public static final ResourceLocation STAB_OFF = rl("stab_off");
    public static final ResourceLocation STAB_1P = rl("stab_1p");
    public static final ResourceLocation STAB_1P_OFF = rl("stab_1p_off");

    private static ResourceLocation rl(String path)
    {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
