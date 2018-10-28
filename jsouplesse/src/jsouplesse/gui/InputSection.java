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
	
	private CheckBox fetchTextInLinkCheckBox;

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
	
	public CheckBox getFetchTextInLink() {
		return fetchTextInLinkCheckBox;
	}

	public void setFetchTextInLinkCheckBox(CheckBox fetchTextInLink) {
		this.fetchTextInLinkCheckBox = fetchTextInLink;
	}

	public boolean isValid() {
		return !inputTag.getText().isEmpty() || !inputAttribute.getText().isEmpty();
	}
	
	public void removeInputSectionFromScreen(Pane screen) {
		screen.getChildren().remove(fetchPageCheckBox);
		screen.getChildren().remove(fetchTextInLinkCheckBox);
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
		elementEvaluatorInput.setShouldFetchTextInLink(fetchTextInLinkCheckBox.isSelected());
		return elementEvaluatorInput;
	}
}
