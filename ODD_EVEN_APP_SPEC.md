# Odd & Even Practice — Application Specification

## 1. Product identity and authority

| Field | Provisional value |
|---|---|
| English product name | Odd & Even Practice |
| Developer | Temple Software |
| Application ID | `com.templesoftware.oddeven` |

This is a separate educational Android application. It must not alter, reuse, or replace the Practice Times Tables package or application ID. The final app name and application ID must be confirmed before a new application module is created because an Android application ID becomes permanent once published on Google Play.

`TEMPLE_DESIGN_SYSTEM.md` governs shared family presentation and engineering conventions. This document governs Odd & Even product behaviour. The quality bar is Practice Times Tables: this app should clearly come from the same developer, but its learning mechanics and domain code are unique rather than a reskin.

## 2. Scope and learning flow

The app teaches children to recognise odd and even whole numbers. Its primary flow is:

`Home → Learn → Repeat → Practice → Results`

All screens support English (UK) and French, offline, in portrait and landscape on phones and tablets.

## 3. Shared visual and navigation rules

Use `TempleTheme`, the common scaffold/app bar, sapphire and aqua family colours, controlled gloss, near-white tinted backgrounds, rounded shapes, and the common feedback/audio behaviours. Odd uses the established primary sapphire treatment (`LearningBlue` `#4147B8`, or the primary glossy gradient `#626BE7` to `#30349A` on rich controls). Even uses the established action-teal treatment (`ActionTeal` `#009D91`, or the secondary glossy gradient `#24BDB0` to `#00796F`). In dark mode use the corresponding established primary and secondary dark tokens. These assignments remain consistent on Learn, Repeat, and Practice and are distinct from error red and reward gold. Validate text contrast for the exact component/background combination during implementation.

Colour is supplementary: every Odd/Even row and button is explicitly text-labelled. Home is simple and child-friendly, with a short localized explanation and a prominent Learn action; it need not duplicate the times-table selector. The app bar has no Home navigation icon, Back on Learn/Repeat, Back with abandonment confirmation in active Practice, and Home/Return on Results. The British/French flag selector remains available throughout.

## 4. Learn

### 4.1 Layout

Show two labelled horizontal areas, **Odd** and **Even**. Each consists of a persistent text label using its identifying treatment and a virtual horizontal number grid to the right. Both grids share identical cell width and horizontal coordinates; every cell accommodates two digits without clipping. Fill as much comfortable horizontal width as the window permits.

The conceptual columns represent consecutive whole numbers. Position 1 contains `1` in Odd and an accessible empty/decorative cell in Even; position 2 contains `2` in Even and an empty cell in Odd; position 3 contains `3` in Odd; position 4 contains `4` in Even; continue alternating. Empty counterparts must not announce misleading semantics.

### 4.2 Sequence and animation

Begin at 1 and reveal numbers sequentially with a restrained fade. Maintain one authoritative next-number/index state. When the initial viewport is comfortably full:

- reveal a bottom **Repeat** action;
- continue generating consecutive numbers;
- scroll both virtual rows smoothly and synchronously left by one common cell offset to make room at the right; and
- place/fade the next number into the right-most position.

Continue through 99 inclusive, never generate 100, and stop automatically with 99 visible/last in the sequence if Repeat has not been pressed. Repeat may be pressed at any time after it first appears and navigates to Repeat.

The sequence job is owned by the Learn ViewModel/lifecycle, is idempotently started, and cannot be duplicated by recomposition. Persist enough state to restore the revealed range, next number, scroll/transition phase, and Repeat availability. On rotation or process restoration, settle or safely resume a partially completed transition without skipping or duplicating a number. Language changes alter labels only.

## 5. Repeat

### 5.1 Lens interaction

A fixed, clearly outlined focus box (“lens”) occupies the centre of the learning region. A horizontal consecutive-number strip passes behind/through it. Initially 1 is centred, later numbers are visible to the right, and preceding space exists to the left.

After approximately 1,000ms, start a smooth leftward advance: the centred number exits left, the next number centres in the lens, and a later number enters from the right. After a randomly selected interval of 1–4 completed advances inclusive, pause exactly once with a number centred. Do not start a second interval or scroll loop while one is active. Choose each interval independently using an injectable random source; avoid an obviously repeating interval pattern (at minimum, do not deliberately cycle `1,2,3,4`). Sequence values should remain within the implementation's safe display range and may continue as long as the instructional session remains active.

At the bottom, show large, labelled **Odd** and **Even** buttons using their stable identifying colours. The centred value is the only classifiable value.

### 5.2 Responses and progression

