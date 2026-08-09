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
 */
public class PlayerAnimationRenderHooks
{
    public static void renderHand(RenderHandEvent event)
    {
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

        event.getPoseStack().translate(sample.translate().x(), sample.translate().y(), sample.translate().z());
        // applied straight as a quaternion, deliberately never round-tripped through Euler angles - see
        // AnimationSample's own doc for why that avoids a visible wobble/roll artifact
        event.getPoseStack().mulPose(sample.rotate());
        event.getPoseStack().scale((float) sample.scale().x(), (float) sample.scale().y(), (float) sample.scale().z());
    }
}
