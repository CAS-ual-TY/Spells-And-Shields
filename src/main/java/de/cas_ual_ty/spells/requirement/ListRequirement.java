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

public class ListRequirement extends Requirement
{
    public static Codec<ListRequirement> makeCodec(RequirementType<ListRequirement> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                ExtraCodecs.lazyInitializedCodec(() -> SpellsCodecs.REQUIREMENT.listOf()).fieldOf("requirements").forGetter(ListRequirement::getList),
                Codec.intRange(1, Integer.MAX_VALUE).fieldOf("minimum").forGetter(ListRequirement::getMinimum)
        ).apply(instance, (list, minimum) -> new ListRequirement(type, list, minimum)));
    }
    
    public static final String ANY_SUFFIX = ".any";
    public static final String ALL_SUFFIX = ".all";
    
    protected List<Requirement> list;
    protected int minimum;
    
    public ListRequirement(RequirementType<?> type)
    {
        super(type);
    }
    
    public ListRequirement(RequirementType<?> type, List<Requirement> list, int minimum)
    {
        this(type);
        this.list = list;
        this.minimum = minimum;
    }
    
    public List<Requirement> getList()
    {
        return list;
    }
    
    public int getMinimum()
    {
        return minimum;
    }
    
    @Override
    protected boolean doesPlayerPass(SpellProgressionHolder spellProgressionHolder, ContainerLevelAccess access)
    {
        return getPassedAmount(spellProgressionHolder, access) > minimum;
    }
    
    @Override
    public void makeDescription(List<Component> tooltip, SpellProgressionHolder spellProgressionHolder, ContainerLevelAccess access)
    {
        int amount = getPassedAmount(spellProgressionHolder, access);
        
        if(minimum == 1)
        {
            tooltip.add(formatComponent(spellProgressionHolder, access, Component.translatable(getDescriptionId() + ANY_SUFFIX)));
        }
        else if(amount == minimum)
        {
            tooltip.add(formatComponent(spellProgressionHolder, access, Component.translatable(getDescriptionId() + ALL_SUFFIX)));
        }
        else
        {
            tooltip.add(formatComponent(spellProgressionHolder, access, Component.translatable(getDescriptionId(), amount, minimum)));
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
        list.forEach(r -> RequirementType.writeToBuf(buf, r));
        
        buf.writeInt(minimum);
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
        
        minimum = buf.readInt();
    }
}
