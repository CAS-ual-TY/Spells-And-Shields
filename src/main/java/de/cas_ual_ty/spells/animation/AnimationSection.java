package de.cas_ual_ty.spells.animation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * One view's (first- or third-person) worth of animation data - a set of independent keyframe tracks, one per
 * body part name. A view's overall duration is the max keyframe time across all its parts (see
 * {@link #getDuration}), not a separately declared length.
 */
public class AnimationSection
{
    public static final Codec<AnimationSection> CODEC = Codec.unboundedMap(Codec.STRING, Keyframe.CODEC.listOf())
            .xmap(AnimationSection::new, AnimationSection::getParts);

    protected final Map<String, List<Keyframe>> parts;
    protected final int duration;

    public AnimationSection(Map<String, List<Keyframe>> parts)
    {
        this.parts = new LinkedHashMap<>();
        int maxTime = 0;

        for(Map.Entry<String, List<Keyframe>> entry : parts.entrySet())
        {
            List<Keyframe> sorted = entry.getValue().stream().sorted(Comparator.comparingInt(Keyframe::time)).toList();
            this.parts.put(entry.getKey(), sorted);

            for(Keyframe keyframe : sorted)
            {
                maxTime = Math.max(maxTime, keyframe.time());
            }
        }

        duration = maxTime;
    }

    public Map<String, List<Keyframe>> getParts()
    {
        return parts;
    }

    /**
     * The overall length of this view, in ticks - the max keyframe time across every part's track.
     */
    public int getDuration()
    {
        return duration;
    }

    /**
     * Samples {@code part}'s track at {@code timeTicks}. Before the track's first keyframe or after its last,
     * holds that keyframe's value. A part with no track at all (or an empty one) returns {@code null} - not the
     * identity transform - so callers know to leave that part alone entirely rather than overwriting whatever
     * vanilla (or anything else) already set it to.
     * <p>
     * {@code translate}/{@code scale} are interpolated with a plain componentwise lerp - straight lines, so
     * that's exact. {@code rotate} is interpolated by converting both keyframes' Euler angles to quaternions and
     * spherically interpolating ({@code slerp}) between them - the result stays a quaternion (see
     * {@link AnimationSample}), it's deliberately never converted back to Euler angles here. Lerping the three
     * Euler components independently (as if they were unrelated numbers) doesn't correspond to a smooth rotation
     * once more than one axis changes at once, since composing three sequential axis rotations doesn't commute;
     * slerp guarantees constant angular velocity along the shortest path regardless. Converting the slerped
     * result back to Euler angles would reintroduce a version of the same problem - the single fixed-axis
     * rotation slerp produces can decompose into Euler components that don't individually move monotonically
     * (eg. a visible "roll" appearing and then undoing itself even though the actual 3D rotation never
     * reversed) - so that conversion is left to whichever consumer actually needs Euler angles (ie. the
     * third-person mixin, for {@code ModelPart}), not done centrally here.
     * <p>
     * The eased shape of a segment is contributed by <em>both</em> of its keyframes, not just the one being
     * arrived at: the earlier keyframe's own {@link EaseType} controls departure (its {@code OUT}/{@code BOTH}
     * eases the start of the segment leaving it, ie. a slow/accelerating start), and the later keyframe's own
     * {@link EaseType} controls arrival ({@code IN}/{@code BOTH} eases the end of the segment arriving at it,
     * ie. a decelerating stop). Both can apply to the same segment at once (a full ease-in-out S-curve); if
     * neither does, the segment is linear.
     */
    @Nullable
    public AnimationSample sample(String part, float timeTicks)
    {
        List<Keyframe> keyframes = parts.get(part);

        if(keyframes == null || keyframes.isEmpty())
        {
            return null;
        }

        Keyframe first = keyframes.get(0);

        if(timeTicks <= first.time())
        {
            return AnimationSample.of(first);
        }

        Keyframe last = keyframes.get(keyframes.size() - 1);

        if(timeTicks >= last.time())
        {
            return AnimationSample.of(last);
        }

        for(int i = 1; i < keyframes.size(); i++)
        {
            Keyframe next = keyframes.get(i);

            if(timeTicks <= next.time())
            {
                Keyframe prev = keyframes.get(i - 1);
                int span = next.time() - prev.time();
                float rawT = span <= 0 ? 1F : (timeTicks - prev.time()) / (float) span;
                float t = shapeSegment(prev.ease(), next.ease(), Mth.clamp(rawT, 0F, 1F));

                return new AnimationSample(
                        lerp(prev.translate(), next.translate(), t),
                        slerpRotation(prev.rotate(), next.rotate(), t),
                        lerp(prev.scale(), next.scale(), t)
                );
            }
        }

        return AnimationSample.of(last);
    }

    /**
     * Combines the departing keyframe's own ease (does it want a slow/accelerating start leaving it?) and the
     * arriving keyframe's own ease (does it want a decelerating stop arriving at it?) into the one curve shape
     * that actually applies to this segment - see {@link #sample}.
     */
    private static float shapeSegment(EaseType prevEase, EaseType nextEase, float t)
    {
        boolean easeOutOfPrev = prevEase == EaseType.OUT || prevEase == EaseType.BOTH;
        boolean easeIntoNext = nextEase == EaseType.IN || nextEase == EaseType.BOTH;

        if(easeOutOfPrev && easeIntoNext)
        {
            return EaseType.BOTH.apply(t);
        }
        else if(easeOutOfPrev)
        {
            return EaseType.IN.apply(t);
        }
        else if(easeIntoNext)
        {
            return EaseType.OUT.apply(t);
        }
        else
        {
            return t;
        }
    }

    private static Vec3 lerp(Vec3 from, Vec3 to, float t)
    {
        return new Vec3(Mth.lerp(t, from.x(), to.x()), Mth.lerp(t, from.y(), to.y()), Mth.lerp(t, from.z(), to.z()));
    }

    private static Quaternionf slerpRotation(Vec3 from, Vec3 to, float t)
    {
        Quaternionf fromQuat = new Quaternionf().rotateXYZ((float) from.x(), (float) from.y(), (float) from.z());
        Quaternionf toQuat = new Quaternionf().rotateXYZ((float) to.x(), (float) to.y(), (float) to.z());
        return fromQuat.slerp(toQuat, t);
    }
}
