# lcnpages backend

Spring Boot 4.0.x REST API for the lcnpages knowledge base.

## Local development

Prerequisites:
- Java 21 LTS
- MongoDB accessible (typically port-forwarded from the lcn-lab cluster)
- Keycloak accessible (typically port-forwarded from the lcn-lab cluster)

Run:
```bash
./mvnw spring-boot:run
```

The API listens on `http://localhost:8080` by default.
Health check: `http://localhost:8080/actuator/health`

## Configuration profiles

- `local`: development on the Mac, MongoDB and Keycloak via port-forward
- `cluster`: deployed in Kubernetes, in-cluster service discovery

Profiles will be configured in Step 1C of Phase 4.
