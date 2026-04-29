# CalSnap Mobile UI Design Specification

> **Source:** Claude Design handoff bundle — CalSnap Mobile Flow  
> **Visual reference:** Cal AI (calai.app) — clean, minimal, big macro numbers  
> **Status:** Design complete · Implementation pending (see [MOBILE_UI_PHASES.md](./MOBILE_UI_PHASES.md))

---

## 1. Design Philosophy

| Principle | Meaning |
|---|---|
| **Clarity first** | Big numbers, high contrast, no decorative chrome |
| **Food is the hero** | Real photography, not icons — every meal card has a photo |
| **Instant feedback** | Every tap has haptic + visual response within 16ms |
| **Calm progress** | Ring fill and count-up animations communicate progress without anxiety |

---

## 2. Design Tokens

### 2.1 Color Palette

```kotlin
// CalSnapColors.kt
object CalSnapColors {
    // Surfaces
    val Background    = Color(0xFFFFFFFF)
    val Surface       = Color(0xFFFFF8F0)   // warm cream — onboarding, paywall
    val SurfaceAlt    = Color(0xFFF7F4EE)
    val Card          = Color(0xFFFFFFFF)
    val Border        = Color(0xFFEDE8DF)
    val Divider       = Color(0xFFF1ECE2)

    // Ink (text/icon)
    val Ink           = Color(0xFF0E0E0E)   // primary text, filled buttons
    val Ink2          = Color(0xFF1F1F1F)
    val Muted         = Color(0xFF7A7468)   // secondary labels
    val Mute2         = Color(0xFFA8A293)   // placeholder, hint
    val Hint          = Color(0xFFC9C2B2)   // ruler ticks, dividers

    // Brand
    val Red           = Color(0xFFE63946)   // accent, CTA, streak, protein
    val RedDark       = Color(0xFFC42E3A)
    val RedSoft       = Color(0xFFFDECEE)   // tip backgrounds, tinted cards

    // Macro accents
    val Carb          = Color(0xFFF4A23A)   // amber
    val CarbBg        = Color(0xFFFEF3E0)
    val Protein       = Color(0xFFE63946)   // same as Red
    val ProteinBg     = Color(0xFFFDECEE)
    val Fat           = Color(0xFF5A8DEF)   // steel blue
    val FatBg         = Color(0xFFE8EFFE)

    // Semantic
    val Good          = Color(0xFF2F8F4F)
    val GoodBg        = Color(0xFFE6F2EA)
    val Warn          = Color(0xFFD08A24)
}
```

### 2.2 Typography

```kotlin
// CalSnapTypography.kt
// Platform fonts resolve automatically:
//   iOS  → SF Pro Display / SF Pro Rounded (via FontFamily.Default)
//   Android → Roboto / Google Sans (via FontFamily.Default)

object CalSnapType {
    // Display — big calorie number on Plan Reveal & Scan Result
    val Display = TextStyle(
        fontSize = 84.sp,
        fontWeight = FontWeight.W700,
        letterSpacing = (-3).sp,
        fontFeatureSettings = "\"tnum\"",  // tabular-nums
        lineHeight = 84.sp,
    )

    // Hero — remaining kcal on Home card, count-up on Scan Result
    val Hero = TextStyle(
        fontSize = 64.sp,
        fontWeight = FontWeight.W700,
        letterSpacing = (-2.5).sp,
        fontFeatureSettings = "\"tnum\"",
    )

    // HeadlineLarge — screen titles ("What's your goal?")
    val HeadlineLarge = TextStyle(
        fontSize = 30.sp,
        fontWeight = FontWeight.W700,
        letterSpacing = (-1).sp,
        lineHeight = 33.sp,
    )

    // HeadlineMedium — card titles, section heads
    val HeadlineMedium = TextStyle(
        fontSize = 22.sp,
        fontWeight = FontWeight.W700,
        letterSpacing = (-0.5).sp,
    )

    // TitleLarge — Welcome hero copy ("Snap your food.")
    val TitleLarge = TextStyle(
        fontSize = 34.sp,
        fontWeight = FontWeight.W700,
        letterSpacing = (-1.2).sp,
        lineHeight = 36.sp,
    )

    // Body — standard content
    val Body = TextStyle(
        fontSize = 15.sp,
        color = Color(0xFF7A7468),  // Muted by default
        lineHeight = 22.sp,
        letterSpacing = (-0.2).sp,
    )

    // Label — uppercase caps labels ("PROTEIN", "DAILY AVERAGE")
    val Label = TextStyle(
        fontSize = 11.sp,
        fontWeight = FontWeight.W600,
        letterSpacing = 0.6.sp,
        // apply .uppercase() at call site
    )

    // ButtonLarge — primary CTA buttons
    val ButtonLarge = TextStyle(
        fontSize = 17.sp,
        fontWeight = FontWeight.W600,
        letterSpacing = (-0.2).sp,
    )
}
```