On an incorrect classification:

- keep the same number centred and paused;
- briefly put both answer controls into failure/error presentation;
- play the Temple synthesized failure sound;
- apply the standard short content/screen judder; and
- re-enable another attempt after the brief feedback.

Repeat is instructional, so wrong attempts are not scored. On a correct classification, provide restrained success feedback, increment `correctClassifications`, choose the next random interval, and resume scrolling only after state is consistently transitioned from paused to moving.

After 5 correct classifications, show **Let’s Go** in the far-right position of the bottom action area while keeping Odd and Even available. Pressing it enters Practice. After 10 correct classifications, hide/remove the two classification controls and leave only Let’s Go. Correct classifications may continue between 5 and 10 if the child does not leave.

Preserve the centred number, visible sequence anchor/offset, phase, chosen remaining advances, correct count, control visibility, feedback event identity, and whether the initial delay has elapsed. Rotation and language changes must not reset progress. Restore an in-flight animation deterministically to a valid centred/scrolling state; never overlap pauses or duplicate loops. Landscape must retain a smooth dominant lens with controls beside or below it according to available height.

## 6. Practice

### 6.1 Questions and controls

Practice is a 60-second classification quiz. Display one random whole number from 1 through 999 inclusive as the dominant central content. At the bottom show large, labelled Odd and Even buttons using the same identifying colours as Learn and Repeat.

Generate through an injectable random source. The next number must differ from the immediately preceding number. Classification is pure domain logic: a value is even when its final decimal digit is `0`, `2`, `4`, `6`, or `8` (equivalently, divisible by 2); otherwise it is odd.

For question ordinals 1, 2, and 3, visually emphasise the final digit with the corresponding Odd/Even identifying treatment as a supplementary scaffold. The whole number remains readable and the answer is not conveyed by colour alone; provide meaningful composed semantics for the complete number. From question 4 onward, use the normal number treatment without a hint.

### 6.2 Timer

Show the common Temple timer at the top-right. It starts only after question 1 is fully visible and both answer controls are interactable. Use a monotonic 60-second deadline, display whole seconds, and keep one guarded timer job. Derive/restabilise remaining time from the persisted deadline and monotonic clock rather than restarting 60 seconds after recreation.

At zero, display `0`, freeze the timer, and prohibit generation of another question. If a question is active, it remains answerable until correctly classified, then navigate to Results. If expiry occurs in the between-question state, navigate directly to Results. UI transitions must re-check expiration before generating or revealing a successor.

### 6.3 Response rules

On the first correct response for an active question:

- atomically complete it and record whether it was accurate;
- play the success chime;
- show a brief gold-star animation toward the upper-right, clear of timer and number;
- if time remains, generate and display the next non-repeating number; otherwise enter the finish state.

On an incorrect response, keep the current question, set `hadWrongAttempt = true`, play failure sound, apply the standard judder, briefly show error presentation on both answer controls, and allow another answer. Further wrong attempts do not increment inaccurate question count: the question is recorded once, on correct completion, as inaccurate. Response timing is not collected in version 1.

Disable or debounce controls during atomic transition/feedback boundaries so rapid taps cannot complete a question twice or answer a newly created question accidentally.

### 6.4 Exit and restoration

Visible Back and system Back during active Practice open the same localized abandon-practice dialog. Confirming abandonment returns to Home (or the appropriate pre-Practice destination chosen during navigation design), removes the active Practice navigation/session state, and cannot later surface stale Results. Cancelling resumes unchanged.

Configuration and language changes preserve the current number, question ordinal, `hadWrongAttempt`, completed/accurate totals, remaining/deadline timing, timer-started/expired state, transition phase, and consumed feedback identities. They must not generate a new number, restart the timer, or duplicate timer/audio/animation loops.

## 7. Results

Each completed question counts once. A question is accurate only when correctly classified with no earlier wrong attempt. Calculate `accuracyPercentage = accurateCompleted / totalCompleted × 100`. Normal flow must have at least one completed question; nevertheless, scoring handles an empty list defensively as 0% without division failure.

Map accuracy to family stars:

| Accuracy | Stars |
|---:|---:|
| `>= 95%` | 5 |
| `>= 85%` and `< 95%` | 4 |
| `>= 70%` and `< 85%` | 3 |
| `>= 50%` and `< 70%` | 2 |
| `< 50%` | 1 |

Display the accuracy and exactly five star positions. Expose the group as “n out of 5 stars”, localized appropriately, and do not expose each decorative star separately. Play the standard synthesized victory fanfare once per genuine Results entry. A ViewModel/SavedStateHandle claim flag must prevent replay on recomposition, rotation, or language change while still permitting a future new Results session to play it once.

