#define HIGHP

varying vec4 v_col;
varying float v_radius;

uniform float u_innerRadius;
uniform float u_outerRadius;

void main() {
    float norm = (v_radius - u_innerRadius) / (u_outerRadius - u_innerRadius);

    float innerFade = smoothstep(0.0, 0.04, norm);
    float outerFade = 1.0 - smoothstep(0.85, 1.0, norm);

    float band = sin(v_radius * 55.0) * 0.12 + 0.88;

    vec4 color = v_col * band;
    color.a *= innerFade * outerFade;
    gl_FragColor = color;
}
