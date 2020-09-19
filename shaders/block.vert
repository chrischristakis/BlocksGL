#version 330 core

layout (location = 0) in vec2 aPos;
layout (location = 1) in vec3 aCol;

uniform mat4 mvp = mat4(1.0);

out vec3 color;

void main()
{
	gl_Position = mvp * vec4(aPos, 0.0, 1.0);
	color = aCol;
}
