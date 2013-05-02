package com.kik.inject.json;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;

import javax.naming.NameNotFoundException;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

import com.kik.inject.iface.ParamProcessor;

public class InjectController implements ParamProcessor {
	private final MavenEnvInjectController mavenEnvInjectController;
	private final FileInjectController fileInjectController;
	private final Log _log;
	
	public InjectController(File rootFileHandle, MavenProject project, File outDir, Log log) {
		_log = log;
		_log.debug("File: " + rootFileHandle.getAbsolutePath());
		_log.debug("File is dir " + rootFileHandle.isDirectory());

		mavenEnvInjectController = new MavenEnvInjectController(project,_log);
		fileInjectController = new FileInjectController(rootFileHandle, outDir, mavenEnvInjectController, _log);
	}

	public boolean outputVarContentsToStream(OutputStream os, String varName)
			throws NameNotFoundException, IOException {
		boolean isHandled = fileInjectController.outputVarContentsToStream(os, varName);
		
		if (!isHandled ) {
			isHandled = mavenEnvInjectController.outputVarContentsToStream(os, varName);
		}

		return isHandled;
	}

	@Override
	public Set<String> getKeySet() {
		Set<String> aggregatedSet = new HashSet<String>();
		aggregatedSet.addAll(mavenEnvInjectController.getKeySet());
		aggregatedSet.addAll(fileInjectController.getKeySet());
		return aggregatedSet;
	}

}
