package de.cas_ual_ty.spells.requirement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.PrimitiveCodec;
import de.cas_ual_ty.spells.capability.SpellProgressionHolder;
import de.cas_ual_ty.spells.registers.RequirementTypes;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.inventory.ContainerLevelAccess;

public class WrappedRequirement extends Requirement
{
    public static final Codec<WrappedRequirement> CODEC = new PrimitiveCodec<>()
    {
        @Override
        public <T> DataResult<WrappedRequirement> read(DynamicOps<T> ops, T input)
        {
            return DataResult.error("Can not (de)serialize a wrapped requirement");
        }
        
        @Override
        public <T> T write(DynamicOps<T> ops, WrappedRequirement value)
        {
            return ops.empty();
        }
    };
    
    protected Requirement requirement;
    protected RequirementStatus status;
    protected MutableComponent component;
    
    public WrappedRequirement(RequirementType<?> type)
    {
        super(type);
    }
    
    protected WrappedRequirement(RequirementType<?> type, Requirement requirement, RequirementStatus status, MutableComponent component)
    {
        this(type);
        this.requirement = requirement;
        this.status = status;
        this.component = component;
    }
    
    public WrappedRequirement(RequirementType<?> type, Requirement requirement)
    {
        this(type, requirement, RequirementStatus.UNDECIDED, null);
    }
    
    public Requirement getRequirement()
    {
        return requirement;
    }
    
    public RequirementStatus getStatus()
    {
        return status;
    }
    
    public MutableComponent getComponent()
    {
        return component;
    }
    
    @Override
    public boolean passes(SpellProgressionHolder spellProgressionHolder, ContainerLevelAccess access)
    {
        return status.isDecided() ? status.passes : requirement.passes(spellProgressionHolder, access);
    }
    
    @Override
    public void onSpellLearned(SpellProgressionHolder spellProgressionHolder, ContainerLevelAccess access)
    {
        requirement.onSpellLearned(spellProgressionHolder, access);
    }
    
    public void decide(SpellProgressionHolder spellProgressionHolder, ContainerLevelAccess access, boolean hidden)
    {
        this.status = RequirementStatus.decide(passes(spellProgressionHolder, access));
        this.component = Component.literal("- ").append(makeDescription(spellProgressionHolder, access).withStyle(hidden ? ChatFormatting.DARK_GRAY : (status.passes ? ChatFormatting.GREEN : ChatFormatting.RED)));
    }
    
    @Override
    public MutableComponent makeDescription(SpellProgressionHolder spellProgressionHolder, ContainerLevelAccess access)
    {
        return this.component != null ? this.component : requirement.makeDescription(spellProgressionHolder, access);
    }
    
    @Override
    public void writeToBuf(FriendlyByteBuf buf)
    {
        buf.writeByte(status.ordinal());
        buf.writeComponent(component);
    }
    
    @Override
    public void readFromBuf(FriendlyByteBuf buf)
    {
        this.status = RequirementStatus.values()[buf.readByte()];
        this.component = (MutableComponent) buf.readComponent();
    }
    
    public static WrappedRequirement wrap(Requirement requirement, SpellProgressionHolder spellProgressionHolder, ContainerLevelAccess access, boolean hidden)
    {
        WrappedRequirement w = new WrappedRequirement(RequirementTypes.WRAPPED.get(), requirement);
        w.decide(spellProgressionHolder, access, hidden);
        return w;
    }
}
