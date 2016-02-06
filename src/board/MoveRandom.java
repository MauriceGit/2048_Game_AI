package board;

public class MoveRandom extends Move {

	/* In welche Richtung der Move geht: -1..1 für beide Richtungen */
	/* 1 - left, 2 - right, 3 - up, 4 - down */
	private int x, y;
	private long number;

	/* Konstruktor */
	public MoveRandom() {
		super(Constants.MoveType.RANDOM);
		this.x = 0; this.y = 0;
	}
	
	public MoveRandom (int x, int y, long number) {
		super(Constants.MoveType.RANDOM);
		this.x = x;
		this.y = y;
		this.number = number;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public long getNumber() {
		return number;
	}

	public void setNumber(long number) {
		this.number = number;
	}
	
	
	
}
