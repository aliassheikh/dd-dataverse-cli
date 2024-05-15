dd-dataverse-cli
================

Command-line client for the Dataverse API.


SYNOPSIS
--------

    dataverse --help

DESCRIPTION
-----------

Command-line client for the Dataverse API. It uses [dans-dataverse-client-lib]{:target="_blank"} to send queries to the [Dataverse API]{:target="_blank"}. It is
currently work in progress. The target version of Dataverse is 6.0.

[dans-dataverse-client-lib]: https://github.com/DANS-KNAW/dans-dataverse-client-lib

[Dataverse API]: http://guides.dataverse.org/en/latest/api/index.html

ARGUMENTS
---------

See `dataverse --help` for a list of available commands.


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
