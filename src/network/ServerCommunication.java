package network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import board.Constants;

import engine.EngineController;
import engine.EngineGeneralAttributes;

public class ServerCommunication {

	/* Instanz des Engine-Controllers (Thread-Control) */
	private EngineController engineController;
	/* Instanz der Klasse mit für die Engine globalen Attributen (vom Server) */
	private EngineGeneralAttributes engineGeneralAttributes;
	/* Instanz des Message-Writers */
	private MessageOutputWriter messageWriter;

	// #################################################################################################################

	public ServerCommunication(EngineGeneralAttributes engineGeneralAttributes,
			MessageOutputWriter messageWriter) {

		this.messageWriter = messageWriter;
		this.engineGeneralAttributes = engineGeneralAttributes;

		this.engineController = new EngineController(engineGeneralAttributes,
				messageWriter);
	}

	// #################################################################################################################

	/**
	 * Regelt die komplette Kommunikation über die vorgeschriebenen Protokolle
	 * mit der AEI.
	 */
	public void inputControl() {

		BufferedReader reader = new BufferedReader(new InputStreamReader(
				System.in));
		boolean isFirstMove = true;

		/*
		 * Schleife, die ununterbrochen versucht Befehle des Arimaa-Servers
		 * einzulesen und zu verarbeiten.
		 */
		while (true) {
			try {
				String[] messageArray;
				String initialMessage = reader.readLine();
				String firstMessage = "";
				String secondMessage = "";

				if (initialMessage == null) {
					messageWriter
							.sendMessage("log ERROR: Input Stream broken. Exit engine.");
					return;
				}

				/*
				 * Message kann in eine Sub-Message unterteilt sein im Falle
				 * 'go' 'ponder'
				 */
				messageArray = initialMessage.split(" ");

				// Log.
				messageWriter.logIncomingMessage(initialMessage);

				if (messageArray.length > 1) {
					firstMessage = messageArray[0];
					secondMessage = messageArray[1];
				}

				switch (messageArray.length) {
				case 0:
					messageWriter
							.sendMessage("log Message mit 0 Zeichen erhalten.");
					return;
				default:
					secondMessage = messageArray[1];
				case 1:
					firstMessage = messageArray[0];
				}

				/* ========= Einlesen von Befehlen ========= */

				if (firstMessage.equals("aei")) {

					messageWriter.sendMessage("protocol-version 1");
					messageWriter.sendMessage("id name BotYeti");
					messageWriter.sendMessage("id author Maurice Tollmien");
					messageWriter.sendMessage("id version 1.0");
					messageWriter.sendMessage("aeiok");

				} else if (firstMessage.equals("isready")) {

					messageWriter.sendMessage("readyok");

				} else if (firstMessage.equals("newgame")) {

					isFirstMove = true;
					engineController.newGame();

				} else if (firstMessage.equals("apply")) {

					engineController.applyMove(messageArray[1]);

					engineController.printBoard();

				} else if (firstMessage.equals("play")) {

					engineController.adoptBoard(messageArray[1]);

					// engineController.printBoard();

					// messageWriter.sendMessage("log started engine");
					engineController.go();

				} else if (firstMessage.equals("add")) {

					engineController.addNumberAt(messageArray[1],
							messageArray[2]);
					engineController.printBoard();

				} else if (firstMessage.equals("print")) {

					engineController.printBoard();

				} else if (firstMessage.equals("setoption")) {

					String name = messageArray[2];
					String value = (messageArray.length < 5) ? ""
							: messageArray[4];
					engineGeneralAttributes.setOption(name, value);

				} else if (firstMessage.equals("makemove")) {

					/* Alle Moves durchgehen und setzen. */
					for (int i = 1; i < messageArray.length; i++) {
						engineController.makeMove(messageArray[i]);
					}

					engineController.logBitboard();

				} else if (firstMessage.equals("go")) {

					if (secondMessage.equals("ponder")) {
						messageWriter
								.sendMessage("log pondering... ... ... naah, joking. Just chillin'");
					} else {

						messageWriter.sendMessage("log started engine");
						engineController.go();

					}

				} else if (firstMessage.equals("stop")) {

					// Stoppt die Engine und printet den BestMove direkt aus.
					engineController.stop();

				} else if (firstMessage.equals("quit")) {

					// Stoppt die Engine und printet keinen BestMove aus.
					// engineController.kill();
					engineController.interrupt();
					return;

				} else {

					// messageWriter.sendMessage("log Message not recognized.");
					// return;

					if (initialMessage.matches("\\(\\[.*\\]\\)")) {
						String fieldSize;
						int depth = 4;

						/* Dynamisch die Feldgröße festlegen */
						fieldSize = initialMessage.replaceAll("[^\\[]", "");
						engineGeneralAttributes.setSize(fieldSize.length());
						engineController.newGame();

						/* Dynamisch die Rekursionstiefe festlegen */
						switch (fieldSize.length()) {
						case 2:
							depth = 59;
							break;
						case 3:
							depth = 9;
							break;
						case 4:
							depth = 7;
							break;
						case 5:
							depth = 5;
							break;
						case 6:
							depth = 5;
							break;
						case 7:
							depth = 4;
							break;
						case 8:
							depth = 4;
							break;
						default:
							depth = 3;
						}
						engineGeneralAttributes.setDepth(depth);
						
						engineController.adoptBoard(initialMessage.replaceAll(
								" ", ""));

						// engineController.printBoard();

						// messageWriter.sendMessage("log started engine");
						engineController.go();
					} else {
						messageWriter
								.sendMessage("log Message not recognized.");
					}

				}

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			}

		}

	}

}
