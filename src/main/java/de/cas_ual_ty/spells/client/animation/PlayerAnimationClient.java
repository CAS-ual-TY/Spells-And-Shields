package de.cas_ual_ty.spells.client.animation;

import de.cas_ual_ty.spells.animation.AnimationSample;
import de.cas_ual_ty.spells.animation.AnimationSection;
import de.cas_ual_ty.spells.animation.PlayerAnimation;
import de.cas_ual_ty.spells.client.SpellsClientUtil;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Client-only tracker of which {@link PlayerAnimation} is currently playing on which entity (by id, not tied
 * to any capability - pure presentation state, never persisted). A new {@link #play} for an entity fully
 * replaces whatever was already playing, no queueing or blending. Both render hooks ({@code RenderHandEvent}
 * for first person, the {@code PlayerModel} mixin for third person) sample through here.
 */
public class PlayerAnimationClient
{
    private static final Map<Integer, Active> ACTIVE = new HashMap<>();

    public static void play(int entityId, PlayerAnimation animation)
    {
        ACTIVE.put(entityId, new Active(animation, currentTime()));
    }

    public static void stop(int entityId)
    {
        ACTIVE.remove(entityId);
    }

    @Nullable
    public static AnimationSample sampleFirstPerson(int entityId, String part, float partialTick)
    {
        return sample(entityId, part, partialTick, PlayerAnimation::getFirstPerson);
    }

    @Nullable
    public static AnimationSample sampleThirdPerson(int entityId, String part, float partialTick)
    {
        return sample(entityId, part, partialTick, PlayerAnimation::getThirdPerson);
    }

    @Nullable
    private static AnimationSample sample(int entityId, String part, float partialTick, Function<PlayerAnimation, AnimationSection> view)
    {
        Active active = ACTIVE.get(entityId);

        if(active == null)
        {
            return null;
        }

        AnimationSection section = view.apply(active.animation());
        float elapsed = elapsedTicks(active, partialTick);

        if(elapsed > section.getDuration())
        {
            return null;
        }

        return section.sample(part, elapsed);
    }

    private static float elapsedTicks(Active active, float partialTick)
    {
        return (currentTime() - active.startTime()) + partialTick;
    }

    private static long currentTime()
    {
        Level level = SpellsClientUtil.getClientLevel();
        return level != null ? level.getGameTime() : 0L;
    }

    private record Active(PlayerAnimation animation, long startTime)
    {
    }
}
