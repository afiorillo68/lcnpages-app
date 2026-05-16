# Handoff — lcnpages Phase 4 Step 1C.1 (Spring Boot 4 + MongoDB blocked)

**Data sessione**: 16 maggio 2026  
**Stato**: Step 1C.1 backend foundation completato come codice ma **bloccato in fase di avvio** per un problema di Spring Boot 4.0.5 + MongoDB autoconfig che non sono riuscito a risolvere in 6 tentativi.  
**Prossima sessione**: ripartire da qui, con Architect "fresco" e (idealmente) avendo studiato la documentazione Spring Boot 4 MongoDB autoconfigure tra una sessione e l'altra.

---

## 1. Stato globale del progetto

### lcn-lab (piattaforma)

- **Phase 1, 2, 3**: tutti **completati e committati**.
- **5 ADR** ratificati, **13 lezioni di metodologia** in `docs/methodology.md`.
- **Disaster recovery** Sealed Secrets validato via cluster reset.
- **README, methodology, BACKLOG** aggiornati con Phase 3 done + Phase 4 1A/1B done + lezioni 12-13. **Committato e pushato**.

### lcnpages-app (demo applicativa)

- **Step 1A** (scaffolding Spring Boot 4 + Angular 21): completato, committato `1d30d13 chore: initial scaffolding`.
- **Step 1B** (Keycloak setup): completato. Client `lcnpages-frontend` configurato, utente `angelo` creato, OIDC flow verificato end-to-end via curl + JWT decoded.
- **Step 1C.1** (backend foundation): **scaffolding committato**, ma **verifica end-to-end fallita**.

---

## 2. Cosa è stato fatto in 1C.1

### Decisioni architetturali ratificate

