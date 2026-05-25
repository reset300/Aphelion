package com.BreadRes.astronautics.client.screenshake;

import net.minecraft.client.Camera;
import net.minecraft.world.phys.Vec3;

public class ScreenshakeInstance {

    private final int duration;
    private final float startingStrength, middleStrength, endingStrength;
    private final Easing startingCurve, endingCurve;
    private final float coefficient;
    private final Vec3 center;
    private final float falloffDistance;
    private final Easing falloffCurve;

    private int progress;
    private boolean expired;

    public ScreenshakeInstance(int duration, float startingStrength, float middleStrength, float endingStrength,
                                Easing startingCurve, Easing endingCurve, float coefficient,
                                Vec3 center, float falloffDistance, Easing falloffCurve) {
        this.duration = duration;
        this.startingStrength = startingStrength;
        this.middleStrength = middleStrength;
        this.endingStrength = endingStrength;
        this.startingCurve = startingCurve;
        this.endingCurve = endingCurve;
        this.coefficient = coefficient;
        this.center = center;
        this.falloffDistance = falloffDistance;
        this.falloffCurve = falloffCurve;
    }

    public void tick() {
        if (progress < duration) {
            progress++;
            if (progress >= duration) expired = true;
        }
    }

    public float getStrength(Camera camera) {
        float strength = getRawStrength();
        if (center == null) return strength;
        double distance = camera.getPosition().distanceTo(center);
        if (distance > falloffDistance || falloffDistance == 0) return 0;
        float eased = falloffCurve.ease((float)(distance / falloffDistance));
        return strength * (1f - eased);
    }

    private float getRawStrength() {
        if (expired) return 0;
        float pct = (progress * coefficient) / (float) duration;
        if (endingStrength != middleStrength) {
            if (pct >= 0.5f)
                return endingCurve.lerp((pct - 0.5f) * 2f, middleStrength, endingStrength);
            else
                return startingCurve.lerp(pct * 2f, startingStrength, middleStrength);
        }
        return startingCurve.lerp(pct, startingStrength, middleStrength);
    }

    public boolean isExpired() { return expired; }
}