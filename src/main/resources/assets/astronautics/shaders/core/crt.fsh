#version 150

uniform sampler2D Sampler0;
uniform float GameTime;
uniform float ScreenWidth;
uniform float ScreenHeight;

in vec2 texCoord;
out vec4 fragColor;

const float warp_amount = 0.08;
const float vignette_strength = -0.01154;

vec2 warp(vec2 uv) {
    vec2 c = uv * 2.0 - 1.0;
    c = tan(c * warp_amount) / tan(warp_amount);
    return c * 0.5 + 0.5;
}

float border(vec2 uv) {
    float edge = 0.07;
    vec2 d = min(uv, 1.0 - uv);
    float b = smoothstep(0.0, edge, min(d.x, d.y));
    return b;
}

float vignette(vec2 uv) {
    vec2 p = uv * (1.0 - uv);
    float v = p.x * p.y * 20.0;

    return mix(0.3, 1.0, pow(v, vignette_strength));
}

void main() {
    vec2 uv = texCoord;
    vec2 wuv = warp(uv);

    wuv = clamp(wuv, 0.001, 0.999);

    if (wuv.x < 0.0 || wuv.x > 1.0 || wuv.y < 0.0 || wuv.y > 1.0) {
        fragColor = vec4(0.0, 0.0, 0.0, 1.0);
        return;
    }

    vec3 col = texture(Sampler0, wuv).rgb;

    float scan = sin(wuv.y * 1200.0);
    col *= 1.0 - scan * 0.12;

    float shift = 0.002;
    col.r = texture(Sampler0, wuv + vec2(shift, 0)).r;
    col.b = texture(Sampler0, wuv - vec2(shift, 0)).b;

    float n = fract(sin(dot(wuv * GameTime, vec2(12.9898,78.233))) * 43758.5453);
    col += (n - 0.5) * 0.03;

    float v = vignette(wuv);
    float b = border(wuv);

    col *= v * b;

    fragColor = vec4(col, 1.0);
}