### 2.3 Spacing & Radius

```kotlin
object CalSnapSpacing {
    val xs  = 4.dp
    val sm  = 8.dp
    val md  = 16.dp
    val lg  = 24.dp
    val xl  = 32.dp
    val xxl = 48.dp

    // Screen horizontal padding
    val screenPad = 20.dp

    // Card internal padding
    val cardPadMd = 20.dp
    val cardPadLg = 24.dp
}

object CalSnapRadius {
    val sm   = 10.dp
    val md   = 14.dp
    val lg   = 18.dp
    val xl   = 22.dp
    val xxl  = 28.dp
    val pill = 999.dp  // fully round pills
    val card = 22.dp   // standard card
}
```

### 2.4 Shadows (Elevation)

```kotlin
object CalSnapShadow {
    // cardShadow — standard floating card
    // 0 1px 2px rgba(20,15,8,.04), 0 6px 24px rgba(20,15,8,.06)
    val Card = listOf(
        Shadow(color = Color(0x0A140F08), offset = Offset(0f, 1f), blurRadius = 2f),
        Shadow(color = Color(0x0F140F08), offset = Offset(0f, 6f), blurRadius = 24f),
    )

    // liftShadow — elevated / focused card
    val Lift = listOf(
        Shadow(color = Color(0x1A140F08), offset = Offset(0f, 8f), blurRadius = 32f),
        Shadow(color = Color(0x0F140F08), offset = Offset(0f, 2f), blurRadius = 6f),
    )
}
```

---

## 3. Component Library (Atoms)

### 3.1 `CalSnapRing` — Calorie progress ring

```
Usage: Home header card (220dp), Scan result mini (80dp), Empty state (120dp)

Parameters:
  size: Dp            — outer diameter (default 220dp)
  strokeWidth: Dp     — ring stroke (default 14dp)
  progress: Float     — 0f..1f (consumed / goal)
  color: Color        — stroke color (default Ink)
  track: Color        — track color (default Divider)
  content: @Composable — center slot (kcal number + label)

Visual spec:
  - SVG arc, startAngle = -90° (12 o'clock)
  - strokeLinecap = Round
  - track always full 360°
  - content slot: vertically centered inside ring
```

### 3.2 `CalSnapMacroBar` — Linear macro progress bar

```
Parameters:
  label: String       — "PROTEIN" / "CARBS" / "FAT"
  current: Float      — grams consumed
  target: Float       — grams target
  color: Color        — fill color (Protein/Carb/Fat token)

Layout:
  [LABEL]
  [currentG]  /targetg
  [████░░░░░░] 4dp height, rounded

Color assignment:
  Protein → Red (#E63946)
  Carbs   → Carb (#F4A23A)
  Fat     → Fat  (#5A8DEF)
```

### 3.3 `CalSnapFoodPhoto` — Food thumbnail with gradient fallback

```
Parameters:
  imageUrl: String?   — nullable; shows gradient if null
  name: String        — used for initial letter in fallback
  size: Dp            — width = height (default 56dp)
  cornerRadius: Dp    — default 14dp

Fallback: gradient using name[0] initial, warm hue palette
```

### 3.4 `CalSnapIcon` — Stroke icon set

```
Names: flame, camera, barcode, search, plus, minus, check, chev-l, chev-r,
       chev-d, close, home, chart, profile, sparkle, edit, water, fork,
       weight, star, arrow-r, flash, gallery, lock, bell, streak

Parameters:
  name: String
  size: Dp (default 22dp)
  color: Color (default Ink)
  strokeWidth: Float (default 1.8f)
```

### 3.5 `CalSnapButton` — Primary CTA button

```
Variants:
  Primary:   background=Ink, text=White, radius=30dp, height=58dp
  Brand:     background=Red, text=White, boxShadow=RedGlow
  Secondary: background=transparent, text=Ink, border optional

Full-width by default (fillMaxWidth).
```

### 3.6 `CalSnapBottomTabBar` — Main navigation bar

