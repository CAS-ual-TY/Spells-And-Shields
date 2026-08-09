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
import dev.kosmx.playerAnim.core.util.Vec3f;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationAccess;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationRegistry;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Only ever touched behind {@link de.cas_ual_ty.spells.compat.ModCompat#PLAYER_ANIMATOR} - see that class' doc.
 * <p>
 * Animations themselves aren't ours to define - PlayerAnimator loads them client-side from
 * {@code assets/<namespace>/player_animations/*.json} (GeckoLib-exported keyframe JSON, auto-detected by
 * {@code LegacyGeckoJsonCodec}), keyed by the animation's own declared {@code name}, not its filename or path -
 * see {@link PlayerAnimationRegistry}. All this hook does is resolve the two registry entries
 * {@link de.cas_ual_ty.spells.spell.action.animation.PlayAnimationAction} sent over and play them on the given
 * player's own persistent {@link ModifierLayer}, discarding whatever it was already playing (a hard cut, no
 * fade).
 */
public class PlayerAnimatorHooks
{
    private static final Logger LOGGER = LoggerFactory.getLogger(PlayerAnimatorHooks.class);

    // one persistent layer per player, added to their real AnimationStack exactly once and then reused -
    // stored via the player's own associated-data slot (see getOrCreateLayer) rather than a map we'd have to
    // clean up ourselves
    private static final ResourceLocation LAYER_ID = ResourceLocation.fromNamespaceAndPath(SpellsAndShields.MOD_ID, "play_animation");
    private static final int PRIORITY = 1000;

    public static void play(Level clientLevel, int entityId, ResourceLocation thirdPersonAnimationId, ResourceLocation firstPersonAnimationId)
    {
        Entity entity = clientLevel.getEntity(entityId);

        if(!(entity instanceof AbstractClientPlayer player))
        {
            return;
        }

        IActualAnimation<?> thirdPerson = resolve(thirdPersonAnimationId);
        IActualAnimation<?> firstPerson = resolve(firstPersonAnimationId);

        if(thirdPerson == null && firstPerson == null)
        {
            return;
        }

        if(firstPerson != null)
        {
            // THIRD_PERSON_MODEL, not VANILLA - VANILLA reuses vanilla's own camera-locked bare-arm render, which
            // sounds ideal, but vanilla's PlayerRenderer.renderHand() forcibly resets rightArm.xRot to 0 right
            // after setupAnim runs, discarding almost any real swing (that's the axis this animation's swing
            // lives on) before it ever renders - no way around it, it's vanilla's own code. THIRD_PERSON_MODEL
            // instead renders the real third-person model from a camera parked near the head - not truly
            // camera-locked like vanilla's own hand, but the full rotation actually survives.
            firstPerson.setFirstPersonMode(FirstPersonMode.THIRD_PERSON_MODEL);
            // right-hand only, matching this action's "main hand only" contract. Also required for the arm to
            // render AT ALL under THIRD_PERSON_MODEL: PlayerRendererMixin#hideBonesInFirstPerson hides every
            // model part in first person, then selectively re-shows rightArm/leftArm based on this - its default
            // (showRightArm=false) hides the arm outright, which is why it was invisible without this.
            firstPerson.setFirstPersonConfiguration(new FirstPersonConfiguration(true, false, true, false));
        }

        getOrCreateLayer(player).setAnimation(new ViewRouter(thirdPerson, firstPerson, player));
    }

    @Nullable
    private static IActualAnimation<?> resolve(@Nullable ResourceLocation animationId)
    {
        if(animationId == null)
        {
            return null;
        }

        IPlayable playable = PlayerAnimationRegistry.getAnimation(animationId);

        if(playable == null)
        {
            LOGGER.warn("No Player Animator animation found for {} (not installed as an asset, or Player Animator itself couldn't parse it)", animationId);
            return null;
        }

        return playable.playAnimation();
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

        AnimationStack stack = PlayerAnimationAccess.getPlayerAnimLayer(player);
        stack.addAnimLayer(PRIORITY, layer);

        return layer;
    }

    /**
     * Plays two independently-authored animations at once and routes every query to whichever one actually
     * matches the current render - {@link FirstPersonMode#isFirstPersonPass()} true means the up-close first
     * person pass, so {@code firstPerson} answers; otherwise (real third person, or any other pass) {@code
     * thirdPerson} does. Either side can be {@code null} if that animation id didn't resolve - the missing side
     * then just contributes nothing for whichever view it would have owned, rather than the whole thing failing.
     * <p>
     * {@code tick()}/{@code setupAnim()} run on both regardless of view, since those track wall-clock animation
     * progress (once per game tick / once per render), not view-specific data - only {@code get3DTransform} (the
     * actual per-bone pose query, asked many times per frame with the view already implied by
     * {@code isFirstPersonPass()}) is routed.
     * <p>
     * {@code getFirstPersonMode}/{@code getFirstPersonConfiguration} always answer from {@code firstPerson}
     * specifically (regardless of current pass) - those two are how {@code AnimationStack} decides whether this
     * layer even wants to participate in first-person rendering at all, they're not per-frame pose queries.
     * <p>
     * {@code body}'s rotation additionally gets a live pitch/yaw bend on top, first-person only, so it (and
     * everything rendered as its child, including {@code rightArm}) follows where the player is actually
     * looking instead of staying at {@code body}'s own facing. Only {@code body} gets this correction, not
     * {@code rightArm} too - the arm renders nested inside body's own {@code PoseStack} rotation (see
     * {@code PlayerRendererMixin#applyBodyTransforms}), so it inherits the correction for free; adding it a
     * second time to the arm's own local rotation would double-count the same term. Yaw compensates for
     * {@code yBodyRot} (the body's facing) lagging behind the camera's actual look yaw, which vanilla only
     * lets catch up gradually - in first person the camera IS the exact look direction, so uncompensated
     * everything visibly dragged with the body instead of the view. Both use the interpolated
     * {@code getViewXRot}/{@code getViewYRot}/{@code getPreciseBodyRotation} accessors (this method's own
     * {@code tickDelta} param) rather than the raw per-tick fields, which only update once per game tick and so
     * flicker at render framerates above 20 fps.
     */
    private static class ViewRouter implements IAnimation
    {
        @Nullable
        private final IActualAnimation<?> thirdPerson;
        @Nullable
        private final IActualAnimation<?> firstPerson;
        private final AbstractClientPlayer player;

        private ViewRouter(@Nullable IActualAnimation<?> thirdPerson, @Nullable IActualAnimation<?> firstPerson, AbstractClientPlayer player)
        {
            this.thirdPerson = thirdPerson;
            this.firstPerson = firstPerson;
            this.player = player;
        }

        @Nullable
        private IActualAnimation<?> active()
        {
            return FirstPersonMode.isFirstPersonPass() ? firstPerson : thirdPerson;
        }

        @Override
        public boolean isActive()
        {
            return (thirdPerson != null && thirdPerson.isActive()) || (firstPerson != null && firstPerson.isActive());
        }

        @Override
        public void tick()
        {
            if(thirdPerson != null) { thirdPerson.tick(); }
            if(firstPerson != null) { firstPerson.tick(); }
        }

        @Override
        public void setupAnim(float tickDelta)
        {
            if(thirdPerson != null) { thirdPerson.setupAnim(tickDelta); }
            if(firstPerson != null) { firstPerson.setupAnim(tickDelta); }
        }

        @Override
        public @NotNull Vec3f get3DTransform(String modelName, TransformType type, float tickDelta, Vec3f value0)
        {
            IActualAnimation<?> active = active();

            if(active == null)
            {
                return value0;
            }

            Vec3f transformed = active.get3DTransform(modelName, type, tickDelta, value0);

            // body only, NOT rightArm too - rightArm renders as a child of body's own PoseStack rotation
            // (see PlayerRendererMixin#applyBodyTransforms, applied before the arm renders), so correcting
            // both double-counts the same view-yaw term: once baked into the shared stack via body, once more
            // in the arm's own local rotation. Correcting body alone is enough - the arm inherits it through
            // the hierarchy for free, with its own keyframed swing still layering on top correctly.
            if(active == firstPerson && type == TransformType.ROTATION && "body".equals(modelName))
            {
                // yaw is negated compared to the arm's old per-bone version: body's rotation is applied via
                // PlayerRendererMixin#applyBodyTransforms as Axis.YP.rotation(vec3f.y) on top of vanilla's own
                // baseline Axis.YP.rotationDegrees(180 - yBodyRot) - substituting "viewYaw" for "yBodyRot" in
                // that same vanilla formula and solving for the needed delta gives (bodyFacing - viewYaw), the
                // opposite sign from a direct "viewYaw - bodyFacing" - that direct form worked for the arm
                // because ModelPart.setRotation composes as a plain local rotation with no such 180-flipped
                // baseline to account for.
                float pitch = (float) Math.toRadians(player.getViewXRot(tickDelta) / 2F);
                float yaw = (float) Math.toRadians(Mth.wrapDegrees(player.getPreciseBodyRotation(tickDelta) - player.getViewYRot(tickDelta)));
                transformed = transformed.add(new Vec3f(pitch, yaw, 0));
            }

            // head renders as a child of body's own transform, so any rotation this animation gives body would
            // otherwise carry the head along with it too. value0 already satisfies "vanilla's body pose +
            // value0 = correct look direction" as a baseline invariant (that's what value0 IS - vanilla's own
            // already-computed, already-correct head rotation) - so cancelling out only OUR OWN extra
            // contribution to body (subtracting it back out of head) is the minimal fix that preserves that
            // invariant. Rebuilding the whole yaw from getViewYRot/getPreciseBodyRotation from scratch (a
            // previous attempt) double-counted compensation vanilla already applies on its own, overshooting.
            if(active == thirdPerson && type == TransformType.ROTATION && "head".equals(modelName))
            {
                Vec3f bodyTwist = thirdPerson.get3DTransform("body", TransformType.ROTATION, tickDelta, Vec3f.ZERO);
                transformed = transformed.add(bodyTwist);
            }

            return transformed;
        }

        @Override
        public @NotNull FirstPersonMode getFirstPersonMode(float tickDelta)
        {
            return firstPerson != null ? firstPerson.getFirstPersonMode(tickDelta) : FirstPersonMode.NONE;
        }

        @Override
        public @NotNull FirstPersonConfiguration getFirstPersonConfiguration(float tickDelta)
        {
            return firstPerson != null ? firstPerson.getFirstPersonConfiguration(tickDelta) : IAnimation.super.getFirstPersonConfiguration(tickDelta);
        }
    }
}
