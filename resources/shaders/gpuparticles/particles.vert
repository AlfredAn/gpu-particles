#version 330 core

uniform vec2 u_TexSize;

in vec4 in_Position;
in vec2 in_TexCoord;

out vec2 pass_TexCoord;

void main()
{
    gl_Position = in_Position;
    pass_TexCoord = in_TexCoord * vec2(1920f / u_TexSize.x, 1080f / u_TexSize.y);//(in_Position.xy + vec2(1, 1)) / 2;
}