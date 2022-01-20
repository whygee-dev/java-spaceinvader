import java.util.ArrayList;

import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;

import static org.lwjgl.opengl.GL11.*;

public class Game {
	private final String WINDOW_TITLE = "Space Invaders";
	private final int width = 1024;
	private final int height = 720;
	private TextureLoader textureLoader;
	private final ArrayList<Entity> entities = new ArrayList<>();
	private final ArrayList<Entity> removeList = new ArrayList<>();
	private Invader invader;
	private Bullet[] bullets;
	private Sprite message;
	private Sprite win;
	private Sprite lose;
	private int	bulletIndex;
	private long lastFire;
	private int	alienCount;
	private boolean	waitingForKeyPress = true;
	private boolean	shouldUpdate;
	private long lastUpdate = getTime();
	private boolean	fireHasBeenReleased;
	private long lastFpsTime;
	private int	fps;
	private static final long timerTicksPerSecond = Sys.getTimerResolution();
	public static boolean gameRunning = true;
	private final boolean fullscreen, OP;
	private int	mouseX;
	private static boolean isApplication;

	public Game(boolean fullscreen, boolean OP) {
		this.fullscreen = fullscreen;
		this.OP = OP;
		initialize();
	}

	public static long getTime() {
		return (Sys.getTime() * 1000) / timerTicksPerSecond;
	}

	public static void sleep(long duration) {
		try {
			Thread.sleep((duration * timerTicksPerSecond) / 1000);
		} catch (InterruptedException err) {
			System.err.println("Thread exception");
		}
	}

	public void initialize() {
		try {
			setDisplayMode();
			Display.setTitle(WINDOW_TITLE);
			Display.setFullscreen(fullscreen);
			Display.create();

			if (isApplication) {
				Mouse.setGrabbed(true);
			}

			glEnable(GL_TEXTURE_2D);

			glDisable(GL_DEPTH_TEST);

			glMatrixMode(GL_PROJECTION);
			glLoadIdentity();

			glOrtho(0, width, height, 0, -1, 1);
			glMatrixMode(GL_MODELVIEW);
			glLoadIdentity();
			glViewport(0, 0, width, height);

			textureLoader = new TextureLoader();


		} catch (LWJGLException le) {
			System.out.println("Init exception");
			le.printStackTrace();
			Game.gameRunning = false;

      		return;
		}

		lose = getSprite("lose.png");
		Sprite pressAnyKey = getSprite("presskey.png");
		win = getSprite("win.png");

		message = pressAnyKey;

		bullets = new Bullet[20];

		for (int i = 0; i < bullets.length; i++) {
			bullets[i] = new Bullet(this, "bullet.png", 0, 0, this.OP);
		}

		startGame();
	}

	private boolean setDisplayMode() {
		try {
			DisplayMode[] dm = org.lwjgl.util.Display.getAvailableDisplayModes(width, height, -1, -1, -1, -1, 60, 60);

			org.lwjgl.util.Display.setDisplayMode(dm, new String[] {
			  "width=" + width,
			  "height=" + height,
			  "freq=" + 60,
			  "bpp=" + org.lwjgl.opengl.Display.getDisplayMode().getBitsPerPixel()
		  	});

		  	return true;
		} catch (Exception e) {
			e.printStackTrace();
		  	System.out.println("Fullscreen fail");
		}

		return false;
	}

	private void startGame() {
		entities.clear();
		initEntities();
	}

	private void initEntities() {
		invader = new Invader(this, "invader.png", 500, 630);
		entities.add(invader);
		alienCount = 0;
		for (int i = 0; i< 5; i++) {
			for (int y = 0; y < 12; y++) {
				Entity alien;

				if (y % 3 == 0) {
					alien = new Alien(this, 100 + (y * 70), (40) + i * 60, "alienship1.png");
				}

				else if (y % 2 == 0) {
					alien = new Alien(this, 100 + (y * 70), (40) + i * 60, "alienship2.png");
				}

				else {
					alien = new Alien(this, 100 + (y * 70), (40) + i * 60, "alienship3.png");
				}

				entities.add(alien);
				alienCount++;
			}
		}
	}

	public void updateLogic() {
		shouldUpdate = true;
	}

