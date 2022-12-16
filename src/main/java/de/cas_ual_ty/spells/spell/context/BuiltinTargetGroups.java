package de.cas_ual_ty.spells.spell.context;

public enum BuiltinTargetGroups
{
    OWNER("owner"),
    PROJECTILE("projectile");
    
    public final String targetGroup;
    
    BuiltinTargetGroups(String targetGroup)
    {
        this.targetGroup = targetGroup;
    }
}
