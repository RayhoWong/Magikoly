attribute vec4 aPosition;
attribute vec2 aTexCoord;
varying vec2 textureCoordinate;

void main() {
    textureCoordinate = aTexCoord;
    gl_Position = aPosition;
}
