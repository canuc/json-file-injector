package com.kik.inject;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.File;
import java.io.IOException;
import java.security.InvalidParameterException;

import javax.naming.NameNotFoundException;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import com.kik.inject.json.InjectController;
import com.kik.inject.replace.InputFileReplacer;

/**
 * Goal which will build json files with file replacements. Also this will allow
 * the sources to read maven project properties.
 * 
 * @phase process-sources
 */
@Mojo(name = "build-json")
public class InjectJSONVars extends AbstractMojo {
	/**
	 * Location of the parameters to specify. These are files with static
	 * information to fill into any vars.
	 * 
	 * @parameter default-value="${project.basedir}/src/inject/"
	 */
	@Parameter(defaultValue = "${project.basedir}/src/inject/")
	private String dirVarFiles;

	/**
	 * The location of the input file. Can be absolute or relative to the
	 * project basedir.
	 * 
	 * @required
	 */
	@Parameter(required = true)
	private String inputFile;

	/**
	 * The token sequence that will be used to begin the replacement matching.
	 * 
	 * @parameter default-value="{*{"
	 */
	@Parameter(defaultValue = "{*{")
	private String paramStartTag;

	/**
	 * The token sequence that will be used to end the replacement matching.
	 * 
	 * @parameter default-value="}}"
	 */
	@Parameter(defaultValue = "}}")
	private String paramEndTag;

	/**
	 * The location of the output files.
	 * 
	 * @parameter default-value="${project.build.directory}/inject
	 */
	@Parameter(defaultValue = "${project.build.directory}/inject")
	private String outputDirectory;
	
	/**
	 * Do you want base64 encoded insertions
	 * 
	 * @parameter default-value="${project.build.directory}/inject
	 */
	@Parameter(defaultValue = "false")
	private boolean base64;

	/**
	 * The project parameter. This will be by default the current project that
	 * this mojo is executed on.
	 * 
	 * @parameter default-value="${project}"
	 */
	@Parameter(defaultValue = "${project}")
	private MavenProject mavenProject;

	public void execute() throws MojoExecutionException {

		getLog().debug("inputFile: " + inputFile);
		getLog().debug("outputFile: " + outputDirectory);
		getLog().debug("dirVarFiles: " + dirVarFiles);

		File fileInput = new File(inputFile);
		File directoryOutput = new File(outputDirectory);
		File fileOut = new File(directoryOutput, fileInput.getName());
		File dirParamValues = new File(dirVarFiles);

		getLog().debug("inputFile: " + inputFile);
		getLog().debug("outputFile: " + outputDirectory);
		getLog().debug("dirVarFiles: " + dirVarFiles);

		if (paramStartTag.equals("")) {
			throw new InvalidParameterException(
					"Cannot set the parameter start tag to empty!");
		}

		if (paramEndTag.equals("")) {
			throw new InvalidParameterException(
					"Cannot set the parameter end tag to empty!");
		}

		try {
			if (!directoryOutput.exists()) {
				directoryOutput.mkdirs();
			}

			if (fileInput.exists()) {
				if (directoryOutput.canWrite()) {
					InputFileReplacer replacer = new InputFileReplacer(
							fileInput, fileOut, paramStartTag, paramEndTag,
							getLog());
					
					InjectController controller = new InjectController(
							dirParamValues, mavenProject,directoryOutput, getLog());
					

					for (String key : controller.getKeySet()) {
						getLog().debug("GOT KEY: " + key);
					}

					replacer.runReplace(controller);

				} else {
					getLog().debug(
							"Error cannot write to the specified file:"
									+ fileOut.getAbsolutePath());
				}
			} else {
				getLog().debug(
						"Error input file does not exist: "
								+ fileInput.getAbsolutePath());
			}

		} catch (NameNotFoundException e) {
			throw new MojoExecutionException("Could not find name", e);
		} catch (IOException e) {
			throw new MojoExecutionException("Issue writing to file", e);
		}
	}
}
