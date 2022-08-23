package de.cas_ual_ty.spells.requirement;

public enum RequirementStatus
{
    PASSES(true),
    FAILING(false),
    UNDECIDED(false);
    
    public final boolean passes;
    
    RequirementStatus(boolean passes)
    {
        this.passes = passes;
    }
    
    public boolean isDecided()
    {
        return this != UNDECIDED;
    }
    
    public static RequirementStatus decide(boolean passes)
    {
        return passes ? PASSES : FAILING;
    }
}