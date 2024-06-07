package jp.jaxa.iss.kibo.rpc.sampleapk;

import gov.nasa.arc.astrobee.Result;
import jp.jaxa.iss.kibo.rpc.api.KiboRpcService;

import gov.nasa.arc.astrobee.types.Point;
import gov.nasa.arc.astrobee.types.Quaternion;

import org.opencv.calib3d.Calib3d;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

public class CameraTest extends KiboRpcService{
    final int LOOP_MAX = 5;

    protected void runPlan1() {
        api.startMission();

        moveHandler(new Point(10.95d, -9.9d, 5.3d), new Quaternion(0f, 0f, 0.707f, -0.707f));

        Mat image = imageHandler();
        image = clearFisheyeEffect(image);
        api.saveMatImage(image, "image.png");

        //deal with image pls
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
        return image;
    }

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