Provide an obvious Home action and make system Back equivalent. Returning Home clears expired Practice and Results payload state so a stale session cannot reappear.

## 8. State model

All screen behaviour is driven by ViewModels exposing immutable `StateFlow`; composables emit intents and render state. Keep pure classification, random-selection constraints, scoring, and state transitions out of composables. Use `SavedStateHandle` for compact, necessary navigation/process-restoration data and DataStore only for durable preferences such as language.

### 8.1 Likely states

```text
LearnUiState
  nextNumber: Int                 // 1..100 sentinel; never render above 99
  firstVisiblePosition: Int
  visible/revealed range or item snapshot
  phase: InitialReveal | Advancing | Complete
  transitionProgress/target as needed for deterministic restoration
  repeatEnabled: Boolean

RepeatUiState
  centredNumber: Int
  sequenceAnchor and visual offset/target
  phase: InitialDelay | Scrolling | PausedForAnswer | Feedback | ReadyOnly
  advancesRemaining: Int
  correctClassifications: Int
  letsGoEnabled: Boolean          // correct count >= 5
  answerControlsVisible: Boolean  // correct count < 10
  feedbackEventId/type

PracticeUiState
  currentNumber: Int
  questionOrdinal: Int
  hadWrongAttempt: Boolean
  completedCount: Int
  accurateCount: Int
  phase: Preparing | Answering | Feedback | BetweenQuestions | ReadyForResults
  timerStarted: Boolean
  deadlineElapsedRealtimeMillis: Long?
  remainingSeconds: Int
  timeExpired: Boolean
  feedbackEventId/type
  previousNumber (if not otherwise represented)

ResultsUiState
  completedCount: Int
  accurateCount: Int
  accuracyPercentage: Double
  accuracyStars: Int
  fanfareClaimed: Boolean
```

Store logical state, not large rendering objects. If a process death makes an old monotonic deadline impossible to compare reliably across boot, restore conservatively according to a documented clock/boot policy rather than granting a fresh timer. Configuration changes within a process use the same deadline normally.

State transitions must be idempotent. ViewModels own at most one learn/repeat animation job and one Practice timer job; cancel jobs in `onCleared`. One-shot audio/visual effects carry unique event IDs or an explicit consumption contract.

## 9. Localization

Provide complete default, `values-en-rGB`, and `values-fr` resources for app name, screen titles, instructions, Odd, Even, Learn, Repeat, Let’s Go, Practice, Results, timer descriptions, star semantics, Home/Back, and the abandonment dialog. No user-visible string is hardcoded in Kotlin.

On first launch use French when the system language is French, otherwise English (UK). Persist an explicit selection in DataStore and apply it immediately through a localized configuration context. Switching language changes visible text and semantics but preserves Learn sequence, Repeat correct count/current lens state, and every Practice/Results value.

## 10. Accessibility

- All controls have at least 48dp touch targets and visible focus/pressed states.
- Odd and Even always have explicit localized labels; colour is never their only cue.
- The lens exposes the centred number clearly and avoids announcing off-centre/decorative numbers as the active prompt.
- The scaffolded final digit still yields a coherent full-number semantic.
- Timer and stars use localized aggregate descriptions.
- Icon-only Back/Home/language controls have localized descriptions and meaningful roles.
- Empty grid positions, spacers, decorative gloss, and animated reward stars have cleared semantics.
- Feedback includes non-colour cues (sound and/or motion/text); reduced or unavailable audio must not remove understandable feedback.
- Font scaling must not clip labels, number content, timer, dialog actions, or the route forward.

## 11. Responsive design

Do not use fixed screen coordinates. Derive layouts from constraints, orientation, available height, density/font scale, and window width; constrain content on large screens.

- **Compact portrait phone:** stack app bar, dominant interaction, and bottom actions. Learn row labels may use a compact fixed-leading column with the grids taking remaining width. Permit safe vertical scrolling for supporting text, never the active question away from its controls.
- **Landscape phone:** minimise vertical gaps. Place classification controls beside the dominant lens/number when height requires it; retain top-right timer visibility and synchronized horizontal rows. Avoid oversized typography that crowds controls.
- **Tablet portrait:** centre a bounded content column/panel, enlarge useful grid/lens space, and avoid stretching buttons edge to edge unnecessarily.
- **Tablet landscape:** use a bounded wide arrangement, potentially interaction left and actions/right support panel, while keeping the learning object visually dominant.
- **Font scaling:** switch layout earlier, allow labels/buttons to wrap or grow, and scroll supporting regions. Grid cell sizing must accommodate two digits at the effective text size; Practice accommodates three. Essential content must not overlap or clip.

