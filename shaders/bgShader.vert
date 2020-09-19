#version 330 core

layout(location = 0) in vec2 aPos;

uniform mat4 mvp = mat4(1.0);

void main()
{
	gl_Position = mvp * vec4(aPos, 0, 1);
}
