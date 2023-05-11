package de.cas_ual_ty.spells.spell.action.fx;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.registers.CtxVarTypes;
import de.cas_ual_ty.spells.registers.SpellActionTypes;
import de.cas_ual_ty.spells.registers.TargetTypes;
import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.action.base.AffectTypeAction;
import de.cas_ual_ty.spells.spell.context.SpellContext;
import de.cas_ual_ty.spells.spell.context.TargetGroup;
import de.cas_ual_ty.spells.spell.target.ITargetType;
import de.cas_ual_ty.spells.spell.target.PositionTarget;
import de.cas_ual_ty.spells.spell.variable.DynamicCtxVar;
import de.cas_ual_ty.spells.util.ParamNames;
import de.cas_ual_ty.spells.util.SpellsUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.registries.ForgeRegistries;

public class PlaySoundAction extends AffectTypeAction<PositionTarget>
{
    public static Codec<PlaySoundAction> makeCodec(SpellActionType<PlaySoundAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                multiTargetsCodec(),
                CtxVarTypes.STRING.get().refCodec().fieldOf(ParamNames.paramString("sound_event")).forGetter(PlaySoundAction::getSoundEvent),
                CtxVarTypes.DOUBLE.get().refCodec().fieldOf(ParamNames.paramDouble("volume")).forGetter(PlaySoundAction::getVolume),
                CtxVarTypes.DOUBLE.get().refCodec().fieldOf(ParamNames.paramDouble("pitch")).forGetter(PlaySoundAction::getPitch)
        ).apply(instance, (activation, multiTargets, particle, count, spread) -> new PlaySoundAction(type, activation, multiTargets, particle, count, spread)));
    }
    
    public static PlaySoundAction make(Object activation, Object multiTargets, DynamicCtxVar<String> soundEvent, DynamicCtxVar<Double> volume, DynamicCtxVar<Double> pitch)
    {
        return new PlaySoundAction(SpellActionTypes.PLAY_SOUND.get(), activation.toString(), multiTargets.toString(), soundEvent, volume, pitch);
    }
    
    public static PlaySoundAction make(Object activation, Object multiTargets, SoundEvent soundEvent, DynamicCtxVar<Double> volume, DynamicCtxVar<Double> pitch)
    {
        return new PlaySoundAction(SpellActionTypes.PLAY_SOUND.get(), activation.toString(), multiTargets.toString(), SpellsUtil.objectToString(soundEvent, ForgeRegistries.SOUND_EVENTS), volume, pitch);
    }
    
    protected DynamicCtxVar<String> soundEvent;
    protected DynamicCtxVar<Double> volume;
    protected DynamicCtxVar<Double> pitch;
    
    public PlaySoundAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public PlaySoundAction(SpellActionType<?> type, String activation, String multiTargets, DynamicCtxVar<String> soundEvent, DynamicCtxVar<Double> volume, DynamicCtxVar<Double> pitch)
    {
        super(type, activation, multiTargets);
        this.soundEvent = soundEvent;
        this.volume = volume;
        this.pitch = pitch;
    }
    
    public DynamicCtxVar<String> getSoundEvent()
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
                SpellsUtil.stringToObject(ctx, soundEvent, ForgeRegistries.SOUND_EVENTS).ifPresent(soundEvent ->
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
        });
    }
}
