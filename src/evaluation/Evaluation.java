package evaluation;

import board.Bitboard;
import board.Constants;
import engine.EngineGeneralAttributes;

public abstract class Evaluation {

	// Instanz der Attribute.
	protected EngineGeneralAttributes generalAttributes;

	// Konstruktor
	public Evaluation(EngineGeneralAttributes generalAttributes) {
		this.generalAttributes = generalAttributes;
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
	public abstract int evaluateBoardState(Bitboard board, boolean isQiescence);

	public int evaluateBoardStateMoveOrdering(Bitboard board) {
		return evaluateBoardState(board, false);
	}
}
