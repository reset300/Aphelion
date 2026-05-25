#version 150

uniform sampler2D DiffuseSampler0;
uniform sampler2D HeatMaskSampler;
uniform sampler2D DiffuseDepthSampler;

uniform float GameTime;
uniform float DistortionStrength;
uniform float ThermalLift;
uniform float NoiseScale;
uniform float TimeScale;

in vec2 texCoord;
out vec4 fragColor;

float hash21(vec2 p) {
    p = fract(p * vec2(234.34, 435.345));
    p += dot(p, p + 34.23);
    return fract(p.x * p.y);
}

float noise2(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);

    float a = hash21(i);
    float b = hash21(i + vec2(1.0, 0.0));
    float c = hash21(i + vec2(0.0, 1.0));
    float d = hash21(i + vec2(1.0, 1.0));

    vec2 u = f * f * (3.0 - 2.0 * f);
    return mix(mix(a, b, u.x), mix(c, d, u.x), u.y);
}

float fbm(vec2 p) {
    float v = 0.0;
    float a = 0.5;

    for(int i = 0; i < 5; i++){
        v += noise2(p) * a;
        p *= 2.0;
        a *= 0.5;
    }

    return v;
}

void main() {
    vec2 uv = texCoord;

    vec4 mask = texture(HeatMaskSampler, uv);
    float m = clamp(mask.a * 2.0 + max(mask.r, max(mask.g, mask.b)), 0.0, 1.0);

    vec3 base = texture(DiffuseSampler0, uv).rgb;
    float depth = texture(DiffuseDepthSampler, uv).r;
    float depthFade = smoothstep(0.0, 0.02, depth);

    if (m <= 0.0001) {
        fragColor = vec4(base, 1.0);
        return;
    }

    float t = GameTime * TimeScale;

    vec2 p = uv * NoiseScale;

    float n1 = fbm(p + vec2(0.0, -t));
    float n2 = fbm(p * 1.7 + vec2(t, -t * 2.0));

    vec2 flow = vec2(n1 - n2, n2 - n1);
    flow.y -= ThermalLift * (0.6 + 0.4 * sin(t * 5.0));
    flow.x += sin(uv.y * 80.0 + t * 10.0) * 0.05;

    float strength = DistortionStrength * 8.0 * m;

    vec2 offset = flow * strength * (1.0 + m * 1.5) * depthFade;

    vec2 uv2 = clamp(uv + offset, vec2(0.001), vec2(0.999));

    vec3 refracted = texture(DiffuseSampler0, uv2).rgb;

    float radial = length((uv - vec2(0.5)) * vec2(1.0, 2.2));
    float falloff = smoothstep(0.8, 0.15, radial);

    vec3 color = mix(base, refracted, m * falloff);

    fragColor = vec4(color, 1.0);
}