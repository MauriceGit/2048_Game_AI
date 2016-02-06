package engine;

import java.util.LinkedList;

import network.MessageOutputWriter;
import board.Bitboard;
import board.Constants;
import board.Move;
import board.MoveNormal;
import board.MoveRandom;

public class EngineController {

	/* Instanz auf die Engine */
	private Engine engine;

	/* Instanz des zugehörigen Threads */
	private Thread engineThread;

	/* Instanz auf die Zeitverwaltung der Engine */
	private EngineTimeController engineTimeController;

	/* Thread mit Zeitverwaltung der Engine */
	private Thread timeControllerThread;

	/* Attribute der Engine */
	private EngineGeneralAttributes engineGeneralAttributes;

	/* Instanz des Message-Writers */
	private MessageOutputWriter messageWriter;

	/* Die korrekte momentane Boardkonfiguration. Korrekter Zustand! */
	private Bitboard board;

	public EngineController(EngineGeneralAttributes engineGeneralAttributes,
			MessageOutputWriter messageWriter) {

		/* Konfigurieren der Search Engine. */
		switch (engineGeneralAttributes.getEngineType()) {
		case Constants.EngineType.ALPHABETA:
			engine = new AlphaBeta(messageWriter, engineGeneralAttributes);
//			messageWriter
//					.sendMessage("log engine type: ALPHABETA. Evaluation: "
//							+ (engineGeneralAttributes.getEvaluation() == Constants.Evaluation.SIMPLE ? "Simple"
//									: "Basic") + ". Depth: "
//							+ engineGeneralAttributes.getDepth()
//							+ ". MoveOrdering: "
//							+ engineGeneralAttributes.getMoveOrdering()
//							+ ". Memory: "
//							+ engineGeneralAttributes.getUseMemory()
//							+ ". Parallelization: "
//							+ engineGeneralAttributes.getUseParallelization()
//							+ ".");
			break;
		case Constants.EngineType.ITERATIVEDEEPENING:
			engine = new IterativeDeepening(messageWriter,
					engineGeneralAttributes);
			messageWriter
					.sendMessage("log engine type: ITERATIVEDEEPENING. Depth: "
							+ engineGeneralAttributes.getDepth()
							+ ". MoveOrdering: "
							+ engineGeneralAttributes.getMoveOrdering()
							+ ". Memory: "
							+ engineGeneralAttributes.getUseMemory()
							+ ". Parallelization: "
							+ engineGeneralAttributes.getUseParallelization()
							+ ".");
			break;
		}

		engineTimeController = new EngineTimeController(engine,
				engineGeneralAttributes, messageWriter);
		this.engineGeneralAttributes = engineGeneralAttributes;
		this.messageWriter = messageWriter;
		this.board = new Bitboard(engineGeneralAttributes);

	}

	/**
	 * Aktualisiert den Boardzustand mit einem neuen Move
	 * 
	 * @param move
	 *            Der Move
	 */
	public void makeMove(String move) {

		String[] array = move.split(",");

		for (int i = 0; i < array.length; i++) {
			board.setBoardValue(i % engineGeneralAttributes.getSize(), engineGeneralAttributes.getSize() -  (i
					/ engineGeneralAttributes.getSize())-1,
					Integer.parseInt(array[i]));
		}

	}
	
	public void adoptBoard(String numbers) {

		String moves = numbers.replaceAll("\\(", "").replaceAll("\\)", "").replaceAll("\\[", "").replaceAll("\\]", "");

		makeMove (moves);

	}

	/**
	 * Printet das aktuelle Board nach StdOut aus. --> Debugg-Funktion!!!!
	 */
	public void printBoard() {

		// System.out.println(board.toString());
		messageWriter.sendMessage("\n" + board.toString());

	}

	private int getBestNumber() {
		int max = 0;
		for (int i = 0; i < engineGeneralAttributes.getSize()
				* engineGeneralAttributes.getSize(); i++) {
			if (board.getBoardValue(i % engineGeneralAttributes.getSize(), i
					/ engineGeneralAttributes.getSize()) > max) {
				max = board.getBoardValue(
						i % engineGeneralAttributes.getSize(), i
								/ engineGeneralAttributes.getSize());
			}
		}
		return max;
	}

