package gr.ilsp.fc.utils;


import gr.ilsp.fc.aligner.factory.ILSPAlignment;
import gr.ilsp.fc.tmxhandler.TMXHandlerUtils.SegPair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class Statistics {
	public static double minimum_space_width = 1;
	private static double text_thres=0.4;
	enum SortingOrder{
	    ASCENDING, DESCENDING;
	};

	
	public static void main(String[] args) throws IOException {
		String[][] temp = new String[3][2];
		temp[0][0] = "en";  temp[0][1] = "25";
		temp[1][0] = "fr";  temp[1][1] = "35";
		temp[2][0] = "aa";  temp[2][1] = "15";
		
		sort2darray(temp,1, "d");
		System.out.println("aaa");
		
	}
	
	private static class ColumnComparator implements Comparator<String[]> {
		private final int iColumn;
		private final SortingOrder order;
		
		public ColumnComparator(int column, SortingOrder order) {
			this.iColumn = column;
			this.order = order;
		}

		@Override 
		public int compare(String[] c1, String[] c2) {
			int result = c1[iColumn].compareTo(c2[iColumn]);
			return order==SortingOrder.ASCENDING ? result : -result;
		}
	}
	
	public static double getMean(Double[] numarray){
		double sum = 0.0;
		for(double a : numarray)
			sum += a;
		return sum/numarray.length;
	}

	public static double getMedian(Double[] numarray){
		if (numarray.length==0)
			return 0;
		double[] new_numarray = new double[numarray.length];
		for (int ii=0;ii<new_numarray.length;ii++)
			new_numarray[ii] = numarray[ii];
		Arrays.sort(new_numarray);
		int middle = new_numarray.length/2;
		double medianValue = 0; //declare variable 
		if (new_numarray.length%2 == 1) 
			medianValue = new_numarray[middle];
		else
			medianValue = (new_numarray[middle-1] + new_numarray[middle]) / 2;

		return medianValue;
	}

	public static double getVariance(Double[] numarray)	{
		double mean = getMean(numarray);
		double temp = 0;
		for(double a :numarray)
			temp += (mean-a)*(mean-a);
		return temp/numarray.length;
	}

	public static double getStdDev(Double[] numarray){
		return Math.sqrt(getVariance(numarray));
	}

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

	public static double getMax(Double[] numarray) {
		if (numarray.length==0)
			return 0;
		double[] new_numarray = new double[numarray.length];
		for (int ii=0;ii<new_numarray.length;ii++)
			new_numarray[ii] = numarray[ii];
		Arrays.sort(new_numarray);
		return new_numarray[new_numarray.length-1];
	}

	public static String[][] sort2darray(String[][] arr, int col, String order) {
		ColumnComparator cc = null;
		if (order.equals("a"));
			cc = new ColumnComparator(0,SortingOrder.ASCENDING);
		if (order.equals("d"))
			cc = new ColumnComparator(0,SortingOrder.DESCENDING);
		Arrays.sort(arr, cc);
		return arr;
	}
	
	public static Set<SegPair> distinctRandomSegPairs(List<SegPair> range, int samplesize){
		Set<SegPair> sample = new HashSet<SegPair>();
		List<Integer> index = new ArrayList<Integer>();
		for (int ii=0;ii<range.size();ii++)
			index.add(ii);
		
		final Random random = new Random();
	    for (int ii=0;ii<samplesize;ii++){
	    	int sel = index.get(random.nextInt(index.size()));
	    	//System.out.println(sel);
	    	sample.add(range.get(sel));
	    	index.remove(sel);
	    	range.remove(sel);
	    	index = new ArrayList<Integer>();
	    	for (int jj=0;jj<range.size();jj++)
				index.add(jj);
	    	
	    }
		return sample;
	}
	
	public static Set<ILSPAlignment> distinctRandomILSPAlignments(List<ILSPAlignment> range, int samplesize){
		Set<ILSPAlignment> sample = new HashSet<ILSPAlignment>();
		List<Integer> index = new ArrayList<Integer>();
		for (int ii=0;ii<range.size();ii++)
			index.add(ii);
		
		final Random random = new Random();
	    for (int ii=0;ii<samplesize;ii++){
	    	int sel = index.get(random.nextInt(index.size()));
	    	//System.out.println(sel);
	    	sample.add(range.get(sel));
	    	index.remove(sel);
	    	range.remove(sel);
	    	index = new ArrayList<Integer>();
	    	for (int jj=0;jj<range.size();jj++)
				index.add(jj);
	    	
	    }
		return sample;
	}
	
	
	public static double otsu(ArrayList<Double> candidate_spaces, double mean_char_width) {
		//FIXME implementation should be improved to avoid redundant loops 
		double thr=0, var1, var2, min_var;

		if (candidate_spaces.size()==0)
			return -1;
		double[] temp = sort(candidate_spaces);
		//if all candidates are higher (lower) than a typical value, all candidates are real (not real).
		if (temp[temp.length-1]<minimum_space_width)   
			return 1000000;
		//if (temp[0]>Math.max(minimum_space_width,)) 
		if (temp[0]>Math.max(minimum_space_width,mean_char_width))	
			return temp[0];

		double bins=50;
		double[] thrs=new double[(int) bins];
		double[] thrs_var=new double[(int) bins];
		double step=temp[temp.length-1]/bins;
		if (step<0.00001)
			return temp[0];
		for (int ii=0;ii<bins;ii++)
			thrs[ii]=temp[0]+ii*step;

		for (int ii=0;ii<thrs.length;ii++){
			ArrayList<Double> class1=new ArrayList<Double>();
			ArrayList<Double> class2=new ArrayList<Double>();
			int count1=0, count2=0;
			for (int jj=0;jj<temp.length;jj++){
				if (temp[jj]<thrs[ii]){
					class1.add(temp[jj]);
					count1++;
				}else{
					class2.add(temp[jj]);
					count2++;
				}
			}
			Double[] class11 = class1.toArray(new Double[0]);
			Double[] class22 = class2.toArray(new Double[0]);
			if (class11.length==0)
				var1=0;
			else
				var1=getVariance(class11); //variance of clas1
			if (class22.length==0)
				var2=0; 
			else
				var2=getVariance(class22); //variance of clas2

			thrs_var[ii]=((double)count1/(double)temp.length)*var1+((double)count2/(double)temp.length)*var2; //intra_class_variance
		}
		min_var=getMin(thrs_var);
		thr=-1;
		for (int ii=0;ii<thrs_var.length;ii++){
			if (thrs_var[ii]==min_var){
				if (thr<0){
					thr=thrs[ii];
				}
				else
					thr=(thr+thrs[ii])/2;
			}
		}
		return thr;
	}
	
	public static double getMin(double[] thrs_var) {
		double res=1000000000;
		for (int ii=0;ii<thrs_var.length;ii++){
			if (thrs_var[ii]<res){
				res=thrs_var[ii];
			}
		}
		return res;
	}

	public static double getMin(Double[] thrs_var) {
		double res=1000000000;
		for (int ii=0;ii<thrs_var.length;ii++){
			if (thrs_var[ii]<res){
				res=thrs_var[ii];
			}
		}
		return res;
	}
	
	private static double[] sort(ArrayList<Double> candidate_spaces) {
		double[] result = new double[candidate_spaces.size()];
		for (int i = 0; i < result.length; i++){
			result[i] =candidate_spaces.get(i);
		}
		Arrays.sort(result);
		return result;
	}
	
}
