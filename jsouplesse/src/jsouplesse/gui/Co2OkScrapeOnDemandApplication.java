package jsouplesse.gui;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import jsouplesse.dataaccess.Connector;
import jsouplesseutil.CrappyLogger;
import jsouplesseutil.IOUtils;

public class Co2OkScrapeOnDemandApplication extends Application {

	private Connector connector;
	
	private CrappyLogger logger;
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		
		// Since FailedScan is not yet used, do not initialize the Connector.
		connector = new Connector();
		
		// Instantiate a logger that writes to a log file.
		logger = new CrappyLogger(false);
		
		MainScreenBuilder builder = new MainScreenBuilder(connector, logger);
		GridPane mainScreen = builder.buildScreen();
		
		Scene main = new Scene(mainScreen, 800, 550);
		
		primaryStage.setScene(main);
		primaryStage.setTitle("Co2ok scrape on demand");
		addOnCloseFunction(primaryStage);
		primaryStage.show();
	}

	/**
	 * Sets the logic that should be executed when the user exits the application
	 * on the primary stage.
	 */
	private void addOnCloseFunction(Stage primaryStage) {
		
		primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent event) {
				// Close the connection to the database.
				connector.close();
				logger.log("Connection to the database closed.");
				logger.deInitialize();
			}
		});
	}
	
	public static void main(String[] args) {
		launch();
	}
}
