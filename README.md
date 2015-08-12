# Multi-Paxos implementation
##Overview
Paxos is a neat algorithm to solve the consensus problem. It was firstly described in <a href="http://www.lamport.org" target="_blank">Leslie Lamport's </a> paper (<a href="http://research.microsoft.com/en-us/um/people/lamport/pubs/paxos-simple.pdf" target="_blank">Paxos Made Simple</a>). Paxos is considered as one of the hardest algorithms to understand, however, I have to confess it is really a neat algorithm after I finally figured out how it works to avoid conflicts when different values are proposed at almost the same time from different clients and make all the nodes in a system agree with only one value. Here we assume that every node will just stop if it fails, and we don't need to worry about Byzantine failure. 

##Run the project using <a href="http://www.scala-sbt.org" target="_blank">SBT</a> (Simple Build Tool)
 * Install SBT on your computer.
  * For mac, you can install sbt using homebrew, from terminal, run command line: 
   ```
     ->brew install sbt
   ```
  * For more instructions about installing SBT on any type of OS, visit <a href="http://www.scala-sbt.org/release/tutorial/Setup.html" target="_blank">this link</a>
 * Clone the project from repo: https://github.com/allenfromu/Single-Decree-Paxos.git
 * Go to the Single-Decree-Paxos directory from terminal and then run command: 
 ``` 
 
   -> sbt run
 ```
**Note that if you want to run different nodes from one computer, you can just use the predefined paxos.config file without adding anything. In the predefined paxos.config, I set up a system with 5 nodes, you need to at least run 3 different nodes in order to try to propose a value into a system. If you decide to run the system within different computers, you need to modify the paxos.config file without changing anything else. I suggest a system with 3 or 5 nodes, that way the majority size of the system is 2 or 3. It is important that the information you put in the paxos.config should be in order and exactly in the right format. Each line in the paxos.config file carries the information of a unique node (id host port). All different nodes should share a same paxos.config, and their unique ids are included in the config file**
 * Once you run a node, you can propose a value from the console.
**e.g**
```
Select node id:1
->propose
value:hello world
node:1
->
```

##Import the project to eclipse
 * Download <a href="http://www.eclipse.org" target="_blank">Eclipse</a> into your computer if you don't have it installed.
 * Add sbteclipse to your plugin definition file (or create one if doesn't exist). You can use either:
  * The global file (for version 0.13 and up) at ~/.sbt/0.13/plugins/plugins.sbt
  * The project-specific file at PROJECT_DIR/project/plugins.sbt
<br>For the latest version:
```
addSbtPlugin("com.typesafe.sbteclipse" % "sbteclipse-plugin" % "4.0.0")

```
 * In the Single-Decree-Paoxs directory, run command:

```
->sbt eclipse
```
 * From the eclipse IDE, import the project to workspace, File->Import->Existing Projects into Workspace.
 * Note you might need to add Akka package into eclipse, to do so:
  * Download the latest version of <a href="http://akka.io/downloads/" target="_blank"> Akka Actor</a>
  * In eclipse, right click the Single-Decree-Paxos project,go Build Paths -> Add External Archives ...
 * Note that if you want to run it from eclipse, main function is in **src/main/scala/Impl/Main.scala**
 
##Supports and further improvement.
This is project can't be done without the support of my supervisors and advisors, <a href="http://www.cs.utah.edu/~ganesh/" target="_blank">Ganesh Gopalakrishnan</a> and Mohammed S. Al-Mahfoudh. 
<br>I am still working on it to make this project bettter, and I like to hear any advice to help make it better. Your advice is invaluable for me, please email me at allenzhaofromu@gmail.com if you think any aspect of the project can be made better.











