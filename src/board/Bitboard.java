package board;

import java.util.Arrays;
import java.util.LinkedList;

import engine.EngineGeneralAttributes;

import memory.Transposition;
import network.MessageOutputWriter;

/**
 * Implementierung der abstrakten Board-Klasse. Die abstrakte Teil ist
 * eigentlich nicht notwendig (zumindest bei der Benennung der Methoden und
 * Rückgabewerte... Aber kann man später auch noch ändern...)
 * 
 * @author maurice
 * 
 */
public class Bitboard {

	/************************************************************/
	/********************* __Variablen__ ************************/
	/************************************************************/

	/**
	 * Instanz mit dem Bitboards.
	 */
	private long[][] bitboard = null;

	/**
	 * Hash-Wert dieses Boards!
	 */
	private long hash = 0L;

	/**
	 * Eine Instanz des Transposition-Tables, damit der MakeMove funktioniert
	 * und den 'hash' korrekt updated.
	 */
	private Transposition transposition = null;

	/**
	 * Instanz der Attribute.
	 */
	private EngineGeneralAttributes generalAttributes;

	/**
	 * PERFORMANCE ENHANCING VARIABLES
	 */
	private long generalBitmapP = 0L;

	/****************************************************************/
	/********************* _Konstruktoren_ **************************/
	/****************************************************************/

	public Bitboard(EngineGeneralAttributes generalAttributes) {

		//this.bitboard = new long[generalAttributes.getSize()][generalAttributes
		//		.getSize()];
		this.generalAttributes = generalAttributes;

		generateEmptyBitboard();
	}

	public Bitboard(EngineGeneralAttributes generalAttributes, long[][] board,
			long[] masks) {
		this.bitboard = board;
		this.generalAttributes = generalAttributes;
	}

	/****************************************************************/
	/********************* Initialisierung **************************/
	/****************************************************************/

	/**
	 * Erzeugt ein zweidimensionales Array für alle versch. Figuren und die
	 * Farben (Gold/Silber).
	 * 
	 * Für jede Figur sind in einem 'long' alle Positionen dieser Figurart
	 * verzeichnet. Z.B.: ...0001000000000111...
	 * 
	 * Damit sind für Gold- und Silber alle Figuren auf dem Feld festgelegt und
	 * zugreifbar.
	 * 
	 * Es ist also ein Array der Größe 'long[6][2]' (6 versch. Figuren, 2
	 * Farben).
	 * 
	 * @return Eine Definition des Spielfeldes unterteilt in Figuren und Farbe
	 *         als Long-Array.
	 */
	public void generateEmptyBitboard() {

		this.bitboard = new long[generalAttributes.getSize()][generalAttributes
		                                      				.getSize()];
		
		/* Bitboard initialisieren mit leerem 'long' */
		for (int i = 0; i < generalAttributes.getSize(); i++) {
			for (int j = 0; j < generalAttributes.getSize(); j++) {
				this.bitboard[i][j] = 0;
				this.bitboard[i][j] = 0;
			}
		}

	}

	/****************************************************************/
	/********************* ____ACTION_____ **************************/
	/****************************************************************/

	/**
	 * Prüft, ob ein Feld leer ist.
	 * 
	 * @param col
	 *            col
	 * @param row
	 *            row
	 * @return obs leer ist.
	 */
	public boolean isEmptyField(int col, int row) {

		return bitboard[col][row] == 0;

	}

	public int getBiggestValue() {
		int max = -1000;

		for (int i = 0; i < generalAttributes.getSize(); i++) {
			for (int j = 0; j < generalAttributes.getSize(); j++) {
				if (getBoardValue(i, j) > max)
					max = getBoardValue(i, j);
			}
		}
		return max;
	}

	/**
	 * Printet das komplette Spielfeld mit allen Figuren.
	 */
	public void printBitboard() {
		MessageOutputWriter messageWriter = new MessageOutputWriter();

		String bitBoardString = toString();

		messageWriter.sendMessage(bitBoardString);
	}

	public String getStringRepresentation() {
		String res = "(";

		for (int i = 0; i < generalAttributes.getSize(); i++) {
			res += "(";
			for (int j = 0; j < generalAttributes.getSize(); j++) {

				res += getBoardValue(j, i)
						+ ((j < generalAttributes.getSize()-1) ? "," : "");

			}
			res += ")" + ((i < generalAttributes.getSize()-1) ? "," : "");
		}

		res += ")";

		return res;
	}
	
