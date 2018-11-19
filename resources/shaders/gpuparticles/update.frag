#version 330 core

uniform sampler2D u_Sampler;
uniform float u_TimeStep;
uniform int u_Iterations;

in vec2 pass_TexCoord;

out vec4 out_Color;

const float G = -0.005;
const vec2 grav = vec2(0, 0);
const int maxIterationFactor = 2048;
const float errorFactor = 16;

//xy = particle x/y
//zw = particle x/y velocity

void applyPhysics(in float timeStep, in float dist, in vec4 before, out vec4 after)
{
    float cx = before.x - grav.x;
    if (cx == 0)
    {
        cx = 0.00001;
    }
    float ang = atan(before.y - grav.y, cx);
    float force = timeStep * G / dist / dist;
    
    after.z = before.z + force * cos(ang);
    after.w = before.w + force * sin(ang) - 0.00 * timeStep;
    
    after.xy = before.xy + before.zw * timeStep;
}

void main()
{
    vec4 particle = texture(u_Sampler, pass_TexCoord);
    
    float errorMargin = errorFactor * u_TimeStep / u_Iterations;
    float errorDist = errorMargin * distance(vec2(0, 0), particle.zw);
    float dist = distance(grav, particle.xy);
    
    int iterations = u_Iterations;
    if (dist < errorDist)
    {
        iterations = int(u_Iterations * min(errorDist / dist, maxIterationFactor));
    }
    else
    {
        iterations = u_Iterations;
    }
    float timeStep = u_TimeStep / iterations;
    vec4 particle2;
    
    for (int i = 0; i < iterations; i++)
    {
        applyPhysics(timeStep, dist, particle, particle2);
        dist = distance(grav, particle2.xy);
        applyPhysics(timeStep, dist, particle2, particle);
        dist = distance(grav, particle.xy);
    }
    out_Color = particle;
}