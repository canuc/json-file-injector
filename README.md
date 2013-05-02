json-file-injector
==================

A mojo that will allow you to inject file data into a json template. This plugin can be used 
to inject data into a cloud formation script as a part of the maven lifecycle.


Sample pom.xml Snippet
======================

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

Options 
=======
+ b - Base64 encoded output
+ e - escaped output

so `{*{var-name|be}}` - would base64 and escape the value or the contents of the file pointed to by `var-name`

`{*{var-name|e}}` - would simply escape the value or the contents of the file pointed to by `var-name`

	src/json/cloud-formation-template.json : 

```json
{
	"AWSTemplateFormatVersion": "2010-09-09",

	"Description": "test environment",

	"Parameters": {

		"InstanceSize": {
			"Description": "Instance size to use for the ec2 instance",
			"Type": "String",
			"Default": "t1.micro",
			"AllowedValues": ["t1.micro", "m1.small", "m1.large", "m1.xlarge", "m2.xlarge", "m2.2xlarge", "m2.4xlarge", "c1.medium", "c1.xlarge", "cc1.4xlarge"],
			"ConstraintDescription": "must select a valid instance type."
		},
	},

	"Resources": 
	{
	"SomeInstanceName": {
		"Type": "AWS::EC2::Instance",
		"Properties": {
			"ImageId": ...
			"InstanceType": {
				"Ref": "InstanceSize"
			},
			"SecurityGroups" : ...
			"Tags": ["{*{random-maven-property|e}}"],
			"UserData" : "{*{template-with-filename|be}}",
			...
			}
		}
	...
	}
}
```
