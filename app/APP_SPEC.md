System Role: You are an expert Senior Android Developer specializing in educational applications built with Jetpack Compose and Kotlin.
Project identity:
Brand/developer: Temple Software
App: Practice Times Tables
Permanent Android application ID: com.templesoftware.practicetimestables
Kotlin/Java namespace: com.templesoftware.practicetimestables
Approved launcher-icon identity: a central multiplication `×` with colourful educational numbers in a polished sapphire/blue-violet design.
Launcher-icon master artwork: `design/app-icon-master.png` (preserve as the source of truth for generated branding assets).
Google Play store icon: `design/play-store-icon-512.png` (store-listing artwork; keep separate from adaptive and legacy launcher resources).
Google Play feature graphic: `design/play-store-feature-1024x500.png`.
Google Play English (UK) screenshots: raw captures in `design/store-screenshots/raw/en-GB/`; presentation assets in `design/store-screenshots/final/en-GB/`.

Implement the following application incrementally in the existing Android Studio project.
Do not attempt to implement the entire specification in a single change.
At each stage:
Inspect the existing project.
Explain the files you intend to create or modify.
Implement only the requested stage.
Run the relevant Gradle build and tests.
Fix all compilation and test failures.
Summarize the changes.
Do not commit changes unless explicitly instructed.
Stop after completing the requested stage. Do not automatically continue to the next stage.

Keep the Android Studio project suitable for eventual Google Play release, including appropriate application metadata, resources, release-compatible configuration, and production-quality code.
Do not fabricate or generate external Play Console artifacts or steps that do not belong inside the Android Studio project.
Implement this app step-by-step. First set up the directory structure, dependencies, and state architecture, then create each screen in separate modular files.
1. Visual Identity & Audio Requirements
   Target Audience: Primary school children (vibrant, high-contrast palette, but clean and modern rather than overly cartoonish).
   Layout & Responsiveness: Must dynamically adapt to both Portrait and Landscape modes on both phones and tablets.
   Localization: On first launch:
   if the system language is French, use French
   otherwise use English (UK)
   After the user explicitly selects a language, persist that preference using DataStore and use it on subsequent launches.
   All user-visible strings must use Android string resources. Provide complete English (UK) and French resource sets. Do not hard-code translated strings inside composables.
   Changing language must update the visible UI immediately without requiring an app restart.
   Built-in Audio (Zero External Asset Files): Implement custom synthesized sound effects programmatically using ToneGenerator / AudioTrack / other built in libraries:
   Success Sound: Short synthesized bell/chime.
   Failure Sound: Short synthesized descending wrong-answer sting.
   Fanfare Sound: Short victory sequence played on the Results screen.
   Audio generation and playback must not block the UI thread. Audio failure must not interrupt or crash gameplay.
   Transitions: visual effects such as fades should be configurable. Repeat defaults are a 2000 ms prompt delay, 500 ms answer fade, 1500 ms completed-answer hold, and 600 ms carousel movement.
