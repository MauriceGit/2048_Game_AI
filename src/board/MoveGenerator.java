package board;

import java.util.LinkedList;

import engine.EngineGeneralAttributes;

public class MoveGenerator {

	private EngineGeneralAttributes generalAttributes;

	public MoveGenerator(EngineGeneralAttributes generalAttributes) {
		this.generalAttributes = generalAttributes;
	}

	private boolean leftShiftLegal(Bitboard board) {

		for (int row = 0; row < generalAttributes.getSize(); row++) {
			boolean foundNumber = false;

			int lastNumber = -1;

			for (int col = generalAttributes.getSize() - 1; col >= 0; col--) {
				if (!board.isEmptyField(col, row)) {
					if (lastNumber == board.getBoardValue(col, row) && lastNumber != 0) {
						return true;
					}
					lastNumber = board.getBoardValue(col, row);
					foundNumber = true;
				} else {
					if (foundNumber) {
						return true;
					}
				}
			}
		}

		return false;
	}

	private boolean rightShiftLegal(Bitboard board) {

		for (int row = 0; row < generalAttributes.getSize(); row++) {
			boolean foundNumber = false;
			int lastNumber = -1;
			for (int col = 0; col < generalAttributes.getSize(); col++) {
				if (lastNumber == board.getBoardValue(col, row) && lastNumber != 0) {
					return true;
				}
				lastNumber = board.getBoardValue(col, row);
				if (!board.isEmptyField(col, row)) {
					foundNumber = true;
				} else {
					if (foundNumber) {
						return true;
					}
				}
			}
		}

		return false;
	}

	private boolean upShiftLegal(Bitboard board) {
		boolean isLegal = false;

		for (int col = 0; col < generalAttributes.getSize(); col++) {
			boolean foundNumber = false;
			int lastNumber = -1;
			for (int row = 0; row < generalAttributes.getSize(); row++) {
				if (lastNumber == board.getBoardValue(col, row) && lastNumber != 0) {
					return true;
				}
				lastNumber = board.getBoardValue(col, row);
				if (!board.isEmptyField(col, row)) {
					foundNumber = true;
				} else {
					if (foundNumber) {
						return true;
					}
				}
			}
		}

		return isLegal;
	}

	private boolean downShiftLegal(Bitboard board) {
		boolean isLegal = false;

		for (int col = 0; col < generalAttributes.getSize(); col++) {
			boolean foundNumber = false;
			int lastNumber = -1;
			for (int row = generalAttributes.getSize() - 1; row >= 0; row--) {
				if (lastNumber == board.getBoardValue(col, row) && lastNumber != 0) {
					return true;
				}
				lastNumber = board.getBoardValue(col, row);
				if (!board.isEmptyField(col, row)) {
					foundNumber = true;
				} else {
					if (foundNumber) {
						return true;
					}
				}
			}
		}

		return isLegal;
	}

	/**
	 * Generiert eine Liste mit allen direkten resultierenden möglichen Zügen
	 * aller Figuren der übergebenen Farbe für eine bestimmte Konstellation des
	 * übergebenen Boards.
	 * 
	 * @param board
	 *            Das Board, für das die Züge generierte werden sollen.
	 * @param color
	 *            Die übergebene Farbe, für die die Züge generiert werden
	 *            sollen.
	 * @return Eine Liste mit allen möglichen Zügen für die übergebene
	 *         Spielsituation.
	 */
	public LinkedList<Move> generateAllMoves(Bitboard board,
			boolean maximizingPlayer) {

		LinkedList<Move> possibleMoves = new LinkedList<Move>();

		if (maximizingPlayer) {
			boolean leftWorks = false;
			boolean upWorks = false;
			boolean rightWorks = false;

			/**
			 * HIER DIE ABFRAGE REIN, OB DER MOVE ÜBERHAUPT LEGAL AUSGEFÜHRT
			 * WERDEN KANN.
			 */

			if (leftShiftLegal(board)) {
				possibleMoves.add(new MoveNormal(Constants.Direction.LEFT));
				leftWorks = true;
			}
			if (upShiftLegal(board)) {
				possibleMoves.add(new MoveNormal(Constants.Direction.UP));
				upWorks = true;
			}
			if (rightShiftLegal(board)) {
				possibleMoves.add(new MoveNormal(Constants.Direction.RIGHT));
				rightWorks = true;
			}
			if (downShiftLegal(board) && !leftWorks && !upWorks && !rightWorks) {
				possibleMoves.add(new MoveNormal(Constants.Direction.DOWN));
			}

		} else {

			for (int i = 0; i < generalAttributes.getSize(); i++) {
				for (int j = 0; j < generalAttributes.getSize(); j++) {
					if (board.isEmptyField(i, j)) {
						possibleMoves.add(new MoveRandom(i, j, 2));
						possibleMoves.add(new MoveRandom(i, j, 4));
					}
				}
			}

		}

		return possibleMoves;
	}
}
