package com.kik.inject.json;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.naming.NameNotFoundException;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.json.simple.JSONObject;

import com.kik.inject.iface.ParamProcessor;

public class InjectController implements ParamProcessor {
	public final Map<String, File> mapConfig;
	public final Map<String, String> systemProperties;

	private final Log _log;
	private static final int CHARACTER_NOT_FOUND = -1;
	private static final int MAX_BUFFER_SIZE = 1024;

	public InjectController(File rootFileHandle, MavenProject project,
			boolean recursive, Log log) {
		_log = log;
		_log.debug("File: " + rootFileHandle.getAbsolutePath());
		_log.debug("File is dir " + rootFileHandle.isDirectory());

		systemProperties = addPropertiesFromProperties(project.getProperties());
		if (rootFileHandle.isDirectory()) {
			mapConfig = getFileList(rootFileHandle, recursive);
		} else {
			mapConfig = new HashMap<String, File>();
			mapConfig.put(getVarEntry("", rootFileHandle), rootFileHandle);
		}
	}

	protected Map<String, String> addPropertiesFromProperties(
			Properties properties) {
		final Map<String, String> systemParams = new HashMap<String, String>();
		Set<Object> propertyKeys = properties.keySet();

		for (Object propertyKey : propertyKeys) {
			if (propertyKey instanceof String) {
				String propertyKeyString = (String) propertyKey;
				systemParams.put(propertyKeyString,
						properties.getProperty(propertyKeyString, "")
								.toString());
			}
		}
		return systemParams;
	}

	protected Map<String, File> getFileList(File directory, boolean recursive) {
		return getFileList(directory, recursive, new String());
	}

	private Map<String, File> getFileList(File directory, boolean recursive,
			String prepend) {
		final Map<String, File> fileMap = new HashMap<String, File>();
		final File[] files = directory.listFiles();

		for (File currFile : files) {
			if (currFile.isFile()) {
				fileMap.put(getVarEntry(prepend, currFile), currFile);
			} else if (recursive && currFile.isDirectory()) {
				if (!currFile.getName().equals("")) {
					String subDirPrepend = prepend + currFile.getName() + ".";
					fileMap.putAll(getFileList(currFile, recursive,
							subDirPrepend));
				}
			}
		}

		return fileMap;
	}

	private String getVarEntry(String prepend, File file) {
		final StringBuilder sb = new StringBuilder(prepend);
		final String fileName = file.getName();

		String varName = fileName;
		int lastIndex = fileName.lastIndexOf(".");

		if (lastIndex != CHARACTER_NOT_FOUND) {
			varName = fileName.substring(0, lastIndex);
		}

		sb.append(varName);
		return sb.toString();
	}

	public void outputVarContentsToStream(OutputStream os, String varName)
			throws NameNotFoundException, IOException {
		if (!mapConfig.containsKey(varName)
				&& !systemProperties.containsKey(varName)) {
			throw new NameNotFoundException("Could not find name: " + varName);
		}

		if (mapConfig.containsKey(varName)) {
			File fileForVar = mapConfig.get(varName);
			InputStream is = null;
			try {
				is = new FileInputStream(fileForVar);

				BufferedInputStream bis = new BufferedInputStream(is);

				byte[] ret = new byte[MAX_BUFFER_SIZE];
				boolean done = false;

				while (!done) {
					int read = bis.read(ret);
					if (read == -1) {
						done = true;
						break;
					}
					String tmpString = new String(ret, 0, read);

					writeJSONEscapedStringToStream(os, tmpString);
				}

				bis.close();

			} catch (IOException ioe) {
				_log.error(ioe);
			} finally {
				if (is != null) {
					try {
						is.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		} else {
			writeJSONEscapedStringToStream(os, systemProperties.get(varName));
		}

		return;
	}

	protected void writeJSONEscapedStringToStream(OutputStream os,
			String toWrite) throws IOException {
		String tmpString = JSONObject.escape(toWrite);
		os.write(tmpString.getBytes(Charset.forName("UTF-8")));
	}

	public Set<String> getKeySet() {
		return mapConfig.keySet();
	}

}
