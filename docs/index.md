dd-dataverse-cli
================

Command-line client for the Dataverse API.


SYNOPSIS
--------

    dataverse --help

DESCRIPTION
-----------

Command-line client for the Dataverse API. It uses [dans-dataverse-client-lib]{:target="_blank"} to send queries to the [Dataverse API]{:target="_blank"}. It is
currently work in progress. The target version of Dataverse is kept in sync with the latest installed by the DANS Data Stations, for example 
[Data Station Archaeology]{:target="_blank"}.

### Basic syntax

    dataverse <object-type> [options] <target> <command> [options]

* `<object-type>`: the type of object to operate on, e.g. `dataset`, `file`, `collection` (i.e. a "dataverse" or "subverse").
* `<target>`: the target object, e.g. the DOI of a dataset. This argument can also be a file with a list of targets. The targets must be separated by
  whitespace and/or newlines, so in general a file with one target per line will work. A dash (`-`) can be used to read targets from standard input. In the case
  of the `collection` object type, leaving out the target will cause the command to default to the root dataverse.
* `<command>`: the command to execute on the target object. The available commands depend on the object type; for example, for a dataset it could be
  `publish`, `delete-draft`, etc. See the help for the specific object type for a list of available commands. If a list of targets is provided, the command
  will be executed on each target in turn, with exactly the same arguments.

### The `--parameters-file f` option

Some commands accept a parameters file as input. This is generally a CSV file with target IDs and command parameters. (However, see the help of the command for
the specifics.) The `--parameters-file` option allows you to perform batch tasks on objects without being restricted to the same arguments for each object. For
example, you could assign different roles to different users on different collections.

    dataverse <object-type> [options] <command> --parameters-file <file> [other options]


[dans-dataverse-client-lib]: https://github.com/DANS-KNAW/dans-dataverse-client-lib

[Dataverse API]: http://guides.dataverse.org/en/latest/api/index.html

[Data Station Archaeology]: https://archaeology.datastations.nl/#dvfooter

ARGUMENTS
---------

See `dataverse --help` for a list of available commands.

EXAMPLES
--------

```bash
# Publish one dataset
dataverse dataset doi:10.5072/dans-2xg-4y5 publish 

# Publish all datasets in the file dataset-list.txt
dataverse dataset dataset-list.txt publish 

# Publish two datasets, sending the DOIs through standard input
echo doi:10.5072/dans-2xg-4y5 doi:10.5072/dans-4zy-aab | dataverse dataset - publish

# Add roles to datasets as specified in role-assignments.csv
dataverse dataset role-assigment add --parameters-file role-assignments.csv 
```

INSTALLATION AND CONFIGURATION
------------------------------
Currently, this project is built as an RPM package for RHEL8 and later. The RPM will install the binaries to
`/opt/dans.knaw.nl/dd-dataverse-cli` and the configuration files to `/etc/opt/dans.knaw.nl/dd-dataverse-cli`.

For installation on systems that do no support RPM and/or systemd:

1. Build the tarball (see next section).
2. Extract it to some location on your system, for example `/opt/dans.knaw.nl/dd-dataverse-cli`.
3. Start the service with the following command
   ```
   /opt/dans.knaw.nl/dd-dataverse-cli/bin/dd-dataverse-cli server /opt/dans.knaw.nl/dd-dataverse-cli/cfg/config.yml 
   ```

BUILDING FROM SOURCE
--------------------
Prerequisites:

* Java 17 or higher
* Maven 3.6.3 or higher
* RPM

Steps:

    git clone https://github.com/DANS-KNAW/dd-dataverse-cli.git
    cd dd-dataverse-cli 
    mvn clean install

If the `rpm` executable is found at `/usr/local/bin/rpm`, the build profile that includes the RPM
packaging will be activated. If `rpm` is available, but at a different path, then activate it by using
Maven's `-P` switch: `mvn -Pprm install`.

Alternatively, to build the tarball execute:

    mvn clean install assembly:single
