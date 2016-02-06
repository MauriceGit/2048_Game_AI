package board;

/**
 * Klasse, die Konstanten zu Spielinformationen, wie Figuren und Farben
 * beinhaltet.
 * 
 * @author maurice
 * 
 */
public class Constants {

	public static class Evaluation {
		public static final int SIMPLE = 0;
		public static final int BASIC = 1;
	}

	public static class Direction {
		public static final int LEFT = 1;
		public static final int RIGHT = 2;
		public static final int UP = 3;
		public static final int DOWN = 4;
		public static final int NONE = 0;
	}

	/**
	 * Mögliche Filenamen.
	 */
	public static class Files {
		public static final String LOGFILE = "2048_bot.log";
		public static final String WATCHERS_LOGFILE = "2048_watch_game.log";
		public static final String CONFFILE = "2048_bot.conf";
	}

	/**
	 * Interne Klasse zur Konfiguration von möglichen Engine-Typen.
	 */
	public static class EngineType {
		public static final int MINIMAX = 0;
		public static final int ALPHABETA = 1;
		public static final int NEGASCOUT = 2;
		public static final int MTDF = 3;
		public static final int MCTS = 4;
		public static final int ITERATIVEDEEPENING = 5;
	}

	/**
	 * Interne Klasse mit Movetypes
	 */
	public static class MoveType {
		public static final int NORMAL = 0;
		public static final int RANDOM = 1;
		public static final int SCORE = 2;
	}

	/**
	 * Interne Klasse mit Node-Types für die Memory.
	 */
	public static class NodeType {
		public static final int EXACT = 0;
		public static final int ALPHA = 1;
		public static final int BETA = 2;
		public static final int NOTHING = 3;
	}

}
