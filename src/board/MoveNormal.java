package board;

public class MoveNormal extends Move {

	/* In welche Richtung der Move geht: -1..1 für beide Richtungen */
	/* 1 - left, 2 - right, 3 - up, 4 - down */
	private int dir;

	/* Konstruktor */
	public MoveNormal(int dir) {
		super(Constants.MoveType.NORMAL);
		this.dir = dir;
	}

	public void setDir(int dir) {
		this.dir = dir;
	}

	public int getDir() {
		return dir;
	}

	public String toString2() {
		String res;
		res = ((dir == Constants.Direction.LEFT) ? "left " : (dir == Constants.Direction.RIGHT) ? "right"
						: (dir == Constants.Direction.UP) ? "up   " : (dir == Constants.Direction.DOWN) ? "down "
								: "-.-  ");

		return res;
	}
	
	@Override
	public String toString() {
		String res;
		res = ((dir == Constants.Direction.LEFT) ? "w" : (dir == Constants.Direction.RIGHT) ? "e"
						: (dir == Constants.Direction.UP) ? "n" : (dir == Constants.Direction.DOWN) ? "s"
								: " ");

		return res;
	}

}
