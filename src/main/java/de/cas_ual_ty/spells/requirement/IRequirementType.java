package de.cas_ual_ty.spells.requirement;

import com.google.gson.JsonObject;
import de.cas_ual_ty.spells.SpellsRegistries;
import de.cas_ual_ty.spells.util.SpellsFileUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistryEntry;
import net.minecraftforge.registries.IForgeRegistryEntry;

import javax.annotation.Nullable;
import java.util.function.Function;

public interface IRequirementType extends IForgeRegistryEntry<IRequirementType.RequirementType>
{
    Requirement create(IRequirementType type);
    
    default Requirement makeInstance()
    {
        return create(this);
    }
    
    static JsonObject writeToJson(Requirement requirement)
    {
        JsonObject json = new JsonObject();
        json.addProperty("type", requirement.getType().getRegistryName().toString());
        requirement.writeToJson(json);
        return json;
    }
    
    @Nullable
    static Requirement readFromJson(JsonObject json)
    {
        String id = SpellsFileUtil.jsonString(json, "type");
        IRequirementType type = SpellsRegistries.REQUIREMENTS_REGISTRY.get().getValue(new ResourceLocation(id));
        
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
        buf.writeRegistryId(requirement.getType());
        requirement.writeToBuf(buf);
    }
    
    static Requirement readFromBuf(FriendlyByteBuf buf)
    {
        IRequirementType type = buf.readRegistryId();
        Requirement requirement = type.makeInstance();
        requirement.readFromBuf(buf);
        return requirement;
    }
    
    // this class is needed because in 1.18 any registry object must also extend/implement IForgeRegistryEntry
    // this means that IRequirementType can not be a functional interface anymore
    // and thus this helper class is needed
    class RequirementType extends ForgeRegistryEntry<RequirementType> implements IRequirementType
    {
        public final Function<RequirementType, Requirement> factory;
        
        public RequirementType(Function<RequirementType, Requirement> factory)
        {
            this.factory = factory;
        }
        
        @Override
        public Requirement create(IRequirementType type)
        {
            return factory.apply((RequirementType) type);
        }
    }
}