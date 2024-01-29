# mvn-send-usage-infos

**mvn-send-usage-infos** is a custom Maven Mojo to send data from project and referenced artifacts (group id, artifact id, version, licenses) to a REST endpoint.

Data will be sent in JSON format without pretty-printing.

The structure will look like this (this example is indented and formatted for better readability):
```json
{
  "projectArtifact": {
    "groupId": "some.group.id",
    "artifactId": "the-artifact-id",
    "version": "1.0.0"
  },
  "referencedArtifacts": [
    {
      "groupId": "some.group.id",
      "artifactId": "artifact-one",
      "version": "1.0.1",
      "scope": "compile",
      "licenses": [
        {
          "name": "The Apache Software License, Version 2.0",
          "url": "http://www.apache.org/licenses/LICENSE-2.0.txt"
        }
      ]
    },
    {
      "groupId": "some.group.id",
      "artifactId": "artifact-two",
      "version": "2.0.6",
      "scope": "compile",
      "licenses": [
        {
          "name": "The Apache License, Version 2.0",
          "url": "http://www.apache.org/licenses/LICENSE-2.0.txt"
        }
      ]
    }    
  ]
}
```

## pre-requisits
As **mvn-send-usage-infos** is currently not available on Maven Central, you need to make sure that it is available to whatever system you're going to use it in your build.

### local builds
You need to clone the repository of **mvn-send-usage-infos** and then install it locally by `mvn clean install`.

### server-side builds (with custom Maven hosting)
First, you need to clone and locally build **mvn-send-usage-infos**, then you need to deploy it to your custom Maven hosting (e.g. Nexus).

While you might have configured your custom Maven hosting to be accessible via your Maven configuration, this might not include a custom Maven hosting location for plugins. So, make sure that `pluginRepositories` in your Maven configuration file includes your custom Maven hosting.

## usage

To execute the Mojo in your build process, it needs to be configured in the `build`/`plugins` section, like this:
```xml
<plugin>
    <groupId>io.github.nilscoding.maven</groupId>
    <artifactId>mvn-send-usage-infos</artifactId>
    <version>1.0.0</version>
    <executions>
        <execution>
            <phase>compile</phase>
            <goals>
                <goal>send-usage-infos</goal>
            </goals>
            <configuration>
                <urlLocation>http://some-server/some/endpoint</urlLocation>
            </configuration>
        </execution>
    </executions>
</plugin>
```

Make sure that you reference the correct version. Also, you need to specify at least the `urlLocation` value, see configuration description below.

Also, you might want to check the [default Maven lifecycle documentation](https://maven.apache.org/guides/introduction/introduction-to-the-lifecycle.html#default-lifecycle) to find the appropriate phase in which you want to execute the Mojo.

## configuration options

### urlLocation
The `urlLocation` configures the URL where the data should be sent to.
The HTTP client that is used for the call will ignore SSL errors, so data can be sent to a server with a self-signed certificate.

### urlMethod (optional)
By default, the HTTP method `POST` will be used. This can be changed with the `urlMethod` configuration option. However, this only affects the used HTTP method verb and the payload will always be transferred in the request body.

### urlAuthHeaderValue (optional)
If the endpoint requires authentication, then the configuration option `urlAuthHeaderValue` can be used to send an `Authorization` header. The value must include the complete header value, so you can define Bearer token, basic auth or any other value here.

## copyright / license

**mvn-send-usage-infos** is licensed under the MIT License, for more details see license.md