Change Log
==========

Version 1.4.1 *(2023-08)*
-------------------------
* Changed Timber to Kermit Logger / added CrashLogException to provide common exception for Logger.e without a provided exception (Ex: Logger.e(CrashLogException()) { })
* Updated versions

Version 1.3.1 *(2023-06)*
-------------------------
* Added Okio Filesystem extension functions: isDirectory, readText, writeText, unzip, unzipFile, copyFileToFileSystem
* Updated versions

Version 1.3.0 *(2023-05)*
-------------------------
* Updated versions: Kotlin 1.8.21 / AGP 8.0.1 / Gradle 8.1.1 / Java 17
* Added Firestore Extension functions for documentFlow(), collectionFlow(), queryFlow() 
* Removed ProcessScope (Use CoroutinesModule to provide ioDispatcher, defaultDispatcher, appScope)

Version 1.2.0 *(2023-02)*
-------------------------
* Updated versions: Kotlin 1.8.10
* Removed SavedStateHandleDelegates

Version 1.1.0 *(2022-10)*
-------------------------
* Updated versions: Firebase dependencies (removed core/added analytics)

Version 1.0.2 *(2022-10)*
-------------------------
* Updated versions: Kotlin 1.7.20 / Firebase dependencies
* Code cleanup

Version 1.0.1 *(2022-08)*
-------------------------
* Updated versions: Kotlin 1.7.10 / Firebase dependencies

Version 1.0.0 *(2022-08)*
-------------------------
* Initial release