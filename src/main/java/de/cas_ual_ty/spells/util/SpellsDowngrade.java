package de.cas_ual_ty.spells.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.serialization.Codec;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Utility methods for the 1.19.2 -> 1.18.2 downgrade
 */
public class SpellsDowngrade
{
    public static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();
    
    public static final Codec<Vec3> VEC3_CODEC = Codec.DOUBLE.listOf().comapFlatMap(
            list -> Util.fixedSize(list, 3).map((list3) -> new Vec3(list3.get(0), list3.get(1), list3.get(2))),
            vec3 -> List.of(vec3.x(), vec3.y(), vec3.z())
    );
    
    public static TranslatableComponent translatable(String s, Object... params)
    {
        return new TranslatableComponent(s, params);
    }
    
    public static TextComponent literal(String s)
    {
        return new TextComponent(s);
    }
    
    public static TextComponent empty()
    {
        return (TextComponent) TextComponent.EMPTY;
    }
    
    public static boolean isEmpty(Component component)
    {
        return TextComponent.EMPTY.equals(component);
    }
}
