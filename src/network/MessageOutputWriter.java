package network;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import engine.EngineGeneralAttributes;

import board.Constants;

public class MessageOutputWriter {

	private EngineGeneralAttributes generalAttributes;
	
	/**
	 * Schreibt eine Zeile in die Log-Datei.
	 */
	public void writeLog(String message) {

		if (generalAttributes.getLogging()) {
			try {
				PrintWriter out = new PrintWriter(new BufferedWriter(
						new FileWriter(Constants.Files.LOGFILE, true)));
				out.println(message);
				out.close();
			} catch (IOException e) {
			}
		}

	}
	
	/**
	 * Schreibt eine Zeile in die Log-Datei.
	 */
	public void writeGameWatchersLog(String message) {
		
			try {
				PrintWriter out = new PrintWriter(new BufferedWriter(
						new FileWriter(Constants.Files.WATCHERS_LOGFILE, true)));
				out.println(message);
				out.close();
			} catch (IOException e) {
			}
		

	}
	
	/**
	 * Loggen und Senden einer Nachricht von der Engine an den Controller.
	 */
	public synchronized void sendMessage(String message) {

		System.out.print(message);
		System.out.flush();

		writeLog("ENG: " + message);

	}

	/**
	 * Loggen von Nachrichten vom Controller an die Engine.
	 */
	public synchronized void logIncomingMessage(String message) {

		writeLog("CTL: " + message);

	}

	/**
	 * Schaffen einer Abgrenzung des neuen- vom alten Log ohne eine neue Datei
	 * anfangen zu müssen.
	 */
	public synchronized void startNewLog() {
		String message = "#############################################################################\n";
		
		message += "Neuer Log vom: "
				+ new SimpleDateFormat("dd.MM.yyyy HH:mm:ss")
		.format(new Date()) + "\n";
		
		message += "#############################################################################";
		
		writeLog(message);
		writeGameWatchersLog(message);
		
		

	}

	public void setGeneralAttributes(
			EngineGeneralAttributes engineGeneralAttributes) {
		this.generalAttributes = engineGeneralAttributes;
		
	}

}
