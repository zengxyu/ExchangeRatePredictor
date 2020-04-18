package org.example.util;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 * 画散点图
 */
public class PlotScatter extends JFrame {
    private static final long serialVersionUID = 6294689542092367723L;
    public PlotScatter(String title, List<Double> realLabels, List<Double> predictedLabels) {

        // Create dataset
        XYDataset dataset = createDataset(realLabels,predictedLabels);
        String t = title + " Comparison chart : Real Labels VS Predicted Labels";
        // Create chart
        JFreeChart chart = ChartFactory.createScatterPlot(
            t, "Date", "Exchange rate", dataset, PlotOrientation.VERTICAL,true,true,true);
        //Changes background color
        XYPlot plot = (XYPlot)chart.getPlot();
        plot.setBackgroundPaint(new Color(255,255,255));


        // Create Panel
        ChartPanel panel = new ChartPanel(chart);
        setContentPane(panel);
    }


    private XYDataset createDataset(List<Double> realLabels, List<Double> predictedLabels) {
        XYSeriesCollection dataset = new XYSeriesCollection();
        XYSeries series1 = new XYSeries("Real labels");
        //RealLabels
        for (int i = 0;i < realLabels.size();i++){
            series1.add(i, realLabels.get(i));
        }
        XYSeries series2 = new XYSeries("Predicted Labels");
        //RealLabels
        for (int i = 0;i < predictedLabels.size();i++){
            series2.add(i, predictedLabels.get(i));
        }
        dataset.addSeries(series1);
        dataset.addSeries(series2);

        return dataset;
    }

    public static void main(String[] args) {

    }
}
