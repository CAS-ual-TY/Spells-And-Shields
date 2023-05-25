package de.cas_ual_ty.spells.requirement;

import de.cas_ual_ty.spells.capability.SpellProgressionHolder;
import de.cas_ual_ty.spells.registers.RequirementTypes;
import de.cas_ual_ty.spells.util.SpellsDowngrade;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.ContainerLevelAccess;

public abstract class Requirement
{
    public static final MutableComponent EMPTY = SpellsDowngrade.empty();
    
    public final RequirementType<?> type;
    
    protected String descriptionId;
    
    public Requirement(RequirementType<?> type)
    {
        this.type = type;
        
        ResourceLocation rl = RequirementTypes.REGISTRY.get().getKey(type);
        this.descriptionId = "requirement." + rl.getNamespace() + "." + rl.getPath();
    }
    
    public RequirementType<?> getType()
    {
        return type;
    }
    
    public abstract boolean passes(SpellProgressionHolder spellProgressionHolder, ContainerLevelAccess access);
    
    public void onSpellLearned(SpellProgressionHolder spellProgressionHolder, ContainerLevelAccess access)
    {
    }
    
    public abstract MutableComponent makeDescription(SpellProgressionHolder spellProgressionHolder, ContainerLevelAccess access);
    
    public String getDescriptionId()
    {
        return descriptionId;
    }
    
    public abstract void writeToBuf(FriendlyByteBuf buf);
    
    public abstract void readFromBuf(FriendlyByteBuf buf);
}
