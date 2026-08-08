package de.cas_ual_ty.spells.spell.action.variable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.registers.CtxVarTypes;
import de.cas_ual_ty.spells.registers.SpellActionTypes;
import de.cas_ual_ty.spells.spell.action.ParamNames;
import de.cas_ual_ty.spells.spell.action.SpellAction;
import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.context.SpellContext;
import de.cas_ual_ty.spells.spell.variable.CtxVarType;
import de.cas_ual_ty.spells.spell.variable.DynamicCtxVar;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

/**
 * Unpacks every key in a {@link CompoundTag} into its own same-named ctx var in a single action - the reverse
 * of {@link PackTagAction}, and the multi-field equivalent of chaining several {@code get_nbt_int}/
 * {@code get_nbt_string}/... compiled calls together. Each entry is expected in the self-describing shape
 * {@code {"type": <registry id>, "value": <that type's own codec output>}} that {@link PackTagAction} writes -
 * the {@link CtxVarType} is looked up from the registry by its id and its own {@link CtxVarType#getImmCodec()}
 * decodes {@code value}, so this works for any registered type without this class needing to know about it,
 * including ones registered later by other mods. A key that isn't in that shape, whose {@code type} isn't a
 * registered {@link CtxVarType}, or whose {@code value} fails to decode, is silently skipped.
 */
public class UnpackTagAction extends SpellAction
{
    public static Codec<UnpackTagAction> makeCodec(SpellActionType<UnpackTagAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                CtxVarTypes.TAG.get().refCodec().fieldOf(ParamNames.paramCompoundTag("source")).forGetter(UnpackTagAction::getSource)
        ).apply(instance, (activation, source) -> new UnpackTagAction(type, activation, source)));
    }

    public static UnpackTagAction make(Object activation, DynamicCtxVar<CompoundTag> source)
    {
        return new UnpackTagAction(SpellActionTypes.UNPACK_TAG.get(), activation.toString(), source);
    }

    protected DynamicCtxVar<CompoundTag> source;

    public UnpackTagAction(SpellActionType<?> type)
    {
        super(type);
    }

    public UnpackTagAction(SpellActionType<?> type, String activation, DynamicCtxVar<CompoundTag> source)
    {
        super(type, activation);
        this.source = source;
    }

    public DynamicCtxVar<CompoundTag> getSource()
    {
        return source;
    }

    @Override
    protected void wasActivated(SpellContext ctx)
    {
        source.getValue(ctx).ifPresent(tag ->
        {
            for(String key : tag.getAllKeys())
            {
                unpack(ctx, tag, key);
            }
        });
    }

    private static void unpack(SpellContext ctx, CompoundTag tag, String key)
    {
        if(!tag.contains(key, Tag.TAG_COMPOUND))
        {
            return;
        }

        CompoundTag entry = tag.getCompound(key);

        if(!entry.contains("type", Tag.TAG_STRING) || !entry.contains("value"))
        {
            return;
        }

        ResourceLocation typeId = ResourceLocation.tryParse(entry.getString("type"));

        if(typeId == null)
        {
            return;
        }

        CtxVarType<?> type = CtxVarTypes.REGISTRY.get(typeId);

        if(type != null)
        {
            unpack(ctx, key, type, entry.get("value"));
        }
    }

    private static <T> void unpack(SpellContext ctx, String key, CtxVarType<T> type, Tag valueTag)
    {
        type.getImmCodec().parse(NbtOps.INSTANCE, valueTag).result().ifPresent(value -> ctx.setCtxVar(type, key, value));
    }
}
