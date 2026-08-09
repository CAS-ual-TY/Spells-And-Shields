package de.cas_ual_ty.spells.client.animation;

import de.cas_ual_ty.spells.animation.AnimationSample;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.client.event.RenderHandEvent;

/**
 * First-person counterpart to {@link de.cas_ual_ty.spells.mixin.PlayerModelMixin}. {@link RenderHandEvent} only
 * ever fires for the local client's own view (there's no "whose hand" to resolve - it's always
 * {@link Minecraft#player}), once per hand, so this samples straight off that.
 * <p>
 * {@code ItemInHandRenderer.renderHandsWithItems} fires this event for main hand, then off hand, both on the
 * very same {@code PoseStack} instance, with no push/pop of its own around the event - and the vanilla draw
 * that follows a non-cancelled event (eg. {@code renderArmWithItem}) pushes only after we already return, so
 * its own pop can never undo what we added here. Left as a bare {@code translate}/{@code mulPose}/{@code scale},
 * our main-hand transform would still be sitting on the stack when the off-hand call starts, visibly animating
 * whatever's in the off hand too. Since there's no event fired "after this hand finishes drawing" to pop
 * against, this pops whatever it itself pushed on the previous call before doing anything else, then pushes
 * fresh only if the current hand actually has a sample - which also means both hands can be animated
 * independently and simultaneously without leaking into each other.
 */
public class PlayerAnimationRenderHooks
{
    private static boolean pushedLastCall = false;

    public static void renderHand(RenderHandEvent event)
    {
        if(pushedLastCall)
        {
            event.getPoseStack().popPose();
            pushedLastCall = false;
        }

        Player player = Minecraft.getInstance().player;

        if(player == null)
        {
            return;
        }

        String part = event.getHand() == InteractionHand.MAIN_HAND ? "main_hand" : "off_hand";
        AnimationSample sample = PlayerAnimationClient.sampleFirstPerson(player.getId(), part, event.getPartialTick());

        if(sample == null)
        {
            return;
        }

        event.getPoseStack().pushPose();
        pushedLastCall = true;

        event.getPoseStack().translate(sample.translate().x(), sample.translate().y(), sample.translate().z());
        // applied straight as a quaternion, deliberately never round-tripped through Euler angles - see
        // AnimationSample's own doc for why that avoids a visible wobble/roll artifact
        event.getPoseStack().mulPose(sample.rotate());
        event.getPoseStack().scale((float) sample.scale().x(), (float) sample.scale().y(), (float) sample.scale().z());
    }
}
