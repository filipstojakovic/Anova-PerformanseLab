package main;

import analysis.AnalysisController;
import analysis.ErrorUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class InputViewController
{
    @FXML
    private TextField numOFAlternatives;

    @FXML
    private TextField numOfMeasurements;

    @FXML
    private Label errorMsg;

    /**
     * Check fields and start analysisview if all good
     */
    @FXML
    void submitClicked(ActionEvent event)
    {

        try
        {
            int alt = Integer.parseInt(numOFAlternatives.getText());
            int measur = Integer.parseInt(numOfMeasurements.getText());
            if(alt<1 || measur<1)
                throw new NumberFormatException();

            FXMLLoader analysisFXML = new FXMLLoader(getClass().getResource("../analysisview.fxml"));
            Parent analysisViewParent = (Parent) analysisFXML.load();

            //sending input values to the view
            AnalysisController analysisController = analysisFXML.getController();
            analysisController.setInputValues(alt, measur);

            Stage stage = Main.getStage();
            Scene scene = new Scene(analysisViewParent);
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.show();

        } catch (NumberFormatException ex)
        {
            ErrorUtil.show();
            errorMsg.setVisible(true);
        } catch (IOException ex)
        {
            ex.printStackTrace();
        }

    }

}