	/**
	 * Erzeugt eine exakte Kopie dieses Bitboards als neue Instanz und gibt
	 * diese zurück.
	 * 
	 * @return Neue Instanz als Kopie dieses Boards.
	 */
	public Bitboard cloneBitboard() {

		Bitboard clone = new Bitboard(this.generalAttributes);

		long[][] res = new long[generalAttributes.getSize()][generalAttributes
				.getSize()];

		for (int i = 0; i < generalAttributes.getSize(); i++) {
			for (int j = 0; j < generalAttributes.getSize(); j++) {
				res[i][j] = this.bitboard[i][j];
			}
		}

		clone.setBitboard(res);
		clone.setZobristHash(this.hash);
		clone.setTransposition(transposition);

		return clone;
	}

	/**
	 * Erstellt keine neuen Instanzen, sondern überschreibt in der übergebenen
	 * Instanz einfach die Werte.
	 * 
	 * @param board
	 * @return
	 */
	public Bitboard softCloneBitboard(Bitboard board) {

		for (int i = 0; i < generalAttributes.getSize(); i++) {
			for (int j = 0; j < generalAttributes.getSize(); j++) {
				board.bitboard[i][j] = this.bitboard[i][j];
			}
		}

		board.setZobristHash(this.hash);
		board.setTransposition(transposition);

		return board;
	}

	public long[][] getBitboard() {
		return this.bitboard;
	}

	public void setBitboard(long[][] board) {
		this.bitboard = board;
	}

	public void setZobristHash(long hash) {
		this.hash = hash;
	}

	public long getZobristHash() {
		return this.hash;
	}

	public void setTransposition(Transposition transposition) {
		this.transposition = transposition;
	}

	/****************************************************************/
	/********************* _____MOVE______ **************************/
	/****************************************************************/

	private void shiftLine(int dir, int row, int start) {

		if (dir == Constants.Direction.LEFT) {

			/* Shift left! */
			for (int i = start; i < generalAttributes.getSize() - 1; i++) {

				if (generalAttributes.getUseMemory()) {

					/* Updaten des Zobrist-Hashes!!!! */
					/*
					 * Entfernen der Zahl an der zu 'i'-Position und setzen der
					 * neuen Zahl von 'i+1'
					 */
					// synchronized (transposition.getClass())
					{
						transposition.setTypeAt(this, getBoardValue(i, row), i,
								row);
						transposition.setTypeAt(this,
								getBoardValue(i + 1, row), i, row);
					}

				}

				this.bitboard[i][row] = this.bitboard[i + 1][row];
			}

			if (generalAttributes.getUseMemory()) {

				/* Updaten des Zobrist-Hashes!!!! */
				/*
				 * Entfernen der Zahl an der zu '3'-Position und setzen der
				 * neuen Zahl '0'
				 */
				// synchronized (transposition.getClass())
				{
					transposition
							.setTypeAt(
									this,
									getBoardValue(
											generalAttributes.getSize() - 1,
											row),
									generalAttributes.getSize() - 1, row);
					transposition.setTypeAt(this, 0,
							generalAttributes.getSize() - 1, row);
				}

			}
			this.bitboard[generalAttributes.getSize() - 1][row] = 0;

		} else {

			/* Shift right! */
			for (int i = start; i > 0; i--) {
				if (generalAttributes.getUseMemory()) {

					/* Updaten des Zobrist-Hashes!!!! */
					/*
					 * Entfernen der Zahl an der zu 'i'-Position und setzen der
					 * neuen Zahl von 'i+1'
					 */
					// synchronized (transposition.getClass())
					{
						transposition.setTypeAt(this, getBoardValue(i, row), i,
								row);
						transposition.setTypeAt(this,
								getBoardValue(i - 1, row), i, row);
					}

				}

				this.bitboard[i][row] = this.bitboard[i - 1][row];
			}
			if (generalAttributes.getUseMemory()) {

				/* Updaten des Zobrist-Hashes!!!! */
				/*
				 * Entfernen der Zahl an der zu '3'-Position und setzen der
				 * neuen Zahl '0'
				 */
				// synchronized (transposition.getClass())
				{
					transposition
							.setTypeAt(this, getBoardValue(0, row), 0, row);
					transposition.setTypeAt(this, 0, 0, row);
				}

			}
			this.bitboard[0][row] = 0;
		}

	}

