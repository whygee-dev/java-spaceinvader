
import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.Canvas;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;

public class GameApplet extends Applet {

	Canvas display_parent;
	Thread gameThread;
	Game game;

	public void startLWJGL() {
		gameThread = new Thread(() -> {
			try {
				Display.setParent(display_parent);

			} catch (LWJGLException e) {
				e.printStackTrace();
			}
			game = new Game(false);
			game.execute();
		});
		gameThread.start();
	}

	private void stopLWJGL() {
		Game.gameRunning = false;
		try {
			gameThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void start() {

	}

	public void stop() {

	}

	public void destroy() {
		remove(display_parent);
		super.destroy();
	}

	public void init() {
		setLayout(new BorderLayout());
		try {
			display_parent = new Canvas() {
				public void addNotify() {
					super.addNotify();
					startLWJGL();
				}
				public void removeNotify() {
					stopLWJGL();
					super.removeNotify();
				}
			};
			display_parent.setSize(getWidth(),getHeight());
			add(display_parent);
			display_parent.setFocusable(true);
			display_parent.requestFocus();
			display_parent.setIgnoreRepaint(true);
			setVisible(true);
		} catch (Exception e) {
			System.err.println(e);
			throw new RuntimeException("Unable to create display");
		}
	}
}