package de.cas_ual_ty.spells.animation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.phys.Vec3;

/**
 * One point on a single body part's animation track. {@code translate}/{@code rotate} (radians) are offsets
 * added on top of the part's existing position/rotation (its rest pose, or whatever vanilla/other logic
 * already set that frame) rather than absolute values - the identity {@code (0,0,0)} default means "no
 * change", not "snap to the model's origin". {@code scale} is likewise multiplied on top, defaulting to
 * {@code (1,1,1)}. {@code ease} contributes to <em>both</em> of this keyframe's adjacent segments: {@code OUT}/
 * {@code BOTH} eases this keyframe's own departure (the start of the segment leaving it, toward the next
 * keyframe), and {@code IN}/{@code BOTH} eases its own arrival (the end of the segment coming from the
 * previous keyframe) - see {@link AnimationSection#sample}. Consequently a track's first keyframe only ever
 * has an outgoing segment to shape (its own {@code IN} component is moot), and the last keyframe only ever has
 * an incoming one (its own {@code OUT} component is moot).
 */
public record Keyframe(int time, Vec3 translate, Vec3 rotate, Vec3 scale, EaseType ease)
{
    public static final Vec3 IDENTITY_SCALE = new Vec3(1D, 1D, 1D);

    public static final Codec<Keyframe> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("time").forGetter(Keyframe::time),
            Vec3.CODEC.optionalFieldOf("translate", Vec3.ZERO).forGetter(Keyframe::translate),
            Vec3.CODEC.optionalFieldOf("rotate", Vec3.ZERO).forGetter(Keyframe::rotate),
            Vec3.CODEC.optionalFieldOf("scale", IDENTITY_SCALE).forGetter(Keyframe::scale),
            EaseType.CODEC.optionalFieldOf("ease", EaseType.NONE).forGetter(Keyframe::ease)
    ).apply(instance, Keyframe::new));
}