```
Tabs (left to right):
  1. Home    (icon: home)
  2. Stats   (icon: chart)
  3. [Snap]  — elevated FAB, 56×56dp, Ink background, camera icon
              marginTop = -22dp (floats above bar)
              boxShadow: 0 8px 24px rgba(0,0,0,.25)
  4. Log     (icon: fork)
  5. Profile (icon: profile)

Bar specs:
  height: ~56dp + 28dp safe area padding
  background: White
  borderTop: 1dp Divider
  active color: Ink
  inactive color: Mute2
  active indicator: animated Dp spring to active tab (tab bar morph anim)

Visibility: shown on Home, Stats, Profile only.
             hidden on camera, onboarding, paywall, scan result.
```

### 3.7 `CalSnapProgressDots` — Onboarding step indicator

```
5 segments (steps 0–4), filled up to current step.
Color: Ink (filled) / rgba(0,0,0,0.08) (unfilled)
Height: 3dp, rounded, gap: 4dp
Back chevron button left (36×36dp circle, rgba(0,0,0,0.05) bg)
```

---

## 4. Navigation Architecture

### 4.1 Screen Inventory

```
sealed class Screen {
    // Auth
    object Welcome             // entry — logged-out state
    object Login
    object Register

    // Onboarding (5-screen flow)
    object OnboardingGoal      // 02 — replaces current step 1
    object OnboardingBody      // 03 — ruler scrubbers
    object OnboardingActivity  // 04 — activity cards
    object OnboardingPlanReveal// 05 — big number reveal

    // Main (bottom tab bar visible)
    object Home
    object Analytics           // "Stats" tab
    object Profile

    // Scan flows (tab bar hidden)
    object AiScan              // camera
    object AiScanAnalyzing     // NEW — sweep line + ingredient labels
    object AiScanResult        // hero result + count-up

    // Logging
    object SearchFood          // "Log" tab entry
    object BarcodeScanner
    object MealDetail          // expanded meal entry (shared element target)

    // Account
    object Notifications
    object SettingsUnits
    object SettingsConnectedApps
    object SettingsPrivacy

    // Subscription
    object Paywall
    object SubscriptionStatus
}
```

### 4.2 Navigation Flow Diagram

```
[Welcome] ──► [Login] ──► [Home]
    │                        │
    ├──► [Register] ──────►  │
    │                        │
    └──► [OnboardingGoal]    │
              │              │
              ▼              │
         [OnboardingBody]    │
              │              │
              ▼              │
       [OnboardingActivity]  │
              │              │
              ▼              │
        [OnboardingPlanReveal] ──(optional)──► [Paywall] ──► [Home]
              │                                               ▲
              └──────────────────────────────────────────────►│

[Home] ─── tab bar ────────────────────────────────
   ├── Home tab     → [Home]
   ├── Stats tab    → [Analytics]
   ├── Snap FAB     → [AiScan] → [AiScanAnalyzing] → [AiScanResult]
   ├── Log tab      → [SearchFood] → [BarcodeScanner] / confirm
   └── Profile tab  → [Profile] → [Notifications]
                               → [SettingsUnits]
                               → [SettingsConnectedApps]
                               → [SettingsPrivacy]
                               → [Paywall]

[AiScanResult] ──► "Not right?" bottom sheet → [SearchFood]
                 └► "Refine portion" bottom sheet (inline)
```

### 4.3 Back-stack rules

- Onboarding is a **linear push stack** (back = previous step).
- Main app uses **tab reset** (selecting same tab pops to root of that tab).
- Scan flow is a **modal stack** dismissed back to its origin (Home or trial entry).
- Bottom sheets are **overlay**, not a navigation destination.

---

## 5. Screen Specifications

### 5.1 Welcome Screen (01)

```
Background: White
Layout (top→bottom):
  ┌──────────────────────────┐
  │  Hero image area  460dp  │  gradient food bowl illustration
  │  Logo top-center         │  CalSnap + camera icon
  ├──────────────────────────┤
  │  "Snap your food."       │  TitleLarge, Ink
  │  "Track your goals."     │  TitleLarge, Red accent
  │  Subtitle body copy      │  Body, Muted
  ├──────────────────────────┤
  │  [Get started →]         │  Primary button (Ink)
  │  Already have account?   │  14sp, tap → Login
  └──────────────────────────┘
```

### 5.2 Goal Pick (02)

