While developing for the eXist Native XML database (http://www.exist-db.org) I needed some additonal Ant Tasks. Since I could not find them on the internet, I decided to write them myself.....

[![](http://www.exist-db.org/logo.jpg)](http://www.exist-db.org/)

What can you expect:
  * unsign jar files
  * repack (normalize) jar files (so they can be signed and packed using Pack200)
  * Pack200 tasks

and

  * SVN tasks, e.g. to determine revision.

You'll need Java5+ to compile it, the tasks are developed for Apache Ant 1.7.1.

As expected, an Ant build.xml file is provided. The code has been developped with Netbeans 6.5 (nbproject folder is provided as well)

Note; the jUnit jar must be installed, either in your ant install or in your favourite IDE.......


# Example code #

from eXist-db.org sources:

### shared ###
```
    <!-- additional set of ant tasks -->
    <property name='asocat-exist.jar' location='${tools.ant}/lib/asocat-exist.jar'/>
    <property name='javasvn.jar' location='${tools.ant}/lib/svnkit-1.1.0.jar'/>
    <available file='${javasvn.jar}' property='svn-present' />
```

### unsign ###
```
    <target name='jnlp-unsign-all'>
        <taskdef name='unsignjar' 
                 classname='nl.ow.dilemma.ant.jar.UnsignJarTask' 
                 classpath='${asocat-exist.jar}'/>
        <unsignjar>
            <fileset dir='.'>
                <include name='exist*.jar'/>
                <include name='start.jar'/>
            </fileset>
            <fileset dir='lib/core'>
                <include name='*.jar'/>
            </fileset>
        </unsignjar>
    </target>
```

### (re)pack ###
```
   <target name='jnlp-pack200'>
        <!-- First remove all signatures from the jar files -->
        <antcall target='jnlp-unsign-all'/>

....
        
        <!-- Secondly pack-unpack the jar files (normalize) -->
        <taskdef name='repack' 
                 classname='nl.ow.dilemma.ant.jar.RepackJarTask' 
                 classpath='${asocat-exist.jar}'/>
        <repack>
            <fileset dir='.'>
                <include name='exist*.jar'/>
                <include name='start.jar'/>
            </fileset>
            <fileset dir='lib/core'>
                <include name='*.jar'/>
            </fileset>
        </repack>
        
        <!-- Sign all jars -->
        <antcall target='jnlp-sign-exist'/>
        <antcall target='jnlp-sign-core'/>
        
        <!-- Create jar.pack.gz files -->
        <taskdef name='pack' 
                 classname='nl.ow.dilemma.ant.jar.Pack200Task' 
                 classpath='${asocat-exist.jar}'/>
        <pack>
            <fileset dir='.'>
                <include name='exist*.jar'/>
                <include name='start.jar'/>
            </fileset>
            <fileset dir='lib/core'>
                <include name='*.jar'/>
            </fileset>
        </pack>
    </target> 
```

### SVN ###
```
  <available classname="org.tmatesoft.svn.cli.SVN" classpath="classpath.core" property="svn-present"/>

  <target name="svn-prepare" if="svn-present">
    <echo>Determining local SVN revision</echo>
    <taskdef name="svninfo" classname="nl.ow.dilemma.ant.svn.SubversionInfoTask">
      <classpath refid="classpath.core"/>
    </taskdef>

    <tstamp/>

    <svninfo />

    <echo>Updating VERSION.txt</echo>
    <propertyfile file="VERSION.txt" comment="build info (updated using svnkit)">
      <entry key="project.version" value="${project.version}"/>
      <entry key="project.build" value="${DSTAMP}"/>
      <entry key="svn.revision" value="${svn.revision}"/>
    </propertyfile>
  </target>

```

### Fetch ###

the following example downloads a zip file and extracts the content specified by the patterns in to the dest directory. The original structure is flattened.
If 'patternset' is left out, the specified is downloaded and stored in 'dest'

```
        <fetch classpathref="classpath.core" dest="${top.dir}/${lib.user}
		url="${include.module.jfreechart.url}" classname="org.jfree.chart.JFreeChart" usecache="true">
            <patternset>
                <include name="**/lib/jfreechart-*.jar"/>
                <include name="**/lib/jcommon-*.jar"/>
                <exclude name="**/lib/jfreechart-*-*.jar"/>
            </patternset>
        </fetch>
```

supported attributes:
  * failonerror="true|false" --> stop on error or always continue
  * maxtime="#"  --> (nr of seconds, default is 0)
  * useCache = "true| false --> "true" will store a copy into ~/.fetch
  * classname="...." --> check if the Class exists, if not download from URL
  * url="..."  --> the location of the resource
  * dest="..." --> location to store files


Fetch is built on top of standard Ant tasks like "Get", "Expand", "Available" and inherits features like http-proxy support.


# Links #
  * [Pack200 and Compression](http://java.sun.com/j2se/1.5.0/docs/guide/deployment/deployment-guide/pack200.html)
  * [Compression and Signing](http://wiki.eclipse.org/index.php/Pack200#Compression_and_Signing)
  * [java-pack200-ant-task](https://java-pack200-ant-task.dev.java.net/) (not maintained)