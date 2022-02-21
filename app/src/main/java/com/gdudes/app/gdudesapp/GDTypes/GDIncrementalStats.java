package com.gdudes.app.gdudesapp.GDTypes;

public class GDIncrementalStats {
    public int MinValue;
    public int MaxValue;
    public int Increment;
    public String Metric;
    public String Imperial;
    public Double ConversionFactor;

    public GDIncrementalStats() {
        MinValue = 0;
        MaxValue = 0;
        Increment = 0;
        Metric = "";
        Imperial = "";
        ConversionFactor = 0.0;
    }

    public GDIncrementalStats(int vMinValue, int vMaxValue, int vIncrement, String vMetric, String vImperial, Double vConversionFactor) {
        MinValue = vMinValue;
        MaxValue = vMaxValue;
        Increment = vIncrement;
        Metric = vMetric;
        Imperial = vImperial;
        ConversionFactor = vConversionFactor;
    }
}
