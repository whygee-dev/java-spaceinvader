
public class Alien extends Entity {

	private static final int DOWNWARD_MOVEMENT = 10;
	private static final int BOTTOM_BORDER = 650;
	private static final int RIGHT_BORDER = 950;
	private static final int LEFT_BORDER = 10;
	private final float moveSpeed = 175;
	private final Game game;

	public Alien(Game game, int x, int y, String image) {
		super(game.getSprite(image), x, y);
		this.game = game;
		dx = -moveSpeed;
	}

	public void move(long delta) {
		if ((dx < 0) && (x < LEFT_BORDER)) {
			game.updateLogic();
		}

		if ((dx > 0) && (x > RIGHT_BORDER)) {
			game.updateLogic();
		}

		super.move(delta);
	}

	public void update() {
		dx = -dx;
		y += DOWNWARD_MOVEMENT;

		if (y > BOTTOM_BORDER) {
			game.death();
		}
	}

	public void collidedWith(Entity other) {
	}
}