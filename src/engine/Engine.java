package engine;

import java.util.LinkedList;

import network.MessageOutputWriter;
import board.Bitboard;
import board.Constants;
import board.Move;
import board.MoveGenerator;
import board.MoveNormal;

/**
 * Die eigentliche Engine, die als Thread gestartet wird und die Berechnung des
 * besten Zuges, Evaluation etc. regelt/ausführt.
 * 
 * @author maurice
 * 
 */
public abstract class Engine implements Runnable {

	/************************************************************/
	/********************* Variablen ************************/
	/************************************************************/

	/**
	 * Verweis auf den eigenen Thread (der ausschließlich hier verwaltet wird!).
	 */
	protected Thread ownThread;

	/**
	 * Der String mit dem aktuellen besten Move für den Server.
	 */
	protected LinkedList<Move> bestMove;

	/**
	 * Ob der beste Move beim Beenden des Threads überhaupt ausgegeben werden
	 * soll oder nicht.
	 */
	protected boolean printBestMove;

	/**
	 * Wieviele Endknoten analysiert und besucht wurden bei der Rekursion.l
	 */
	protected int leafNodesVisited = 0;

	/**
	 * Boolean über den der Thread/Engine kontrolliert beendet werden kann.
	 */
	protected boolean isAllowedToRun;

	/**
	 * Verweis auf die Writer-Instanz zur Kommunikation mit dem Server (Senden
	 * v. Nachrichten).
	 */
	protected MessageOutputWriter messageWriter;

	/**
	 * Instanz der Root-Position des Boards. Also des aktuellen Zustands!
	 */
	protected Bitboard board;

	/**
	 * Instanz auf die Klasse mit Attributen.
	 */
	protected EngineGeneralAttributes generalAttributes;

	/**
	 * Kann eine Liste mit allen möglichen Zügen generieren.
	 */
	protected MoveGenerator moveGen;

	/****************************************************************/
	/********************* Konstruktor **************************/
	/****************************************************************/

	public Engine(MessageOutputWriter messageWriter,
			EngineGeneralAttributes generalAttributes) {
		this.ownThread = new Thread(this);
		this.bestMove = new LinkedList<Move>();
		this.isAllowedToRun = true;
		this.printBestMove = false;
		this.messageWriter = messageWriter;
		this.generalAttributes = generalAttributes;
		this.moveGen = new MoveGenerator(generalAttributes);
	}

	/**
	 * gibt eine Instanz des eigenen Threads zurück.
	 * 
	 * @return Instanz des eig. Threads.
	 */
	public Thread getOwnThread() {
		return ownThread;
	}

	/**
	 * Die komplette eigentliche Berechnung des besten Moves!
	 */
	public abstract void run();

	/**
	 * Ermittelt eine Stringrekonstruktion aus dem Bestmove, die der Controller
	 * verstehen kann.
	 * 
	 * @return Stringrepresentation der Moves.
	 */
	public String getBestMove() {
//		String move = "bestmove ";
//		move += bestMove.getFirst().toString();
//		return move;
		
		return bestMove.getFirst().toString();
	}

	public void stopThread() {
		this.isAllowedToRun = false;
	}

	public void setPrintBestMove(boolean flag) {
		this.printBestMove = flag;
	}

	/**
	 * Da ein Thread niemals mehrfach gestartet werden kann, muss eine neue
	 * Instanz erzeugt werden. Alle Variablen, die zum Lauf relevant sind werden
	 * neu initialisiert.
	 */
	public abstract void newThread();

	/**
	 * Setzt die private Instanz des Boards vor jeder neuen Berechnung auf die
	 * aktuelle Position!
	 * 
	 * @param board
	 *            die aktuelle Boardkonfiguration des Spielfeldes
	 */
	public void setBoard(Bitboard board) {
		this.board = board;
	}

	/**
	 * Errechnet den maximalen Wert und gibt diesen zurück.
	 * 
	 * @param x
	 *            ein Wert
	 * @param y
	 *            nochn Wert
	 * @return Der Höchste.
	 */
	public int max(int x, int y) {
		return (x >= y) ? x : y;
	}

	/**
	 * Errechnet den minimalen Wert und gibt diesen zurück.
	 * 
	 * @param x
	 *            ein Wert
	 * @param y
	 *            nochn Wert
	 * @return Der Niedrigste.
	 */
	public int min(int x, int y) {
		return (x <= y) ? x : y;
	}

}
