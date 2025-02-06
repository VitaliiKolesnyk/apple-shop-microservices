FROM jenkins/jenkins:2.485-jdk17

USER root

# Install required dependencies
RUN apt-get update -qq \
    && apt-get install -qqy apt-transport-https ca-certificates curl gnupg2 software-properties-common

# Install Docker
RUN curl -fsSL https://download.docker.com/linux/debian/gpg | apt-key add - \
    && add-apt-repository \
    "deb [arch=amd64] https://download.docker.com/linux/debian \
    $(lsb_release -cs) \
    stable" \
    && apt-get update -qq \
    && apt-get -y install docker-ce

# Install kubectl
RUN curl -LO "https://dl.k8s.io/release/v1.21.0/bin/linux/amd64/kubectl" \
    && chmod +x kubectl \
    && mv kubectl /usr/local/bin/

# Add Jenkins user to the Docker group
RUN usermod -aG docker jenkins
