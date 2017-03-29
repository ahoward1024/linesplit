package com.esw.linesplit;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;

// !!! To file
@SuppressWarnings("unused")
class Inputs {

    // Mouse
    public static Vector2 mouse = new Vector2(0,0);
    public static Vector2 imouse = new Vector2(0,0);
    // Regular                   // Shifted
    public static boolean mouseleft;     public static boolean shiftMouseLeft;
    public static boolean mousemiddle;   public static boolean shiftMouseMiddle;
    public static boolean mouseright;    public static boolean shiftMouseRight;
    public static boolean mouseforward;  public static boolean shiftMouseFoward;
    public static boolean mouseback;     public static boolean shiftMouseBack;
    public static boolean clicked;

    // Modifiers          // Left                // Right
    public static boolean shift;  public static boolean lshift; static boolean rshift;
    public static boolean ctrl;   public static boolean lctrl;  static boolean rctrl;
    public static boolean alt;    public static boolean lalt;   static boolean ralt;
    public static boolean meta;   public static boolean lmeta;  static boolean rmeta;
    public static boolean menu;
    public static boolean esc;

    // Alpha keys  /* NOTE: A capital key will be tied to shift!! */
    // Unshifted                 // Shifted
    public static boolean tab;           public static boolean shifttab;
    public static boolean q;             public static boolean Q;
    public static boolean w;             public static boolean W;
    public static boolean e;             public static boolean E;
    public static boolean r;             public static boolean R;
    public static boolean t;             public static boolean T;
    public static boolean y;             public static boolean Y;
    public static boolean u;             public static boolean U;
    public static boolean i;             public static boolean I;
    public static boolean o;             public static boolean O;
    public static boolean p;             public static boolean P;
    public static boolean leftsquare;    public static boolean leftcurly;
    public static boolean rightsquare;   public static boolean rightcurly;
    public static boolean backslash;     public static boolean pipe;
    public static boolean a;             public static boolean A;
    public static boolean s;             public static boolean S;
    public static boolean d;             public static boolean D;
    public static boolean f;             public static boolean F;
    public static boolean g;             public static boolean G;
    public static boolean h;             public static boolean H;
    public static boolean j;             public static boolean J;
    public static boolean k;             public static boolean K;
    public static boolean l;             public static boolean L;
    public static boolean semicolon;     public static boolean colon;
    public static boolean singlequote;   public static boolean doublequote;
    public static boolean enter;         public static boolean shiftenter;
    public static boolean z;             public static boolean Z;
    public static boolean x;             public static boolean X;
    public static boolean c;             public static boolean C;
    public static boolean v;             public static boolean V;
    public static boolean b;             public static boolean B;
    public static boolean n;             public static boolean N;
    public static boolean m;             public static boolean M;
    public static boolean comma;         public static boolean leftangle;
    public static boolean period;        public static boolean rightangle;
    public static boolean forwardslash;  public static boolean question;
    public static boolean space;

    // Number keys/symbols
    // Unshifted           // Shifted
    public static boolean tick;    public static boolean tilde;
    public static boolean one;     public static boolean bang;
    public static boolean two;     public static boolean at;
    public static boolean three;   public static boolean hash;
    public static boolean four;    public static boolean dollar;
    public static boolean five;    public static boolean percent;
    public static boolean six;     public static boolean caret;
    public static boolean seven;   public static boolean and;
    public static boolean eight;   public static boolean star;
    public static boolean nine;    public static boolean leftparen;
    public static boolean zero;    public static boolean rightparen;
    public static boolean dash;    public static boolean underscore;
    public static boolean equals;  public static boolean plus;

    public static boolean F1;
    public static boolean F2;
    public static boolean F3;
    public static boolean F4;
    public static boolean F5;
    public static boolean F6;
    public static boolean F7;
    public static boolean F8;
    public static boolean F9;
    public static boolean F10;
    public static boolean F11;
    public static boolean F12;

    public static boolean sysrq; boolean prtscn;
    public static boolean scrlock;
    public static boolean pause; boolean brk;

    public static boolean insert;
    public static boolean home;
    public static boolean pageup;
    public static boolean delete;
    public static boolean end;
    public static boolean pagedown;

    public static boolean up;
    public static boolean down;
    public static boolean left;
    public static boolean right;

    // Num Keys
    public static boolean numone;
    public static boolean numtwo;
    public static boolean numthree;
    public static boolean numfour;
    public static boolean numfive;
    public static boolean numsix;
    public static boolean numseven;
    public static boolean numeight;
    public static boolean numnine;
    public static boolean numzero;
    public static boolean numlock;
    public static boolean numforwardslash;
    public static boolean numstar;
    public static boolean numplus;
    public static boolean numminus;
    public static boolean numenter;
    public static boolean numdot;

    public static int ScreenWidth;
    public static int ScreenHeight;

    public Inputs() {

    }

    public Inputs(int width, int height) {
        ScreenWidth = width;
        ScreenHeight = height;
    }

    public static void getAllInputs() {

    }

    public static void getBaseInputs() {
        getMouseInputs();
        getGameInputs();
        getEditInputs();
    }

    /**
     * Grab all of the mouse inputs.
     */
    public static void getMouseInputs() {
        // LibGDX specifies the mouse's (0,0) to be in the upper left corner while the
        // graphic's (0,0) is in the lower left. Getting the ScreenHeight - mouse.y
        // normalizes mouse inputs to the graphics coordinates.
        mouse.x = Gdx.input.getX(); mouse.y = ScreenHeight - Gdx.input.getY();
        imouse.x = mouse.x; imouse.y = mouse.y + ScreenHeight;
        mouseleft = Gdx.input.isButtonPressed(Input.Buttons.LEFT);
        mousemiddle = Gdx.input.isButtonPressed(Input.Buttons.MIDDLE);
        mouseright = Gdx.input.isButtonPressed(Input.Buttons.RIGHT);
        mouseforward = Gdx.input.isButtonPressed(Input.Buttons.FORWARD);
        mouseback = Gdx.input.isButtonPressed(Input.Buttons.BACK);
    }

    public static void getGameInputs() {
    }

    public static void getEditInputs() {
        esc = Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE); // Kill app
    }
}