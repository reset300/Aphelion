#version 150

uniform float Time;
uniform float Alpha;
in vec4 vertColor;
in vec3 worldDir;

out vec4 fragColor;

float hash3(vec3 p) {
    p = fract(p * vec3(443.8975, 397.2973, 491.1871));
    p += dot(p.zxy, p.yxz + 19.19);
    return fract(p.x * p.y * p.z);
}

float noise(vec3 p) {
    vec3 i = floor(p);
    vec3 f = fract(p);
    f = f*f*(3.0-2.0*f);
    return mix(
    mix(mix(hash3(i),            hash3(i+vec3(1,0,0)),f.x),
    mix(hash3(i+vec3(0,1,0)),hash3(i+vec3(1,1,0)),f.x),f.y),
    mix(mix(hash3(i+vec3(0,0,1)),hash3(i+vec3(1,0,1)),f.x),
    mix(hash3(i+vec3(0,1,1)),hash3(i+vec3(1,1,1)),f.x),f.y),
    f.z);
}

float stars(vec3 dir, float density, float size) {
    vec3 d = normalize(dir);
    float cellSize = 1.0 / density;
    vec3 cell = floor(d / cellSize);
    float result = 0.0;

    for (int x = -1; x <= 1; x++) {
        for (int y = -1; y <= 1; y++) {
            for (int z = -1; z <= 1; z++) {
                vec3 c = cell + vec3(x, y, z);
                float h = hash3(c);

                if (h > 0.93) {
                    vec3 starPos = normalize((c + vec3(
                    hash3(c * 1.3),
                    hash3(c * 1.7),
                    hash3(c * 2.1)
                    )) * cellSize);

                    float dist = length(d - starPos);
                    float brightness = smoothstep(size, 0.0, dist);
                    brightness *= 0.7 + 0.6 * h;

                    if (h > 0.995) {
                        brightness *= 3.0;
                    }

                    float twinkle = 0.8 + 0.2 * sin(Time * (hash3(c*3.7)*2.0+0.5) + h*6.28);
                    result += brightness * twinkle;
                }
            }
        }
    }
    return clamp(result, 0.0, 1.0);
}

void main() {
    vec3 dir = normalize(worldDir);
    vec3 color = vec3(0.0);

    float n = noise(dir * 2.5) * 0.5 + noise(dir * 5.0) * 0.3 + noise(dir * 10.0) * 0.2;
    n = smoothstep(0.55, 0.75, n);
    color += vec3(0.01, 0.01, 0.06) * n;

    float neb = noise(dir * 1.8);
    neb = smoothstep(0.65, 0.9, neb);
    neb *= 0.95 + 0.05 * sin(Time * 0.2);
    color += vec3(0.25, 0.05, 0.15) * neb * 0.25;

    vec3 dir2 = normalize(dir * vec3(1.0, 0.9, 1.0));

    color += vec3(1.0, 0.98, 0.95) * stars(dir, 6.0, 0.004) * 1.2;
    color += vec3(0.85, 0.90, 1.0) * stars(dir, 18.0, 0.0015) * 0.5;
    color += vec3(0.6, 0.7, 1.0)   * stars(dir2, 10.0, 0.003) * 0.7;

    float band = exp(-pow(dir.y * 4.0, 2.0));
    color += vec3(0.15, 0.12, 0.25) * band * 0.3;

    float bias = dir.y * 0.5 + 0.5;
    color *= mix(0.85, 1.1, bias);

    float grain = hash3(dir * 500.0) * 0.015;
    color += vec3(grain);

    float vignette = 1.0 - 0.15 * length(dir.xy);
    color *= vignette;

    fragColor = vec4(color, Alpha) * vertColor;
}