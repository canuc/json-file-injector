package com.kik.inject.iface;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;

import javax.naming.NameNotFoundException;

/**
 * The default interface to inject parameters into the output stream.
 * 
 * @author julian
 */
public interface ParamProcessor {
	boolean outputVarContentsToStream(OutputStream os, String varName)
			throws NameNotFoundException, IOException;
	
	Set<String> getKeySet();
}
