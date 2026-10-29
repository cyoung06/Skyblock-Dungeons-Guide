#version 330

uniform sampler2D Sampler0;

in vec2 texCoord0;
in vec4 vertexColor;
flat in vec2 position;
flat in vec2 size;

out vec4 fragColor;

void main(void)
{
    // Result
    vec2 coord = position + size * (texCoord0 - floor(texCoord0));

    // Output
    fragColor = texture(Sampler0, coord);
}
