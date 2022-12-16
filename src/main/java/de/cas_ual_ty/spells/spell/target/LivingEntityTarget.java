package de.cas_ual_ty.spells.spell.target;

import net.minecraft.world.entity.LivingEntity;

public class LivingEntityTarget extends EntityTarget
{
    protected LivingEntity livingEntity;
    
    public LivingEntityTarget(ITargetType<?> type, LivingEntity livingEntity)
    {
        super(type, livingEntity);
        this.livingEntity = livingEntity;
    }
    
    public LivingEntity getLivingEntity()
    {
        return livingEntity;
    }
}
