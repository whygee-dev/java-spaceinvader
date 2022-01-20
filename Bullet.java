
public class Bullet extends Entity {

	private static final int TOP_BORDER	= -100;
	private final float moveSpeed = -400;
	private final Game game;
	private boolean	used;

	public Bullet(Game game, String sprite, int x, int y) {
		super(game.getSprite(sprite), x, y);
		this.game = game;
		dy = moveSpeed;
	}

	public void reinitialize(int x, int y) {
		this.x = x;
		this.y = y;
		used = false;
	}

	public void move(long delta) {
		super.move(delta);

		if (y < TOP_BORDER) {
			game.removeEntity(this);
		}
	}

	public void collidedWith(Entity other) {
		if (used) {
			return;
		}

		if (other instanceof Alien) {
			game.removeEntity(this);
			game.removeEntity(other);
			game.alienKilled();
			used = true;
		}
	}
}