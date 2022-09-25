package de.cas_ual_ty.spells.requirement;

import com.google.gson.JsonObject;
import de.cas_ual_ty.spells.SpellsRegistries;
import de.cas_ual_ty.spells.capability.SpellProgressionHolder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.ContainerLevelAccess;

public abstract class Requirement
{
    public final IRequirementType.RequirementType type;
    
    protected String descriptionId;
    
    public Requirement(IRequirementType.RequirementType type)
    {
        this.type = type;
        ResourceLocation rl = SpellsRegistries.REQUIREMENTS_REGISTRY.get().getKey(type);
        this.descriptionId = "requirement." + rl.getNamespace() + "." + rl.getPath();
    }
    
    public IRequirementType.RequirementType getType()
    {
        return type;
    }
    
    public abstract boolean passes(SpellProgressionHolder spellProgressionHolder, ContainerLevelAccess access);
    
    public abstract MutableComponent makeDescription(SpellProgressionHolder spellProgressionHolder, ContainerLevelAccess access);
    
    public String getDescriptionId()
    {
        return descriptionId;
    }
    
    public abstract void writeToJson(JsonObject json);
    
    public abstract void readFromJson(JsonObject json);
    
    public abstract void writeToBuf(FriendlyByteBuf buf);
    
    public abstract void readFromBuf(FriendlyByteBuf buf);
}