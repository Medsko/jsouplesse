package jsouplesse.gui;

import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;

public class InputSection {
	
	private Label labelTag;
	
	private TextField inputTag;
	
	private Label labelAttribute;
	
	private TextField inputAttribute;
	
	private CheckBox fetchPageCheckBox;

	public Label getLabelTagName() {
		return labelTag;
	}

	public void setLabelTag(Label labelTag) {
		this.labelTag = labelTag;
	}

	public TextField getInputTag() {
		return inputTag;
	}

	public void setInputTag(TextField inputTag) {
		this.inputTag = inputTag;
	}

	public Label getLabelAttribute() {
		return labelAttribute;
	}

	public void setLabelAttribute(Label labelAttribute) {
		this.labelAttribute = labelAttribute;
	}

	public TextField getInputAttribute() {
		return inputAttribute;
	}

	public void setInputAttribute(TextField inputAttribute) {
		this.inputAttribute = inputAttribute;
	}

	public CheckBox getFetchPageCheckBox() {
		return fetchPageCheckBox;
	}

	public void setFetchPageCheckBox(CheckBox fetchPageCheckBox) {
		this.fetchPageCheckBox = fetchPageCheckBox;
	}
	
	public boolean isValid() {
		return !inputTag.getText().isEmpty() || !inputAttribute.getText().isEmpty();
	}
	
	public void removeInputSectionFromScreen(Pane screen) {
		screen.getChildren().remove(fetchPageCheckBox);
		screen.getChildren().remove(inputAttribute);
		screen.getChildren().remove(labelAttribute);
		screen.getChildren().remove(inputTag);
		screen.getChildren().remove(labelTag);
	}
	
	public ElementEvaluatorInput getElementEvaluatorInput() {
		ElementEvaluatorInput elementEvaluatorInput = new ElementEvaluatorInput();
		elementEvaluatorInput.setTag(inputTag.getText());
		elementEvaluatorInput.setAttribute(inputAttribute.getText());
		elementEvaluatorInput.setShouldFetchWebPage(fetchPageCheckBox.isSelected());
		return elementEvaluatorInput;
	}
}
