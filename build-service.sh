#!/bin/bash

# Export environment variables from .env file
export $(cat .env | xargs)

# Clean and build the project
./gradlew clean build

# Run the Spring Boot application
./gradlew bootRun