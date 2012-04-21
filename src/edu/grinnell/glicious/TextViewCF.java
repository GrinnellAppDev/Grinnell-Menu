package edu.grinnell.glicious;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;
import edu.grinnell.glicious.R;

public class TextViewCF extends TextView { 
	private static final String TAG = "TextViewCF"; 

        public TextViewCF(Context context) {
            super(context);
        }

        public TextViewCF(Context context, AttributeSet attrs) {
            super(context, attrs);
            UiUtil.setCustomFont(this,context,attrs,
                    R.styleable.TextViewCF,
                    R.styleable.TextViewCF_customFont);
        }

        public TextViewCF(Context context, AttributeSet attrs, int defStyle) {
            super(context, attrs, defStyle);
            UiUtil.setCustomFont(this,context,attrs,
            		R.styleable.TextViewCF,
                    R.styleable.TextViewCF_customFont);
        }
    }
 
