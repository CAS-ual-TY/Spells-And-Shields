package de.cas_ual_ty.spells.requirement;

import com.google.gson.JsonObject;
import de.cas_ual_ty.spells.SpellsRegistries;
import de.cas_ual_ty.spells.capability.SpellProgressionHolder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.inventory.ContainerLevelAccess;

public abstract class Requirement
{
    public final IRequirementType<?> type;
    
    protected String descriptionId;
    
    public Requirement(IRequirementType<?> type)
    {
        this.type = type;
        this.descriptionId = SpellsRegistries.REQUIREMENTS_REGISTRY.get().getKey(type).toString();
    }
    
    public IRequirementType<?> getType()
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