package jp.jaxa.iss.kibo.rpc.sampleapk;

import android.content.res.AssetManager;

import gov.nasa.arc.astrobee.Result;
import jp.jaxa.iss.kibo.rpc.api.KiboRpcService;

import gov.nasa.arc.astrobee.types.Point;
import gov.nasa.arc.astrobee.types.Quaternion;

import org.opencv.calib3d.Calib3d;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.dnn.Dnn;
import org.opencv.dnn.Net;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class YourService extends KiboRpcService{
    //setting up some required variables
    //maximum api retries
    final int LOOP_MAX = 5;

    //observed item name and quantity
    private String image1_name = "beaker";
    private String image2_name = "beaker";
    private String image3_name = "beaker";
    private String image4_name = "beaker";

    private int image1_value = 1;
    private int image2_value = 1;
    private int image3_value = 1;
    private int image4_value = 1;

    //observed wanted-item name
    private String imagewanted_name = "";

    //model
    public static final String MODEL_PATH = "frozen_graph.pb"; // SavedModel format, i.e., the '.pb' file
    public static final String WEIGHTS_PATH = "frozen_graph.pbtxt"; // .pbtxt file

    File tempFile;

    @Override
    protected void runPlan1(){
        try {
            // Get the AssetManager instance
            AssetManager assetManager = getAssets();

            // Open the file from assets as an InputStream
            InputStream inputStream = assetManager.open(MODEL_PATH);

            // Create a temporary file in the cache directory
            File cacheDir = getCacheDir();
            tempFile = File.createTempFile("temp", ".pb", cacheDir);

            // Write the contents of the InputStream to the temporary file
            FileOutputStream outputStream = new FileOutputStream(tempFile);
            byte[] buffer = new byte[1024];
            int length = 0;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            // Close the streams
            outputStream.flush();
            outputStream.close();
            inputStream.close();
        }
        catch (IOException e){
            e.printStackTrace();
        }

        api.startMission();

        runObserve();
        reportRoundDone();

        wantedObserve();
    }

    @Override
    protected void runPlan2(){
       // write your plan 2 here.
    }

    @Override
    protected void runPlan3(){
        // write your plan 3 here.
    }

    private void moveHandler(Point arvpoint, Quaternion arvquaternion){
        //retrying api.moveTo in case of failure
        Result result;
        result = api.moveTo(arvpoint, arvquaternion, false);

        int loopCounter = 0;
        while(!result.hasSucceeded() && loopCounter < LOOP_MAX){
            result = api.moveTo(arvpoint, arvquaternion, true);
            ++loopCounter;
        }
    }

    private Mat imageHandler(){
        //navcam image taken delay
        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        //retrying api.getMatNavCam in case of failure
        Mat image = api.getMatNavCam();
        int loopCounter = 0;
        while(image == null && loopCounter < LOOP_MAX){
            image = api.getMatNavCam();
            ++loopCounter;
        }
        //fixing fisheye effect
        image = unfisheye(image);
        return image;
    }

    private void locGoTo(int pos){
        //default position setup
        Point arvpoint;
        Quaternion arvquaternion;
        if (pos == 1){
            //Get to area 1
            arvpoint = new Point(10.95d, -9.9d, 5.3d);
            arvquaternion = new Quaternion(0f, 0f, 0.707f, -0.707f);

            moveHandler(arvpoint, arvquaternion);
        }
        else if (pos == 2){
            //Get to area 2
            arvpoint = new Point(11d, -8.7d, 4.4d);
            arvquaternion = new Quaternion(0f, 0.707f, 0f, 0.707f);

            moveHandler(arvpoint, arvquaternion);
        }
        else if (pos == 3){
            //Get to area 3
            arvpoint = new Point(11d, -7.9d, 4.4d);
            arvquaternion = new Quaternion(0f, 0.707f, 0f, 0.707f);

            moveHandler(arvpoint, arvquaternion);
        }
        else if (pos == 4){
            //Get to area 4
            arvpoint = new Point(10.65d, -6.8d, 5d);
            arvquaternion = new Quaternion(0f, 0f, -1f, 0f);

            moveHandler(arvpoint, arvquaternion);
        }
        else if (pos == 5){
            //Get to astronaut
            arvpoint = new Point(11.14d, -6.77d, 4.96d);
            arvquaternion = new Quaternion(0f, 0f, 0.707f, 0.707f);

            moveHandler(arvpoint, arvquaternion);
        }
    }

    private void runObserve(){
        //main code for observing each position (a bit scrubbed, will update when image processing is complete)
        locGoTo(1);
        Mat image1 = imageHandler();
        getImageData(image1, 1);

        Point arvpoint = new Point(11d, -8.65d, 4.6d);
        Quaternion arvquaternion = new Quaternion(0f, 0.573f, 0f, 0.819f);
        moveHandler(arvpoint, arvquaternion);

        locGoTo(2);
        Mat image2 = imageHandler();
        getImageData(image2, 2);

        locGoTo(3);
        Mat image3 = imageHandler();
        getImageData(image3, 3);

        arvpoint = new Point(10.85d, -7.9d, 4.4d);
        arvquaternion = new Quaternion(0f, 0.383f, 0f, 0.924f);
        moveHandler(arvpoint, arvquaternion);

        locGoTo(4);
        Mat image4 = imageHandler();
        getImageData(image4, 4);

        locGoTo(5);

        //saving image as png file (can delete)
        api.saveMatImage(image1, "image1.png");
        api.saveMatImage(image2, "image2.png");
        api.saveMatImage(image3, "image3.png");
        api.saveMatImage(image4, "image4.png");
    }

    private void reportRoundDone(){
        //main code for reporting to astronaut
        api.setAreaInfo(1, image1_name, image1_value);
        api.setAreaInfo(2, image2_name, image2_value);
        api.setAreaInfo(3, image3_name, image3_value);
        api.setAreaInfo(4, image4_name, image4_value);
        api.reportRoundingCompletion();
    }

    private void wantedObserve(){
        //main code for observing wanted item, then move to that item to report
        Mat imagewanted = imageHandler();
        getImageData(imagewanted, 5);

        if (imagewanted_name == image1_name){
            locGoTo(4);
        }
        else if (imagewanted_name == image2_name){
            locGoTo(4);
        }
        else if (imagewanted_name == image3_name){
            locGoTo(4);
        }
        else if (imagewanted_name == image4_name){
            locGoTo(4);
        }
        else{
            locGoTo(4);
        }
        //taking image for the chosen item (can delete)
        Mat imagechosen = imageHandler();
        //saving image as a png file (can delete)
        api.saveMatImage(imagewanted, "imagewanted.png");
        api.saveMatImage(imagechosen, "imagechosen.png");
        //notifying and taking snapshot of the item wanted by astronaut
        api.notifyRecognitionItem();
        api.takeTargetItemSnapshot();
    }

    private void getImageData(Mat image, int area){
        //main code for image processing
        //take in image and the area number, then change variable data (imageX_name, imageX_value)

        String[] LABEL_MAP = {"unlabeled", "beaker", "goggle", "hammer", "top", "kapton_tape", "pipette", "screwdriver", "thermometer", "watch", "wrench"};

        System.out.println("loading model");

        Net model = Dnn.readNetFromTensorflow(tempFile.getAbsolutePath());

        System.out.println("successfully loaded");

        Mat rgbImage = new Mat(image.size(), image.type());
        Imgproc.cvtColor(image, rgbImage, Imgproc.COLOR_GRAY2RGB);

        Mat blob = Dnn.blobFromImage(rgbImage, 1.0/127.5, new Size(512, 512), new Scalar(127.5, 127.5, 127.5), true, false);

        model.setInput(blob);
        Mat cvOut = model.forward();

        for (int i = 0; i < cvOut.size(2); i++) {
            Mat detection = cvOut.row(0).colRange(i, i + 1);
            double score = detection.get(0, 2)[0];
            if (score > 0.3) {
                String label = LABEL_MAP[(int) detection.get(0, 1)[0]];

                System.out.println(label);
                if (area == 1){
                    image1_name = label;
                }
                else if (area == 2){
                    image2_name = label;
                }
                else if (area == 3){
                    image3_name = label;
                }
                else if (area == 4){
                    image4_name = label;
                }
                else if (area == 5){
                    imagewanted_name = label;
                }
            }
        }
    }

    private Mat unfisheye(Mat image){
        //configuration for removing fisheye effect
        Mat cameraMatrix = new Mat(3, 3, CvType.CV_64F);
        cameraMatrix.put(0, 0, api.getNavCamIntrinsics()[0]);

        Mat cameraCoefficients = new Mat(1, 5, CvType.CV_64F);
        cameraCoefficients.put(0, 0, api.getNavCamIntrinsics()[1]);
        cameraCoefficients.convertTo(cameraCoefficients, CvType.CV_64F);

        Mat undistortImg = new Mat();
        Calib3d.undistort(image, undistortImg, cameraMatrix, cameraCoefficients);

        return undistortImg;
    }
}