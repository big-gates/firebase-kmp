# firebase-common Public API Coverage

Updated: 2026-02-15  
Coverage (v1): **21 / 25 = 84.0%**

## Scope
This catalog tracks Firebase Core/app-bootstrap APIs exposed by `firebase-common`.

## Implemented (21)
1. `Firebase.app`
2. `Firebase.app(name: String)`
3. `Firebase.initializeApp(context)`
4. `Firebase.initializeApp(context, options)`
5. `Firebase.initializeApp(context, options, name)`
6. `Firebase.getApps(context)`
7. `FirebaseApp.name`
8. `FirebaseApp.options`
9. `FirebaseApp.dataCollectionDefaultEnabled`
10. `FirebaseApp.automaticResourceManagementEnabled`
11. `FirebaseApp.persistenceKey`
12. `FirebaseApp.delete()`
13. `FirebaseOptions.applicationId`
14. `FirebaseOptions.apiKey`
15. `FirebaseOptions.databaseUrl`
16. `FirebaseOptions.gaTrackingId`
17. `FirebaseOptions.storageBucket`
18. `FirebaseOptions.projectId`
19. `FirebaseOptions.gcmSenderId`
20. `FirebaseOptions.authDomain`
21. `FirebaseOptions.serviceAccountPath` (JVM-only)

## Missing / Planned (4)
1. Global Firebase app-level data collection switch parity API.
2. Core log-level parity API.
3. Named default-app replacement/reconfiguration parity behavior API.
4. Explicit runtime validation API for option parity diagnostics.
