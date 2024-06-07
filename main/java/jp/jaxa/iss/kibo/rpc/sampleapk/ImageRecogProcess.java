package jp.jaxa.iss.kibo.rpc.sampleapk;

import org.opencv.core.Mat;

public class ImageRecogProcess{
    private String area1_name = "beaker";
    private String area2_name = "beaker";
    private String area3_name = "beaker";
    private String area4_name = "beaker";
    private String area5_name = "beaker";

    private int area1_count = 1;
    private int area2_count = 1;
    private int area3_count = 1;
    private int area4_count = 1;

    ImageRecogProcess(){

    }

    public void imageRecognition(Mat image, int area){
        //deal with image
        //after determining the image, change areaX_name and areaX_count
    }

    public String getName(int area){
        String name = "beaker";
        if (area == 1){
            name = area1_name;
        }
        else if (area == 2){
            name = area2_name;
        }
        else if (area == 3){
            name = area3_name;
        }
        else if (area == 4){
            name = area4_name;
        }
        else if (area == 5){
            name = area5_name;
        }
        return name;
    }

    public int getCount(int area){
        int count = 1;
        if (area == 1){
            count = area1_count;
        }
        else if (area == 2){
            count = area2_count;
        }
        else if (area == 3){
            count = area3_count;
        }
        else if (area == 4){
            count = area4_count;
        }
        return count;
    }

    public boolean isArea(int area){
        if (area == 1 && area1_name == area5_name){
            return true;
        }
        else if (area == 2 && area2_name == area5_name){
            return true;
        }
        else if (area == 3 && area3_name == area5_name){
            return true;
        }
        else if (area == 4 && area4_name == area5_name){
            return true;
        }
        return false;
    }
}