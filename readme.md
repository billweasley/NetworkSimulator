### Directly run the programme:
- Require Java Runtime 1.8
- Click and run the file called "org.net.simulator-1.0-SNAPSHOT.jar" in root folder.
- Suggested resolution: 1920 x 1080  
**Note: because currently the UI do not support resize,
this is most appreciated resolution.**



### Compilation dependency:
Please first ensure the following software normally running on your computer:

+ JDK 1.8
+ IntelliJ IDEA (latest version) with gradle
+ Kotlin 1.2
+ TornadoFX 1.7.15
+ (Optional but recommend) TornadoFX PlugIn For IntelliJ IDEA  


Dependency Shown as in **build.gradle**, if you could
open the project folder *NetworkSimulator* in IntelliJ IDEA with gradle,
then it should loaded all required dependency automatically, which includes following...  

---
+ JUnit 4
+ Koma 0.11+
+ GraphStream 1.3 (included in the lib file)  
+ TornadoFX 1.7.15

If you have TornadoFX PlugIn installed with you, you can compile and run the program use "application\run" command located in the right-hand side of your gradle panel.  

If you do not going to install the TornadoFX PlugIn, you can then run "main" method in
src/main/kotlin/presentation/ui/SimulatorApp.kt.   
The method by default is
commented, please uncomment it if you want to run it.

### Some main references:  

```  

@article
{
 AspnesR2007,
 author = {James Aspnes and Eric Ruppert},
 title = {An introduction to population protocols},
 journal = {Bulletin of the European Association for Theoretical Computer Science},
 volume=93,
 pages={98--117},
 month=oct,
 year = 2007
}


@article{
 MS16a,
 author = {Michail, Othon and Spirakis, Paul G.},
 title = {Simple and efficient local codes for distributed stable network construction},
 journal = {Distributed Computing},
 volume = {29},
 number = {3},
 pages = {207--237},
 year = {2016},
 issn = {0178-2770},
 doi = {http://dx.doi.org/10.1007/s00446-015-0257-4},
 url = {https://link.springer.com/article/10.1007/s00446-015-0257-4},
 publisher = {Springer Berlin Heidelberg}
}




@article{
 Mi17,
 author = {Michail, Othon},
 title = {Terminating distributed construction of shapes and patterns in a fair solution of automata},
 journal = {Distributed Computing},
 volume = {},
 number = {},
 pages = {1--23},
 year = {2017},
 issn = {0178-2770},
 doi = {http://dx.doi.org/10.1007/s00446-017-0309-z},
 url = {http://link.springer.com/article/10.1007/s00446-017-0309-z},
 publisher = {Springer Berlin Heidelberg}
}

```
