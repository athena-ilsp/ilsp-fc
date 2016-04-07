package gr.ilsp.fc.utils;


import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

public class Statistics {

	private static double text_thres=0.4;
	
	public static double getMean(Double[] numarray)
	{
		double sum = 0.0;
		for(double a : numarray)
			sum += a;
		return sum/numarray.length;
	}

	public static double getMedian(Double[] numarray){
		if (numarray.length==0)
			return 0;
		Arrays.sort(numarray);
		int middle = numarray.length/2;
		double medianValue = 0; //declare variable 
		if (numarray.length%2 == 1) 
		    medianValue = numarray[middle];
		else
		   medianValue = (numarray[middle-1] + numarray[middle]) / 2;
		
		return medianValue;
	}
	
	
	
	public static double getVariance(Double[] numarray)
	{
		double mean = getMean(numarray);
		double temp = 0;
		for(double a :numarray)
			temp += (mean-a)*(mean-a);
		return temp/numarray.length;
	}

	public static double getStdDev(Double[] numarray)
	{
		return Math.sqrt(getVariance(numarray));
	}

	/*public static double median(Double[] numarray) 
	{
		Double[] b = new Double[numarray.length];
		System.arraycopy(numarray, 0, b, 0, b.length);
		Arrays.sort(b);

		if (numarray.length % 2 == 0) 
			return (b[(b.length / 2) - 1] + b[b.length / 2]) / 2.0;
		else 
			return b[b.length / 2];
	}*/
	
	public static int min3(int i, int j, int k) {
		int l = Math.min(i, j);
		int res = Math.min(l, k);
		return res;
	}
	
	public static double editDist(String s, String t) {
		int cost;

		if (s==t)	return 0;
		if (s.length() == 0) return t.length();
		if (t.length() == 0) return s.length();

		int[] v0 = new int[t.length() + 1];
		int[] v1 = new int[t.length() + 1];

		for (int i = 0; i < v0.length; i++)
			v0[i] = i;

		for (int i = 0; i < s.length(); i++){
			// calculate v1 (current row distances) from the previous row v0
			// first element of v1 is A[i+1][0]
			//   edit distance is delete (i+1) chars from s to match empty t
			v1[0] = i + 1;
			// use formula to fill in the rest of the row
			for (int j = 0; j < t.length(); j++){
				if (s.substring(i, i+1).equals(t.substring(j, j+1))) 
					cost=0;
				else
					cost=1;
				v1[j + 1] = Statistics.min3(v1[j] + 1, v0[j + 1] + 1, v0[j] + cost);
			}
			// copy v1 (current row) to v0 (previous row) for next iteration
			for (int j = 0; j < v0.length; j++)
				v0[j] = v1[j];
		}
		return v1[t.length()];
	}
	
	public static int editDist(int[] sl, int[] tl) {
		int n=sl.length, m=tl.length, cost, sl_i, tl_j; 
		int d[][] = new int[n+1][m+1]; // matrix
		for (int i=0; i<=n; i++) {d[i][0]=i;}
		for (int j=0; j<=m; j++) {d[0][j]=j;}
		for (int i=1; i<= n; i++) {
			sl_i = sl[i-1];
			for (int j=1; j<=m; j++) {
				tl_j = tl[j-1];
				if (sl_i<0 & tl_j<0) {
					if (sl_i==tl_j)
						cost = 0;
					else
						cost = 1;
				}
				else{
					if (sl_i*tl_j<0)
						cost = 1000000000;
					else{
						if (Double.parseDouble(Integer.toString(Math.abs(sl_i-tl_j)))/
								Double.parseDouble(Integer.toString(Math.max(sl_i,tl_j)))>=text_thres)
							cost=1;
						else
							cost=0;
					}
				}
				d[i][j] = Statistics.min3(d[i-1][j]+1, d[i][j-1]+1, d[i-1][j-1]+cost);
			}
		}
		return d[n][m];
	}
	
	/**
	 * calculate by using SVM with linear kernel
	 * @param f1
	 * @param f2
	 * @param f3
	 * @param sv
	 * @param w
	 * @param b
	 * @param degree
	 * @return
	 */
	public static double SVM_test(double f1, double f2, double f3,
			double[][] sv, double[][] w, double[][] b, double degree) {
		Double res=0.0;
		double temp2, temp1;
		double par1, par2, par3, par123;
		for (int j=0;j<sv.length;j++){
			par1 = f1*sv[j][0];
			par2 = f2*sv[j][1];
			par3 = f3*sv[j][2];
			par123 = par1 + par2 + par3+1;
			temp1=Math.pow(par123,degree);
			temp2=temp1*w[j][0];
			res=res+temp2; 
		}
		res=res+b[0][0];
		return res;
	}

	public static HashMap<Double, Double> getBins(Double[] numArray) {
		HashMap<Double, Double> bins= new HashMap<Double, Double>();
		double tempFreq = 0;
		for (int ii=0;ii<numArray.length;ii++){
			if (bins.containsKey(numArray[ii]))
				tempFreq = bins.get(numArray[ii])+1;
			else
				tempFreq = 1.0;
			
			bins.put(numArray[ii],tempFreq);
		}
		return bins;
	}

	public static boolean isMainBin(HashMap<Double, Double> binsAttrs,	double thr_persent) {
		boolean res=false;
		double bin_key=0;
		Set<Double> bins = binsAttrs.keySet();
		double bins_num = (double)bins.size();
		Iterator<Double> it_bin = bins.iterator();
		while (it_bin.hasNext()){
			bin_key = it_bin.next();
			if ((binsAttrs.get(bin_key)/bins_num)>thr_persent){
				return true;
			}
		}
		return res;
	}
	
	
}
