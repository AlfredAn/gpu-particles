#version 330 core

uniform sampler2D u_Sampler;
uniform mat4 u_Matrix;

in vec2 in_TexCoord;

out vec4 pass_Color;

vec3 hsv2rgb(vec3 c)
{
    const vec4 K = vec4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);
    vec3 p = abs(fract(c.xxx + K.xyz) * 6.0 - K.www);
    return c.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), c.y);
}

void main()
{
    vec4 particle = texture(u_Sampler, in_TexCoord);
    if (particle.x == 0 && particle.y == 0)
    {
        pass_Color = vec4(0, 0, 0, 0);
        gl_Position = vec4(0, 0, 0, 0);
        return;
    }
    
    gl_Position = u_Matrix * vec4(particle.x, particle.y, 0, 1);
    
    float v = distance(vec2(0, 0), particle.zw);
    pass_Color = vec4(hsv2rgb(vec3((1 - 1 / (1 + v*4)) * 0.8, 0.9, 0.25)), 1);
}