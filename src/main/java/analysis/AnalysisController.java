package analysis;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import main.Main;

import java.io.IOException;
import java.util.*;

public class AnalysisController
{
    public static final double TEXT_FIELD_WIDTH = 100.0;
    @FXML
    private TextField probabilityTF;

    @FXML
    private Label overallMeanLbl;

    @FXML
    private Label ssaLbl;

    @FXML
    private Label sseLbl;

    @FXML
    private Label sstLbl;

    @FXML
    private Label varianceALbl;

    @FXML
    private Label varianceELbl;

    @FXML
    private Label FcalLbl;

    @FXML
    private Label FtabLbl;

    @FXML
    private Label resultLbl;

    @FXML
    private TextArea contrastTF;

    @FXML
    private GridPane gridPane;

    private int numOFAlternatives;
    private int numOfMeasurements;
    private int numOfColumns;
    private int numOfRows;
    private Map<Integer, List<Double>> columnHashMap;   // <Column number, values from column>
    private double probability;

    //get inputs from input window
    public void setInputValues(int numOFAlternatives, int numOfMeasurements)
    {
        this.numOFAlternatives = numOFAlternatives;
        this.numOfColumns = numOFAlternatives + 1;
        this.numOfMeasurements = numOfMeasurements;
        this.numOfRows = numOfMeasurements + 2;
        createTable();
    }

    private void createTable()
    {
        gridPane.setAlignment(Pos.CENTER);

        for (int col = 0; col < numOfColumns; col++)
        {
            for (int row = 0; row < numOfRows; row++)
            {
                TextField textField = new TextField();
                textField.setPrefWidth(TEXT_FIELD_WIDTH);
                textField.setAlignment(Pos.CENTER);


                if (col == 0 || row == 0 || row == numOfRows - 1)
                {
                    String text = "";
                    if (row == 0 && col == 0)
                        text = "br. mjer/alt";
                    else if (col == 0 && row == numOfRows - 1)
                        text = "sr. vrijednost";
                    else if (col == 0)
                        text = row + ".";
                    else if (row == 0)
                        text = col + ". alternativa";

                    textField.setText(text);
                    textField.setEditable(false);
                    textField.setStyle("-fx-background-color: #2ecc71");

                }
                StackPane pane = new StackPane(textField);
                StackPane.setAlignment(textField, Pos.CENTER);
                gridPane.add(pane, col, row);
            }
        }
    }

    //get all values from
    public List<Double> getColumnValues(int col) throws NumberFormatException
    {
        List<Double> values = new ArrayList<>();
        for (int row = 1; row < numOfRows - 1; row++)
        {
            Double value = getValueFromGridPane(row, col);

            values.add(getValueFromGridPane(row, col));
        }
        return values;
    }

    //get value from position
    private Double getValueFromGridPane(int row, int col) throws NumberFormatException
    {

        for (Node node : gridPane.getChildren())
        {
            if (node instanceof StackPane && GridPane.getColumnIndex(node) == col && GridPane.getRowIndex(node) == row)
            {
                StackPane pane = (StackPane) node;
                TextField textField = (TextField) pane.getChildren().get(0);
                return Double.parseDouble(textField.getText());
            }
        }
        return null;
    }

    @FXML
    void goBack(ActionEvent event)
    {
        try
        {
            FXMLLoader inputFXML = new FXMLLoader(getClass().getResource("../inputview.fxml"));
            Parent inputViewParent = (Parent) inputFXML.load();

            Stage stage = Main.getStage();
            Scene scene = new Scene(inputViewParent);
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException ex)
        {
            ex.printStackTrace();
        }
    }

    /**
     * check if all fields are valid and saved them to hashmap
     *
     * @return true - all good, false - something is not good
     */
    public boolean checkColumnsAndSave()
    {

        columnHashMap = new HashMap<>();
        try
        {
            probability = Double.parseDouble(probabilityTF.getText());
            if (!(0.0 < probability || probability > 1.0))
                throw new NumberFormatException();

            for (int i = 1; i < numOfColumns; i++)
            {
                List<Double> columnValues = getColumnValues(i);
                columnHashMap.put(i - 1, columnValues);   // i-1 da krece od 0, bice lakse kasnije
            }
        } catch (NumberFormatException ex)
        {

            ErrorUtil.show();
            return false;
        }

        return true;
    }

    //Pritisnuto dugme Analiziraj (start analysis)
    @FXML
    void startAnalysis(ActionEvent event)
    {
        if (!checkColumnsAndSave())
            return;

        new Thread(() ->
        {
            AnalysisModel analysisModel = new AnalysisModel(numOfMeasurements, numOFAlternatives, probability, columnHashMap);
            analysisModel.startAnalysis();

            Platform.runLater(() ->
            {
                fillTableWithResults(analysisModel);
            });
        }).start();

    }

    // popuni view sa dobijenim rezultatima
    private void fillTableWithResults(AnalysisModel analysisModel)
    {
        List<Double> alterMeans = analysisModel.alterMeans;
        for (int i = 0; i < alterMeans.size(); i++)
            setValueToGridPane(numOfRows - 1, i + 1, alterMeans.get(i));

        overallMeanLbl.setText(String.format("%.3f", analysisModel.overAllMean));
        ssaLbl.setText(String.format("%.3f", analysisModel.SSA));
        sseLbl.setText(String.format("%.3f", analysisModel.SSE));
        sstLbl.setText(String.format("%.3f", analysisModel.SST));

        varianceALbl.setText(String.format("%.3f", analysisModel.varianceA));
        varianceELbl.setText(String.format("%.3f", analysisModel.varianceE));

        FcalLbl.setText(String.format("%.3f", analysisModel.Fcal));
        FtabLbl.setText(String.format("%.3f", analysisModel.Ftab));
        resultLbl.setText(analysisModel.getAnovaResult());

        List<String> contrastResult = analysisModel.contrastResult;
        contrastTF.clear();
        for (String text : contrastResult)
            contrastTF.appendText(text);
    }

    //sluzi za postavljanje srednjih vrijednosti
    private void setValueToGridPane(int row, int col, double value)
    {

        for (Node node : gridPane.getChildren())
        {
            if (node instanceof StackPane && GridPane.getColumnIndex(node) == col && GridPane.getRowIndex(node) == row)
            {
                StackPane pane = (StackPane) node;
                TextField textField = (TextField) pane.getChildren().get(0);
                textField.setText(String.format("%.3f", value));
            }
        }
    }

}
