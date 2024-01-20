package de.cas_ual_ty.spells.requirement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.capability.SpellProgressionHolder;
import de.cas_ual_ty.spells.util.SpellsCodecs;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.inventory.ContainerLevelAccess;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class MaxRequirement extends Requirement
{
    public static Codec<MaxRequirement> makeCodec(RequirementType<MaxRequirement> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                ExtraCodecs.lazyInitializedCodec(() -> SpellsCodecs.REQUIREMENT.listOf()).fieldOf("requirements").forGetter(MaxRequirement::getList),
                Codec.intRange(0, Integer.MAX_VALUE).fieldOf("maximum").forGetter(MaxRequirement::getMaximum)
        ).apply(instance, (list, maximum) -> new MaxRequirement(type, list, maximum)));
    }
    
    public static final String NONE_SUFFIX = ".none";
    public static final String NOT_SUFFIX = ".not";
    
    protected List<Requirement> list;
    protected int maximum;
    
    public MaxRequirement(RequirementType<?> type)
    {
        super(type);
    }
    
    public MaxRequirement(RequirementType<?> type, List<Requirement> list, int maximum)
    {
        this(type);
        this.list = list;
        this.maximum = maximum;
    }
    
    public List<Requirement> getList()
    {
        return list;
    }
    
    public int getMaximum()
    {
        return maximum;
    }
    
    @Override
    protected boolean doesPlayerPass(SpellProgressionHolder spellProgressionHolder, ContainerLevelAccess access)
    {
        return getPassedAmount(spellProgressionHolder, access) <= maximum;
    }
    
    @Override
    public void makeDescription(List<Component> tooltip, SpellProgressionHolder spellProgressionHolder, ContainerLevelAccess access)
    {
        int amount = getPassedAmount(spellProgressionHolder, access);
        
        if(maximum == 0)
        {
            if(list.size() == 1)
            {
                tooltip.add(formatComponent(spellProgressionHolder, access, Component.translatable(getDescriptionId() + NOT_SUFFIX)));
            }
            else
            {
                tooltip.add(formatComponent(spellProgressionHolder, access, Component.translatable(getDescriptionId() + NONE_SUFFIX)));
            }
        }
        else
        {
            tooltip.add(formatComponent(spellProgressionHolder, access, Component.translatable(getDescriptionId(), amount, maximum)));
        }
        
        List<Component> subTooltip = new LinkedList<>();
        list.forEach(r -> r.makeDescription(subTooltip, spellProgressionHolder, access));
        subTooltip.stream().map(c -> Component.literal("  ").append(c)).forEach(tooltip::add);
    }
    
    protected int getPassedAmount(SpellProgressionHolder spellProgressionHolder, ContainerLevelAccess access)
    {
        return (int) list.stream().filter(r -> r.doesPlayerPass(spellProgressionHolder, access)).count();
    }
    
    @Override
    public void writeToBuf(FriendlyByteBuf buf)
    {
        buf.writeInt(list.size());
        list.forEach(r -> RequirementType.writeToBuf(buf, r));
        
        buf.writeInt(maximum);
    }
    
    @Override
    public void readFromBuf(FriendlyByteBuf buf)
    {
        int size = buf.readInt();
        list = new ArrayList<>(size);
        for(int i = 0; i < size; i++)
        {
            list.add(RequirementType.readFromBuf(buf));
        }
        
        maximum = buf.readInt();
    }
}