2. Screen Architecture & Application Logic
   All screens use a common app bar with the language selector on the right
   Home: No navigation icon.
   Refresh: Back icon on the left returns to Home.
   Repeat: Back icon on the left returns to Home.
   Quiz: Back navigation invokes the localized abandon-quiz confirmation.
   Results: Home/Return icon on the left returns to Home.
   Screen 1: Front Page (Home)
   Toggle Grid: Block of selectable toggles for numbers 2 through 12.
   If no saved table-selection preference exists, initialize the selected tables to 2, 3, 4, and 5. Afterward, persist changes using DataStore.
   Row 1: Numbers 2, 3, 4, 5
   Row 2: Numbers 6, 7, 8, 9, 10
   Row 3: Numbers 11, 12
   At least one number must be selected, and if only one is selected then tapping on it does not deselect it.
   Top Bar: Language selector dropdown (Top Right).
   Action Navigation Buttons:
   Refresh (Navigates to Refresh Screen starting with the lowest toggled number).
   Repeat (Navigates directly to Repeat Screen starting with the lowest toggled number).
   Let's Go (Launches the timed quiz).
   Screen 2: Refresh Screen
   Displays the multiplication table ("1 × n" up to "12 × n") for the currently active selected number in numerical sequence.
   Bottom Action Button:
   Displays "Next" to proceed to the next toggled number in ascending numerical order.
   If viewing the last toggled number in the selection, the button label changes to "Repeat", which navigates directly to the Repeat Screen for that same set of selected numbers.
   Screen 3: Repeat Screen
   The carousel begins with the localized instruction "Repeat out loud" as its first centred item.
   Vertical Carousel Animation:
   Center line shows the question "a × b = " (where 'a' are the numbers 1 to 12, and 'b' is the times table) followed by the answer "c" fading in.
   Where available, the previous item (with answer) and next item (without answer) are displayed above and below the center line, dimmed to 50% opacity and slightly blurred if the blur effect is available. At the first and last items, the unavailable adjacent item is simply omitted while maintaining the centre item's position.
   Once the answer fade-in has completed, the completed equation remains visible for 1500 ms, then the carousel smoothly advances to the next line.
   Completion Action:
   After the last multiplication item "12 × n" completes its normal hold, a button appears at the bottom.
   If more selected numbers remain: Button reads "Next" (moves to the next toggled number in numerical order).
   If no selected numbers remain: Button reads "Let's Go" (launches the Quiz mode).
   Screen 4: Let's Go (Quiz Screen)

Timer:  Formula for Total Seconds = 120 + (Count of Extra Selected Tables × 24), where extra selected tables are the total number of selected tables - 1. The timer starts when all transitions and effects have completed and the keypad becomes interactive. The timer is displayed in whole seconds and counts down each second. The quiz timer and active-question state must survive ordinary configuration changes such as device rotation without restarting the quiz or generating a new question. Do not allow recomposition to restart timers, sound playback, or animations incorrectly. Once the quiz countdown has started, it runs continuously, including during transitions between questions. Only per-question response timing begins when the new question becomes interactive. If the countdown reaches zero after a question has been correctly completed but before the next question becomes interactive, do not create another question; transition to Results after the current success transition completes.

Question Sequence & Randomization:
Generate questions from a shuffled pool containing all facts for the selected tables, from 1 × n through 12 × n. Do not repeat a question until the current pool has been exhausted. When the pool is exhausted, reshuffle all eligible questions and begin again. The same question must never appear twice consecutively across reshuffles. The shuffled pool contains multiplication facts, not rendered question formats. For example, the fact for the 8 times table: 7 × 8 = 56 occurs once in the pool. The first fact "a" ranges from 1 through to 12. The second "b" is always the selected times-table number. Do not commute operands when constructing the underlying fact.
First half of countdown timer: While remaining time is greater than 50% of Total Seconds, questions must use the format "a × b = ?"
Second half of countdown timer: Once remaining time is less than or equal to 50% of Total Seconds, questions with multiple selected tables randomly use formats "? × b = c", "a × ? = c", and "a × b = ?". With exactly one selected table, use only "? × b = c" and "a × b = ?"; never hide the fixed selected-table operand.
The display format is determined when a new question is created. An already-displayed question must not change format if the timer crosses the 50% boundary while that question is being answered.

UI Positioning & Controls:
Timer counting down rendered in top-right corner.
Question text rendered one third down from top of the screen.
Answer box rendered directly above the input pad. It displays empty character placeholders corresponding to the exact number of digits in the correct answer (e.g., "_ _" for 12). Correct digits replace the placeholders as they are entered. For answer 12 it starts "_ _", after pressing "1" it becomes "1 _", and after pressing "2" it becomes "1 2".
Disable keypad input while transitioning between questions. Enable it only when the new question is fully visible. The new question's response timer begins at the same moment input becomes enabled.
Number Pad: 3×4 grid block containing digits 0–9 and no Clear button, anchored at the bottom. The 0 is centred in the final row and the other two cells are empty and non-interactive. So:
1   2   3
4   5   6
7   8   9
    0

