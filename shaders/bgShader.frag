#version 330 core
#define PI 3.141592

out vec4 fragColor;

void main()
{
//	float xCol = max(0,1-mod(gl_FragCoord.x, 40.0));
//	float yCol = max(0,1-mod(gl_FragCoord.y, 40.0));
//	vec3 added = vec3(xCol + yCol);
//	fragColor = vec4(added, 1);
	fragColor = vec4(0, 0.03, 0.14, 1);
}
