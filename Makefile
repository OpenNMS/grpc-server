## 
# Makefile to build grpc-server
##
.PHONY: help deps-build compile package oci clean

.DEFAULT_GOAL := package

SHELL                := /bin/bash -o nounset -o pipefail -o errexit
WORKING_DIRECTORY    := $(shell pwd)

help:
	@echo ""
	@echo "Makefile to gRPC Kafka gateway"
	@echo ""
	@echo "Requirements:"
	@echo "  * OpenJDK 11 JDK with java an javac in your search path"
	@echo "  * Docker installed to build the container image"
	@echo "  * Maven 3.6.x installed to compile and package the java binary"
	@echo ""
	@echo "Targets:"
	@echo "  help:             Show this help"
	@echo "  deps-build:       Test requirements to compile source and build OCI image"
	@echo "  compile:          Validate, compile and run tests with Maven"
	@echo "  package:          Package and assemble Java archive"
	@echo "  oci:              Create OCI with docker"
	@echo "  clean:            Delete all Maven build artifacts and OCI files"
	@echo ""

deps-build:
	@command -v javac
	@command -v mvn -v
	@command -v docker

compile:
	@echo "Maven validate ..."
	mvn validate
	@echo "Maven compile ... "
	mvn compile
	@echo "Maven tests ..."
	mvn verify

package: compile
	@echo "Maven package ..."
	mvn package -DskipTests

oci: package
	@echo "Create OCI ..."
	docker build -t grpc-server .

clean:
	mvn clean

all: oci
  