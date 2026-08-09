package de.cas_ual_ty.spells.compat;

import net.neoforged.fml.ModList;

/**
 * Whether the optional Player Animator compat is actually available - it's {@code compileOnly}/
 * {@code localRuntime} in {@code build.gradle} and declared {@code type="optional"} in
 * {@code neoforge.mods.toml}, so it's not required to run this mod at all. Code that touches its classes must
 * only ever do so behind this flag (and live in the {@code compat.playeranimator} package) - the JVM only
 * verifies a class's own references when that class is first loaded, so as long as nothing outside that package
 * references them directly, a missing library never crashes classloading, it just leaves this {@code false} and
 * the compat hook a no-op.
 */
public class ModCompat
{
    public static final boolean PLAYER_ANIMATOR = ModList.get().isLoaded("playeranimator");
}
