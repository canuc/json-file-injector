package com.kik.inject.iface;

import java.io.IOException;
import java.io.OutputStream;

import javax.naming.NameNotFoundException;

/**
 * The default interface to inject parameters into the output stream.
 * 
 * @author julian
 */
public interface ParamProcessor {
	void outputVarContentsToStream(OutputStream os, String varName)
			throws NameNotFoundException, IOException;
}
