package de.cas_ual_ty.spells.compat.playeranimator;

import de.cas_ual_ty.spells.SpellsAndShields;
import dev.kosmx.playerAnim.api.IPlayable;
import dev.kosmx.playerAnim.api.TransformType;
import dev.kosmx.playerAnim.api.firstPerson.FirstPersonConfiguration;
import dev.kosmx.playerAnim.api.firstPerson.FirstPersonMode;
import dev.kosmx.playerAnim.api.layered.AnimationStack;
import dev.kosmx.playerAnim.api.layered.IActualAnimation;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.api.layered.modifier.AbstractModifier;
import dev.kosmx.playerAnim.core.util.Vec3f;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationAccess;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationRegistry;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Only ever touched behind {@link de.cas_ual_ty.spells.compat.ModCompat#PLAYER_ANIMATOR} - see that class' doc.
 * <p>
 * Animations themselves aren't ours to define - PlayerAnimator loads them client-side from
 * {@code assets/<namespace>/player_animations/*.json} (GeckoLib-exported keyframe JSON, auto-detected by
 * {@code LegacyGeckoJsonCodec}), keyed by the animation's own declared {@code name}, not its filename or path -
 * see {@link PlayerAnimationRegistry}. All this hook does is resolve that registry entry and play it on the
 * given player's own persistent {@link ModifierLayer}, discarding whatever it was already playing (a hard cut,
 * no fade) - matching {@code de.cas_ual_ty.spells.spell.action.animation.PlayAnimationAction}'s contract.
 */
public class PlayerAnimatorHooks
{
    private static final Logger LOGGER = LoggerFactory.getLogger(PlayerAnimatorHooks.class);

    // one persistent layer per player, added to their real AnimationStack exactly once and then reused -
    // stored via the player's own associated-data slot (see getOrCreateLayer) rather than a map we'd have to
    // clean up ourselves
    private static final ResourceLocation LAYER_ID = ResourceLocation.fromNamespaceAndPath(SpellsAndShields.MOD_ID, "play_animation");
    private static final int PRIORITY = 1000;

    // right-hand only, matching this action's "main hand only" contract - THIRD_PERSON_MODEL reuses the exact
    // same right_arm/right_item bone data as third person, rendered from a first-person-adjacent viewpoint,
    // rather than needing a wholly separate first-person animation format/track
    private static final FirstPersonConfiguration FIRST_PERSON_CONFIG = new FirstPersonConfiguration(true, false, true, false);

    public static void play(Level clientLevel, int entityId, ResourceLocation animationId)
    {
        Entity entity = clientLevel.getEntity(entityId);

        if(!(entity instanceof AbstractClientPlayer player))
        {
            return;
        }

        IPlayable playable = PlayerAnimationRegistry.getAnimation(animationId);

        if(playable == null)
        {
            LOGGER.warn("No Player Animator animation found for {} (not installed as an asset, or Player Animator itself couldn't parse it)", animationId);
            return;
        }

        IActualAnimation<?> actual = playable.playAnimation();
        actual.setFirstPersonMode(FirstPersonMode.THIRD_PERSON_MODEL);
        actual.setFirstPersonConfiguration(FIRST_PERSON_CONFIG);

        getOrCreateLayer(player).setAnimation(actual);
    }

    @SuppressWarnings("unchecked")
    private static ModifierLayer<IAnimation> getOrCreateLayer(AbstractClientPlayer player)
    {
        PlayerAnimationAccess.PlayerAssociatedAnimationData data = PlayerAnimationAccess.getPlayerAssociatedData(player);
        IAnimation existing = data.get(LAYER_ID);

        if(existing instanceof ModifierLayer<?> layer)
        {
            return (ModifierLayer<IAnimation>) layer;
        }

        ModifierLayer<IAnimation> layer = new ModifierLayer<>();
        data.set(LAYER_ID, layer);
        layer.addModifierBefore(new AimModifier(player));

        AnimationStack stack = PlayerAnimationAccess.getPlayerAnimLayer(player);
        stack.addAnimLayer(PRIORITY, layer);

        return layer;
    }

    /**
     * First-person-only aim follow, applied on top of whatever keyframed animation is currently playing on
     * this player's layer - without it the arm always thrusts in the exact fixed direction baked into the
     * keyframes, regardless of where the player is actually looking. Added once per player (see
     * {@link #getOrCreateLayer}), not per animation - it keeps working correctly across every future
     * {@link #play} call on the same layer since it just wraps whatever's currently beneath it.
     * <p>
     * Completely inert outside {@link FirstPersonMode#isFirstPersonPass()} - {@code THIRD_PERSON_MODEL} renders
     * using this exact same {@link ModifierLayer} for both the real third-person camera AND the up-close
     * first-person pass, so without this guard the look-direction bend was also happening in real third person.
     * Third person must render the animation exactly as authored, untouched.
     * <p>
     * {@code body}'s own keyframed rotation (whatever the JSON itself animates, eg. {@code test_stab.json}'s
     * torso twist) is suppressed entirely in first person, not just left un-adjusted - {@code body}'s rotation
     * is applied to the whole matrix stack before the arm renders (see PlayerAnimator's own
     * {@code PlayerRendererMixin#applyBodyTransforms}), so any body motion at all drags the arm along with it;
     * that's why the arm still looked glued to the body even after we stopped adding our own adjustment to it.
     * There's no body visible from this viewpoint to justify that drag.
     * <p>
     * {@code rightArm} itself only gets pitch/yaw bend, not touched for other parts. Yaw compensates for
     * {@code yBodyRot} (the body's facing) lagging behind the camera's actual look yaw, which vanilla only lets
     * catch up gradually - harmless in third person, but in first person the camera IS the exact look direction,
     * so that lag visibly dragged the arm with the body instead of the view. Pitch/yaw both use the interpolated
     * {@code getViewXRot}/{@code getViewYRot}/{@code getPreciseBodyRotation} accessors (this method's own
     * {@code tickDelta} param) rather than the raw per-tick fields - those only update once per game tick, so
     * reading them directly caused a visible flicker between ticks at render framerates above 20 fps.
     */
    private static class AimModifier extends AbstractModifier
    {
        private final AbstractClientPlayer player;

        private AimModifier(AbstractClientPlayer player)
        {
            this.player = player;
        }

        @Override
        public Vec3f get3DTransform(String modelName, TransformType type, float tickDelta, Vec3f value0)
        {
            if(!FirstPersonMode.isFirstPersonPass() || type != TransformType.ROTATION)
            {
                return super.get3DTransform(modelName, type, tickDelta, value0);
            }

            if("body".equals(modelName))
            {
                return value0;
            }

            if(!"rightArm".equals(modelName))
            {
                return super.get3DTransform(modelName, type, tickDelta, value0);
            }

            Vec3f transformed = super.get3DTransform(modelName, type, tickDelta, value0);
            float pitch = (float) Math.toRadians(player.getViewXRot(tickDelta) / 2F);
            float yaw = (float) Math.toRadians(Mth.wrapDegrees(player.getViewYRot(tickDelta) - player.getPreciseBodyRotation(tickDelta)));

            return transformed.add(new Vec3f(pitch, yaw, 0));
        }
    }
}
