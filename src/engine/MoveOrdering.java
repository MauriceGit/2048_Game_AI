package engine;

import java.util.LinkedList;

import evaluation.SimpleEvaluation;

import memory.Transposition;
import memory.ZobristHash;
import network.MessageOutputWriter;
import board.Bitboard;
import board.Move;
import board.MoveScore;

public class MoveOrdering {

	private EngineGeneralAttributes generalAttributes;

	private MessageOutputWriter messageWriter;

	/**
	 * Konstruktor
	 */
	public MoveOrdering(EngineGeneralAttributes generalAttributes,
			MessageOutputWriter messageWriter) {
		this.generalAttributes = generalAttributes;
		this.messageWriter = messageWriter;
	}

	/**
	 * Bezieht sich beim Moveordering ausschließlich auf Werte aus dem
	 * Transposition table!
	 * 
	 * @param board
	 *            Spielfeld
	 * @param moveList
	 *            nicht sortierte Liste mit Moves
	 * @param transposition
	 *            Memory-Boardpositionen
	 * @param depth
	 *            Rekursionstiefe
	 * @return
	 */
	public LinkedList<Move> moveOrderingFromMemory(Bitboard board,
			LinkedList<Move> moveList, Transposition transposition, int depth) {

		/* Allerletzte Rekursion und daher kein Moveordering möglich und nötig */
		if (depth == 0) {
			return moveList;
		}

		Bitboard newBoard = board.cloneBitboard();

		LinkedList<MoveOrderItem> sortedMoveList = new LinkedList<MoveOrderItem>();

		for (Move move : moveList) {

			newBoard = board.softCloneBitboard(newBoard);
			newBoard.applyMove(move);
			ZobristHash zobrist = transposition.lookupHash(newBoard
					.getZobristHash());
			boolean put = false;
			if (zobrist != null) {

				if (!sortedMoveList.isEmpty()) {
					for (int i = 0; i < sortedMoveList.size(); i++) {
						MoveOrderItem resMove = sortedMoveList.get(i);

						if (zobrist.score > resMove.getScore()) {
							sortedMoveList.add(i, new MoveOrderItem(move,
									zobrist.score));
							put = true;
							break;
						}

					}

					if (!put) {
						sortedMoveList.addLast(new MoveOrderItem(move,
								zobrist.score));
					}
				} else {
					sortedMoveList.add(new MoveOrderItem(move, zobrist.score));
				}

			} else {
				/* darf eigentlich nicht passieren... */
				sortedMoveList.addLast(new MoveOrderItem(move, -1000000));
			}

		}

		LinkedList<Move> resultList = new LinkedList<Move>();

		/* Jetzt eine normale Liste erzeugen! */
		for (MoveOrderItem moveItem : sortedMoveList) {
			resultList.add(moveItem.getMove());
		}

		return resultList;

	}

}
