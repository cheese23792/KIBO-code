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

    //setting recognition tool
    ImageRecogProcess recognitionTool = new ImageRecogProcess();

    @Override
    protected void runPlan1() {
        api.startMission();

        runObservationRound();
        runSpottingRound();
    }

    @Override
    protected void runPlan2() {
        // write your plan 2 here.
    }

    @Override
    protected void runPlan3() {
        // write your plan 3 here.
    }

    private void runObservationRound() {
        //main code for observing each position
        Mat image;
        locateDefaultPoint(1);
        image = imageHandler();
        recognitionTool.imageRecognition(image, 1);
        api.saveMatImage(image, "image1.png"); //can delete

        moveHandler(new Point(11d, -8.65d, 4.6d), new Quaternion(0f, 0.573f, 0f, 0.819f));

        locateDefaultPoint(2);
        image = imageHandler();
        recognitionTool.imageRecognition(image, 2);
        api.saveMatImage(image, "image2.png"); //can delete

        locateDefaultPoint(3);
        image = imageHandler();
        recognitionTool.imageRecognition(image, 3);
        api.saveMatImage(image, "image3.png"); //can delete

        moveHandler(new Point(10.85d, -7.9d, 4.4d), new Quaternion(0f, 0.383f, 0f, 0.924f));

        locateDefaultPoint(4);
        image = imageHandler();
        recognitionTool.imageRecognition(image, 4);
        api.saveMatImage(image, "image4.png"); //can delete

        locateDefaultPoint(5);

        api.setAreaInfo(1, recognitionTool.getName(1), recognitionTool.getCount(1));
        api.setAreaInfo(2, recognitionTool.getName(2), recognitionTool.getCount(2));
        api.setAreaInfo(3, recognitionTool.getName(3), recognitionTool.getCount(3));
        api.setAreaInfo(4, recognitionTool.getName(4), recognitionTool.getCount(4));

        api.reportRoundingCompletion();
    }

    private void runSpottingRound() {
        //main code for observing wanted item, then move to that item to report
        Mat image;
        image = imageHandler();
        recognitionTool.imageRecognition(image, 5);
        api.saveMatImage(image, "image5.png");

        if (recognitionTool.isArea(1)) {
            locateDefaultPoint(4);
        }
        else if (recognitionTool.isArea(2)) {
            locateDefaultPoint(4);
        }
        else if (recognitionTool.isArea(3)) {
            locateDefaultPoint(4);
        }
        else if (recognitionTool.isArea(4)) {
            locateDefaultPoint(4);
        }
        else {
            locateDefaultPoint(4);
        }

        //taking image for the chosen item (can delete)
        image = imageHandler();
        api.saveMatImage(image, "image6.png");

        //notifying and taking snapshot of the item wanted by astronaut
        api.notifyRecognitionItem();
        api.takeTargetItemSnapshot();
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
        image = clearFisheyeEffect(image);
        return image;
    }

    private void locateDefaultPoint(int pos) {
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

    //image methods
    private Mat clearFisheyeEffect(Mat image) {
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
