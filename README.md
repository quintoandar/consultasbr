consultasbr
===========

A series of libs that allow different queries about a person in Brazil: CPF (~Security Social ID), Antecedentes Criminais PF (~criminal record with federal police), Processos no TJSP (~public records of court processes/cases of São Paulo State).

* New: IGPMReader to read IGP-M index. 

Usage
-----
First add the repository:

```xml
<repositories>
  ...
  <repository>
    <id>QuintoAndar Repo ConsultasBr</id>
    <url>https://raw.github.com/quintoandar/consultasbr/master/mvn-repo</url>
  </repository>
  ...
</repositories>
  
```

Then add the dependency artifact:

```xml
<dependencies>
  ...
  <dependency>
    <groupId>br.com.quintoandar</groupId>
    <artifactId>consultasbr</artifactId>
    <version>1.1.0-SNAPSHOT</version>
  </dependency>
  ...
</dependencies>
```
