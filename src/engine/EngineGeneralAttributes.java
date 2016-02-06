package engine;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import network.MessageOutputWriter;

import board.Constants;

public class EngineGeneralAttributes {

	// Generelle Spiel-Attribute
	private int size = 4;
	private int evaluation = Constants.Evaluation.SIMPLE;
	private boolean competitionMode = false;
	
	// space
	private int space = 200;
	private int difference = 2;
	private int monotonicity = 20;
	private int biggestValueEval = 20;
	private boolean logging = true;
	private int biggestValueInCornerEval = 50;
	private int highNumberBonus = 10;
	private int sameTilesAdjacent = 1;
	private int biggestNumbersInFirstRow = 0;

	private int depth = 10;

	private double moveTime = 10000.0;

	/*************************************************************************************************************************/
	/**
	 * Attribute, die für die Engine relevant sind aus einem Konfigurationsfile
	 * laden!
	 */
	/*************************************************************************************************************************/

	// Der Typ der Engine (alphabeta ...)
	private int engineType = 1;
	// Search-depth. Entspricht der variable 'this.depth' und wird nur genutzt,
	// wenn != -1.
	private int searchDepth = -1;
	// Move-Ordering aktiv?
	private boolean moveOrdering = false;
	// Memory nutzen?
	private boolean useMemory = false;
	// Parallele Berechnung?
	private boolean useParallelization = false;
	// Eine Instanz des MessageWriters zum Loggen von Nachrichten.
	private MessageOutputWriter messageWriter;

	/* Konstruktor */
	public EngineGeneralAttributes(MessageOutputWriter messageWriter) {
		this.messageWriter = messageWriter;
	}

	/**
	 * Läd ein paar Konfigurationen für die Engine aus einem Conf-file.
	 */
	public void loadEngineConfigFromFile() {

		InputStream fis;
		BufferedReader br;
		String line;

		try {
			fis = new FileInputStream(Constants.Files.CONFFILE);

			br = new BufferedReader(new InputStreamReader(fis,
					Charset.forName("UTF-8")));
			while ((line = br.readLine()) != null) {

				/* Engine-Typ */
				if (line.matches("engine = (minimax|alphabeta|negascout|mtdf|mcts|iterativedeepening)")) {
					String[] tmpArray = line.split(" ");

					if (tmpArray[2].equals("minimax")) {
						engineType = Constants.EngineType.MINIMAX;
					} else if (tmpArray[2].equals("alphabeta")) {
						engineType = Constants.EngineType.ALPHABETA;
					} else if (tmpArray[2].equals("iterativedeepening")) {
						engineType = Constants.EngineType.ITERATIVEDEEPENING;
					} else if (tmpArray[2].equals("negascout")) {
						engineType = Constants.EngineType.NEGASCOUT;
					} else if (tmpArray[2].equals("mtdf")) {
						engineType = Constants.EngineType.MTDF;
					} else if (tmpArray[2].equals("mcts")) {
						engineType = Constants.EngineType.MCTS;
					}
					messageWriter.writeLog("log Engine set to " + tmpArray[2]);
				}

				/* Iterationstiefe festlegen */
				if (line.matches("depth = [0-9]+")) {
					searchDepth = Integer.parseInt(line.split(" ")[2]);
					depth = searchDepth;
					messageWriter.writeLog("log search depth set to "
							+ line.split(" ")[2]);
				}

				/* Move-Ordering festlegen */
				if (line.matches("moveordering = (true|false)")) {
					if (line.split(" ")[2].equals("false")) {
						messageWriter.writeLog("log set moveOrdering to false");
						this.moveOrdering = false;
					} else if (line.split(" ")[2].equals("true")) {
						messageWriter.writeLog("log set moveOrdering to true");
						this.moveOrdering = true;
					}
				}

				/* Memory-Nutzung festlegen */
				if (line.matches("usememory = (true|false)")) {
					if (line.split(" ")[2].equals("false")) {
						messageWriter.writeLog("log set useMemory to false");
						this.useMemory = false;
					} else {
						messageWriter.writeLog("log set useMemory to true");
						this.useMemory = true;
					}
				}

				/* Memory-Nutzung festlegen */
				if (line.matches("useparallelization = (true|false)")) {
					if (line.split(" ")[2].equals("false")) {
						messageWriter
								.writeLog("log set parallelization to false");
						this.useParallelization = false;
					} else {
						messageWriter
								.writeLog("log set parallelization to true");
						this.useParallelization = true;
					}
				}

				/* Globale Variablen festlegen */
				if (line.matches("space = [0-9]+")) {
					this.space = Integer.parseInt(line.split(" ")[2]);
				}
				if (line.matches("difference = [0-9]+")) {
					this.difference = Integer.parseInt(line.split(" ")[2]);
				}
				if (line.matches("monotonicity = [0-9]+")) {
					this.monotonicity = Integer.parseInt(line.split(" ")[2]);
				}
				if (line.matches("biggestValueEval = [0-9]+")) {
					this.biggestValueEval = Integer
							.parseInt(line.split(" ")[2]);
				}
				if (line.matches("logging = (true|false)")) {
					this.logging = line.split(" ")[2].equals("true");
				}
				if (line.matches("biggestValueInCornerEval = [0-9]+")) {
					this.biggestValueInCornerEval = Integer.parseInt(line
							.split(" ")[2]);
				}
				if (line.matches("highNumberBonus = [0-9]+")) {
					this.highNumberBonus = Integer.parseInt(line.split(" ")[2]);
				}
				if (line.matches("sameTilesClose = [0-9]+")) {
					this.sameTilesAdjacent = Integer
							.parseInt(line.split(" ")[2]);
				}
				if (line.matches("biggestNumbersInFirstRow = [0-9]+")) {
					this.biggestNumbersInFirstRow = Integer.parseInt(line
							.split(" ")[2]);
				}
				if (line.matches("size = [0-9]+")) {
					this.size = Integer.parseInt(line.split(" ")[2]);
				}
				if (line.matches("evaluation = (simple|basic)")) {
					this.evaluation = line.split(" ")[2].equals("simple") ? Constants.Evaluation.SIMPLE
							: (line.split(" ")[2].equals("basic") ? Constants.Evaluation.BASIC
									: Constants.Evaluation.SIMPLE);

				}
				if (line.matches("competitionMode = (true|false)")) {
					this.competitionMode = line.split(" ")[2].equals("true");
				}
			}

			// Done with the file
			br.close();

		} catch (Exception e) {
		}
	}

	
	