	private void shiftFile(int dir, int col, int start) {

		if (dir == Constants.Direction.DOWN) {

			/* Shift down! */
			for (int i = start; i < generalAttributes.getSize() - 1; i++) {
				if (generalAttributes.getUseMemory()) {

					/* Updaten des Zobrist-Hashes!!!! */
					/*
					 * Entfernen der Zahl an der zu 'i'-Position und setzen der
					 * neuen Zahl von 'i+1'
					 */
					// synchronized (transposition.getClass())
					{
						transposition.setTypeAt(this, getBoardValue(col, i),
								col, i);
						transposition.setTypeAt(this,
								getBoardValue(col, i + 1), col, i);
					}

				}
				this.bitboard[col][i] = this.bitboard[col][i + 1];
			}
			if (generalAttributes.getUseMemory()) {

				/* Updaten des Zobrist-Hashes!!!! */
				/*
				 * Entfernen der Zahl an der zu 'i'-Position und setzen der
				 * neuen Zahl von 'i+1'
				 */
				// synchronized (transposition.getClass())
				{
					transposition
							.setTypeAt(
									this,
									getBoardValue(col,
											generalAttributes.getSize() - 1),
									col, generalAttributes.getSize() - 1);
					transposition.setTypeAt(this, 0, col,
							generalAttributes.getSize() - 1);
				}

			}
			this.bitboard[col][generalAttributes.getSize() - 1] = 0;

		} else {

			/* Shift up! */
			for (int i = start; i > 0; i--) {
				if (generalAttributes.getUseMemory()) {

					/* Updaten des Zobrist-Hashes!!!! */
					/*
					 * Entfernen der Zahl an der zu 'i'-Position und setzen der
					 * neuen Zahl von 'i+1'
					 */
					// synchronized (transposition.getClass())
					{
						// transposition.setTypeAt(this, getBoardValue(col, i),
						// col, i);
						transposition.setTypeAt(this,
								getBoardValue(col, i - 1), col, i);
					}

				}
				this.bitboard[col][i] = this.bitboard[col][i - 1];
			}
			if (generalAttributes.getUseMemory()) {

				/* Updaten des Zobrist-Hashes!!!! */
				/*
				 * Entfernen der Zahl an der zu 'i'-Position und setzen der
				 * neuen Zahl von 'i+1'
				 */
				// synchronized (transposition.getClass())
				{
					transposition
							.setTypeAt(this, getBoardValue(col, 0), col, 0);
					transposition.setTypeAt(this, 0, col, 0);
				}

			}
			this.bitboard[col][0] = 0;
		}

	}

