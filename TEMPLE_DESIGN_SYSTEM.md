# Temple Software Educational-App Design System

## 1. Purpose

This document defines the shared visual language, interaction patterns, accessibility baseline, and engineering conventions for Temple Software educational Android applications. Practice Times Tables is the reference implementation; its subject-specific behaviour remains governed by `app/APP_SPEC.md`.

The system is reusable across different primary-school subjects. It deliberately contains no times-table business logic and is not a mandate for one universal quiz engine.

## 2. Product principles

Temple educational apps must be:

- primary-school friendly, with a polished, professional character rather than a toy-like one;
- clearly focused on the learning task, with simple navigation and minimal distraction;
- colourful but controlled, readable, and high contrast;
- free of advertising, analytics, behavioural tracking, accounts, and cloud dependencies;
- offline-first, with child-appropriate encouragement and correction;
- fully usable in English (UK) and French;
- responsive in portrait and landscape on phones and tablets; and
- accessible by design, not by later retrofit.

## 3. Visual identity

### 3.1 Core light palette

Use the established values and semantic roles below. App-specific identifying colours may be added only when the subject needs them, and must harmonise with this palette.

| Token | Value | Role |
|---|---:|---|
| `LearningBlue` | `#4147B8` | Primary learning focus, selected controls, navigation emphasis |
| `OnLearningBlue` | `#FFFFFF` | Content on primary |
| `LearningBlueContainer` | `#E4E7FF` | Gentle primary container |
| `OnLearningBlueContainer` | `#202A72` | Content on primary container |
| `ActionTeal` | `#009D91` | Secondary/action emphasis |
| `OnActionTeal` | `#FFFFFF` | Content on secondary |
| `ActionTealContainer` | `#C5F0EA` | Gentle secondary container |
| `OnActionTealContainer` | `#004F49` | Content on secondary container |
| `CelebrationGold` | `#D89400` | Positive/reward emphasis |
| `CelebrationGoldContainer` | `#FFE2AC` | Reward container |
| `OnCelebrationGoldContainer` | `#573900` | Content on reward container |
| `WarmBackground` | `#F8FAFF` | Near-white, subtly blue-tinted app background |
| `WarmSurface` | `#FFFCF8` | Near-white warm surface |
| `PaleCream` | `#FFF5E8` | Low surface container |
| `PaleBlue` | `#EDF2FF` | Standard surface container / soft panel |
| `PaleAqua` | `#E5F8F5` | High surface container |
| `PaleLavender` | `#F1EEFF` | Highest surface container |
| `WarmSurfaceVariant` | `#E8ECFA` | Alternate surface |
| `DeepNavyInk` | `#1D2440` | Primary text on light surfaces |
| `MutedSlateInk` | `#4D5575` | Supporting/de-emphasised text and outline |
| `SoftOutline` | `#BCC6E8` | Subtle borders |
| `WarmError` | `#B3263E` | Error content |
| `SuccessGreen` | `#24734F` | Semantic success where gold is not appropriate |

### 3.2 Gloss and accent tokens

| Token | Light value | Intended use |
|---|---:|---|
| Primary gradient top / bottom | `#626BE7` / `#30349A` | Royal-indigo/sapphire primary actions and rich result panels |
| Secondary gradient top / bottom | `#24BDB0` / `#00796F` | Vivid turquoise/aqua secondary actions and panels |
| Gloss highlight | white at 38% alpha | Fine upper/translucent border highlight |
| Gold star | `#F2B83B` | Earned stars and brief success animation |
| Unearned star on rich surface | white at 52% alpha | Empty star positions |
| Panel border | `#B9C5F4` | Soft learning-panel outline |
| Coral accent | `#E8636F` | Restrained accent or error-adjacent emphasis only |

### 3.3 Dark palette

Dark mode is an established part of the theme and must remain intentional: primary `#C0C5FF`, primary container `#303C9A`, secondary `#76D8CE`, secondary container `#005049`, tertiary/gold `#FFC96F`, error `#FFB3BC`, background `#181A22`, surface `#1E2029`, elevated surfaces `#222631`/`#272B38`/`#303545`, main ink `#E7E5ED`, muted ink `#C7C5D0`, and outline `#474A57`. Dark glossy gradients use `#7079EE` to `#3A3FA8` and `#35C5B8` to `#087C73`; the highlight is white at 30% alpha.

Disabled controls reduce opacity (the reference glossy button uses 46%) while retaining their semantic hue. De-emphasised content uses the appropriate `onSurfaceVariant`, outline, or controlled alpha—not arbitrary grey. Error red/coral is brief and limited; it must never dominate ordinary learning UI.

## 4. Glossy component treatment

Temple's characteristic depth comes from restrained vertical gradients: a lighter upper colour, a darker lower edge, a fine translucent highlight border, rounded clipping, and a soft shadow. Pressed controls scale slightly (approximately 0.975; 0.97 for tactile controls), reduce elevation from approximately 5dp to 1dp, and may move down 1dp. The reference press transition is quick (90ms). This should feel responsive, not bouncy.

