# KIBO-code
Updates  
v. 0.1 : Code uploaded to github  
v. 0.2 : Added ImageRecogProcess.java as a class that handles image processing  
v. 0.3 : Encounter an error (**java.lang.OutOfMemoryError: Failed to allocate a 24613612 byte allocation with 8384608 free bytes and 16MB until OOM**)  
         **Main log is in error.log (line 2381)**  
         **Requires handling**  



***To run plan 1 without using image recognition class (Avoiding error and test movement), comment out all imageX_name = imageRecog.matchTemplate(image, X); in the method : private void getImageData(Mat image, int area) in YourService.java***
