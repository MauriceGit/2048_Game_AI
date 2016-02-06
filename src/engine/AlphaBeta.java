package engine;

import java.util.Iterator;
import java.util.LinkedList;

import memory.Transposition;
import memory.ZobristHash;
import network.MessageOutputWriter;
import board.Bitboard;
import board.Constants;
import board.Move;
import board.MoveRandom;
import board.MoveScore;
import evaluation.BasicEvaluation;
import evaluation.Evaluation;
import evaluation.SimpleEvaluation;

public class AlphaBeta extends Engine {

	/**
	 * Private Instanz der Memory-Haltung (Transposition-Table)
	 */
	private Transposition transposition;

	private long oldBoardHash;

	private int oldScore = 0;

	private int nodesVisited = 0;

	private Evaluation evaluation;

	private MoveOrdering moveOrder;

	/**
	 * Konstruktor
	 */
	public AlphaBeta(MessageOutputWriter messageWriter,
			EngineGeneralAttributes generalAttributes) {
		super(messageWriter, generalAttributes);
		this.transposition = new Transposition();
		this.transposition.initTranspsition();
	}

	/**
	 * Errechnet den Alpha-Beta-Wert für eine bestimmte Rekursionstiefe eines
	 * Root-Knotens.
	 * 
	 * @param board
	 *            Der Root-Knoten
	 * @param depth
	 *            Rekursionstiefe
	 * @param maximizingPlayer
	 *            Der Spieler, für den der beste Zug ermittelt werden soll
	 * @param color
	 *            Die jeweilige Farbe
	 * @param moveNr
	 *            Überprüfung, dass 4 Züge pro Spieler möglich sind.
	 */
	public LinkedList<Move> alphaBeta(Bitboard board, int depth,
			boolean maximizingPlayer, int alpha, int beta, boolean useMemory,
			boolean moveOrdering, boolean normalSearch, int maxSearchDepth,
			boolean isQuiescence, boolean isPVNode, boolean isExtendedSearch) {

		/* Generiert Liste mit allen verfügbaren Moves! */
		LinkedList<Move> oneMoveList = moveGen.generateAllMoves(board,
				maximizingPlayer);
		int newAlpha = alpha;
		int newBeta = beta;
		int newDepth = depth;

		/* Rekursionsabbruch */
		if (depth <= 0 || oneMoveList.size() == 0 || !super.isAllowedToRun) {
			Move move;

			/* Kein Move-Ordering */
			if (normalSearch) {

				if (oneMoveList.size() == 0) {
					move = new MoveScore(-100000);
				} else {
					move = new MoveScore(evaluation.evaluateBoardState(board,
							isQuiescence));
				}
				nodesVisited++;

			} else {
				move = new MoveScore(
						evaluation.evaluateBoardState(board, false));
			}

			LinkedList<Move> res = new LinkedList<Move>();
			res.add(move);
			return res;
		}

		/* Memory-Nutzung! */
		if (useMemory) {

			ZobristHash zobrist = transposition.lookupHash(board
					.getZobristHash());

			/*
			 * einen Eintrag im Cache gefunden, der entweder auf der gleichen
			 * Rekursionstiefe erzeugt wurde, oder vorher. Wenn vorher heißt
			 * das, dass mit hoher Wahrscheinlichkeit im gleichen
			 * Auswertungsbaum schon mal die gleiche Boardkonstellation
			 * augetaucht ist - mit weniger Moves.
			 */
			if (zobrist != null
					// && (depth % 4) == 0

					&& zobrist.depth >= depth
					&& zobrist.maximizingPlayer == maximizingPlayer
			// && maximizingPlayer

			) {
				switch (zobrist.nodeType) {
				case Constants.NodeType.EXACT:

					LinkedList<Move> bestValue = new LinkedList<Move>();
					bestValue.add(new MoveScore(zobrist.score));
					return bestValue;

				case Constants.NodeType.ALPHA:

					/* neuer lower-bound -> Eingrenzen */
					if (zobrist.score > newAlpha) {
						newAlpha = zobrist.score;
					}

					break;
				case Constants.NodeType.BETA:

					/* neuer upper-bound -> Eingrenzen */
					if (zobrist.score < newBeta) {
						newBeta = zobrist.score;
					}

					break;
				}

				/*
				 * Vll haben wir durch die neuen Bounds ja grade einen perfekten
				 * Score geschaffen? Können wir prunen?
				 */
				if (newAlpha >= newBeta) {
					LinkedList<Move> bestValue = new LinkedList<Move>();
					bestValue.add(new MoveScore(zobrist.score));

					return bestValue;
				}
			}

		}

		/* Move-Ordering! */
		if (moveOrdering) {
			// oneMoveList = moveOrder.moveOrdering(board, oneMoveList, depth,
			// moveNr, maximizingPlayer, color, newAlpha, newBeta);
			oneMoveList = moveOrder.moveOrderingFromMemory(board, oneMoveList,
					transposition, depth);
		}

		/* Normales Alpha-Beta! */
		LinkedList<Move> bestValue = new LinkedList<Move>();
		ZobristHash zobristValue = null;

		if (useMemory) {
			zobristValue = new ZobristHash();
		}

		/*
		 * Performance-Optimierung - Nur noch eine eigene Instanz des Bitboards
		 * pro Rekursionsebene! (statt ca. 11.)
		 */
//		Bitboard newBoard = board.cloneBitboard();
		Bitboard newBoard = new Bitboard(generalAttributes);
		boolean isFirst = true;

		/* Liste mit Threads! */
		LinkedList<ParallelAlphaBetaWrapper> threadList = new LinkedList<ParallelAlphaBetaWrapper>();

		/* Schleife über alle Kinder */
		for (int i = 0; i < oneMoveList.size(); i++) {
			Move move = oneMoveList.get(i);

			Move moveApplied = move;

			LinkedList<Move> value = new LinkedList<Move>();

			if (isPVNode) {

				if (isFirst) {

					Iterator<Move> moveIt = oneMoveList.iterator();

					newBoard = board.softCloneBitboard(newBoard);

					newDepth = depth - 1;

					/* Zug anwenden */
					newBoard.applyMove(move);

					boolean extended = isExtendedSearch;

					/* Alles normal! */
					value = alphaBeta(newBoard, newDepth, !maximizingPlayer,
							newAlpha, newBeta, useMemory, moveOrdering,
							normalSearch, maxSearchDepth, isQuiescence,
							isPVNode, extended);

					if (moveIt.hasNext()) {
						moveIt.next();
					}

					int count = 0;

					/* Liste mit Threads erzeugen und starten -> Multithreading */
					while (moveIt.hasNext()) {
						Move tmpMove = moveIt.next();

						ParallelAlphaBetaWrapper parallelAlphaBeta = new ParallelAlphaBetaWrapper();

						newBoard = board.cloneBitboard();

						newDepth = depth - 1;

						/* Zug anwenden */
						newBoard.applyMove(tmpMove);

						boolean isaPVNode = false;
						extended = isExtendedSearch;

						parallelAlphaBeta.setAlphaBetaRunParams(this, tmpMove,
								newBoard, newDepth, !maximizingPlayer,
								newAlpha, newBeta, useMemory, moveOrdering,
								normalSearch, maxSearchDepth, isQuiescence,
								isaPVNode, extended);

						parallelAlphaBeta.newThread();
						threadList.add(parallelAlphaBeta);
						parallelAlphaBeta.getThread().start();
						count++;
					}

				} else { /* isFirst */

					if (i < threadList.size()) {
						/* Darf eigentlich niemals einen Überlauf geben hier... */
						ParallelAlphaBetaWrapper pAB = threadList.get(i - 1);
						try {
							pAB.getThread().join();
						} catch (InterruptedException e) {
						}

						value = pAB.getResultList();
						moveApplied = pAB.getMove();

						if (value == null) {
							value = new LinkedList<Move>();
						}
					} else {
						/* break könnte auch gehen... */
						continue;
					}
				}

			} else { /* isPV */

				newBoard = board.softCloneBitboard(newBoard);

				newDepth = depth - 1;

				/* Zug anwenden */
				newBoard.applyMove(move);

				/* Alles normal! */
				value = alphaBeta(newBoard, newDepth, !maximizingPlayer,
						newAlpha, newBeta, useMemory, moveOrdering,
						normalSearch, maxSearchDepth, isQuiescence, false,
						isExtendedSearch);

			}

			if (maximizingPlayer) {

				/*
				 * Den wichtigen, besten Move merken und als bestValue
				 * verzeichnen.
				 */
				if (((MoveScore) value.getLast()).getScore() > newAlpha) {

					bestValue = value;
					bestValue.addFirst(moveApplied);
					newAlpha = ((MoveScore) value.getLast()).getScore();

				}

				/* Pruning! -> Beta cut-off */
				if (newBeta <= newAlpha) {
					if (useMemory) {
						zobristValue.nodeType = Constants.NodeType.ALPHA;
					}

					/* Mögliche aktive Threads platt machen!!! */
					Iterator<ParallelAlphaBetaWrapper> it = threadList
							.iterator();
					while (it.hasNext()) {
						it.next().getThread().interrupt();
					}

					break;
				}

			} else {

				/*
				 * Den wichtigen, besten Move merken und als bestValue
				 * verzeichnen.
				 */
				if (((MoveScore) value.getLast()).getScore() < newBeta) {

					bestValue = value;
					bestValue.addFirst(moveApplied);
					newBeta = ((MoveScore) value.getLast()).getScore();

				}

				/* Pruning -> Alpha cut-off */
				if (newBeta <= newAlpha) {
					if (useMemory) {
						zobristValue.nodeType = Constants.NodeType.BETA;
					}

					/* Mögliche aktive Threads platt machen!!! */
					Iterator<ParallelAlphaBetaWrapper> it = threadList
							.iterator();
					while (it.hasNext()) {
						it.next().getThread().interrupt();
					}

					break;
				}

			}

			isFirst = false;
		}

		if (bestValue.size() == 0) {
			if (maximizingPlayer) {
				bestValue.add(new MoveScore(newAlpha));
			} else {
				bestValue.add(new MoveScore(newBeta));
			}
		}

		if (useMemory) {
			zobristValue.nodeType = (zobristValue.nodeType == Constants.NodeType.BETA) ? Constants.NodeType.BETA
					: (zobristValue.nodeType == Constants.NodeType.ALPHA ? Constants.NodeType.ALPHA
							: Constants.NodeType.EXACT);

			/* Füge die aktuelle Position in das Table ein! */
			zobristValue.depth = depth;
			zobristValue.hash = board.getZobristHash();
			zobristValue.score = ((MoveScore) bestValue.getLast()).getScore();
			zobristValue.maximizingPlayer = maximizingPlayer;

			synchronized (transposition.getClass()) {
				transposition.addTableEntry(zobristValue);
			}
		}

		return bestValue;
	}

