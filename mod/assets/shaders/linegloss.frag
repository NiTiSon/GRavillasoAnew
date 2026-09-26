varying vec4 v_color;
varying vec2 v_texCoord;

uniform vec2 u_resolution;
uniform float u_time;

void main() {
    float dist = abs(v_texCoord.y - 0.5);
    float gloss = 1.0 - smoothstep(0.0, 0.1, dist);
    float pulse = sin(u_time * 4.0 + v_texCoord.x * 15.0) * 0.5 + 0.5;
    float alpha = gloss * pulse * 0.4;
    gl_FragColor = vec4(v_color.rgb, v_color.a * alpha);
}