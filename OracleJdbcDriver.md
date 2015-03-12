The Oracle JDBC driver is not part of the project nor the oracle plugin.

# Problem #
Due license issues, we cannot provide a built-in driver support. Because of this, Oracle provides no official maven dependency support for this jar files.

# Solution for "vdb-release-2009" #
You can download a suitable file at http://www.oracle.com/technetwork/database/enterprise-edition/jdbc-10201-088211.html -- for example the ojdbc14.jar.

In order to build a jar file **with built-in oracle support**:
  1. Make sure that the actual build was successful. The directory _target_ should contain at least two artifacts: visualDependencies.jar and visualDependencies.one-jar.jar
  1. Download a suitable ojdbc jar file and place it the directory /oracle/lib.
  1. Perform the ant script at /src/main/scripts/build-with-ojdbc.xml. The default parameters require an ojdbc14.jar at /oracle/lib/ojdbc14.jar and the one-jar artifact.

Note: Because you want a single, executable jar-file, the non-onejar-file is not usable.

Note: The test cases will fail at the moment because they were crerated against a real databse. This database is not available any more.

So, in short:
  1. Download and save the ojdbc14 at /oracle/lib/ojdbc14.jar
  1. mvn clean package _or better_ mvn clean package -Dmaven.test.skip=true
  1. ant -buildfile src/main/scripts/build-with-ojdbc.xml attachOJDBC