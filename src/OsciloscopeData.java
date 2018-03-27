import javafx.scene.chart.XYChart;

import java.awt.geom.Rectangle2D;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class OsciloscopeData{
    Integer CAN_SPEED = 500000;
    Integer bitInterval;

    private String filePath;
    private List<Sample> samples;
    private List<Sample> frameSamples;
    private List<Rectangle2D.Double> bitRectangles;
    private List<XYChart.Data> chartData;

    private List<Sample> bitSamples;
    String messageBitsString;
    private Double firstXValueIDMessage;
    private Double lastXValueIDMessage;
    private Double sampleMaxValue;
    private Double sampleMinValue;
    private Double averageMaxValue;
    private Double averageMinValue;

    private Double minX;
    private Double maxX;

// na pozniej
    private final Integer ID_LENGTH = 11;
    private final Integer ACK_POS_FROM_END = 12;

    boolean isCANMessageInData;

    public OsciloscopeData(String filePath) {
        this.filePath = filePath;
        this.isCANMessageInData = false;
        this.messageBitsString = "";
        samples = new ArrayList<>();
        frameSamples = new ArrayList<>();
        bitRectangles = new ArrayList<>();
        chartData = new ArrayList<>();
        readData();

        if(isCANMessageInData){
            countBitInterval();
            findCANMessage();
            findMessageBits();
            find_Y_MaxMinValues();
            find_Y_AverageMinMax();
        }

        find_X_MinMax();
    }

    private void countBitInterval() {
        Double samplingTime = Math.abs(samples.get(101).getTime() - samples.get(100).getTime());
        Double bitIntervalDouble = 1/samplingTime;
        bitInterval = bitIntervalDouble.intValue() / CAN_SPEED;
    }

    public Double getMinX() {
        return minX;
    }

    public Double getMaxX() {
        return maxX;
    }

    public List<Sample> getSamples() {
        return samples;
    }

    public List<Sample> getAllSamples() {
        return samples;
    }

    public List<Sample> getFrameSamples() {
        return frameSamples.subList(0, frameSamples.size());
    }

    public boolean isCANMessageInData() {
        return isCANMessageInData;
    }

    public Double getSampleMaxValue() {
        return sampleMaxValue;
    }

    public Double getSampleMinValue() {
        return sampleMinValue;
    }

    public Double getAverageMaxValue() {
        return averageMaxValue;
    }

    public Double getAverageMinValue() {
        return averageMinValue;
    }

    public Double getFirstXValueIDMessage() {
        return firstXValueIDMessage;
    }

    public Double getLastXValueIDMessage() {
        return lastXValueIDMessage;
    }

    public List<Rectangle2D.Double> getBitRectangles() {
        return bitRectangles;
    }

    private void readData() {
        try {
            BufferedReader in = new BufferedReader(new FileReader(filePath));
            String str;
            while ((str = in.readLine()) != null)
                processStringToSamples(str);
            in.close();
        }
        catch (IOException e) {
        }
    }

    private void processStringToSamples(String str) {
        String patternString = "(\\d+|\\-)\\.\\d+(e\\-\\d+)*,(\\-|\\d+)?\\d+(\\.\\d+)*";
        Pattern pattern = Pattern.compile(patternString);

        Matcher matcher = pattern.matcher(str);

        if(true == matcher.find()){
            String[] parts = str.split(",");

            if(Math.abs(Double.parseDouble(parts[1])) >= 1.5){
                isCANMessageInData = true;
            }
            Sample sample = new Sample(Double.parseDouble(parts[0]), Double.parseDouble(parts[1]));
            samples.add(sample);
            chartData.add(new XYChart.Data(sample.getTime(), sample.getValue()));
        }
        else{
            System.out.println(str);
            System.out.println("Nie");
        }
    }

    public void findCANMessage(){
        boolean beginningFound = false;
        boolean endFound = false;

        for(int i = 0; i < samples.size(); i++){
            if(beginningFound){
                frameSamples.add(samples.get(i));
            }
            else {
                if(Math.abs(samples.get(i + 2 * bitInterval).getValue()) >= 0.5){
                    beginningFound = true;
                }
            }
        }

        for(int i = samples.size(); i >= 0; i--){
            if(endFound){
                break;
            }
            else {
                frameSamples.remove(frameSamples.size() - 1);
                if(Math.abs(samples.get(i - ACK_POS_FROM_END * bitInterval).getValue()) >= 0.5){
                    endFound = true;
                }
            }
        }
    }

    private void find_Y_MaxMinValues(){
        Double min = Double.MAX_VALUE;
        Double max = Double.MIN_VALUE;

        for (Sample sample : frameSamples){
            if(sample.getValue() > max){
                max = sample.getValue();
            }
            if(sample.getValue() < min){
                min = sample.getValue();
            }
        }
        sampleMaxValue = max;
        sampleMinValue = min;
    }

    private void find_Y_AverageMinMax(){
        boolean beginningFound = false;
        Double maxSum = 0d;
        Double minSum = 0d;

        Integer maxSumItems = 0;
        Integer minSumItems = 0;

        for(int i = 0; i < frameSamples.size() - bitInterval * (ACK_POS_FROM_END + 2); i++) {
            Double sum = 0d;


            if(!beginningFound && Math.abs(frameSamples.get(i).getValue()) >= 1.5){
                beginningFound = true;
                i += bitInterval * 5;
            }

            if(beginningFound){
                double average = 0;
                if((i + bitInterval) < frameSamples.size()){
                    for(int j = i; j < i + bitInterval; j++){
                        sum += frameSamples.get(j).getValue();
                    }
                    average = sum/bitInterval;

                    if(Math.abs(average) > 1.5){
                        maxSum += sum;
                        maxSumItems++;
                    }
                    else{
                        minSum += sum;
                        minSumItems++;
                    }
                    i += bitInterval;
                }
                else{
                    break;
                }
            }

        }
        averageMaxValue = maxSum/(maxSumItems * bitInterval);
        averageMinValue = minSum/(minSumItems * bitInterval);

//        System.out.println("maxSum " + maxSum);
//        System.out.println("minSum " + minSum);
//        System.out.println("maxSumItems " + maxSumItems);
//        System.out.println("minSumItems " + minSumItems);
//        System.out.println("averageMax " + averageMaxValue);
//        System.out.println("averageMin " + averageMinValue);

        System.out.println();
    }

    private void find_X_MinMax() {
        if(isCANMessageInData){
            minX = frameSamples.get(0).getTime();
            maxX = frameSamples.get(frameSamples.size() - 1).getTime();
        }
        else{
            minX = samples.get(0).getTime();
            maxX = samples.get(frameSamples.size() - 1).getTime();
        }
    }

    public void findMessageBits(){

        messageBitsString= "0x";
        boolean beginningFound = false;
        Integer firstIndexOfID = 0;

        System.out.println("Bit interval " + bitInterval);
        int frameSamplesSize = frameSamples.size();
        for(int i = 0; i < frameSamplesSize; i++) {

            if(beginningFound){
                double sum = 0;

                if((i + bitInterval) < frameSamplesSize){
                    for(int j = i; j < i + bitInterval; j++){
                        sum += frameSamples.get(j).getValue();
                    }

                    double average = sum/bitInterval;
//                    System.out.println("Average: " + average);
                    if(Math.abs(average) > 1.5){
                        messageBitsString += "0";
//                        System.out.println("0");
                    }
                    else{
                        messageBitsString += "1";
//                        System.out.println("1");
                    }

                    bitRectangles.add(new Rectangle2D.Double(frameSamples.get(i).getTime(), -0.5, frameSamples.get(i+bitInterval).getTime() - frameSamples.get(i).getTime(), 0.5));
                    i += bitInterval;
                }
                else{
                    break;
                }
            }

            if(!beginningFound && Math.abs(frameSamples.get(i).getValue()) >= 1.5){
                beginningFound = true;
                firstXValueIDMessage = frameSamples.get(i + bitInterval).getTime();
                firstIndexOfID = i + bitInterval;
                i += bitInterval;
            }
        }
        lastXValueIDMessage = frameSamples.get(firstIndexOfID + bitInterval * ID_LENGTH).getTime();
        System.out.println("Message bits " + messageBitsString);
        System.out.println("Substring: " + messageBitsString.substring(2, 13));
    }

    public String getMessageStringID() {
        if(messageBitsString.isEmpty()){
            return "No CAN message in data";
        }

        Integer valueOfFrameID = Integer.parseInt(messageBitsString.substring(2, 13), 2);
        return "0x" + Integer.toHexString(valueOfFrameID);
    }

    public List<XYChart.Data> getChartData() {
        return chartData;
    }

    @Override
    public String toString() {
        return "OsciloscopeData{" +
                "filePath='" + filePath + '\'' +
                ", samples=" + samples +
                '}';
    }
}
