package de.cas_ual_ty.spells.client.particle;

import net.minecraft.world.phys.Vec3;

/**
 * One particle's live, per-tick state within a {@link CustomParticleEmitterInstance}. {@link #prevPosition} is
 * kept alongside {@link #position} purely for render-time partial-tick interpolation (same idea as vanilla
 * {@code Particle}'s own xo/yo/zo) - it's the position as of the END of the previous tick, updated once per
 * tick right before {@link #position} changes again.
 */
public class CustomParticleInstance
{
    public final int index;

    public Vec3 position;
    public Vec3 prevPosition;
    public Vec3 motion;

    public double red;
    public double green;
    public double blue;
    public double alpha;

    public int age;

    public CustomParticleInstance(int index, Vec3 position)
    {
        this.index = index;
        this.position = position;
        this.prevPosition = position;
        this.motion = Vec3.ZERO;
        this.red = 1.0;
        this.green = 1.0;
        this.blue = 1.0;
        this.alpha = 1.0;
        this.age = 0;
    }
}
