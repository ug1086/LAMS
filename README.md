# LAMS
A Laboratory Appointment Management System developed using Java

To run the project-
- install glassfish5 (full)
- run it by cmd(ing) into (e.g. your\path\to\glassfish5\glassfish\bin>asadmin start-domain)
- run localhost:4848 on the browser and go to Applications tab
- deploy the war file and click on the start button
- click on the deployed war file (e.g. GargUP2) and click launch
- copy the first link and add the path (e.g. /resources/Services/Appointments) based on the ApplicationConfig and your Service classes.
- use the URL on Soap UI or Postman or other client.


To generate the war file:
- jvf yourLastNameSoapService.war WEB-INF from the directory just above the WEB-INF directory. (using cmd)
