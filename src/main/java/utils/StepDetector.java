package utils;


import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

/**
 * Implementing the algorithm of counting
 * There are related algorithms online
 * The first step-by-step algorithm is calculated based on the X, Y, and Z accelerations of the phone.
 * I am not familiar with related algorithms
 * So use a piece of open source code to complete the algorithm for counting
 */
public class StepDetector implements SensorEventListener {

    public static int CURRENT_SETP = 0;
    public int walk = 0;
    public static float SENSITIVITY = 8; // SENSITIVITY靈敏度

    private float mLastValues[] = new float[3 * 2];
    private float mScale[] = new float[2];
    private float mYOffset;//displacement
    private static long mEnd = 0; //movement interval
    private static long mStart = 0;//motion start time
    private Context context;
/**
      * 最後加速度方向
      */
    private float mLastDirections[] = new float[3 * 2];//最後的方向
    private float mLastExtremes[][] = { new float[3 * 2], new float[3 * 2] };
    private float mLastDiff[] = new float[3 * 2];
    private int mLastMatch = -1;

/**
      * 傳入上下文的構造函數
      *
      * @param context
      */
            public StepDetector(Context context) {
        super();
        this.context = context;
        // 用於判斷是否計步的值
        int h = 480;
        mYOffset = h * 0.5f;
        mScale[0] = -(h * 0.5f * (1.0f / (SensorManager.STANDARD_GRAVITY * 2)));//重力加速度
        mScale[1] = -(h * 0.5f * (1.0f / (SensorManager.MAGNETIC_FIELD_EARTH_MAX)));//地球最大磁場
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor sensor = event.sensor;
        synchronized (this) {
            if (sensor.getType() == Sensor.TYPE_ORIENTATION) {
            } else {
                // 判斷傳感器的類型是否為重力傳感器(加速度傳感器)
                int j = (sensor.getType() == Sensor.TYPE_ACCELEROMETER) ? 1 : 0;
                if (j == 1) {
                    float vSum = 0;
                    // 獲取x軸、y軸、z軸的加速度
                    for (int i = 0; i < 3; i++) {
                        final float v = mYOffset + event.values[i] * mScale[j];
                        vSum += v;
                    }
                    int k = 0;
                    float v = vSum / 3;//獲取三軸加速度的平均值
                    // 判斷人是否處於行走中，主要從以下幾個方面判斷：
                    // 人如果走起來了，一般會連續多走幾步。因此，如果沒有連續4-5個波動，那麼就極大可能是乾擾。
                    // 人走動的波動，比坐車產生的波動要大，因此可以看波峰波谷的高度，只檢測高於某個高度的波峰波谷。
                    // 人的反射神經決定了人快速動的極限，怎麼都不可能兩步之間小於0.2秒，因此間隔小於0.2秒的波峰波谷直接跳過通過重力加速計感應，
                    // 重力變化的方向，大小。與正常走路或跑步時的重力變化比對，達到一定相似度時認為是在走路或跑步。實現起來很簡單，只要手機有重力感應器就能實現。
                    // 軟件記步數的精準度跟用戶的補償以及體重有關，也跟用戶設置的傳感器的靈敏度有關係，在設置頁面可以對相應的參數進行調節。一旦調節結束，可以重新開始。
                    float direction = (v > mLastValues[k] ? 1 : (v < mLastValues[k] ? -1 : 0));
                    if (direction == -mLastDirections[k]) {
                        int extType = (direction > 0 ? 0 : 1);
                        mLastExtremes[extType][k] = mLastValues[k];
                        float diff = Math.abs(mLastExtremes[extType][k] - mLastExtremes[1 - extType][k]);

                        if (diff > SENSITIVITY) {
                            boolean isAlmostAsLargeAsPrevious = diff > (mLastDiff[k] * 2 / 3);
                            boolean isPreviousLargeEnough = mLastDiff[k] > (diff / 3);
                            boolean isNotContra = (mLastMatch != 1 - extType);

                            if (isAlmostAsLargeAsPrevious && isPreviousLargeEnough && isNotContra) {
                                mEnd = System.currentTimeMillis();
                                // 通過判斷兩次運動間隔判斷是否走了一步
                                if (mEnd - mStart > 500) {
                                    CURRENT_SETP++;
                                    mLastMatch = extType;
                                    mStart = mEnd;
// Log.e("步數", CURRENT_SETP + "");                                }
                                } else {
                                    mLastMatch = -1;
                                }
                            }
                            mLastDiff[k] = diff;
                        }
                        mLastDirections[k] = direction;
                        mLastValues[k] = v;
                    }
                }
            }
        }
    }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

}
