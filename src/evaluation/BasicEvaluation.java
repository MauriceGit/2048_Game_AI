package evaluation;

import board.Bitboard;
import board.Constants;
import engine.EngineGeneralAttributes;

public class BasicEvaluation extends Evaluation {

	/* Konstruktor */
	public BasicEvaluation(EngineGeneralAttributes generalAttributes) {
		super(generalAttributes);
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

	private int evaluateBiggestValue(Bitboard board) {
		int score = 0;

		for (int i = 0; i < generalAttributes.getSize(); i++) {
			if (biggestValueAtSideHorizontal(board, i)) {
				score += 1;
			}
			if (biggestValueAtSideVertical(board, i)) {
				score += 1;
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

		score += evaluateFreeSpace(board) * 20000;
		score += evaluateBiggestValue(board) * 20000;

		return score;
	}

}
