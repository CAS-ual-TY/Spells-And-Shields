package de.cas_ual_ty.spells.animation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

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
     * vanilla (or anything else) already set it to. Interpolation between two keyframes is linear, shaped by
     * the arriving keyframe's own {@link EaseType}.
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
                float t = span <= 0 ? 1F : (timeTicks - prev.time()) / (float) span;
                t = next.ease().apply(Mth.clamp(t, 0F, 1F));

                return new AnimationSample(
                        lerp(prev.translate(), next.translate(), t),
                        lerp(prev.rotate(), next.rotate(), t),
                        lerp(prev.scale(), next.scale(), t)
                );
            }
        }

        return AnimationSample.of(last);
    }

    private static Vec3 lerp(Vec3 from, Vec3 to, float t)
    {
        return new Vec3(Mth.lerp(t, from.x(), to.x()), Mth.lerp(t, from.y(), to.y()), Mth.lerp(t, from.z(), to.z()));
    }
}
