package de.cas_ual_ty.spells.spell.action.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.registers.CtxVarTypes;
import de.cas_ual_ty.spells.registers.TargetTypes;
import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.context.SpellContext;
import de.cas_ual_ty.spells.spell.context.TargetGroup;
import de.cas_ual_ty.spells.spell.target.ITargetType;
import de.cas_ual_ty.spells.spell.target.PositionTarget;
import de.cas_ual_ty.spells.spell.variable.DynamicCtxVar;
import de.cas_ual_ty.spells.util.ParamNames;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

public class PlaySoundAction extends AffectTypeAction<PositionTarget>
{
    public static Codec<PlaySoundAction> makeCodec(SpellActionType<PlaySoundAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                targetsCodec(),
                SoundEvent.CODEC.fieldOf("sound_event").forGetter(PlaySoundAction::getSoundEvent),
                CtxVarTypes.DOUBLE.get().refCodec().fieldOf(ParamNames.paramDouble("volume")).forGetter(PlaySoundAction::getVolume),
                CtxVarTypes.DOUBLE.get().refCodec().fieldOf(ParamNames.paramDouble("pitch")).forGetter(PlaySoundAction::getPitch)
        ).apply(instance, (activation, targets, particle, count, spread) -> new PlaySoundAction(type, activation, targets, particle, count, spread)));
    }
    
    protected SoundEvent soundEvent;
    protected DynamicCtxVar<Double> volume;
    protected DynamicCtxVar<Double> pitch;
    
    public PlaySoundAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public PlaySoundAction(SpellActionType<?> type, String activation, String targets, SoundEvent soundEvent, DynamicCtxVar<Double> volume, DynamicCtxVar<Double> pitch)
    {
        super(type, activation, targets);
        this.soundEvent = soundEvent;
        this.volume = volume;
        this.pitch = pitch;
    }
    
    public SoundEvent getSoundEvent()
    {
        return soundEvent;
    }
    
    public DynamicCtxVar<Double> getVolume()
    {
        return volume;
    }
    
    public DynamicCtxVar<Double> getPitch()
    {
        return pitch;
    }
    
    @Override
    public ITargetType<PositionTarget> getAffectedType()
    {
        return TargetTypes.POSITION.get();
    }
    
    @Override
    public void affectTarget(SpellContext ctx, TargetGroup group, PositionTarget positionTarget)
    {
        volume.getValue(ctx).ifPresent(volume ->
        {
            pitch.getValue(ctx).ifPresent(pitch ->
            {
                if(positionTarget.getLevel() instanceof ServerLevel level)
                {
                    level.playSound(
                            null,
                            positionTarget.getPosition().x(),
                            positionTarget.getPosition().y(),
                            positionTarget.getPosition().z(),
                            soundEvent,
                            SoundSource.PLAYERS,
                            volume.floatValue(),
                            pitch.floatValue()
                    );
                }
            });
        });
    }
}
