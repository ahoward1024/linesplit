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
    LineSplit.Direction direction;

    public Dot(LineSplit.Direction d, Vector2 v, float s) {
        update(d, v, s);
    }

    public void update(LineSplit.Direction d, Vector2 v, float scale) {
        direction = d;
        center = v;
        sprite = new Sprite(new Texture(Gdx.files.internal(d + ".png")));
        sprite.setScale(scale);
        sprite.setCenter(v.x, v.y);
        //sprite.setAlpha(0.25f);
    }

    public void setCenter(Vector2 v) {
        center = v;
        sprite.setCenter(v.x, v.y);
    }

}
