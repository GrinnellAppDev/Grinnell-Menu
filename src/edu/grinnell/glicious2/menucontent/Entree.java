package edu.grinnell.glicious2.menucontent;

import org.json.JSONObject;

public class Entree {

    	public static final int VENUENTREE 	= 0;
    	public static final int DISHENTREE 	= 1;
    	
    	// JSON Entree keys..
    	public static final String NAME		 = "name";
    	public static final String VEGAN	 = "vegan";
    	public static final String OVOLACTO	 = "ovolacto";
    	public static final String GLUTENFREE = "gluten_free";
    	public static final String HALAL	 = "halal";
    	public static final String PASSOVER	 = "passover";
    	public static final String NUTRITION = "nutrition";
    	
        public String id;
        public String name;
        public boolean vegan;
        public boolean ovolacto;
        public boolean glutenfree;
        public boolean halal;
        public boolean passover;
        public JSONObject nutrition;
        public int type;

        public Entree(String id, JSONObject entree, int type) {
        	this(entree, type);
            this.id = id;
        }
        
        public Entree(JSONObject entree, int type) {
        	this.name 			= entree.optString(NAME);
        	this.id = this.name;
        	this.vegan 			= entree.optBoolean(VEGAN);
        	this.ovolacto 		= entree.optBoolean(OVOLACTO);
        	this.glutenfree 	= entree.optBoolean(GLUTENFREE);
        	this.halal 			= entree.optBoolean(HALAL);
        	this.passover 		= entree.optBoolean(PASSOVER);
        	this.nutrition 		= entree.optJSONObject(NUTRITION);
        	this.type = type;
        }

        public Entree( String id, String name, int type ) {
        	this.id  = id;
        	this.name = name;
        	this.type = type;
        }
        
        @Override
        public String toString() {
        	return name;
        }
    }