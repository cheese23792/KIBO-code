package jp.jaxa.iss.kibo.rpc.sampleapk;

import android.content.Context;
import android.graphics.Bitmap;

import org.opencv.android.Utils;
import org.opencv.android.Utils.*;

import org.opencv.core.Mat;

public class TFDetector {

    private static TFLiteHelper helper = new TFLiteHelper();

    public static void analyzeImage(Context context,  Mat CVimage){

        // Implementing this on OpenCV
//         Image imageAnalyzer = ImageAnalysis.Builder()
//                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
//                .setTargetRotation(fragmentCameraBinding.viewFinder.display.rotation)
//                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
//                .setOutputImageFormat(OUTPUT_IMAGE_FORMAT_RGBA_8888)
//                .build()
//                .also {
//                it.setAnalyzer(cameraExecutor) { image ->
//                    if (!::bitmapBuffer.isInitialized) {
//                        bitmapBuffer = Bitmap.createBitmap(
//                                image.width,
//                                image.height,
//                                Bitmap.Config.ARGB_8888
//                        )
//                    }
//                    detectObjects(image)
//            }
//        }

        Bitmap bitmap = Bitmap.createBitmap(CVimage.width(), CVimage.height(), Bitmap.Config.RGB_565);
        Utils.matToBitmap(CVimage, bitmap);
        System.out.println("[Debug] Bitmap created");
        detectObjects(context, bitmap);
    }

    private static void detectObjects(Context context, Bitmap image) {
        // Copy out RGB bits to the shared bitmap buffer
        helper.detect(context, image, 0);
    }
}
