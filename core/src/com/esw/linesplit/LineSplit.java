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

	Dot tmpdot;
	Line tmpline;
	float linewidth = 16.0f;
	float scale = 0.06f;
	Direction currentDirection;
	Vector2 lineStart, lineEnd;

	Cursor cursor;

	Array<Dot> dots = new Array<Dot>();
	Array<Line> lines = new Array<Line>();
	Array<Vector2> caps = new Array<Vector2>();

	BitmapFont debugMessage;

	Vector2 mouse = new Vector2(0,0);
	Vector2 mouseClick = new Vector2(0,0);

	Vector2 botedge;
	Vector2 topedge;
	boolean edit = false;
	Direction dotType = Direction.N;
	boolean draw = true;
	boolean play = false;

	float deltaTime;
	float globalTime;

	boolean dead = false;

	int animCounter = 0;
	boolean playanim = false;
	boolean playedanim = false;
	float timeStarted;

	Color linedeathcolor = new Color(GColor.LIGHT_BLUE_800);
	Color linelifecolor = new Color(GColor.LIGHT_BLUE_600);
	Color backgroundcolor = new Color(GColor.LIGHT_BLUE_50);

	//===================================================================================================

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

	class Cursor {
		Vector2 center;
		float radius;

		public Cursor(Vector2 v, float r) {
			center = v;
			radius = r;
		}
	}

	class Dot {

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

		debugMessage = new BitmapFont();
		debugMessage.setColor(Color.BLACK);

		botedge = new Vector2(pad, pad);
		topedge = new Vector2(ScreenWidth - pad, ScreenHeight - pad);

		currentDirection = Direction.N;

		lineStart = new Vector2(pad + (pad / 2), pad + (pad / 2));
		lineEnd = calcDir(lineStart, currentDirection);

		cursor = new Cursor(lineStart, linewidth / 2);

		tmpline = new Line(lineStart, lineStart);
		tmpline.color = new Color(Color.LIME);

		lines.add(tmpline);

		dots.add(new Dot(Direction.E, new Vector2(120, 280), scale));
	}

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
		// FIXME: bad framerate when editing
		if(edit) {
			if(draw) {
				if(tmpdot == null) {
					tmpdot = new Dot(dotType, mouse, scale);
				}

				if(Gdx.input.justTouched()) {
					boolean isdot = false;
					for(Dot d : dots) {
						if(isInsideCircle(mouseClick, d.center, radius)) {
							isdot = true;
						}
					}
					if(!isdot) {
						dots.add(tmpdot);
						tmpdot = null;
					} else {
						System.out.println("There is already a dot in position: " + mouseClick);
					}
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
			currentDirection = Direction.N;
			cursor.center = new Vector2(pad + (pad / 2), pad + (pad / 2));
			lineStart = cursor.center;
			lineEnd = calcDir(lineStart, currentDirection);
			lines.clear();
			caps.clear();
			tmpline.start = lineStart;
			tmpline.end = lineStart;
			lines.add(tmpline);
			dead = false;
			animCounter = 0;
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
			if(currentDirection == Direction.NW || currentDirection == Direction.NE || currentDirection == Direction.SW || currentDirection == Direction.SE)
				percent = timeSinceStarted / 0.3f;
			else percent = timeSinceStarted / 0.2f;

			cursor.center = lerp(lineStart, lineEnd, percent);
			tmpline.end = cursor.center;

			if((cursor.center.x < (pad + cursor.radius) || cursor.center.x > ScreenWidth - pad - cursor.radius) || (
					cursor.center.y < (pad + cursor.radius) || cursor.center.y > ScreenHeight - pad - cursor.radius)) {
				play = false;
				dead = true;
			}

			if(percent >= 1.0f) {
				cursor.center = lineEnd;
				lineStart = cursor.center;
				timeStarted = globalTime;

				for(Dot d : dots) {
					if(isInsideCircle(cursor.center, d.center, 1)) {
						tmpline.end = lineEnd;
						currentDirection = d.direction;
						lines.add(new Line(tmpline));
						caps.add(tmpline.end);
						tmpline.start = lineStart;
						playanim = true;
					}
				}

				lineEnd = calcDir(cursor.center, currentDirection);
			}
		}
	}

	@Override
	public void render () {

		inputs();

		update();

		Gdx.gl.glClearColor(backgroundcolor.r, backgroundcolor.g, backgroundcolor.b, backgroundcolor.a);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		deltaTime = Gdx.graphics.getDeltaTime();
		globalTime += deltaTime;

		shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

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
				shapeRenderer.setColor(linedeathcolor); // DEATH
				shapeRenderer.rectLine(l.start, l.end, linewidth);
			} else {
				shapeRenderer.setColor(linelifecolor); // LIFE
				shapeRenderer.rectLine(l.start, l.end, linewidth);
			}
		}

		for(Vector2 v : caps) {
			shapeRenderer.circle(v.x, v.y, linewidth / 2, 16);
		}

		shapeRenderer.circle(cursor.center.x, cursor.center.y, cursor.radius);

		if(playanim){
			if(!playedanim) {
				shapeRenderer.arc(120, 280, 40, 270, animCounter, 45);
				shapeRenderer.arc(120, 280, 40, 270, -animCounter, 45);
				if (animCounter < 180) {
					animCounter += 7;
				} else {
					playedanim = true;
				}
			} else {
				shapeRenderer.arc(120, 280, 40, 0, animCounter, 45);
				shapeRenderer.arc(120, 280, 40, 0, -animCounter, 45);
				if (animCounter > 0) {
					animCounter -= 7;
				} else {
					playanim = false;
				}
			}

		}

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


		debugMessage.draw(batch, "Direction: " + currentDirection, ScreenWidth / 2, ScreenHeight - 10);
		debugMessage.draw(batch, "Cursor: " + cursor.center, ScreenWidth / 2, ScreenHeight - 30);
		debugMessage.draw(batch, "Mouse: " + mouse, ScreenWidth / 2, ScreenHeight - 50);

		debugMessage.draw(batch, "Dots: " + dots.size, ScreenWidth - 70, ScreenHeight - 10);
		debugMessage.draw(batch, "Lines: " + lines.size, ScreenWidth - 70, ScreenHeight - 30);
		debugMessage.draw(batch, "Caps: " + caps.size, ScreenWidth - 70, ScreenHeight - 50);

		if(play) debugMessage.draw(batch, "Play", 30, ScreenHeight - 10);
		else     debugMessage.draw(batch, "Paused", 30, ScreenHeight - 10);

		if(edit) {
			if (draw) debugMessage.draw(batch, "Draw mode", 30, ScreenHeight - 30);
			else debugMessage.draw(batch, "Erase mode", 30, ScreenHeight - 30);
		}

		batch.end();

		if(edit && !draw) {
			tmpdot = null;
			beginblend();
			for(Dot d : dots) {
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
		shapeRenderer.setColor(1, 0, 0, 0.5f); // RED half ALPHA
	}

	public void endblend() {
		shapeRenderer.end();
		Gdx.gl.glDisable(GL20.GL_BLEND);
	}

	public static boolean isInsideCircle(Vector2 testpoint, Vector2 center, float radius) {
		return Math.pow((center.x - testpoint.x), 2) + Math.pow((center.y - testpoint.y), 2) <= Math.pow(radius, 2);
	}

	@SuppressWarnings("unused")
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
			}
			break;
			case NE: {
				v = new Vector2(start.x + pad, start.y + pad);
			}
			break;
			case E: {
				v = new Vector2(start.x + pad, start.y);
			}
			break;
			case SE: {
				v = new Vector2(start.x + pad, start.y - pad);
			}
			break;
			case S: {
				v = new Vector2(start.x, start.y - pad);
			}
			break;
			case SW: {
				v = new Vector2(start.x - pad, start.y - pad);
			}
			break;
			case W: {
				v = new Vector2(start.x - pad, start.y);
			}
			break;
			case NW: {
				v = new Vector2(start.x - pad, start.y + pad);
			}
			break;
			case M: {
				v = start;
			}
		}

		return v;
	}
}
