package com.esw.linesplit;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
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
	Array<Points> lines = new Array<Points>();

	BitmapFont bitmapFont;

	float scale = 0.16f;

	Dot test;

	public LineSplit(int w, int h) {
		ScreenWidth = w;
		ScreenHeight = h;
	}

	enum Direction {
		N,
		NE,
		E,
		SE,
		S,
		SW,
		W,
		NW,
		M
	}

	class Points {
		Vector2 start;
		Vector2 end;

		public Points(Vector2 v1, Vector2 v2) {
			start = v1;
			end = v2;
		}

		public Points(Points p) {
			start = p.start;
			end = p.end;
		}
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

		botedge = new Vector2(pad, pad);
		topedge = new Vector2(ScreenWidth - pad, ScreenHeight - pad);

		dir = Direction.N;

		begin = new Vector2(pad + (pad / 2), pad + (pad / 2));
		end = calcDir(begin, dir);

		test = new Dot(Direction.M, begin, 0.05f);
		test.sprite.setColor(Color.RED);

		points = new Points(begin, begin);

		lines.add(points);
	}

	Vector2 mouse = new Vector2(0,0);
	Vector2 botedge;
	Vector2 topedge;
	boolean edit = false;
	Direction dotType = Direction.N;
	boolean draw = true;
	float deltaTime;
	float globalTime;
	boolean play = false;
	Direction dir;
	Vector2 begin, end;

	float timeStarted;

	Points points;

	boolean dead = false;

	float linewidth = 8.0f;

	@Override
	public void render () {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		deltaTime = Gdx.graphics.getDeltaTime();
		globalTime += deltaTime;

		mouse.x = Gdx.input.getX();
		mouse.y = ScreenHeight - Gdx.input.getY();

		if(Gdx.input.isKeyJustPressed(Input.Keys.TAB)) edit = !edit;
		if(Gdx.input.isKeyJustPressed(Input.Keys.A)) draw = !draw;

		if(Gdx.input.isKeyJustPressed(Input.Keys.D) || Gdx.input.isKeyJustPressed(Input.Keys.NUM_5)) dotType = Direction.M;
		if(Gdx.input.isKeyJustPressed(Input.Keys.W) || Gdx.input.isKeyJustPressed(Input.Keys.NUM_7)) dotType = Direction.NW;
		if(Gdx.input.isKeyJustPressed(Input.Keys.E) || Gdx.input.isKeyJustPressed(Input.Keys.NUM_8)) dotType = Direction.N;
		if(Gdx.input.isKeyJustPressed(Input.Keys.R) || Gdx.input.isKeyJustPressed(Input.Keys.NUM_9)) dotType = Direction.NE;
		if(Gdx.input.isKeyJustPressed(Input.Keys.F) || Gdx.input.isKeyJustPressed(Input.Keys.NUM_6)) dotType = Direction.E;
		if(Gdx.input.isKeyJustPressed(Input.Keys.V) || Gdx.input.isKeyJustPressed(Input.Keys.NUM_3)) dotType = Direction.SE;
		if(Gdx.input.isKeyJustPressed(Input.Keys.C) || Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) dotType = Direction.S;
		if(Gdx.input.isKeyJustPressed(Input.Keys.X) || Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) dotType = Direction.SW;
		if(Gdx.input.isKeyJustPressed(Input.Keys.S) || Gdx.input.isKeyJustPressed(Input.Keys.NUM_4)) dotType = Direction.W;

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


		if(dead) shapeRenderer.setColor(Color.RED);
		else shapeRenderer.setColor(Color.LIME);

		shapeRenderer.end();

		shapeRenderer.begin();
		shapeRenderer.set(ShapeRenderer.ShapeType.Filled);
		for(Points p : lines) {
			shapeRenderer.rectLine(p.start, p.end, linewidth);
		}
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

		//test.sprite.draw(batch);

		bitmapFont.draw(batch, "Direction: " + dir, ScreenWidth / 2, ScreenHeight - 30);

		if(play) bitmapFont.draw(batch, "Play", 30, ScreenHeight - 10);
		else bitmapFont.draw(batch, "Paused", 30, ScreenHeight - 10);

		if(edit) {
			if(draw) {
				if(current == null) {
					current = new Dot(dotType, mouse, scale);
				}

				bitmapFont.draw(batch, "Draw mode", 30, ScreenHeight - 30);

				if (Gdx.input.justTouched()) {
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

		if(Gdx.input.isKeyJustPressed(Input.Keys.G)) {
			dir = Direction.N;
			test.setCenter(new Vector2(pad + (pad / 2), pad + (pad / 2)));
			begin = test.center;
			end = calcDir(begin, dir);
			lines.clear();
			points.start = begin;
			points.end = begin;
			lines.add(points);
			dead = false;
		}

		if(Gdx.input.isKeyJustPressed(Input.Keys.SPACE) && !dead) {
			play = !play;
			timeStarted = globalTime;
		}

		if(play && !dead) {
			float timeSinceStarted = globalTime - timeStarted;

			float percent;
			if(dir == Direction.NW ||  dir == Direction.NE || dir == Direction.SW || dir == Direction.SE) percent = timeSinceStarted / 0.3f;
			else percent = timeSinceStarted / 0.2f;

			Vector2 v = lerp(begin, end, percent);

			test.setCenter(v);
			points.end = test.center;

			if((test.center.x < pad || test.center.x > ScreenWidth - pad) || (
					test.center.y < pad || test.center.y > ScreenHeight - pad))
			{
				play = false;
				dead = true;
			}

			if(percent >= 1.0f) {
				test.setCenter(end);
				begin = test.center;
				timeStarted = globalTime;

				for(Dot d : dots) {
					if(isInsideCircle(test.center, d.center, 1)) {
						//points.end = calcEnd(end, dir);
						points.end = end;
						dir = d.direction;
						lines.add(new Points(points));
						points.start = begin;
					}
				}

				end = calcDir(test.center, dir);
			}
		}
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

	public static float lerp(float edge0, float edge1, float t) {
		return (1 - t) * edge0 + t * edge1;
	}

	public static Vector2 lerp(Vector2 v1, Vector2 v2, float t) {
		Vector2 v = new Vector2(0,0);

		v.x = (1 - t) * v1.x + t * v2.x;
		v.y = (1 - t) * v1.y + t * v2.y;

		return  v;
	}

	public Vector2 calcDir(Vector2 start, Direction dir) {
		Vector2 v = new Vector2(0.0f, 0.0f);
		switch(dir) {
			case N: {
				v = new Vector2(start.x, start.y + pad);
			} break;
			case NE: {
				v = new Vector2(start.x + pad, start.y + pad);
			} break;
			case E: {
				v = new Vector2(start.x + pad, start.y);
			} break;
			case SE: {
				v = new Vector2(start.x + pad, start.y - pad);
			} break;
			case S: {
				v = new Vector2(start.x, start.y - pad);
			} break;
			case SW: {
				v = new Vector2(start.x - pad, start.y - pad);
			} break;
			case W: {
				v = new Vector2(start.x - pad, start.y);
			} break;
			case NW: {
				v = new Vector2(start.x - pad, start.y + pad);
			} break;
			case M: {
				v = start;
			}
		}

		return v;
	}

	public Vector2 calcEnd(Vector2 end, Direction dir) {
		float div = 4.0f;
		float add = 0.5f;
		Vector2 v = new Vector2(0.0f, 0.0f);
		switch(dir) {
			case N: {
				v = new Vector2(end.x, end.y + (linewidth / 2));
			} break;
			case NE: {
				v = new Vector2(end.x + (linewidth / div) + add, end.y + (linewidth / div) + add);
			} break;
			case E: {
				v = new Vector2(end.x + (linewidth / 2), end.y);
			} break;
			case SE: {
				v = new Vector2(end.x + (linewidth / div) + add, end.y - (linewidth / div) + add);
			} break;
			case S: {
				v = new Vector2(end.x, end.y - (linewidth / 2));
			} break;
			case SW: {
				v = new Vector2(end.x - (linewidth / div) + add, end.y - (linewidth / div) + add);
			} break;
			case W: {
				v = new Vector2(end.x - (linewidth / 2), end.y);
			} break;
			case NW: {
				v = new Vector2(end.x - (linewidth / div) + add, end.y + (linewidth / div) + add);
			} break;
			case M: {
				v = end;
			}
		}

		return v;
	}
}
