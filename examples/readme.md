Running the examples from the command line
------------------------------------------

You need to build the examples using maven:

    mvn clean install -pl examples -am
    ./examples/target/hawt-app/bin/run.sh <example name> <example arguments>
        

Build Example:
--------------

    ./examples/target/hawt-app/bin/run.sh ImageBuildExample http://localhost:2375 myimage /path/to/image docker.io myaccount