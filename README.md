json-file-injector
==================

A mojo that will allow you to inject file data into a json template.

Sample Usage
============

 	<plugin>
	 <groupId>com.kik.inject</groupId>
	 <artifactId>json-file-injector</artifactId>
	 <version>1.0</version>
	 <executions>
    	   <execution>
      		<id>build-json</id>
		    <configuration>
			    <inputFile>src/json/cloud-formation-template.json</inputFile>
		    </configuration>
		    <phase>process-test-resources</phase>
				<goals>
				    <goal>build-json</goal>
	  		    </goals>
			</execution>
    	</executions>
	</plugin>
