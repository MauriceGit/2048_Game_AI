package evaluation;

import board.Bitboard;
import board.Constants;
import engine.EngineGeneralAttributes;

public class SimpleEvaluation extends Evaluation {

	/* Konstruktor */
	public SimpleEvaluation(EngineGeneralAttributes generalAttributes) {
		super(generalAttributes);
	}

	private int valueDiff(Bitboard board, int col, int row) {
		int value = 0;

		// left
		if (col > 0) {
			value += Math.abs(board.getBoardValue(col, row)
					- board.getBoardValue(col - 1, row));
		}
		// right
		if (col < generalAttributes.getSize() - 1) {
			value += Math.abs(board.getBoardValue(col, row)
					- board.getBoardValue(col + 1, row));
		}
		// up
		if (row < generalAttributes.getSize() - 1) {
			value += Math.abs(board.getBoardValue(col, row)
					- board.getBoardValue(col, row + 1));
		}
		// down
		if (row > 0) {
			value += Math.abs(board.getBoardValue(col, row)
					- board.getBoardValue(col, row - 1));
		}

		return value;
	}

	private int evaluateValueDifference(Bitboard board) {
		int score = 0;

		for (int i = 0; i < generalAttributes.getSize()
				* generalAttributes.getSize(); i++) {
			int col = i % generalAttributes.getSize();
			int row = i / generalAttributes.getSize();

			score += valueDiff(board, col, row);
		}

		/* Zur Minimierung der Kantendifferenzen */
		score *= -1;

		return score;
	}

	private int evaluateFreeSpace(Bitboard board) {
		int score = 0;

		for (int i = 0; i < generalAttributes.getSize()
				* generalAttributes.getSize(); i++) {
			int col = i % generalAttributes.getSize();
			int row = i / generalAttributes.getSize();

			if (board.isEmptyField(col, row)) {
				score += 1;
			}
		}

		return score;
	}

	private boolean isAscendingRow(Bitboard board, int row) {

		for (int i = 0; i < generalAttributes.getSize() - 1; i++) {
			if (board.getBoardValue(i, row) > board.getBoardValue(i + 1, row)) {
				return false;
			}
		}

		return true;
	}

	private boolean isDescendingRow(Bitboard board, int row) {

		for (int i = 0; i < generalAttributes.getSize() - 1; i++) {
			if (board.getBoardValue(i, row) < board.getBoardValue(i + 1, row)) {
				return false;
			}
		}

		return true;
	}

	private boolean isAscendingFile(Bitboard board, int col) {

		for (int i = 0; i < generalAttributes.getSize() - 1; i++) {
			if (board.getBoardValue(col, i) > board.getBoardValue(col, i + 1)) {
				return false;
			}
		}

		return true;
	}

	private boolean isDescendingFile(Bitboard board, int col) {

		for (int i = 0; i < generalAttributes.getSize() - 1; i++) {
			if (board.getBoardValue(col, i) < board.getBoardValue(col, i + 1)) {
				return false;
			}
		}

		return true;
	}

	private int getBiggestValue(Bitboard board) {
		int max = -1000;

		for (int i = 0; i < generalAttributes.getSize(); i++) {
			for (int j = 0; j < generalAttributes.getSize(); j++) {
				if (board.getBoardValue(i, j) > max)
					max = board.getBoardValue(i, j);
			}
		}
		return max;
	}

	private boolean biggestValueAtSideHorizontal(Bitboard board, int row) {

		int leftSide = board.getBoardValue(0, row);
		int rightSide = board.getBoardValue(generalAttributes.getSize() - 1,
				row);

		for (int i = 0; i < generalAttributes.getSize(); i++) {
			if (board.getBoardValue(i, row) > leftSide
					&& board.getBoardValue(i, row) > rightSide) {
				return false;
			}
		}

		return true;
	}

	private boolean biggestValueAtSideVertical(Bitboard board, int col) {

		int downSide = board.getBoardValue(col, 0);
		int upSide = board.getBoardValue(col, generalAttributes.getSize() - 1);

		for (int i = 0; i < generalAttributes.getSize(); i++) {
			if (board.getBoardValue(col, i) > downSide
					&& board.getBoardValue(col, i) > upSide) {
				return false;
			}
		}

		return true;
	}

