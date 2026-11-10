# DevOps unit converter

Java 17 unit converter with a browser form and REST API, prepared by Anshul Vaibhav for the DevOps course.

## Build and run

```bash
./scripts/maven.sh clean verify
java -jar target/unit-converter.jar
```

Open http://localhost:8080. The existing JUnit suite contains nine tests.

## Continuous integration

The Jenkinsfile checks out this GitHub repository, tests the application, packages the JAR, and archives a JAR named with the Jenkins build number and Git revision. Configure Jenkins tools named `jdk17` and `maven3`. SCM polling checks for changes once per minute; run the job once after connecting it.

## Local deployment

- `ansible/`: Ubuntu/Debian systemd service role, variables and restart handler. Configure an actual inventory before running it.
- `Dockerfile`: multi-stage build and non-root Java runtime.
- `k8s/`: three-replica Deployment, Service, ConfigMap and startup/readiness/liveness probes.
- `./scripts/deploy-kind.sh`: build the image and deploy on the dedicated local Kind cluster.
- `./scripts/scale-demo.sh`: demonstrate manual scaling from three to five and back to three Ready pods.

The application source and relevant Git history were copied from the completed course project. This publishing copy contains the application and deployment files; restricting the history to these files changes Git commit IDs. New live demonstration branches and merges are real Git operations performed against this repository.

Jenkins, Ansible, Docker and Kubernetes run in a local Ubuntu VM on the Mac. GitHub hosts the source repository; AWS is not required.
