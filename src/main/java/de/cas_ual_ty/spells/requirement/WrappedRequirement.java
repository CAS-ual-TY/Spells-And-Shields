package de.cas_ual_ty.spells.requirement;

import com.google.gson.JsonObject;
import de.cas_ual_ty.spells.SpellsRegistries;
import de.cas_ual_ty.spells.capability.SpellProgressionHolder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.inventory.ContainerLevelAccess;

public class WrappedRequirement extends Requirement
{
    protected Requirement requirement;
    protected RequirementStatus status;
    protected MutableComponent component;
    
    public WrappedRequirement(IRequirementType<?> type)
    {
        super(type);
    }
    
    public WrappedRequirement(IRequirementType<?> type, Requirement requirement)
    {
        super(type);
        this.requirement = requirement;
        this.status = RequirementStatus.UNDECIDED;
        this.component = null;
    }
    
    @Override
    public boolean passes(SpellProgressionHolder spellProgressionHolder, ContainerLevelAccess access)
    {
        return status.isDecided() ? status.passes : requirement.passes(spellProgressionHolder, access);
    }
    
    public void decide(SpellProgressionHolder spellProgressionHolder, ContainerLevelAccess access)
    {
        this.status = RequirementStatus.decide(passes(spellProgressionHolder, access));
        this.component = makeDescription(spellProgressionHolder, access);
        
        if(status.isDecided())
        {
            component = component.withStyle(status.passes ? ChatFormatting.GREEN : ChatFormatting.RED);
        }
    }
    
    @Override
    public MutableComponent makeDescription(SpellProgressionHolder spellProgressionHolder, ContainerLevelAccess access)
    {
        return this.component != null ? this.component : requirement.makeDescription(spellProgressionHolder, access);
    }
    
    @Override
    public void writeToJson(JsonObject json)
    {
        throw new IllegalStateException();
    }
    
    @Override
    public void readFromJson(JsonObject json)
    {
        throw new IllegalStateException();
    }
    
    @Override
    public void writeToBuf(FriendlyByteBuf buf)
    {
        //IRequirementType.writeToBuf(buf, requirement);
        buf.writeByte(status.ordinal());
        buf.writeComponent(component);
    }
    
    @Override
    public void readFromBuf(FriendlyByteBuf buf)
    {
        //this.requirement =  IRequirementType.readFromBuf(buf);
        this.status = RequirementStatus.values()[buf.readByte()];
        this.component = (MutableComponent) buf.readComponent();
    }
    
    public static WrappedRequirement wrap(Requirement requirement, SpellProgressionHolder spellProgressionHolder, ContainerLevelAccess access)
    {
        WrappedRequirement w = new WrappedRequirement(SpellsRegistries.WRAPPED_REQUIREMENT.get(), requirement);
        w.decide(spellProgressionHolder, access);
        return w;
    }
}
