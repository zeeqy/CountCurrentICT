# CountCurrent: Electricity Bill Calculator

An Android app that calculates monthly electricity bills using Malaysia's TNB tiered tariff, applies a rebate, and saves every calculation to the cloud.

Built for **ICT602 (Mobile Technology and Development)** at UiTM.

## Features

- Pick a billing month and enter units used (1 to 1000 kWh)
- Tiered TNB tariff calculation with a 0 to 5% rebate
- Records saved to **Firebase Cloud Firestore**
- Records list with detail view, edit and delete
- About page with author and app info

## Tech stack

Java · Android SDK (min SDK 21, target SDK 34) · Material Components · Firebase Firestore

## Project structure

```
app/src/main/java/com/countcurrent/app/
  MainActivity.java       calculator screen
  RecordsActivity.java    saved records list
  DetailActivity.java     view / edit / delete a record
  FirestoreManager.java   Firestore CRUD wrapper
  ElectricityRecord.java  data model
```

## Running

1. Open the project in Android Studio.
2. Create a Firebase project, enable Cloud Firestore, and add an Android app with the package `com.countcurrent.app`.
3. Download your own `google-services.json` into `app/` (it is git-ignored).
4. Run on an emulator or device.
