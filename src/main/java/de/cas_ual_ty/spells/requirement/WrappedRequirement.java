package de.cas_ual_ty.spells.requirement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.PrimitiveCodec;
import de.cas_ual_ty.spells.capability.SpellProgressionHolder;
import de.cas_ual_ty.spells.registers.RequirementTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.ContainerLevelAccess;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

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
    protected List<Component> cachedTooltip;
    
    public WrappedRequirement(RequirementType<?> type)
    {
        super(type);
    }
    
    protected WrappedRequirement(RequirementType<?> type, Requirement requirement, RequirementStatus status, List<Component> cachedTooltip)
    {
        this(type);
        this.requirement = requirement;
        this.status = status;
        this.cachedTooltip = cachedTooltip;
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
    
    public List<Component> getCachedTooltip()
    {
        return cachedTooltip;
    }
    
    @Override
    protected boolean doesPlayerPass(SpellProgressionHolder spellProgressionHolder, ContainerLevelAccess access)
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
        status = RequirementStatus.decide(passes(spellProgressionHolder, access));
        cachedTooltip = new LinkedList<>();
        requirement.makeDescription(cachedTooltip, spellProgressionHolder, access);
    }
    
    @Override
    public void makeDescription(List<Component> tooltip, SpellProgressionHolder spellProgressionHolder, ContainerLevelAccess access)
    {
        if(cachedTooltip != null)
        {
            tooltip.addAll(cachedTooltip);
        }
    }
    
    @Override
    public void writeToBuf(FriendlyByteBuf buf)
    {
        buf.writeByte(status.ordinal());
        buf.writeInt(cachedTooltip.size());
        cachedTooltip.forEach(buf::writeComponent);
    }
    
    @Override
    public void readFromBuf(FriendlyByteBuf buf)
    {
        status = RequirementStatus.values()[buf.readByte()];
        int size = buf.readInt();
        cachedTooltip = new ArrayList<>(size);
        for(int i = 0; i < size; i++)
        {
            cachedTooltip.add(buf.readComponent());
        }
    }
    
    public static WrappedRequirement wrap(Requirement requirement, SpellProgressionHolder spellProgressionHolder, ContainerLevelAccess access, boolean hidden)
    {
        WrappedRequirement w = new WrappedRequirement(RequirementTypes.WRAPPED.get(), requirement);
        w.decide(spellProgressionHolder, access, hidden);
        return w;
    }
}