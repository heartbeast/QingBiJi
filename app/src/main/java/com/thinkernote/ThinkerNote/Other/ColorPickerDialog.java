package com.thinkernote.ThinkerNote.Other;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import com.thinkernote.ThinkerNote.Utils.MLog;

public class ColorPickerDialog extends Dialog {
	private final boolean debug = true;
	private final String TAG = "ColorPicker";
	
	Context context;
	private String title;//鏍囬
	private int mInitialColor;//鍒濆棰滆壊
    private OnColorChangedListener mListener;

	/**
     * 鍒濆棰滆壊榛戣壊
     * @param context
     * @param title 瀵硅瘽妗嗘爣棰�
     * @param listener 鍥炶皟
     */
    public ColorPickerDialog(Context context, String title, 
    		OnColorChangedListener listener) {
    	this(context, Color.BLACK, title, listener);
    }
    
    /**
     * 
     * @param context
     * @param initialColor 鍒濆棰滆壊
     * @param title 鏍囬
     * @param listener 鍥炶皟
     */
    public ColorPickerDialog(Context context, int initialColor, 
    		String title, OnColorChangedListener listener) {
        super(context);
        this.context = context;
        mListener = listener;
        mInitialColor = initialColor;
        this.title = title;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowManager manager = getWindow().getWindowManager();
		int height = (int) (manager.getDefaultDisplay().getHeight() * 0.6f);
		int width = (int) (manager.getDefaultDisplay().getWidth() * 0.6f);
		int min = Math.min(width, height);
		
		ColorPickerView myView = new ColorPickerView(context, 
				Math.max(min+76, 316), 
				Math.max(min, 240));
        setContentView(myView);
        setTitle(title);
    }
    
    private class ColorPickerView extends View {
    	private Paint mPaint;//娓愬彉鑹茬幆鐢荤瑪
    	private Paint mCenterPaint;//涓棿鍦嗙敾绗�
    	private Paint mLinePaint;//鍒嗛殧绾跨敾绗�
    	private Paint mRectPaint;//娓愬彉鏂瑰潡鐢荤瑪
    	
    	private Shader rectShader;//娓愬彉鏂瑰潡娓愬彉鍥惧儚
    	private float rectLeft;//娓愬彉鏂瑰潡宸鍧愭爣
    	private float rectTop;//娓愬彉鏂瑰潡鍙硏鍧愭爣
    	private float rectRight;//娓愬彉鏂瑰潡涓妝鍧愭爣
    	private float rectBottom;//娓愬彉鏂瑰潡涓媦鍧愭爣
        
    	private final int[] mCircleColors;//娓愬彉鑹茬幆棰滆壊
    	private final int[] mRectColors;//娓愬彉鏂瑰潡棰滆壊
    	
    	private int mHeight;//View楂�
    	private int mWidth;//View瀹�
    	private float r;//鑹茬幆鍗婂緞(paint涓儴)
    	private float centerRadius;//涓績鍦嗗崐寰�
    	
    	private boolean downInCircle = true;//鎸夊湪娓愬彉鐜笂
    	private boolean downInRect;//鎸夊湪娓愬彉鏂瑰潡涓�
    	private boolean highlightCenter;//楂樹寒
    	private boolean highlightCenterLittle;//寰寒
    	
		public ColorPickerView(Context context, int height, int width) {
			super(context);
			this.mHeight = height - 36;
			this.mWidth = width;
			setMinimumHeight(height - 36);
			setMinimumWidth(width);
			
			//娓愬彉鑹茬幆鍙傛暟
	    	mCircleColors = new int[] {0xFFFF0000, 0xFFFF00FF, 0xFF0000FF, 
	    			0xFF00FFFF, 0xFF00FF00,0xFFFFFF00, 0xFFFF0000};
	    	Shader s = new SweepGradient(0, 0, mCircleColors, null);
            mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mPaint.setShader(s);
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeWidth(50);
            r = width / 2 * 0.7f - mPaint.getStrokeWidth() * 0.5f;
            
            //涓績鍦嗗弬鏁�
            mCenterPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mCenterPaint.setColor(mInitialColor);
            mCenterPaint.setStrokeWidth(5);
            centerRadius = (r - mPaint.getStrokeWidth() / 2 ) * 0.7f;
            
            //杈规鍙傛暟
            mLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mLinePaint.setColor(Color.parseColor("#72A1D1"));
            mLinePaint.setStrokeWidth(4);
            
            //榛戠櫧娓愬彉鍙傛暟
            mRectColors = new int[]{0xFF000000, mCenterPaint.getColor(), 0xFFFFFFFF};
            mRectPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mRectPaint.setStrokeWidth(5);
            rectLeft = -r - mPaint.getStrokeWidth() * 0.5f;
            rectTop = r + mPaint.getStrokeWidth() * 0.5f + 
            		mLinePaint.getStrokeMiter() * 0.5f + 15;
            rectRight = r + mPaint.getStrokeWidth() * 0.5f;
            rectBottom = rectTop + 50;
		}

