#version 150

in vec2 texCoord;
in vec4 vertexColor;

out vec4 fragColor;

void main() {
  vec2 p = texCoord * 2.0 - 1.0;
  float r = length(p);
  float axial = 1.0 - texCoord.y;

  float core = exp(-r * r * 2.3);
  float edge = 1.0 - smoothstep(0.7, 1.18, r);
  float lengthFade = smoothstep(0.02, 0.12, axial) * (1.0 - smoothstep(0.82, 1.0, axial));

  float a = core * edge * lengthFade * vertexColor.a;

  fragColor = vec4(a, a * 0.55, a * 0.18, a);
}