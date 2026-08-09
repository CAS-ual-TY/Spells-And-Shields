package de.cas_ual_ty.spells.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.cas_ual_ty.spells.SpellsAndShields;
import de.cas_ual_ty.spells.registers.PlayerAnimations;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;

import java.util.concurrent.CompletableFuture;

/**
 * Generates {@code assets/spells_and_shields/player_animations/stab.json} - Player Animator's own GeckoLib-style
 * keyframe format, not a Minecraft {@link com.mojang.serialization.Codec} - PlayerAnimator's codec is
 * decode-only, and round-tripping through its Java objects would lossily convert degrees to radians and back,
 * so this builds the raw {@link JsonObject} tree directly instead, matching exactly what's hand-authored today.
 * <p>
 * Both {@link PlayerAnimations#STAB} and {@link PlayerAnimations#STAB_1P} live in this one file - see
 * that class' doc: PlayerAnimator keys animations by their own declared {@code name}, not filename, so multiple
 * independent animations can freely share one file with no interaction between them.
 * <p>
 * A segment's easing is anchored to its <em>departing</em> keyframe, not the one it arrives at (verified against
 * PlayerAnimator's own {@code getValueFromKeyframes}, which defaults {@code isEasingBefore} to {@code false}) -
 * a keyframe with no explicit easing silently becomes a hard step ({@code Ease.CONSTANT}), so every keyframe
 * that starts a segment with actual motion needs its own {@code easing} set explicitly.
 */
public class PlayerAnimationsGen implements DataProvider
{
    protected static final String ROTATION = "rotation";
    protected static final String POSITION = "position";

    protected static final String RIGHT_ARM = "right_arm";
    protected static final String BODY = "body";

    protected static final String EASE_OUT_QUAD = "easeOutQuad";
    protected static final String EASE_IN_QUAD = "easeInQuad";

    protected final PackOutput.PathProvider pathProvider;

    public PlayerAnimationsGen(PackOutput packOutput)
    {
        this.pathProvider = packOutput.createPathProvider(PackOutput.Target.RESOURCE_PACK, "player_animations");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output)
    {
        JsonObject animations = new JsonObject();
        animations.add(PlayerAnimations.STAB.getPath(), stab());
        animations.add(PlayerAnimations.STAB_1P.getPath(), stab1p());

        JsonObject file = new JsonObject();
        file.addProperty("format_version", "1.8.0");
        file.add("animations", animations);

        ResourceLocation fileId = ResourceLocation.fromNamespaceAndPath(SpellsAndShields.MOD_ID, "stab");
        return DataProvider.saveStable(output, file, pathProvider.json(fileId));
    }

    protected JsonObject stab()
    {
        JsonObject animation = animation(0.5, false);

        JsonObject bones = new JsonObject();
        bones.add(RIGHT_ARM, bone(ROTATION, track(
                "0.0", eased(0, 0, 0, EASE_OUT_QUAD),
                "0.15", vec(-100, 10, 5),
                "0.35", eased(-100, 10, 5, EASE_IN_QUAD),
                "0.5", vec(0, 0, 0)
        )));
        bones.add(BODY, bone(ROTATION, track(
                "0.0", eased(0, 0, 0, EASE_OUT_QUAD),
                "0.15", vec(0, -20, 0),
                "0.35", eased(0, -20, 0, EASE_IN_QUAD),
                "0.5", vec(0, 0, 0)
        )));
        animation.add("bones", bones);

        return animation;
    }

    protected JsonObject stab1p()
    {
        JsonObject animation = animation(0.6, false);

        JsonObject rightArm = new JsonObject();
        rightArm.add(ROTATION, track(
                "0.0", eased(0, 0, 0, EASE_OUT_QUAD),
                "0.15", vec(-100, 10, 5),
                "0.35", eased(-100, 10, 5, EASE_IN_QUAD),
                "0.5", vec(0, 0, 0)
        ));
        rightArm.add(POSITION, track(
                "0.15", vec(0, 0, -7),
                "0.35", eased(0, 0, -7, EASE_IN_QUAD),
                "0.5", vec(0, 0, 0)
        ));

        JsonObject bones = new JsonObject();
        bones.add(RIGHT_ARM, rightArm);
        animation.add("bones", bones);

        return animation;
    }

    protected static JsonObject animation(double lengthSeconds, boolean loop)
    {
        JsonObject animation = new JsonObject();
        animation.addProperty("animation_length", lengthSeconds);
        animation.addProperty("loop", loop);
        return animation;
    }

    protected static JsonObject bone(String type, JsonObject track)
    {
        JsonObject bone = new JsonObject();
        bone.add(type, track);
        return bone;
    }

    protected static JsonObject track(Object... tickValuePairs)
    {
        JsonObject track = new JsonObject();

        for(int i = 0; i < tickValuePairs.length; i += 2)
        {
            track.add((String) tickValuePairs[i], (JsonElement) tickValuePairs[i + 1]);
        }

        return track;
    }

    protected static JsonArray vec(double x, double y, double z)
    {
        JsonArray array = new JsonArray();
        array.add(x);
        array.add(y);
        array.add(z);
        return array;
    }

    protected static JsonObject eased(double x, double y, double z, String easing)
    {
        JsonObject keyframe = new JsonObject();
        keyframe.add("vector", vec(x, y, z));
        keyframe.addProperty("easing", easing);
        return keyframe;
    }

    @Override
    public String getName()
    {
        return "Spells & Shields Player Animation Files";
    }
}
