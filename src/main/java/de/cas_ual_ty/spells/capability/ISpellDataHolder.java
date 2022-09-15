package de.cas_ual_ty.spells.capability;

import de.cas_ual_ty.spells.spelldata.ISpellDataType;
import de.cas_ual_ty.spells.spelldata.SpellData;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.List;
import java.util.function.Consumer;

public interface ISpellDataHolder extends INBTSerializable<ListTag>
{
    LivingEntity getEntity();
    
    void tick();
    
    void add(SpellData data);
    
    void remove(SpellData spellData);
    
    void removeAllOfType(ISpellDataType<?> type);
    
    boolean hasOfType(ISpellDataType<?> type);
    
    void clear();
    
    List<SpellData> getList();
    
    <D extends SpellData> List<D> getAllOfType(ISpellDataType<D> type);
    
    <D extends SpellData> void forEachOfType(ISpellDataType<D> type, Consumer<D> consumer);
}