	public boolean isCompetitionMode() {
		return competitionMode;
	}

	public void setCompetitionMode(boolean competitionMode) {
		this.competitionMode = competitionMode;
	}

	public int getEvaluation() {
		return evaluation;
	}

	public void setEvaluation(int evaluation) {
		this.evaluation = evaluation;
	}

	public int getBiggestNumbersInFirstRow() {
		return biggestNumbersInFirstRow;
	}

	public void setBiggestNumbersInFirstRow(int biggestNumberInFirstRow) {
		this.biggestNumbersInFirstRow = biggestNumberInFirstRow;
	}

	public int getHighNumberBonus() {
		return highNumberBonus;
	}

	public void setHighNumberBonus(int highNumberBonus) {
		this.highNumberBonus = highNumberBonus;
	}

	public int getBiggestValueInCornerEval() {
		return biggestValueInCornerEval;
	}

	public void setBiggestValueInCornerEval(int biggestValueInCornerEval) {
		this.biggestValueInCornerEval = biggestValueInCornerEval;
	}

	public int getBiggestValueEval() {
		return biggestValueEval;
	}

	public void setBiggestValueEval(int biggestValueEval) {
		this.biggestValueEval = biggestValueEval;
	}

	public int getSameTilesAdjacent() {
		return sameTilesAdjacent;
	}

	public void setSameTilesAdjacent(int sameTilesAdjacent) {
		this.sameTilesAdjacent = sameTilesAdjacent;
	}

	public int getSpace() {
		return space;
	}

	public void setSpace(int space) {
		this.space = space;
	}

	public int getDifference() {
		return difference;
	}

	public void setDifference(int difference) {
		this.difference = difference;
	}

	public int getMonotonicity() {
		return monotonicity;
	}

	public void setMonotonicity(int monotonicity) {
		this.monotonicity = monotonicity;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}
	
	public void setDepth(int depth) {
		this.depth = depth;
	}

	/**
	 * Speichert die globalen Attribute und Optionen
	 */
	public void setOption(String name, String value) {
		name = name.toLowerCase();

		messageWriter.writeLog("log set Option '" + name + " = " + value + "'");

		if (name.equals("depth")) {
			/*
			 * Die Einstellung des eigenen Config-Files ist immer im Recht, vor
			 * dem Controller!!!
			 */
			if (this.searchDepth == -1)
				this.depth = Integer.parseInt(value);
		} else {
			messageWriter.sendMessage("log WARNING: Option '" + name + " = "
					+ value + "' not recognized.");
		}

	}

	public boolean getLogging() {
		return logging;
	}

	public void setLogging(boolean logging) {
		this.logging = logging;
	}

	public int getDepth() {
		return depth;
	}

	public int getEngineType() {
		return engineType;
	}

	public int getSearchDepth() {
		return searchDepth;
	}

	public boolean getMoveOrdering() {
		return moveOrdering;
	}

	public boolean getUseMemory() {
		return useMemory;
	}

	public boolean getUseParallelization() {
		return useParallelization;
	}

	public MessageOutputWriter getWriter() {
		return this.messageWriter;
	}

	public double getMoveTime() {
		return moveTime;
	}

	public void setMoveTime(double moveTime) {
		this.moveTime = moveTime;
	}
}
