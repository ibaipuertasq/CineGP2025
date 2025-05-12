CINE DEUSTO
============================

This example relies on the DataNucleus Maven plugin. Check the database configuration in the *datanucleus.properties* file and the JDBC driver dependency specified in the *pom.xml* file.

Run the following command to build everything and enhance the DB classes:

      mvn clean compile

Make sure that the database was correctly configured. Use the contents of the file *create-productsdb.sql* to create the database and grant privileges. For example,

      mysql –uroot -p < sql/cineDB.sql

therfore, execute the following command to enhance the database classes

      mvn datanucleus:enhance

Run the following command to create database schema for this sample.

      mvn datanucleus:schema-create

Run the following command to launch the main example class 
   
      mvn exec:java

Run the following command to remove the database schema
   
      mvn datanucleus:schema-delete

To launch the server run the command

      mvn jetty:run

To launch the JUnits run the commnad

      mvn test
          or
      mvn clean test

Pruebas de Integración:

      mvn clean verify -Pintegration-tests

Pruebas de Rendimiento:

      mvn clean verify -Pperformance-tests

To launch the web client open the link:

http://localhost:8080

If you want to see the documentation cretaed by doxygen, run the following command:

      mvn  doxygen:report