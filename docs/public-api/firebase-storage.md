# firebase-storage Public API Coverage

Updated: 2026-02-15  
Coverage (v1): **13 / 31 = 41.9%**

## Scope
This catalog tracks Cloud Storage APIs exposed by `firebase-storage`.

## Implemented (13)
1. `Firebase.storage`
2. `Firebase.storage(url)`
3. `FirebaseStorage.useEmulator(host, port)`
4. `FirebaseStorage.reference`
5. `FirebaseStorage.reference(path)`
6. `FirebaseStorage.referenceFromUrl(url)`
7. `StorageReference.bucket`
8. `StorageReference.fullPath`
9. `StorageReference.name`
10. `StorageReference.child(path)`
11. `StorageReference.getBytes(maxDownloadSizeBytes)`
12. `StorageReference.getDownloadUrl()`
13. `StorageReference.delete()`

## Missing / Planned (18)
1. `StorageReference.putBytes(bytes)` parity (Android/JVM implemented, iOS pending)
2. `StorageReference.parent`
3. `StorageReference.root`
4. `StorageReference.path` alias parity
5. Metadata read API
6. Metadata update API
7. Upload with metadata API
8. Upload from file URI/path API
9. Upload from stream API (where supported)
10. Resumable upload task abstraction
11. Download-to-file API
12. Stream download API
13. `list(maxResults, pageToken)` API
14. `listAll()` API
15. Retry time configuration APIs
16. Bucket-wide retry configuration parity
17. Task progress state abstraction
18. Storage error-code normalization API
