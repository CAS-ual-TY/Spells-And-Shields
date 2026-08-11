package de.cas_ual_ty.spells.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.cas_ual_ty.spells.registers.PlayerAnimations;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;

import java.util.concurrent.CompletableFuture;

/**
 * Generates {@code assets/spells_and_shields/player_animations/*.json} - Player Animator's own GeckoLib-style
 * keyframe format, not a Minecraft {@link com.mojang.serialization.Codec} - PlayerAnimator's codec is
 * decode-only, and round-tripping through its Java objects would lossily convert degrees to radians and back,
 * so this builds the raw {@link JsonObject} tree directly instead, matching exactly what's hand-authored today.
 * <p>
 * Each spell's third-person/first-person animation pair is written together via {@link #writePair} - see its
 * own doc. Add new pairs directly in {@link #run}.
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
    protected static final String LEFT_ARM = "left_arm";
    protected static final String RIGHT_ITEM = "right_item";
    protected static final String LEFT_ITEM = "left_item";
    protected static final String RIGHT_LEG = "right_leg";
    protected static final String LEFT_LEG = "left_leg";
    protected static final String BODY = "body";

    protected static final String LINEAR = "linear";
    protected static final String CONSTANT = "constant";

    protected static final String EASE_IN_SINE = "easeInSine";
    protected static final String EASE_OUT_SINE = "easeOutSine";
    protected static final String EASE_IN_OUT_SINE = "easeInOutSine";

    protected static final String EASE_IN_CUBIC = "easeInCubic";
    protected static final String EASE_OUT_CUBIC = "easeOutCubic";
    protected static final String EASE_IN_OUT_CUBIC = "easeInOutCubic";

    protected static final String EASE_IN_QUAD = "easeInQuad";
    protected static final String EASE_OUT_QUAD = "easeOutQuad";
    protected static final String EASE_IN_OUT_QUAD = "easeInOutQuad";

    protected static final String EASE_IN_QUART = "easeInQuart";
    protected static final String EASE_OUT_QUART = "easeOutQuart";
    protected static final String EASE_IN_OUT_QUART = "easeInOutQuart";

    protected static final String EASE_IN_QUINT = "easeInQuint";
    protected static final String EASE_OUT_QUINT = "easeOutQuint";
    protected static final String EASE_IN_OUT_QUINT = "easeInOutQuint";

    protected static final String EASE_IN_EXPO = "easeInExpo";
    protected static final String EASE_OUT_EXPO = "easeOutExpo";
    protected static final String EASE_IN_OUT_EXPO = "easeInOutExpo";

    protected static final String EASE_IN_CIRC = "easeInCirc";
    protected static final String EASE_OUT_CIRC = "easeOutCirc";
    protected static final String EASE_IN_OUT_CIRC = "easeInOutCirc";

    // these three (plus STEP below) also accept an optional numeric "easingArgs" on the keyframe - only via the
    // object form ({"vector":[...], "easing":..., "easingArgs":[...]}), not the plain array shorthand
    protected static final String EASE_IN_BACK = "easeInBack";
    protected static final String EASE_OUT_BACK = "easeOutBack";
    protected static final String EASE_IN_OUT_BACK = "easeInOutBack";

    protected static final String EASE_IN_ELASTIC = "easeInElastic";
    protected static final String EASE_OUT_ELASTIC = "easeOutElastic";
    protected static final String EASE_IN_OUT_ELASTIC = "easeInOutElastic";

    protected static final String EASE_IN_BOUNCE = "easeInBounce";
    protected static final String EASE_OUT_BOUNCE = "easeOutBounce";
    protected static final String EASE_IN_OUT_BOUNCE = "easeInOutBounce";

    protected static final String CATMULL_ROM = "catmullRom";
    protected static final String STEP = "step";

    protected final PackOutput.PathProvider pathProvider;

    public PlayerAnimationsGen(PackOutput packOutput)
    {
        this.pathProvider = packOutput.createPathProvider(PackOutput.Target.RESOURCE_PACK, "player_animations");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output)
    {
        return CompletableFuture.allOf(
                writePairWithOff(output, PlayerAnimations.STAB, PlayerAnimations.STAB_OFF, PlayerAnimations.STAB_1P, PlayerAnimations.STAB_1P_OFF, stab(), reverse(stab()), stab1p(), reverse(stab1p()))
        );
    }

    /**
     * Writes one third-person/first-person animation pair into a single file, named after the third-person id
     * (see {@link PlayerAnimations}' own doc - PlayerAnimator keys by declared {@code name}, not filename, so
     * the filename itself is only for our own organization).
     */
    protected CompletableFuture<?> writePair(CachedOutput output, ResourceLocation thirdPersonId, ResourceLocation firstPersonId, JsonObject thirdPerson, JsonObject firstPerson)
    {
        JsonObject animations = new JsonObject();
        animations.add(thirdPersonId.getPath(), thirdPerson);
        animations.add(firstPersonId.getPath(), firstPerson);

        JsonObject file = new JsonObject();
        file.addProperty("format_version", "1.8.0");
        file.add("animations", animations);

        return DataProvider.saveStable(output, file, pathProvider.json(thirdPersonId));
    }

    protected CompletableFuture<?> writePairWithOff(CachedOutput output, ResourceLocation thirdPersonId, ResourceLocation thirdPersonOffId, ResourceLocation firstPersonId, ResourceLocation firstPersonOffId, JsonObject thirdPerson, JsonObject thirdPersonOff, JsonObject firstPerson, JsonObject firstPersonOff)
    {
        JsonObject animations = new JsonObject();
        animations.add(thirdPersonId.getPath(), thirdPerson);
        animations.add(thirdPersonOffId.getPath(), thirdPersonOff);
        animations.add(firstPersonId.getPath(), firstPerson);
        animations.add(firstPersonOffId.getPath(), firstPersonOff);

        JsonObject file = new JsonObject();
        file.addProperty("format_version", "1.8.0");
        file.add("animations", animations);

        return DataProvider.saveStable(output, file, pathProvider.json(thirdPersonId));
    }

    protected JsonObject stab()
    {
        JsonObject animation = animation(0.5, false);

        JsonObject bones = new JsonObject();
        bones.add(RIGHT_ARM, bone(ROTATION, track(
                "0.0", eased(0, 0, 0, EASE_OUT_QUAD),
                "0.15", vec(-90, 10, 5),
                "0.35", eased(-90, 10, 5, EASE_IN_QUAD),
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
        JsonObject animation = animation(1.6, false);

        JsonObject rightArm = new JsonObject();
        rightArm.add(ROTATION, track(
                "0.0", eased(0, 0, 0, EASE_OUT_QUAD),
                "0.10", vec(-90, 10, 5),
                "0.35", eased(-90, 10, 5, LINEAR),
                "0.6", vec(0, 0, 0)
        ));
        rightArm.add(POSITION, track(
                "0.0", vec(0, 0, 2),
                "0.10", vec(0, 0, 2),
                "0.20", vec(0, 0, -7),
                "0.35", eased(0, 0, -6, LINEAR),
                "0.6", vec(0, 0, 0)
        ));

        JsonObject bones = new JsonObject();
        bones.add(RIGHT_ARM, rightArm);
        animation.add("bones", bones);

        return animation;
    }

    /**
     * Mirrors an animation across the left-right plane - right_arm/right_item/right_leg swap with their left_*
     * counterparts (any other bone, eg. {@code body}, keeps its own name). Y always negates on both tracks.
     * {@code position}'s X (left-right offset) also negates, Z (forward/back reach - the same direction for
     * both arms) doesn't. {@code rotation} is the opposite: X (pitch - both arms raise/lower the same way)
     * doesn't negate, Z (roll) does. Deep-copies everything else (easing, animation_length, loop, ...) unchanged,
     * so {@code reverse(stab1p())} stands in for a hand-authored {@code stab1p_off()} without duplicating the
     * whole track by hand.
     */
    protected static JsonObject reverse(JsonObject animation)
    {
        JsonObject result = new JsonObject();
        result.addProperty("animation_length", animation.get("animation_length").getAsDouble());
        result.addProperty("loop", animation.get("loop").getAsBoolean());

        JsonObject bones = new JsonObject();

        for(var boneEntry : animation.getAsJsonObject("bones").entrySet())
        {
            bones.add(mirrorBoneName(boneEntry.getKey()), mirrorBone(boneEntry.getValue().getAsJsonObject()));
        }

        result.add("bones", bones);

        return result;
    }

    protected static String mirrorBoneName(String name)
    {
        return switch(name)
        {
            case RIGHT_ARM -> LEFT_ARM;
            case LEFT_ARM -> RIGHT_ARM;
            case RIGHT_ITEM -> LEFT_ITEM;
            case LEFT_ITEM -> RIGHT_ITEM;
            case RIGHT_LEG -> LEFT_LEG;
            case LEFT_LEG -> RIGHT_LEG;
            default -> name;
        };
    }

    protected static JsonObject mirrorBone(JsonObject bone)
    {
        JsonObject result = new JsonObject();

        for(var trackEntry : bone.entrySet())
        {
            String type = trackEntry.getKey();

            if(POSITION.equals(type))
            {
                // left-right offset (X) flips, forward/back reach (Z) doesn't
                result.add(type, mirrorTrack(trackEntry.getValue().getAsJsonObject(), true, false));
            }
            else if(ROTATION.equals(type))
            {
                // pitch (X) doesn't flip, roll (Z) does
                result.add(type, mirrorTrack(trackEntry.getValue().getAsJsonObject(), false, true));
            }
            else
            {
                result.add(type, trackEntry.getValue());
            }
        }

        return result;
    }

    protected static JsonObject mirrorTrack(JsonObject track, boolean negateX, boolean negateZ)
    {
        JsonObject result = new JsonObject();

        for(var keyframeEntry : track.entrySet())
        {
            result.add(keyframeEntry.getKey(), mirrorKeyframe(keyframeEntry.getValue(), negateX, negateZ));
        }

        return result;
    }

    protected static JsonElement mirrorKeyframe(JsonElement keyframe, boolean negateX, boolean negateZ)
    {
        if(keyframe.isJsonArray())
        {
            return mirrorVec(keyframe.getAsJsonArray(), negateX, negateZ);
        }

        JsonObject source = keyframe.getAsJsonObject();
        JsonObject result = new JsonObject();

        for(var fieldEntry : source.entrySet())
        {
            result.add(fieldEntry.getKey(), "vector".equals(fieldEntry.getKey()) ? mirrorVec(fieldEntry.getValue().getAsJsonArray(), negateX, negateZ) : fieldEntry.getValue());
        }

        return result;
    }

    // Y (index 1) always negates on both tracks. X (index 0) and Z (index 2) are complementary between the two
    // track types - see mirrorBone for which is which.
    protected static JsonArray mirrorVec(JsonArray original, boolean negateX, boolean negateZ)
    {
        double x = original.get(0).getAsDouble();
        double y = original.get(1).getAsDouble();
        double z = original.get(2).getAsDouble();
        return vec(negateX ? -x : x, -y, negateZ ? -z : z);
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