```
Background: Surface (warm cream)
Progress dots: step 0/4
Content:
  "What's your goal?"  HeadlineLarge
  Subtitle body copy

  3 goal cards (vertical stack, gap 12dp):
    ┌─────────────────────────────────────┐
    │ [icon 48×48] Title       [○ radio] │
    │             Subtitle               │
    └─────────────────────────────────────┘
    Selected: border 2dp Ink, radio filled Ink
    Unselected: border transparent, bg White

  Red tip callout (sparkle icon, RedSoft bg):
    "Tip — most people lose 0.5–1 lb / week comfortably."

CTA: [Continue] Primary button
```

### 5.3 Body Profile (03)

```
"A few quick numbers"  HeadlineLarge
Subtitle: "Nothing leaves your phone."

Weight section:
  Label: CURRENT WEIGHT (uppercase, Muted)
  Value: 168 lb — Hero size (56sp, W700)
  Ruler: horizontal draggable ruler
    - 50 ticks, major every 5th
    - Red center cursor line
    - Fade edges (gradient overlay)
    - Haptic: SelectionHaptic per major tick

Height section: identical pattern → 5′10″

Age + Sex row (2 cards):
  ┌────────────┐ ┌────────────┐
  │ AGE        │ │ SEX        │
  │  32        │ │  Male      │
  └────────────┘ └────────────┘
  Tap → picker sheet
```

### 5.4 Activity Level (04)

```
"How active are you?"  HeadlineLarge
Subtitle: "Outside of intentional workouts."

5 activity cards (vertical, gap 10dp):
  ┌──────────────────────────────────────┐
  │ [1] Sedentary  desk / little exercise│
  │ [2] Lightly active  1–2x / week      │  ← selected
  │ [3] Moderate  3–5x / week            │
  │ [4] Very active  daily               │
  │ [5] Athlete  multiple / day          │
  └──────────────────────────────────────┘
  Selected: border 2dp Ink + check icon right
```

### 5.5 Plan Reveal (05)

```
Background: Surface
"✦ YOUR PLAN" pill badge (Ink bg, White text)

Headline: "Hit this every day and you'll lose 1 lb / wk."
  "1 lb / wk" → Red accent

Big number card (White, cardShadow):
  ┌────────────────────────────┐
  │  DAILY CALORIES            │
  │       2,140                │  Display (84sp) tabular-nums
  │  kcal · 500 below maint.  │
  │  ─────────────────────────│
  │ [Protein] [Carbs]  [Fat]  │  3 colored mini cards
  └────────────────────────────┘

Green callout: "✓ Goal date — reach 158 lb by Jul 14"

CTA: [Let's crush today →]  Brand button (Red), RedGlow shadow
```

### 5.6 Home Screen (Variant A — locked)

```
Top section (no app bar):
  ┌──────────────────────────────────────┐
  │ [Avatar]  Today, Apr 28       [🔥 7] │  streak pill RedSoft
  │ Hey Sam 👋                           │
  ├──────────────────────────────────────┤
  │  Big ring card (White, cardShadow):  │
  │  ┌──────────────────────────────┐   │
  │  │ Calories left     [2,140 goal]│  │
  │  │  1,328 kcal  ─── [RING 220dp]│  │
  │  │  Remaining        Ring 62%   │  │
  │  └──────────────────────────────┘   │
  │                                      │
  │  Macro bars row:                     │
  │  Protein ████░ 112/160g             │
  │  Carbs   ████████░ 198/220g         │
  │  Fat     ███░ 44/68g                │
  │                                      │
  │  [Breakfast 420 kcal]               │
  │    Eggs & toast · 8:30 AM · 380kcal │  MealRow
  │  [Lunch 620 kcal]                   │
  │    Caesar wrap · 12:45 PM           │  MealRow
  │  ...                                │
  └──────────────────────────────────────┘

Bottom: CalSnapBottomTabBar (Home active)

Empty state (no meals):
  Ring at 0%, value shows "2,140" full goal
  "Snap your first meal" motivational card
  How It Works 3-step strip
```

### 5.7 MealRow component

```
┌──────────────────────────────────────────┐
│ [Photo 56×56] FoodName           620kcal │
│               Meal · HH:MM               │
└──────────────────────────────────────────┘

Swipe left → reveals red [Delete] panel (80dp wide)
Threshold: 50pt show, 200pt commit
```

### 5.8 AI Scan — Camera

