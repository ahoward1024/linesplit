package com.esw.linesplit;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class LineSplit extends ApplicationAdapter {

	int ScreenWidth, ScreenHeight;
	int pad, line, radius;

	ShapeRenderer shapeRenderer;
	SpriteBatch batch;

	Dot current;
	Array<Dot> dots = new Array<Dot>();

	BitmapFont bitmapFont;

	float scale = 0.16f;

	public LineSplit(int w, int h) {
		ScreenWidth = w;
		ScreenHeight = h;
	}

	@Override
	public void create () {
		pad = 80;
		line = pad / 2;
		radius = line - (line / 4);

		shapeRenderer = new ShapeRenderer();
		shapeRenderer.setAutoShapeType(true);

		batch = new SpriteBatch();

		bitmapFont = new BitmapFont();

		current = new Dot(Dot.Type.mid, new Vector2(0,0), scale);
	}

	Vector2 mouse = new Vector2(0,0);
	boolean edit = false;
	Dot.Type dotType = Dot.Type.mid;
	boolean draw = true;
	@Override
	public void render () {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		mouse.x = Gdx.input.getX();
		mouse.y = ScreenHeight - Gdx.input.getY();

		if(Gdx.input.isKeyJustPressed(Input.Keys.TAB)) edit = !edit;
		if(Gdx.input.isKeyJustPressed(Input.Keys.A)) draw = !draw;

		if(Gdx.input.isKeyJustPressed(Input.Keys.D) || Gdx.input.isKeyJustPressed(Input.Keys.NUM_5)) dotType = Dot.Type.mid;
		if(Gdx.input.isKeyJustPressed(Input.Keys.W) || Gdx.input.isKeyJustPressed(Input.Keys.NUM_7)) dotType = Dot.Type.upleft;
		if(Gdx.input.isKeyJustPressed(Input.Keys.E) || Gdx.input.isKeyJustPressed(Input.Keys.NUM_8)) dotType = Dot.Type.up;
		if(Gdx.input.isKeyJustPressed(Input.Keys.R) || Gdx.input.isKeyJustPressed(Input.Keys.NUM_9)) dotType = Dot.Type.upright;
		if(Gdx.input.isKeyJustPressed(Input.Keys.F) || Gdx.input.isKeyJustPressed(Input.Keys.NUM_6)) dotType = Dot.Type.right;
		if(Gdx.input.isKeyJustPressed(Input.Keys.V) || Gdx.input.isKeyJustPressed(Input.Keys.NUM_3)) dotType = Dot.Type.downright;
		if(Gdx.input.isKeyJustPressed(Input.Keys.C) || Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) dotType = Dot.Type.down;
		if(Gdx.input.isKeyJustPressed(Input.Keys.X) || Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) dotType = Dot.Type.downleft;
		if(Gdx.input.isKeyJustPressed(Input.Keys.S) || Gdx.input.isKeyJustPressed(Input.Keys.NUM_4)) dotType = Dot.Type.left;

		shapeRenderer.begin();
		shapeRenderer.set(ShapeRenderer.ShapeType.Line);

		shapeRenderer.setColor(Color.DARK_GRAY);
		for(int i = pad; i <= ScreenWidth - (pad * 2); i += pad) {
			shapeRenderer.line(i, pad, i, ScreenHeight - pad);
		}
		for(int i = pad; i <= ScreenHeight - (pad * 2); i += pad) {
			shapeRenderer.line(pad, i, ScreenWidth - pad, i);
		}
		shapeRenderer.rect(pad, pad, ScreenWidth - (pad * 2), ScreenHeight - (pad * 2));
		shapeRenderer.end();

		batch.begin();

		for(Dot d : dots) {
			if(d != null) {
				d.sprite.draw(batch);
			}
		}

		if(current != null) {
			current.sprite.draw(batch);
		}

		if(edit) {
			if(draw) {
				if(current == null) {
					current = new Dot(dotType, mouse, scale);
				}

				bitmapFont.draw(batch, "Draw mode", 30, ScreenHeight - 30);

				if (Gdx.input.justTouched()) {
					System.out.println("Touched");
					dots.add(current);
					current = null;
				} else {
					Vector2 snap = new Vector2(0,0);
					snap.x = (float)(Math.floor(mouse.x / pad)) * pad;
					snap.y = (float)(Math.floor(mouse.y / pad)) * pad;
					snap.x += line;
					snap.y += line;

					current.update(dotType, snap, scale);
				}

			} else {
				current = null;

				bitmapFont.draw(batch, "Erase mode", 30, ScreenHeight - 30);
				batch.end();

				beginblend();
				for(Dot d : dots) {
					shapeRenderer.circle(d.center.x, d.center.y, radius);
				}
				endblend();
				for(int i = 0; i < dots.size; i++) {
					Dot d = dots.get(i);
					if(Gdx.input.justTouched()) {
						if (isInsideCircle(mouse, d.center, radius)) {
							dots.removeIndex(i);
						}
					}
				}

				batch.begin();
			}
		} else {
			current = null;
		}

		batch.end();

	}

	public void beginblend() {
		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		shapeRenderer.begin();
		shapeRenderer.set(ShapeRenderer.ShapeType.Filled);
		shapeRenderer.setColor(1, 0, 0, 0.5f);
	}

	public void endblend() {
		shapeRenderer.end();
		Gdx.gl.glDisable(GL20.GL_BLEND);
	}

	public static boolean isInsideCircle(Vector2 testpoint, Vector2 center, float radius) {
		return Math.pow((center.x - testpoint.x), 2) + Math.pow((center.y - testpoint.y), 2) <= Math.pow(radius, 2);
	}
}
