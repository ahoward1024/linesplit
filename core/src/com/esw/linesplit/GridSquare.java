package com.esw.linesplit;

import com.badlogic.gdx.math.Vector2;

/**
 * Created by Alex on 12/6/2015.
 * Copyright echosoftworks 2015
 */
public class GridSquare {
    Vector2 pos = new Vector2(-1,-1);

    public GridSquare(int x, int y, int pad) {
        pos.x = set(x, pad);
        pos.y = set(y, pad);
    }

    static Vector2 grid(int x, int y, int pad) {
        return new Vector2(set(x, pad), set(y, pad));
    }

    static float set(int a, int pad) {
        float f;
        int start = (pad + (pad / 2));
        if (a == 0) f = start;
        else f = (start + (pad * a));
        return f;
    }

    @Override
    public String toString() {
        return pos.toString();
    }
}
