package com.yjsoft.mlkit;

import static org.opencv.core.Core.meanStdDev;
import static org.opencv.core.CvType.CV_64F;
import static org.opencv.imgproc.Imgproc.COLOR_BGR2GRAY;
import static org.opencv.imgproc.Imgproc.Laplacian;
import static org.opencv.imgproc.Imgproc.blur;
import static org.opencv.imgproc.Imgproc.cvtColor;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.Image;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;

public class OpenCvUtil {
    // 模糊度阈值
    private static final int BLUR_THRESHOLD500 = 500;
    private static final int BLUR_THRESHOLD300 = 300;
    private static final int BLUR_THRESHOLD50 = 50;
    private static final int BLUR_THRESHOLD15 = 15;

    /**
     * Mat 转Bitmap
     *
     * @param mat
     * @return
     */
    public static Bitmap matToBitmap(Mat mat) {
        Bitmap result = null;
        if (mat != null) {
            result = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.RGB_565);
            if (result != null) {
                Utils.matToBitmap(mat, result);
            }
        }
        return result;
    }

    /**
     * Bitmap 转 Mat
     *
     * @param bitmap
     * @return Mat
     */
    public static Mat bitmapToMat(Bitmap bitmap) {
        Mat result = null;
        Bitmap bmp32 = bitmap.copy(Bitmap.Config.RGB_565, true);
        result = new Mat(bitmap.getHeight(), bitmap.getWidth(), CvType.CV_8UC2, new Scalar(0));
        Utils.bitmapToMat(bmp32, result);
        return result;
    }

    /**
     * 计算图像标准差
     *
     * @param bitmap
     * @return
     */
    public static double getStandardDeviation(Bitmap bitmap) {
        double result = 0.0;
        if (bitmap != null) {
            result = getStdDev(bitmap);
        }
        return result;
    }

    /**
     * 计算图像方差
     *
     * @param bitmap
     * @return
     */
    public static double getSquareDeviation(Bitmap bitmap) {
        double result = 0.0;
        if (bitmap != null) {
            result = getStdDev(bitmap);
        }
        return result * result;
    }

    /**
     * 输入标准差，判断图像是否清晰
     *
     * @param st
     * @param tv
     * @param context
     */
    public static void judgeBlurByStdDev(final double st, final TextView tv, final Context context) {
        judgeBlurBySquDev(st * st, null);
    }

    /**
     * 通过方差判断
     *
     * @param sq
     */
    public static void judgeBlurBySquDev(final double sq, String text) {
        StringBuilder sb = new StringBuilder(" ");
        double tempSt = sq;
        // 标准差 -> 方差
//                double tempSt = st * st;
        sb.append(tempSt).append("\t");
        // 颜色可以自行设置
        if (tempSt > BLUR_THRESHOLD500) {
            sb.append("清晰");
        } else if (tempSt > BLUR_THRESHOLD300) {
            sb.append("不清晰");
        } else if (tempSt > BLUR_THRESHOLD50) {
            sb.append("很不清晰 ");
        } else if (tempSt > BLUR_THRESHOLD15) {
            sb.append("非常不清晰 ");
        } else {
            sb.append("完全看不清了 ");
        }
        if (!TextUtils.isEmpty(text)) {
            sb.append("__" + text);
        }
//        Log.e("扫描梯度处理", sb.toString());
    }

    /**
     * 获取模糊位图
     *
     * @param srcBitmap
     * @return
     */
    public static Bitmap getBlurBitmap(final Bitmap srcBitmap) {
        Mat srcImage = bitmapToMat(srcBitmap);
        Mat blurImage = new Mat();
        blur(srcImage, blurImage, new Size(3, 3));
        return matToBitmap(blurImage);
    }

    // region tool

    /**
     * 获取标准差
     *
     * @param bitmap
     * @return double 标准差
     */
    private static double getStdDev(Bitmap bitmap) {
        Mat matSrc = bitmapToMat(bitmap);
        Mat mat = new Mat();
        int channel = matSrc.channels();
        //  1表示图像是灰度图
        if (channel != 1) {
            cvtColor(matSrc, mat, COLOR_BGR2GRAY);
        } else {
            mat = matSrc;
        }
        Mat lap = new Mat();
        Laplacian(mat, lap, CV_64F);
        MatOfDouble s = new MatOfDouble();
        meanStdDev(lap, new MatOfDouble(), s);
        double st = s.get(0, 0)[0];
//        Log.d(TAG, "getStdDev: s.get(0,0)[0] = "+s.get(0,0)[0]);
        judgeBlurBySquDev(st * st,null);
        return st;
    }

    // endregion
}

