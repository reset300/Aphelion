#version 150

in vec3 Position;
in vec2 UV0;
in vec4 Color;

uniform mat4 InvProjMat;
uniform mat4 InvViewMat;

out vec4 vertColor;
out vec3 worldDir;

void main() {
    gl_Position = vec4(Position.xy, 0.9999, 1.0);
    vertColor = Color;

    vec4 rayClip = vec4(Position.xy, -1.0, 1.0);
    vec4 rayView = InvProjMat * rayClip;
    rayView = vec4(rayView.xy, -1.0, 0.0);
    worldDir = normalize((InvViewMat * rayView).xyz);
}