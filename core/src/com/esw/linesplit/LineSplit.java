package com.esw.linesplit;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class LineSplit extends ApplicationAdapter {

	int WindowWidth, WindowHeight;
	int pad, halfGridSquare, eraseRadius;

	ShapeRenderer shapeRenderer;
	SpriteBatch batch;
	OrthographicCamera camera;

	Dot currentEditDot;
	Line currentLine;
	float linewidth = 16.0f;
	float dotScale;
	Direction currentDirection;
	Direction previousDirection;

	Vector2 lastPosition, nextPosition;

	Cursor head;
	Cursor tail;

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
	float animspeed = 8;
	float animRadius;

	float movementSpeed = 0.4f;
	float lerpTimer = 0.0f;
	float movePercent = 0.0f;


	Color linedeathcolor = new Color(GColor.LIGHT_BLUE_800);
	Color linelifecolor = new Color(GColor.LIGHT_BLUE_600);
	Color backgroundcolor = new Color(GColor.LIGHT_BLUE_50);

	Tool tool = Tool.draw;

	//===================================================================================================

	public LineSplit(int w, int h) {
		WindowWidth = w;
		WindowHeight = h;
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
		Vector2 center = new Vector2(0,0);
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

	int getGCD(int w, int h) {
		int num ;
		if(w > h) num = w;
		else num = h;

		for(int i = num - 1; i > 0; i--) {
			if((w % i == 0) && (h % i == 0)) return i;
		}
		return 0;
	}

	@Override
	public void create () {
		pad = getGCD(WindowWidth, WindowHeight); // Buffer from the ege of the screen to the bottom left corner of the grid (GCD of WindowWidth and WindowHeight)
		halfGridSquare = pad / 2; // The width of each of the lines
		eraseRadius = halfGridSquare - (halfGridSquare / 4); // Radius of erase circles
		linewidth = eraseRadius / 2.0f;
		dotScale = (eraseRadius / 1000.0f);
		animRadius = eraseRadius + (linewidth / 2.0f);

		shapeRenderer = new ShapeRenderer();
		shapeRenderer.setAutoShapeType(true);

		camera = new OrthographicCamera(WindowWidth, WindowHeight);
		camera.position.set(camera.viewportWidth / 2, camera.viewportHeight / 2, 0.0f);
		camera.update();

		batch = new SpriteBatch();
		batch.setProjectionMatrix(camera.combined);

		debugMessage = new BitmapFont(); // DEBUG
		debugMessage.setColor(Color.BLACK); // DEBUG

		botedge = new Vector2(pad, pad); // Vector that captures the bottom edge of the screen
		topedge = new Vector2(WindowWidth - pad, WindowHeight - pad); // Vector that captures the top edge of the screen

		currentDirection = Direction.N;
		previousDirection = Direction.N;

		dots.add(new Dot(Direction.E, GridSquare.grid(0, 2, pad), dotScale)); // DEBUG
		dots.add(new Dot(Direction.N, GridSquare.grid(2, 2, pad), dotScale)); // DEBUG
		dots.add(new Dot(Direction.W, GridSquare.grid(2, 4, pad), dotScale)); // DEBUG
		dots.add(new Dot(Direction.M, GridSquare.grid(0, 4, pad), dotScale)); // DEBUG

		head = new Cursor(new Vector2(), 0);
		tail = new Cursor(new Vector2(), 0);

	}

	public void inputs() {
		mouse.x = Gdx.input.getX();
		mouse.y = WindowHeight - Gdx.input.getY();

		if(Gdx.input.justTouched()) mouseClick = new Vector2(mouse.x, mouse.y);

		if(Gdx.input.isKeyJustPressed(Input.Keys.TAB)) edit = !edit;

		if(edit) {
			if (Gdx.input.isKeyJustPressed(Input.Keys.A)) {
				if (tool == Tool.draw) tool = Tool.erase;
				else if (tool == Tool.erase) tool = Tool.draw;
			}

			if (Gdx.input.isKeyJustPressed(Input.Keys.D) || Gdx.input.isKeyJustPressed(Input.Keys.NUM_5))
				dotType = Direction.M;
			if (Gdx.input.isKeyJustPressed(Input.Keys.W) || Gdx.input.isKeyJustPressed(Input.Keys.NUM_7))
				dotType = Direction.NW;
			if (Gdx.input.isKeyJustPressed(Input.Keys.E) || Gdx.input.isKeyJustPressed(Input.Keys.NUM_8))
				dotType = Direction.N;
			if (Gdx.input.isKeyJustPressed(Input.Keys.R) || Gdx.input.isKeyJustPressed(Input.Keys.NUM_9))
				dotType = Direction.NE;
			if (Gdx.input.isKeyJustPressed(Input.Keys.F) || Gdx.input.isKeyJustPressed(Input.Keys.NUM_6))
				dotType = Direction.E;
			if (Gdx.input.isKeyJustPressed(Input.Keys.V) || Gdx.input.isKeyJustPressed(Input.Keys.NUM_3))
				dotType = Direction.SE;
			if (Gdx.input.isKeyJustPressed(Input.Keys.C) || Gdx.input.isKeyJustPressed(Input.Keys.NUM_2))
				dotType = Direction.S;
			if (Gdx.input.isKeyJustPressed(Input.Keys.X) || Gdx.input.isKeyJustPressed(Input.Keys.NUM_1))
				dotType = Direction.SW;
			if (Gdx.input.isKeyJustPressed(Input.Keys.S) || Gdx.input.isKeyJustPressed(Input.Keys.NUM_4))
				dotType = Direction.W;
		}
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
				snap.x += halfGridSquare;
				snap.y += halfGridSquare;

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
		play = false;
		head = new Cursor(new Vector2(), 0);
		tail = new Cursor(new Vector2(), 0);
		currentLine = null;
		lines.clear();
		caps.clear();
		nextPosition = null;
		lastPosition = null;
		currentDirection = Direction.N;
		dead = false;
		animCounter = 0;
		playanim = false;
		playedanim = false;
		moveTail = false;
		lerpTimer = 0.0f;
		movePercent = 0.0f;
	}

	float angle = 0.0f;
	boolean moveTail = false;
	Vector2 startPos;
	public void update() {

		if(Gdx.input.isKeyJustPressed(Input.Keys.G)) reset();

		if(edit) edit();
		else {
			if(currentEditDot != null) currentEditDot = null;
			if((Gdx.input.isKeyJustPressed(Input.Keys.SPACE) || Gdx.input.justTouched())) {
				play = !play;

				if(play && !playanim) {
					Vector2 snap = new Vector2(0, 0);
					snap.x = (float) (Math.floor(mouseClick.x / pad)) * pad;
					snap.y = (float) (Math.floor(mouseClick.y / pad)) * pad;
					snap.x += halfGridSquare;
					snap.y += halfGridSquare;

					mouseClick = snap;

					head = new Cursor(snap, linewidth / 2);
					tail = new Cursor(snap, linewidth / 2);
					lastPosition = head.center;
					nextPosition = calcDir(head.center, currentDirection);
					currentLine = new Line(head.center, lastPosition);
					startPos = head.center;

					lines.add(currentLine);
					caps.add(head.center); // Add first cap to the lines

					lastPosition = head.center;
					nextPosition = calcDir(head.center, currentDirection);
				} else {
					reset();
				}
			}
		}

		if(Gdx.input.isTouched()) {
			angle = MathUtils.atan2(mouse.y - mouseClick.y, mouse.x - mouseClick.x);
			angle *= (180 / MathUtils.PI);
			if(angle <  0) angle = 360 + angle;

			if((angle < 30 && angle > 0) || (angle < 360 && angle > 330)) { angle = 0; currentDirection = Direction.E; }
			else if(angle < 60 && angle > 30) { angle = 45;  currentDirection = Direction.NE; }
			else if(angle < 120 && angle > 60) { angle = 90; currentDirection = Direction.N; }
			else if(angle < 150 && angle > 120) { angle = 135; currentDirection = Direction.NW; }
			else if(angle < 210 && angle > 150) { angle = 180; currentDirection = Direction.W; }
			else if(angle < 240 && angle > 210) { angle = 225; currentDirection = Direction.SW; }
			else if(angle < 300 && angle > 240) { angle = 270; currentDirection = Direction.S; }
			else if(angle < 330 && angle > 300) { angle = 315; currentDirection = Direction.SE; }

			nextPosition = calcDir(head.center, currentDirection);
		}

		if(play && !Gdx.input.isTouched()) {
			lerpTimer += deltaTime;

			if (currentDirection == Direction.NW || currentDirection == Direction.NE
					|| currentDirection == Direction.SW || currentDirection == Direction.SE) {
				movePercent = lerpTimer / (movementSpeed + 0.1f);
			} else {
				movePercent = lerpTimer / movementSpeed;
			}

			head.center = lerp(lastPosition, nextPosition, movePercent);
			currentLine.start = head.center;

			if(Vector2.dst(head.center.x,  head.center.y, startPos.x, startPos.y) > (pad * 3)) {
				moveTail = true;
			}

			if(moveTail) {
				tail.center = lerp(lastPosition, nextPosition, movePercent);
			}

			if((head.center.x < (pad + head.radius) || head.center.x > (WindowWidth - pad - head.radius)) || (
					head.center.y < (pad + head.radius) || head.center.y > WindowHeight - pad - head.radius)) {
				play = false;
				dead = true;
			}

			if(movePercent >= 1.0f) {

				lerpTimer = 0.0f;
				movePercent = 0.0f;

				head.center = nextPosition;

				lastPosition = head.center;

				for(Dot d : dots) {
					if(isInsideCircle(head.center, d.center, 1)) {
						System.out.println("Hit dot : " + d.direction + " at location : " + d.center); // DEBUG
						if(d.direction == Direction.M) {
							play = false;
							dead = true;
						} else {
							currentDirection = d.direction;
							caps.add(head.center);
							lines.add(new Line(currentLine));
							currentLine.start = head.center;
							currentLine.end = head.center;
							playanim = true;
							playedanim = false;
							animationPos = head.center;
							play = false;
						}
						System.out.println("Previous: " + previousDirection + " | Current : " + currentDirection);
					}
				}

				nextPosition = calcDir(head.center, currentDirection);
			}
		}

		if(dead && Gdx.input.justTouched()) reset();
	}

	Vector2 animationPos = new Vector2(0,0);

	@Override
	public void render () {
		deltaTime = Gdx.graphics.getDeltaTime();
		globalTime += deltaTime;

		inputs();

		if(edit) edit();
		else update();

		Gdx.gl.glClearColor(backgroundcolor.r, backgroundcolor.g, backgroundcolor.b, backgroundcolor.a);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		camera.update();

		// SHAPE RENDERING =============================================================================================

		// DRAW GRID
		float gridlinewidth = linewidth / 8;
		shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
		shapeRenderer.setColor(Color.LIGHT_GRAY);
		for(int i = pad; i <= WindowWidth - pad; i += pad) {
			shapeRenderer.rectLine(i, pad, i, WindowHeight - pad, gridlinewidth);
		}
		for(int i = pad; i <= WindowHeight - pad; i += pad) {
			shapeRenderer.rectLine(pad, i, WindowWidth - pad, i, gridlinewidth);
		}
		// DRAW GRID CORNERS
		shapeRenderer.circle(pad, pad, gridlinewidth / 2);
		shapeRenderer.circle(pad, WindowHeight - pad, gridlinewidth / 2);
		shapeRenderer.circle(WindowWidth - pad, pad, gridlinewidth / 2);
		shapeRenderer.circle(WindowWidth - pad, WindowHeight - pad, gridlinewidth / 2);

		// DRAW LINES
		for(Line l : lines) {
			if(l != null) {
				if (dead) shapeRenderer.setColor(linedeathcolor);
				else shapeRenderer.setColor(linelifecolor);

				shapeRenderer.rectLine(l.start, l.end, linewidth);
			}
		}

		// DRAW CAPS
		for(Vector2 v : caps) {
			if(v != null) shapeRenderer.circle(v.x, v.y, linewidth / 2, 16);
		}

		// DRAW CURSOR
		if(head != null) shapeRenderer.circle(head.center.x, head.center.y, head.radius);
		shapeRenderer.setColor(Color.FOREST);
		if(tail != null) shapeRenderer.circle(tail.center.x, tail.center.y, tail.radius);

		if(playanim) playAnim(animationPos);

		shapeRenderer.end();
		// =============================================================================================================

		// SPRITE BATCH ================================================================================================
		batch.begin();

		if(currentEditDot != null) currentEditDot.sprite.draw(batch);

		for(Dot d : dots) {
			d.sprite.draw(batch);
		}

		debugMessage.draw(batch, "Cursor: " + head.center, WindowWidth / 2, WindowHeight - 10);
		debugMessage.draw(batch, "Mouse: " + mouse, WindowWidth / 2, WindowHeight - 30);
		debugMessage.draw(batch, "Angle:" + angle, WindowWidth / 2, WindowHeight - 50);

		debugMessage.draw(batch, "Dots: " + dots.size, WindowWidth - 70, WindowHeight - 10);
		debugMessage.draw(batch, "Lines: " + lines.size, WindowWidth - 70, WindowHeight - 30);
		debugMessage.draw(batch, "Caps: " + caps.size, WindowWidth - 70, WindowHeight - 50);

		if(play) debugMessage.draw(batch, "Play", 30, WindowHeight - 10);
		else     debugMessage.draw(batch, "Paused", 30, WindowHeight - 10);

		debugMessage.draw(batch, "Start: " + nextPosition, 120, WindowHeight - 10);
		debugMessage.draw(batch, "End:   " + lastPosition, 120, WindowHeight - 30);
		debugMessage.draw(batch, "Direction: " + currentDirection, 120, WindowHeight - 50);

		if(edit) {
			if (tool == Tool.draw) {
				debugMessage.draw(batch, "Draw mode", 30, WindowHeight - 30);
			} else if (tool == Tool.erase) {
				debugMessage.draw(batch, "Erase mode", 30, WindowHeight - 30);
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

		shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
		if(Gdx.input.isTouched()) {
			int linel = 200;
			float hyp = (float)Math.sqrt(Math.pow(linel, 2) + Math.pow(linel, 2));
			hyp /= 2;
			shapeRenderer.setColor(Color.BLACK);
			if(angle == 0) shapeRenderer.line(mouseClick.x, mouseClick.y, mouseClick.x + linel, mouseClick.y); // 0
			else if(angle == 45) shapeRenderer.line(mouseClick.x, mouseClick.y, mouseClick.x + hyp, mouseClick.y + hyp); // 45
			else if(angle == 90) shapeRenderer.line(mouseClick.x, mouseClick.y, mouseClick.x, mouseClick.y + linel); // 90
			else if(angle == 135) shapeRenderer.line(mouseClick.x, mouseClick.y, mouseClick.x - hyp, mouseClick.y + hyp); // 135
			else if(angle == 180) shapeRenderer.line(mouseClick.x, mouseClick.y, mouseClick.x - linel, mouseClick.y); // 180
			else if(angle == 225) shapeRenderer.line(mouseClick.x, mouseClick.y, mouseClick.x - hyp, mouseClick.y - hyp); // 225
			else if(angle == 270) shapeRenderer.line(mouseClick.x, mouseClick.y, mouseClick.x, mouseClick.y - linel); // 270
			else if(angle == 315) shapeRenderer.line(mouseClick.x, mouseClick.y, mouseClick.x + hyp, mouseClick.y - hyp); // 315
		}
		// DEBUG
		shapeRenderer.set(ShapeRenderer.ShapeType.Filled);
		shapeRenderer.setColor(Color.GREEN);
		if(nextPosition != null) shapeRenderer.circle(nextPosition.x, nextPosition.y, 4);
		shapeRenderer.setColor(Color.RED);
		if(lastPosition != null) shapeRenderer.circle(lastPosition.x, lastPosition.y, 4);
		shapeRenderer.setColor(Color.BROWN);
		shapeRenderer.end();
	}

	void drawDottedLine(int dotDist, float x1, float y1, float x2, float y2) {
		Vector2 vec2 = new Vector2(x2, y2).sub(new Vector2(x1, y1));
		float length = vec2.len();
		for(int i = 0; i < length; i += dotDist) {
			vec2.clamp(length - i, length - i);
			shapeRenderer.point(x1 + vec2.x, y1 + vec2.y, 0);
		}
	}

	public void playAnim(Vector2 v) {
		Vector2 a = calcAngle();
		if(!playedanim) {
			shapeRenderer.arc(v.x, v.y, animRadius, a.x, animCounter, 45);
			shapeRenderer.arc(v.x, v.y, animRadius, a.x, -animCounter, 45);

			if(animCounter < 180) animCounter += animspeed;
			else {
				playedanim = true;
			}
		} else {
			shapeRenderer.arc(v.x, v.y, animRadius, a.y, animCounter, 45);
			shapeRenderer.arc(v.x, v.y, animRadius, a.y, -animCounter, 45);

			if(animCounter > 0) animCounter -= animspeed;
			else {
				playanim = false;
				/*for(int i = 0; i < dots.size; i++) {
					Dot d = dots.get(i);
					if(isInsideCircle(head.center, d.center, 1)) {
						// dots.removeIndex(i); // REMOVE DOT (STC?)
					}
				}*/
				play = true;
				previousDirection = currentDirection;
			}
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

	public Vector2 calcAngle() {
		Vector2 v = new Vector2(0.0f, 0.0f);

		switch(previousDirection) {
			case N:  v.x = 270; break;
			case NE: v.x = 225; break;
			case E:  v.x = 180; break;
			case SE: v.x = 135; break;
			case S:  v.x = 90;  break;
			case SW: v.x = 45;  break;
			case W:  v.x = 0;   break;
			case NW: v.x = 315; break;
		}
		switch(currentDirection) {
			case N:  v.y = 90; break;
			case NE: v.y = 45; break;
			case E:  v.y = 0; break;
			case SE: v.y = 315; break;
			case S:  v.y = 270;  break;
			case SW: v.y = 225;  break;
			case W:  v.y = 180;   break;
			case NW: v.y = 135; break;
		}

		return v;
	}
}
