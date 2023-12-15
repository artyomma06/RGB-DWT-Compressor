# Image Compressor

This Java program, `ImageCompressor`, implements image compression and decomposition using the Discrete Wavelet Transform (DWT) technique. It takes an 512 by 512 rgb image file as input, performs the DWT on its RGB channels, and displays the compressed result.

## Description

The `ImageCompressor` program utilizes the DWT technique to decompose and reconstruct an image, providing a compressed representation. It leverages concepts from wavelets for computer graphics, specifically focusing on low-pass and high-pass filtering for both rows and columns.

## Requirements

- Java Runtime Environment (JRE)

## Usage

To run the program, compile and execute the Java file using the following commands:

javac ImageCompressor.java
java ImageCompressor imagepath.rgb
