# Define build stage
FROM egovio/alpine-maven-builder-jdk-8:gcp AS build

# Define WORK_DIR argument with a default value
ARG WORK_DIR=.

# Set working directory
WORKDIR /app

# Copy the project files
COPY ${WORK_DIR}/pom.xml ./${WORK_DIR}/

# Run Maven clean and package commands
RUN mvn -f ./${WORK_DIR}/pom.xml clean package -DskipTests

# Create runtime image
FROM egovio/wildfly:1-helm-fin-e6312078

# Switch to non-root user
USER jboss

# Copy artifacts from build stage to Wildfly deployment directory
COPY --from=build /app/egov/egov-ear/*.ear /opt/jboss/wildfly/standalone/deployments/

# Define default command
CMD ["/opt/jboss/wildfly/bin/standalone.sh", "-b", "0.0.0.0", "-bmanagement", "0.0.0.0", "-Ddb.migration.enabled=true", "-Ddev.mode=true", "-Ddb.flyway.validateon.migrate=true"]
