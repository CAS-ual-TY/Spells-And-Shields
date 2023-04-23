package de.cas_ual_ty.spells.spell.context;

public enum BuiltinTargetGroups
{
    OWNER("owner"),
    PROJECTILE("projectile"),
    ENTITY_HIT("entity_hit"),
    BLOCK_HIT("block_hit"),
    HIT_POSITION("hit_position"),
    HOLDER("holder");
    
    public final String targetGroup;
    
    BuiltinTargetGroups(String targetGroup)
    {
        this.targetGroup = targetGroup;
    }
    
    @Override
    public String toString()
    {
        return targetGroup;
    }
}
