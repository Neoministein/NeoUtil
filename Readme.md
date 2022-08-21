# NeoUtil

NeoUtil is a set of utility library's which I made to faster jumpstart a project with a wide arrange of default functionality.
This project heavily relies on [CDI](https://jakarta.ee/specifications/cdi/3.0/) for most of the functionality to work.

## Module

**Common:** Utility classes

**Framework:** Logic for config, event, persistence, queue, security

**Microprofile:** Microprofile specific impl for framework

**Helidon:** Helidon specific impl for framework


## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes.

### Requirements

- JDK: 17 or newer
- Maven: 3.6.1 or newer
- Elasticsearch: 7.15.5 or newer

### Build

**Full build**
```bash
$ mvn clean install
```

**Run Integration Tests**
```bash
$ mvn clean install -Pintegration
```
