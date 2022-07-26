package de.cas_ual_ty.spells.event;

import de.cas_ual_ty.spells.capability.ManaHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;

public class ManaRegValueEvent extends PlayerEvent
{
    protected final ManaHolder manaHolder;
    protected final double originalValue;
    
    protected double value;
    
    public ManaRegValueEvent(Player player, ManaHolder manaHolder, double value)
    {
        super(player);
        this.manaHolder = manaHolder;
        this.originalValue = value;
        this.value = value;
    }
    
    public ManaHolder getManaHolder()
    {
        return manaHolder;
    }
    
    public double getOriginalValue()
    {
        return originalValue;
    }
    
    public double getValue()
    {
        return value;
    }
    
    public void setValue(double value)
    {
        this.value = value;
    }
    
    public void modifyValue(ModifyFunction function)
    {
        this.value = function.apply(this.value);
    }
    
    @FunctionalInterface
    public interface ModifyFunction
    {
        double apply(double value);
    }
}
