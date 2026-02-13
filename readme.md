# mvn-send-usage-infos

**mvn-send-usage-infos** is a custom Maven Mojo to send data from project and referenced artifacts (group id, artifact id, version, licenses, name, description, website URL, packaging, dependency trail) in JSON format to a URL endpoint or save them to a file.

Data will be sent in JSON format, no pretty-printing will be done for sending to URL endpoints, pretty-printing is enabled for writing to a file, but behaviour in both cases can be manually controlled.

The structure will look like this.

```json
{
  "projectArtifact": {
    "groupId": "some.group.id",
    "artifactId": "the-artifact-id",
    "version": "1.0.0",
    "packaging": "jar",
    "name": "The Artifact",
    "description": "Some Artifact",
    "websiteUrl": "https://github.com/"
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
      ],
      "name": "Artifact One",
      "description": "One Artifact",
      "websiteUrl": "https://github.com/"
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
      ],
      "name": "Artifact Two",
      "description": "Other Artifact",
      "websiteUrl": "https://github.com/"
    }    
  ]
}
```

Since Mojo version 1.1.0 `name`, `description` and `websiteUrl` will be included if available.

Since Mojo version 1.2.0 `packaging` and `dependencyTrail` will be included if available.

## additional notes

### the `dependencyTrail`

If available (mostly for referenced artifacts), the dependency trail will have at least two elements:
- The first element will refer to the `projectArtifact`.
- The last element will be the (referenced) artifact itself.

So, if the dependency trail contains exactly 2 elements, it's a direct dependency.
If it contains 3 or more elements, then it's an indirect dependency.
This can be checked by using `Artifact.isDirectDependencyOf(otherGatv)` when using the data classes in custom code.

Dependency trail elements will be in GATV format.

### the artifact's GATV

The `Artifact` class has a `getGatv()` method which returns the artifact coordinates, including the type.
This format is often referred to as GATV.

This method is useful if you use the data classes from this project to load the generated JSON file and process the dependency trails,
because its values are the artifact coordinates given GATV format.

### multiple output targets

Since Mojo version 1.2.0 you can specify multiple lines in `urlLocation` to save/send the data to multiple targets at once.
Targets must not be of the same type, so you can both save the data to a local file, while also sending it to a REST endpoint.
When using multiple targets, make sure to cleanly separate them with new-lines (splitting will be done via regex `[\n|\r]`) and each entry will be trimmed.
Also, empty lines will be ignored.

### special output target `@logging`

Since Mojo version 1.2.0 you can output the data to Maven build logging by specifying `@logging` in `urlLocation`.
The output will be written in pretty-printing using `INFO` logging. Alternatively, you can specify `@logging:warn`, `@logging:error` or `@logging:debug` for a different log type.
Appending `(single-line)` will suppress pretty-printing and output the data in one line. Please note that this option only works for logging target.

## pre-requisits
As **mvn-send-usage-infos** is currently not available on Maven Central, you need to make sure that it is available to whatever system you're going to use it in your build.

### local builds
You need to clone the repository of **mvn-send-usage-infos** and then install it locally by `mvn clean install`.

### server-side builds (with custom Maven hosting)
First, you need to clone and locally build **mvn-send-usage-infos**, then you need to deploy it to your custom Maven hosting (e.g. Nexus).

While you might have configured your custom Maven hosting to be accessible via your Maven configuration, this might not include a custom Maven hosting location for plugins. So, make sure that `pluginRepositories` in your Maven configuration file includes your custom Maven hosting.

## usage

### during build process, specified in project pom

To execute the Mojo in your build process, it needs to be configured in the `build`/`plugins` section, like this:
```xml
<plugin>
    <groupId>io.github.nilscoding.maven</groupId>
    <artifactId>mvn-send-usage-infos</artifactId>
    <version>1.2.0</version>
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

Make sure that you reference the correct version. Also, you need to specify at least the `urlLocation` value, see configuration description below. The value for `urlLocation` can be specified via a placeholder variable.

Also, you might want to check the [default Maven lifecycle documentation](https://maven.apache.org/guides/introduction/introduction-to-the-lifecycle.html#default-lifecycle) to find the appropriate phase in which you want to execute the Mojo.

### invoked as a goal on a project

If you don't want to include the data in the pom file, you can install the plugin locally and invoke it on any project like this (from project folder, required pom.xml):

```bash
mvn io.github.nilscoding.maven:mvn-send-usage-infos:1.2.0:send-usage-infos -DurlLocation="http://some-server/"
```

## configuration options

### urlLocation
The `urlLocation` configures the target where the data should be sent to.

In Mojo version 1.0.0 you must specify a web URL (starting with `http://` or `https://`).

Since Mojo version 1.1.0 you can alternatively specify a `file://`, or leave out the protocol completely to write to a local file.
You can either specify a complete file name or just point to a directory (which will then write a file `usage-infos.json` there).
For security reasons, existing files will not be overwritten, so you need to manually delete them before (or clean them during the build).

The HTTP client that is used for the call will ignore SSL errors, so data can be sent to a server with a self-signed certificate.

### urlMethod (optional)
By default, the HTTP method `POST` will be used. This can be changed with the `urlMethod` configuration option. However, this only affects the used HTTP method verb and the payload will always be transferred in the request body. This option is only used for URL endpoints.

### urlAuthHeaderValue (optional)
If the URL endpoint requires authentication, then the configuration option `urlAuthHeaderValue` can be used to send an `Authorization` header. The value must include the complete header value, so you can define Bearer token, basic auth or any other value here.  This option is only used for URL endpoints.

### prettyPrint (optional)
This optional boolean flag (value `true` or `false`) controls if the JSON data should be pretty-printed or not.

By default, JSON being sent to a web URL will not be pretty-printed, but JSON in file output will be pretty-printed.

## copyright / license

**mvn-send-usage-infos** is licensed under the MIT License, for more details see license.md