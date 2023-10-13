# TESLauncher

### NOTE: This project is unfinished and is not even in alpha stage. Do not have any expectations.

Yet another launcher for Minecraft 
![images/Screenshot.png](images/Screenshot.png)

### Quick start:
First, make sure you have [Maven](https://maven.apache.org/) installed. To check it, run
```shell
mvn -v
```
If your Maven home and OS information is displayed, it means you have installed Maven correctly. Then run these commands:
```shell
git clone https://github.com/TESLauncher/TESLauncher
cd TESLauncher
mvn clean compile assembly:single
java -jar target/TESLauncher-1.0-jar-with-dependencies.jar
```