- **1C-A** Audience handling: **Opzione B — audience mapper in Keycloak**. ✓ **Implementato e verificato**.
- **1C-B** Roles flattening: **Opzione α — Custom JwtAuthenticationConverter in Spring Boot**. ✓ Scaffolded (non ancora testato perché blocked sull'avvio).
- **1C-C** Profili: solo `local` funzionante in 1C.1, `cluster-dev` placeholder per Step 1D. ✓
- **1C-D** Commit: singolo atomico. ✓

### Codice scritto da Code (Engineer)

Committato come `feat: 1C.1 backend foundation (Page model, MongoDB repository, OIDC resource server)`:

- `backend/src/main/java/lab/localcloudnative/lcnpages/domain/Page.java`
- `backend/src/main/java/lab/localcloudnative/lcnpages/repository/PageRepository.java`
- `backend/src/main/java/lab/localcloudnative/lcnpages/security/KeycloakJwtAuthenticationConverter.java`
- `backend/src/main/java/lab/localcloudnative/lcnpages/security/SecurityConfig.java`
- `backend/src/main/java/lab/localcloudnative/lcnpages/controller/PageController.java`
- `backend/src/main/resources/application.yml`
- `backend/src/main/resources/application-local.yml`
- `backend/src/main/resources/application-cluster-dev.yml`

`./mvnw clean compile` → BUILD SUCCESS.

### Setup operativo

- `/etc/hosts` aggiornato con `127.0.0.1 keycloak.lcn-lab.local` (già presente da Phase 3).
- Audience mapper `lcnpages-audience` di tipo `Audience` configurato in Keycloak su `lcnpages-frontend-dedicated → Mappers`. **Verificato funzionante**: il JWT ora ha `aud: ["lcnpages-frontend", "account"]`.
- MongoDB: database `lcnpages` creato, utente `lcnapp` creato con ruolo `readWrite` sul database `lcnpages`. **Verificato funzionante**: connessione + insert/find/delete via mongosh ok.
- Port-forward MongoDB attivo: `kubectl -n platform-mongodb port-forward svc/mongodb 27017:27017`.
- `mongosh` installato sul Mac via brew (versione 2.8.3).

---

## 3. Il problema bloccante: Spring Boot 4 non si autentica a MongoDB

### Sintomo iniziale

Quando si lancia `./mvnw spring-boot:run` con profilo `local`:

```
Caused by: com.mongodb.MongoCommandException: Command execution failed
on MongoDB server with error 13 (Unauthorized):
'Command createIndexes requires authentication' on server localhost:27017.
```

### Causa identificata (dopo varie diagnosi)

Nel log di startup, alla riga `MongoClientSettings{...}`:

```
credential=null
```

Spring Boot **non sta passando le credenziali al MongoClient**, anche se l'`application-local.yml` le definisce correttamente.

E le richieste vanno al database `test` (default Mongo), non `lcnpages`:

```
Command "createIndexes" started on database "test"
```

Quindi due problemi correlati:
1. Credenziali non risolte
2. Database default non rispettato

### I 6 tentativi (tutti falliti)

**Tentativo 1**: `spring.data.mongodb.uri` con `${LCNAPP_PASSWORD}` interpolato dalla env var. → `credential=null`, fallito.

**Tentativo 2**: Diagnosi varie via mongosh per escludere problemi MongoDB-side (permessi `readWrite` insufficienti? caratteri speciali nella password?). Risultato: MongoDB lato server è perfetto; il problema è solo Spring.

**Tentativo 3**: Property granulari invece di `uri`:
```yaml
spring:
  data:
    mongodb:
      host: localhost
      port: 27017
      database: lcnpages
      authentication-database: lcnpages
      username: lcnapp
      password: ${LCNAPP_PASSWORD}
```
→ Ancora `credential=null`. Fallito.

**Tentativo 4** (questo era il punto in cui avrei dovuto fermarmi): bean Java esplicito `MongoConfig.java` con `MongoCredential.createCredential(...)`:
```java
@Configuration
@Profile("local")
public class MongoConfig {
    @Value("${LCNAPP_PASSWORD}")
    private String password;
    
    @Bean
    public MongoClient mongoClient() {
        MongoCredential credential = MongoCredential.createCredential(
            "lcnapp", "lcnpages", password.toCharArray()
        );
        MongoClientSettings settings = MongoClientSettings.builder()
            .credential(credential)
            .applyConnectionString(new ConnectionString("mongodb://localhost:27017/lcnpages"))
            .build();
        return MongoClients.create(settings);
    }
}
```
→ **Parziale**! Ora `credential=MongoCredential{userName='lcnapp', source='lcnpages', password=<hidden>}` è popolato. MA il database usato per `createIndexes` è ancora `test` (default Mongo), non `lcnpages`. Errore nuovo: `not authorized on test to execute command createIndexes`.

**Tentativo 5**: aggiunto `spring.data.mongodb.database: lcnpages` in `application.yml` (era già presente). Nessun effetto: Spring Data MongoDB ignora la property quando c'è un `MongoClient` bean custom — usa il database dalla connection string del bean.

**Tentativo 6**: non eseguito, sessione fermata.

### Cause radice ipotizzate (da verificare nella prossima sessione)

1. Spring Boot 4.0.x ha cambiato l'autoconfig MongoDB rispetto a 3.x in modo non documentato chiaramente. Le property `spring.data.mongodb.*` non producono più un `MongoCredential` automaticamente, anche con la sintassi corretta.
2. Spring Data MongoDB richiede un `MongoDatabaseFactory` bean per il default database; non basta il `MongoClient` con connection string.
3. Possibili bug specifici di 4.0.5 da verificare su GitHub (release notes, issue tracker).

---

## 4. Stato concreto dei file sul disco

### Modificati ma NON committati

`backend/src/main/resources/application.yml` — verosimilmente ha `spring.data.mongodb.database: lcnpages` aggiunto.

`backend/src/main/resources/application-local.yml` — verosimilmente ha solo la sezione `security`, senza più la sezione `spring.data.mongodb`.

### Creati ma NON committati

`backend/src/main/java/lab/localcloudnative/lcnpages/config/MongoConfig.java` — il bean Java esplicito, **funzionante per le credenziali ma incompleto per il database default**.

### Stato git atteso

```
cd ~/Dropbox/Lavoro/lcnpages-app
git status
```

Dovrebbe mostrare 2 file modificati + 1 file nuovo nella cartella `config/`.

### Decisione di cleanup

**Da decidere nella prossima sessione**, ma due opzioni:

- **Opzione A — Rollback al commit `feat: 1C.1 backend foundation`**: cancellare tutte le modifiche e ripartire da zero con un fix definitivo.
- **Opzione B — Mantenere `MongoConfig.java` e completarlo**: aggiungere il `MongoDatabaseFactory` bean al config Java, ratificarlo come pattern del lab.

Mia raccomandazione per la prossima sessione: **Opzione A**. Il bean Java esplicito è un patch sintomatico, non un design intenzionale. Meglio capire perché Spring Boot 4 non legge le property e fixare alla radice.

---

## 5. Cleanup operativo già spiegato al Decision-maker

1. **Spring Boot**: già morto, niente da fare.
2. **Port-forward MongoDB terminale**: Ctrl+C oppure lasciarlo running (mongosh da Mac ne ha ancora bisogno).
3. **Variabili shell**: `unset LCNAPP_PASSWORD APP_PWD ROOT_PWD`.

L'utente `lcnapp` su MongoDB **resta**: utile per la prossima sessione, no point in re-creating.

---

## 6. Lezioni metodologiche da annotare al prossimo cleanup documentale

Tre lezioni candidate per `docs/methodology.md`:

### Episode 14 candidate — When tentative count exceeds 3, stopping IS the fix

Six consecutive Architect-proposed fixes for a Spring Boot 4 + MongoDB autoconfig problem, none successful. The Decision-maker spent hours testing fixes that were guesses, not informed by documentation reading or actual knowledge of Spring Boot 4 internals.

The right behavior would have been: at the 3rd failed tentative, the Architect should explicitly say "I don't have enough specific knowledge of this framework version. We should stop and study the docs before continuing." Continuing to guess wastes the Decision-maker's time and produces frustration without resolution.

The pattern: when the Architect notices "I'm formulating tentative fix N+1 because the symptom changed from N", it's a signal that the underlying mental model is wrong, not just incomplete. The right move is to surface the lack of knowledge, not produce another guess.

### Episode 15 candidate — The diagnostic signal in the log was missed for hours

The MongoClientSettings log entry contains `credential=null` from the very first run. That single field — visible in plain text in the log — was the entire story. But the Architect's attention was on the stack trace (which is dramatic and Java-like), so the field was skipped.

Stack traces are loud but often secondary. The actual diagnostic information lives in INFO log lines preceding the failure. The Architect should learn to scan the calm logs *before* the exception, not just the exception itself.

Companion to Episode 13 (when UI fails, drop to protocol): when a stack trace fails to reveal the cause, drop down to inspect every field of the relevant log entries.

### Episode 16 candidate — Framework major version migration has hidden contract changes

Spring Boot 4.0 changed MongoDB autoconfig behavior compared to 3.x in ways that are not surfaced clearly in the migration guide or default error messages. Property `spring.data.mongodb.uri` with credentials no longer produces a populated `MongoCredential` in some configurations.

When adopting the latest major version of a framework for a project, expect a budget of "undocumented edge cases" to surface during first real use. Plan accordingly: either choose the previous stable major (3.x), or budget time to study the source code of the autoconfig classes when behavior diverges from expectation.

For this project specifically: Spring Boot 4.0.5 was a deliberate choice for the lab to be future-aligned. The price is paid here. Document the workaround that eventually works as ADR or runbook entry for future workloads.

---

## 7. Come riprendere nella nuova chat

Suggerimento di prompt di apertura per la nuova chat:

> "Riprendiamo il progetto lcnpages dopo una pausa per saturazione del contesto della chat precedente. Allego il file handoff-lcnpages-1c1.md con stato puntuale e dettagli del blocco. Studia il file prima di rispondere, poi propongo come ripartire."

Poi allegare questo file all'inizio della nuova sessione.

Probabile primo passo della nuova sessione:

1. Verificare stato git in `~/Dropbox/Lavoro/lcnpages-app/`
2. Decidere se rollback (opzione A) o mantieni e completa (opzione B)
3. Con conoscenza Spring Boot 4 aggiornata, proporre fix definitivo
4. Verificare end-to-end fino a `curl http://localhost:8080/api/pages` con JWT angelo

---

## 8. Reminder strategici da non dimenticare

- **Production-mode finalization per lcnpages** (post-Step 1C, in Step 1D): backend e frontend in cluster, build via GHA, deploy via Argo CD, TLS via Apisix. Da aggiungere a BACKLOG quando faremo cleanup.
- **Audience mapper Keycloak come pattern ricorrente**: candidato per runbook `docs/how-to/keycloak-add-oidc-client.md` quando il pattern sarà confermato funzionante end-to-end.
- **Lezioni 14, 15, 16 candidate** da formalizzare in `methodology.md` al prossimo cleanup documentale del lab.
- **Step 1C ha 8 sub-step** previsti, 1C.1 è il primo. Dopo il fix Spring Boot+Mongo, restano 1C.2 → 1C.8 (vedi /mnt/transcripts per dettaglio).
