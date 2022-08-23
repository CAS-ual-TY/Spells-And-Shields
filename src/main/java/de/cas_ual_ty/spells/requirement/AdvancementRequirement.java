package de.cas_ual_ty.spells.requirement;

import com.google.gson.JsonObject;
import de.cas_ual_ty.spells.SpellsAndShields;
import de.cas_ual_ty.spells.capability.SpellProgressionHolder;
import de.cas_ual_ty.spells.util.SpellsFileUtil;
import net.minecraft.advancements.Advancement;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.ContainerLevelAccess;

public class AdvancementRequirement extends Requirement
{
    protected ResourceLocation advancementRL;
    
    public AdvancementRequirement(IRequirementType.RequirementType type)
    {
        super(type);
    }
    
    public AdvancementRequirement(IRequirementType.RequirementType type, ResourceLocation advancementRL)
    {
        super(type);
        this.advancementRL = advancementRL;
    }
    
    @Override
    public boolean passes(SpellProgressionHolder spellProgressionHolder, ContainerLevelAccess access)
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
            
            player.server.getAdvancements().getAllAdvancements().stream().forEach(ad -> SpellsAndShields.LOGGER.info(ad.getId().toString()));
            
            if(a != null)
            {
                return new TranslatableComponent(descriptionId, a.getDisplay().getTitle(), advancementRL.toString());
            }
            else
            {
                return new TranslatableComponent(descriptionId + ".error", advancementRL.toString());
            }
        }
        
        return (MutableComponent) TextComponent.EMPTY;
    }
    
    @Override
    public void writeToJson(JsonObject json)
    {
        json.addProperty("advancement", advancementRL.toString());
    }
    
    @Override
    public void readFromJson(JsonObject json)
    {
        this.advancementRL = new ResourceLocation(SpellsFileUtil.jsonString(json, "advancement"));
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
