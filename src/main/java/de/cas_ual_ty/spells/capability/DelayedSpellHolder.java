package de.cas_ual_ty.spells.capability;

import de.cas_ual_ty.spells.registers.CtxVarTypes;
import de.cas_ual_ty.spells.registers.SpellTrees;
import de.cas_ual_ty.spells.registers.Spells;
import de.cas_ual_ty.spells.spell.Spell;
import de.cas_ual_ty.spells.spell.SpellInstance;
import de.cas_ual_ty.spells.spell.context.BuiltinTargetGroups;
import de.cas_ual_ty.spells.spell.context.BuiltinVariables;
import de.cas_ual_ty.spells.spell.target.Target;
import de.cas_ual_ty.spells.spelltree.SpellTree;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.common.util.LazyOptional;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.UUID;

public class DelayedSpellHolder implements IDelayedSpellHolder
{
    public final Entity holder;
    private LinkedList<DelayedSpell> spells;
    
    public DelayedSpellHolder(Entity holder)
    {
        this.holder = holder;
        spells = new LinkedList<>();
    }
    
    private void activate(DelayedSpell spell)
    {
        SpellInstance s = spell.spell;
        if(s != null)
        {
            s.run(holder.level, null, spell.activation, ctx ->
            {
                ctx.getOrCreateTargetGroup(BuiltinTargetGroups.HOLDER.targetGroup).addTargets(Target.of(holder));
                ctx.setCtxVar(CtxVarTypes.INT.get(), BuiltinVariables.DELAY_TIME.name, spell.getTime());
                ctx.setCtxVar(CtxVarTypes.COMPOUND_TAG.get(), BuiltinVariables.DELAY_TAG.name, spell.tag);
                
                if(spell.uuid != null)
                {
                    ctx.setCtxVar(CtxVarTypes.STRING.get(), BuiltinVariables.DELAY_UUID.name, spell.uuid.toString());
                }
            });
        }
    }
    
    @Override
    public void addDelayedSpell(SpellInstance spell, UUID uuid, String activation, int tickTime, CompoundTag tag)
    {
        spells.add(new DelayedSpell(spell, uuid, activation, tickTime, tag));
    }
    
    @Override
    public boolean hasDelayedSpell(UUID uuid)
    {
        return uuid == null ? false : spells.stream().anyMatch(s -> uuid.equals(s.uuid));
    }
    
    @Override
    public boolean removeDelayedSpell(UUID uuid, boolean forceActivate)
    {
        if(uuid == null)
        {
            return false;
        }
        
        Iterator<DelayedSpell> iterator = spells.iterator();
        
        while(iterator.hasNext())
        {
            DelayedSpell spell = iterator.next();
            
            if(uuid.equals(spell.uuid))
            {
                iterator.remove();
                
                if(forceActivate)
                {
                    activate(spell);
                }
                
                return true;
            }
        }
        
        return false;
    }
    
    @Override
    public void tick()
    {
        LinkedList<DelayedSpell> theSpells = spells;
        spells = new LinkedList<>();
        
        theSpells.removeIf(s ->
        {
            if(s.tick())
            {
                activate(s);
                return true;
            }
            return false;
        });
        
        while(!theSpells.isEmpty())
        {
            spells.addFirst(theSpells.removeLast());
        }
    }
    
    @Override
    public Entity getHolder()
    {
        return holder;
    }
    
    @Override
    public ListTag serializeNBT()
    {
        Registry<Spell> spellRegistry = Spells.getRegistry(holder.level);
        ListTag tag = new ListTag();
        spells.stream().map(ds -> ds.serializeNBT(spellRegistry)).forEach(tag::add);
        return tag;
    }
    
    @Override
    public void deserializeNBT(ListTag nbt)
    {
        Registry<SpellTree> spellTreeRegistry = SpellTrees.getRegistry(holder.level);
        Registry<Spell> spellRegistry = Spells.getRegistry(holder.level);
        nbt.stream()
                .filter(t -> t instanceof CompoundTag)
                .map(t -> new DelayedSpell((CompoundTag) t, spellTreeRegistry, spellRegistry))
                .filter(s -> s.spell != null)
                .forEach(spells::add);
    }
    
    public static class DelayedSpell
    {
        public final SpellInstance spell;
        public final UUID uuid;
        public final String activation;
        public final int tickTime;
        public final CompoundTag tag;
        
        private int time;
        
        public DelayedSpell(SpellInstance spell, UUID uuid, String activation, int tickTime, CompoundTag tag)
        {
            this.spell = spell;
            this.uuid = uuid;
            this.activation = activation;
            this.tickTime = tickTime;
            this.tag = tag;
            this.time = 0;
        }
        
        public DelayedSpell(CompoundTag tag, Registry<SpellTree> spellTreeRegistry, Registry<Spell> spellRegistry)
        {
            this.spell = SpellInstance.fromNbt(tag, spellTreeRegistry, spellRegistry);
            
            if(tag.hasUUID("uuid"))
            {
                uuid = tag.getUUID("uuid");
            }
            else
            {
                uuid = null;
            }
            
            activation = tag.getString("activation");
            tickTime = tag.getInt("maxTime");
            time = tag.getInt("time");
            this.tag = tag.getCompound("tag");
        }
        
        public boolean tick()
        {
            if(!spell.getSpell().isBound())
            {
                return false;
            }
            
            return ++time >= tickTime;
        }
        
        public int getTime()
        {
            return time;
        }
        
        public CompoundTag serializeNBT(Registry<Spell> spellRegistry)
        {
            CompoundTag tag = new CompoundTag();
            
            spell.toNbt(tag, spellRegistry);
            
            if(uuid != null)
            {
                tag.putUUID("uuid", uuid);
            }
            
            tag.putString("activation", activation);
            tag.putInt("maxTime", tickTime);
            tag.putInt("time", time);
            tag.put("tag", this.tag);
            return tag;
        }
    }
    
    public static LazyOptional<DelayedSpellHolder> getHolder(Entity entity)
    {
        return entity.getCapability(SpellsCapabilities.DELAYED_SPELL_HOLDER_CAPABILITY).cast();
    }
}
