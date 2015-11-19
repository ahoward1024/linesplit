package com.esw.linesplit;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;

/**
 * Created by Alex on 11/19/2015.
 * Copyright echosoftworks 2015
 */
public class Dot {

    Sprite sprite;
    Vector2 center;
    Type type;

    public enum Type {
        up,
        upright,
        right,
        downright,
        down,
        downleft,
        left,
        upleft,
        mid
    }

    public Dot(Type t, Vector2 v, float s) {
        update(t, v, s);
    }

    public void update(Type t, Vector2 v, float scale) {
        type = t;
        center = v;
        sprite = new Sprite(new Texture(Gdx.files.internal(t + ".png")));
        sprite.setScale(scale);
        sprite.setCenter(v.x, v.y);
    }

}
