# SetOfAtomContainers.groovy
**Source code:**
```groovy
import org.openscience.cdk.silent.*;
import org.openscience.cdk.*;
set = new AtomContainerSet()
println "This set has $set.atomContainerCount containers"
anAtomContainer = new AtomContainer()
anotherAtomContainer = new AtomContainer()
set.addAtomContainer(anAtomContainer)
set.addAtomContainer(anotherAtomContainer)
println "Now it has $set.atomContainerCount containers"
```
**Output:**
```plain
This set has 0 containers
Now it has 2 containers
```