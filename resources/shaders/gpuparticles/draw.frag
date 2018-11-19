#version 330 core

in vec4 pass_Color;

out vec4 out_Color;

void main()
{
    if (pass_Color.a == 0)
    {
        discard;
    }
    out_Color = pass_Color;
}