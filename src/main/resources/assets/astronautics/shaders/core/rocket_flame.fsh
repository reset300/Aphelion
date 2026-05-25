#version 150

uniform float Time;
uniform float Throttle;
uniform float FlameLength;
uniform float FlameRadius;
uniform float CoreRadius;
uniform float OuterStrength;
uniform float Brightness;

in vec2 texCoord;
in vec4 vertexColor;
in vec3 localPos;

out vec4 fragColor;

float hash21(vec2 p){
  p = fract(p * vec2(123.34,456.21));
  p += dot(p,p+45.32);
  return fract(p.x*p.y);
}

float noise2(vec2 p){
  vec2 i=floor(p);
  vec2 f=fract(p);
  float a=hash21(i);
  float b=hash21(i+vec2(1,0));
  float c=hash21(i+vec2(0,1));
  float d=hash21(i+vec2(1,1));
  vec2 u=f*f*(3.0-2.0*f);
  return mix(mix(a,b,u.x),mix(c,d,u.x),u.y);
}

float fbm(vec2 p){
  float v=0.0, a=0.5;
  for(int i=0;i<5;i++){
    v+=noise2(p)*a;
    p*=2.02; a*=0.5;
  }
  return v;
}

float saturate(float x){ return clamp(x,0.0,1.0); }
vec3 tonemap(vec3 c){ return c/(1.0+c*0.55); }

void main(){
  vec2 uv = texCoord;
  float fromNozzle = 1.0 - uv.y;

  float rWorld = length(localPos.xz);
  float angle  = atan(localPos.z, localPos.x);

  float throat     = smoothstep(0.0, 0.1, fromNozzle);
  float throatTight = mix(0.15, 1.0, throat);

  float expansion = pow(fromNozzle, 0.62);
  float radius = mix(CoreRadius * 1.6, FlameRadius, expansion) * throatTight;

  float shockWave = sin(fromNozzle * 28.0 - Time * 16.0) * exp(-fromNozzle * 1.8);
  float shock = 0.5 + 0.5 * shockWave;

  float n1 = fbm(vec2(angle * 1.8 + Time * 0.25, fromNozzle * 7.0  - Time * 2.2));
  float n2 = fbm(vec2(angle * 3.5 - Time * 0.9,  fromNozzle * 15.0 + Time * 3.1));
  float n3 = fbm(vec2(angle * 0.9 + Time * 1.1,  fromNozzle * 4.0  - Time * 1.5));

  float jitter =
  sin(Time * 130.0 + fromNozzle * 45.0) * 0.08 +
  sin(Time * 270.0 + fromNozzle * 95.0) * 0.04;

  float turbulence = 0.82 + 0.28*n1 + 0.14*n2 + 0.08*n3 + jitter;

  float x = rWorld / max(FlameRadius, 0.001);
  float warpedX = x * (1.0 + (turbulence - 0.82) * 0.18);
  float r = warpedX / max(radius / max(FlameRadius, 0.001), 0.001);
  r *= mix(1.0, 0.78, shock * 0.3);

  float inner = exp(-(r*r) / (0.008 + 0.018*Throttle));
  float mid   = exp(-(r*r) / (0.09  + 0.22 *Throttle));
  float outer = exp(-(r*r) / (0.38  + 0.65 *Throttle));
  float glow  = exp(-(r*r) / (1.2   + 1.5  *Throttle));

  float shell =
  smoothstep(0.3, 0.85, r)
  * (1.0 - smoothstep(0.85, 1.25, r))
  * outer * 0.9;

  float fadeIn  = smoothstep(0.0,  0.04, fromNozzle);
  float fadeOut = 1.0 - smoothstep(0.78, 1.0, fromNozzle);
  float axial   = fadeIn * fadeOut;

  float nozzleHot  = exp(-fromNozzle * 0.8);
  float coreBreak  = mix(1.0, turbulence * 1.15, smoothstep(0.2, 0.85, fromNozzle));
  float tailSpread = mix(1.0, 1.35, smoothstep(0.5, 1.0, fromNozzle));

  inner *= axial * nozzleHot * coreBreak;
  mid   *= axial * (0.65 + turbulence * 0.45) * tailSpread;
  outer *= axial * OuterStrength * (0.55 + turbulence * 0.65) * tailSpread;
  glow  *= axial * OuterStrength * 0.35;
  shell *= axial * OuterStrength;

  float diamond =
  pow(max(0.0, 1.0 - abs(r - shock * 0.18) * 6.0), 3.5)
  * exp(-fromNozzle * 2.5)
  * smoothstep(0.02, 0.15, Throttle);

  float spear = exp(-(r*r) / 0.006) * exp(-fromNozzle * 1.4) * nozzleHot;

  vec3 white   = vec3(1.0,  1.0,  1.0);
  vec3 iceBlue = vec3(0.72, 0.88, 1.0);
  vec3 blue    = vec3(0.45, 0.65, 1.0);
  vec3 violet  = vec3(0.58, 0.28, 1.0);
  vec3 purple  = vec3(0.48, 0.15, 0.85);
  vec3 pink    = vec3(0.75, 0.25, 0.9);

  float axialBlend = smoothstep(0.0, 0.7, fromNozzle);
  vec3 coreColor = mix(white,  iceBlue, smoothstep(0.0, 0.4, fromNozzle));
  vec3 midColor  = mix(blue,   violet,  axialBlend);
  vec3 outerColor= mix(violet, purple,  axialBlend);
  vec3 glowColor = mix(pink,   purple,  axialBlend);

  float flicker =
  0.92 +
  0.06 * sin(Time * 180.0 + fromNozzle * 12.0) +
  0.03 * sin(Time * 420.0 + fromNozzle * 30.0) +
  0.02 * sin(Time * 730.0);

  vec3 color = vec3(0.0);
  color += coreColor  * inner  * 0.6  * flicker;
  color += midColor   * mid    * 1.4;
  color += outerColor * outer  * 0.8;
  color += glowColor  * glow   * 0.4;
  color += outerColor * shell  * 0.9;
  color += white      * diamond* 0.5;
  color += white      * spear * 0.7 * flicker;

  float alpha =
  inner * 0.5  +
  mid   * 0.55 +
  outer * 0.45 +
  glow  * 0.2  +
  shell * 0.4  +
  diamond * 0.2 +
  spear * 0.35;

  alpha *= (1.0 - smoothstep(1.1, 1.45, r));
  alpha *= smoothstep(0.01, 0.15, Throttle);
  alpha  = saturate(alpha);

  color *= Brightness;
  vec3 finalColor = tonemap(color);

  if(alpha < 0.004) discard;

  fragColor = vec4(finalColor, alpha) * vertexColor;
}