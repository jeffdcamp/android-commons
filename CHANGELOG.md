# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [1.11.0] - 2024-11-16

### Changed

- Fix issue with CacheApiResponse
- Changed DirectDownloader to use ktor
- Updated all versions

## [1.10.0] - 2024-09-21

### Added

- Added Support for Real-time RemoteConfig
- Added RefreshFlow

### Changed

- Updated all versions


## [1.8.1] - 2024-09-05

### Added

- Added InstantEpochSerializer
- Added Instant.dayOfWeek(...), Instant.nextDayOfWeek(...), Instant.previousOrSameDayOfWeek(...) 

### Changed

- Updated Double.toPercentageText() to better support KMP
- Android SDK 35
- Gradle 8.10
- Kotlin 2.0.20
- Updated all other versions

## [1.8.0] - 2024-06-10

### Added

- Added KtorKermitLogger
- Added tests for ApiResponse, CacheApiResponse

### Changed

- Gradle 8.8
- Improvements to ApiResponse, CacheApiResponse, KtorExt
- Updated all other versions

## [1.7.0] - 2024-05-25

### Changed

- Kotlin 2.0.0
- Updated all dependencies to latest

## [1.6.0] - 2024-03-16

### Added

- Added KotlinDateTimeTextConverter to support kotlin-datetime converters for Room
- Added CacheApiResponse for supporting etags and ApiResponse
- Added HttpRequestBuilder.cacheHeaders

### Changed

- Min SDK to 23
- Updated ApiResponse to support error response json (type E)
- Updated executeSafely to better support updated ApiResponse AND CacheApiResponse
- Improved dependencies (Changed all plugins to alias)

### Removed

- Removed KotlinSerializationFactory for Retrofit
- Removed < 23 code from NetworkUtil

## [1.5.0] - 2024-02-17

### Added

- Added EmailUtil.isValidEmailAddress(), getUsername(), getDomain(), getSendEmailIntent()
- Added NumberFormatExt: isNumeric(), trimTrailingZero(), toStringTrimTrailingZero(), toRoundedText(), toPercentageText()
- Added List.toCsv() (easy way to export a csv file)

### Changed

- Updated versions

## [1.4.6] - 2023-12-28

### Added

- ApiResponse for handling network responses
- KtorExt HttpClient.executeSafely(), HttpResponse.saveBodyToFile()
- Support for Ktor and ApiResponse via executeSafely()


### Changed

- Updated versions including: Kotlin 1.9.21, Firebase, Okio 3.7.0, etc
- Changed BaseFirebaseRemoteConfig to block while loading default values (prevent empty values on first launch)
- Migrated off of Firebase KTX (https://firebase.google.com/docs/android/kotlin-migration)


## [1.4.5] - 2023-11-18

### Changed

- Updated versions including: Kotlin 1.9.20, Okhttp 4.12.0, Firebase, etc

## [1.4.4] - 2023-10-07

### Added

- Added kotlin datetime trimToSeconds

### Changed

- Updated BaseFirebaseRemoteConfig to better support KMP
- Updated Versions
- Updated Changelog format

### Removed

- Removed ThreeTenExt (Date.toOffsetDateTime())


Version 1.4.3 *(2023-09)*
-------------------------
* Added DirectDownloader and DirectUploader
* Updated versions

Version 1.4.2 *(2023-08)*
-------------------------
* Improved SavedStateHandleExt.kt: Removed generic get() to prevent runtime issues / created SavedStateHandle extensions that are type safe (return a value that is expected)
* Updated versions

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