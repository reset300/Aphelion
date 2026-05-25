package com.BreadRes.astronautics.client.screenshake;

import net.minecraft.world.phys.Vec3;

public class ScreenshakeBuilder {

    private int duration = 20;
    private float startingStrength = 1f, middleStrength = 0f, endingStrength = 0f;
    private Easing startingCurve = Easing.LINEAR, endingCurve = Easing.LINEAR;
    private float coefficient = 1.0f;
    private Vec3 center = null;
    private float falloffDistance = 16f;
    private Easing falloffCurve = Easing.QUAD_OUT;

    public static ScreenshakeBuilder create() { return new ScreenshakeBuilder(); }

    public ScreenshakeBuilder duration(int duration) { this.duration = duration; return this; }
    public ScreenshakeBuilder strength(float s) { return strength(s, s, 0); }
    public ScreenshakeBuilder strength(float start, float mid, float end) {
        this.startingStrength = start; this.middleStrength = mid; this.endingStrength = end; return this;
    }
    public ScreenshakeBuilder easing(Easing start, Easing end) {
        this.startingCurve = start; this.endingCurve = end; return this;
    }
    public ScreenshakeBuilder coefficient(float c) { this.coefficient = c; return this; }
    public ScreenshakeBuilder at(Vec3 center, float falloff) {
        this.center = center; this.falloffDistance = falloff; return this;
    }
    public ScreenshakeBuilder at(Vec3 center, float falloff, Easing curve) {
        this.center = center; this.falloffDistance = falloff; this.falloffCurve = curve; return this;
    }

    public ScreenshakeInstance build() {
        return new ScreenshakeInstance(duration, startingStrength, middleStrength, endingStrength,
                startingCurve, endingCurve, coefficient, center, falloffDistance, falloffCurve);
    }
}