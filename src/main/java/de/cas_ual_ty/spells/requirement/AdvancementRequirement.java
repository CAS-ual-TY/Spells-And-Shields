package de.cas_ual_ty.spells.requirement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.capability.SpellProgressionHolder;
import net.minecraft.advancements.Advancement;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.ContainerLevelAccess;

import java.util.List;

public class AdvancementRequirement extends Requirement
{
    public static Codec<AdvancementRequirement> makeCodec(RequirementType<AdvancementRequirement> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                ResourceLocation.CODEC.fieldOf("advancement").forGetter(AdvancementRequirement::getAdvancementRL)
        ).apply(instance, (advancementRL) -> new AdvancementRequirement(type, advancementRL)));
    }
    
    public static final String ERROR_SUFFIX = ".error";
    
    protected ResourceLocation advancementRL;
    protected Advancement advancement;
    
    public AdvancementRequirement(RequirementType<?> type)
    {
        super(type);
    }
    
    public AdvancementRequirement(RequirementType<?> type, ResourceLocation advancementRL)
    {
        this(type);
        this.advancementRL = advancementRL;
    }
    
    public ResourceLocation getAdvancementRL()
    {
        return advancementRL;
    }
    
    public Advancement getAdvancement(MinecraftServer server)
    {
        if(advancement == null)
        {
            advancement = server.getAdvancements().getAdvancement(advancementRL);
        }
        
        return advancement;
    }
    
    @Override
    protected boolean doesPlayerPass(SpellProgressionHolder spellProgressionHolder, ContainerLevelAccess access)
    {
        if(spellProgressionHolder.getPlayer() instanceof ServerPlayer player)
        {
            Advancement a = getAdvancement(player.server);
            
            if(a != null)
            {
                return player.getAdvancements().getOrStartProgress(a).isDone();
            }
            else
            {
                return true;
            }
        }
        
        return false;
    }
    
    @Override
    public void makeDescription(List<Component> tooltip, SpellProgressionHolder spellProgressionHolder, ContainerLevelAccess access)
    {
        if(spellProgressionHolder.getPlayer() instanceof ServerPlayer player)
        {
            Advancement a = getAdvancement(player.server);
            
            if(a != null)
            {
                tooltip.add(formatComponent(spellProgressionHolder, access, Component.translatable(descriptionId, a.getDisplay().getTitle())));
            }
            else
            {
                tooltip.add(formatComponent(spellProgressionHolder, access, Component.translatable(descriptionId + ERROR_SUFFIX, advancementRL.toString())));
            }
        }
    }
    
    @Override
    public void writeToBuf(FriendlyByteBuf buf)
    {
        buf.writeResourceLocation(advancementRL);
    }
    
    @Override
    public void readFromBuf(FriendlyByteBuf buf)
    {
        advancementRL = buf.readResourceLocation();
    }
}
