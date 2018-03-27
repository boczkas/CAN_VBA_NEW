import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.image.WritableImage;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.net.MalformedURLException;

public class Main extends Application{

    public static void main(String[] args) {
       launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        File[] files = new File("C:\\Java\\CAN_VBA_NEW_fast\\integration\\NewFiles").listFiles();

        int i = 0;

        for(File file : files){


            long startTime = System.nanoTime();
            OsciloscopeData osciloscopeData = new OsciloscopeData(file.getPath());

            primaryStage.setTitle("VBA");

            final NumberAxis xAxis = new NumberAxis();
            final NumberAxis yAxis = new NumberAxis();

            setupChartAxis(osciloscopeData, xAxis, yAxis);

            final LineChart lineChart = new LineChart<>(xAxis, yAxis);

            lineChart.setTitle(osciloscopeData.getMessageStringID());
            lineChart.setCreateSymbols(false);

            XYChart.Series samples = new XYChart.Series();
            samples.setName("samples");

            XYChart.Series averageMaxValueSeries = new XYChart.Series();
            XYChart.Series averageMinValueSeries = new XYChart.Series();
            XYChart.Series peakMaxValueSeries = new XYChart.Series();
            XYChart.Series peakMinValueSeries = new XYChart.Series();
            XYChart.Series idRectangle = new XYChart.Series();

            setupMarkersSeriesNames(osciloscopeData, averageMaxValueSeries, averageMinValueSeries, peakMaxValueSeries, peakMinValueSeries, idRectangle);
            setupMarkersSeriesValues(osciloscopeData, averageMaxValueSeries, averageMinValueSeries, peakMaxValueSeries, peakMinValueSeries, idRectangle);

            setupSamplesSerie(osciloscopeData, samples);

            lineChart.getData().addAll(samples, averageMaxValueSeries, averageMinValueSeries, peakMaxValueSeries, peakMinValueSeries, idRectangle);
            lineChart.setLegendSide(Side.RIGHT);
            lineChart.setAnimated(false);

            Scene scene = new Scene(lineChart, 800, 600);

            setSceneStyle(scene);

//            primaryStage.setScene(scene);
//            primaryStage.show();

            WritableImage snapShot = scene.snapshot(null);
            ImageIO.write(SwingFXUtils.fromFXImage(snapShot, null), "png", new File("pictures/" + osciloscopeData.getMessageStringID() + "_" + i++ + ".png"));
            long endTime = System.nanoTime();

            System.out.println("Execution time: " + (endTime - startTime) / 1000000 + "ms");
        }
    }

    private void setSceneStyle(Scene scene) throws MalformedURLException {
        scene.getStylesheets().add((new File("styles.css").toURI().toURL()).toExternalForm());
    }

    private void setupSamplesSerie(OsciloscopeData osciloscopeData, XYChart.Series samples) {
            samples.getData().addAll(osciloscopeData.getChartData());
    }

    private void setupMarkersSeriesValues(OsciloscopeData osciloscopeData, XYChart.Series averageMaxValueSeries, XYChart.Series averageMinValueSeries,
                                          XYChart.Series peakMaxValueSeries, XYChart.Series peakMinValueSeries, XYChart.Series idRectangleSeries) {
        averageMaxValueSeries.getData().add(new XYChart.Data(osciloscopeData.getMinX(), osciloscopeData.getAverageMaxValue()));
        averageMaxValueSeries.getData().add(new XYChart.Data(osciloscopeData.getMaxX(), osciloscopeData.getAverageMaxValue()));

        averageMinValueSeries.getData().add(new XYChart.Data(osciloscopeData.getMinX(), osciloscopeData.getAverageMinValue()));
        averageMinValueSeries.getData().add(new XYChart.Data(osciloscopeData.getMaxX(), osciloscopeData.getAverageMinValue()));

        peakMaxValueSeries.getData().add(new XYChart.Data(osciloscopeData.getMinX(), osciloscopeData.getSampleMaxValue()));
        peakMaxValueSeries.getData().add(new XYChart.Data(osciloscopeData.getMaxX(), osciloscopeData.getSampleMaxValue()));

        peakMinValueSeries.getData().add(new XYChart.Data(osciloscopeData.getMinX(), osciloscopeData.getSampleMinValue()));
        peakMinValueSeries.getData().add(new XYChart.Data(osciloscopeData.getMaxX(), osciloscopeData.getSampleMinValue()));

        double MARGIN = 0.1;
        Rectangle2D.Double idRectangle = new Rectangle2D.Double(osciloscopeData.getFirstXValueIDMessage(), osciloscopeData.getSampleMinValue() - MARGIN,
                osciloscopeData.getLastXValueIDMessage() - osciloscopeData.getFirstXValueIDMessage(),
                osciloscopeData.getSampleMaxValue() - osciloscopeData.getSampleMinValue() + MARGIN);


        idRectangleSeries.getData().add(new XYChart.Data(idRectangle.getMinX(), idRectangle.getMaxY()));
        idRectangleSeries.getData().add(new XYChart.Data(idRectangle.getMaxX(), idRectangle.getMaxY()));
        idRectangleSeries.getData().add(new XYChart.Data(idRectangle.getMaxX(), idRectangle.getMinY()));
        idRectangleSeries.getData().add(new XYChart.Data(idRectangle.getMinX(), idRectangle.getMinY()));
        idRectangleSeries.getData().add(new XYChart.Data(idRectangle.getMinX(), idRectangle.getMinY()));
        idRectangleSeries.getData().add(new XYChart.Data(idRectangle.getMinX(), idRectangle.getMaxY()));
        idRectangleSeries.getData().add(new XYChart.Data(idRectangle.getMaxX(), idRectangle.getMaxY()));
        idRectangleSeries.getData().add(new XYChart.Data(idRectangle.getMaxX(), idRectangle.getMinY()));
    }

    private void setupMarkersSeriesNames(OsciloscopeData osciloscopeData, XYChart.Series averageMaxValueSeries, XYChart.Series averageMinValueSeries,
                                         XYChart.Series peakMaxValueSeries, XYChart.Series peakMinValueSeries, XYChart.Series idRectangleSeries) {
        averageMaxValueSeries.setName("average max: " + String.format("%.3f", osciloscopeData.getAverageMaxValue()));
        averageMinValueSeries.setName("average min: " + String.format("%.3f",osciloscopeData.getAverageMinValue()));
        peakMaxValueSeries.setName("peak max: " + String.format("%.3f",osciloscopeData.getSampleMaxValue()));
        peakMinValueSeries.setName("peak min: " + String.format("%.3f",osciloscopeData.getSampleMinValue()));
        idRectangleSeries.setName("ID");
    }

    private void setupChartAxis(OsciloscopeData osciloscopeData, NumberAxis xAxis, NumberAxis yAxis) {
        xAxis.setLabel("Time");
        xAxis.setAutoRanging(false);
        xAxis.setLowerBound(osciloscopeData.getMinX());
        xAxis.setUpperBound(osciloscopeData.getMaxX());
        xAxis.setTickLabelFormatter(new NumberAxis.DefaultFormatter(xAxis){
            @Override
            public String toString(Number object) {
                return String.format("%1.5f", object);
            }
        });
        xAxis.setTickUnit(0.00001);
        yAxis.setLabel("Voltage");
    }
}
