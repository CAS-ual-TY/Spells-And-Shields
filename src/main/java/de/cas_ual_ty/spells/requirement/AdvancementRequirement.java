package de.cas_ual_ty.spells.requirement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.capability.SpellProgressionHolder;
import de.cas_ual_ty.spells.util.SpellsDowngrade;
import net.minecraft.advancements.Advancement;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.ContainerLevelAccess;

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
    
    @Override
    protected boolean doesPlayerPass(SpellProgressionHolder spellProgressionHolder, ContainerLevelAccess access)
    {
        if(spellProgressionHolder.getPlayer() instanceof ServerPlayer player)
        {
            Advancement a = player.server.getAdvancements().getAdvancement(advancementRL);
            
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
    public MutableComponent makeDescription(SpellProgressionHolder spellProgressionHolder, ContainerLevelAccess access)
    {
        if(spellProgressionHolder.getPlayer() instanceof ServerPlayer player)
        {
            Advancement a = player.server.getAdvancements().getAdvancement(advancementRL);
            
            if(a != null)
            {
                return SpellsDowngrade.translatable(descriptionId, a.getDisplay().getTitle());
            }
            else
            {
                return SpellsDowngrade.translatable(descriptionId + ERROR_SUFFIX, advancementRL.toString());
            }
        }
        
        return SpellsDowngrade.empty();
    }
    
    @Override
    public void writeToBuf(FriendlyByteBuf buf)
    {
        buf.writeResourceLocation(advancementRL);
    }
    
    @Override
    public void readFromBuf(FriendlyByteBuf buf)
    {
        advancementRL = buf.readRegistryId();
    }
}
