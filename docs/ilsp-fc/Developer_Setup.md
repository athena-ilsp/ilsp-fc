# Developer Setup

We assume a recent Eclipse installation with Maven Integration (m2e) installed. The following were tested in Eclipse Juno with m2e 1.1

* Go to the [Files](Files.md) page and download the latest ilsp-fc*project.zip archive. 
* In Eclipse, go to menu `File->Import->General->Existing Projects Into Workspace`. 
* Choose Next.
* Click Select archive file.
* Browse to where you saved the ilsp-fc*project.zip archive.
* Press Finish.
* The project will now be imported and all dependencies will be downloaded.

An alternative way of building ILSP-FC using Maven only  is the following:

* Make sure a recent version of Maven (3.*) is installed on your machine
* Download from the [Files](Files.md) page the latest ilsp-fc*project.zip archive, e.g. 

```console
[user@machine:~/src ]$ wget http://nlp.ilsp.gr/redmine/attachments/ilsp-fc-x.x-project.zip
[user@machine:~/src ]$ unzip ilsp-fc-x.x-project.zip
[user@machine:~/src ]$ cd ilsp-fc-x.x
[user@machine:~/src/ilsp-fc-x.x ]$ mvn clean install
```

* A runnable jar is now created. You can test it by running, e.g.,

```console
[user@machine:~/src/ilsp-fc-x.x ]$ java -jar ./target/ilsp-fc-x.x-jar-with-dependencies.jar --help
```

* See the [GettingStarted](Getting_Started.md) page for more examples on how to run ILSP-FC






