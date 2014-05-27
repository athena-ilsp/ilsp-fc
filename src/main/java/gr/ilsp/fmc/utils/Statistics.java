package gr.ilsp.fmc.utils;


import java.util.Arrays;

public class Statistics {


	public static double getMean(Double[] temp)
	{
		double sum = 0.0;
		for(double a : temp)
			sum += a;
		return sum/temp.length;
	}

	public static double getVariance(Double[] data)
	{
		double mean = getMean(data);
		double temp = 0;
		for(double a :data)
			temp += (mean-a)*(mean-a);
		return temp/data.length;
	}

	public static double getStdDev(Double[] data)
	{
		return Math.sqrt(getVariance(data));
	}

	public static double median(Double[] data) 
	{
		Double[] b = new Double[data.length];
		System.arraycopy(data, 0, b, 0, b.length);
		Arrays.sort(b);

		if (data.length % 2 == 0) 
			return (b[(b.length / 2) - 1] + b[b.length / 2]) / 2.0;
		else 
			return b[b.length / 2];
	}
}