	public void removeEntity(Entity entity) {
		removeList.add(entity);
	}

	public void death() {
		message = lose;
		waitingForKeyPress = true;
	}

	public void win() {
		message = win;
		waitingForKeyPress = true;
	}

	public void alienKilled() {
		alienCount--;

		if (alienCount == 0) {
			win();
		}

		for ( Entity entity : entities ) {
			if ( entity instanceof Alien) {
				entity.setHorizontalMovement(entity.getHorizontalMovement() * 1.02f);
			}
		}
	}

	public void fire() {
		long firingInterval = this.OP ? 50 : 300;
		if (System.currentTimeMillis() - lastFire < firingInterval) {
			return;
		}

		lastFire = System.currentTimeMillis();
		Bullet shot = bullets[bulletIndex++ % bullets.length];
		shot.reinitialize(invader.getX() + 15, invader.getY() - 30);
		entities.add(shot);
	}

	private void gameLoop() {
		while (Game.gameRunning) {
			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
			glMatrixMode(GL_MODELVIEW);
			glLoadIdentity();

			frameRendering();

			Display.update();
		}

		Display.destroy();
	}

	public void frameRendering() {
		Display.sync(60);

		long delta = getTime() - lastUpdate;
		lastUpdate = getTime();
		lastFpsTime += delta;
		fps++;

		if (lastFpsTime >= 1000) {
			lastFpsTime = 0;
			fps = 0;
		}

		if (!waitingForKeyPress) {
			for ( Entity entity : entities ) {
				entity.move(delta);
			}
		}

		for ( Entity entity : entities ) {
			entity.draw();
		}

		for (int p = 0; p < entities.size(); p++) {
			for (int s = p + 1; s < entities.size(); s++) {
				Entity me = entities.get(p);
				Entity him = entities.get(s);

				if (me.collidesWith(him)) {
					me.collidedWith(him);
					him.collidedWith(me);
				}
			}
		}

		entities.removeAll(removeList);
		removeList.clear();

		if (shouldUpdate) {
			for ( Entity entity : entities ) {
				entity.update();
			}

			shouldUpdate = false;
		}

		if (waitingForKeyPress) {
			message.draw(420, 350);
		}

		invader.setHorizontalMovement(0);

    	mouseX = Mouse.getDX();

		boolean leftPressed = hasInput(Keyboard.KEY_LEFT);
		boolean rightPressed = hasInput(Keyboard.KEY_RIGHT);
		boolean firePressed = hasInput(Keyboard.KEY_SPACE);

		if (!waitingForKeyPress) {
			float moveSpeed = this.OP ? 1000 : 500;
			if ((leftPressed) && (!rightPressed)) {
				invader.setHorizontalMovement(-moveSpeed);
			} else if ((rightPressed) && (!leftPressed)) {
				invader.setHorizontalMovement(moveSpeed);
			}

			if (firePressed) {
				fire();
			}
		} else {
			if (!firePressed) {
				fireHasBeenReleased = true;
			}
			if ((firePressed) && (fireHasBeenReleased)) {
				waitingForKeyPress = false;
				fireHasBeenReleased = false;
				startGame();
			}
		}

		if ((Display.isCloseRequested() || Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) && isApplication) {
			Game.gameRunning = false;
		}
	}

	private boolean hasInput(int direction) {
		switch(direction) {
			case Keyboard.KEY_SPACE:
				return Keyboard.isKeyDown(Keyboard.KEY_SPACE) || Mouse.isButtonDown(0);
			case Keyboard.KEY_LEFT:
				return Keyboard.isKeyDown(Keyboard.KEY_LEFT) || mouseX < 0;
			case Keyboard.KEY_RIGHT:
				return Keyboard.isKeyDown(Keyboard.KEY_RIGHT) || mouseX > 0;
		}

		return false;
	}

	public static void main(String[] args) {
		isApplication = true;
		System.out.println("pass fs as argument for fullscreen");
		System.out.println("pass op as argument for op mode");
		new Game((args.length > 0 && "fs".equals(args[0])), (args.length > 0 && "op".equals(args[0]))).execute();
		System.exit(0);
	}

	public void execute() {
		gameLoop();
	}

	public Sprite getSprite(String ref) {
		return new Sprite(textureLoader, ref);
	}
}
