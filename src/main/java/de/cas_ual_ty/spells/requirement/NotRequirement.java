package de.cas_ual_ty.spells.requirement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.capability.SpellProgressionHolder;
import de.cas_ual_ty.spells.util.SpellsCodecs;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.inventory.ContainerLevelAccess;

import java.util.LinkedList;
import java.util.List;

public class NotRequirement extends Requirement
{
    public static Codec<NotRequirement> makeCodec(RequirementType<NotRequirement> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                ExtraCodecs.lazyInitializedCodec(() -> SpellsCodecs.REQUIREMENT).fieldOf("requirement").forGetter(NotRequirement::getRequirement)
        ).apply(instance, (list) -> new NotRequirement(type, list)));
    }
    
    protected Requirement requirement;
    
    public NotRequirement(RequirementType<?> type)
    {
        super(type);
    }
    
    public NotRequirement(RequirementType<?> type, Requirement requirement)
    {
        this(type);
        this.requirement = requirement;
    }
    
    public Requirement getRequirement()
    {
        return requirement;
    }
    
    @Override
    protected boolean doesPlayerPass(SpellProgressionHolder spellProgressionHolder, ContainerLevelAccess access)
    {
        return !requirement.doesPlayerPass(spellProgressionHolder, access);
    }
    
    @Override
    public void makeDescription(List<Component> tooltip, SpellProgressionHolder spellProgressionHolder, ContainerLevelAccess access)
    {
        tooltip.add(formatComponent(spellProgressionHolder, access, Component.translatable(getDescriptionId())));
        
        List<Component> subTooltip = new LinkedList<>();
        requirement.makeDescription(subTooltip, spellProgressionHolder, access);
        subTooltip.stream().map(c -> Component.literal("  ").append(c)).forEach(tooltip::add);
    }
    
    @Override
    public void writeToBuf(FriendlyByteBuf buf)
    {
        RequirementType.writeToBuf(buf, requirement);
    }
    
    @Override
    public void readFromBuf(FriendlyByteBuf buf)
    {
        requirement = RequirementType.readFromBuf(buf);
    }
}
