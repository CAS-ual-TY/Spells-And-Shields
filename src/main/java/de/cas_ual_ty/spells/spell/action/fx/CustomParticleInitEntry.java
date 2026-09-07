package de.cas_ual_ty.spells.spell.action.fx;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.registers.SpellsCodecs;
import de.cas_ual_ty.spells.spell.variable.CtxVarType;

/**
 * One entry of a {@code CustomParticleEmitterActionBase#initialize} list - a named ctx var evaluated once per
 * particle at spawn from a raw DSL formula string, then kept on the particle for later per-tick formulas.
 */
public record CustomParticleInitEntry(CtxVarType<?> type, String name, String value)
{
    public static final Codec<CustomParticleInitEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.lazyInitialized(() -> SpellsCodecs.CTX_VAR_TYPE).fieldOf("type").forGetter(CustomParticleInitEntry::type),
            Codec.STRING.fieldOf("name").forGetter(CustomParticleInitEntry::name),
            Codec.STRING.fieldOf("value").forGetter(CustomParticleInitEntry::value)
    ).apply(instance, CustomParticleInitEntry::new));
}
