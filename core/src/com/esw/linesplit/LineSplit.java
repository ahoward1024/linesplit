package com.esw.linesplit;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class LineSplit extends ApplicationAdapter {

	int ScreenWidth, ScreenHeight;
	int pad, line, radius;

	ShapeRenderer shapeRenderer;
	SpriteBatch batch;

	Dot tmpdot;
	Array<Dot> dots = new Array<Dot>();
	Array<Line> lines = new Array<Line>();
	Array<Vector2> caps = new Array<Vector2>();

	BitmapFont bitmapFont;

	float scale = 0.16f;

	Dot cursor;

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

	class Line {
		Vector2 start;
		Vector2 end;
		Color color;

		public Line(Vector2 v1, Vector2 v2) {
			start = v1;
			end = v2;
		}

		public Line(Line p) {
			start = p.start;
			end = p.end;
			color = p.color;
		}
	}

	@Override
	public void create () {
		pad = 80; // GCD of ScreenWidth and ScreenHeight
		line = pad / 2;
		radius = line - (line / 4); // Radius of erase circles

		shapeRenderer = new ShapeRenderer();
		shapeRenderer.setAutoShapeType(true);

		batch = new SpriteBatch();

		bitmapFont = new BitmapFont();

		botedge = new Vector2(pad, pad);
		topedge = new Vector2(ScreenWidth - pad, ScreenHeight - pad);

		dir = Direction.N;

		begin = new Vector2(pad + (pad / 2), pad + (pad / 2));
		end = calcDir(begin, dir);

		cursor = new Dot(Direction.M, begin, 0.05f);
		cursor.sprite.setColor(Color.RED);

		tmpline = new Line(begin, begin);
		tmpline.color = new Color(Color.LIME);

		lines.add(tmpline);
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

	Line tmpline;

	boolean dead = false;

	float linewidth = 16.0f;

	int currentColor = 0;

	Vector2 mouseClick = new Vector2(0,0);

	public void inputs() {
		mouse.x = Gdx.input.getX();
		mouse.y = ScreenHeight - Gdx.input.getY();

		if(Gdx.input.justTouched()) mouseClick = new Vector2(mouse.x, mouse.y);
		else mouseClick = new Vector2(0,0);

		if(Gdx.input.isKeyJustPressed(Input.Keys.TAB)) edit = !edit;
		if(Gdx.input.isKeyJustPressed(Input.Keys.A)) draw = !draw;

		if(Gdx.input.isKeyJustPressed(Input.Keys.B)) playanim = true;

		if(Gdx.input.isKeyJustPressed(Input.Keys.D) || Gdx.input.isKeyJustPressed(Input.Keys.NUM_5)) dotType = Direction.M;
		if(Gdx.input.isKeyJustPressed(Input.Keys.W) || Gdx.input.isKeyJustPressed(Input.Keys.NUM_7)) dotType = Direction.NW;
		if(Gdx.input.isKeyJustPressed(Input.Keys.E) || Gdx.input.isKeyJustPressed(Input.Keys.NUM_8)) dotType = Direction.N;
		if(Gdx.input.isKeyJustPressed(Input.Keys.R) || Gdx.input.isKeyJustPressed(Input.Keys.NUM_9)) dotType = Direction.NE;
		if(Gdx.input.isKeyJustPressed(Input.Keys.F) || Gdx.input.isKeyJustPressed(Input.Keys.NUM_6)) dotType = Direction.E;
		if(Gdx.input.isKeyJustPressed(Input.Keys.V) || Gdx.input.isKeyJustPressed(Input.Keys.NUM_3)) dotType = Direction.SE;
		if(Gdx.input.isKeyJustPressed(Input.Keys.C) || Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) dotType = Direction.S;
		if(Gdx.input.isKeyJustPressed(Input.Keys.X) || Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) dotType = Direction.SW;
		if(Gdx.input.isKeyJustPressed(Input.Keys.S) || Gdx.input.isKeyJustPressed(Input.Keys.NUM_4)) dotType = Direction.W;
	}

	public void update() {
		if(edit) {
			if(draw) {
				if (tmpdot == null) {
					tmpdot = new Dot(dotType, mouse, scale);
				}

				if (Gdx.input.justTouched()) {
					for(Dot d : dots) {
						if(!isInsideCircle(mouseClick, d.center, radius)) {
						} else {
							System.out.println("There is already a dot in position: " + d.center);
						}
					}
					dots.add(tmpdot);
					tmpdot = null;
				} else {
					Vector2 snap = new Vector2(0, 0);
					snap.x = (float) (Math.floor(mouse.x / pad)) * pad;
					snap.y = (float) (Math.floor(mouse.y / pad)) * pad;
					snap.x += line;
					snap.y += line;

					tmpdot.update(dotType, snap, scale);
				}
			} else {
				tmpdot = null;
				if(Gdx.input.justTouched()) {
					for(int i = 0; i < dots.size; i++) {
						Dot d = dots.get(i);
						if(isInsideCircle(mouseClick, d.center, radius)) {
							dots.removeIndex(i);
						}
					}
				}
			}
		} else {
			tmpdot = null;
		}

		if(Gdx.input.isKeyJustPressed(Input.Keys.G)) {
			dir = Direction.N;
			cursor.setCenter(new Vector2(pad + (pad / 2), pad + (pad / 2)));
			begin = cursor.center;
			end = calcDir(begin, dir);
			lines.clear();
			caps.clear();
			tmpline.start = begin;
			tmpline.end = begin;
			lines.add(tmpline);
			dead = false;
			rnd = 10;
			up = 0;
			playanim = false;
			playedanim = false;
		}

		if(Gdx.input.isKeyJustPressed(Input.Keys.SPACE) && !dead) {
			play = !play;
			timeStarted = globalTime;
			caps.add(tmpline.start);
		}

		if(play && !dead) {
			float timeSinceStarted = globalTime - timeStarted;

			float percent;
			if (dir == Direction.NW || dir == Direction.NE || dir == Direction.SW || dir == Direction.SE)
				percent = timeSinceStarted / 0.3f;
			else percent = timeSinceStarted / 0.2f;

			Vector2 v = lerp(begin, end, percent);

			cursor.setCenter(v);
			tmpline.end = cursor.center;

			if ((cursor.center.x < pad || cursor.center.x > ScreenWidth - pad) || (
					cursor.center.y < pad || cursor.center.y > ScreenHeight - pad)) {
				play = false;
				dead = true;
			}

			if (percent >= 1.0f) {
				cursor.setCenter(end);
				begin = cursor.center;
				timeStarted = globalTime;

				for (Dot d : dots) {
					if (isInsideCircle(cursor.center, d.center, 1)) {
						tmpline.color = setColor();
						currentColor++;
						//tmpline.end = calcEnd(end, dir);
						tmpline.end = end;
						dir = d.direction;
						lines.add(new Line(tmpline));
						caps.add(tmpline.end);
						tmpline.start = begin;
						playanim = true;
					}
				}

				end = calcDir(cursor.center, dir);
			}
		}
	}

	int rnd = 10;
	int up = 0;
	boolean playanim = false;
	boolean playedanim = false;
	float vert[] = {10, 10, 100, 400, 256, 740, 500, 700, 500, 400, 900, 600};
 	@Override
	public void render () {

		inputs();

		update();

		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		deltaTime = Gdx.graphics.getDeltaTime();
		globalTime += deltaTime;

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

		shapeRenderer.set(ShapeRenderer.ShapeType.Filled);
		for(Line l : lines) {
			if(dead) {
				shapeRenderer.setColor(Color.RED);
				shapeRenderer.rectLine(l.start, l.end, linewidth);
			} else {
				shapeRenderer.setColor(Color.GREEN);
				shapeRenderer.rectLine(l.start, l.end, linewidth);
			}
		}

		for(Vector2 v : caps) {
			shapeRenderer.circle(v.x, v.y, linewidth / 2, 16);
		}


		shapeRenderer.circle(400, 400, 20, 128);
		shapeRenderer.arc(400, 320, 20, 0, 270, 10);

		if(playanim){
			playedanim = true;
			//shapeRenderer.arc(120, 280, rnd, 270, 90, 45);
			//if (rnd < 35) rnd++;

			shapeRenderer.arc(120, 280, 35, 270, up, 45);
			shapeRenderer.arc(120, 280, 35, 270, -up, 45);
			if (up < 180) up += 5;
			else playanim = false;
		}

		if(!playanim && playedanim) {
			shapeRenderer.arc(120, 280, 35, 270, 180, 45);
			shapeRenderer.arc(120, 280, 35, 270, -180, 45);
		}

		shapeRenderer.set(ShapeRenderer.ShapeType.Line);
		shapeRenderer.circle(400, 480, 20);
		shapeRenderer.arc(400, 560, 20, 0, 270);

		int num = 20;
		for(int i = 0;  i < 10; i++) {
			shapeRenderer.circle(400, 640, num, 256);
			num++;
		}

		shapeRenderer.polyline(vert);


		shapeRenderer.end();

		batch.begin();

		for(Dot d : dots) {
			if(d != null) {
				d.sprite.draw(batch);
			}
		}

		if(tmpdot != null) {
			tmpdot.sprite.draw(batch);
		}

		// cursor.sprite.draw(batch); // Debug

		bitmapFont.draw(batch, "Direction: " + dir, ScreenWidth / 2, ScreenHeight - 10);
		bitmapFont.draw(batch, "Mouse: " + mouse, ScreenWidth / 2, ScreenHeight - 30);

		if(play) bitmapFont.draw(batch, "Play", 30, ScreenHeight - 10);
		else     bitmapFont.draw(batch, "Paused", 30, ScreenHeight - 10);

		if(edit) {
			if (draw) bitmapFont.draw(batch, "Draw mode", 30, ScreenHeight - 30);
			else bitmapFont.draw(batch, "Erase mode", 30, ScreenHeight - 30);
		}

		batch.end();

		if(edit && !draw) {
			tmpdot = null;
			beginblend();
			for (Dot d : dots) {
				shapeRenderer.circle(d.center.x, d.center.y, radius);
			}
			endblend();
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

	public Color setColor() {
		if(currentColor == 9) currentColor = 0;
		Color c;
		switch(currentColor) {
			case 0: c = new Color(Color.LIME); break;
			case 1: c = new Color(Color.CORAL); break;
			case 2: c = new Color(Color.TEAL); break;
			case 3: c = new Color(Color.GOLD); break;
			case 4: c = new Color(Color.SALMON); break;
			case 5: c = new Color(Color.VIOLET); break;
			case 6: c = new Color(Color.CHARTREUSE); break;
			case 7: c = new Color(Color.MAGENTA); break;
			case 8: c = new Color(Color.YELLOW); break;
			default: c = new Color(Color.WHITE);
		}
		return c;
	}
}
