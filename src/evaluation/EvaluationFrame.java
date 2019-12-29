package evaluation;

import javax.swing.JFrame;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import models.Evaluator;

public abstract class EvaluationFrame implements Evaluator {
	private JFrame frame;
	private XYSeries series = new XYSeries("Evaluation");
	private long time;
	public EvaluationFrame(String name, String lossName) {
		frame = new JFrame();
	    XYSeriesCollection dataset = new XYSeriesCollection();
	    dataset.addSeries(series);
	    time = 0;
	    JFreeChart chart = ChartFactory.createXYLineChart(
	    	lossName,
	        "#Interactions",
	        "Value",
	        dataset,
	        PlotOrientation.VERTICAL,
	        true, true, false);

	    ChartPanel panel = new ChartPanel(chart);
	    frame.setContentPane(panel);
		frame.setSize(800, 400);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}
	
	public long getTime() {
		return time;
	}
	
	public abstract double register(Object source, double outcome);
	
	@Override
	public void aggregate(Object source, double outcome) {
		series.add(time, register(source, outcome));
	    time += 1;
	    frame.repaint();
	}
}