Use glossy treatment for primary calls to action, selected/high-value controls, major result panels, and occasional subject-defining controls. Use calm Material surfaces for instructions, dense information, dialogs, menus, app bars, and repeated supporting cards. The established corner hierarchy is 10dp small, 16dp medium, 20dp large, and 28dp extra-large; the reference glossy action uses an 18dp radius.

Do not use arbitrary rainbow colouring, neon colours, excessive gradients, flat grey-heavy UI, or decorative effects that compete with the learning content. Depth must remain subtle and must not imitate physical materials excessively.

## 5. Typography

Use the platform default font family and Material typography hierarchy. The reference theme establishes bold 30sp/38sp `headlineMedium`, semibold 22sp/28sp `titleLarge`, normal 16sp/24sp `bodyLarge`, and semibold 17sp/22sp `labelLarge`. These are baseline theme tokens, not permission to hard-code every screen.

- App-bar/screen titles use `titleLarge`, normally semibold and primary-coloured.
- Instructional and supporting copy uses `bodyLarge` and `onSurfaceVariant`.
- The current number, equation, clock, or other central learning object is the strongest and largest typographic element, commonly extra-bold.
- Button labels use `labelLarge` or a contextually larger bold style and must remain concise.
- Results headings use headline/title styles; supporting metrics remain subordinate.

Adjust prominent content responsively for width and height. Honour Android font scaling, allow wrapping where meaningful, avoid clipping essential text, and choose scrolling or a revised arrangement before shrinking text below comfortable reading size.

## 6. Common screen structure

Use a Material 3 `Scaffold` with the subtly tinted background, a common top app bar, a dominant content region, and bottom actions where the flow needs them. The app bar normally has a centred title, primary-coloured title/icons, a calm `surfaceContainer` background, navigation on the left, and the language selector on the right.

Home has no navigation icon. Child flows use Back; Results uses an explicit Home/Return icon. System Back and the visible icon must have consistent behaviour. Bottom actions should be visually obvious without crowding the central task.

Portrait commonly stacks content and actions. Landscape may place the learning area and controls side by side. Tablets constrain readable content to sensible maximum widths rather than stretching every element. Constrained heights or large font scales may require vertical scrolling. Home screens should share this hierarchy and family character without copying Practice Times Tables content.

## 7. Common reusable components

Names below describe intended family abstractions; they need not initially live in a shared module.

- `TempleTheme`: Material 3 light/dark colour schemes, typography, shapes, and educational colour tokens.
- `TempleScreenScaffold`: background, insets, app bar slot, responsive content bounds, and optional bottom action area.
- `TempleAppBar`: optional Back/Home navigation, title, and language selector.
- `TemplePrimaryButton`: primary sapphire action with clear enabled/disabled semantics.
- `TempleSecondaryButton`: quieter secondary action using teal or a calm surface as appropriate.
- `TempleGlossyButton`: the reusable gradient, highlight, shadow, press-depth, and accessibility treatment.
- `TempleCard`: rounded, restrained surface for instruction or grouped content.
- `TempleLanguageSelector`: British/French flag trigger, localized accessibility label, radio-style selected semantics, and persistent selection.
- `TempleResultsStars`: exactly five positions, gold earned stars, subdued unearned stars, and a single localized rating semantic.
- `TempleQuizTimer`: whole-time display with a meaningful accessibility description and error treatment at zero.
- `TempleSuccessFeedback`: non-blocking synthesized chime plus brief positive visual event and optional gold-star animation.
- `TempleFailureFeedback`: non-blocking synthesized failure sting, brief red state, and short judder.

Do not turn equations, table selectors, number lenses, maps, clocks, or other subject interfaces into generic components without demonstrated reuse.

## 8. Interaction feedback

Correct answers play a short locally synthesized bell/chime, show restrained positive feedback, and may animate a gold star where it will not obscure learning content or a timer. Incorrect input plays a short synthesized descending sting, applies the standard short content/screen judder, and briefly uses error red with a friendly retry message where useful. It must not be punitive, alarming, or block another attempt. Entering Results plays a short synthesized victory fanfare once per genuine entry.

Audio must be generated locally with Android facilities such as `AudioTrack` or `ToneGenerator`; it uses no external asset and no network. Preparation/playback must run off the UI thread, calls must be non-blocking, resources must be released, and all audio failures must be caught and logged without interrupting learning. Feedback is modelled as one-shot, uniquely identified events so recomposition does not replay it.

The reference motion constants include a 200ms general transition, 400ms/10dp quiz shake, 900ms retry feedback, and 850ms success feedback. Apps may tune timings for their activity while preserving the restrained family feel.

## 9. Language and localization

