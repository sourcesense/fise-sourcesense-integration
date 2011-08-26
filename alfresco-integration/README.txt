##### SUMMARY #####

The FISE Alfresco integration is based on an AMP module and it is consists of two Alfresco components:

1. A custom behaviour named iksFiseEnrichBehaviour

2. A custom action named iks-fise-enrich-action

You can choose to use one of these components that are declared together in the Spring context by default.


##### FISE CUSTOM BEHAVIOUR #####

The custom behaviour is based on the OnCreateNode policy, this means that it will be executed every time 
that a node will be stored in the repository.

By default this behaviour is enable so, if you don't need to use it you have to disable it commenting 
its Spring definition in the Spring context (module-context.xml) in the following way:

1.	Change the Spring bean definition in the src/main/config/module-context.xml from this:

<!-- IKS - FISE custom behaviour that will start for each new content in Alfresco -->
<bean id="iksFiseEnrichBehaviour" 
      class="com.sourcesense.iksproject.enhance.alfresco.policies.FISEExtractAndEnrichContentPolicy"
      init-method="init">
	<property name="iksFiseAlfrescoBl" ref="iksFiseAlfrescoBlForBehaviour"/>
	<property name="contentService" ref="contentService"/>
	<property name="policyComponent" ref="policyComponent"/>
</bean>

To this:

<!-- IKS - FISE custom behaviour that will start for each new content in Alfresco -->
<!--
<bean id="iksFiseEnrichBehaviour" 
      class="com.sourcesense.iksproject.enhance.alfresco.policies.FISEExtractAndEnrichContentPolicy"
      init-method="init">
	<property name="iksFiseAlfrescoBl" ref="iksFiseAlfrescoBlForBehaviour"/>
	<property name="contentService" ref="contentService"/>
	<property name="policyComponent" ref="policyComponent"/>
</bean>
-->

2.	Build and package the AMP

3.	Stop the Alfresco instance.

4.	Deploy the AMP in the Alfresco web application using the Alfresco MMT module or simply using Maven (mvn clean package -P webapp)

5.	Restart Alfresco using the command "./alfresco.sh start" or you can use Maven to test your configuration (mvn clean integration-test -P webapp).


##### FISE CUSTOM ACTION #####

The custom action can be assigned to any rule on spaces using the Alfresco Explorer.
To configure the custom action you have to navigate your space where you want to run this action and then you have to:

-	click on More Actions -> View Details -> Manage Content Rules

-	click on Create Rule and follow these three steps below:

	-	In this first step you have to select WHICH contents will be involved in the rule 
		using the condition for the scope of the rule for FISE.
		For example select "All items" and press the button "Set Values and Add".
		When you have finished to add all the other potential conditions press the Next button.
	
	-	In this second step you have to add all the ACTIONS that you want to execute for this rule, 
		for example now we want to add only the "IKS FISE Enrich Content Action", then select it and press the button "Set Values and Add".
	
	-	In the last step you have to set WHEN you want to run this rule (Inbound, Outbound, Update).
		Inbound means that the rule will be executed when a new content will be stored in the space.
		Outbound means that the rule will be executed when a content will be removed or moved from the space.
		Update means that the rule will be executed when a content will be updated in the space.
		
		Then you need to set a title for the rule and if apply the rule to all the sub spaces.
		Another setting is "Run rule in background" that will be run the rule using a separated thread, 
		in this way the user session will be set free but remember that for each request of this type will be created a new thread.
		Take care about the threadpool in your application server!
		
		Finally you can press the Finish button to save the new rule with the FISE-Alfresco integration.
	

To correctly configure and deploy the FISE integration in Alfresco from the source code you have to execute the following steps:

1.	Create and configure the dir.root (this is the filesystem directory of the repository)
	By default the folder is set to /Applications/alfresco/iks (following the Mac OS X filesystem).
	If you need to change this folder you will find a property in the pom.xml named alfresco.data.location.

2.	Create a new database for the repository, for example, if you want to run Alfresco on MySQL you have to 
	run the script that you will find here:
	
	/alfresco-integration/scripts/mysql/db_setup.sql
	
	By default this script will create a new database named iks with a new user iks identified by the password iks.
	
	Remember to enable UTF-8 encoding when you are creating your database using your own DBMS.
	For specific information about other DBMS setup in Alfresco please visit the official wiki of Alfresco:
	http://wiki.alfresco.com

3.	Configure the database for the repository using the following maven properties in the pom.xml:
	- alfresco.db.name (default value = iks)
	- alfresco.db.username (default value = iks)
	- alfresco.db.password (default value = iks)


##### HOW TO RUN THE ALFRESCO-FISE INTEGRATION IN AN ALFRESCO INSTANCE USING JETTY #####

This section is tested on Alfresco 3.2r Community.

To build, package and deploy the FISE AMP using one command you can use the following quickstart:
$MAVEN_OPTS="-Xms256m -Xmx512m -XX:PermSize=128m" mvn  clean integration-test -P webapp 

This command will run the jetty embedded with overlayed the currently FISE AMP.

Every time that you run this maven command the Jetty server will return an error, but if you try to change the pom.xml, 
for example adding a blank space and then you try to save it, the Jetty deployer will find the Spring loader and 
Alfresco will start correctly.

Alfresco will be available at the following URL:
http://localhost:8080/alfresco-integration-webapp/

To debug with jetty you have to add the following arguments:
-Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,address=4000,server=y,suspend=y

Then you can debug using the port 4000 on your local machine, change the address parameter if you need to set another port.

Other features:
- AMP customized build : 										 mvn clean package
- AMP dependencies management: 									'mvn clean package' can take care of overlay deps
- Alfresco webapp integration via war creation					 mvn clean package -P webapp
- jetty embedded build for fast testing,						 mvn clean integration-test -P webapp


##### HOW TO BUILD AND DEPLOY THE FINAL ARTIFACT IN ALFRESCO #####

Running the following Maven command:

mvn clean package

You will find the alfresco-integration.amp in the target folder of the project, to install it in Alfresco you can use:

1.	The Alfresco Module Management Tool (alfresco-mmt.jar)
2.	The Maven command integrated in this project but this means that you are building the project (mvn clean package -P webapp)
3.	If you are using the Alfresco Tomcat bundle, you can copy the alfresco-integration.amp in the alfresco-tomcat-bundle/amps folder. 
	And then you can run the command ./apply_amps.sh from the root of the Tomcat bundle to install the FISE AMP module in the alfresco.war.
