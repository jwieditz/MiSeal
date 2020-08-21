

# Minutiae Separating Algorithm (MiSeal)

This project includes 

 - the MiSeal R package including the algorithms *MiSeal* and *PostSeal* for separating a superposition of a homogeneous Poisson process and an inhomogeneous Strauss-hard core process.	 
 - a graphical and a command line interface for investigating a selected number of a fingerprint's features such as 
	 - its orientation field (OF), 
	 - its ridge frequency (RF),
	 - the field and ridge divergence,
	 - the intensity of necessary minutiae within patches


If you are interested in 

 - the MiSeal R-package, go to [MiSeal](#miseal),
 - the graphical tool, go to [graphical fingerprint tool](#graphical-fingerprint-tool),
 - the command line tool, go to [command line fingerprint tool](#command-line-fingerprint-tool).

This repository is supplementary to <cite>Wieditz, J., Pokern, Y., Schuhmacher, D., Huckemann, S. (2020+). Characteristic and Necessary Minutiae in Fingerprints.</cite>

| ![ScreenshotGUI](https://github.com/jwieditz/MiSeal/blob/master/GUI/GUI_screenshot.png) | 
|:--:| 
| *Screenshot of the [graphical fingerprint tool](#graphical-fingerprint-tool) applied to the provided example.* |

## Acknowledgements
The image provided as an example was taken from <cite>Maio, D., Maltoni, D., Cappelli, R., Wayman, J. L., & Jain, A. K. (2002, August). FVC2002: Second fingerprint verification competition. In Object recognition supported by user interaction for service robots (Vol. 3, pp. 811-814). IEEE.</cite>
  
# MiSeal

1. Install the R-package MiSeal via
	`library(remotes)`
	`install_github('jwieditz/MiSeal/MiSeal')`.
2. Load the library via `library(MiSeal)`.
3. For a fingerprint application, run `example(MiSeal)`.
4. For a general point process application, run `example(PostSeal)`.

We provide an already correctly formatted example data set in the example folder of this repository as well as in the R-packages' data. The simulations were made using the latest R version 4.0.2 (2020-06-22) -- "Taking Off Again". Note that for the execution we additionally require Java 11. For more information, see the R documentation.


# Graphical fingerprint tool

 1. Unzip the jmiseal.zip archive from the GUI folder.
 2. Go to the /jmiseal/bin folder.
 3. Depending on your OS run the following file:
	 - ## Linux
		 - JMiSeal
	 - ## Windows
		 - JMiSeal.bat
	 - ## macOS
		 - currently not available




  

# Command line fingerprint tool

For usage of the command line tool, you can either use the precompiled fingerprint.jar from the commandLineTool folder via

> `java -jar fingerprint.jar [OPTIONS] fingerprint_image.png`

from the repository or compile the uploaded java code. All setting options are stated below.
  
**Table of contents**
- [Arguments](#arguments)
  * [Main program arguments](#main-program-arguments)
  * [Normalization arguments](#normalization-arguments)
  * [Orientation estimation arguments](#orientation-estimation-arguments)
    + [Arguments for the default estimation method](#arguments-for-the-default-estimation-method)
    + [Arguments for the HWJ estimation method](#arguments-for-the-hwj-estimation-method)
  * [Ridge frequency estimation arguments](#ridge-frequency-estimation-arguments)
    + [Arguments for the default ridge frequency estimation method](#arguments-for-the-default-ridge-frequency-estimation-method)
    + [Arguments for the curved region ridge frequency estimation method](#arguments-for-the-curved-region-ridge-frequency-estimation-method)
  * [Divergence estimation arguments](#divergence-estimation-arguments)
    + [Arguments for the default divergence estimator](#arguments-for-the-default-divergence-estimator)
    + [Arguments for the curved region divergence estimator](#arguments-for-the-curved-region-divergence-estimator)
  * [Intensity estimation arguments](#intensity-estimation-arguments)
- [Interpolation](#interpolation)
  * [Bilinear](#bilinear)
  * [Nearest neighbour](#nearest-neighbour)
  * [Gaussian](#gaussian)
- [Smoothing](#smoothing)
  * [1D-Smoothing](#1d-smoothing)
    + [Gaussian](#gaussian)
  * [2D-Smoothing](#2d-smoothing)
    + [Average](#average)
    + [Gaussian](#gaussian-1)
- [Examples](#examples)


# Arguments

## Main program arguments

| name | alternative names | description | default value | comment | example |
| --- | --- | --- | --- | --- | --- |
| -m | --minutiae | The input file for the minutiae | *empty* | The file must be in the following format: <pre><code>imageHeight<br>imageWidth<br>dpi<br>numberOfMinutiae<br>x y orientation<br>...<br>x y orientation</code></pre> | `-m minutiae.txt` |
| -roi | --region-of-interest | The input file for the region of interest | *empty* | The file must be a CSV with number of columns equal to the image width and number of rows equal to the image height. Each cell contains either `true`/`1` if the pixel at this position in the image belongs to the region of interest and `false`/`0` otherwise. | `-roi roi.csv` |
| -oi | --orientation-input | The input file for the orientation | *empty* | The must must be a PNG image with grey values where each pixel's value is equal to the orientation at that pixel in degrees (0 - 180). Note that when you supply an orientation image, the `--skip-orientation` flag is implicitly set. | `-oi orientation.png` |
| -h | --help | Display the help and exit | | | `-h`|
| --nogui | | Disable plotting the images | | When this flag is set, then no images will be plotted. This can be useful if you want to run the program for a batch of images. | `--nogui` |
| -pw | --patch-width | The patch width | | Must be a positive integer. Specify either *patch width* and *patch height* or *number of horizontal patches* and *number of vertical patches*. Patch width and height is given precedence over number of horizontal and vertical patches. If no correct values are given, the number of horizontal and vertical patches is set to `10`. | `-pw 16` |
| -ph | --patch-height | The patch height | | See comments for `-pw`. | `-ph 16` |
| -pnh | --patches-horizontal | The number of horizontal patches | | See comments for `-pw`. | `-pnh 10` |
| -pnv | --patches-vertical | The number of vertical patches | | See comments for `-pw`. | `-pnv 10` |
| --skip-orientation | | Skip the orientation of the estimation field | | When this flag is set, the orientation won't be estimated by the program. Note that when you pass an orientation image with `-oi`, then `--skip-orientation` is implicitly set. | `--skip-orientation` |
| --skip-ridge-frequency | | Skip the estimation of the ridge frequency | | When this flag is set, the ridge frequency won't be estimated. | `--skip-ridge-frequency` |
| --skip-divergence | | Skip the estimation of the divergence | | When this flag is set, the divergence field won't be estimated. | `--skip-divergence` |
| --skip-intensity | | Skip the estimation of the intensity | | When this flag is set, the intensity of the fingerprint won't be estimated. | `--skip-intensity` |
| --skip-line-divergence | | Skip the calculation of the line divergence | | When this flag is set, the line divergence of the fingerprint won't be calculated. | `--skip-line-divergence` |
| *arguments for the normalization* | | | | See below for a full list of arguments which you can pass for the normalization step. | |
| *arguments for the orientation estimation* | | | | See below for a full list of arguments which you can pass for the orientation field estimation step. | |
| *arguments for the ridge frequency estimation* | | | | See below for a full list of arguments which you can pass for the ridge frequency estimation step. | |
| *arguments for the divergence estimation* | | | | See below for a full list of arguments which you can pass for the divergence estimation step. | |
| *arguments for the intensity estimation* | | | | See below for a full list of arguments which you can pass for the divergence estimation step. | |
---

## Normalization arguments

| name | alternative names | description | default value | comment | example |
| --- | --- | --- | --- | --- | --- |
| -nm | --normalize-mean | Normalize the image such that it has the given mean | *empty* | This value must be strictly greater than 0. If it is omitted but a normalization variance is specified, the image will be normalized such that only the variance changes and the mean remains. | `-nm 100` |
| -nv | --normalize-variance | Normalize the image such that it has the given variance | *empty* | This value must be strictly greater than 0. If it is omitted but a normalization mean is specified, the image will be normalized such that only the mean changes and the variance remains. | `-nv 100` |

---

## Orientation estimation arguments

| name | alternative names | description | default value | comment | example |
| --- | --- | --- | --- | --- | --- |
| -o | --orientation | Choose the orientation estimation method | default | Possible values are: <ol><li>*`default`: The default orientation estimator which is based on the image gradient method.<li>`hwj`: This method uses the estimation method proposed in "Fingerprint Image Enhancement: Algorithm and Performance Evaluation" by Hong et al.</ol> | `-o hwj` |
| -oo | --orientation-output | The output file for the image orientation (.csv) | *empty* | The orientation field will be written as a CSV file where each entry represents the orientation in radians at position (column, row). | `-oo output/orientation.csv` |
| -Odefault | | Dynamic parameters for the default orientation estimation method | *empty* | If you have chosen `default` as the orientation method, you can use this parameter to pass additional arguments to the default orientation estimation. See below for a full list.| `-OdefaultSomeValue=123` |
| -Ohwj | | Dynamic parameters for the HWJ orientation estimation method | *empty* | If you have chosen `hwj` as the orientation estimation method, you can use this parameter to pass additional arguments to the HWJ orientation estimator. See below for a full list. | `-OhwjSomeValue=123` |

### Arguments for the default estimation method

You can pass additional arguments to the default orientation estimation method by appending `<name>=` to `-Odefault`, followed directly by the assigned value (no whitespace). Example: `-OdefaultSigmaQ=25`.

| name  | description | default value | example |
| --- | --- | --- | --- |
| SigmaQ | The sigma_q parameter. | 25 | `-OdefaultSigmaQ=5` |
| Iter | The number of iterations. | 1 | `-OdefaultIter=3` |
| Unwrap | The unwrapping type. One of *lines*, *spirals* or *diamonds*. Leave empty to disable unwrapping. | *empty* | `-OdefaultUnwrap=lines` |

### Arguments for the HWJ estimation method

You can pass additional arguments for the HWJ orientation estimation method by appending `<name>=` to `-Ohwj`, followed directly by the assigned value (no whitespace). Example: `-OhwjLowPassFilter=true`.

| name  | description | default value | example |
| --- | --- | --- | --- |
| LowPassFilter | Set this to `true` if you want to apply a low pass filter at the end of the estimation. | false | `-OhwjLowPassFilter=true` |

---

## Ridge frequency estimation arguments

| name | alternative names | description | default value | comment | example |
| --- | --- | --- | --- | --- | --- |
| -r | --ridgefrequency | Choose the ridge frequency estimation method | default | Possible values are: <ol><li>`default`: The default ridge frequency estimator is based on a method proposed in "Fingerprint Image Enhancement: Algorithm and Performance Evaluation" by Hong et al.<li>`cr`: The curved region estimation method is based on the method proposed in "Curved-Region-Based Ridge Frequency Estimation and Curved Gabor Filters for Fingerprint Image Enhancements" by C. Gottschlich.</ol> | `-r cr` |
| -ro | --ridgefrequency-output | The output file for the ridge frequency (.csv) | *empty* | The ridge frequency will be written as a CSV file where each entry represents the ridge frequency at (column, row). | `-ro output/ridgefrequency.csv` |
| -Rdefault | | Dynamic parameters for the default ridge frequency estimation method | *empty* | If you have have chosen `default` as the ridge frequency estimation method, you can use this parameter to pass additional arguments to the default ridge frequency estimator. See below for a full list. | `-RdefaultSomeValue=123` |
| -Rcr | | Dynamic parameters for the curved region ridge frequency estimation method | *empty* | If you have chosen `cr` as the ridge frequency estimation method, you can use this parameter to pass additional arguments to the curved region ridge frequency estimator. See below for a full list. | `-RcrSomeValue=123` |

### Arguments for the default ridge frequency estimation method

You can pass additional arguments to the default ridge frequency estimation method by appending `<name>=` to `-Rdefault`, followed directly by the assigned value (no whitespace). Example: `-RdefaultWindowWidth=25`.

| name  | description | default value | example |
| --- | --- | --- | --- |
| WindowWidth | Specifies the width of the scanning window. Must be odd. | 17 | `-RdefaultWindowWidth=17` |
| OrientationWindowWidth | Specifies the width of the scanning window for the orientation. Must be odd. | 33 | `-RdefaultOrientationWindowWidth=33` |
| Interpolation | The interpolation which should be used to interpolate missing values. See section *Interpolation* for a list of possible values. | gauss | `-RdefaultInterpolation=gauss` |
| Smoothing | The smoothing algorithm which should be used to smooth the ridge frequency matrix. See section *Smoothing* for a list of possible values. | gauss | `-RdefaultSmoothing=gauss` |

### Arguments for the curved region ridge frequency estimation method

You can pass additional arguments to the curved region ridge frequency estimation method by appending `<name>=` to `-Rcr`, followed directly by the assigned value (no whitespace). Example: `-RcrP=16`.

| name  | description | default value | example |
| --- | --- | --- | --- |
| P | The number of vertical points in the curved region. | 16 | `-RcrP=16` |
| Q | The number of horizontal points in the curved region. | 32 | `-RcrQ=32` |
| GreyInterp | The interpolation which should be used to interpolate the grey values. See section *Interpolation* for a list of possible values. | nn | `-RcrGreyInterp=nn` |
| OrientationInterp | The interpolation which should be used to interpolate the orientation. See section *Interpolation* for a list of possible values. | nn | `-RcrORientationInterp=nn` |
| FinalSmooth | The smoothing algorithm which should be used to smooth the final ridge frequency matrix. See section *Smoothing* for a list of possible values. | avg | `-RcrFinalSmooth=avg` |
| ProfileSmooth | The smoothing method which should be used to smooth the grey level profile. See section *Smoothing* for a list of possible values. | gauss | `-RcrProfileSmooth=gauss` |
| ProfileSmoothIterations | Specifies how often the profile smoothing can be repeated. | 3 | `-RcrProfileSmoothIterations=3` |

---

## Divergence estimation arguments

| name | alternative names | description | default value | comment | example |
| --- | --- | --- | --- | --- | --- |
| -d | --divergence | Choose the divergence estimation method | default | Possible values are: <ol><li>`default`: The default divergence estimation method is based on image gradients.<li>`cr`: The curved region divergence estimation method uses curved regions and calculates the divergence as the absolute difference between the length of the right and the left side of the region.</ol> | `-d cr` |
| -do | --divergence-output | The output file for the divergence (.csv) | *empty* | The divergence will be written as a CSV file where each entry represents the divergence at (column, row). | `-do output/divergence.csv` |
| -Ddefault | | Dynamic parameters for the default divergence estimation method | *empty* |  If you have chosen `default` as the divergence estimation method, you can use this parameter to pass additional arguments to the default divergence estimator. | `-DdefaultSomeValue=123` |
| -Dcr | | Dynamic parameters for the curved region divergence estimation method | *empty* | If you have chosen `cr` as the divergence estimation method, you can use this parameter to pass additional arguments to the curved region divergence estimator. | `-DcrSomeValue=123` |

### Arguments for the default divergence estimator

You can pass additional arguments to the default divergence estimation method by appending `<name>=` to `-Ddefault`, followed directly by the assigned value (no whitespace). Example: `-DdefaultSmoothing=gauss`.

| name  | description | default value | example |
| --- | --- | --- | --- |
| Smoothing | The smoothing algorithm which should be used to smooth the divergence matrix. See section *Smoothing* for a list of possible values. | gauss | `-DdefaultSmoothing=gauss` |

### Arguments for the curved region divergence estimator

You can pass additional arguments to the curved region divergence estimation method by appending `<name>=` to `-Dcr`, followed directly by the assigned value (no whitespace). Example: `-DcrSmoothing=gauss`.

| name  | description | default value | example |
| --- | --- | --- | --- |
| P | The number of vertical points in the curved region. | 16 | `-DcrP=16` |
| Q | The number of horizontal points in the curved region. | 32 | `-DcrQ=32` |
| OrientationInterp | The interpolation which should be used to interpolate the orientation. See section *Interpolation* for a list of possible values. | nn | `-DcrOrientationInterp=nn` |
| Smoothing | The smoothing which should be used to smooth the divergence matrix. See section *Smoothing* for a list of possible values. | gauss | `-DcrSmoothing=gauss` |
| UseRealDistance | Whether to use the real length of each side (i.e. the sum of the distances between two consecutive points) instead of the overall length (i.e. the distance between the first and last point on a side). | true | `-DcrUseRealDistance=true` |
| RemoveLineDivergence | Whether to remove the line divergence (the average interridge distance along the points at one side) by dividing the length by this value. | true | `-DcrRemoveLineDivergence=true` |

---

## Intensity estimation arguments

| name | alternative names | description | default value | comment | example |
| --- | --- | --- | --- | --- | --- |
| -i | --intensity | Choose the intensity estimation method | default | Currently only the `default` method is available. This calculates the intensity as `intensity[x][y] = ridge_frequency[x][y] * divergence[x][y]`. | `-i default` |
| -io | --intensity-output | The output file for the intensity (.csv) | *empty* | The intensity will be written as a CSV file where each entry represents the intensity at (column, row). | `-io output/intensity.csv` |

---

## Line divergence calculation arguments

| name | alternative names | description | default value | comment | example |
| --- | --- | --- | --- | --- | --- |
| -lo | --line-divergence-output | The output file for the line divergence (.csv) | *empty* | The line divergence will be written as a CSV file where each entry represents the line divergence at (column, row). | `-lo output/linedivergence.csv` |
| -L | | Dynamic parameters for the line divergence calculation | *empty* | You can use this parameter to pass additional arguments to the line divergence calculator. | `-LSomeValue=123` |

### Dynamic arguments for the line divergence calculation

You can pass additional arguments to the line divergence calculator by appending `<name>=` to `-L`, followed directly by the assigned value (no whitespace). Example: `-LSmoothing=gauss`.

| name  | description | default value | example |
| --- | --- | --- | --- |
| Smoothing | The smoothing algorithm which should be used to smooth the line convergence matrix. See section *Smoothing* for a list of possible values. | gauss | `-LSmoothing=gauss` |

---

# Interpolation

You can choose from different interpolation methods. Some methods have parameters which can be used to further specify the interpolation method. These parameters are directly appended to the dynamic argument of the interpolation method.  
**Examples:**
* `-DcrOrientationInterp=nn`
* `-RcrGreyInterp=gauss -RcrGreyInterpMean=0 -RcrGreyInterpVariance=9 -RcrGreyInterpSize=5`

## Bilinear

**Name:** `bilinear`  
**Arguments:** The bilinear interpolation has no further arguments.  
**Example:** `-RcrGreyInterp=bilinear`

## Nearest neighbour

**Name:** `nn`  
**Arguments:** The nearest neighbour interpolation has no further arguments.  
**Example:** `-RcrGreyInterp=nn`

## Gaussian

**Name:** `gauss`  
**Comment:** This method is taken from "Fingerprint Image Enhancement: Algorithm and Performance Evaluation" by Hong et al.  
**Arguments:**

| name  | description | default value | example |
| --- | --- | --- | --- |
| Mean | The mean of the gaussian distribution. | 0 | `-RcrGreyInterpMean=1` |
| Variance | The variance of the gaussian distribution. | 1 | `-RcrGreyInterpVariance=9` |
| Width | The width of the window. | 1 | `-RcrGreyInterpWidth=10` |
| Size | The size of the gaussian kernel. Must be odd. | 1 | `-RcrGreyInterpSize=15` |

**Example:** `-RcrGreyInterp=gauss -RcrGreyInterpMean=1 -RcrGreyInterpVariance=9 -RcrGreyInterpWidth=10 -RcrGreyInterpSize=15`

---

# Smoothing

## 1D-Smoothing

There is only one one-dimensional smoothing algorithm. You can pass additional arguments by directly appending them to the dynamic argument of the smoothing algorithm.  
**Examples:**
* `-RcrProfileSmooth=gauss`
* `-RcrProfileSmooth=gauss -RcrProfileSmoothMean=0 -RcrProfileSmoothVariance=9`

### Gaussian

**Name:** `gauss`  
**Arguments:**

| name  | description | default value | example |
| --- | --- | --- | --- |
| Size | The size of the kernel. Must be odd. | 1 | `-RcrProfileSmoothSize=7` |
| Mean | The mean of the gaussian distribution. | 0 | `-RcrProfileSmoothMean=1` |
| Variance | The variance of the gaussian distribution. | 1 | `-RcrProfileSmoothVariance=9` |

**Example:** `-RcrProfileSmooth=gauss -RcrProfileSmoothSize=7 -RcrProfileSmoothMean=0 -RcrProfileSmoothVariance=9`

---

## 2D-Smoothing

You can choose from different two-dimensional smoothing algorithms. Some of them have parameters which can be used to further specify the smoothing method. These parameters are directly appended to the dynamic argument of the smoothing method.  
**Examples:**
* `-DdefaultSmoothing=avg`
* `-DcrSmoothing=gauss -DcrSmoothingSize=7 -DcrSmoothingMean=1 -DcrSmoothingVariance=9`

### Average

**Name:** `avg`
**Arguments:**

| name  | description | default value | example |
| --- | --- | --- | --- |
| Size | The size of the averaging window. Must be odd. | 1 | `-DdefaultSmoothingSize=11` |

**Example:** `-DdefaultSmoothing=avg -DdefaultSmoothingSize=11`

### Gaussian

**Name:** `gauss`
**Arguments:**

| name  | description | default value | example |
| --- | --- | --- | --- |
| Size | The size of the kernel. Must be odd. | 1 | `-DcrSmoothingSize=5` |
| Mean | The mean of the gaussian distribution. | 0 | `-DcrSmoothingMean=1` |
| Variance | The variance of teh gaussian distribution. | 1 | `-DcrSmoothingVariance=9` |

**Example:** `-DcrSmoothing=gauss -DrcSmoothingSize=5 -DcrSmoothingMean=1 -DcrSmoothingVariance=9`

---

# Examples

* Run the program on the image `input/fingerprint.png` with default arguments:  
   ```
   $ java -jar fingerprint.jar input/fingerprint.png
   ```
* Run the program on the image `input/fingerprint.png` with minutiae `input/minutiae.txt` and orientation image `input/orientation.png`.
   ```
   $ java -jar fingerprint.jar \
        -m input/minutiae.txt \
        -oi input/orientation.png \
        input/fingerprint.png`
   ```
* Run the program on the image `input/fingerprint.png` without estimating the ridge frequency and without plotting the images, use the `default` divergence estimator and save the divergence to `output/divergence.csv`.
   ```
   $ java -jar fingerprint.jar \
        --nogui \
        --skip-ridge-frequency \
        -d default \
        -do output/divergence.csv \
        input/fingerprint.png
   ```
* Run the program on the image `input/fingerprint.png` with orientation image `input/orientation.png`, save the curved region ridge frequency to `output/ridgefrequency.csv` and estimate the divergence using curved regions with average smoothing of size 35, but without using the real distance and without removing the line divergence. Finally, save the divergence to `output/divergence.csv`.
   ```
   $ java -jar fingerprint.jar \
        -oi input/orientation.png \
        -r cr \
        -ro output/ridgefrequency.csv \
        -d cr \
        -DcrSmoothing=avg \
        -DcrSmoothingSize=35 \
        -DcrUseRealDistance=false \
        -DcrRemoveLineDivergence=false \
        -do output/divergence.csv \
        input/fingerprint.png
   ```

# Licence

This package is released under the [GPL3.0 licence](https://github.com/jwieditz/MiSeal/blob/master/license).

We acknowledge the following projects which are included in the Java project:

 - JCommander, Copyright 2010 Cedric Beust <cedric@beust.com>, https://github.com/cbeust/jcommander
 - ControlsFX, https://github.com/controlsfx/controlsfx 

