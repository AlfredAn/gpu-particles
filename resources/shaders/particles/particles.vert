#version 150 core

uniform mat4 u_Matrix;

in vec4 in_Position;
in vec4 in_Color;

out vec4 pass_Color;

void main()
{
    gl_Position = u_Matrix * in_Position;
    pass_Color = in_Color;
}