	public void run() {

		/* Minus infinity */
		int alpha = -1000000000;
		/* Infinity */
		int beta = 1000000000;

		super.leafNodesVisited = 0;
		boolean moveOrderingSet = false;

		long startTime = System.currentTimeMillis();

		/*
		 * Dem Board eine Instanz des Transposition-Tables mitgeben, damit damit
		 * ein Makemove funktioniert
		 */
		board.setTransposition(transposition);

		/*
		 * Den Zobrist-Hash bei jedem neuen Zug neu initialisieren!
		 */
		// board.setZobristHash(transposition.generateZobristHashForTable(board));

		/*
		 * das Transposition-Table neu aufsetzen, dass alte Memory-Einträge
		 * gelöscht werden!
		 */
		if (generalAttributes.getUseMemory()) {
			transposition.clearTable();
		}

		if (generalAttributes.getEvaluation() == Constants.Evaluation.SIMPLE) {
			evaluation = new SimpleEvaluation(generalAttributes);
		} else {
			if (generalAttributes.getEvaluation() == Constants.Evaluation.BASIC) {
				evaluation = new BasicEvaluation(generalAttributes);
			} else {
				evaluation = new SimpleEvaluation(generalAttributes);
			}
		}

		oldScore = evaluation.evaluateBoardState(board, false);

		if (generalAttributes.getMoveOrdering()
				&& super.generalAttributes.getDepth() >= 6) {
			moveOrder = new MoveOrdering(generalAttributes, messageWriter);
			moveOrderingSet = true;
		}

		int newDepth = super.generalAttributes.getDepth();

//		if (board.getBiggestValue() >= 1024) {
//			newDepth++;
//			if (board.getBiggestValue() >= 2048) {
//				newDepth++;
//			}
//		}

		super.bestMove = alphaBeta(board, newDepth, true, alpha, beta,
				super.generalAttributes.getUseMemory(), moveOrderingSet, true,
				super.generalAttributes.getDepth(), false,
				super.generalAttributes.getUseParallelization(), false);

//		System.out.println(bestMove);
//
		long estimatedTime = System.currentTimeMillis() - startTime;
//
//		messageWriter.sendMessage("log finished calculating best move"
//				+ ((super.isAllowedToRun == true) ? " all by himself :-)"
//						: ", being interrupted by the timer..."));
//
//		messageWriter.sendMessage("log time used (ms): " + estimatedTime + ", depth: " + newDepth);
//
//		messageWriter.sendMessage("log old score: "
//				+ evaluation.evaluateBoardState(board, false));
//
//		messageWriter.sendMessage("log score: "
//				+ ((MoveScore) super.bestMove.getLast()).getScore());
//
//		messageWriter.sendMessage("log node count: " + nodesVisited);
//
//		messageWriter.sendMessage("log used memory: "
//				+ (Runtime.getRuntime().totalMemory() - Runtime.getRuntime()
//						.freeMemory()) / (1024 * 1024) + "mb");

		/* Ausgabe des besten Moves! */
		if (super.printBestMove) {
			
			if (generalAttributes.isCompetitionMode()) {
				if (getBestMove().equals("exit")) {
					messageWriter.sendMessage("quit");
					System.exit(0);
				}
				
				messageWriter.sendMessage(getBestMove());
			} else {
				if (getBestMove().equals("exit")) {
					messageWriter.sendMessage("quit");
					System.exit(0);
				}
				
				board.applyMove(bestMove.getFirst());
				messageWriter.sendMessage(board.getStringRepresentation());
			}
			
			messageWriter.writeGameWatchersLog(board.toString());
			
		}
	}

	public void setOldBoardHash(long oldBoardHash) {
		this.oldBoardHash = oldBoardHash;
	}

	public int getNodesVisited() {
		return this.nodesVisited;
	}

	public void setEvaluation(Evaluation evaluation) {
		this.evaluation = evaluation;
	}

	public void setMoveOrdering(MoveOrdering moveOrder) {
		this.moveOrder = moveOrder;
	}

	/**
	 * Da ein Thread niemals mehrfach gestartet werden kann, muss eine neue
	 * Instanz erzeugt werden. Alle Variablen, die zum Lauf relevant sind werden
	 * neu initialisiert.
	 */
	public void newThread() {
		super.ownThread = new Thread(this);
		super.bestMove = new LinkedList<Move>();
		super.isAllowedToRun = true;
	}

}