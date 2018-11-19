#version 150 core

in vec4 pass_Color;

out vec4 out_Color;

void main()
{
    out_Color = vec4(1f/256, 1f/256, 1f/256, 1);
}