

# Requirements #

All sub projects of _visualDependencies_ use the Java 6 SDK. The build management is done with Maven 2. The projects are built-with Eclipse ones, so Eclipse users will have no problems importing the project as a standard Java/Maven project.

Note: We cannot ship the required ojdbc.jar directly in our source code repository due license issues. See here: OracleJdbcDriver.

# Checkout #
## Subversion URL ##
The source code project (latest) is located at http://code.google.com/p/visualdependencies/source/browse/#svn/vdb-release-2009/trunk.

## Details for Eclipse (recommended) ##

Note: You have to install the [m2eclipse](http://m2eclipse.sonatype.org/) plugin. This is the de-facto standard Maven plugin for Eclipse. This will provide a built-in Maven, too.

In Eclipse 3.6 (current release), import a new projekt using Eclipse's Import from SVN feature. For SVN information, see the tab [Source](http://code.google.com/p/visualdependencies/source/list) at the top.

That's it. Build a new jar artifact or just start the Main.java.

## Details for command line/no Build-Management-IDE ##

Note: You have to install Maven2 and make it available in your path environment. Unfortunately, we cannot provide a full Maven install guide.

Just checkout the code from the SVN. For SVN information, see the tab [Source](http://code.google.com/p/visualdependencies/source/list) at the top.

That's it. Build a new jar artifact via mvn package and start the jar.

# Build #
## One-Jar ##
One-Jar artifacts are built-in now. In general, you find the appropriate jar near the normal jar. For example, the normal artifact build.jar the one-jar's name is called build.one-jar.jar.