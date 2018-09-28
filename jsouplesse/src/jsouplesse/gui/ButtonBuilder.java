package jsouplesse.gui;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;

/**
 * Builds standard format buttons. 
 */
public class ButtonBuilder {

	public Button build(String text, EventHandler<ActionEvent> handler) {
		Button button = new Button(text);
		button.setPrefHeight(50);
		button.setPrefWidth(175);
		button.setOnAction(handler);
		
		return button;
	}
	
}
