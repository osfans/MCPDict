package com.osfans.mcpdict;

/** Hit-testing the monotone cubic band without rasterising a full-height Region. */
public final class SankeyHit {
    private SankeyHit() {}
    private static float cubic(float a,float b,float c,float d,float t){float u=1-t;return u*u*u*a+3*u*u*t*b+3*u*t*t*c+t*t*t*d;}
    public static boolean contains(float width,float leftY,float rightY,float thickness,float x,float y,float tolerance){
        if(x<width*.24f-tolerance||x>width*.74f+tolerance)return false;
        float low=0,high=1;
        for(int i=0;i<16;i++){float t=(low+high)/2;if(cubic(width*.24f,width*.46f,width*.54f,width*.74f,t)<x)low=t;else high=t;}
        float top=cubic(leftY,leftY,rightY,rightY,(low+high)/2);
        return y>=top-tolerance&&y<=top+thickness+tolerance;
    }
}
