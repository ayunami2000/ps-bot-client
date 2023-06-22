/*
	This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

	N.B.  the above text was copied from http://www.gnu.org/licenses/gpl.html
	unmodified. I have not attached a copy of the GNU license to the source...

    Copyright (C) 2011-2012 Timo Rantalainen
*/

package me.ayunami2000.psbot.AudioParser;

import me.ayunami2000.psbot.AudioParser.Analysis.Analysis;
import me.ayunami2000.psbot.PitchDetection;
import me.ayunami2000.psbot.PsBot;

import javax.sound.sampled.*;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.util.Arrays;

public class Capture implements Runnable{
	/*Implement analysis here*/

	AudioFormat aFormat;
	TargetDataLine line;
	DataLine.Info info;
	PitchDetection mainProgram;
	int bitDepth;
	int bitSelection;
	int stereo;
	/*Constructor*/
	public Capture(int bitDepthIn, PitchDetection mainProgram){
		bitDepth = bitDepthIn;
		bitSelection = bitDepth/8;
		this.mainProgram = mainProgram;
		//mainProgram.rawFigure.f0s = null;
		stereo = 1; /*Capture mono*/
	}
	public Capture(PitchDetection mainProgram){
		this(16,mainProgram);
	}

	private static byte[] repeatByteArray(byte[] arr, int newLength) {
		byte[] dup = Arrays.copyOf(arr, newLength);
		for (int last = arr.length; last != 0 && last < newLength; last <<= 1) {
			System.arraycopy(dup, 0, dup, last, Math.min(last << 1, newLength) - last);
		}
		return dup;
	}
	private static byte[] concatByteArray(byte[] first, byte[] second) {
		byte[] result = Arrays.copyOf(first, first.length + second.length);
		System.arraycopy(second, 0, result, first.length, second.length);
		return result;
	}
	private static byte[] addByteArray(byte[] a, int index, byte[] num) {
		byte[] result = new byte[a.length];
		System.arraycopy(a, 0, result, 0, index);
		System.arraycopy(a, index, result, index + num.length, a.length - index - num.length);
		//result[index] = num;
		System.arraycopy(num, 0, result, index, num.length);
		return result;
	}

