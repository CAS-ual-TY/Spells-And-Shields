package de.cas_ual_ty.spells.capability;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import de.cas_ual_ty.spells.SpellsAndShields;
import de.cas_ual_ty.spells.network.ParticleEmitterSyncMessage;
import de.cas_ual_ty.spells.registers.Spells;
import de.cas_ual_ty.spells.spell.Spell;
import de.cas_ual_ty.spells.util.SpellsUtil;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.LinkedList;

public class ParticleEmitterHolder implements INBTSerializable<ListTag>
{
    public final Entity holder;
    private LinkedList<ParticleEmitter> list;
    
    public ParticleEmitterHolder(Entity holder)
    {
        this.holder = holder;
        list = new LinkedList<>();
    }
    
    public void addParticleEmitter(ParticleEmitter emitter)
    {
        if(!holder.level.isClientSide)
        {
            sendSync(emitter);
        }
        
        list.add(emitter);
    }
    
    public void clear()
    {
        list.clear();
    }
    
    public void tick(boolean emit)
    {
        LinkedList<ParticleEmitter> newList = new LinkedList<>();
        
        for(ParticleEmitter e : list)
        {
            if(e.tick() && emit)
            {
                for(int i = 0; i < e.amount; i++)
                {
                    Vec3 pos = holder.position().add(e.offset).add(e.motionSpread ? holder.getDeltaMovement().scale((double) i / (double) e.amount) : Vec3.ZERO);
                    double x = pos.x + (SpellsUtil.RANDOM.nextDouble() - 0.5D) * e.spread;
                    double y = pos.y + (SpellsUtil.RANDOM.nextDouble() - 0.5D) * e.spread;
                    double z = pos.z + (SpellsUtil.RANDOM.nextDouble() - 0.5D) * e.spread;
                    
                    holder.level.addParticle(e.particle, x, y, z, 0, 0, 0);
                }
            }
            
            if(!e.remove())
            {
                newList.add(e);
            }
        }
        
        list = newList;
    }
    
    public Entity getHolder()
    {
        return holder;
    }
    
    @Override
    public ListTag serializeNBT()
    {
        Registry<Spell> spellRegistry = Spells.getRegistry(holder.level);
        ListTag tag = new ListTag();
        list.stream().map(ParticleEmitter::serializeNBT).forEach(tag::add);
        return tag;
    }
    
    @Override
    public void deserializeNBT(ListTag nbt)
    {
        nbt.stream()
                .filter(t -> t instanceof CompoundTag)
                .map(t -> new ParticleEmitter((CompoundTag) t))
                .filter(e -> e.particle != null)
                .forEach(list::add);
    }
    
    public ParticleEmitterSyncMessage makeSyncMessage()
    {
        return new ParticleEmitterSyncMessage(holder.getId(), true, list);
    }
    
    public void sendSync(ParticleEmitter emitter)
    {
        SpellsAndShields.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> holder), new ParticleEmitterSyncMessage(holder.getId(), false, ImmutableList.of(emitter)));
    }
    
    public static class ParticleEmitter
    {
        public final int duration;
        public final int delay;
        public final int amount;
        public final double spread;
        public final boolean motionSpread;
        public final Vec3 offset;
        public final ParticleOptions particle;
        
        private int time;
        
        public ParticleEmitter(int duration, int delay, int amount, double spread, boolean motionSpread, Vec3 offset, ParticleOptions particle, int time)
        {
            this.duration = duration;
            this.delay = delay;
            this.amount = amount;
            this.spread = spread;
            this.motionSpread = motionSpread;
            this.offset = offset;
            this.particle = particle;
            this.time = time;
        }
        
        public ParticleEmitter(int duration, int delay, int amount, double spread, boolean motionSpread, Vec3 offset, ParticleOptions particle)
        {
            this(duration, delay, amount, spread, motionSpread, offset, particle, duration);
        }
        
        public ParticleEmitter(CompoundTag tag)
        {
            this(
                    tag.getInt("duration"),
                    tag.getInt("delay"),
                    tag.getInt("amount"),
                    tag.getDouble("spread"),
                    tag.getBoolean("motionSpread"),
                    new Vec3(tag.getDouble("offX"), tag.getDouble("offY"), tag.getDouble("offZ")),
                    ParticleTypes.CODEC.decode(NbtOps.INSTANCE, tag.get("particle")).get().map(Pair::getFirst, o -> null),
                    tag.getInt("time")
            );
        }
        
        public boolean tick()
        {
            return (time-- % delay) == 0;
        }
        
        public boolean remove()
        {
            return time <= 0;
        }
        
        public int getTime()
        {
            return time;
        }
        
        public CompoundTag serializeNBT()
        {
            CompoundTag tag = new CompoundTag();
            
            tag.putInt("duration", duration);
            tag.putInt("delay", delay);
            tag.putInt("amount", amount);
            tag.putDouble("spread", spread);
            tag.putBoolean("motionSpread", motionSpread);
            tag.putDouble("offX", offset.x);
            tag.putDouble("offY", offset.y);
            tag.putDouble("offZ", offset.z);
            tag.put("particle", ParticleTypes.CODEC.encodeStart(NbtOps.INSTANCE, particle).get().map(t -> t, o -> new CompoundTag()));
            tag.putInt("time", time);
            
            return tag;
        }
        
        public void toByteBuf(FriendlyByteBuf buf)
        {
            buf.writeInt(duration);
            buf.writeShort(delay);
            buf.writeByte(amount);
            buf.writeFloat((float) spread);
            buf.writeBoolean(motionSpread);
            buf.writeFloat((float) offset.x);
            buf.writeFloat((float) offset.y);
            buf.writeFloat((float) offset.z);
            buf.writeRegistryId(ForgeRegistries.PARTICLE_TYPES, particle.getType());
            particle.writeToNetwork(buf);
            buf.writeInt(time);
        }
        
        public static <P extends ParticleOptions> ParticleEmitter fromByteBuf(FriendlyByteBuf buf)
        {
            int duration = buf.readInt();
            int delay = buf.readShort();
            int amount = buf.readByte();
            double spread = buf.readFloat();
            boolean motionSpread = buf.readBoolean();
            Vec3 offset = new Vec3(buf.readFloat(), buf.readFloat(), buf.readFloat());
            ParticleType<P> particleType = buf.readRegistryId();
            P particle = particleType.getDeserializer().fromNetwork(particleType, buf);
            int time = buf.readInt();
            return new ParticleEmitter(duration, delay, amount, spread, motionSpread, offset, particle, time);
        }
        
        @Override
        public String toString()
        {
            return "ParticleEmitter{" +
                    "duration=" + duration +
                    ", delay=" + delay +
                    ", amount=" + amount +
                    ", spread=" + spread +
                    ", motionSpread=" + motionSpread +
                    ", offset=" + offset +
                    ", particle=" + particle +
                    ", time=" + time +
                    '}';
        }
    }
    
    public static LazyOptional<ParticleEmitterHolder> getHolder(Entity entity)
    {
        return entity.getCapability(SpellsCapabilities.PARTICLE_EMITTER_HOLDER_CAPABILITY).cast();
    }
}
