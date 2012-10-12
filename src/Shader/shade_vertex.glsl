//--------------------------------------------------------------------------------------
// Order Independent Transparency Vertex Shader
//
// Author: Louis Bavoil
// Email: sdkfeedback@nvidia.com
//
// Copyright (c) NVIDIA Corporation. All rights reserved.
//--------------------------------------------------------------------------------------

vec3 ShadeVertex() {
   // Original
	float diffuse = abs(normalize(gl_NormalMatrix * gl_Normal).z);
   /*
   vec3 L = (0, 100, 0);
	float diffuse = normalize( L - gl_Vertex);
   */
	return vec3(gl_Vertex.xy, diffuse);
}
