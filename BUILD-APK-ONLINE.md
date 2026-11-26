# Build the APK online with GitHub Actions

This project includes `.github/workflows/build-apk.yml`. GitHub's hosted runner will compile the APK for you; no Android Studio or computer setup is required beyond uploading the project to GitHub.

1. Sign in to GitHub and create a **new public or private repository** named `KaizenDiscipline`.
2. Upload the *contents* of this project folder (including the hidden `.github` folder) to the repository. The files `settings.gradle`, `build.gradle`, `gradle.properties`, and `app/` must be at the repository root.
3. Open the repository's **Actions** tab. If prompted, enable workflows.
4. Select **Build KAIZEN APK** → **Run workflow**.
5. Wait for the run to finish successfully. Open the completed run and download the `KAIZEN-debug-apk` artifact.
6. Extract the downloaded artifact; it contains `app-debug.apk`. Open it on your Android phone to install. You may need to allow installation from your browser or file manager.

The workflow builds a debug APK for testing. It is not a Play Store release-signed APK.
