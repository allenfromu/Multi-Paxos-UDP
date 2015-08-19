# Multi-Paxos implementation
##Overview
Multi-Paxos is one of the use cases of Paxos. I had previously implemented and published the Single-Decree-Paxos in <a href="https://github.com/allenfromu/Single-Decree-Paxos" target="_blank">this Repo</a> before I implemented multi-paxos. It is important for users to understand the Single-Decree-Paxos before trying to understand multi-paxos. This project was built initially as one of the distributed system benchmarks to help with the DS2 lang research project in the <a href="http://formalverification.cs.utah.edu" target="_blank">Gauss Group</a> at the <a href="http://www.cs.utah.edu" target="_blank">SOC University of Utah.</a> The code is less and simpler thanks to an extremely advanced language Scala and the Akka Actor package, users can definely run this project to help understand how paxos and multi-paxos works.

##Run the project using <a href="http://www.scala-sbt.org" target="_blank">SBT</a> (Simple Build Tool)
 * Install SBT on your computer.
  * For mac, you can install sbt using homebrew, from terminal, run command line: 
   ```
     ->brew install sbt
   ```
  * For more instructions about installing SBT on any type of OS, visit <a href="http://www.scala-sbt.org/release/tutorial/Setup.html" target="_blank">this link</a>
 * Clone the project from repo: https://github.com/allenfromu/Multi-Paxos-UDP.git
 * Go to the Multi-Paxos directory from terminal and then run command: 
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
By doing above, you ask node 1 to propose value 'hello world' from node 1.
##Import the project to eclipse
 * Download <a href="http://www.eclipse.org" target="_blank">Eclipse</a> into your computer if you don't have it installed.
 * Add sbteclipse to your plugin definition file (or create one if doesn't exist). You can use either:
  * The global file (for version 0.13 and up) at ~/.sbt/0.13/plugins/plugins.sbt
  * The project-specific file at PROJECT_DIR/project/plugins.sbt
<br>For the latest version:
```
addSbtPlugin("com.typesafe.sbteclipse" % "sbteclipse-plugin" % "4.0.0")

```
 * In the Multi-Paoxs directory, run command:

```
->sbt eclipse
```
 * From the eclipse IDE, import the project to workspace, File->Import->Existing Projects into Workspace.
 * Note that if you want to run it from eclipse, main function is in **src/main/scala/Impl/Main.scala**
 
##Commands to manipulate a paxos
 * run 'propose' to propose a value.
 * run 'PL' to look up the log table.
 
##Supports and further improvement.
This is project can't be done without the support of my supervisors and advisors, <a href="http://www.cs.utah.edu/~ganesh/" target="_blank">Ganesh Gopalakrishnan</a> and <a href="https://sites.google.com/site/mohammedmahfoudh/home" target="_blank">Mohammed S. Al-Mahfoudh</a>. 
Also, Thanks to the great advice from Dr. <a href="http://rstutsman.github.io">Ryan Stutsman </a>.
<br><br>I am still working on it to make this project bettter, and I like to hear any advice to help make it better. Your advice is invaluable for me, please email me at allenzhaofromu@gmail.com if you think any aspect of the project can be made better.











