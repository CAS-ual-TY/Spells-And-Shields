package de.cas_ual_ty.spells.requirement;

import de.cas_ual_ty.spells.capability.SpellProgressionHolder;
import de.cas_ual_ty.spells.registers.RequirementTypes;
import de.cas_ual_ty.spells.util.SpellsDowngrade;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.ContainerLevelAccess;

import java.util.List;

public abstract class Requirement
{
    public static final MutableComponent EMPTY = SpellsDowngrade.empty();
    
    public final RequirementType<?> type;
    
    protected String descriptionId;
    
    public Requirement(RequirementType<?> type)
    {
        this.type = type;
        
        ResourceLocation rl = RequirementTypes.REGISTRY.get().getKey(type);
        descriptionId = "requirement." + rl.getNamespace() + "." + rl.getPath();
    }
    
    public RequirementType<?> getType()
    {
        return type;
    }
    
    public boolean passes(SpellProgressionHolder spellProgressionHolder, ContainerLevelAccess access)
    {
        return spellProgressionHolder.getPlayer().isCreative() ? (creativeModePasses() || doesPlayerPass(spellProgressionHolder, access)) : doesPlayerPass(spellProgressionHolder, access);
    }
    
    
    protected abstract boolean doesPlayerPass(SpellProgressionHolder spellProgressionHolder, ContainerLevelAccess access);
    
    public boolean creativeModePasses()
    {
        return true;
    }
    
    public void onSpellLearned(SpellProgressionHolder spellProgressionHolder, ContainerLevelAccess access)
    {
    }
    
    public abstract void makeDescription(List<Component> tooltip, SpellProgressionHolder spellProgressionHolder, ContainerLevelAccess access);
    
    public String getDescriptionId()
    {
        return descriptionId;
    }
    
    public MutableComponent formatComponent(SpellProgressionHolder spellProgressionHolder, ContainerLevelAccess access, MutableComponent component)
    {
        return SpellsDowngrade.literal("- ").append(component).withStyle(doesPlayerPass(spellProgressionHolder, access) ? ChatFormatting.GREEN : ChatFormatting.RED);
    }
    
    public abstract void writeToBuf(FriendlyByteBuf buf);
    
    public abstract void readFromBuf(FriendlyByteBuf buf);
}