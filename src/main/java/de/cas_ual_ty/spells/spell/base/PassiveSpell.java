package de.cas_ual_ty.spells.spell.base;

import de.cas_ual_ty.spells.capability.ManaHolder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;

import javax.annotation.Nullable;
import java.util.List;

public class PassiveSpell extends BaseSpell
{
    public static final String KEY_WHEN_APPLIED = "spell.passive.when_applied";
    
    @Override
    public boolean activate(ManaHolder manaHolder)
    {
        return false;
    }
    
    @Override
    public List<Component> getTooltip(@Nullable Component keyBindTooltip)
    {
        return super.getTooltip(null);
    }
    
    public static MutableComponent whenAppliedComponent()
    {
        return new TranslatableComponent(KEY_WHEN_APPLIED).withStyle(ChatFormatting.DARK_PURPLE);
    }
}
