package de.cas_ual_ty.spells.requirement;

import com.google.gson.JsonObject;
import de.cas_ual_ty.spells.SpellsRegistries;
import de.cas_ual_ty.spells.util.SpellsFileUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;

public interface IRequirementType<R extends Requirement>
{
    R create(IRequirementType<R> type);
    
    default R makeInstance()
    {
        return create(this);
    }
    
    static JsonObject writeToJson(Requirement requirement)
    {
        JsonObject json = new JsonObject();
        
        json.addProperty("type", SpellsRegistries.REQUIREMENTS_REGISTRY.get().getKey(requirement.getType()).toString());
        requirement.writeToJson(json);
        
        return json;
    }
    
    @Nullable
    static Requirement readFromJson(JsonObject json)
    {
        String id = SpellsFileUtil.jsonString(json, "type");
        IRequirementType<?> type = SpellsRegistries.REQUIREMENTS_REGISTRY.get().getValue(new ResourceLocation(id));
        
        if(type == null)
        {
            return null;
        }
        
        Requirement requirement = type.makeInstance();
        requirement.readFromJson(json);
        
        return requirement;
    }
    
    static void writeToBuf(FriendlyByteBuf buf, Requirement requirement)
    {
        buf.writeRegistryId(SpellsRegistries.REQUIREMENTS_REGISTRY.get(), requirement.getType());
        requirement.writeToBuf(buf);
    }
    
    static Requirement readFromBuf(FriendlyByteBuf buf)
    {
        IRequirementType<?> type = buf.readRegistryId();
        Requirement requirement = type.makeInstance();
        requirement.readFromBuf(buf);
        return requirement;
    }
}
