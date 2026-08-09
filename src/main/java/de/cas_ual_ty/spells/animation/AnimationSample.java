package de.cas_ual_ty.spells.animation;

import net.minecraft.world.phys.Vec3;

/**
 * A resolved transform for one body part at one point in time - what {@link AnimationSection#sample} produces.
 */
public record AnimationSample(Vec3 translate, Vec3 rotate, Vec3 scale)
{
    public static AnimationSample of(Keyframe keyframe)
    {
        return new AnimationSample(keyframe.translate(), keyframe.rotate(), keyframe.scale());
    }
}