		@Override
		protected void onDraw(Canvas canvas) {
			//绉诲姩涓績
            canvas.translate(mWidth / 2, mHeight / 2 - 50);
            //鐢讳腑蹇冨渾
            canvas.drawCircle(0, 0, centerRadius,  mCenterPaint);
            //鏄惁鏄剧ず涓績鍦嗗鐨勫皬鍦嗙幆
            if (highlightCenter || highlightCenterLittle) {
                int c = mCenterPaint.getColor();
                mCenterPaint.setStyle(Paint.Style.STROKE);
                if(highlightCenter) {
                	mCenterPaint.setAlpha(0xFF);
                }else if(highlightCenterLittle) {
                	mCenterPaint.setAlpha(0x90);
                }
                canvas.drawCircle(0, 0, 
                		centerRadius + mCenterPaint.getStrokeWidth(),  mCenterPaint);
                
                mCenterPaint.setStyle(Paint.Style.FILL);
                mCenterPaint.setColor(c);
            }
            //鐢昏壊鐜�
            canvas.drawOval(new RectF(-r, -r, r, r), mPaint);
            //鐢婚粦鐧芥笎鍙樺潡
            if(downInCircle) {
            	mRectColors[1] = mCenterPaint.getColor();
            }
            rectShader = new LinearGradient(rectLeft, 0, rectRight, 0, mRectColors, null, Shader.TileMode.MIRROR);
            mRectPaint.setShader(rectShader);
            canvas.drawRect(rectLeft, rectTop, rectRight, rectBottom, mRectPaint);
            float offset = mLinePaint.getStrokeWidth() / 2;
            canvas.drawLine(rectLeft - offset, rectTop - offset * 2, 
            		rectLeft - offset, rectBottom + offset * 2, mLinePaint);//宸�
            canvas.drawLine(rectLeft - offset * 2, rectTop - offset, 
            		rectRight + offset * 2, rectTop - offset, mLinePaint);//涓�
            canvas.drawLine(rectRight + offset, rectTop - offset * 2, 
            		rectRight + offset, rectBottom + offset * 2, mLinePaint);//鍙�
            canvas.drawLine(rectLeft - offset * 2, rectBottom + offset, 
            		rectRight + offset * 2, rectBottom + offset, mLinePaint);//涓�
			super.onDraw(canvas);
		}
		
		@Override
		public boolean onTouchEvent(MotionEvent event) {
			float x = event.getX() - mWidth / 2;
            float y = event.getY() - mHeight / 2 + 50;
            boolean inCircle = inColorCircle(x, y, 
            		r + mPaint.getStrokeWidth() / 2, r - mPaint.getStrokeWidth() / 2);
            boolean inCenter = inCenter(x, y, centerRadius);
            boolean inRect = inRect(x, y);
            
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                	downInCircle = inCircle;
                	downInRect = inRect;
                	highlightCenter = inCenter;
                case MotionEvent.ACTION_MOVE:
                	if(downInCircle && inCircle) {//down鎸夊湪娓愬彉鑹茬幆鍐�, 涓攎ove涔熷湪娓愬彉鑹茬幆鍐�
                		float angle = (float) Math.atan2(y, x);
                        float unit = (float) (angle / (2 * Math.PI));
                        if (unit < 0) {
                            unit += 1;
                        }
	               		mCenterPaint.setColor(interpCircleColor(mCircleColors, unit));
	               		if(debug) MLog.v(TAG, "鑹茬幆鍐�, 鍧愭爣: " + x + "," + y);
                	}else if(downInRect && inRect) {//down鍦ㄦ笎鍙樻柟鍧楀唴, 涓攎ove涔熷湪娓愬彉鏂瑰潡鍐�
                		mCenterPaint.setColor(interpRectColor(mRectColors, x));
                	}
                	if(debug) MLog.v(TAG, "[MOVE] 楂樹寒: " + highlightCenter + "寰寒: " + highlightCenterLittle + " 涓績: " + inCenter);
                	if((highlightCenter && inCenter) || (highlightCenterLittle && inCenter)) {//鐐瑰嚮涓績鍦�, 褰撳墠绉诲姩鍦ㄤ腑蹇冨渾
                		highlightCenter = true;
                		highlightCenterLittle = false;
                	} else if(highlightCenter || highlightCenterLittle) {//鐐瑰嚮鍦ㄤ腑蹇冨渾, 褰撳墠绉诲嚭涓績鍦�
                		highlightCenter = false;
                		highlightCenterLittle = true;
                	} else {
                		highlightCenter = false;
                		highlightCenterLittle = false;
                	}
                   	invalidate();
                	break;
                case MotionEvent.ACTION_UP:
                	if(highlightCenter && inCenter) {//鐐瑰嚮鍦ㄤ腑蹇冨渾, 涓斿綋鍓嶅惎鍔ㄥ湪涓績鍦�
                		if(mListener != null) {
                			mListener.colorChanged(mCenterPaint.getColor());
                    		ColorPickerDialog.this.dismiss();
                		}
                	}
                	if(downInCircle) {
                		downInCircle = false;
                	}
                	if(downInRect) {
                		downInRect = false;
                	}
                	if(highlightCenter) {
                		highlightCenter = false;
                	}
                	if(highlightCenterLittle) {
                		highlightCenterLittle = false;
                	}
                	invalidate();
                    break;
            }
            return true;
		}

