# FMPMM: Flexible Manufacturing Process Mechanism Model for process execution

## Project Structure Description
Below are brief explanations of some important directories in the project.
```
src/main/java/edu/hit/fmpmm/  
├─config  // Some configuration classes
├─controller  // Some controller classes
├─domain
│  ├─exception
│  ├─neo4j  // Entity classes related to FMPKG
│  │  └─node
│  ├─sim
│  │  └─robot  // Related entity classes of robots
│  │      └─instance
│  └─web
├─dto
├─mapper
├─repo
├─server
├─service
│  ├─aapc
│  │  ├─aa  // Implementation code of action actuator
│  │  │  └─actions
│  │  └─pc  // Implementation code of parameter calculator
│  │      └─parameters
│  ├─process  // Implementation code for process execution
│  ├─sim
│  └─web
│      └─impl
├─util
└─FmpmmApplication.java  // Project initiation class
```
## Detailed Introduction to the Implementation of Parameter Calculator

- Under the `service/aapc/pc` directory, `ParameterCalculator` represents an abstract parameter calculator, in which the properties and methods that a parameter calculator should have are described abstractly. Both `ObjectParameterCalculator` and `RobotParameterCalculator` inherit from the `ParameterCalculator` class and are two categories of parameter calculators. `ParameterFactory` is a factory for producing parameter calculators.
- In the `service/aapc/pc/parameters` directory, there are several specific parameter calculators, which inherit from `ObjectParameterCalculator` or `RobotParameterCalculator` respectively according to their categories.
- In the `value()` method of the parameter calculator, the parameter values will be calculated according to the **process mechanism** and various matched **resources**.


## Detailed Introduction to the Implementation of Action Actuator

- Under the `service/aa` directory, `ActionActuator` is an abstract action actuator class. 
- The action actuator instances are placed in the `service/aa/actions` directory, and they all inherit from the `ActionActuator` class. 
- The action actuator instances have all implemented the abstract `go()` method, which represents the code for performing actions. Each `go()` method needs to pass in parameters according to the specific situation. 
- `ActionFactory` is a factory for producing action actuators.

## Usage Instructions
The interface that needs to be called for process execution is "sim/product".
If the IP address configured for the project is `1.2.3.4`, the following service needs to be accessed by the GET method for process execution. The parameter passed is the name of the assembly.

`http://1.2.3.4:8080/sim/product?obj=xxx`

## Explanation on Expanding Project Functionality

If you want to reproduce the code in different environments or expand the functions of the code, you just need to expand it according to the following ideas.

### FMPKG

Construct the process knowledge graph according to the structure of FMPKG.

### Robot Programming

According to the robot simulation software you are using, construct different types of robot entity classes and place the robot entity classes under the `domain/sim` package. And make the corresponding configuration in the `robot.properties`.

### Parameter Calculator

If a new type of parameter calculator is to be added, a new parameter calculator entity class should be constructed and inherit from `ObjectParameterCalculator` or `RobotParameterCalculator`, and then be placed under the `service/pc/parameters` package. And make the corresponding configuration in the `parameter.properties`.

### Action Actuator

You can write code for different action actuators according to the rules provided by the simulation software you are using. Call the robot's methods in the action actuator so that the implementation of the action execution can change according to different robots. Place the specific motion actuators under the `service/aa/actions` package and make the corresponding configuration in the `action.properties`.
