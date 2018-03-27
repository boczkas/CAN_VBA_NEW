public class Sample {
    private Double time;
    private Double value;

    public Sample() {
        this.time = new Double(0);
        this.value = new Double(0);
    }

    public Sample(Double time, Double value) {
        this.time = time;
        this.value = value;
    }

    public Double getTime() {
        return time;
    }

    public Double getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "time " + time + " value " + value + "\n";
    }
}
