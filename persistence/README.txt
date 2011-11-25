Long Term Persistence for JavaBeans, version 1.0
------------------------------------------------

For information about the design and implementation of this package 
and the persistence model, see the JCP website at: 

    http://java.sun.com/jcp    

and the Swing Connection Articles at:    

    http://java.sun.com/products/jfc/tsc

The new streams:  

	XMLEncoder and XMLDecoder 

are analogs of the ObjectOutputStream and ObjectInputStream that are used 
by the serialization framework introduced in JDK1.1 and work the same way.  
       
Included with this distribution is an example class called 
TestInput.java that deserializes the file whose name is passed 
to it as the first argument. This implementation of the persistence 
APIs now uses the JAX parser. To use it with J2SE v 1.3 you need to 
download jaxp.jar and parser.jar from java.sun.com/xml. To run the 
the example using Sun's J2SE v1.3, add the archiver and parsing 
jars to the bootclasspath and start the java VM in the usual way. 

For example:   

  C:\jdk1.3\bin\java.exe -Xbootclasspath:archiver.jar;C:\Java\jars\parser.jar;C:\Java\jars\jaxp.jar;C:\jdk1.3\jre\lib\rt.jar TestInput TestInput.xml


(in fact, TestInput.xml is the default filename so this argument 
is optional in this case). 

