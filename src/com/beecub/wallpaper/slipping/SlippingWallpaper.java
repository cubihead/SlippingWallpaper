package com.beecub.wallpaper.slipping;

import java.util.ArrayList;
import java.util.Random;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.service.wallpaper.WallpaperService;
import android.view.SurfaceHolder;

public class SlippingWallpaper extends WallpaperService {
    public static final String SHARED_PREFS_NAME="SlippingWallpaperSettings";
    private final Handler mHandler = new Handler();

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public Engine onCreateEngine() {
        return new SlippingEngine();
    }

    class SlippingEngine extends Engine 
        implements SharedPreferences.OnSharedPreferenceChangeListener {

        private int mCanvasHeight = 1;
        private int mCanvasWidth = 1;
        
        private String mType = "1";
        private int mSpeed = 1;
        private int mRandomSpeed = 0;
        private float mSlipperLength = 0;
        private String mSlipperImage = "1";
        private String mBrightness = "0";
        private int mOrientation = 1;
        
        private Bitmap mBrightnessImage1;
        private Bitmap mBrightnessImage2;
        
        public ArrayList<Slipper> mSlipper = new ArrayList<Slipper>();
        
        private boolean mVisible;

        private SharedPreferences mPrefs;
        
        
        private final Runnable mDrawSlipper = new Runnable() {
            public void run() {
                drawFrame();
            }
        };
        
        
        SlippingEngine() {
            mPrefs = SlippingWallpaper.this.getSharedPreferences(SHARED_PREFS_NAME, 0);
            mPrefs.registerOnSharedPreferenceChangeListener(this);
            onSharedPreferenceChanged(mPrefs, null);
        }
        
        public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
            mType = prefs.getString("type", "1");
            mSpeed = Integer.valueOf(prefs.getString("speed", "1"));
            mRandomSpeed = Integer.valueOf(prefs.getString("randomspeed", "1"));
            mSlipperLength = Float.valueOf(prefs.getString("length", "0"));
            mSlipperImage = prefs.getString("color", "1");
            mOrientation = Integer.valueOf(prefs.getString("orientation", "1"));
            
            
            mBrightness = prefs.getString("brightness", "0");
            
            createSlipper(mSpeed, mRandomSpeed, mSlipperLength, mType, mSlipperImage);
        }

        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            mHandler.removeCallbacks(mDrawSlipper);
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            mVisible = visible;
            if (visible) {
                drawFrame();
            } else {
                mHandler.removeCallbacks(mDrawSlipper);
            }
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            super.onSurfaceChanged(holder, format, width, height);
            mCanvasWidth = width;
            mCanvasHeight = height;

            createSlipper(mSpeed, mRandomSpeed, mSlipperLength, mType, mSlipperImage);
            
            mBrightnessImage1  = BitmapFactory.decodeResource(getResources(),
                    R.drawable.overlay_darklight);
            mBrightnessImage1 = Bitmap.createScaledBitmap(
                    mBrightnessImage1, width, height, true);
            
            mBrightnessImage2  = BitmapFactory.decodeResource(getResources(),
                    R.drawable.overlay_dark);
            mBrightnessImage2 = Bitmap.createScaledBitmap(
                    mBrightnessImage2, width, height, true);
            
