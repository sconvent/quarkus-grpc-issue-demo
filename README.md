Quarkus gRPC Quickstart
========================

This project is adapted from the `grpc-plain-text-quickstart` project by Quarkus.

It is a demo for https://github.com/quarkusio/quarkus/issues/46475

The testcase can be run with `mvn test`.

Note that in the `pom.xml` the max heap space for the testcase is specified.
Also: Due to restrictions in my environment, the gRPC code generator could only be imported for x86 platforms. Please change this dependency for your needs if necessary.