	public void applyMove(String dir) {

		int dirInt = dir.equals("left") ? Constants.Direction.LEFT : (dir
				.equals("right") ? Constants.Direction.RIGHT : (dir
				.equals("up") ? Constants.Direction.UP
				: (dir.equals("down") ? Constants.Direction.DOWN
						: Constants.Direction.NONE)));

		if (dirInt != Constants.Direction.NONE) {
			Move move = new MoveNormal(dirInt);

			messageWriter.writeGameWatchersLog("apply " + dir);

			this.board.applyMove(move);

			/* Erzeugt random 2 oder 4 auf einem der freien Felder */
			LinkedList<Move> moveList = new LinkedList<Move>();
			for (int i = 0; i < engineGeneralAttributes.getSize()
					* engineGeneralAttributes.getSize(); i++) {
				if (board.isEmptyField(i % engineGeneralAttributes.getSize(), i
						/ engineGeneralAttributes.getSize())) {

					moveList.add(new MoveRandom(i
							% engineGeneralAttributes.getSize(), i
							/ engineGeneralAttributes.getSize(), 2));
					moveList.add(new MoveRandom(i
							% engineGeneralAttributes.getSize(), i
							/ engineGeneralAttributes.getSize(), 2));
					moveList.add(new MoveRandom(i
							% engineGeneralAttributes.getSize(), i
							/ engineGeneralAttributes.getSize(), 2));
					moveList.add(new MoveRandom(i
							% engineGeneralAttributes.getSize(), i
							/ engineGeneralAttributes.getSize(), 2));
					moveList.add(new MoveRandom(i
							% engineGeneralAttributes.getSize(), i
							/ engineGeneralAttributes.getSize(), 2));
					moveList.add(new MoveRandom(i
							% engineGeneralAttributes.getSize(), i
							/ engineGeneralAttributes.getSize(), 2));
					moveList.add(new MoveRandom(i
							% engineGeneralAttributes.getSize(), i
							/ engineGeneralAttributes.getSize(), 2));
					moveList.add(new MoveRandom(i
							% engineGeneralAttributes.getSize(), i
							/ engineGeneralAttributes.getSize(), 2));
					moveList.add(new MoveRandom(i
							% engineGeneralAttributes.getSize(), i
							/ engineGeneralAttributes.getSize(), 2));
					moveList.add(new MoveRandom(i
							% engineGeneralAttributes.getSize(), i
							/ engineGeneralAttributes.getSize(), 4));
				}
			}

			if (moveList.size() == 0) {

				messageWriter
						.sendMessage("No more Space available to place a new number - Program exits.\nBeste Zahl: "
								+ getBestNumber());
				messageWriter
						.writeGameWatchersLog("No more Space available - Program exits.");
				System.exit(0);
			}

			int pos = (int) (Math.random() * 100.0) % moveList.size();

			board.applyMove(moveList.get(pos));

			moveList = null;

			messageWriter.writeGameWatchersLog(board.toString());
		} else {
			messageWriter.sendMessage("Move not valid - Program exits.");
			messageWriter
					.writeGameWatchersLog("Move not valid - Program exits.\nBeste Zahl: "
							+ getBestNumber());
			System.exit(0);
		}

	}

	public void addNumberAt(String what, String where) {

		int number = Integer.parseInt(what);
		int pos = Integer.parseInt(where);

		this.board.setBoardValue(pos % engineGeneralAttributes.getSize(), pos
				/ engineGeneralAttributes.getSize(), number);
	}

	/**
	 * Beginnt ein neues Spiel. Also leeren des Boards, etc...
	 */
	public void newGame() {

		board.generateEmptyBitboard();
	}

	/**
	 * Printet die aktuelle Spielsituation in die Logdatei.
	 */
	public void logBitboard() {
		messageWriter.writeLog(board.toString());
	}

	/**
	 * Startet die Engine als Thread mit dem dazugehörigen Time-Controller.
	 */
	public void go() {

		/********************** STOP ************************/

		engine.setPrintBestMove(false);
		engine.stopThread();
		try {
			engine.getOwnThread().join();
		} catch (InterruptedException e) {
			messageWriter.sendMessage("log InterruptedException beim join.");
		}

		engineTimeController.getOwnThread().interrupt();
		try {
			engineTimeController.getOwnThread().join();
		} catch (InterruptedException e) {
			messageWriter.sendMessage("log InterruptedException beim join.");
		}

		/********************** START ***********************/

		/* Startet die Engine selber */
		engine.setPrintBestMove(true);
		engine.setBoard(board);

		engine.newThread();
		engineThread = engine.getOwnThread();
		engineThread.start();

		/* Startet die Zeitkontrolle/Messung für die Engine */
		engineTimeController.newThread();
		engineTimeController.setEngineThread(engineThread);
		timeControllerThread = engineTimeController.getOwnThread();
		timeControllerThread.start();
	}

	/**
	 * Stoppt die Engine und setzt das Flag, dass das best-erreichteste Ergebnis
	 * ausgegeben wird.
	 */
	public void stop() {

		engine.setPrintBestMove(true);

		engine.stopThread();
		try {
			engine.getOwnThread().join();
		} catch (InterruptedException e) {
			messageWriter.sendMessage("log InterruptedException beim join.");
		}

		engineTimeController.stop();
		timeControllerThread.interrupt();
		try {
			timeControllerThread.join();
		} catch (InterruptedException e) {
			messageWriter.sendMessage("log InterruptedException beim join.");
		}

	}

	/**
	 * Genau, wie stop() aber ohne die Ausgabe des bestmove am Ende. Stilles
	 * Beenden.
	 */
	public void kill() {

		// Kein Ausprinten des Ergebnisses!
		engine.setPrintBestMove(false);

		// Stoppen des Engine-Threads
		engine.stopThread();
		try {
			engine.getOwnThread().join();
		} catch (InterruptedException e) {
			messageWriter.sendMessage("log InterruptedException beim join.");
		}

		// Stoppen des Timer-Threads.
		// --------------> Unter Umständen ändern auf .interrupt(), um ihn hart
		// zu beenden (Um Verzögerung zu vermeiden, wenn der Timer nur alle paar
		// Sekunden aufgerufen wird und auf eine Beendigung checkt...)
		engineTimeController.stop();
		if (timeControllerThread != null) {
			timeControllerThread.interrupt();
			try {
				timeControllerThread.join();
			} catch (InterruptedException e) {
				messageWriter
						.sendMessage("log InterruptedException beim join.");
			}
		}
	}

	/**
	 * Ganz böses Beenden ohne Rücksicht auf Verluste!!!
	 */
	public void interrupt() {
		engine.setPrintBestMove(false);
		engine.getOwnThread().interrupt();
		if (timeControllerThread != null)
			timeControllerThread.interrupt();
	}

	public void getBestMove() {

		engine.setPrintBestMove(true);

		// sollte eig. immer tot sein hier.
		engine.stopThread();

		try {
			engine.getOwnThread().join();
		} catch (InterruptedException e) {
			messageWriter.sendMessage("log InterruptedException beim join.");
		}

	}

}