- Supply complete English (UK) (`en-GB`) and French (`fr`) resources.
- On first launch, select French when the system locale language is French; otherwise select English (UK).
- Persist an explicit user choice in Preferences DataStore and apply it immediately without app restart.
- Changing language must not reset an active learning activity.
- Represent selection with programmatically drawn British and French flag icons. The control requires a localized description naming the current language; menu entries expose selected/radio semantics.
- Put every user-visible phrase in Android string resources. Keep mathematical symbols, formatting patterns, or other non-linguistic resources separate where that improves correctness and reuse.

## 10. Accessibility

- Interactive targets are at least 48dp in both dimensions.
- Icon-only controls have localized content descriptions.
- Never rely on colour alone: add text labels, icons, shape, selection state, or explicit feedback.
- Support Android font scaling without clipping essential content or actions.
- Provide meaningful roles, selected state, merged descriptions, and concise semantics for compound learning content.
- Clear semantics from decorative stars, empty grid placeholders, layout spacers, and duplicated visual text; they must not become misleading focus targets.
- Preserve active state across configuration changes.
- Use dp/sp and constraint-based arrangements, not fixed pixel positioning.
- Keep every screen usable on compact portrait phones, short landscape phones, tablets, and reverse rotations. Scroll supporting content when needed; preserve the central learning interaction.

## 11. Architecture

The baseline stack is Kotlin, Jetpack Compose, Material 3, Navigation Compose, Android `ViewModel`, immutable `StateFlow` UI state, and Preferences DataStore. Use lifecycle-aware state collection and unidirectional flow: UI renders state and emits actions; the ViewModel/domain layer validates actions and publishes a new immutable state.

Separate concerns as follows:

- **UI:** composables, responsive arrangements, semantics, transient rendering animations, and navigation callbacks.
- **State:** screen ViewModels, immutable UI state, lifecycle-safe jobs, and consumable feedback/navigation events.
- **Domain:** pure Kotlin rules, generation, validation, timing decisions, and scoring, independently unit testable.
- **Persistence:** repository interfaces and DataStore implementations for durable user preferences; `SavedStateHandle` for compact navigation/process-restorable session values where appropriate.
- **Reusable infrastructure:** theme, app bar, localization host, feedback/audio controllers, responsive window information, and common controls.

Use constructor/factory wiring. Do not add a dependency-injection framework unless later complexity provides a concrete justification.

## 12. Quiz and activity conventions

- Use monotonic time (`SystemClock.elapsedRealtime` or an injectable equivalent) for countdown deadlines and response timing.
- Persist a deadline/essential session snapshot; recompute displayed remaining time from the deadline rather than trusting delayed ticks.
- A timer reaching zero normally freezes visibly at zero and lets the already-active question finish; it must not create another question afterward.
- Multiple incorrect attempts on one question make that one question inaccurate, not several inaccurate questions.
- Language and configuration changes do not reset activity state, choose a new question, or restart timing.
- Guard jobs so recomposition/restoration cannot create duplicate timer or animation loops or overlapping transitions.
- Back navigation from an active learning session asks for localized abandonment confirmation when progress would be lost.

These are behavioural conventions, not a requirement for a single cross-subject quiz engine.

## 13. Results

Accuracy stars have one family-wide meaning:

| Accuracy | Stars |
|---:|---:|
| `>= 95%` | 5 |
| `>= 85%` and `< 95%` | 4 |
| `>= 70%` and `< 85%` | 3 |
| `>= 50%` and `< 70%` | 2 |
| `< 50%` | 1 |

Display exactly five positions and expose them as one localized semantic such as “n out of 5 stars”. An app may add speed, progress, or subject-specific metrics when pedagogically useful, but must not alter the meaning of accuracy stars. Handle empty input defensively even if normal navigation prevents an empty Results screen.

## 14. Privacy and permissions

The family baseline is no advertising, analytics, behavioural tracking, accounts, cloud dependency, or network requirement for learning. Request no unnecessary runtime permission. Preferences and learning state remain on-device unless a future product specification explicitly and transparently requires otherwise.

## 15. Build and release conventions

Before release, pass Android lint, JVM/unit tests, debug assembly, and release assembly/bundle checks. Enable release optimization, R8, and resource shrinking where applicable and verify the optimized build. Keep signing configuration secure: use Google Play App Signing with a separate upload key, load local signing values from an ignored properties file, fail clearly when required release credentials are missing, and never commit keystores, passwords, real signing properties, or generated release binaries.

Every product needs its own confirmed application ID/namespace, launcher and store assets, localized metadata/screenshots, and Google Play listing. Application IDs are permanent after publication and must be confirmed before module creation.

## 16. What must remain app-specific

Do not generalise merely to create apparent reuse. Subject/domain rules, question generation, learning sequences, maps, clocks, arithmetic algorithms, multiplication algorithms, and subject-specific animations remain in their product. Extract shared code only after genuine reuse is demonstrated and the abstraction preserves clarity.
