package edu.grinnell.glicious.menucontent;

import java.util.HashMap;
import java.util.Map;

public class NutritionUtil {

	public static Map<String, NutritionInfo> INFO = new HashMap<String, NutritionInfo>();
	
	static {
		INFO.put("KCAL", new NutritionInfo(NutritionInfo.KCAL, 	"", 	1));
		INFO.put("FAT", new NutritionInfo(NutritionInfo.FAT, 	"g", 	2));
		INFO.put("SFA", new NutritionInfo(NutritionInfo.SFA, 	"g", 	3));
		INFO.put("MONO", new NutritionInfo(NutritionInfo.MONO, 	"g", 	4));
		INFO.put("POLY", new NutritionInfo(NutritionInfo.POLY, 	"g", 	5));
		INFO.put("FATRN", new NutritionInfo(NutritionInfo.FATRN,"g", 	6));
		INFO.put("CHOL", new NutritionInfo(NutritionInfo.CHOL, 	"mg", 	7));
		INFO.put("NA", new NutritionInfo(NutritionInfo.NA, 		"mg", 	8));
		INFO.put("TDFB", new NutritionInfo(NutritionInfo.TDFB, 	"mg", 	9));
		INFO.put("SUGR", new NutritionInfo(NutritionInfo.SUGR, 	"g", 	10));
		INFO.put("PRO", new NutritionInfo(NutritionInfo.PRO, 	"g", 	11));
		INFO.put("VTAIU", new NutritionInfo(NutritionInfo.VTAIU,"IU", 	12));
		INFO.put("VITC", new NutritionInfo(NutritionInfo.VITC, 	"mg", 	13));
		INFO.put("B6", new NutritionInfo(NutritionInfo.B6, 		"mg", 	14));
		INFO.put("B12", new NutritionInfo(NutritionInfo.B12 , 	"mcg", 	15));
		INFO.put("CHO", new NutritionInfo(NutritionInfo.CHO, 	"mg", 	16));
		INFO.put("CA", new NutritionInfo(NutritionInfo.CA, 		"mg",	17));
		INFO.put("FE", new NutritionInfo(NutritionInfo.FE, 		"mg", 	18));
		INFO.put("K", new NutritionInfo(NutritionInfo.K, 		"mg", 	19));
		INFO.put("ZN", new NutritionInfo(NutritionInfo.ZN, 		"mg", 	20));
		
	}
	
	public static class NutritionInfo {

		public static final String VITC 	= "Vitamin C";
		public static final String TDFB 	= "Dietary Fiber";
		public static final String FE 		= "Iron";
		public static final String B6 		= "Vitamin B6";
		public static final String B12 		= "Vitamin B12";
		public static final String MONO 	= "Monounsaturated Fat";
		public static final String CA 		= "Calcium";
		public static final String CHOL 	= "Cholesterol";
		public static final String FATRN 	= "Trans Fat";
		public static final String K 		= "Potassium";
		public static final String ZN 		= "Zinc";
		public static final String VTAIU 	= "Vitamin A";
		public static final String POLY 	= "Polyunsaturated Fat";
		public static final String SUGR 	= "Sugar";
		public static final String KCAL 	= "Calories";
		public static final String PRO 		= "Protein";
		public static final String CHO 		= "Choline";
		public static final String FAT 		= "Fat";
		public static final String NA 		= "Sodium";
		public static final String SFA 		= "Saturated Fat";
		
		public NutritionInfo() {}
		
		public NutritionInfo(String name, String unit) {
			this.name = name;
			this.unit = unit;
			this.order = 0;
			
		}
		
		public NutritionInfo(String name, String unit, int order) {
			this(name, unit);
			this.order = order;
		}
		
		public String name;
		public String unit;
		public int order;
		
	}
}