```
Background: #0A0A0A (near black)
Full-screen camera viewfinder (photo backdrop)
Vignette overlay: radial-gradient dark edges

Top bar (60dp from top):
  [×close]    ✦ AI Scan    [⚡flash]

Center overlay:
  Corner brackets (4 L-shaped corners, 36×36dp, white 3dp stroke, r=12dp)
  Hint text: "Center your meal in the frame" — 14sp White

Bottom section:
  Mode switcher pill row: [Scan ●] [Barcode] [Gallery]
    Active: White bg, Ink text
    Inactive: rgba(255,255,255,0.15) bg, White text

  Shutter button: 72×72dp circle, White fill, 4dp White ring outside
    Tap → shutter flash + photo fly-up animation → AiScanAnalyzing
```

### 5.9 AI Scan — Analyzing

```
Background: dark food photo (blurred/dimmed)
Full-screen sweep animation:

  Sweeping red line (top ↔ bottom, 1.6s loop):
    2dp height, gradient (transparent → Red → transparent)
    Red glow box-shadow

  Ingredient label pills (appear staggered 200ms each):
    ┌──────────┐
    │ ● Greens │  dark bg, Red border, White text
    └──────────┘
    Scale 0.8→1 + fade-in on appear

  Bottom card (slides up from bottom):
    "Analyzing your meal..." — 14sp White
    Spinner (Red border-top)
    "AI is identifying ingredients"
```

### 5.10 AI Scan — Result (Hero screen)

```
Top 40%: food hero photo (full-bleed, dark at bottom)
  Back button top-left (glass pill)
  "📸 AI Scan" badge top-center

Bottom 60% (White sheet, borderRadius 28dp top):
  ┌─────────────────────────────────────┐
  │  485 kcal                           │  Hero (64sp) tabular, count-up anim
  │  ↓ 23% under goal                  │  12sp Muted
  │                                     │
  │  [P 22g] [C 58g] [F 16g]           │  3 macro pills (fade-in + slide-up)
  │                                     │
  │  ─────────────────────────────────  │
  │  Food name: Quinoa Bowl             │
  │  Confidence: 94%  [bar]            │
  │                                     │
  │  [← Not right?]  [Add to Diary →]  │  2 buttons row
  └─────────────────────────────────────┘

"Not right?" → opens AI Correction bottom sheet
"Add to Diary" → meal type picker pill row → confirm log
```

### 5.11 AI Correction Bottom Sheet

```
Sheet 1 — Refine portion:
  Drag handle (36×4dp pill, Divider)
  "Refine portion"  H700
  Food name subtitle

  Portion selector: [½×] [1×●] [1.5×] [2×]
  Preview: "1 cup · 220 kcal"  surfaceAlt card

  [Confirm — 220 kcal]  Primary button

Sheet 2 — Swap food:
  "Not the right food?"  H700
  Search field (icon + placeholder)
  Alternative list: FoodCard rows with macro chips
```

### 5.12 Analytics Screen

```
Header: "Stats"  H700  +  [W|M|Y] segmented control

Daily average card:
  1,888 kcal · 88% of goal  (40sp tabular)
  Bar chart (7 bars): today bar = Red, others = Ink at 0.85 opacity
  Dashed goal line at 2,140

Macro split card:
  Stacked bar: Protein 25% | Carbs 47% | Fat 28%
  3-column legend with % and grams

Weight trend card:
  168.0 lb ↓ 4 lb (Green)
  SVG line chart with Red fill gradient
  "Last 8 weeks"
```

### 5.13 Profile Screen

```
Avatar (64×64dp gradient circle) — name initial
Name + email

Stats row:
  ┌──────────┬──────────┬──────────┐
  │ 168 lb   │   7 🔥   │  21 days │
  │ Weight   │  Streak  │  Tracked │
  └──────────┴──────────┴──────────┘

Settings list (arrow-right rows):
  Units & preferences  →
  Notifications        →
  Connected apps       →
  Privacy & data       →
  Upgrade to Premium   →  (if not premium)
  Sign out             (Red text)
```

### 5.14 Settings — Units & Preferences

```
[Weight unit]  lb / kg  (segmented toggle)
[Height unit]  ft,in / cm
[Daily goal]   text field (kcal override)
[Goal pace]    slider or picker
```

### 5.15 Settings — Notifications

```
Toggle rows:
  Meal reminders        ●──
  Breakfast (8:00 AM)   ──● (sub-row, appears when above on)
  Lunch (12:30 PM)      ──●
  Dinner (7:00 PM)      ──●
  Streak warning        ●──
  Weekly summary        ●──
```

