package de.cas_ual_ty.spells.spell.action.attribute;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.registers.CtxVarTypes;
import de.cas_ual_ty.spells.registers.SpellActionTypes;
import de.cas_ual_ty.spells.spell.action.SpellAction;
import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.context.SpellContext;
import de.cas_ual_ty.spells.spell.variable.DynamicCtxVar;
import de.cas_ual_ty.spells.util.ParamNames;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

public class CheckTagAction extends SpellAction
{
    public static Codec<CheckTagAction> makeCodec(SpellActionType<CheckTagAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                Codec.STRING.fieldOf(ParamNames.paramStringImm("registry_id")).forGetter(CheckTagAction::getRegistry),
                CtxVarTypes.STRING.get().refCodec().fieldOf(ParamNames.paramString("tag")).forGetter(CheckTagAction::getTag),
                CtxVarTypes.STRING.get().refCodec().fieldOf(ParamNames.paramString("entry_id")).forGetter(CheckTagAction::getEntry)
        ).apply(instance, (activation, registry, tag, entry) -> new CheckTagAction(type, activation, registry, tag, entry)));
    }
    
    public static CheckTagAction make(Object activation, String registry, DynamicCtxVar<String> tag, DynamicCtxVar<String> entry)
    {
        return new CheckTagAction(SpellActionTypes.CHECK_TAG.get(), activation.toString(), registry, tag, entry);
    }
    
    protected String registry;
    protected DynamicCtxVar<String> tag;
    protected DynamicCtxVar<String> entry;
    
    public CheckTagAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public CheckTagAction(SpellActionType<?> type, String activation, String registry, DynamicCtxVar<String> tag, DynamicCtxVar<String> entry)
    {
        super(type, activation);
        this.registry = registry;
        this.tag = tag;
        this.entry = entry;
    }
    
    public String getRegistry()
    {
        return registry;
    }
    
    public DynamicCtxVar<String> getTag()
    {
        return tag;
    }
    
    public DynamicCtxVar<String> getEntry()
    {
        return entry;
    }
    
    @Override
    protected void wasActivated(SpellContext ctx)
    {
        tag.getValue(ctx).ifPresent(tag ->
        {
            entry.getValue(ctx).ifPresent(entry ->
            {
                if(!isTag(registry, tag, entry))
                {
                    ctx.deactivate(activation);
                }
            });
        });
    }
    
    protected <V> boolean isTag(String registryRL, String tagRL, String entryRL)
    {
        Registry<V> registry = (Registry<V>) BuiltInRegistries.REGISTRY.get(new ResourceLocation(registryRL));
        
        if(registry == null)
        {
            return false;
        }
        
        ResourceKey<? extends Registry<V>> registryKey = registry.key();
        TagKey<V> tagKey = TagKey.create(registryKey, new ResourceLocation(tagRL));
        
        return registry.getTag(tagKey).map(tag -> tag.contains(Holder.direct(registry.get(new ResourceLocation(entryRL))))).orElse(false);
    }
}
