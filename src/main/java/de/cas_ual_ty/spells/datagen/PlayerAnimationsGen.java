package de.cas_ual_ty.spells.datagen;

import de.cas_ual_ty.spells.animation.AnimationSection;
import de.cas_ual_ty.spells.animation.EaseType;
import de.cas_ual_ty.spells.animation.Keyframe;
import de.cas_ual_ty.spells.animation.PlayerAnimation;
import de.cas_ual_ty.spells.registers.PlayerAnimations;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Map;

public class PlayerAnimationsGen
{
    protected String modId;
    protected final BootstrapContext<PlayerAnimation> context;

    public PlayerAnimationsGen(String modId, BootstrapContext<PlayerAnimation> context)
    {
        this.modId = modId;
        this.context = context;
        addPlayerAnimations();
    }

    public void addPlayerAnimations()
    {
        addAnimation("test_stab", testStab());
    }

    public void addAnimation(String key, PlayerAnimation animation)
    {
        context.register(ResourceKey.create(PlayerAnimations.REGISTRY_KEY, ResourceLocation.fromNamespaceAndPath(modId, key)), animation);
    }

    protected PlayerAnimation testStab()
    {
        AnimationSection firstPerson = section(Map.of(
                "main_hand", List.of(
                        keyframe(0, vec3(0.0, 0.0, 0.0), vec3(0.0, 0.0, 0.0)),
                        keyframe(3, vec3(0.0, 0.05, -0.9), vec3(-0.5, 0.03, 0.05)),
                        keyframe(5, vec3(0.0, 0.05, -0.9), vec3(-0.5, 0.03, 0.05), EaseType.OUT),
                        keyframe(10, vec3(0.0, 0.0, 0.0), vec3(0.0, 0.0, 0.0), EaseType.IN)
                )
        ));

        AnimationSection thirdPerson = section(Map.of(
                "right_arm", List.of(
                        keyframe(0, vec3(0.0, 0.0, 0.0)),
                        keyframe(4, vec3(0.0, 0.0, -2.0), vec3(-1.9, -0.1, 0.1), EaseType.OUT),
                        keyframe(5, vec3(0.0, 0.0, -2.0), vec3(-1.9, -0.1, 0.1)),
                        keyframe(10, vec3(0.0, 0.0, 0.0), vec3(0.0, 0.0, 0.0), EaseType.BOTH)
                ),
                "body", List.of(
                        keyframe(0, vec3(0.0, 0.0, 0.0)),
                        keyframe(4, vec3(0.0, 0.2, 0.0), EaseType.OUT),
                        keyframe(5, vec3(0.0, 0.2, 0.0)),
                        keyframe(10, vec3(0.0, 0.0, 0.0), EaseType.IN)
                )
        ));

        return new PlayerAnimation(firstPerson, thirdPerson);
    }

    // ----- shared building blocks -----

    protected static AnimationSection section(Map<String, List<Keyframe>> parts)
    {
        return new AnimationSection(parts);
    }

    protected static Vec3 vec3(double x, double y, double z)
    {
        return new Vec3(x, y, z);
    }

    protected static Keyframe keyframe(int time, Vec3 translate, Vec3 rotate, EaseType ease)
    {
        return new Keyframe(time, translate, rotate, Keyframe.IDENTITY_SCALE, ease);
    }

    protected static Keyframe keyframe(int time, Vec3 translate, Vec3 rotate)
    {
        return keyframe(time, translate, rotate, EaseType.NONE);
    }

    // rotate-only keyframes (translate defaults to zero) - matches the third-person "body" track, which never
    // moves the part's pivot, only rotates it
    protected static Keyframe keyframe(int time, Vec3 rotate, EaseType ease)
    {
        return keyframe(time, Vec3.ZERO, rotate, ease);
    }

    protected static Keyframe keyframe(int time, Vec3 rotate)
    {
        return keyframe(time, Vec3.ZERO, rotate, EaseType.NONE);
    }

    public String getName()
    {
        return "Spells & Shields Player Animation Files";
    }
}