### 5.16 Paywall Screen (end-of-onboarding variant)

```
Background: Surface (warm cream)
"✦ GO PREMIUM" badge pill (Ink)

Headline: "Track smarter.  Reach your goal faster."

Feature cards (3):
  [✦] Unlimited AI scans
  [✦] Weekly progress reports
  [✦] Connected apps (Apple Health, Google Fit)

Price row:
  [$9.99/mo]  or  [$59.99/yr  → 50% off  ●]
  Toggle between monthly / yearly

[Start 7-day free trial]  Brand button (Red)
[Maybe later]  Muted text link

Trust signals: lock icon + "Cancel anytime · Billed by App Store/Google Play"
```

### 5.17 Empty & Error States

| State | Trigger | UI |
|---|---|---|
| Home empty | No meals logged today | Motivational "Snap your first meal" card, ring at 0% |
| AI low confidence | Confidence < 50% | "Hmm, not quite sure 🤔" + 4 recovery options |
| Offline | No network | Offline banner + cached data shown greyed |
| Camera denied | Permission missing | Illustration + "Go to Settings" deep link |
| Barcode not found | No match in DB | "No match" card + manual entry CTA |

---

## 6. Animation Specifications

All 12 animations from the developer handoff:

### A1 — Ring fill on load
```
Trigger:   Home screen mount, day rollover
Duration:  900ms
Easing:    easeOutCubic (cubic-bezier(0.33, 1, 0.68, 1))
Compose:   animateFloatAsState(targetValue = progress, animationSpec = tween(900, easing = CubicBezierEasing(0.33f,1f,0.68f,1f)))
```

### A2 — Count-up reveal (Scan Result)
```
Trigger:   AiScanResult screen mount (80ms after photo settles)
Duration:  1100ms
Easing:    easeOutQuart (cubic-bezier(0.25, 1, 0.5, 1))
Target:    0 → final kcal integer
Macro pills: fade-in + translateY 8dp→0, staggered 80ms each
Compose:   animateIntAsState + LaunchedEffect(Unit) delay(80)
```

### A3 — Scan line sweep (Analyzing)
```
Trigger:   AiScanAnalyzing mount
Pattern:   InfiniteTransition, top → bottom, 1600ms, then reverse (pingPong)
Red glow:  box shadow 0 0 16px Red
Labels:    appear staggered 200ms; scale 0.8→1 + alpha 0→1, tween 250ms easeOut
```

### A4 — Shared element: meal row photo → detail hero
```
Trigger:   Tap meal row
Duration:  380ms
Curve:     cubic-bezier(0.2, 0.7, 0.2, 1)
API:       SharedTransitionLayout + Modifier.sharedElement("photo.{id}")
Size:      56×56dp → full width × ~200dp hero
```

### A5 — Pull to refresh
```
Threshold: 60pt
Haptic:    LightImpact at threshold, SuccessNotification on completion
Spinner:   Rotate on drag (pull * 6°), spin on release
Compose:   PullToRefreshBox (Material3)
```

### A6 — Swipe to delete
```
Threshold reveal:  50pt → show red Delete panel
Threshold commit:  200pt → confirm delete
Haptic:            MediumImpact on commit
Collapse:          animateContentSize 200ms on confirmed delete
Compose:           SwipeToDismissBox
```

### A7 — Bottom sheet spring
```
Trigger:   Any sheet (Add food, Refine portion, Swap food)
Spring:    stiffness=380f, dampingRatio=0.32f
Backdrop:  alpha 0→0.5, tween 180ms
Dismiss:   drag handle or swipe down
Compose:   ModalBottomSheet with spring(stiffness=380f, dampingRatio=0.32f)
```

### A8 — Tab bar morph indicator
```
Trigger:   Tab selection change
Spring:    stiffness=600f, dampingRatio=0.6f
Indicator: animateDpAsState for x-position of active underline/pill
Compose:   animateDpAsState + spring(...)
```

### A9 — Ruler scrub (Onboarding Body)
```
Trigger:   Drag gesture on weight / height ruler
Haptic:    SelectionHaptic every major tick (every 5 units)
Snap:      snap to nearest whole unit on release
Compose:   Draggable modifier + derived scroll offset + LaunchedEffect for haptic
```

### A10 — Streak pulse + confetti
```
Trigger:   New streak milestone reached
Pulse:     InfiniteTransition scale 1→1.15→1, 800ms, 3 repeats
Confetti:  12 particles, random angle 0–360°, radius 0→80dp, alpha 1→0, 600ms
Compose:   Custom Canvas particle system + LaunchedEffect
```

