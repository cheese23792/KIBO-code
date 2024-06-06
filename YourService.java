package jp.jaxa.iss.kibo.rpc.sampleapk;

import gov.nasa.arc.astrobee.Result;
import jp.jaxa.iss.kibo.rpc.api.KiboRpcService;

import gov.nasa.arc.astrobee.types.Point;
import gov.nasa.arc.astrobee.types.Quaternion;

import org.opencv.calib3d.Calib3d;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import org.opencv.dnn.Dnn;
import org.opencv.dnn.Net;

public class YourService extends KiboRpcService{
    //setting up some required variables
    //maximum api retries
    final int LOOP_MAX = 5;

    //observed item name and quantity
    final String image1_name = "beaker";
    final String image2_name = "beaker";
    final String image3_name = "beaker";
    final String image4_name = "beaker";

    final int image1_value = 1;
    final int image2_value = 1;
    final int image3_value = 1;
    final int image4_value = 1;

    //observed wanted-item name
    final String imagewanted_name = "";

    //model
    //public static final String MODEL_PATH = "C:\\Users\\Cheese\\Downloads\\KIBO\\5thKibo-RPC_SampleAPK\\SampleApk\\app\\src\\main\\opencvmodel\\frozen_graph.pb"; // SavedModel format, i.e., the '.pb' file
    //public static final String WEIGHTS_PATH = "C:\\Users\\Cheese\\Downloads\\KIBO\\5thKibo-RPC_SampleAPK\\SampleApk\\app\\src\\main\\opencvmodel\\frozen_graph.pbtxt"; // .pbtxt file

    @Override
    protected void runPlan1(){
        //Net model = Dnn.readNet(MODEL_PATH, WEIGHTS_PATH);
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
        /*
        getImageData(image1, 1);
        */
        locGoTo(1);

        Point arvpoint = new Point(10.95d, -8.6d, 4.6d);
        Quaternion arvquaternion = new Quaternion(0f, 0.707f, 0f, 0.707f);
        moveHandler(arvpoint, arvquaternion);

        locGoTo(2);
        Mat image2 = imageHandler();
        /*
        getImageData(image2, 2);
        */
        locGoTo(2);

        locGoTo(3);
        Mat image3 = imageHandler();
        /*
        getImageData(image3, 3);
        */
        locGoTo(3);

        arvpoint = new Point(10.85d, -7.9d, 4.4d);
        arvquaternion = new Quaternion(0f, 0.707f, 0f, 0.707f);
        moveHandler(arvpoint, arvquaternion);

        locGoTo(4);
        Mat image4 = imageHandler();
        /*
        getImageData(image4, 4);
        */
        locGoTo(4);

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
        /*
        getImageData(imagewanted, 5);
        */
        if (imagewanted_name == image1_name){

        }
        else if (imagewanted_name == image2_name){

        }
        else if (imagewanted_name == image3_name){

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