            drawFrame();
        }

        @Override
        public void onSurfaceCreated(SurfaceHolder holder) {
            super.onSurfaceCreated(holder);
        }

        @Override
        public void onSurfaceDestroyed(SurfaceHolder holder) {
            super.onSurfaceDestroyed(holder);
            mVisible = false;
            mHandler.removeCallbacks(mDrawSlipper);
        }

        @Override
        public void onOffsetsChanged(float xOffset, float yOffset,
                float xStep, float yStep, int xPixels, int yPixels) {
            drawFrame();
        }

        void drawFrame() {
            final SurfaceHolder holder = getSurfaceHolder();

            Canvas c = null;
            try {
                c = holder.lockCanvas();
                if (c != null) {
                    // draw something
                    onDraw(c);
                }
            } finally {
                if (c != null) holder.unlockCanvasAndPost(c);
            }
            
            // Reschedule the next redraw
            mHandler.removeCallbacks(mDrawSlipper);
            if (mVisible) {
                mHandler.postDelayed(mDrawSlipper, 1000 / 25);
            }
        }
        
        void onDraw(Canvas c) {
            c.save();
            
//            if(mOrientation == 3) {
//                c.rotate(90F, mCanvasHeight, mCanvasWidth);
//            } else if(mOrientation == 4) {
//                Log.v("beecub", "" + mCanvasWidth + " | " + mCanvasHeight);
//                c.rotate(-90F, mCanvasWidth, mCanvasHeight / 2F);
//            }
            
            c.drawColor(Color.BLACK);
            
            Slipper slipper;
            for(int i = 0; i < mSlipper.size(); i++) {
                slipper = mSlipper.get(i);
                slipper.mDrawable.setBounds(slipper.mLeft, slipper.mTop, slipper.mRight, slipper.mBottom);
                slipper.mDrawable.draw(c);
                
                if(mOrientation == 1) {
                    slipper.mLeft += slipper.mSpeed;
                    slipper.mRight += slipper.mSpeed;
                    
                    if(slipper.mLeft > mCanvasWidth) {
                        slipper.mLeft = 0 - slipper.mWidth;
                        slipper.mRight = 0;
                    }
                } else if(mOrientation == 2) {
                    slipper.mLeft -= slipper.mSpeed;
                    slipper.mRight -= slipper.mSpeed;
                    
                    if(slipper.mRight < 0) {
                        slipper.mLeft = mCanvasWidth;
                        slipper.mRight = mCanvasWidth + slipper.mWidth;
                    }
//                } else if(mOrientation == 4) {
//                    slipper.mLeft += slipper.mSpeed;
//                    slipper.mRight += slipper.mSpeed;
//                    
//                    if(slipper.mLeft > mCanvasHeight) {
//                        slipper.mLeft = 0 - slipper.mWidth;
//                        slipper.mRight = 0;
//                    }
                }
            }
            
            c.restore();
            
            if(mBrightness.equalsIgnoreCase("0")) {
                ;
            } else if(mBrightness.equalsIgnoreCase("darklight")) {
                c.drawBitmap(mBrightnessImage1, 0, 0 , null);
            } else if(mBrightness.equalsIgnoreCase("dark")) {
                c.drawBitmap(mBrightnessImage2, 0, 0 , null);
            }            
            
            c.restore();
        }
        
        void reset(Canvas c) {
            Paint paint = new Paint();
            paint.setXfermode(new PorterDuffXfermode(Mode.CLEAR));
            c.drawPaint(paint);
        }
        
        private void createSlipper(int speed, int randomspeed, float length, String stype, String color) {
            mSlipper.clear();
            
            Random generator = new Random();
            int random = 0;
            
            int next = 0;
            
            Drawable slipperImage;
            
            int sheight = getResources().getDrawable(R.drawable.slipper_1_1).getIntrinsicHeight();
            
            int amount = mCanvasHeight / sheight;
            
            for(int i = 1; i <= amount; i++) {
                
                // type
                String type;
                if(stype.equalsIgnoreCase("10")) {
                    int random4 = generator.nextInt(2) + 1;
                    type = String.valueOf(random4);
                } else {
                    type = stype;
                }
                
                // color
                if(!color.equalsIgnoreCase("10")) {
                    int resID = getResources().getIdentifier("slipper_" + type + "_" + color, "drawable", getPackageName());
                    slipperImage = getResources().getDrawable(resID);
                } else {
                    int random3 = generator.nextInt(5) + 1;
                    int resID = getResources().getIdentifier("slipper_" + type + "_" + String.valueOf(random3), "drawable", getPackageName());
                    slipperImage = getResources().getDrawable(resID);
                }
                
                // random speed                
                if(randomspeed > 0)
                    random = generator.nextInt(randomspeed) + 1;
                else
                    random = 0;
                
                // length
                int swidth = slipperImage.getIntrinsicWidth();
                if(length < 10)
                    swidth = (int) (swidth * length);
                else if(length == 10) {
                    int random2 = generator.nextInt(3) + 1;
                    if(random2 == 1)
                        swidth = (int) (swidth / 2);
                    else if(random2 == 2)
                        swidth = (int) (swidth * 1.5);
                    else if(random2 == 3)
                        ;
                }
                
                // create slipper
                Slipper slipper = new Slipper(mSpeed + random, 0 - swidth, next, 0, next + sheight, swidth, sheight, slipperImage);
                mSlipper.add(slipper);
                
                // next
                next += sheight;
            }
        }

    }
    
    public class Slipper {
        public int mSpeed;
        public int mLeft;
        public int mTop;
        public int mRight;
        public int mBottom;
        public int mWidth;
        public int mHeight;
        public Drawable mDrawable;
        
        public Slipper(int speed, int left, int top, int right, int bottom, int width, int height, Drawable drawable) {
            mSpeed = speed;
            mLeft = left;
            mTop = top;
            mRight = right;
            mBottom = bottom;
            mWidth = width;
            mHeight = height;
            mDrawable = drawable;
        }
    }
    
}
