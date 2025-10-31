# Generic Clustering Service


Agents please read Agents.md

<!-- BEGIN: JULES-APPEND-ONLY -->
<!-- Jules: append new notes below, do not modify content above this line. -->

## Integration Tests

Are defined in the gcs-it subproject. Cause them to run with

    ./gradlew -PrunIT :gcs-it:test

or in a gh action

    - name: Full-stack integration tests
      run: ./gradlew --no-daemon -PrunIT :gcs-it:test


## How to Run

To run the application, use the following command:

```bash
./gradlew :app:run
```

## API Endpoints

### Health Check

To check the health of the application, use the following command:

```bash
curl http://localhost:8080/health
```

### Ingest

To ingest a new record, use the following command:

```bash
curl -X POST -H "Content-Type: application/json" -d '{"id": "6", "text": "This is a new sentence."}' http://localhost:8080/ingest
```

### Feedback

To provide feedback on a candidate duplicate, use the following command:

```bash
curl -X POST -H "Content-Type: application/json" -d '{"score": 0.8, "isDup": true}' http://localhost:8080/feedback
```

### Cluster

To retrieve the members of a cluster, use the following command:

```bash
curl http://localhost:8080/cluster/123
```
<!-- END: JULES-APPEND-ONLY -->

