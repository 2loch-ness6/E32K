# Android App Improvement Report

## 1. Executive Summary
The `MarauderController` Android application is currently in a functional but fragile state ("Alpha"). While it successfully compiles and implements the core MVVM architecture, its reliance on brittle text parsing and loose state management makes it prone to desynchronization with the hardware. The user experience is functional but requires manual intervention for many tasks.

## 2. Critical Stability Improvements

### A. Protocol & Parsing (High Priority)
*   **Problem**: `MarauderProtocolParser.kt` uses strict Regex patterns. If the firmware output varies (even by a whitespace), parsing fails silently or logs warnings, leading to empty lists in the UI.
*   **Solution**:
    1.  **Unit Tests**: Create a suite of unit tests for `MarauderProtocolParser` feeding it captured real-world serial output to ensure robustness.
    2.  **Loose Parsing**: Relax regex patterns to be more "fuzzy" (e.g., allow variable whitespace, optional columns).
    3.  **State Machine**: Implement a proper state machine in `MarauderRepository` that knows what command was sent and expects a specific type of response, rather than passively listening to all lines.

### B. Serial Communication
*   **Problem**: `SerialConnectionManager` reads raw bytes and converts to strings. While the line-splitting logic is present, there is no timeout or retry mechanism for command acknowledgments.
*   **Solution**: Implement a `CommandQueue` with timeouts. When sending `stopscan`, the app should *wait* for the "Stopping..." response before allowing new commands to prevent race conditions.

## 3. Usability & UX Enhancements

### A. "Live" Dashboard
*   **Current**: User must manually click "Scan", wait, click "Stop", click "Refresh List".
*   **Improvement**: Implement a "Live Scan" mode where the app automatically stops scanning periodically, lists APs, updates the UI, and resumes scanning, giving the illusion of a real-time view.

### B. Feedback Loops
*   **Current**: "Attack in progress" is a simple boolean state.
*   **Improvement**: Show real-time feedback if available (e.g., packet counts sent, deauths confirmed) by parsing the continuous output during an attack.

### C. Terminal Integration
*   **Current**: Overlay terminal.
*   **Improvement**: Make the terminal a "bottom sheet" that can be swiped up from any screen, allowing power users to debug issues without leaving their context.

## 4. Architecture & Code Quality

### A. Dependencies
*   **Current**: Recently fixed Gradle/Kotlin versions to enable building.
*   **Improvement**: Lock dependencies in a `libs.versions.toml` (Version Catalog) for better management and easier upgrades.

### B. Error Handling
*   **Current**: `try-catch` blocks often log and swallow errors.
*   **Improvement**: Surface connection errors to the user via Snackbars/Toasts (e.g., "Device disconnected unexpectedly", "Failed to parse AP list").

## 5. Roadmap

1.  **Phase 1 (Stabilization) [DONE]**: Unit tests for Parser, `CommandQueue` implementation, Error surfacing.
2.  **Phase 2 (Automation) [PARTIAL]**: "Live Scan" workflow [DONE], Auto-connect/Reconnect logic.
3.  **Phase 3 (Polish)**: Material 3 visual polish, Themes, Onboarding wizard.
