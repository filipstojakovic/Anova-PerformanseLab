package analysis;

import net.sourceforge.jdistlib.F;
import net.sourceforge.jdistlib.T;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AnalysisModel
{
    private int numOfMeasurements;
    private int numOFAlternatives;
    private Map<Integer, List<Double>> columnHashMap;
    double probability;

    //calculated values
    double overAllMean;         //ukupna prosjecna vrijednost
    List<Double> alterMeans;    //prosjecna vrijednost po kolonama
    double SSA = 0.0;           // varijacija usljed efekta alternativa(SSA)
    double dfSSA;               //stepen slobode ssa
    double SSE = 0.0;           // varijacija usljed gresaka u mjerenju (SSE)
    double dfSSE;               // stepen slobode sse
    double SST = 0.0;           // SST = SSA + SSE
    double varianceA;           //varijansa alternativa
    double varianceE;           //varijansa gresaka
    double Fcal;                // Fizr
    double Ftab;                // F tabelarno
    double alpha;               // 1 - probability
    List<String> contrastResult;

    public AnalysisModel(int numOfMeasurements, int numOFAlternatives, double probability, Map<Integer, List<Double>> columnHashMap)
    {
        this.numOfMeasurements = numOfMeasurements;
        this.numOFAlternatives = numOFAlternatives;
        this.columnHashMap = columnHashMap;

        this.probability = probability;
        alterMeans = new ArrayList<>(numOFAlternatives);
    }

    public String getAnovaResult()
    {
        return "Sistemi se " + (Fcal > Ftab ? "" : "ne ") + "razlikuju!";
    }

    public void startAnalysis()
    {
        calculateColumnsAndTotalMean();
        calculateSSA();
        calculateSSE();
        calculateSST();
        calculateVarianceA();
        calcualteVarianceE();
        calculateFcal();
        calculateFtab();
        calculateContrast();
    }

    private void calculateColumnsAndTotalMean()
    {
        double overAllSum = 0.0;
        for (List<Double> columnValue : columnHashMap.values())
        {
            double columnSum = columnValue.stream().reduce(0.0, Double::sum);
            overAllSum += columnSum;

            double columnMean = columnSum / numOfMeasurements;
            alterMeans.add(columnMean);
        }
        overAllMean = overAllSum / (numOfMeasurements * numOFAlternatives);
    }

    private void calculateSSE()
    {
        SSE = 0.0;
        for (int i = 0; i < numOFAlternatives; i++)
        {
            List<Double> columnValues = columnHashMap.get(i);
            double columnMean = alterMeans.get(i);
            SSE += columnValues.stream().mapToDouble(y_ij -> (y_ij - columnMean) * (y_ij - columnMean)).sum();
        }
        dfSSE = numOFAlternatives * (numOfMeasurements - 1);
    }

    private void calculateSSA()
    {
        SSA = alterMeans.stream().mapToDouble(yj -> (yj - overAllMean) * (yj - overAllMean)).sum();// * numOfMeasurements;
        SSA *= numOfMeasurements;

        dfSSA = numOFAlternatives - 1;
    }

    private void calculateSST()
    {
        SST = SSA + SSE;
    }

    private void calculateVarianceA()
    {
        varianceA = SSA / dfSSA;
    }

    private void calcualteVarianceE()
    {
        varianceE = SSE / dfSSE;
    }

    private void calculateFcal()
    {
        Fcal = varianceA / varianceE;
    }

    private void calculateFtab()
    {
        alpha = 1 - probability;
        Ftab = F.quantile(alpha, dfSSA, dfSSE, false, false);
    }

    // izracunaj interval povjerenja
    private void calculateContrast()
    {
        double Sc = Math.sqrt(varianceE * 2.0 / (numOfMeasurements * numOFAlternatives));
        double[] alphaEffects = alterMeans.stream().mapToDouble(alpha -> alpha - overAllMean).toArray();
        double Tdistribution = T.quantile(alpha / 2.0, dfSSE, false, false);
        //Tdist= 1.78228755564932 za 0.9, 5 mjerenja, 3 alternative

        contrastResult = new ArrayList<>();
        for (int i = 0; i < numOFAlternatives - 1; i++)
            for (int j = i + 1; j < numOFAlternatives; j++)
            {
                double c = alphaEffects[i] - alphaEffects[j];
                double c1 = c - Tdistribution * Sc;
                double c2 = c + Tdistribution * Sc;

                //ako obuhvata nulu => nema razlike
                boolean zeroIncluded = true;
                if ((c1 < 0.0 && c2 < 0.0) || (c1 > 0.0 && c2 > 0.0))
                    zeroIncluded = false;

                String text = "Alternative " + (i + 1) + " i " + (j + 1) + " se" + (zeroIncluded ? " ne" : "") + " razlikuju ";
                String interval = String.format("(%.3f, %.3f)", c1, c2);
                contrastResult.add(text + interval + "\n");
            }

    }

}
