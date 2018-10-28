package jsouplesse.util;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.jsoup.nodes.Node;

/**
 * First try at a logger class, which can write log messages to either
 * the command line or a log file, depending on the implementation.
 */
public class CrappyLogger {

	private boolean isCommandLineFine;
	
	private BufferedWriter writer;
	
	private boolean isLogFileWritable;
	
	private static Path LOG_FILE;
	
	public final static String PATH_TO_LOG_DIRECTORY = "C:/jsouplesse/log";
	
	public CrappyLogger(boolean isCommandLineFine) {
		
		this.isCommandLineFine = isCommandLineFine;
		
		if (!isCommandLineFine) {
			try {
				initialize();
			} catch (IOException ioex) {
				ioex.printStackTrace();
				// Too bad...no logs for this user.
			}
		}
	}
	
	public void initialize() throws IOException {
		// Set the path to the log directory.
		Path logDirectory = Paths.get(PATH_TO_LOG_DIRECTORY);
		
		if (IOUtils.fileNotExistsAndIsWritable(logDirectory)) {
			Files.createDirectories(logDirectory);
		}
		
		if (!Files.exists(logDirectory))
			// Failed to create the directory.
			return;
		
		// Determine the current date.
		LocalDate localDate = LocalDate.now();
		
		// Use a formatter (of the dtf persuasion gehehe) to format the date.
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("ddMMyyy");
		String today = dtf.format(localDate);
		// Now set the file path.
		LOG_FILE = logDirectory.resolve(today + "log.txt");
		
		writer = Files.newBufferedWriter(LOG_FILE, StandardOpenOption.APPEND, 
				StandardOpenOption.CREATE, StandardOpenOption.WRITE);
		
		isLogFileWritable = true;
		
		writer.close();
	}
	
	public void deInitialize() {
		// Check to make sure the reader isn't null.
		if (writer != null) {
			try {
				writer.close();
			} catch (IOException ioex) {
				// Too bad...
				ioex.printStackTrace();
			}
		}
	}
	
	public void log(String message) {
		
		if (isCommandLineFine)
			// Write the message to the command line.
			System.out.println(message);
		
		else if (isLogFileWritable) {
		
			try {
				
				writer = Files.newBufferedWriter(LOG_FILE, StandardOpenOption.APPEND, 
						StandardOpenOption.CREATE, StandardOpenOption.WRITE);
				
				// Try to write the message to the log file.
				writer.write(message);
				writer.newLine();
				
				writer.close();
				
			} catch (IOException ioex) {
				// Too bad...no logs for this user.
				ioex.printStackTrace();
			}
		}
	}
	
	// TODO: this method might be useless (i.e. calling node.toString() yields same result)
	public void logHtml(Node node) {
		if (isCommandLineFine)
			// Write the message to the command line.
			System.out.println(node.toString());
		
		else if (isLogFileWritable) {
		
			try {
				writer = Files.newBufferedWriter(LOG_FILE, StandardOpenOption.APPEND, 
						StandardOpenOption.CREATE, StandardOpenOption.WRITE);
				// Append the HTML of the node and its children to the writer.
				node.html(writer);				
				writer.newLine();
				
				writer.close();
				
			} catch (IOException ioex) {
				// Too bad...no logs for this user.
				ioex.printStackTrace();
			}
		}
	}
}
