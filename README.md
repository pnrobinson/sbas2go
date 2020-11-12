# sbas2go

This app is intended to use [phenol]() library code to perform
Gene Ontology analysis on differentially expressed and differentially spliced genes 
from the sbas2go project.

## Setup

### Building the app

Clone the application from GitHub and use maven to build the app.

```
git clone https://github.com/pnrobinson/sbas2go.git
cd sbas2go
mvn package
```
This will generate the app in the ``target`` subdirectory. We assume that you have put the app on the PATH
for the following commands.

### Download the sbas files

Download the sbas files from the project [zenodo repository](https://zenodo.org/record/4179559).
For this analysis, we only need the following three files:

* as.tar.gz 
* dge.tar.gz
* fromGTF.tar.gz 

Unpack all three files in the same directory. This app expects to be passed
the path to that directory as ``-s,--sbas``.

### Download the GO files
Run the download command to download the necessary Gene Ontology files.

```
java -jar sbas2go download
```

## Running the app

Following this, simply enter
```
java -jar sbas.jar --data <path/to/sbas/directory>
```
