#version 150 core

uniform mat4 u_Matrix;
uniform float u_Time;

in float in_Radius;

out vec4 pass_Color;

vec3 hsv2rgb(vec3 c)
{
    const vec4 K = vec4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);
    vec3 p = abs(fract(c.xxx + K.xyz) * 6.0 - K.www);
    return c.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), c.y);
}

void main()
{
    float invRadius = 1 - in_Radius;
    float angle = u_Time * invRadius;
    gl_Position = u_Matrix * vec4(in_Radius * cos(angle), in_Radius * sin(angle), 0, 1);
    
    pass_Color = vec4(hsv2rgb(vec3(invRadius, 1, 1)), 1f/64);
}