	private int evaluateMonotonicity(Bitboard board) {

		int score = 0;
		int dir = Constants.Direction.NONE;
		int lastDir = Constants.Direction.NONE;
		int rowCheck = 0;
		int colCheck = 0;

		/* Alle 4 Reihen checken! */
		for (int row = 0; row < generalAttributes.getSize(); row++) {

			if (isAscendingRow(board, row)) {
				dir = Constants.Direction.RIGHT;
				score += 1;
			} else {
				if (isDescendingRow(board, row)) {
					dir = Constants.Direction.LEFT;
					score += 1;
				}
			}

			if (lastDir != Constants.Direction.NONE) {
				if (lastDir == dir) {
					score += 10;
					rowCheck++;
				}
			} else {
				if (dir != Constants.Direction.NONE) {
					score += 10;
					rowCheck++;
				}
			}

			lastDir = dir;

		}

		dir = Constants.Direction.NONE;
		lastDir = Constants.Direction.NONE;

		/* Alle vier Spalten checken! */
		for (int col = 0; col < generalAttributes.getSize(); col++) {

			if (isAscendingFile(board, col)) {
				dir = Constants.Direction.UP;
				score += 1;
			} else {
				if (isDescendingFile(board, col)) {
					dir = Constants.Direction.DOWN;
					score += 1;
				}
			}

			if (lastDir != Constants.Direction.NONE) {
				if (lastDir == dir) {
					score += 10;
					colCheck++;
				}
			} else {
				if (dir != Constants.Direction.NONE) {
					score += 10;
					colCheck++;
				}
			}

			lastDir = dir;
		}

		/* Alle 4 Reihen sind gleich sortiert! */
		if (rowCheck == generalAttributes.getSize()) {
			score += 20;
		}

		/* Alle 4 Spalten sind gleich sortiert! */
		if (colCheck == generalAttributes.getSize()) {
			score += 20;
		}

		return score;

	}

	private boolean biggestValuesInTheMiddleFiles(Bitboard board, int row) {

		boolean res = false;
		int middle = (int) (generalAttributes.getSize() / 2 + 0.001);
		int lastLeftNumber = board.getBoardValue(0, row);
		int lastRightNumber = board.getBoardValue(
				generalAttributes.getSize() - 1, row);

		for (int col = 0; col < middle; col++) {

			/* Pyramidenbildung! */
			if (!(lastLeftNumber <= board.getBoardValue(col, row))
					|| !(lastRightNumber <= board.getBoardValue(
							generalAttributes.getSize() - 1 - col, row))) {
				return false;
			}

			lastLeftNumber = board.getBoardValue(col, row);
			lastRightNumber = board.getBoardValue(generalAttributes.getSize()
					- 1 - col, row);
		}

		return res;

	}

	private int evaluateMonotonicityOptimized(Bitboard board) {

		int score = 0;

		/* Oberste Reihe auf- oder absteigend sortiert! */
		if (isAscendingRow(board, generalAttributes.getSize() - 1)
				|| isDescendingRow(board, generalAttributes.getSize() - 1)) {
			score += 20;
		}

		if (biggestValuesInTheMiddleFiles(board,
				generalAttributes.getSize() - 2)) {
			score += 5;
		}

		if (biggestValuesInTheMiddleFiles(board,
				generalAttributes.getSize() - 2)) {
			score += 2;
		}

		/* Alle vier Spalten checken! */
		for (int col = 0; col < generalAttributes.getSize(); col++) {

			if (isAscendingFile(board, col)) {
				score += 2;
			}
		}

		return score;

	}

	private int evaluateAlternatingMonotonicity(Bitboard board) {
		int score = 0;
		int direction = Constants.Direction.NONE;
		int firstDirection = Constants.Direction.NONE;
		int firstRow = (generalAttributes.getSize() - 1) % 2;
		int points = 5;

		firstDirection = isAscendingRow(board, generalAttributes.getSize() - 1) ? Constants.Direction.RIGHT
				: (isDescendingRow(board, generalAttributes.getSize() - 1) ? Constants.Direction.LEFT
						: Constants.Direction.NONE);

		/* Oberste Reihe auf- oder absteigend sortiert! */
		if (firstDirection != Constants.Direction.NONE) {
			score += 20;
		}

		for (int row = generalAttributes.getSize() - 2; row >= 0; row--) {

			direction = isAscendingRow(board, row) ? Constants.Direction.RIGHT
					: (isDescendingRow(board, row) ? Constants.Direction.LEFT
							: Constants.Direction.NONE);

			if (direction == Constants.Direction.NONE) {
				points -= 1;
			}

			/* Dann muss es gleich sortiert sein! */
			if (row % 2 == firstRow) {
				if (firstDirection == direction) {
					score += points;
				}
			} else {
				if (firstDirection != direction) {
					score += points;
				}
			}

		}

		/* Alle vier Spalten checken! */
		for (int col = 0; col < generalAttributes.getSize(); col++) {

			if (isAscendingFile(board, col)) {
				score += 2;
			}
		}

		return score;
	}