Every orientation maintains 48dp targets, safe insets, readable contrast, and an obvious next action.

## 12. Architecture and implementation boundaries

Use Kotlin, Jetpack Compose, Material 3, Navigation Compose, ViewModel, immutable StateFlow, DataStore, SavedStateHandle, and unidirectional state flow. Use no dependency-injection framework for version 1. Suggested separation:

- `domain/oddeven`: pure classification and question selection;
- `domain/results`: accuracy aggregation/star mapping;
- `data/preferences`: locale persistence;
- screen packages for Home, Learn, Repeat, Practice, and Results state/ViewModels/composables;
- reusable app-local Temple theme, scaffold, controls, responsive utilities, and synthesized audio infrastructure.

Do not import Practice Times Tables domain code or make multiplication abstractions generic. Shared source extraction should occur only after genuine reuse is demonstrated.

## 13. Testing requirements

### 13.1 Unit/domain tests

- Classification: 1 odd; 2 even; 0 even if the helper accepts zero; representative one-, two-, and three-digit values; 999 odd; 998 even.
- Number generation: values stay in 1..999 and exactly the same value never repeats immediately, including deterministic/random boundary cases.
- Scaffold: enabled for question ordinals 1–3 and disabled for question 4 onward.
- Accuracy: wrong attempt marks the active question inaccurate; several wrong attempts still create one inaccurate completion; first-attempt correct remains accurate.
- Timer: duration is 60 seconds; it starts only when the first question becomes interactive; active-question expiry freezes at zero and requires correct completion; no next question is produced after expiry; between-question expiry finishes directly; duplicate start calls create no duplicate loop.
- Repeat random interval is always 1–4 inclusive and deterministic fakes can exercise each boundary; five correct answers enable Let’s Go; ten hide classification controls; incorrect answers do not advance or score.
- Results: test thresholds immediately below and at 50%, 70%, 85%, and 95%, plus 0% and 100%; empty/zero-total input is defensive; star count remains 1..5 for a valid/defensive percentage.
- Fanfare claim/event is delivered once and is not duplicated by recomposition-style repeated reads.

### 13.2 ViewModel/restoration tests

- Learn restoration retains its exact sequence position and never skips/exceeds 99.
- Repeat restoration retains centred number, advances remaining, and correct count, with no progress loss or duplicate loop.
- Practice restoration retains number, ordinal/scaffold state, wrong-attempt flag, score, timer deadline/remaining/expired state, and prevents duplicate timers.
- Results restoration retains score and fanfare claim.
- English (UK) and French selection/default rules work; language switching preserves each active activity state.

### 13.3 Compose/instrumentation tests

- English and French labels, dialogs, timer descriptions, and “n out of 5 stars” semantics are correct.
- Odd/Even controls expose labels and remain at least 48dp.
- Off-lens/decorative items and empty grid cells do not provide misleading semantics.
- Compact portrait, landscape phone, tablet portrait, tablet landscape, and increased font scale layouts keep central content and required actions visible and non-overlapping.
- Back navigation confirms abandonment only when active progress would be lost.
- Rapid/repeated input cannot double-complete a question or create overlapping Repeat pauses.

## 14. Release and privacy requirements

Before release, run lint, all JVM/unit and relevant instrumentation tests, debug build, release build/bundle, and verify R8/resource shrinking. Use secure local upload signing; never commit keystores or secrets. Produce app-specific launcher/store artwork and localized Play listing assets rather than reusing Practice Times Tables branding unchanged.

The initial version has no network permission or dependency, accounts, analytics, ads, behavioural tracking, cloud sync, online leaderboards, user profiles, external audio assets, or unnecessary runtime permissions.

## 15. Out of scope for the initial version

- accounts or user profiles;
- networking, cloud sync, or online leaderboards;
- analytics or behavioural tracking;
- advertising or monetization;
- external audio assets;
- unnecessary runtime permissions;
- dependency-injection frameworks; and
- achievements beyond standard Results feedback.

## 16. Product decisions required before implementation

The following assumptions are intentionally not invented in this specification:

1. Confirm the final visible names in English and French and the permanent application ID before module creation.
2. Approve the exact accessible Odd and Even identifying colour tokens in both light and dark themes. They should be selected from or derived compatibly with the Temple palette, but error coral/red and reward gold should remain reserved for feedback.
No other product decision is required for the defined initial learning flow. These identity confirmations do not affect the learning rules, scoring thresholds, privacy baseline, or architecture.
