
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.swing.*;
import java.lang.Math;


public class ImageCompressor {

	JFrame frame;
	JLabel lbIm1;
	BufferedImage imgOne;
	BufferedImage imgTwo;
	int width = 512; // default image width and height
	int height = 512;

	/**
	 * Reads the RGB image of given width and height from the specified imgPath into the provided BufferedImage.
	 * This method is adapted from the assignment 1 starter code.
	 */
	private void readImageRGB(int width, int height, String imgPath, BufferedImage img) {
		try {
			// Calculate the length of the frame in bytes
			int frameLength = width * height * 3;

			// Open the image file for reading
			File file = new File(imgPath);
			RandomAccessFile raf = new RandomAccessFile(file, "r");
			raf.seek(0);

			// Read the bytes from the file
			long len = frameLength;
			byte[] bytes = new byte[(int) len];
			raf.read(bytes);

			int ind = 0;
			// Iterate over each pixel in the image
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					// Extract RGB values from the bytes
					byte a = 0;
					byte r = bytes[ind];
					byte g = bytes[ind + height * width];
					byte b = bytes[ind + height * width * 2];

					// Combine RGB values into a pixel
					int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
					// Alternative method for combining RGB values
					// int pix = ((a << 24) + (r << 16) + (g << 8) + b);

					// Set the pixel in the BufferedImage
					img.setRGB(x, y, pix);
					ind++;
				}
			}
		} catch (FileNotFoundException e) {
			// Handle file not found exception
			e.printStackTrace();
		} catch (IOException e) {
			// Handle IO exception
			e.printStackTrace();
		}
	}

	/** Takes a 2d array and calculates the low and high passes for an individual row
	 *  Note: The following code is built off of lecture and the Psuedo code given to us by the professor 
	 *  The only difference is the fact that I am using a 2d array and specify the index and that this
	 *  goes from 0 the h/2 instead of 1 to h/2 therefore Change in index logic and using /2 since dont
	 *  need to normalize. Also sets high pass values to zero as instructed by the professor.
	 *  from the Wavelets for Computer Graphics: A primer part 1 paper(link below to online version, but used one
	 *  on DEN):
	 * 	https://legacy.sites.fas.harvard.edu/~cs278/papers/stollnitz95wavelets.pdf
	 *  https://en.wikipedia.org/wiki/Discrete_wavelet_transform
	 */
	public void rowDecompositionForStep(int[][] reals, int index, int height) {
		int[] cPrime = new int[height];
		for (int i = 0; i < (height / 2); i++) {
			cPrime[i] = (int) ((reals[index][2*i] + reals[index][2*i + 1]) / 2); // may need to change casting type
			cPrime[(height / 2) + i] = (int) ((reals[index][2*i] - reals[index][2*i + 1]) / 2);
		}

		for (int i = 0; i < height; i++) {
			reals[index][i] = cPrime[i];
		}

		// Set High pass values to zero
		for (int i = height / 2; i < reals[index].length; i++) {
			reals[index][i] = 0;
		}

	}

	/** Takes a 2d array and calculates the low and high passes for an individual row
	 *  Note: The following code is built off of lecture and the Psuedo code given to us by the professor
	 *  From the Wavelets for Computer Graphics: A primer part 1 paper(link below to online version, but used one
	 *  on DEN):
	 * 	https://legacy.sites.fas.harvard.edu/~cs278/papers/stollnitz95wavelets.pdf
	 *  https://en.wikipedia.org/wiki/Discrete_wavelet_transform
	 */
	public void rowDecomposition(int[][] reals, int index, int runs) {
		int height = reals[index].length;
		int times = 9 - runs;
		for (int i = 0; i < times; i++) {
			rowDecompositionForStep(reals, index, height);
			height = height / 2;
		}
	}

	/** Takes a 2d array and calculates the low and high passes for an individual column
	 *  Note: The following code is built off of lecture and the Psuedo code given to us by the professor 
	 *  The only difference is the fact that I am using a 2d array and specify the index and that this
	 *  goes from 0 the h/2 instead of 1 to h/2 therefore Change in index logic and using /2 since dont
	 *  need to normalize. Also sets high pass values to zero as instructed by the professor
	 *  from the Wavelets for Computer Graphics: A primer part 1 paper(link below to online version, but used one
	 *  on DEN):
	 * 	https://legacy.sites.fas.harvard.edu/~cs278/papers/stollnitz95wavelets.pdf
	 *  https://en.wikipedia.org/wiki/Discrete_wavelet_transform
	 */
	public void colDecompositionForStep(int[][] reals, int index, int width) {
		int[] cPrime = new int[width];
		for (int i = 0; i < (width / 2); i++) {
			cPrime[i] = (int) ((reals[2*i][index] + reals[2*i + 1][index]) / 2);
			cPrime[(width / 2) + i] = (int) ((reals[2*i][index] - reals[2*i + 1][index]) / 2);
		}

		for (int i = 0; i < width; i++) {
			reals[i][index] = cPrime[i];
		}

		// Set High pass values to zero
		for (int i = width / 2; i < reals.length; i++) {
			reals[i][index] = 0;
		}
	}

	/** Takes a 2d array and calculates the low and high passes for an individual column
	 *  Note: The following code is built off of lecture and the Psuedo code given to us by the professor
	 *  From the Wavelets for Computer Graphics: A primer part 1 paper(link below to online version, but used one
	 *  on DEN):
	 * 	https://legacy.sites.fas.harvard.edu/~cs278/papers/stollnitz95wavelets.pdf
	 *  https://en.wikipedia.org/wiki/Discrete_wavelet_transform
	 */
	public void colDecomposition(int[][] reals, int index, int runs) {
		int width = reals.length;
		int times = 9 - runs;
		for (int i = 0; i < times; i++) {
			colDecompositionForStep(reals, index, width);
			width = width / 2;
		}
	}

	/** Takes a 2d array and calculates the low and high passes for a row and column
	 * 	I didn't end up using this, but am keeing it incase I end up deciding to go back and implement this
	 *  approach as well.
	 *  Note: The following code is built off of lecture and the Psuedo code given to us by the professor
	 *  From the Wavelets for Computer Graphics: A primer part 1 paper(link below to online version, but used one
	 *  on DEN):
	 * 	https://legacy.sites.fas.harvard.edu/~cs278/papers/stollnitz95wavelets.pdf
	 *  https://en.wikipedia.org/wiki/Discrete_wavelet_transform
	 */
	public void nonStandardDecomposition(int[][] reals) {
		int height = reals.length;
		while (height > 1) {
			for (int i = 0; i < height; i++) {
				rowDecompositionForStep(reals, i, height);
			}
			for (int i = 0; i < height; i++) {
				colDecompositionForStep(reals, i, height);
			}
			height = height / 2;
		}
	}

	/** Takes a 2d array and calculates the inverse from the low pass filter for a column
	 *  Note: The following code is built off of the inverse from
	 *  lecture and the Psuedo code given to us by the professor. 
	 *  Zeroing logic was told to me by TA
	 *  From the Wavelets for Computer Graphics: A primer part 1 paper(link below to online version, but used one
	 *  on DEN):
	 * 	https://legacy.sites.fas.harvard.edu/~cs278/papers/stollnitz95wavelets.pdf
	 *  https://en.wikipedia.org/wiki/Discrete_wavelet_transform
	 */
	public void colCompositionForStep(int[][] reals, int index, int width) {
		int[] cPrime = new int[width * 2];
		for (int i = 0; i < width; i++) {
			cPrime[i * 2] = reals[i][index] + reals[width + i][index]; // may need to change casting type
			cPrime[(i*2) + 1] = reals[i][index] - reals[width + i][index];
		}

		for (int i = 0; i < cPrime.length; i++) {
			reals[i][index] = cPrime[i];
		}
	}


	/** Takes a 2d array and calculates the inverse low pass for an individual column
	 *  Note: The following code is built off of lecture and the Psuedo code given to us by the professor
	 *  From the Wavelets for Computer Graphics: A primer part 1 paper(link below to online version, but used one
	 *  on DEN):
	 * 	https://legacy.sites.fas.harvard.edu/~cs278/papers/stollnitz95wavelets.pdf
	 *  https://en.wikipedia.org/wiki/Discrete_wavelet_transform
	 */
	public void colComposition(int[][] reals, int index, int n) {
		int width = (int) Math.pow(2, n);
		while (width < reals.length) {
			colCompositionForStep(reals, index, width);
			width = width * 2;
		}
	}


	/** Takes a 2d array and calculates the inverse from the low pass filter for a row
	 *  Note: The following code is built off of the inverse from
	 *  lecture and the Psuedo code given to us by the professor. 
	 *  Zeroing logic was told to me by TA
	 *  From the Wavelets for Computer Graphics: A primer part 1 paper(link below to online version, but used one
	 *  on DEN):
	 * 	https://legacy.sites.fas.harvard.edu/~cs278/papers/stollnitz95wavelets.pdf
	 *  https://en.wikipedia.org/wiki/Discrete_wavelet_transform
	 */
	public void rowCompositionForStep(int[][] reals, int index, int height) {
		int[] cPrime = new int[height * 2];
		for (int i = 0; i < height; i++) {
			cPrime[i * 2] = reals[index][i] + reals[index][height + i];
			cPrime[(i*2) + 1] = reals[index][i] - reals[index][height + i];
		}
		for (int i = 0; i < cPrime.length; i++) {
			reals[index][i] = cPrime[i];
		}
	}

	/** Takes a 2d array and calculates the inverse low pass for a row
	 *  Note: The following code is built off of lecture and the Psuedo code given to us by the professor
	 *  From the Wavelets for Computer Graphics: A primer part 1 paper(link below to online version, but used one
	 *  on DEN):
	 * 	https://legacy.sites.fas.harvard.edu/~cs278/papers/stollnitz95wavelets.pdf
	 *  https://en.wikipedia.org/wiki/Discrete_wavelet_transform
	 */
	public void rowComposition(int[][] reals, int index, int n) {
		int height = (int) Math.pow(2, n);
		while (height < reals[index].length) {
			rowCompositionForStep(reals, index, height);
			height = height * 2;
		}
	}

	// Function which does both the encoding and decoding for a buffered image and stores it inside of buffered
	// image. n indicates how deep we are going in. logic from assignment/lecture
	public void encoder(BufferedImage img, int n) {
		// buffers for each individual channel.
		int[][] Rvals = new int[512][512];
		int[][] Gvals = new int[512][512];
		int[][] Bvals = new int[512][512];

		// Code below is taken from my implementation in assignments 1/2 in order to extract channel values.
		for(int y = 0; y < img.getHeight(); y++) {
			for(int x = 0; x < img.getWidth(); x++) {
				int pix = img.getRGB(x, y);
				int r = ((pix >> 16) & 0xFF);
				int g = ((pix >> 8) & 0xFF);
				int b = ((pix) & 0xFF);
				Rvals[y][x] = r;
				Gvals[y][x] = g;
				Bvals[y][x] = b;
			}	
		}

		// Doing decomposition for all rows
		for (int i = 0; i < img.getHeight(); i++) {
			rowDecomposition(Rvals, i, n);
			rowDecomposition(Gvals, i, n); 
			rowDecomposition(Bvals, i, n);
		}
		
		// Doing decomposition for all columns
		for (int i = 0; i < img.getWidth(); i++) {
			colDecomposition(Rvals, i, n);
			colDecomposition(Gvals, i, n);
			colDecomposition(Bvals, i, n);
		}
		
		// Doing composition for all columns
		for (int i = 0; i < img.getWidth(); i++) {
			colComposition(Rvals, i, n);
			colComposition(Gvals, i, n);
			colComposition(Bvals, i, n);
		}

		// Doing composition for all rows.
		for (int i = 0; i < img.getHeight(); i++) {
			rowComposition(Rvals, i, n);
			rowComposition(Gvals, i, n);
			rowComposition(Bvals, i, n);
		}

		// Setting new values
		for(int y = 0; y < img.getHeight(); y++) {
			for(int x = 0; x < img.getWidth(); x++) {
				int r = Rvals[y][x];
				int g = Gvals[y][x];
				int b = Bvals[y][x];
				int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
        		img.setRGB(x, y, pix);
			}	
		}

	}

	/**
	 * Creates copy of an image to another image.
	 * Taken from My implementation of assignment 1.
	 */
	private void copyImage(BufferedImage firstImage, BufferedImage secondImage) {
		for(int y = 0; y < firstImage.getHeight(); y++) {
			for(int x = 0; x < firstImage.getWidth(); x++) {
				int pix = firstImage.getRGB(x, y);
				secondImage.setRGB(x,y,pix);
			}
		}
	}

	public void showIms(String[] args){

		// Read a parameter from command line
		int param1 = Integer.parseInt(args[1]);

		// Read in the specified image
		imgOne = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		imgTwo = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		readImageRGB(width, height, args[0], imgOne);
		// Use label to display the image
		frame = new JFrame();
		GridBagLayout gLayout = new GridBagLayout();
		frame.getContentPane().setLayout(gLayout);

		lbIm1 = new JLabel(new ImageIcon(imgOne));

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.CENTER;
		c.weightx = 0.5;
		c.gridx = 0;
		c.gridy = 0;

		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 1;
		frame.getContentPane().add(lbIm1, c);

		frame.pack();
		
		if (param1 == -1) {
			for (int i = 0; i < 10; i++) {
				copyImage(imgOne, imgTwo);
				encoder(imgTwo, i);
				lbIm1.setIcon(new ImageIcon(imgTwo));
				frame.setVisible(true);
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {}
			}
		} else {
			encoder(imgOne, param1);
			lbIm1.setIcon(new ImageIcon(imgOne));
			frame.setVisible(true);
		}
	}

	public static void main(String[] args) {
		ImageCompressor ren = new ImageCompressor();
		ren.showIms(args);
	}

}
