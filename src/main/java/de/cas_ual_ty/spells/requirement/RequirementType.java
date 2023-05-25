package de.cas_ual_ty.spells.requirement;

import com.mojang.serialization.Codec;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.registries.ForgeRegistryEntry;

import java.util.function.Function;

public class RequirementType<R extends Requirement> extends ForgeRegistryEntry<RequirementType<?>>
{
    private Function<RequirementType<R>, R> constructor;
    private Codec<R> codec;
    
    // 1.19.2 -> 1.18.2 downgrade
    public RequirementType()
    {
    
    }
    
    public RequirementType(Function<RequirementType<R>, R> constructor, Function<RequirementType<R>, Codec<R>> codec)
    {
        this.constructor = constructor;
        this.codec = codec.apply(this);
    }
    
    public Codec<R> getCodec()
    {
        return codec;
    }
    
    public R makeInstance()
    {
        return constructor.apply(this);
    }
    
    public static void writeToBuf(FriendlyByteBuf buf, Requirement requirement)
    {
        buf.writeRegistryId(requirement.getType());
        requirement.writeToBuf(buf);
    }
    
    public static Requirement readFromBuf(FriendlyByteBuf buf)
    {
        RequirementType<?> type = buf.readRegistryId();
        Requirement requirement = type.makeInstance();
        requirement.readFromBuf(buf);
        return requirement;
    }
}
