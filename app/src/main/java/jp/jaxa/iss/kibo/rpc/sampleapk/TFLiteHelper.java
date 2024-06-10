package jp.jaxa.iss.kibo.rpc.sampleapk;

import android.content.Context;
import android.util.Log;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.SystemClock;
import android.util.Log;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.Rot90Op;
import org.tensorflow.lite.task.core.BaseOptions;
import org.tensorflow.lite.task.vision.detector.Detection;
import org.tensorflow.lite.task.vision.detector.ObjectDetector;

import java.io.IOException;
import java.util.List;


public class TFLiteHelper {
    private static final int DELEGATE_CPU = 0;
    private static final int DELEGATE_GPU = 1;
    private static final int DELEGATE_NNAPI = 2;

    private float threshold = 0.5f;
    private int numThreads = 2;
    private int maxResults = 3;
    private int currentDelegate = 0;
    private Context context;
    private static DetectorListener objectDetectorListener;

    // For this example this needs to be a var so it can be reset on changes. If the ObjectDetector
    // will not change, a lazy val would be preferable.
    private static ObjectDetector objectDetector = null;

//    public TFLiteHelper() {
//        setupObjectDetector();
//    }

    // Initialize the object detector using current settings on the
    // thread that is using it. CPU and NNAPI delegates can be used with detectors
    // that are created on the main thread and used on a background thread, but
    // the GPU delegate needs to be used on the thread that initialized the detector
    private void setupObjectDetector() {
        System.out.println("[Debug] Setting up option");
        // Create the base options for the detector using specifies max results and score threshold
        ObjectDetector.ObjectDetectorOptions.Builder optionsBuilder =
                ObjectDetector.ObjectDetectorOptions.builder()
                        .setScoreThreshold(threshold)
                        .setMaxResults(maxResults);

        // Set general detection options, including number of used threads
        BaseOptions.Builder baseOptionsBuilder = BaseOptions.builder().setNumThreads(numThreads);

        // Use the specified hardware for running the model. Default to CPU
        switch (currentDelegate) {
            case DELEGATE_CPU:
                // Default
                break;
            case DELEGATE_GPU:
                break;
            case DELEGATE_NNAPI:
                baseOptionsBuilder.useNnapi();
                break;
        }

        optionsBuilder.setBaseOptions(baseOptionsBuilder.build());
        String modelName = "model.tflite";

        try {
            System.out.println("[Debug] Loading model");
            objectDetector = ObjectDetector.createFromFileAndOptions(context, modelName, optionsBuilder.build());
            System.out.println("[Debug] Successfully loaded");
        } catch (IllegalStateException | IOException e) {
            objectDetectorListener.onError("Object detector failed to initialize. See error logs for details");
            Log.e("Test", "TFLite failed to load model with error: " + e.getMessage());
        }
    }

    public void detect(Context lcontext, Bitmap image, int imageRotation) {

        if (objectDetector == null){
            context = lcontext;
            System.out.println("[Debug] Initializing objectDetector");
            setupObjectDetector();
        }
        System.out.println("[Debug] Found objectDetector");

        // Inference time is the difference between the system time at the start and finish of the
        // process
        long inferenceTime = SystemClock.uptimeMillis();

        // Create preprocessor for the image.
        // See https://www.tensorflow.org/lite/inference_with_metadata/
        //            lite_support#imageprocessor_architecture
        ImageProcessor imageProcessor = new ImageProcessor.Builder()
                .add(new Rot90Op(-imageRotation / 90))
                .build();

        // Preprocess the image and convert it into a TensorImage for detection.
        System.out.println("[Debug] creating tensor image");
        TensorImage tensorImage = imageProcessor.process(TensorImage.fromBitmap(image));
        System.out.println("[Debug] Successfully created");

        System.out.println("[Debug] Detecting...");
        List<Detection> results = objectDetector.detect(tensorImage);
        System.out.println("[Debug] Success!");
        inferenceTime = SystemClock.uptimeMillis() - inferenceTime;

        System.out.println("/n=========================================================");
        for (int i = 0; i < results.size(); i++)
            System.out.println("[Detection] result: " + results.get(i));
        System.out.println("=========================================================");
        objectDetectorListener.onResults(
                results,
                inferenceTime,
                tensorImage.getHeight(),
                tensorImage.getWidth());
    }

    interface DetectorListener {
        void onError(String error);
        void onResults(
                List<Detection> results,
                long inferenceTime,
                int imageHeight,
                int imageWidth
        );
    }

//    private Bitmap createBox(){
//
//        return
//    }
}

