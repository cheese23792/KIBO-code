package jp.jaxa.iss.kibo.rpc.sampleapk;

import gov.nasa.arc.astrobee.Result;
import jp.jaxa.iss.kibo.rpc.api.KiboRpcService;

import gov.nasa.arc.astrobee.types.Point;
import gov.nasa.arc.astrobee.types.Quaternion;

import org.opencv.calib3d.Calib3d;
import org.opencv.core.CvType;
import org.opencv.core.Mat;


public class YourService extends KiboRpcService {
    //setting up some required variables
    //maximum api retries
    final int LOOP_MAX = 5;

    //observed item name and quantity
    private String image1_name = "beaker";
    private String image2_name = "beaker";
    private String image3_name = "beaker";
    private String image4_name = "beaker";

    //observed wanted-item name
    private String imagewanted_name = "";

    //recog class
    ImageRecogProcess imageRecog = new ImageRecogProcess();

    @Override
    protected void runPlan1() {
        api.startMission();

        runObserve();
        runWanted();
    }

    @Override
    protected void runPlan2() {
        // write your plan 2 here.
    }

    @Override
    protected void runPlan3() {
        // write your plan 3 here.
    }

    private void moveHandler(Point arvpoint, Quaternion arvquaternion) {
        //retrying api.moveTo in case of failure
        Result result;
        result = api.moveTo(arvpoint, arvquaternion, false);

        int loopCounter = 0;
        while (!result.hasSucceeded() && loopCounter < LOOP_MAX) {
            result = api.moveTo(arvpoint, arvquaternion, true);
            ++loopCounter;
        }
    }

    private Mat imageHandler() {
        //navcam image taken delay
        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        //retrying api.getMatNavCam in case of failure
        Mat image = api.getMatNavCam();
        int loopCounter = 0;
        while (image == null && loopCounter < LOOP_MAX) {
            image = api.getMatNavCam();
            ++loopCounter;
        }
        //fixing fisheye effect
        image = unfisheye(image);
        return image;
    }

    private void locGoTo(int pos) {
        //default position setup
        Point arvpoint;
        Quaternion arvquaternion;
        if (pos == 1) {
            //Get to area 1
            arvpoint = new Point(10.95d, -9.9d, 5.3d);
            arvquaternion = new Quaternion(0f, 0f, 0.707f, -0.707f);

            moveHandler(arvpoint, arvquaternion);
        } else if (pos == 2) {
            //Get to area 2
            arvpoint = new Point(11d, -8.7d, 4.4d);
            arvquaternion = new Quaternion(0f, 0.707f, 0f, 0.707f);

            moveHandler(arvpoint, arvquaternion);
        } else if (pos == 3) {
            //Get to area 3
            arvpoint = new Point(11d, -7.9d, 4.4d);
            arvquaternion = new Quaternion(0f, 0.707f, 0f, 0.707f);

            moveHandler(arvpoint, arvquaternion);
        } else if (pos == 4) {
            //Get to area 4
            arvpoint = new Point(10.65d, -6.8d, 5d);
            arvquaternion = new Quaternion(0f, 0f, -1f, 0f);

            moveHandler(arvpoint, arvquaternion);
        } else if (pos == 5) {
            //Get to astronaut
            arvpoint = new Point(11.14d, -6.77d, 4.96d);
            arvquaternion = new Quaternion(0f, 0f, 0.707f, 0.707f);

            moveHandler(arvpoint, arvquaternion);
        }
    }

    private void runObserve() {
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

    private void runWanted() {
        //main code for observing wanted item, then move to that item to report
        api.reportRoundingCompletion();

        Mat imagewanted = imageHandler();
        getImageData(imagewanted, 5);

        if (imagewanted_name == image1_name) {
            locGoTo(4);
        } else if (imagewanted_name == image2_name) {
            locGoTo(4);
        } else if (imagewanted_name == image3_name) {
            locGoTo(4);
        } else if (imagewanted_name == image4_name) {
            locGoTo(4);
        } else {
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

    private void getImageData(Mat image, int area) {
        //main code for image processing
        if (area == 1) {
            image1_name = imageRecog.matchTemplate(image, 1);
        } else if (area == 2) {
            image2_name = imageRecog.matchTemplate(image, 2);
        } else if (area == 3) {
            image3_name = imageRecog.matchTemplate(image, 3);
        } else if (area == 4) {
            image4_name = imageRecog.matchTemplate(image, 4);
        } else if (area == 5) {
            imagewanted_name = imageRecog.matchTemplate(image, 5);
        }
    }

    private Mat unfisheye(Mat image) {
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