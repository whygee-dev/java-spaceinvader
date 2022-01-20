import java.awt.Rectangle;

public abstract class Entity {
	protected float	x;
	protected float	y;
	protected Sprite sprite;
	protected float	dx;
	protected float	dy;
	private final Rectangle	myRect = new Rectangle();
	private final Rectangle opponentRect = new Rectangle();

	protected Entity(Sprite sprite, int x, int y) {
		this.sprite = sprite;
		this.x = x;
		this.y = y;
	}

	public void move(long delta) {
		x += (delta * dx) / 1000;
		y += (delta * dy) / 1000;
	}

	public void setHorizontalMovement(float dx) {
		this.dx = dx;
	}

	public void setVerticalMovement(float dy) {
		this.dy = dy;
	}

	public float getHorizontalMovement() {
		return dx;
	}

	public void draw() {
		sprite.draw((int) x, (int) y);
	}

	public void update() {
	}

	public int getX() {
		return (int) x;
	}

	public int getY() {
		return (int) y;
	}

	public boolean collidesWith(Entity other) {
		myRect.setBounds((int) x, (int) y, sprite.getWidth(), sprite.getHeight());
		opponentRect.setBounds((int) other.x, (int) other.y, other.sprite.getWidth(), other.sprite.getHeight());

		return myRect.intersects(opponentRect);
	}

	public abstract void collidedWith(Entity other);
}