	private int getBiggestValueWithoutFirstRow(Bitboard board) {
		int max = -1000;

		for (int i = 0; i < generalAttributes.getSize(); i++) {
			for (int j = 0; j < generalAttributes.getSize() - 1; j++) {
				if (board.getBoardValue(i, j) > max)
					max = board.getBoardValue(i, j);
			}
		}
		return max;
	}

	private boolean biggestValueInCorner(Bitboard board) {
		int max = getBiggestValue(board);

		if (max == board.getBoardValue(0, 0)
				|| max == board.getBoardValue(0,
						generalAttributes.getSize() - 1)
				|| max == board.getBoardValue(generalAttributes.getSize() - 1,
						0)
				|| max == board.getBoardValue(generalAttributes.getSize() - 1,
						generalAttributes.getSize() - 1)) {
			return true;
		}
		return false;
	}

	private int evaluateBiggestValue(Bitboard board) {
		int score = 0;

		for (int i = 0; i < generalAttributes.getSize(); i++) {
			if (biggestValueAtSideHorizontal(board, i)) {
				score += 10;
			}
			if (biggestValueAtSideVertical(board, i)) {
				score += 10;
			}

		}
		return score;
	}

	private int evaluateHighNumberBonus(Bitboard board) {

		int max = getBiggestValue(board);

		for (int exp = 11; exp < 20; exp++) {
			if ((int) (Math.pow(2, exp) + 0.001) == max) {
				return exp - 10;
			}
		}

		return 0;

	}

	private int evaluateSameTilesClose(Bitboard board) {

		int score = 0;
		int number = -1;

		/* second Row horizontal */
		for (int i = 0; i < generalAttributes.getSize(); i++) {
			/* second Row horizontal */
			if (board.getBoardValue(i, generalAttributes.getSize() - 2) == number) {
				score += 1;
			}

			number = board.getBoardValue(i, generalAttributes.getSize() - 2);

			/* first Row vertical */
			if (board.getBoardValue(i, generalAttributes.getSize() - 1) == number) {
				score += 2;
			}
		}

		return score;

	}

	private int biggestNumbersInFirstRow(Bitboard board) {
		int score = 0;

		int biggestNumber = getBiggestValueWithoutFirstRow(board);

		for (int i = 0; i < generalAttributes.getSize(); i++) {
			if (board.getBoardValue(i, generalAttributes.getSize() - 1) > biggestNumber) {
				score++;
			}
		}

		return score;
	}

	/**
	 * Evaluiert die übergebene Boardsituation und gibt eine Zahl zwischen 0 und
	 * 100 zurück, oder so...
	 * 
	 * Sie wird immer für Gold berechnet!!! Bei Bedarf wird das Ergebnis dann
	 * negiert.
	 * 
	 * @param board
	 *            die zu evaluierende Boardsituation.
	 * @return Der errechnete Wert
	 */
	public int evaluateBoardState(Bitboard board, boolean isQuiescence) {
		int score = 0;

		score += evaluateValueDifference(board)
				* generalAttributes.getDifference();

		score += evaluateFreeSpace(board) * generalAttributes.getSpace();

		// score += evaluateMonotonicity(board)
		// * generalAttributes.getMonotonicity();

		// score += evaluateMonotonicityOptimized(board)
		// * generalAttributes.getMonotonicity();

		score += evaluateAlternatingMonotonicity(board)
				* generalAttributes.getMonotonicity();

		score += evaluateBiggestValue(board)
				* generalAttributes.getBiggestValueEval();

		if (biggestValueInCorner(board)) {
			score += generalAttributes.getBiggestValueInCornerEval();
		} else {
			score -= generalAttributes.getBiggestValueInCornerEval() / 2;
		}

		score += evaluateHighNumberBonus(board)
				* generalAttributes.getHighNumberBonus();

		score += evaluateSameTilesClose(board)
				* generalAttributes.getSameTilesAdjacent();

		score += biggestNumbersInFirstRow(board)
				* generalAttributes.getBiggestNumbersInFirstRow();

		return score;
	}

}
