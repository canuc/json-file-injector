package com.kik.inject.replace;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;

import javax.naming.NameNotFoundException;

import org.apache.maven.plugin.logging.Log;

import com.kik.inject.iface.ParamProcessor;

public class InputFileReplacer {
	
	private static enum ParseType {
		ParseTypeBody,
		ParseTypePossibleVar,
		ParseVarBody,
		ParseVarEnd
	};
	
	private final String paramStartSequence;
	private final String paramEndSequence;
	
	private final File fileOut;
	private final File fileIn;
	private final Log _log;
	
	public InputFileReplacer( File replaceIn, File replaceOut, String paramStart, String paramEnd,  Log log ) {
		fileOut = replaceOut;
		fileIn = replaceIn;
		_log = log;
		paramStartSequence = paramStart;
		paramEndSequence = paramEnd;
	}
	
	/**
	 * This will do our replacement. It will pass any found variables down to the specfied processor
	 * 
	 * @param processor 
	 * @throws NameNotFoundException if a var is found that could not be matched this will be thrown
	 * @throws IOException IOException will be thrown if we cannot read or write from the file
	 */
	public void runReplace( ParamProcessor processor) throws NameNotFoundException, IOException {
		ParseType type = ParseType.ParseTypeBody;
		String partial = "";
		
		FileInputStream is = null; 
		FileOutputStream os = null;
		
		try {
			is = new FileInputStream(fileIn);
			os =  new FileOutputStream(fileOut,false);
			int currByte= is.read();
			while (currByte != -1) {

				if ( type == ParseType.ParseTypeBody ){
					if ( currByte == paramStartSequence.charAt(0)) {
						partial += paramStartSequence.charAt(0);
						type = ParseType.ParseTypePossibleVar;
					} else {
						os.write(currByte);
					}
				} else if ( type == ParseType.ParseTypePossibleVar ) {
					partial += new String(new int[]{currByte},0,1);
					if ( paramStartSequence.startsWith(partial)) {
						// If we have found all the end of the declaration
						if ( partial.length() == paramStartSequence.length() ) {
							type = ParseType.ParseVarBody;
							partial = "";
						}
					} else {
						os.write(partial.getBytes(Charset.forName("UTF-8")));
						type = ParseType.ParseTypeBody;
						partial = ""; // Drop out
					}
				} else if ( type == ParseType.ParseVarBody ) {
					partial += new String(new int[]{currByte},0,1);
					
					if ( partial.endsWith(paramEndSequence)) {
						String varName = partial.substring(0,partial.length()-paramEndSequence.length());
						processor.outputVarContentsToStream(os, varName);
						type = ParseType.ParseTypeBody;
						partial = "";
					}
				}
				
				currByte = is.read();
			}
		} finally {
			if ( is != null ) {
				is.close();
			}
			
			if (os != null ) {
				os.close();
			}
		}
		
		if ( type != ParseType.ParseTypeBody ) {
			_log.warn("Did not complete variable, still in body block..");
		}
	}
	
	
}
