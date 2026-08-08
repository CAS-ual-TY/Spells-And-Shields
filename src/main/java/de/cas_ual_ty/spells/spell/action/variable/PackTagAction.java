package de.cas_ual_ty.spells.spell.action.variable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.registers.CtxVarTypes;
import de.cas_ual_ty.spells.registers.SpellActionTypes;
import de.cas_ual_ty.spells.spell.action.ParamNames;
import de.cas_ual_ty.spells.spell.action.SpellAction;
import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.context.SpellContext;
import de.cas_ual_ty.spells.spell.variable.CtxVar;
import de.cas_ual_ty.spells.spell.variable.CtxVarType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;

import java.util.List;

/**
 * Packs a list of named ctx vars into a fresh {@link CompoundTag} in a single action, using each variable's own
 * name as its NBT key - the multi-field equivalent of chaining several {@code put_nbt_int}/{@code put_nbt_string}/
 * ... compiled calls together. Each entry is stored self-describing - {@code {"type": <registry id>, "value":
 * <that type's own codec output>}} - via {@link CtxVarType#getImmCodec()}, the same codec every other ctx var
 * value is already (de)serialized with, so this works for any registered {@link CtxVarType} without this class
 * needing to know about it, including ones registered later by other mods. A name not currently set in the
 * context is silently skipped. See {@link UnpackTagAction} for the reverse operation.
 */
public class PackTagAction extends SpellAction
{
    public static Codec<PackTagAction> makeCodec(SpellActionType<PackTagAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                Codec.STRING.listOf().fieldOf("names").forGetter(PackTagAction::getNames),
                Codec.STRING.fieldOf(ParamNames.varResult()).forGetter(PackTagAction::getResult)
        ).apply(instance, (activation, names, result) -> new PackTagAction(type, activation, names, result)));
    }

    public static PackTagAction make(Object activation, List<String> names, Object result)
    {
        return new PackTagAction(SpellActionTypes.PACK_TAG.get(), activation.toString(), names, result.toString());
    }

    protected List<String> names;
    protected String result;

    public PackTagAction(SpellActionType<?> type)
    {
        super(type);
    }

    public PackTagAction(SpellActionType<?> type, String activation, List<String> names, String result)
    {
        super(type, activation);
        this.names = names;
        this.result = result;
    }

    public List<String> getNames()
    {
        return names;
    }

    public String getResult()
    {
        return result;
    }

    @Override
    protected void wasActivated(SpellContext ctx)
    {
        CompoundTag packed = new CompoundTag();

        for(String name : names)
        {
            CtxVar<?> ctxVar = ctx.getCtxVar(name);

            if(ctxVar != null)
            {
                pack(packed, name, ctxVar);
            }
        }

        ctx.setCtxVar(CtxVarTypes.TAG.get(), result, packed);
    }

    private static <T> void pack(CompoundTag tag, String key, CtxVar<T> ctxVar)
    {
        CtxVarType<T> type = ctxVar.getType();

        type.getImmCodec().encodeStart(NbtOps.INSTANCE, ctxVar.getValue()).result().ifPresent(encoded ->
        {
            CompoundTag entry = new CompoundTag();
            entry.putString("type", CtxVarTypes.REGISTRY.getKey(type).toString());
            entry.put("value", encoded);
            tag.put(key, entry);
        });
    }
}
