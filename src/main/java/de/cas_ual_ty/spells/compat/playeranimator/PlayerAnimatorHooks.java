package de.cas_ual_ty.spells.compat.playeranimator;

import com.mojang.math.Axis;
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
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;
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
 * see {@link PlayerAnimationRegistry}. {@link #play} resolves the two registry entries
 * {@link de.cas_ual_ty.spells.spell.action.animation.PlayAnimationAction} sent over and plays them on the given
 * player's own persistent {@link ModifierLayer}, discarding whatever it was already playing (a hard cut, no
 * fade). {@link #onRenderPlayerPre}/{@link #onRenderPlayerPost} additionally make the whole player model follow
 * the camera's look direction, first-person only - see their own doc.
 */
@EventBusSubscriber(modid = SpellsAndShields.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
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

        getOrCreateLayer(player).setAnimation(new ViewRouter(thirdPerson, firstPerson));
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
     * Specifically whether the FIRST-PERSON side is still active - not the combined
     * {@code ModifierLayer.isActive()} (true as long as EITHER view's animation is still running). Only ever
     * called from the render hooks below, which only ever matter during the first-person pass - gating on the
     * combined OR would keep the whole-model rotation wrap applying even after the first-person animation
     * itself finished, just because the third-person one (a different length) happened to still be running.
     */
    private static boolean hasActiveFirstPersonAnimation(AbstractClientPlayer player)
    {
        IAnimation existing = PlayerAnimationAccess.getPlayerAssociatedData(player).get(LAYER_ID);
        return existing instanceof ModifierLayer<?> layer
                && layer.getAnimation() instanceof ViewRouter router
                && router.firstPerson != null
                && router.firstPerson.isActive();
    }

    /**
     * Makes the whole player model follow the camera's look direction in first person, rigidly, as one piece -
     * not per-bone (per-bone correction of {@code body}/{@code rightArm} through PlayerAnimator's own animation
     * system was tried repeatedly and always hit one of two problems: pitching {@code body} floats the hand
     * through a wide arc, since {@code body} pivots at the model's root/feet, far from the hand; pitching only
     * {@code rightArm} leaves {@code body} - and the camera's implied resting frame - out of sync with where the
     * camera actually points, so the correctly-bent arm still looks misplaced. Wrapping the whole render in one
     * rigid rotation sidesteps both).
     * <p>
     * Hooked at {@link RenderPlayerEvent.Pre}/{@link RenderPlayerEvent.Post} specifically because those fire
     * (confirmed in {@code PlayerRenderer.render}) strictly outside vanilla's own
     * {@code pushPose}/{@code setupRotations}/{@code popPose} sequence - Pre before all of it, Post after - so
     * our own push/pop pair cleanly wraps vanilla's own, with nothing left over to leak into whatever renders
     * next on the same shared PoseStack.
     * <p>
     * Gated on {@link FirstPersonMode#isFirstPersonPass()} (only meaningful during the up-close first-person
     * pass) and {@link #hasActiveFirstPersonAnimation} (only while this player's first-person animation is
     * actually still playing) - both checks must agree between Pre and Post or the push/pop pair goes
     * unbalanced; they're safe to re-evaluate independently since neither can change mid-render (the pass flag
     * only flips between separate render calls, and nothing else mutates the layer while this synchronous call
     * is on the stack).
     * <p>
     * The entity's own local origin (where a naive rotation would pivot around) sits at its feet, not somewhere
     * natural like the eyes - rotating straight there swings the head/chest through a wide arc instead of
     * tilting in place. Translating up to eye height before rotating (and back down after) fixes that - the
     * eye/camera is also just the conceptually correct pivot for a first-person view in the first place, the one
     * fixed point everything else swings around.
     * <p>
     * The rotation itself needs three terms, not two - this was the actual bug behind several earlier failed
     * attempts (wrong direction depending on facing, pitch bleeding into yaw at some angles). Vanilla's own
     * {@code setupRotations}, called AFTER this hook returns, still applies its own
     * {@code Axis.YP.rotationDegrees(180 - yBodyRot)} - and because {@code PoseStack} operations compose by
     * appending onto the right of whatever's already there, that later call ends up applied to the raw model
     * geometry FIRST (innermost), with everything from this hook wrapped OUTSIDE it. So a plain two-term
     * "yaw then pitch" here still leaves pitch rotating geometry that vanilla has already yawed by the real body
     * facing - the pitch axis silently depends on facing direction, exactly the reported symptom. The fix is to
     * explicitly cancel vanilla's upcoming rotation first (innermost among our own calls), apply a now-clean
     * pitch, then establish the true desired facing (outermost) - three rotations, not two.
     */
    @SubscribeEvent
    public static void onRenderPlayerPre(RenderPlayerEvent.Pre event)
    {
        if(!(event.getEntity() instanceof AbstractClientPlayer player) || !FirstPersonMode.isFirstPersonPass() || !hasActiveFirstPersonAnimation(player))
        {
            return;
        }

        float partialTick = event.getPartialTick();
        float eyeHeight = player.getEyeHeight();

        // cancels vanilla's own upcoming Axis.YP.rotationDegrees(180 - yBodyRot), applied to raw geometry first
        float undoVanillaYaw = (float) Math.toRadians(player.getPreciseBodyRotation(partialTick) - 180F);
        float pitch = (float) Math.toRadians(-player.getViewXRot(partialTick));
        // re-establishes the same "180 - X" facing convention vanilla uses, but with view yaw instead of body yaw
        float finalYaw = (float) Math.toRadians(180F - player.getViewYRot(partialTick));

        event.getPoseStack().pushPose();
        event.getPoseStack().translate(0, eyeHeight, 0);
        // call order matters: PoseStack appends new mulPose calls onto the right, so the LAST call here acts on
        // the (vanilla-rotated) geometry FIRST - undoVanillaYaw has to be last for that reason
        event.getPoseStack().mulPose(Axis.YP.rotation(finalYaw));
        event.getPoseStack().mulPose(Axis.XP.rotation(pitch));
        event.getPoseStack().mulPose(Axis.YP.rotation(undoVanillaYaw));
        event.getPoseStack().translate(0, -eyeHeight, 0);
    }

    @SubscribeEvent
    public static void onRenderPlayerPost(RenderPlayerEvent.Post event)
    {
        if(!(event.getEntity() instanceof AbstractClientPlayer player) || !FirstPersonMode.isFirstPersonPass() || !hasActiveFirstPersonAnimation(player))
        {
            return;
        }

        event.getPoseStack().popPose();
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
     */
    private static class ViewRouter implements IAnimation
    {
        @Nullable
        private final IActualAnimation<?> thirdPerson;
        @Nullable
        private final IActualAnimation<?> firstPerson;

        private ViewRouter(@Nullable IActualAnimation<?> thirdPerson, @Nullable IActualAnimation<?> firstPerson)
        {
            this.thirdPerson = thirdPerson;
            this.firstPerson = firstPerson;
        }

        @Nullable
        private IActualAnimation<?> active()
        {
            // checks the CANDIDATE side's own isActive(), not just which view we're in - the two animations can
            // have different lengths (see PlayAnimationAction/stab.json), so one finishing shouldn't leave its
            // own view stuck querying a frozen-at-rest animation object just because the OTHER view's animation
            // (a completely independent length) happens to still be running.
            IActualAnimation<?> candidate = FirstPersonMode.isFirstPersonPass() ? firstPerson : thirdPerson;
            return candidate != null && candidate.isActive() ? candidate : null;
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

            // head renders as a child of body's own transform, so any rotation this animation gives body would
            // otherwise carry the head along with it too. value0 already satisfies "vanilla's body pose +
            // value0 = correct look direction" as a baseline invariant (that's what value0 IS - vanilla's own
            // already-computed, already-correct head rotation) - so cancelling out only OUR OWN extra
            // contribution to body (subtracting it back out of head) is the minimal fix that preserves that
            // invariant.
            if(active == thirdPerson && type == TransformType.ROTATION && "head".equals(modelName))
            {
                Vec3f bodyTwist = thirdPerson.get3DTransform("body", TransformType.ROTATION, tickDelta, Vec3f.ZERO);
                transformed = transformed.add(bodyTwist);
            }

            return transformed;
        }

        // both gated on firstPerson.isActive(), not just firstPerson != null - getFirstPersonMode() on the
        // underlying animation is a static flag (set once via setFirstPersonMode in play(), never tied to
        // whether it's still actually playing), so without this check the "fake third person" 1p render trick
        // would keep firing even after firstPerson's own animation finished, for as long as the layer stays
        // alive - which it does as long as thirdPerson (a completely independent length) is still running.

        @Override
        public @NotNull FirstPersonMode getFirstPersonMode(float tickDelta)
        {
            return firstPerson != null && firstPerson.isActive() ? firstPerson.getFirstPersonMode(tickDelta) : FirstPersonMode.NONE;
        }

        @Override
        public @NotNull FirstPersonConfiguration getFirstPersonConfiguration(float tickDelta)
        {
            return firstPerson != null && firstPerson.isActive() ? firstPerson.getFirstPersonConfiguration(tickDelta) : IAnimation.super.getFirstPersonConfiguration(tickDelta);
        }
    }
}
