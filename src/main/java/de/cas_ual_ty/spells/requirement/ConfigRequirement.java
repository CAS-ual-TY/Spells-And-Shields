package de.cas_ual_ty.spells.requirement;

import com.mojang.serialization.Codec;
import de.cas_ual_ty.spells.SpellsConfig;
import de.cas_ual_ty.spells.capability.SpellProgressionHolder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.inventory.ContainerLevelAccess;

public class ConfigRequirement extends Requirement
{
    public static Codec<ConfigRequirement> makeCodec(RequirementType<ConfigRequirement> type)
    {
        return Codec.unit(() -> new ConfigRequirement(type));
    }
    
    public ConfigRequirement(RequirementType<?> type)
    {
        super(type);
    }
    
    @Override
    protected boolean doesPlayerPass(SpellProgressionHolder spellProgressionHolder, ContainerLevelAccess access)
    {
        return SpellsConfig.SPELL_TREES.get();
    }
    
    @Override
    public boolean creativeModePasses()
    {
        return false;
    }
    
    @Override
    public MutableComponent makeDescription(SpellProgressionHolder spellProgressionHolder, ContainerLevelAccess access)
    {
        return !SpellsConfig.SPELL_TREES.get() && spellProgressionHolder.getPlayer().isCreative() ? Component.translatable(descriptionId) : Component.empty();
    }
    
    @Override
    public void writeToBuf(FriendlyByteBuf buf)
    {
    }
    
    @Override
    public void readFromBuf(FriendlyByteBuf buf)
    {
    }
}
