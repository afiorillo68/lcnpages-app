# lcnpages — Notion-like knowledge base

A demo application for the [localcloudnative-lab](https://github.com/afiorillo68/localcloudnative-lab),
showcasing the platform's capabilities for hosting cloud-native enterprise workloads.

`lcnpages` is a Notion-like knowledge base focused on hierarchical
markdown pages with internal wikilinks, tags, and full-text search.
Authentication is federated via Keycloak using OIDC.

## Stack

- **Backend**: Spring Boot 4.0.x, Java 21 LTS, Maven, MongoDB, Spring
  Security OAuth2 Resource Server
- **Frontend**: Angular 21.x, standalone components, zoneless change
  detection, Tailwind CSS, Vitest
- **Deploy**: Kubernetes via Kustomize, exposed through Apache Apisix
  with TLS edge termination

## Status

Phase 4 Step 1A — scaffolding complete. Functional implementation
in progress.

## Repository structure

```
backend/    Spring Boot REST API
frontend/   Angular SPA
deploy/     Kubernetes manifests (Kustomize)
docs/       Project documentation
```

## Local development

See `backend/README.md` and `frontend/README.md` for component-specific
instructions. Development pattern: backend and frontend run locally
on the developer's Mac, while MongoDB and Keycloak run in the lab's
k3d cluster (accessed via port-forward).
