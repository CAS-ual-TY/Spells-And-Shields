package de.cas_ual_ty.spells.spell.action.fx;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.registers.SpellsCodecs;
import de.cas_ual_ty.spells.spell.variable.CtxVarType;

/**
 * One entry of a {@code CustomParticleEmitterActionBase#initialize} list - a named ctx var, evaluated once per
 * particle at spawn (against that particle's own {@code index}, {@code age} 0) from a raw DSL formula string,
 * then kept on the particle so every later per-tick evaluation of {@code position}/{@code motion}/{@code color}/
 * {@code alpha} can reference it by name (see {@code CustomParticleInstance#initVars}).
 * <p>
 * Like {@code position}/{@code motion}/{@code color}/{@code alpha}, {@link #value} is a raw DSL formula string,
 * NOT a {@link de.cas_ual_ty.spells.spell.variable.DynamicCtxVar} resolved server-side - the server has no
 * concept of individual particles or their {@code index} to evaluate against, so this compiles and evaluates
 * client-side (see {@code CustomParticleInitVar}), same as those fields.
 */
public record CustomParticleInitEntry(CtxVarType<?> type, String name, String value)
{
    public static final Codec<CustomParticleInitEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.lazyInitialized(() -> SpellsCodecs.CTX_VAR_TYPE).fieldOf("type").forGetter(CustomParticleInitEntry::type),
            Codec.STRING.fieldOf("name").forGetter(CustomParticleInitEntry::name),
            Codec.STRING.fieldOf("value").forGetter(CustomParticleInitEntry::value)
    ).apply(instance, CustomParticleInitEntry::new));
}