	/**
	 * Führt einen bestimmten MOve auf dem Feld tatsäch aus und verändert die
	 * Boardsituation.
	 * 
	 * @param move
	 *            der Move.
	 */
	public void applyMove(MoveNormal move) {

		switch (move.getDir()) {
		case Constants.Direction.LEFT:

			for (int row = 0; row < generalAttributes.getSize(); row++) {

				for (int col = 0; col < generalAttributes.getSize() - 1; col++) {
					int check = 0;
					/* Shift */
					while (isEmptyField(col, row)
							&& check <= generalAttributes.getSize() - 1) {
						shiftLine(Constants.Direction.LEFT, row, col);
						check++;
					}
				}

			}

			for (int row = 0; row < generalAttributes.getSize(); row++) {

				for (int col = 0; col < generalAttributes.getSize() - 1; col++) {
					int check = 0;
					/* Shift */
					while (isEmptyField(col, row)
							&& check <= generalAttributes.getSize() - 1) {
						shiftLine(Constants.Direction.LEFT, row, col);
						check++;
					}

					/* Merge */
					if (getBoardValue(col, row) == getBoardValue(col + 1, row)
							&& getBoardValue(col, row) != 0) {

						if (generalAttributes.getUseMemory()) {

							/* Updaten des Zobrist-Hashes!!!! */
							/*
							 * Entfernen der Zahl an der zu 'i'-Position und
							 * setzen der neuen Zahl von 'i+1'
							 */
							// synchronized (transposition.getClass())
							{
								transposition.setTypeAt(this,
										getBoardValue(col, row), col, row);
								transposition.setTypeAt(this,
										getBoardValue(col + 1, row) * 2, col,
										row);
								transposition.setTypeAt(this,
										getBoardValue(col + 1, row), col + 1,
										row);
								transposition.setTypeAt(this, 0, col + 1, row);
							}

						}
						this.bitboard[col][row] = 2 * this.bitboard[col][row];
						this.bitboard[col + 1][row] = 0;
						shiftLine(Constants.Direction.LEFT, row, col + 1);
					}

				}

			}

			break;
		case Constants.Direction.RIGHT:

			for (int row = 0; row < generalAttributes.getSize(); row++) {
				for (int col = generalAttributes.getSize() - 1; col > 0; col--) {
					int check = 0;
					/* Shift */
					while (isEmptyField(col, row)
							&& check <= generalAttributes.getSize() - 1) {
						shiftLine(Constants.Direction.RIGHT, row, col);
						check++;
					}
				}
			}

			for (int row = 0; row < generalAttributes.getSize(); row++) {

				for (int col = generalAttributes.getSize() - 1; col > 0; col--) {
					int check = 0;

					/* Shift */
					while (isEmptyField(col, row)
							&& check <= generalAttributes.getSize() - 1) {
						shiftLine(Constants.Direction.RIGHT, row, col);
						check++;
					}

					/* Merge */
					if (getBoardValue(col, row) == getBoardValue(col - 1, row)
							&& getBoardValue(col, row) != 0) {
						if (generalAttributes.getUseMemory()) {

							/* Updaten des Zobrist-Hashes!!!! */
							/*
							 * Entfernen der Zahl an der zu 'i'-Position und
							 * setzen der neuen Zahl von 'i+1'
							 */
							// synchronized (transposition.getClass())
							{
								transposition.setTypeAt(this,
										getBoardValue(col, row), col, row);
								transposition.setTypeAt(this,
										getBoardValue(col - 1, row) * 2, col,
										row);
								transposition.setTypeAt(this,
										getBoardValue(col - 1, row), col - 1,
										row);
								transposition.setTypeAt(this, 0, col - 1, row);
							}

						}
						this.bitboard[col][row] = 2 * this.bitboard[col][row];
						this.bitboard[col - 1][row] = 0;
						shiftLine(Constants.Direction.RIGHT, row, col - 1);
					}

				}

			}

			break;
		case Constants.Direction.UP:

			for (int col = 0; col < generalAttributes.getSize(); col++) {

				for (int row = generalAttributes.getSize() - 1; row > 0; row--) {
					int check = 0;
					/* Shift */
					while (isEmptyField(col, row)
							&& check <= generalAttributes.getSize() - 1) {
						shiftFile(Constants.Direction.UP, col, row);
						check++;
					}
				}

			}

			for (int col = 0; col < generalAttributes.getSize(); col++) {

				for (int row = generalAttributes.getSize() - 1; row > 0; row--) {
					int check = 0;
					/* Shift */
					while (isEmptyField(col, row)
							&& check <= generalAttributes.getSize() - 1) {
						shiftFile(Constants.Direction.UP, col, row);
						check++;
					}

					/* Merge */
					if (getBoardValue(col, row) == getBoardValue(col, row - 1)
							&& getBoardValue(col, row) != 0) {
						if (generalAttributes.getUseMemory()) {

							/* Updaten des Zobrist-Hashes!!!! */
							/*
							 * Entfernen der Zahl an der zu 'i'-Position und
							 * setzen der neuen Zahl von 'i+1'
							 */
							// synchronized (transposition.getClass())
							{
								transposition.setTypeAt(this,
										getBoardValue(col, row), col, row);
								transposition.setTypeAt(this,
										getBoardValue(col, row - 1) * 2, col,
										row);
								transposition.setTypeAt(this,
										getBoardValue(col, row - 1), col,
										row - 1);
								transposition.setTypeAt(this, 0, col, row - 1);
							}

						}
						this.bitboard[col][row] *= 2;
						this.bitboard[col][row - 1] = 0;
						shiftFile(Constants.Direction.UP, col, row - 1);
					}

				}

			}

			break;
		case Constants.Direction.DOWN:

			for (int col = 0; col < generalAttributes.getSize(); col++) {

				for (int row = 0; row < generalAttributes.getSize() - 1; row++) {
					int check = 0;
					/* Shift */
					while (isEmptyField(col, row)
							&& check <= generalAttributes.getSize() - 1) {
						shiftFile(Constants.Direction.DOWN, col, row);
						check++;
					}
				}

			}

			for (int col = 0; col < generalAttributes.getSize(); col++) {

				for (int row = 0; row < generalAttributes.getSize() - 1; row++) {
					int check = 0;
					/* Shift */
					while (isEmptyField(col, row)
							&& check <= generalAttributes.getSize() - 1) {
						shiftFile(Constants.Direction.DOWN, col, row);
						check++;
					}

					/* Merge */
					if (getBoardValue(col, row) == getBoardValue(col, row + 1)
							&& getBoardValue(col, row) != 0) {
						if (generalAttributes.getUseMemory()) {

							/* Updaten des Zobrist-Hashes!!!! */
							/*
							 * Entfernen der Zahl an der zu 'i'-Position und
							 * setzen der neuen Zahl von 'i+1'
							 */
							// synchronized (transposition.getClass())
							{
								transposition.setTypeAt(this,
										getBoardValue(col, row), col, row);
								transposition.setTypeAt(this,
										getBoardValue(col, row + 1) * 2, col,
										row);
								transposition.setTypeAt(this,
										getBoardValue(col, row + 1), col,
										row + 1);
								transposition.setTypeAt(this, 0, col, row + 1);
							}

						}
						this.bitboard[col][row] = 2 * this.bitboard[col][row];
						this.bitboard[col][row + 1] = 0;
						shiftFile(Constants.Direction.DOWN, col, row + 1);
					}

				}

			}

			break;

		}

	}

