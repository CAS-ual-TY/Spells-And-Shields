package de.cas_ual_ty.spells.client.particle;

import com.mojang.serialization.Codec;

/**
 * Position and rotation attachment are independent toggles (see {@link CustomParticleEmitterInstance}), not one
 * combined "attached or not" flag - eg. {@code ABSOLUTE} position + {@code RELATIVE} rotation means a particle
 * stays put in the world but still reorients as the attached entity turns.
 */
public enum CustomParticleAttachMode
{
    /**
     * Resolved once (or independently of the entity) - the entity moving/turning afterward doesn't affect it.
     */
    ABSOLUTE,
    /**
     * Re-resolved against the entity's current (interpolated) transform every frame.
     */
    RELATIVE;

    public static final Codec<CustomParticleAttachMode> CODEC = Codec.STRING.xmap(s -> valueOf(s.toUpperCase()), mode -> mode.name().toLowerCase());
}