		@Override
		protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
			super.onMeasure(mWidth, mHeight);
		}

		/**
		 * 鍧愭爣鏄惁鍦ㄨ壊鐜笂
		 * @param x 鍧愭爣
		 * @param y 鍧愭爣
		 * @param outRadius 鑹茬幆澶栧崐寰�
		 * @param inRadius 鑹茬幆鍐呭崐寰�
		 * @return
		 */
		private boolean inColorCircle(float x, float y, float outRadius, float inRadius) {
			double outCircle = Math.PI * outRadius * outRadius;
			double inCircle = Math.PI * inRadius * inRadius;
			double fingerCircle = Math.PI * (x * x + y * y);
			if(fingerCircle < outCircle && fingerCircle > inCircle) {
				return true;
			}else {
				return false;
			}
		}
		
		/**
		 * 鍧愭爣鏄惁鍦ㄤ腑蹇冨渾涓�
		 * @param x 鍧愭爣
		 * @param y 鍧愭爣
		 * @param centerRadius 鍦嗗崐寰�
		 * @return
		 */
		private boolean inCenter(float x, float y, float centerRadius) {
			double centerCircle = Math.PI * centerRadius * centerRadius;
			double fingerCircle = Math.PI * (x * x + y * y);
			if(fingerCircle < centerCircle) {
				return true;
			}else {
				return false;
			}
		}
		
		/**
		 * 鍧愭爣鏄惁鍦ㄦ笎鍙樿壊涓�
		 * @param x
		 * @param y
		 * @return
		 */
		private boolean inRect(float x, float y) {
			if( x <= rectRight && x >=rectLeft && y <= rectBottom && y >=rectTop) {
				return true;
			} else {
				return false;
			}
		}
		
		/**
		 * 鑾峰彇鍦嗙幆涓婇鑹�
		 * @param colors
		 * @param unit
		 * @return
		 */
		private int interpCircleColor(int colors[], float unit) {
            if (unit <= 0) {
                return colors[0];
            }
            if (unit >= 1) {
                return colors[colors.length - 1];
            }
            
            float p = unit * (colors.length - 1);
            int i = (int)p;
            p -= i;

            // now p is just the fractional part [0...1) and i is the index
            int c0 = colors[i];
            int c1 = colors[i+1];
            int a = ave(Color.alpha(c0), Color.alpha(c1), p);
            int r = ave(Color.red(c0), Color.red(c1), p);
            int g = ave(Color.green(c0), Color.green(c1), p);
            int b = ave(Color.blue(c0), Color.blue(c1), p);
            
            return Color.argb(a, r, g, b);
        }
		
		/**
		 * 鑾峰彇娓愬彉鍧椾笂棰滆壊
		 * @param colors
		 * @param x
		 * @return
		 */
		private int interpRectColor(int colors[], float x) {
			int a, r, g, b, c0, c1;
        	float p;
        	if (x < 0) {
        		c0 = colors[0]; 
        		c1 = colors[1];
        		p = (x + rectRight) / rectRight;
        	} else {
        		c0 = colors[1];
        		c1 = colors[2];
        		p = x / rectRight;
        	}
        	a = ave(Color.alpha(c0), Color.alpha(c1), p);
        	r = ave(Color.red(c0), Color.red(c1), p);
        	g = ave(Color.green(c0), Color.green(c1), p);
        	b = ave(Color.blue(c0), Color.blue(c1), p);
        	return Color.argb(a, r, g, b);
		}
		
		private int ave(int s, int d, float p) {
            return s + Math.round(p * (d - s));
        }
    }
    
    /**
     * 鍥炶皟鎺ュ彛
     * @author <a href="clarkamx@gmail.com">LynK</a>
     * 
     * Create on 2012-1-6 涓婂崍8:21:05
     *
     */
    public interface OnColorChangedListener {
    	/**
    	 * 鍥炶皟鍑芥暟
    	 * @param color 閫変腑鐨勯鑹�
    	 */
        void colorChanged(int color);
    }
    
    public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public int getmInitialColor() {
		return mInitialColor;
	}

	public void setmInitialColor(int mInitialColor) {
		this.mInitialColor = mInitialColor;
	}

	public OnColorChangedListener getmListener() {
		return mListener;
	}

	public void setmListener(OnColorChangedListener mListener) {
		this.mListener = mListener;
	}
}
