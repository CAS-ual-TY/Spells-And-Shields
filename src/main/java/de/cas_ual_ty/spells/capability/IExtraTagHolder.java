package de.cas_ual_ty.spells.capability;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;

public interface IExtraTagHolder extends INBTSerializable<CompoundTag>
{
    void applyExtraTag(CompoundTag tag);
    
    CompoundTag getExtraTag();
}