	/**
	 * Führt einen bestimmten MOve auf dem Feld tatsäch aus und verändert die
	 * Boardsituation.
	 * 
	 * @param move
	 *            der Move.
	 */
	public void applyMove(MoveRandom move) {

		if (generalAttributes.getUseMemory()) {

			/* Updaten des Zobrist-Hashes!!!! */
			/*
			 * Entfernen der Zahl an der zu 'i'-Position und setzen der neuen
			 * Zahl von 'i+1'
			 */
			// synchronized (transposition.getClass())
			{
				transposition.setTypeAt(this, (int) move.getNumber(),
						move.getX(), move.getY());
			}

		}

		this.bitboard[move.getX()][move.getY()] = move.getNumber();

	}

	/**
	 * Wendet einen konkreten Move auf ein Board an!
	 * 
	 * @param move
	 *            Der anzuwendene Move
	 */
	public void applyMove(Move move) {

		/*
		 * Unter Umständen ist der Special Move nicht einer, sondern gleich
		 * mehrere.
		 */
		switch (move.getMoveType()) {
		case Constants.MoveType.NORMAL:

			applyMove((MoveNormal) move);

			break;
		case Constants.MoveType.RANDOM:

			applyMove((MoveRandom) move);

			break;
		}

	}

	/**
	 * Erzeugt einen String mit der grafischen Darstellung des Spielfeldes. Alle
	 * Figuren beider Parteien werden abgebildet.
	 */
	public String toString() {
		String output = "";

		for (int y = generalAttributes.getSize() - 1; y >= 0; y--) {
			for (int x = 0; x < generalAttributes.getSize(); x++) {

				output = output + this.bitboard[x][y] + "\t";

			}
			output += "\n";
		}

		return output;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(bitboard);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Bitboard other = (Bitboard) obj;
		if (!Arrays.deepEquals(bitboard, other.bitboard))
			return false;

		if (other.hashCode() == this.hashCode()) {
			return true;
		} else {
			return false;
		}

	}

	public int getBoardValue(int x, int y) {

		return (int) this.bitboard[x][y];
	}

	public void setBoardValue(int x, int y, int value) {
		this.bitboard[x][y] = value;
	}

}
