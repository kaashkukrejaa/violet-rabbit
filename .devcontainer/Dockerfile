FROM mcr.microsoft.com/devcontainers/base:ubuntu-24.04

# Install Java 17 JDK (not JRE)
RUN apt-get update && export DEBIAN_FRONTEND=noninteractive \
    && apt-get -y install --no-install-recommends \
    openjdk-17-jdk \
    openjdk-17-jdk-headless \
    maven \
    sqlite3 \
    && apt-get clean -y && rm -rf /var/lib/apt/lists/*

# Install Node.js 20
RUN curl -fsSL https://deb.nodesource.com/setup_20.x | bash - \
    && apt-get install -y nodejs \
    && apt-get clean -y && rm -rf /var/lib/apt/lists/*

# Set JAVA_HOME to JDK (not JRE) - critical for Maven compilation
# Dynamically find the correct Java installation path using update-alternatives
RUN JAVA_PATH=$(update-alternatives --query javac | grep 'Value:' | awk '{print $2}' | sed 's|/bin/javac||') && \
    echo "export JAVA_HOME=${JAVA_PATH}" >> /etc/profile.d/java.sh && \
    echo "export PATH=\${JAVA_HOME}/bin:\${PATH}" >> /etc/profile.d/java.sh && \
    chmod +x /etc/profile.d/java.sh

# Set JAVA_HOME in Dockerfile environment (using typical path for amd64/arm64)
# The actual runtime will use /etc/profile.d/java.sh for the correct path
RUN bash -c 'source /etc/profile.d/java.sh && echo "ENV JAVA_HOME=${JAVA_HOME}" > /tmp/java_env' && \
    bash -c 'source /etc/profile.d/java.sh && ln -sf ${JAVA_HOME} /usr/lib/jvm/default-java'

ENV JAVA_HOME=/usr/lib/jvm/default-java
ENV PATH="${JAVA_HOME}/bin:${PATH}"

# Verify JDK installation (javac must be present for Maven)
RUN java -version && \
    javac -version && \
    mvn -version && \
    node -v && \
    npm -v && \
    echo "JAVA_HOME=$JAVA_HOME"

# Create workspace directory
RUN mkdir -p /workspace
WORKDIR /workspace
