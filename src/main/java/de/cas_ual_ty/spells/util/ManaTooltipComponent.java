package de.cas_ual_ty.spells.util;

import net.minecraft.util.Mth;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

public class ManaTooltipComponent implements TooltipComponent
{
    public final int mana;
    
    public ManaTooltipComponent(float mana)
    {
        this.mana = Mth.ceil(mana);
    }
}