	public void run() {
				aFormat = new AudioFormat(mainProgram.samplingRate,bitDepth,stereo,true,false);
				info = new DataLine.Info(TargetDataLine.class, aFormat);
			//System.out.println(info);
			try{
				Mixer.Info[] mixerInfos = AudioSystem.getMixerInfo();
				Mixer mixer = null;
				for (int i = 0; i < mixerInfos.length; i++) {
					if (mixerInfos[i].getName().trim().equals(PsBot.audioThread.deviceName)) {
						mixer = AudioSystem.getMixer(mixerInfos[i]);
						break;
					}
				}
				if(mixer==null){
					for (int i = 0; i < mixerInfos.length; i++) {
						try {
							int index = Integer.parseInt(PsBot.audioThread.deviceName);
							if (index==i) {
								mixer = AudioSystem.getMixer(mixerInfos[i]);
								break;
							}
						}catch(NumberFormatException e){
							break;
						}
					}
				}
				if(mixer==null){
					PsBot.chatMsg("Error: Device not found!");
					PsBot.isPlaying=false;
					return;
				}
				//line = (TargetDataLine) AudioSystem.getLine(info);
				line = (TargetDataLine) mixer.getLine(info);
				line.open(aFormat,line.getBufferSize());
				line.start();		//Start capturing
				int bufferSize = mainProgram.fftWindow*bitSelection*stereo;
				int limSize=PsBot.audioThread.bpmTypeNumber;
				byte buffer[] = new byte[0];
				if(limSize>bufferSize){
					bufferSize=limSize;
				}
				if(limSize==bufferSize){
					buffer = new byte[bufferSize];
				}
				int testC = 0;
				byte[] lastPart=new byte[bufferSize-limSize];
				while (mainProgram.continueCapturing) {
					int count;
					if(limSize==bufferSize){
						count = line.read(buffer, 0, buffer.length); /*Blocking call to read*/
					}else{
						byte limBuf[] = new byte[limSize];
						count = line.read(limBuf, 0, limSize);
						buffer = concatByteArray(lastPart, limBuf);
						lastPart = addByteArray(lastPart, 0, limBuf);
					}
					//System.out.println("Got data "+count);
					if (count > 0) {
						if (bitSelection ==1){
							//mainProgram.rawFigure.drawImage(buffer,mainProgram.imWidth,mainProgram.imHeight);
							/*Add pitch detection here for 8 bit, not implemented...*/
						}
						if (bitSelection ==2){
							short[] data = byteArrayToShortArray(buffer);

							if (false){
							   /*Build test signal*/
							   double[] tempSignal = new double[data.length];
							   for (int i = 0;i<data.length;++i){
								  tempSignal[i] = 0;
								for (int h =0;h<20;++h){

									tempSignal[i] += (1.0/(2.0+1.0+((double)h))*
									(
									Math.sin(2.0*Math.PI*((double)(i+testC))/mainProgram.samplingRate*82.4*((double)h+1.0))
									)
									*Math.pow(2.0,13.0)
									);

									tempSignal[i] += (1.0/(2.0+1.0+((double)h))*
									(
									Math.sin(2.0*Math.PI*((double)(i+testC))/mainProgram.samplingRate*123.5*((double)h+1.0))
									)
									*Math.pow(2.0,13.0)
									);

									tempSignal[i] += (1.0/(2.0+1.0+((double)h))*
									(
									Math.sin(2.0*Math.PI*((double)(i+testC))/mainProgram.samplingRate*164.8*((double)h+1.0))
									)
									*Math.pow(2.0,13.0)
									);
								}
								//System.out.print(data[i]+" ");
								data[i] = (short) tempSignal[i];
							   }
							   if (false && testC == 0){
								printResult(data,new String("signal.bin"));
								++testC;

							   }
							}
							//mainProgram.rawFigure.drawImage(data,mainProgram.imWidth,mainProgram.imHeight);
							/*Add pitch detection here for 16 bit*/
							//System.out.println("To analysis");
							Analysis analysis = new Analysis(data,mainProgram);	//FFT + klapuri analysis
							//System.out.println("Out of analysis");
							if (false && testC == 1){
							   printResult(analysis.klapuri.whitened,new String("whitened.bin"));
								printResult(analysis.amplitudes,new String("amplitudes.bin"));
								printResult(analysis.klapuri.gammaCoeff,new String("gamma.bin"));
							}

							new Thread(() -> {
								PsBot.audioThread.pitchDetect(analysis);
							}).start();

							/*
							mainProgram.rawFigure.clearPlot();
							mainProgram.rawFigure.plotTrace(data);
							mainProgram.rawFigure.paintImageToDraw();


							mainProgram.whitenedFftFigure.clearPlot();
							mainProgram.whitenedFftFigure.plotTrace(analysis.klapuri.whitened,analysis.whitenedMaximum,1024);
							mainProgram.whitenedFftFigure.plotNumber(analysis.klapuri.f0s);
							mainProgram.whitenedFftFigure.paintImageToDraw();
							*/
						}

					}

				}
				line.stop();
				line.flush();
				line.close();
			} catch  (Exception err){
				//System.err.println("Error: " + err.getMessage());
				err.printStackTrace();
				if(line!=null&&line.isOpen()){
					line.stop();
					line.flush();
					line.close();
				}
			}
		}

		public static short[] byteArrayToShortArray(byte[] arrayIn){
			short[] shortArray = new short[arrayIn.length/2];
			for (int i = 0;i<shortArray.length;++i){
				shortArray[i] = (short) (((((int) arrayIn[2*i+1]) & 0XFF)<< 8) | (((int) arrayIn[2*i]) & 0XFF));
			}
			return shortArray;
		}

		public void printResult(short[] array,String fileName){
		   	double[] temp = new double[array.length];
		   	for (int i =0;i<array.length;++i){
		   	 	temp[i] = (double) array[i];
		   	}
		   	printResult(temp,fileName);
		}

		public void printResult(double[] array,String fileName){
			try{
				/*Wrap the array to double buffer*/
				DoubleBuffer db = DoubleBuffer.wrap(array);
				db.rewind();
				byte[] byteArray = new byte[array.length*8];
				/*cast float buffer to bytebuffer, and get the byte array*/
				ByteBuffer.wrap(byteArray).asDoubleBuffer().put(db);
				/*Print the results to a file*/
				BufferedOutputStream oStream = new BufferedOutputStream(new FileOutputStream(fileName));
				oStream.write(byteArray);
				oStream.flush();
				oStream.close();
				oStream = null;
			}catch (Exception err){
				System.out.println(err.toString());
				System.out.println("Couldn't write the signal file");
			}
		}

}