Validation Rules:
Evaluate input immediately on every character keypress. Each keypress should match the digits sequentially from left to right in the correct answer. For instance "12" must be keypress "1" followed by keypress "2".
If a wrong digit is pressed at any position: Record the mistake immediately, briefly show the attempted digit for 100 ms, then clear the placeholders. Start the failure sound, screen judder/shake, and fading "Try Again" feedback immediately and independently.
If the full answer is completed correctly: Stop response timing immediately, display the completed answer for 100 ms, and start the success bell and rotating gold-star feedback immediately. Advance after the brief confirmation without waiting for feedback animations or audio.
For each question, measure response time from the moment the question becomes fully visible and the keypad becomes interactive until the final correct digit is entered. Incorrect keypresses do not restart or reset that question's response timer. Store each question's response time in milliseconds for scoring purposes.
During an active quiz, Android system Back must display a localized confirmation dialog before abandoning the quiz and returning Home.
End of Quiz State:
When time reaches 0:00, the timer freezes at 0:00.
The player must complete the active question. Upon entering the correct answer while time is 0:00, transition directly to the Results Screen.
Screen 5: Results Screen
Audio: Play built-in fanfare sound on entry.
Top Bar: Return icon on top-left (navigates back to Front Page).
Accuracy:
Accuracy is measured per question. A question is counted as accurate if it is completed without any incorrect digit keypress.
If one or more incorrect digits are entered during a question, that question counts as one inaccurate question regardless of how many incorrect keypresses occur.
Accuracy percentage = (number of questions completed without a mistake / total completed questions) × 100.
Overall Accuracy and Speed are calculated directly from all quiz responses.
Per-table Accuracy and Speed are calculated only from responses belonging to that table.
Do not calculate overall ratings by averaging the per-table star ratings.
Overall Scores:
Large Accuracy label with overall 5-star graphic rating taking into account all responses.
Large Speed label with overall 5-star graphic rating taking into account all responses.
Table Breakdown (Displayed if more than 1 table was selected):
A 3-column table: Column 1 = Table Name (e.g., "2×"), Column 2 = Accuracy (5 small stars), Column 3 = Speed (5 small stars).
Star Scoring Mathematics:
Accuracy Stars (only whole stars):
>= 95% accuracy = 5 stars
>= 85% to < 95% accuracy = 4 stars
>= 70% to < 85% accuracy = 3 stars
>= 50% to < 70% accuracy = 2 stars
< 50% accuracy = 1 star

Speed Stars (only whole stars):
If no valid response times exist for a scoring group, do not calculate a speed rating for that group.
Trim floor(numberOfResponses × 0.10) of the slowest responses. If fewer than 10 responses are available, trim zero responses.
Calculate average time of remaining responses AvgTime.
AvgTime < 2.0 seconds = 5 stars
AvgTime >= 2.0 and < 4.0 seconds = 4 stars
AvgTime >= 4.0 and < 6.0 seconds = 3 stars
AvgTime >= 6.0 and < 8.0 seconds = 2 stars
AvgTime >= 8.0 seconds = 1 star
Apply the 10% trimming independently when calculating: overall Speed, and each individual table's Speed


3. Implementation Deliverables
   Provide well-structured Kotlin Jetpack Compose UI components, custom ViewModels, and state handlers.
   Prefer pure Kotlin classes/functions for question generation, validation, timing calculations and scoring logic so that these can be unit tested without Android framework dependencies.
   Ensure all UI is visually clear, readable, adaptive to orientation changes, and easy for primary-school-age children to operate.
   Use:
   Kotlin
   Jetpack Compose
   Material 3
   Navigation Compose
   ViewModel
   StateFlow with immutable UI state
   DataStore for persistent user preferences

Do the following:
Minimum interactive touch target: 48dp.
Do not use color as the only indicator of selection, success, or failure.
Supply accessibility descriptions for icon-only controls.
Support font scaling without clipping essential content.
Preserve relevant UI/session state through configuration changes.
Avoid fixed pixel positioning.
Support compact phones, landscape phones, and tablet-sized windows.

Do not add:
Network access
User accounts
Analytics
Advertising
Cloud services
Unnecessary runtime permissions
Dependency injection frameworks unless clearly required

Provide unit tests for:
timer duration calculation
question pool generation
question format generation
digit-by-digit validation
accuracy scoring
speed scoring
10% response-time trimming
selected-table persistence/default behavior
accuracy classification per question
first-half/second-half question-format boundary
selected-table operand preservation in generated facts
timer and active-question preservation across configuration changes
