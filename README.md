## Connecting to SFTP server from Apigee

This repo shows how to upload files to an sftp server with the [JSch](http://www.jcraft.com/jsch/) library using Apigee's Java Callout policy. 
This is meant to serve as an example. It's not recommended that you use this code in a production environment.

The sample code uses simple username/password authentication to login to the SFTP server. If you want to make this
Java Callout production-grade consider extending to use more advanced JSch features such as public key authentication
as well as enabling strict host checking. 



## How it works

The Java Callout policy takes all its input as Properties. The value of the properties can be 
either a literal string hardcoded on the policy itself, or a flow variable. To use a flow variable
use the following pattern: `{flowVar}` . Here are the properties supported by this Java Callout:

* **host** (required, String) - The hostname for the SFTP server
* **port** (optional, Integer, default: 22) - The port number for the SFTP server
* **username** (required, String) - This is the SFTP username
* **username** (optional, String, default: "") - This is the SFTP password
* **file-path** (required, String) - Directory where to upload the file to
* **file-name** (required, String) - Name for the file to be uploaded
* **file-content** (required, String) - Text contents for for the file to be uploaded


### Pre-built distribution

You can find the pre-built jar file for the Java Callout in the dist/ directory.


### Using in Apigee

Here is a sample XML of the Java Callout policy with some hardcoded values:


```xml
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<JavaCallout async="false" continueOnError="false" enabled="true" name="JC-SFTPUpload">
    <DisplayName>JC-SFTPUpload</DisplayName>
    <Properties>
        <Property name="debug">true</Property>
        <Property name="username">sftpuser</Property>
        <Property name="password">SuperSecret#123!</Property>
        <Property name="host">34.11.234.12</Property>
        <Property name="port">22</Property>
        <Property name="file-path">/some/dir</Property>
        <Property name="file-name">newfile.txt</Property>
        <Property name="file-content">Hello from Apigee!</Property>
    </Properties>
    <ClassName>com.google.apigee.edgecallouts.SFTPCallout</ClassName>
    <ResourceURL>java://edge-callout-sftp.jar</ResourceURL>
</JavaCallout>
```

In the example above, the contents of the file to be uploaded is hardcoded as "Hello from Apigee!".
Ideally, this would come from the contents of a request object. You could do this by using `{request.content}`.

Also, the username and password should not be hardcoded. The best practice is to store these in an Apigee
encrypted [Key-Value-Map](https://docs.apigee.com/api-platform/reference/policies/key-value-map-operations-policy).


### Build Prerequisites


  * [Maven 3.6.1 or later](https://maven.apache.org/download.cgi)
  * [Java SE 9 or later](https://www.oracle.com/technetwork/java/javase/downloads/index.html)
  * bash (Linux shell)
  * cURL
  

### Building it


If you want to build the Java Callout yourself, follow these instructions.

First, we will run the `buildsetup.sh` script to download Apigee's Java Callout libraries:

```bash
$ ./buildsetup.sh
```

This script downloads a couple of JAR files and installs them in maven.

Then, we need to compile and package the actual Java Callout code:

```bash
$ cd callout
$ mvn package
```

Once this is done you will see a new jar file  "edge-callout-sftp.jar" within the target directory. 
That is the build output.


### Not Google Product Clause

This is not an officially supported Google product.