### A11 — Shutter → photo fly-up (Camera → Analyzing)
```
Trigger:   Shutter button tap
Step 1:    White flash overlay alpha 0→1→0, 200ms
Step 2:    Captured photo thumbnail animates from center → top-left, 300ms
Step 3:    Navigate to AiScanAnalyzing
Compose:   AnimatedVisibility + offsetAnimation
```

### A12 — Count-in entry animation (onboarding screens)
```
Trigger:   Each onboarding screen appear
Elements:  Headline + cards fade-in + translateY 24dp→0
Stagger:   40ms per element
Duration:  320ms easeOut
Compose:   AnimatedVisibility or manual animateFloatAsState on entry
```

---

## 7. Haptic Map

| Moment | Haptic type | Platform |
|---|---|---|
| Ruler tick (major) | SelectionFeedback | iOS: `UISelectionFeedbackGenerator` / Android: `KEYBOARD_TAP` |
| Swipe-to-delete commit | MediumImpact | iOS: `UIImpactFeedbackGenerator(.medium)` / Android: `CONFIRM` |
| Pull-to-refresh threshold | LightImpact | iOS: `UIImpactFeedbackGenerator(.light)` / Android: `CLOCK_TICK` |
| Pull-to-refresh complete | SuccessNotification | iOS: `UINotificationFeedbackGenerator(.success)` / Android: `LONG_PRESS` |
| Goal card selection | LightImpact | both |
| Activity card selection | LightImpact | both |
| Shutter tap | MediumImpact | both |
| Scan result reveal | SuccessNotification | both |
| Streak milestone | SuccessNotification | both |
| Error / scan fail | ErrorNotification | iOS: `UINotificationFeedbackGenerator(.error)` / Android: `REJECT` |

### Haptic `expect/actual` interface

```kotlin
// commonMain
expect object HapticFeedback {
    fun light()
    fun medium()
    fun selection()
    fun success()
    fun error()
}

// androidMain
actual object HapticFeedback {
    actual fun light()     { view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK) }
    actual fun medium()    { view.performHapticFeedback(HapticFeedbackConstants.CONFIRM) }
    actual fun selection() { view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP) }
    actual fun success()   { view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS) }
    actual fun error()     { view.performHapticFeedback(HapticFeedbackConstants.REJECT) }
}

// iosMain
actual object HapticFeedback {
    actual fun light()     { UIImpactFeedbackGenerator(UIImpactFeedbackStyleLight).impactOccurred() }
    actual fun medium()    { UIImpactFeedbackGenerator(UIImpactFeedbackStyleMedium).impactOccurred() }
    actual fun selection() { UISelectionFeedbackGenerator().selectionChanged() }
    actual fun success()   { UINotificationFeedbackGenerator().notificationOccurred(UINotificationFeedbackTypeSuccess) }
    actual fun error()     { UINotificationFeedbackGenerator().notificationOccurred(UINotificationFeedbackTypeError) }
}
```

---

## 8. Platform Differences (iOS vs Android)

| Aspect | iOS | Android |
|---|---|---|
| Font | SF Pro (FontFamily.Default) | Roboto / Google Sans |
| Bottom nav | Custom `CalSnapBottomTabBar` composable | M3 `NavigationBar` |
| Cards | White, soft shadow | M3 `Card` with `CardDefaults.elevatedCardColors` |
| Status bar | Light content on dark surfaces | `WindowCompat.setDecorFitsSystemWindows(false)` |
| Back gesture | Swipe from left edge (system) | Android back button / predictive back |
| Haptics | `UIFeedbackGenerator` family | `ViewCompat.performHapticFeedback` |

Handling in code:
```kotlin
val isIOS = remember { getPlatform().name.startsWith("iOS") }

@Composable
fun AppBottomBar(selectedTab: Tab, onSelect: (Tab) -> Unit) {
    if (isIOS) {
        CalSnapIOSTabBar(selectedTab, onSelect)
    } else {
        CalSnapAndroidNavBar(selectedTab, onSelect)
    }
}
```

---

## 9. Backend API Contracts Required

