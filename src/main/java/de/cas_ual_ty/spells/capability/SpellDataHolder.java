package de.cas_ual_ty.spells.capability;

import de.cas_ual_ty.spells.spelldata.ISpellDataType;
import de.cas_ual_ty.spells.spelldata.ITickedSpellData;
import de.cas_ual_ty.spells.spelldata.SpellData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.util.LazyOptional;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class SpellDataHolder implements ISpellDataHolder
{
    protected LivingEntity entity;
    protected List<SpellData> list;
    
    public SpellDataHolder(LivingEntity entity)
    {
        this.entity = entity;
        this.list = new LinkedList<>();
    }
    
    @Override
    public LivingEntity getEntity()
    {
        return entity;
    }
    
    @Override
    public void tick()
    {
        list.stream().filter(d -> d instanceof ITickedSpellData)
                .map(d -> (ITickedSpellData) d)
                .filter(d -> d.tickOnClient() || !entity.level.isClientSide)
                .forEach(d -> d.tick(this));
        
        LinkedList<SpellData> newList = new LinkedList<>();
        list.stream().filter(d -> !d.shouldRemove(this)).forEach(newList::add);
        list = newList;
    }
    
    @Override
    public void add(SpellData data)
    {
        list.add(data);
    }
    
    @Override
    public void remove(SpellData spellData)
    {
        list.remove(spellData);
    }
    
    @Override
    public void removeAllOfType(ISpellDataType<?> type)
    {
        list.removeIf(data -> data.type == type);
    }
    
    @Override
    public boolean hasOfType(ISpellDataType<?> type)
    {
        return list.stream().anyMatch(data -> data.type == type);
    }
    
    @Override
    public void clear()
    {
        list.clear();
    }
    
    @Override
    public List<SpellData> getList()
    {
        return list;
    }
    
    @Override
    public <D extends SpellData> List<D> getAllOfType(ISpellDataType<D> type)
    {
        List<D> list = new LinkedList<>();
        forEachOfType(type, list::add);
        return list;
    }
    
    @Override
    public <D extends SpellData> void forEachOfType(ISpellDataType<D> type, Consumer<D> consumer)
    {
        list.stream().filter(data -> data.type == type).map(data -> (D) data).forEach(consumer);
    }
    
    @Override
    public ListTag serializeNBT()
    {
        ListTag tag = new ListTag();
        list.stream().map(ISpellDataType::serialize).forEach(tag::add);
        return tag;
    }
    
    @Override
    public void deserializeNBT(ListTag nbt)
    {
        list.clear();
        nbt.stream().filter(tag -> tag instanceof CompoundTag)
                .map(t -> ISpellDataType.deserialize((CompoundTag) t))
                .filter(Objects::nonNull)
                .forEach(list::add);
    }
    
    public static LazyOptional<SpellDataHolder> getSpellDataHolder(LivingEntity entity)
    {
        return entity.getCapability(SpellsCapabilities.SPELL_DATA_CAPABILITY).cast();
    }
}
