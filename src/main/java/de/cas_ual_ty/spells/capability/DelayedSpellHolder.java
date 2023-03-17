package de.cas_ual_ty.spells.capability;

import de.cas_ual_ty.spells.registers.CtxVarTypes;
import de.cas_ual_ty.spells.registers.SpellTrees;
import de.cas_ual_ty.spells.spell.SpellInstance;
import de.cas_ual_ty.spells.spell.context.BuiltinTargetGroups;
import de.cas_ual_ty.spells.spell.context.BuiltinVariables;
import de.cas_ual_ty.spells.spell.target.Target;
import de.cas_ual_ty.spells.spelltree.SpellNodeId;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.common.util.LazyOptional;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class DelayedSpellHolder implements IDelayedSpellHolder
{
    public final Entity holder;
    private List<DelayedSpell> spells;
    
    public DelayedSpellHolder(Entity holder)
    {
        this.holder = holder;
        spells = new LinkedList<>();
    }
    
    private void activate(DelayedSpell spell)
    {
        SpellInstance s = spell.spell.getSpellInstance(SpellTrees.getRegistry(holder.level));
        if(s != null)
        {
            s.run(holder.level, null, spell.activation, ctx ->
            {
                ctx.getOrCreateTargetGroup(BuiltinTargetGroups.HOLDER.targetGroup).addTargets(Target.of(holder));
                ctx.setCtxVar(CtxVarTypes.INT.get(), BuiltinVariables.DELAY_TIME.name, spell.getTime());
            });
        }
    }
    
    @Override
    public void addDelayedSpell(SpellNodeId spell, UUID uuid, String activation, int tickTime)
    {
        spells.add(new DelayedSpell(spell, uuid, activation, tickTime));
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
        
        return spells.stream().filter(s -> uuid.equals(s.uuid)).findAny().map(s ->
        {
            activate(s);
            return true;
        }).orElse(false);
    }
    
    @Override
    public void tick()
    {
        spells.removeIf(s ->
        {
            if(s.tick())
            {
                activate(s);
                return true;
            }
            return false;
        });
    }
    
    @Override
    public Entity getHolder()
    {
        return holder;
    }
    
    @Override
    public ListTag serializeNBT()
    {
        ListTag tag = new ListTag();
        spells.stream().map(DelayedSpell::serializeNBT).forEach(tag::add);
        return tag;
    }
    
    @Override
    public void deserializeNBT(ListTag nbt)
    {
        nbt.stream()
                .filter(t -> t instanceof CompoundTag)
                .map(t -> (CompoundTag) t)
                .map(DelayedSpell::new)
                .filter(s -> s.spell != null)
                .forEach(spells::add);
    }
    
    public static class DelayedSpell
    {
        public final SpellNodeId spell;
        public final UUID uuid;
        public final String activation;
        public final int tickTime;
        
        private int time;
        
        public DelayedSpell(SpellNodeId spell, UUID uuid, String activation, int tickTime)
        {
            this.spell = spell;
            this.uuid = uuid;
            this.activation = activation;
            this.tickTime = tickTime;
            this.time = 0;
        }
        
        public DelayedSpell(CompoundTag tag)
        {
            this.spell = SpellNodeId.fromNbt(tag);
            
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
        }
        
        public boolean tick()
        {
            return ++time >= tickTime;
        }
        
        public int getTime()
        {
            return time;
        }
        
        public CompoundTag serializeNBT()
        {
            CompoundTag tag = new CompoundTag();
            spell.toNbt(tag);
            
            if(uuid != null)
            {
                tag.putUUID("uuid", uuid);
            }
            
            tag.putString("activation", activation);
            tag.putInt("maxTime", tickTime);
            tag.putInt("time", time);
            return tag;
        }
    }
    
    public static LazyOptional<DelayedSpellHolder> getHolder(Entity entity)
    {
        return entity.getCapability(SpellsCapabilities.DELAYED_SPELL_HOLDER_CAPABILITY).cast();
    }
}
