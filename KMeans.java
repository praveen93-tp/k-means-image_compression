
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;
import javax.imageio.ImageIO;

public class KMeans {
	public static void main(String[] args) {
		if (args.length < 2){
		    System.out.println("Usage: Kmeans <input-image> <k>");
		    return;
		}
		try{
			BufferedImage originalImage = ImageIO.read(new File(args[0]));
			int k=Integer.parseInt(args[1]);
			double bytes,input_image_size=0.0,output_image_size = 0.0;
			System.out.println("K-Value:"+k);
			File input_file = new File(args[0]);
			if(input_file.exists())
			{bytes = input_file.length();
			 input_image_size = (bytes / 1024);
			 }
			System.out.println("Input Image size:"+input_image_size+" KB");
			double[] each_outputImageSize = new double[5];
			for(int i=0;i<5;i++){
				String s = args[0].substring(0,args[0].length() - 4)+"_"+"K="+k+"_Img"+i+args[0].substring(args[0].length() - 4, args[0].length());
			    BufferedImage kmeansJpg = kmeans_helper(originalImage,k);
			    ImageIO.write(kmeansJpg, "jpg", new File(s)); 
			    File output_file = new File(s);
				if(output_file.exists())
				{
					bytes = output_file.length();
					each_outputImageSize[i]=(bytes / 1024);
					output_image_size = output_image_size + (bytes / 1024);
				}
			}	
			output_image_size = output_image_size/5;
			System.out.println("Output Image size(average of 5):"+output_image_size+"KB");
			double varience = 0;
			for(int i=0;i<each_outputImageSize.length;i++){
				varience = varience + Math.pow((each_outputImageSize[i]-output_image_size),2);
			}
			varience = varience/5;
			double comp_ratio = 100 - ((output_image_size/input_image_size)*100);
			System.out.println("Compression Ratio:"+comp_ratio);
			System.out.println("Varience:"+varience);
			
		}catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}

	private static BufferedImage kmeans_helper(BufferedImage actualImage, int k) {
		int width = actualImage.getWidth();
		int height = actualImage.getHeight();
		BufferedImage kmeansImage = new BufferedImage(width, height, actualImage.getType());
		Graphics2D g = kmeansImage.createGraphics();
		g.drawImage(actualImage, 0, 0, width, height, null);
		int[] rgb = new int[width* height];
		int count = 0;
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				rgb[count++] = kmeansImage.getRGB(i, j);
			}
		}
		kmeans(rgb, k);
		count = 0;
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				kmeansImage.setRGB(i, j, rgb[count]);
				count++;
			}
		}
		return kmeansImage;
	}
	
	private static void kmeans(int[] rgb, int k) {
		//int rgbLength = rgb.length;
		Color[] rgbColor = new Color[rgb.length];
		Color[] ColorCluster = new Color[rgb.length];
		int[]   ColorClusterID = new int[rgb.length];
		
		for(int i = 0;i<rgb.length;i++) {
			rgbColor[i] = new Color(rgb[i]);
			ColorClusterID[i] = -1;
		}
		
		Color[] currentCluster = new Color[k];		
		int randomNumber[]= ThreadLocalRandom.current().ints(0,  rgb.length).distinct().limit(k).toArray();
		Color[] modifiedColorCluster = new Color[k];	
		
		for(int j=0;j<k;j++) {
			currentCluster[j]=rgbColor[randomNumber[j]];
		}
		int flag = 1;
		int maxIterations = 1000;
		int numIteration=0;
		double[] distance = new double[maxIterations+1];		
		distance[0]=Double.MAX_VALUE;
		while(flag == 1) {
			flag = 0;
			numIteration++;
			distance[numIteration]=assignCLustersToPixels(rgbColor,currentCluster,ColorCluster,ColorClusterID,k,rgb.length);
			modifiedColorCluster= findMeans(rgbColor,ColorClusterID,modifiedColorCluster,k,rgb.length);
			if(!clusterCenterCheck(currentCluster, modifiedColorCluster,k)){
				flag = 1;
				for(int j=0;j<k;j++) {
					currentCluster[j]=modifiedColorCluster[j];}
				}
			}
		for(int i = 0;i<rgb.length;i++) {
			rgb[i]= getValue(ColorCluster[i].getRed(),ColorCluster[i].getGreen(),ColorCluster[i].getBlue());
		}
		return;
	  }
	
	private static boolean clusterCenterCheck(Color[] colorCluster, Color[] newColorCluster, int k) {
		for(int j=0;j<k;j++) {			
			if((colorCluster[j].getRed()!=newColorCluster[j].getRed())||
					(colorCluster[j].getGreen()!=newColorCluster[j].getGreen())
					||(colorCluster[j].getBlue()!=newColorCluster[j].getBlue())) {
				return false;
			}
		}
		return true;
	}
	
	
	private static Color[] findMeans(Color[] rgbColor, int[] ColorClusterID,Color[] newColorCluster,int k,int rgbLength) {
		double[] red =   new double[k];
		double[] green = new double[k];
		double[] blue =  new double[k];
		double[] alpha = new double[k];
		double[] count = new double[k];
		for(int i = 0;i<rgbLength;i++) {
			red[ColorClusterID[i]]=red[ColorClusterID[i]]+rgbColor[i].getRed();
			green[ColorClusterID[i]]=green[ColorClusterID[i]]+rgbColor[i].getGreen();
			blue[ColorClusterID[i]]=blue[ColorClusterID[i]]+rgbColor[i].getBlue();
			alpha[ColorClusterID[i]]=alpha[ColorClusterID[i]]+rgbColor[i].getAlpha();
			count[ColorClusterID[i]]++;
		}
		int r,b,g;
		for(int j=0;j<k;j++) {
			r = (int)(red[j]/count[j]);
			g = (int)(green[j]/count[j]);
			b = (int)(blue[j]/count[j]);
			newColorCluster[j]=new Color(getValue(r,g,b));
		}
		return newColorCluster;
	}

	private static double assignCLustersToPixels(Color[] rgbColor, Color[] colorCluster,
			Color[] assignedColorCluster, int[] assignedColorClusterID, int k, int rgbLength) {
		double minDistance;
		int index;
		double dist;
		double totaldistance = 0;
		for(int i=0;i<rgbLength;i++) {
			minDistance = Double.MAX_VALUE;
			index = -1;
			for(int j = 0;j<k;j++) {
				dist = calculateDistance(rgbColor[i],colorCluster[j]);
				if(dist<minDistance) {
					minDistance = dist;
					index = j;
				}
			}
			totaldistance = totaldistance + minDistance;
			if(assignedColorClusterID[i]!=index) {
			}
			assignedColorCluster[i]=colorCluster[index];
			assignedColorClusterID[i]=index;
		}
		return totaldistance;
	}
	public static int getValue(int r,int g,int b){
	    r = (r << 16) & 0x00FF0000; 
	    g = (g << 8) & 0x0000FF00; 
	    b = b & 0x000000FF; 
	    return 0xFF000000|r|g|b; 
	}
	private static double calculateDistance(Color mycolor1, Color mycolor2) {
		double red_diff = Math.pow(mycolor1.getRed()-mycolor2.getRed(),2);
		double blue_diff = Math.pow(mycolor1.getRed()-mycolor2.getRed(),2);
		double green_diff = Math.pow(mycolor1.getRed()-mycolor2.getRed(),2);
		double alph_diff = Math.pow(mycolor1.getRed()-mycolor2.getRed(),2);
		double calculatedDistance = Math.sqrt(red_diff+blue_diff+green_diff+alph_diff);
		return calculatedDistance;
	}
}