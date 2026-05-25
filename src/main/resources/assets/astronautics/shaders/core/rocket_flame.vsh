#version 150

in vec3 Position;
in vec2 UV0;
in vec4 Color;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;

out vec2 texCoord;
out vec4 vertexColor;
out vec3 localPos;

void main() {
  texCoord    = UV0;
  vertexColor = vec4(1.0, 1.0, 1.0, Color.a);
  localPos    = vec3(Color.r * 4.0 - 2.0, Position.y, Color.g * 4.0 - 2.0);
  gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
}