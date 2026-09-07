package de.cas_ual_ty.spells.client.particle;

import de.cas_ual_ty.spells.spell.variable.CtxVar;
import net.minecraft.world.phys.Vec3;

import java.util.Map;

/**
 * One particle's live, per-tick state. {@link #prevPosition} is the position as of the end of the previous
 * tick, kept for render-time partial-tick interpolation. {@link #spawnPosition} is this particle's own anchor
 * for ABSOLUTE positioning, captured fresh per batch (not shared emitter-wide), so a repeating ABSOLUTE-attached
 * emitter reads as a trail.
 */
public class CustomParticleInstance
{
    /** Reset to 0 every batch; see {@link #totalIndex} for one that never repeats across batches. */
    public final int index;
    public final int totalIndex;

    public Vec3 position;
    public Vec3 prevPosition;
    public Vec3 motion;

    public double red;
    public double green;
    public double blue;
    public double alpha;

    public int age;

    public final Vec3 spawnPosition;

    /** {@code -1} means unlimited (lives until the emitter itself expires). */
    public final int maxAge;

    public final Map<String, CtxVar<?>> initVars;

    public CustomParticleInstance(int index, int totalIndex, Vec3 position, Vec3 spawnPosition, int maxAge, Map<String, CtxVar<?>> initVars)
    {
        this.index = index;
        this.totalIndex = totalIndex;
        this.position = position;
        this.prevPosition = position;
        this.motion = Vec3.ZERO;
        this.red = 1.0;
        this.green = 1.0;
        this.blue = 1.0;
        this.alpha = 1.0;
        this.age = 0;
        this.spawnPosition = spawnPosition;
        this.maxAge = maxAge;
        this.initVars = initVars;
    }
}
