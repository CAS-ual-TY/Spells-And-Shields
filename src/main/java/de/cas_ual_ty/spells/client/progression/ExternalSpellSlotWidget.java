package de.cas_ual_ty.spells.client.progression;

import com.mojang.blaze3d.vertex.PoseStack;

import java.util.function.IntConsumer;

public class ExternalSpellSlotWidget extends SpellSlotWidget
{
    public ExternalSpellSlotWidget(int x, int y, int slot, IntConsumer onPress, OnTooltip tooltip)
    {
        super(x, y, slot, onPress, tooltip);
    }
    
    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float deltaTick)
    {
        super.render(poseStack, mouseX, mouseY, deltaTick);
        
        poseStack.pushPose();
        poseStack.translate(0, 0, 95);
        renderToolTip(poseStack, mouseX, mouseY);
        poseStack.popPose();
    }
}
