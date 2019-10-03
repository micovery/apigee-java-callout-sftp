// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.apigee.edgecallouts;


import com.apigee.flow.execution.ExecutionContext;
import com.apigee.flow.execution.ExecutionResult;
import com.apigee.flow.execution.spi.Execution;
import com.apigee.flow.message.Message;
import com.apigee.flow.message.MessageContext;
import com.google.apigee.edgecallouts.util.Debug;
import com.google.apigee.edgecallouts.util.VarResolver;
import com.jcraft.jsch.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class SFTPCallout implements Execution {

  public static final String CALLOUT_VAR_PREFIX = "jsch-ftp";
  public static final String SFTP_USER_PROP = "username";
  public static final String SFTP_PASS_PROP = "password";

  public static final String SFTP_HOST_PROP = "host";
  public static final String SFTP_PORT_PROP = "port";

  public static final String SFTP_FILEPATH_PROP = "file-path";
  public static final String SFTP_FILENAME_PROP = "file-name";
  public static final String SFTP_FILE_CONTENT_PROP = "file-content";

  public static final String MESSAGE_VAR_PROP = "message-variable-ref";
  public static final String VERB_PROP = "verb";
  public static final String PATH_PROP = "path";
  public static final String RESOURCE_PROP = "resource";

  public static final String X_AMZ_CONTENT_SHA256 = "x-Amz-content-sha256";
  public static final String X_AMZ_DATE = "X-Amz-Date";
  public static final String AUTHORIZATION = "Authorization";
  public static final String HOST = "Host";


  public static void upload(String content,
                     String filePath,
                     String fileName,
                     String username,
                     String password,
                     String host,
                     Integer port) throws JSchException, SftpException {
    JSch jsch = new JSch();
    Session session = jsch.getSession(username, host, 22);
    session.setPassword(password);
    session.setConfig(new java.util.Properties() {{
      put("StrictHostKeyChecking", "no");
    }});
    session.connect();

    Channel channel = session.openChannel("sftp");
    channel.connect();

    ChannelSftp channelSftp = (ChannelSftp) channel;
    channelSftp.cd(filePath);

    InputStream stream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
    channelSftp.put(stream, fileName);
    channelSftp.exit();
    session.disconnect();
  }


  private final Map properties;
  private ByteArrayOutputStream stdoutOS;
  private ByteArrayOutputStream stderrOS;
  private PrintStream stdout;
  private PrintStream stderr;

  public SFTPCallout(Map properties) throws UnsupportedEncodingException {
    this.properties = properties;
    this.stdoutOS = new ByteArrayOutputStream();
    this.stderrOS = new ByteArrayOutputStream();
    this.stdout = new PrintStream(stdoutOS, true, StandardCharsets.UTF_8.name());
    this.stderr = new PrintStream(stderrOS, true, StandardCharsets.UTF_8.name());
  }

  private void saveOutputs(MessageContext msgCtx) {
    msgCtx.setVariable(CALLOUT_VAR_PREFIX + ".info.stdout", new String(stdoutOS.toByteArray(), StandardCharsets.UTF_8));
    msgCtx.setVariable(CALLOUT_VAR_PREFIX + ".info.stderr", new String(stderrOS.toByteArray(), StandardCharsets.UTF_8));
  }

  public ExecutionResult execute(MessageContext messageContext, ExecutionContext executionContext) {
    try {

      VarResolver vars = new VarResolver(messageContext, properties);
      Debug dbg = new Debug(messageContext, CALLOUT_VAR_PREFIX);

      Boolean debug = vars.getProp("debug", Boolean.class, false);

      String host = vars.getRequiredProp(SFTP_HOST_PROP,"");
      Integer port = vars.getProp(SFTP_PORT_PROP, Integer.class, 22);

      String username = vars.getRequiredProp(SFTP_USER_PROP,"");
      String password = vars.getProp(SFTP_PASS_PROP, String.class, "");

      String fileName = vars.getProp(SFTP_FILENAME_PROP);
      String filePath = vars.getProp(SFTP_FILEPATH_PROP);
      String fileContent = vars.getProp(SFTP_FILE_CONTENT_PROP);

      if (debug) {
        dbg.setVar(SFTP_HOST_PROP, host);
        dbg.setVar(SFTP_PORT_PROP, Integer.toString(port));
        dbg.setVar(SFTP_USER_PROP, username);
        dbg.setVar(SFTP_FILEPATH_PROP, filePath);
        dbg.setVar(SFTP_FILENAME_PROP, fileName);
      }

      upload(fileContent,filePath,fileName, username, password, host, port);
      return ExecutionResult.SUCCESS;

    } catch (Error | Exception e) {
      e.printStackTrace(stderr);
      return ExecutionResult.ABORT;
    } finally {
      saveOutputs(messageContext);
    }
  }
}