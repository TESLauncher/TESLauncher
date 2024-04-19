# TESLauncher

> [!WARNING]  
> This project is unfinished. Do not have any expectations.

Yet another launcher for Minecraft 
![images/Screenshot.png](images/Screenshot.png)

### Quick start:
First, make sure you have [Maven](https://maven.apache.org/) installed. To check it, run
```shell
mvn -v
```
If your Maven home and OS information is displayed, it means you have installed Maven correctly. Then simply run these commands:
```shell
git clone https://github.com/TESLauncher/TESLauncher
cd TESLauncher
mvn clean package
```
If "BUILD SUCCESS" is displayed, use the following command to run the launcher:
```shell
java -jar target/TESLauncher-0.11.0.jar
```

### Warning
Launcher will create its folders in the directory where `java` command was executed. That means that if you run <br>
```shell
java -jar target/TESLauncher-0.11.0.jar
```
launcher will run in project's root directory. If you want to run the launcher in any other place, specify a path to the working directory using `--workDir` parameter. <br>
Example:
```shell
java -jar target/TESLauncher-0.11.0.jar --workDir C:\Users\User\Documents\TESLauncher
```