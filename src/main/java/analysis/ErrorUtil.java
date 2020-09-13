package analysis;

import javafx.scene.control.Alert;
import javafx.scene.control.TextField;

public class ErrorUtil
{
    //show error message
    public static void show()
    {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Greška");
        alert.setHeaderText("Nisu dobro unešeni podaci.");
        alert.showAndWait();
    }

    //show error message
    public static void show(String errorMsg)
    {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Greška");
        alert.setHeaderText(errorMsg);
        alert.showAndWait();
    }

}
