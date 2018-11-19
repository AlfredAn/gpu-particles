#version 330 core

in vec2 pass_TexCoord;

out vec4 out_Color;

// A single iteration of Bob Jenkins' One-At-A-Time hashing algorithm.
uint hash( uint x ) {
    x += ( x << 10u );
    x ^= ( x >>  6u );
    x += ( x <<  3u );
    x ^= ( x >> 11u );
    x += ( x << 15u );
    return x;
}

// Compound versions of the hashing algorithm I whipped together.
uint hash( uvec2 v ) { return hash( v.x ^ hash(v.y)                         ); }
uint hash( uvec3 v ) { return hash( v.x ^ hash(v.y) ^ hash(v.z)             ); }
uint hash( uvec4 v ) { return hash( v.x ^ hash(v.y) ^ hash(v.z) ^ hash(v.w) ); }

// Construct a float with half-open range [0:1] using low 23 bits.
// All zeroes yields 0.0, all ones yields the next smallest representable value below 1.0.
float floatConstruct( uint m ) {
    const uint ieeeMantissa = 0x007FFFFFu; // binary32 mantissa bitmask
    const uint ieeeOne      = 0x3F800000u; // 1.0 in IEEE binary32

    m &= ieeeMantissa;                     // Keep only mantissa bits (fractional part)
    m |= ieeeOne;                          // Add fractional part to 1.0

    float  f = uintBitsToFloat( m );       // Range [1:2]
    return f - 1.0;                        // Range [0:1]
}

// Pseudo-random value in half-open range [0:1].
float random( float x ) { return floatConstruct(hash(floatBitsToUint(x))); }
float random( vec2  v ) { return floatConstruct(hash(floatBitsToUint(v))); }
float random( vec3  v ) { return floatConstruct(hash(floatBitsToUint(v))); }
float random( vec4  v ) { return floatConstruct(hash(floatBitsToUint(v))); }

const float twopi = 6.283185307179586476925286766559;

void main()
{
    float dist = random(pass_TexCoord) / 2 + 0.5;
    float ang = random(pass_TexCoord + vec2(2, 0)) * twopi;
    
    out_Color.x = 1 * cos(ang);
    out_Color.y = 1 * sin(ang);
    out_Color.zw = out_Color.yx / 128;
    
    //exploding oval (grav-down=0, grav-center=-0.005)
    /*out_Color.x = 1 * cos(ang);
    out_Color.y = 1 * sin(ang);
    out_Color.zw = out_Color.yx / 128;*/
    
    //ring
    /*out_Color.x = dist * cos(ang);
    out_Color.y = dist * sin(ang);
    out_Color.zw = vec2(0.01, 0);*/
    
    //square
    /*out_Color.x = random(pass_TexCoord); //x
    out_Color.y = random(pass_TexCoord + vec2(2, 0)); //y
    out_Color.z = random(pass_TexCoord + vec2(4, 0)) / 16; //xv
    out_Color.w = random(pass_TexCoord + vec2(6, 0)) / 16; //yv
    
    out_Color.xy = out_Color.xy * 2 - vec2(1, 1);
    out_Color.zw = vec2(0, 0);//out_Color.zw * 2 - vec2(1f/16, 1f/16);*/
    
    //out_Color = vec4(pass_TexCoord, 0, 1);
}