See full gap analysis in [MOBILE_UI_PHASES.md § Backend Gaps](./MOBILE_UI_PHASES.md#backend-gaps).

| # | Endpoint | Change | Phase needed |
|---|---|---|---|
| BE-01 | `GET /api/diary/today` | Add `streakDays: Int` to response | Phase 3 |
| BE-02 | `MealLogEntry` model | Add `loggedAt: String` (ISO-8601) | Phase 3 |
| BE-03 | `GET /api/onboarding/bmr-preview` | Add `estimatedGoalDate: String` | Phase 2 |
| BE-04 | `GET /api/diary/today` | Add `waterMl: Int`; new `POST /api/diary/water` | Phase 3 |
| BE-05 | `GET/PUT /api/user/notification-preferences` | New endpoint | Phase 6 |
| BE-06 | `GET/PUT /api/user/settings` | Add `unitSystem: "METRIC"/"IMPERIAL"` | Phase 6 |
| BE-07 | `GET /api/analytics/weight-history?range=90d` | New endpoint `[{date, weightKg}]` | Phase 6 |
| BE-08 | `GET /api/analytics/weekly` | Verify/add `{date, calories, protein, carbs, fat}[]` | Phase 6 |
| BE-09 | `PATCH /api/meal-logs/{id}` | Add `{quantityG?, foodItemId?}` update | Phase 4 |
| BE-10 | `DELETE /api/user` | New endpoint — account deletion | Phase 6 |
| BE-11 | `POST/DELETE /api/user/integrations/{provider}` | New — Apple Health / Google Fit | Phase 7 |

---

## 10. File Structure (target)

```
mobile/composeApp/src/commonMain/kotlin/com/company/app/
├── ui/
│   ├── theme/
│   │   ├── CalSnapColors.kt      ← NEW (Phase 0)
│   │   ├── CalSnapTypography.kt  ← NEW (Phase 0)
│   │   ├── CalSnapTokens.kt      ← NEW (Phase 0) — spacing, radius, shadow
│   │   └── CalSnapTheme.kt       ← NEW (Phase 0) — MaterialTheme wrapper
│   │
│   ├── components/               ← NEW folder (Phase 0)
│   │   ├── CalSnapRing.kt
│   │   ├── CalSnapMacroBar.kt
│   │   ├── CalSnapFoodPhoto.kt
│   │   ├── CalSnapIcon.kt
│   │   ├── CalSnapButton.kt
│   │   └── CalSnapBottomTabBar.kt
│   │
│   ├── platform/
│   │   └── HapticFeedback.kt     ← NEW expect (Phase 2)
│   │       (+ androidMain/iosMain actuals)
│   │
│   ├── onboarding/
│   │   ├── OnboardingGoalScreen.kt      ← NEW (Phase 2)
│   │   ├── OnboardingBodyScreen.kt      ← NEW (Phase 2)
│   │   ├── OnboardingActivityScreen.kt  ← NEW (Phase 2)
│   │   ├── OnboardingPlanRevealScreen.kt← NEW (Phase 2)
│   │   └── OnboardingViewModel.kt       ← UPDATE (Phase 2)
│   │
│   ├── home/
│   │   ├── HomeScreen.kt         ← REDESIGN (Phase 3)
│   │   └── HomeViewModel.kt      ← minor update (Phase 3)
│   │
│   ├── aiscan/
│   │   ├── AiScanScreen.kt       ← REDESIGN (Phase 4)
│   │   ├── AiScanAnalyzingScreen.kt ← NEW (Phase 4)
│   │   ├── AiScanResultScreen.kt ← REDESIGN (Phase 4)
│   │   └── AiScanViewModel.kt    ← UPDATE (Phase 4)
│   │
│   ├── logging/
│   │   ├── SearchFoodScreen.kt   ← REDESIGN (Phase 5)
│   │   ├── MealDetailScreen.kt   ← NEW (Phase 5)
│   │   └── BarcodeScannerScreen.kt ← REDESIGN (Phase 5)
│   │
│   ├── analytics/
│   │   └── AnalyticsScreen.kt    ← REDESIGN (Phase 6)
│   │
│   ├── profile/
│   │   ├── ProfileScreen.kt      ← REDESIGN (Phase 6)
│   │   ├── NotificationsScreen.kt← NEW (Phase 6)
│   │   ├── SettingsUnitsScreen.kt← NEW (Phase 6)
│   │   ├── SettingsAppsScreen.kt ← NEW (Phase 6)
│   │   └── SettingsPrivacyScreen.kt← NEW (Phase 6)
│   │
│   └── subscription/
│       └── PaywallScreen.kt      ← REDESIGN (Phase 7)
│
└── App.kt                        ← UPDATE navigation (Phase 1)
```
