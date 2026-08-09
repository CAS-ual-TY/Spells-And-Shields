package de.cas_ual_ty.spells.animation;

import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

/**
 * A resolved transform for one body part at one point in time - what {@link AnimationSection#sample} produces.
 * {@code rotate} is a quaternion, not Euler angles, specifically so consumers that compose it via quaternion
 * multiplication (eg. {@code PoseStack.mulPose}, used for first-person) can use it directly - decomposing an
 * interpolated quaternion into separate X/Y/Z numbers and only then reapplying it is where the wobble came
 * from (see the third-person mixin, which does need Euler angles for {@code ModelPart} and extracts them
 * itself from this same quaternion).
 */
public record AnimationSample(Vec3 translate, Quaternionf rotate, Vec3 scale)
{
    public static AnimationSample of(Keyframe keyframe)
    {
        Quaternionf rotate = new Quaternionf().rotateXYZ((float) keyframe.rotate().x(), (float) keyframe.rotate().y(), (float) keyframe.rotate().z());
        return new AnimationSample(keyframe.translate(), rotate, keyframe.scale());
    }
}
