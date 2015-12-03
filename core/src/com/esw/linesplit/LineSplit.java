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
	int pad, line, eraseRadius;

	ShapeRenderer shapeRenderer;
	SpriteBatch batch;

	Dot currentEditDot;
	Line currentLine;
	float linewidth = 16.0f;
	float dotScale = 0.06f;
	Direction currentDirection;

	Vector2 lastPosition, nextPosition;

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
	boolean play = false;

	float deltaTime;
	float globalTime;

	boolean dead = false;

	int animCounter = 0;
	boolean playanim = false;
	boolean playedanim = false;
	float animspeed = 7;

	float lerpTimeStarted;

	Color linedeathcolor = new Color(GColor.LIGHT_BLUE_800);
	Color linelifecolor = new Color(GColor.LIGHT_BLUE_600);
	Color backgroundcolor = new Color(GColor.LIGHT_BLUE_50);

	Tool tool = Tool.draw;

	//===================================================================================================

	public LineSplit(int w, int h) {
		ScreenWidth = w;
		ScreenHeight = h;
	}

	enum Tool {
		draw,
		erase
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

		public Dot(LineSplit.Direction direction, Vector2 center, float scale) {
			update(direction, center, scale);
		}

		public void update(LineSplit.Direction direction, Vector2 center, float scale) {
			this.direction = direction;
			this.center = center;
			if(sprite != null) sprite.getTexture().dispose();
			sprite = new Sprite(new Texture(Gdx.files.internal(direction + ".png")));
			sprite.setScale(scale);
			sprite.setCenter(center.x, center.y);
		}

	}

	@Override
	public void create () {
		pad = 80; // Buffer from the ege of the screen to the bottom left corner of the grid (GCD of ScreenWidth and ScreenHeight)
		line = pad / 2; // The width of each of the lines
		eraseRadius = line - (line / 4); // Radius of erase circles

		shapeRenderer = new ShapeRenderer();
		shapeRenderer.setAutoShapeType(true);

		batch = new SpriteBatch();

		debugMessage = new BitmapFont(); // DEBUG
		debugMessage.setColor(Color.BLACK); // DEBUG

		botedge = new Vector2(pad, pad); // Vector that captures the bottom edge of the screen
		topedge = new Vector2(ScreenWidth - pad, ScreenHeight - pad); // Vector that captures the top edge of the screen

		currentDirection = Direction.N;

		cursor = new Cursor(new Vector2(120, 120), linewidth / 2);
		lastPosition = cursor.center;
		nextPosition = calcDir(cursor.center, currentDirection);
		currentLine = new Line(cursor.center, lastPosition);
		lines.add(currentLine);
		caps.add(cursor.center); // Add first cap to the lines

		// TODO create grid system for dots (eg (0,0) bottom left corner of grid)
		dots.add(new Dot(Direction.E, new Vector2(120, 280), dotScale)); // DEBUG DOT
	}

	public void inputs() {
		mouse.x = Gdx.input.getX();
		mouse.y = ScreenHeight - Gdx.input.getY();

		if(Gdx.input.justTouched()) mouseClick = new Vector2(mouse.x, mouse.y);
		else mouseClick = new Vector2(0,0);

		if(Gdx.input.isKeyJustPressed(Input.Keys.TAB)) edit = !edit;

		if(Gdx.input.isKeyJustPressed(Input.Keys.A)) {
			if(tool == Tool.draw) tool = Tool.erase;
			else if(tool == Tool.erase) tool = Tool.draw;
		}

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

	public void edit() {
		if(tool == Tool.draw) {
			if(currentEditDot == null) currentEditDot = new Dot(dotType, mouse, dotScale);

			if(Gdx.input.justTouched()) {
				boolean isDotInPosition = false;
				for(Dot d: dots) {
					if(isInsideCircle(mouseClick, d.center, eraseRadius)) {
						isDotInPosition = true;
					}
				}

				if(isDotInPosition) {
					System.out.println("There is already a dot in position: " + mouseClick);
				} else {
					dots.add(currentEditDot);
					currentEditDot = null;
				}
			} else {
				Vector2 snap = new Vector2(0, 0);
				snap.x = (float) (Math.floor(mouse.x / pad)) * pad;
				snap.y = (float) (Math.floor(mouse.y / pad)) * pad;
				snap.x += line;
				snap.y += line;

				currentEditDot.update(dotType, snap, dotScale);
			}
		} else if(tool == Tool.erase) {
			if(currentEditDot != null) currentEditDot = null;
			if(Gdx.input.justTouched()) {
				for(int i = 0; i < dots.size; i++) {
					Dot d = dots.get(i);
					if(isInsideCircle(mouseClick, d.center, eraseRadius)) {
						dots.removeIndex(i);
					}
				}
			}
		}
	}

	public void reset() {
		lerpTimeStarted = globalTime;
		lines.clear();
		caps.clear();
		currentDirection = Direction.N;
		cursor.center = new Vector2(120, 120); // DEBUG (STC)
		lastPosition = cursor.center;
		nextPosition = calcDir(cursor.center, currentDirection);
		currentLine = new Line(cursor.center, lastPosition);
		lines.add(currentLine);
		dead = false;
		animCounter = 0;
		playanim = false;
		playedanim = false;
		caps.add(lastPosition);
	}

	float movementSpeed = 1.5f;

	public void update() {

		if(edit) edit();
		else { if(currentEditDot != null) currentEditDot = null; }

		if(Gdx.input.isKeyJustPressed(Input.Keys.SPACE) && !dead) {
			lerpTimeStarted = globalTime;
			play = !play;
		}

		if(play && !dead) {
			float timeSinceStartedMoving = globalTime - lerpTimeStarted;

			float movementPercent;

			if(currentDirection == Direction.NW || currentDirection == Direction.NE
					|| currentDirection == Direction.SW || currentDirection == Direction.SE) {
				movementPercent = timeSinceStartedMoving / (movementSpeed + 0.1f);
			} else {
				movementPercent = timeSinceStartedMoving / movementSpeed;
			}

			cursor.center = lerp(lastPosition, nextPosition, movementPercent);
			currentLine.start = cursor.center;

			if((cursor.center.x < (pad + cursor.radius) || cursor.center.x > (ScreenWidth - pad - cursor.radius)) || (
					cursor.center.y < (pad + cursor.radius) || cursor.center.y > ScreenHeight - pad - cursor.radius)) {
				play = false;
				dead = true;
			}

			if(movementPercent >= 1.0f) {

				lerpTimeStarted = globalTime;

				cursor.center = nextPosition;

				lastPosition = cursor.center;

				for(Dot d : dots) {
					if(isInsideCircle(cursor.center, d.center, 1)) {
						System.out.println("Hit dot : " + d.direction + " at location : " + d.center); // DEBUG
						if(d.direction == Direction.M) {
							play = false;
							dead = true;
						} else {
							currentDirection = d.direction;
							caps.add(cursor.center);
							lines.add(new Line(currentLine));
							currentLine.start = cursor.center;
							currentLine.end = cursor.center;
							playanim = true;
						}
					}
				}

				nextPosition = calcDir(cursor.center, currentDirection);
			}
		}
	}

	int ctr = 0;
	@Override
	public void render () {
		deltaTime = Gdx.graphics.getDeltaTime();
		globalTime += deltaTime;

		inputs();

		if(Gdx.input.isKeyJustPressed(Input.Keys.G)) reset();

		update();

		Gdx.gl.glClearColor(backgroundcolor.r, backgroundcolor.g, backgroundcolor.b, backgroundcolor.a);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		// SHAPE RENDERING =============================================================================================
		// DRAW GRID
		float gridlinewidth = linewidth / 8;
		shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
		shapeRenderer.setColor(Color.LIGHT_GRAY);
		for(int i = pad; i <= ScreenWidth - pad; i += pad) {
			shapeRenderer.rectLine(i, pad, i, ScreenHeight - pad, gridlinewidth);
		}
		for(int i = pad; i <= ScreenHeight - pad; i += pad) {
			shapeRenderer.rectLine(pad, i, ScreenWidth - pad, i, gridlinewidth);
		}
		// DRAW GRID CORNERS
		shapeRenderer.circle(pad, pad, gridlinewidth / 2);
		shapeRenderer.circle(pad, ScreenHeight - pad, gridlinewidth / 2);
		shapeRenderer.circle(ScreenWidth - pad, pad, gridlinewidth / 2);
		shapeRenderer.circle(ScreenWidth - pad, ScreenHeight - pad, gridlinewidth / 2);

		// DRAW LINES
		for(Line l : lines) {
			if(dead) shapeRenderer.setColor(linedeathcolor);
			else     shapeRenderer.setColor(linelifecolor);

			shapeRenderer.rectLine(l.start, l.end, linewidth);
		}

		// DRAW CAPS
		for(Vector2 v : caps) {
			shapeRenderer.circle(v.x, v.y, linewidth / 2, 16);
		}

		// DRAW CURSOR
		shapeRenderer.circle(cursor.center.x, cursor.center.y, cursor.radius);

		if(playanim) playAnim();

		shapeRenderer.end();
		// =============================================================================================================

		// SPRITE BATCH ================================================================================================
		batch.begin();

		if(currentEditDot != null) currentEditDot.sprite.draw(batch);

		for(Dot d : dots) {
			d.sprite.draw(batch);
		}

		debugMessage.draw(batch, "Direction: " + currentDirection, ScreenWidth / 2, ScreenHeight - 10);
		debugMessage.draw(batch, "Cursor: " + cursor.center, ScreenWidth / 2, ScreenHeight - 30);
		debugMessage.draw(batch, "Mouse: " + mouse, ScreenWidth / 2, ScreenHeight - 50);

		debugMessage.draw(batch, "Dots: " + dots.size, ScreenWidth - 70, ScreenHeight - 10);
		debugMessage.draw(batch, "Lines: " + lines.size, ScreenWidth - 70, ScreenHeight - 30);
		debugMessage.draw(batch, "Caps: " + caps.size, ScreenWidth - 70, ScreenHeight - 50);

		if(play) debugMessage.draw(batch, "Play", 30, ScreenHeight - 10);
		else     debugMessage.draw(batch, "Paused", 30, ScreenHeight - 10);

		debugMessage.draw(batch, "Start: " + nextPosition, 120, ScreenHeight - 10);
		debugMessage.draw(batch, "End:   " + lastPosition, 120, ScreenHeight - 30);

		if(edit) {
			if (tool == Tool.draw) {
				debugMessage.draw(batch, "Draw mode", 30, ScreenHeight - 30);
			} else if (tool == Tool.erase) {
				debugMessage.draw(batch, "Erase mode", 30, ScreenHeight - 30);
			}
		}

		batch.end();
		// =============================================================================================================

		// ERASE DOTS ==================================================================================================
		if(edit && (tool == Tool.erase)) {
			beginblend();
			for(Dot d : dots) {
				shapeRenderer.circle(d.center.x, d.center.y, eraseRadius);
			}
			endblend();
		}
		// =============================================================================================================

		shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
		// DEBUG
		shapeRenderer.setColor(Color.GREEN);
		shapeRenderer.circle(nextPosition.x, nextPosition.y, 4);
		shapeRenderer.setColor(Color.RED);
		shapeRenderer.circle(lastPosition.x, lastPosition.y, 4);
		shapeRenderer.end();
	}

	public void playAnim() {
		if(!playedanim) {
			shapeRenderer.arc(120, 280, 40, 270, animCounter, 45);
			shapeRenderer.arc(120, 280, 40, 270, -animCounter, 45);

			if(animCounter < 180) animCounter += animspeed;
			else playedanim = true;
		} else {
			shapeRenderer.arc(120, 280, 40, 0, animCounter, 45);
			shapeRenderer.arc(120, 280, 40, 0, -animCounter, 45);

			if(animCounter > 0) animCounter -= animspeed;
			else playanim = false;
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
			case N:  v = new Vector2(start.x, start.y + pad);       break;
			case NE: v = new Vector2(start.x + pad, start.y + pad); break;
			case E:  v = new Vector2(start.x + pad, start.y);       break;
			case SE: v = new Vector2(start.x + pad, start.y - pad); break;
			case S:  v = new Vector2(start.x, start.y - pad);       break;
			case SW: v = new Vector2(start.x - pad, start.y - pad); break;
			case W:  v = new Vector2(start.x - pad, start.y);       break;
			case NW: v = new Vector2(start.x - pad, start.y + pad); break;
			case M:  v = start;
		}
		return v;
	}
}
