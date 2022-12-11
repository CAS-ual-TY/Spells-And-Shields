package de.cas_ual_ty.spells.requirement;

import com.mojang.serialization.Codec;
import de.cas_ual_ty.spells.SpellsRegistries;
import net.minecraft.network.FriendlyByteBuf;

import java.util.function.Function;

public class RequirementType<R extends Requirement>
{
    private Function<RequirementType<R>, R> constructor;
    private Codec<R> codec;
    
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
        buf.writeRegistryId(SpellsRegistries.REQUIREMENTS_REGISTRY.get(), requirement.getType());
        requirement.writeToBuf(buf);
    }
    
    public static Requirement readFromBuf(FriendlyByteBuf buf)
    {
        RequirementType<?> type = buf.readRegistryId();
        Requirement requirement = type.makeInstance();
        requirement.readFromBuf(buf);
        return requirement;
    }
    
    static <R extends Requirement> RequirementType<R> make(Function<RequirementType<R>, R> constructor, Function<RequirementType<R>, Codec<R>> codec)
    {
        return new RequirementType<R>(constructor, codec);
    }
}
