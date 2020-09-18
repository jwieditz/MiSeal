/**
 * Utilities.java
 *
 * This class contains a bunch of static methods which are used by other
 * functions
 */

package de.unigoettingen.math.fingerprint.utilities;

import java.lang.reflect.Array;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.MediaTracker;
import java.awt.image.ImageObserver;
import java.lang.Math;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;


//import utilities.Interval;

public class Utilities
{

    public static double [][] arrayToMatrix(double[]array, int width)
    {
        if (array.length%width>0)
        {
            throw new IllegalArgumentException("Array's length isn't a multiple of width");
        }

        double [][] matrix = new double[array.length/width][width];

        for(int i=0; i<array.length; i++)
        {
            matrix[i/width][i%width] = array[i];
        }

        return matrix;
    }

    public static int [][] arrayToMatrix(int[]array, int width)
    {
        if (array.length%width>0)
        {
            throw new IllegalArgumentException("Array's length isn't a multiple of width");
        }

        int [][] matrix = new int[array.length/width][width];

        for(int i=0;i<array.length;i++)
        {
            matrix[i/width][i%width] = array[i];
        }

        return matrix;
    }

    //http://leepoint.net/notes-java/data/arrays/arrays-ex-reverse.html
    public static void reverseArray(int[] b)
    {
        int left  = 0;          // index of leftmost element
        int right = b.length-1; // index of rightmost element
        int temp;

        while (left < right)
        {
            temp = b[left];
            b[left]  = b[right];
            b[right] = temp;

            // move the bounds toward the center
            left++;
            right--;
       }
    }//endmethod reverse

    public static int arrayMin(double [] array)
    {
        double min = Double.MAX_VALUE;
        int index=-1;

        for(int i=0; i<array.length; i++)
        {
            if (array[i] < min)
            {
                index = i;
                min = array[i];
            }
        }

        return index;
    }
    
    public static double arrayMinValue(double [] array)
    {
        double min = Double.MAX_VALUE;

        for(int i=0;i<array.length;i++)
        {
            if (array[i]<min)
            {
                min = array[i];
            }
        }

        return min;
    }

    public static int arrayMax(double [] array)
    {
        double max = Double.MIN_VALUE;
        int index=-1;

        for(int i=0;i<array.length;i++)
        {
            if (array[i]>max)
            {
                index = i;
                max = array[i];
            }
        }

        return index;
    }
    
    public static double arrayMaxValue(double [] array)
    {
        double max = Double.MIN_VALUE;

        for(int i=0;i<array.length;i++)
        {
            if (array[i]>max)
            {
                max = array[i];
            }
        }

        return max;
    }    
    
    public static int[] matrixMax(double [][] matrix)
    {
        double max = Double.MIN_VALUE;
        int indeces[] ={-1,-1};

        for(int i=0;i<matrix.length;i++)
        {
            for(int j=0;j<matrix[i].length;j++)
            {
                if (matrix[i][j]>max)
                {
                    max = matrix[i][j];
                    indeces[0] = i; indeces[1]=j;
                }
            }
        }

        return indeces;
    }



    public static <T> void reverseArray(T[] b)
    {
        int left  = 0;          // index of leftmost element
        int right = b.length-1; // index of rightmost element
        T temp;

        while (left < right)
        {
            temp = b[left];
            b[left]  = b[right];
            b[right] = temp;

            // move the bounds toward the center
            left++;
            right--;
       }
    }//endmethod reverse

    public static String formatTime(long l)
    {
        double secs = l/1000.0;
        int millsecs = (int)((secs - Math.floor(secs))*1000);
        double min = secs/60.0;
        
        return (int)Math.floor(min)+"min "+((int)Math.floor(secs)%60)+"."+millsecs+"secs";

    }
    
    public static int[][] readImage(String absolutFilename) {

        BufferedImage bi = null;
        File file = new File(absolutFilename);
        try {
            bi = ImageIO.read(file);
        } catch (IOException e) {
        }
        int w = bi.getWidth();
        int h = bi.getHeight();
        int[][] gray = new int[w][h];
        BufferedImage im = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics g = im.getGraphics();
        g.drawImage(bi, 0, 0, null);
        g.dispose();
        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                int argb = im.getRGB(i, j);
                gray[i][j] = (argb >> 16) & 0xff;
            }
        }
        bi = null;
        return gray;

    }
    
    public static double[][] elementwiseInverse( double[][] image ){
        
        int w = image.length;
        int h = image[0].length;
        double[][] output = new double[w][h];
        
        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                
                if ( !Double.isNaN(image[i][j]) && image[i][j] != 0 ) {
                    
                    output[i][j] = 1.0 / image[i][j];
                    
                } else {
                    
                    output[i][j] = Double.NaN;
                }
            }
        }
        
        return output;       
    }
    

    /*public static int crossCorrelation(int[]a, int[]b,int delta)
    {
        int sum = 0;
        for(int i=0;i<a.length;i++)
        {
            try{sum+=a[i]*b[i+delta];}
            catch(ArrayIndexOutOfBoundsException aioobe){}
        }

        return sum;
    }

    public static int[] crossCorrelation(int[]a,int  [] b)
    {
        int []sum = new int[b.length*3];
        int m = b.length-1;
        for(int delta=-m;delta<=m*2;delta++)
        {
            sum[delta+m] = crossCorrelation(a, b, -delta);
        }
        return sum;
    }*/

//    public static <T> T[] arrayRange(T[] A, Interval interval)
//    {
//        T[] range = (T[]) Array.newInstance(A.getClass().getComponentType(), interval.length());
//
//        for(int i=0;i<range.length;i++)
//        {
//            range[i] = A[(i+interval.getFrom())%interval.getMax()];
//        }
//        
//        return (T[])range;
//    }
}
