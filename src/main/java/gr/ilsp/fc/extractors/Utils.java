package gr.ilsp.fc.extractors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

public class Utils {

	public static double minimum_space_width = 1;
	
	private static final HashMap<String,String> invalidChars = new HashMap<String,String>(){
		/**
		 * 
		 */
		private static final long serialVersionUID = -7208860988679686271L;

		{ 
			put("\\uFEFF", " "); //ZERO WIDTH NO-BREAK SPACE
			put("\\u00a0", " "); //NO BREAK SPACE
			put("\\u200E"," "); //LEFT-TO-RIGHT MARK
			put("\\u0097", "-"); //END OF GUARDED AREA
			put("\\u0092", "’"); //APOSTROPHE
			put("\\u0313","’"); //COMBINING COMMA ABOVE
			put("\\u0094", "”"); //CLOSE DOUBLE QUOTE
			put("\\u0093", "“"); //OPEN DOUBLE QUOTE
			put("\\u0095", "•"); 
			put("\\u0096", "-");
			put("\\u0081", " ");
			put("\\u202f", " "); //NARROW NO-BREAK SPACE
			put("\\u2206", "Δ"); //INCREMENT
			put("\\u02bc", "’"); //MODIFIER LETTER APOSTROPHE
			put("\\u003e", ">"); //GREATER-THAN SIGN
			put("\\uFFFD",""); //REPLACEMENT CHARACTER Specials
			put("\\uF0D8",""); //INVALID UNICODE CHARACTER
			put("\\uF02D",""); //INVALID UNICODE CHARACTER
			put("\\uF0FC",""); //INVALID UNICODE CHARACTER
			put("\\uF034",""); //INVALID UNICODE CHARACTER
			put("\\uF076",""); //INVALID UNICODE CHARACTER
			put("\\uF0BC",""); //INVALID UNICODE CHARACTER
			put("\\uF06C",""); //INVALID UNICODE CHARACTER
			put("\\uF0E8",""); //INVALID UNICODE CHARACTER
			put("\\uF0B7",""); //INVALID UNICODE CHARACTER
			put("\\uF0A7",""); //INVALID UNICODE CHARACTER
			put("\\uF0FB",""); //INVALID UNICODE CHARACTER
			put("\\uF06E",""); //INVALID UNICODE CHARACTER
			put("\\uF0F1",""); //INVALID UNICODE CHARACTER
			put("\\uF075",""); //INVALID UNICODE CHARACTER
			put("\\u2126","Ω"); //OHM SIGN
			put("\\u25B6","►"); //BLACK RIGHT-POINTING TRIANGLE
			put("\\u200F"," "); //RIGHT-TO-LEFT MARK
			put("\\u0080","€"); //RIGHT-TO-LEFT MARK
			put("\\u2082","2"); // SUBSCRIPT TWO
			//got from prokopis
			put("\\u2002"," "); // EN SPACE
			put("\\u2003"," "); // EM SPACE
			put("\\u2004"," "); // THREE-PER-EM SPACE
			put("\\u2005"," "); // FOUR-PER-EM SPACE
			put("\\u2006"," "); // SIX-PER-EM SPACE
			put("\\u2007"," "); // FIGURE SPACE
			put("\\u2008"," "); // PUNCTUATION SPACE
			put("\\u2009"," "); // THIN SPACE
			put("\\u200A"," "); // HAIR SPACE
		}
	};
		
	public static double otsu_sections(ArrayList<Double> candidate_spaces) {
		//FIXME implementation should be improved to avoid redundant loops 
		double thr=0, var1, var2, min_var;

		if (candidate_spaces.size()==0)
			return -1;
		double[] temp = sort(candidate_spaces);

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

	public static double getMax(double[] thrs_var) {
		double res=-1000000000;
		for (int ii=0;ii<thrs_var.length;ii++){
			if (thrs_var[ii]>res){
				res=thrs_var[ii];
			}
		}
		return res;
	}
		
	public static double getMedian(double[] m) {
	    int middle = m.length/2;
	    if (m.length%2 == 1) {
	        return m[middle];
	    } else {
	        return (m[middle-1] + m[middle]) / 2.0;
	    }
	}
	
	private static double[] sort(ArrayList<Double> candidate_spaces) {
		double[] result = new double[candidate_spaces.size()];
		for (int i = 0; i < result.length; i++){
			result[i] =candidate_spaces.get(i);
		}
		Arrays.sort(result);
		return result;
	}

	public static double getMean(Double[] temp)	{
		double sum = 0.0;
		for(double a : temp)
			sum += a;
		return sum/temp.length;
	}
	
	public static double getMean(double[] temp)	{
		double sum = 0.0;
		for(double a : temp)
			sum += a;
		return sum/temp.length;
	}
		
	public static double getVariance(Double[] data)	{
		double mean = getMean(data);
		double temp = 0;
		for(double a :data)
			temp += (mean-a)*(mean-a);
		return temp/data.length;
	}

	public static double[] find_most_commonValue(double[] x_st) {
		int c=0;
		double[] result=new double[2];
		HashMap<Double, Integer> temp = new HashMap<Double, Integer>();
		for (int ii=0;ii<x_st.length;ii++){
			if (temp.containsKey(x_st[ii]))
				temp.put(x_st[ii], temp.get(x_st[ii])+1);
			else
				temp.put(x_st[ii], 1);	
		}
		Set<Double> keys=temp.keySet();
		Iterator<Double> it = keys.iterator();
		int[] temp_counter=new int[keys.size()];
		double[] values = new double[keys.size()];
		while (it.hasNext()){
			double key=it.next();
			temp_counter[c]=temp.get(key);
			values[c]=key;
			c++;
		}
		Arrays.sort(temp_counter);
	
		if (temp_counter[temp_counter.length-1]==1){
			Arrays.sort(values);
			result[0]=values[c-1];
			result[1]=-1;
			return result;
		}
		it = keys.iterator();
		c=temp_counter[keys.size()-1];
		result[1]=c;
		keys=temp.keySet();
		it = keys.iterator();
		while (it.hasNext()){
			result[0]=it.next();
			if (temp.get(result[0])==c)
				break;
		}
		return result;
	}

	public static String normalizeContent(String text){
		for (String s:invalidChars.keySet()){
			text = text.replaceAll(s, invalidChars.get(s));
		}
		return text;
	}
	
 	public static int[] sortArrayList(ArrayList<Integer> pars) {
		int[] array=new int[pars.size()];
		for (int ii=0;ii<pars.size();ii++)
			array[ii]=pars.get(ii);

		Arrays.sort(array);
		return array;
	}

 	
 	public static double getVariance(double[] data)	{
		double mean = getMean(data);
		double temp = 0;
		for(double a :data)
			temp += (mean-a)*(mean-a);
		return temp/data.length;
	}

	public static double getStdDev(double[] data){
		return Math.sqrt(getVariance(data));
	}

	public static double median(Double[] data) 	{
		Double[] b = new Double[data.length];
		System.arraycopy(data, 0, b, 0, b.length);
		Arrays.sort(b);

		if (data.length % 2 == 0) 
			return (b[(b.length / 2) - 1] + b[b.length / 2]) / 2.0;
		else 
			return b[b.length / 2];
	}
 	
}
