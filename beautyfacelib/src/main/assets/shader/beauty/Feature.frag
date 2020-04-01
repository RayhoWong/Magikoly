precision highp float;

varying highp vec2 textureCoordinate;
uniform sampler2D inputTexture;



const int MAX_CONTOUR_POINT_COUNT=7;
uniform highp float radius;
uniform highp float aspectRatio;
uniform float leftContourPoints[MAX_CONTOUR_POINT_COUNT*2];
uniform float rightContourPoints[MAX_CONTOUR_POINT_COUNT*2];
uniform float deltaArray[MAX_CONTOUR_POINT_COUNT];
uniform int arraySize;

highp vec2 warpPositionToUse(vec2 currentPoint, vec2 contourPointA,  vec2 contourPointB, float radius, float delta, float aspectRatio)
{
    vec2 positionToUse = currentPoint;

    vec2 currentPointToUse = vec2(currentPoint.x, currentPoint.y * aspectRatio + 0.5 - 0.5 * aspectRatio);
    vec2 contourPointAToUse = vec2(contourPointA.x, contourPointA.y * aspectRatio + 0.5 - 0.5 * aspectRatio);

    float r = distance(currentPointToUse, contourPointAToUse);
    if(r < radius){
        vec2 dir = normalize(contourPointB - contourPointA);
        float dist = radius * radius - r * r;
        float alpha = dist / (dist + (r-delta) * (r-delta));
        alpha = alpha * alpha;
        positionToUse = positionToUse - alpha * delta * dir;
    }

    return positionToUse;

}


void main()
{
    vec2 positionToUse = textureCoordinate;
    if(radius>0.0){
        for (int i = 0; i < arraySize; i++)
        {
            positionToUse = warpPositionToUse(positionToUse, vec2(leftContourPoints[i * 2], leftContourPoints[i * 2 + 1]), vec2(rightContourPoints[i * 2], rightContourPoints[i * 2 + 1]), radius, deltaArray[i], aspectRatio);
            positionToUse = warpPositionToUse(positionToUse, vec2(rightContourPoints[i * 2], rightContourPoints[i * 2 + 1]), vec2(leftContourPoints[i * 2], leftContourPoints[i * 2 + 1]), radius, deltaArray[i], aspectRatio);
        }
    }
    gl_FragColor = texture2D(inputTexture, positionToUse);

}
