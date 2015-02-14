package edu.grinnell.glicious;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

public class CustomTextView extends TextView {

	
	public CustomTextView(Context context) {
		super(context);
		init(null);
	}
	
	public CustomTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		if (!isInEditMode())
		init(attrs);
	}
	
	public CustomTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		if (!isInEditMode())
		init(attrs);
	}
	
	private void init(AttributeSet attrs) {
		if (attrs!=null) {
			 TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.CustomTextView);
			 String fontName = a.getString(R.styleable.CustomTextView_fontName);
			 String fontColor = a.getString(R.styleable.CustomTextView_fontColor);
			 if (fontName!=null) {
				 Typeface myTypeface = Typeface.createFromAsset(getContext().getAssets(), "fonts/"+fontName);
				 setTypeface(myTypeface);
			 }
			 if (fontColor != null){
				 setTextColor(Color.parseColor(fontColor));
			 }
			 
			 a.recycle();
		}
		
	}

}
