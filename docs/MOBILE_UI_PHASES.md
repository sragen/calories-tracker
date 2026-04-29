# CalSnap Mobile UI — Implementation Phases

> **Design spec:** [MOBILE_UI_DESIGN.md](./MOBILE_UI_DESIGN.md)  
> **Stack:** Kotlin Multiplatform + Compose Multiplatform 1.7.3  
> **Approach:** Phase 0 first (tokens) → each phase builds on previous

---

## Overview

```
Phase 0    Phase 1    Phase 2    Phase 3    Phase 4    Phase 5    Phase 6    Phase 7
Tokens  →  NavArch →  Onboarding→  Home   →  AiScan  →  Logging →  Account →  Polish
(0 BE)     (0 BE)     (BE-03)    (BE-01,   (BE-09)    (0 BE)     (BE-05..   (0 BE)
                                  02, 04)                          BE-11)
```

---

## Backend Gaps (full list)

| ID | Endpoint | Required change | Needed in |
|---|---|---|---|
| BE-01 | `GET /api/diary/today` | Add `streakDays: Int` | Phase 3 |
| BE-02 | `MealLogEntry` model | Add `loggedAt: String` ISO-8601 | Phase 3 |
| BE-03 | `GET /api/onboarding/bmr-preview` | Add `estimatedGoalDate: String` ("Jul 14") | Phase 2 |
| BE-04 | `GET /api/diary/today` + new `POST /api/diary/water` | Add `waterMl: Int` to diary response | Phase 3 |
| BE-05 | `GET/PUT /api/user/notification-preferences` | New endpoint — reminder toggles + times | Phase 6 |
| BE-06 | `GET/PUT /api/user/settings` | Add `unitSystem: "METRIC"/"IMPERIAL"` | Phase 6 |
| BE-07 | `GET /api/analytics/weight-history?range=90d` | New — returns `[{date: String, weightKg: Float}]` | Phase 6 |
| BE-08 | `GET /api/analytics/weekly` | Verify/add `[{date, calories, protein, carbs, fat}]` | Phase 6 |
| BE-09 | `PATCH /api/meal-logs/{id}` | Body: `{quantityG?: Float, foodItemId?: Long}` | Phase 4 |
| BE-10 | `DELETE /api/user` | Account deletion | Phase 6 |
| BE-11 | `POST/DELETE /api/user/integrations/{provider}` | Apple Health / Google Fit tokens | Phase 7 |

---

## Phase 0 — Design Tokens & Atom Components

**Goal:** Zero screens change. Establish the visual foundation every subsequent phase uses.

**Deliverables:**

| File | Action | Notes |
|---|---|---|
| `ui/theme/CalSnapColors.kt` | CREATE | All `CS.*` tokens as Compose Color vals |
| `ui/theme/CalSnapTypography.kt` | CREATE | Display, Hero, HeadlineLarge, Body, Label, ButtonLarge |
| `ui/theme/CalSnapTokens.kt` | CREATE | Spacing, radius, shadow constants |
| `ui/theme/CalSnapTheme.kt` | CREATE | MaterialTheme wrapper applying above |
| `ui/components/CalSnapRing.kt` | CREATE | 220dp calorie ring — Canvas drawArc |
| `ui/components/CalSnapMacroBar.kt` | CREATE | Linear macro progress bar atom |
| `ui/components/CalSnapFoodPhoto.kt` | CREATE | Photo thumbnail + gradient fallback |
| `ui/components/CalSnapIcon.kt` | CREATE | Stroke icon set (22 icons) |
| `ui/components/CalSnapButton.kt` | CREATE | Primary, Brand, Secondary variants |
| `ui/components/CalSnapBottomTabBar.kt` | CREATE | Tab bar with elevated Snap FAB |
| `ui/platform/HapticFeedback.kt` | CREATE | `expect` interface |
| `ui/platform/HapticFeedback.android.kt` | CREATE | `actual` Android impl |
| `ui/platform/HapticFeedback.ios.kt` | CREATE | `actual` iOS impl |
| `App.kt` | UPDATE | Wrap in `CalSnapTheme {}` |

**Acceptance criteria:**
- `CalSnapTheme {}` compiles and renders on both platforms
- All atom components render in isolation (Compose Preview)
- HapticFeedback.light() / .medium() / .success() callable from commonMain

**BE deps:** None  
**Animations:** None (foundation only)

---

## Phase 1 — Navigation Architecture

**Goal:** Replace top-bar icon navigation with bottom tab bar. No visual redesign yet.

**Deliverables:**

| File | Action | Notes |
|---|---|---|
| `ui/navigation/Screen.kt` | UPDATE | Add: `OnboardingGoal`, `OnboardingBody`, `OnboardingActivity`, `OnboardingPlanReveal`, `AiScanAnalyzing`, `MealDetail`, `Notifications`, `SettingsUnits`, `SettingsConnectedApps`, `SettingsPrivacy` |
| `App.kt` | REFACTOR | Replace flat `when(currentScreen)` with tab-aware structure; `CalSnapBottomTabBar` visible on Home/Analytics/Profile tabs |
| `ui/home/HomeScreen.kt` | MINOR | Remove `TopAppBar`; delete old emoji icon buttons; navigation now via tab bar |

**Back-stack rules (from design spec):**
- Onboarding: linear push stack
- Main: tab reset on re-tap
- Scan/modals: modal stack, dismiss returns to origin

**Acceptance criteria:**
- Bottom tab bar visible on Home, Stats, Profile
- Tab bar hidden on camera, onboarding, paywall, scan result
- Snap FAB opens AiScan
- All existing screens still reachable

**BE deps:** None  
**Animations:** Tab bar morph (A8) — `animateDpAsState` spring indicator

---

## Phase 2 — Onboarding Redesign (5 Screens)

**Goal:** Replace 2-step OutlinedTextField form with 5-screen Cal AI-style flow.

**Current flow:** Welcome → (Onboarding: Step1 body form → Step2 goal/preview)  
**New flow:** Welcome → OnboardingGoal → OnboardingBody → OnboardingActivity → OnboardingPlanReveal

**Deliverables:**

| File | Action | Screen |
|---|---|---|
| `ui/welcome/WelcomeScreen.kt` | REDESIGN | 01 — hero photo, "Snap your food / Track your goals", Get started + Sign in |
| `ui/onboarding/OnboardingGoalScreen.kt` | NEW | 02 — 3 goal cards with selection state + red tip callout |
| `ui/onboarding/OnboardingBodyScreen.kt` | NEW | 03 — ruler scrubbers for weight/height + age/sex cards |
| `ui/onboarding/OnboardingActivityScreen.kt` | NEW | 04 — 5 numbered activity cards |
| `ui/onboarding/OnboardingPlanRevealScreen.kt` | NEW | 05 — big 2,140 kcal, macro plan, goal date, "Let's crush today" |
| `ui/onboarding/OnboardingViewModel.kt` | UPDATE | Expose `estimatedGoalDate` from bmr-preview response |
| `App.kt` | UPDATE | Route through 5 onboarding steps |

**BE deps:** BE-03 (estimatedGoalDate on plan reveal)

**Animations:**
- A9 (ruler scrub with selection haptic)
- A12 (screen entry: fade + translateY stagger)
- Progress dots animate on step advance

**Acceptance criteria:**
- Full 5-screen flow navigable on iOS and Android simulator
- Ruler scrubber responds to drag, snaps to integer, triggers haptic on major ticks
- Plan reveal shows real numbers from bmr-preview API
- "Let's crush today" navigates to Home (or Paywall if paywall variant toggled)

---

## Phase 3 — Home Screen Redesign (Variant A)

**Goal:** Full Cal AI-style home dashboard. This is the screen users spend 80% of time on.

**Deliverables:**

| Component | Notes |
|---|---|
| `HomeHeader` | Avatar (initials gradient), date, streak pill (RedSoft bg) |
| Big calorie ring card | `CalSnapRing` 220dp, remaining kcal Hero text, goal label |
| Macro bars row | `CalSnapMacroBar` × 3 (Protein/Carbs/Fat) |
| `MealSection` × 4 | Breakfast/Lunch/Dinner/Snack with `MealRow` |
| `MealRow` | Photo thumb 56dp, name, timestamp (HH:MM), kcal right-aligned |
| Empty state | Motivational card + 3-step How It Works strip |
| HomeViewModel | Map new `streakDays` and `loggedAt` fields from API |

**BE deps:** BE-01 (streak), BE-02 (loggedAt timestamp), BE-04 (water)

**Animations:**
- A1 (ring fill on mount, 900ms easeOutCubic)
- A5 (pull-to-refresh: 60pt threshold, light + success haptic)
- A6 (swipe-to-delete: 50pt/200pt thresholds, medium haptic, collapse 200ms)

**Acceptance criteria:**
- Ring animates from 0 to actual progress on Home mount
- Swipe left on meal row reveals red Delete panel
- Streak badge shows live streak from API
- Meal rows show time (e.g., "8:30 AM")
- Empty state shows when no meals logged

---

## Phase 4 — AI Scan Flow (3 Screens + Correction)

**Goal:** Redesign the hero feature. Camera → Analyzing → Result is the app's #1 conversion moment.

**Deliverables:**

| File | Action | Notes |
|---|---|---|
| `AiScanScreen.kt` | REDESIGN | Dark viewfinder, corner brackets, mode switcher, shutter button |
| `AiScanAnalyzingScreen.kt` | NEW | Sweep line anim + staggered ingredient label pills |
| `AiScanResultScreen.kt` | REDESIGN | Hero photo top 40%, count-up kcal, macro pills, 2 CTAs |
| `AiCorrectionSheet.kt` | NEW | "Refine portion" + "Swap food" bottom sheets |
| `AiScanViewModel.kt` | UPDATE | Expose state for new Analyzing screen; support PATCH for correction |

**BE deps:** BE-09 (PATCH meal-log for correction)

**Animations:**
- A2 (count-up 0→kcal, 1100ms, 80ms stagger for macro pills)
- A3 (scan line sweep, 1600ms pingPong loop, staggered label pills)
- A7 (bottom sheet spring: stiffness=380, damping=32)
- A11 (shutter → photo fly-up → navigate to Analyzing)

**Acceptance criteria:**
- Analyzing screen plays scan line animation while AI call is in-flight
- Result screen kcal count-up plays on mount
- "Not right?" opens correction sheet
- Refine portion updates kcal preview in real-time
- Corrected log posts via PATCH to backend

---

## Phase 5 — Logging Screens (Barcode + Search + Meal Detail)

**Goal:** Visual consistency across all food-logging entry points.

**Deliverables:**

| File | Action | Notes |
|---|---|---|
| `BarcodeScannerScreen.kt` | REDESIGN | Dark camera UI, consistent with AiScan |
| `SearchFoodScreen.kt` | REDESIGN | Search bar + pill filter (All/My Foods/Recent), food cards with macro chips |
| `MealDetailScreen.kt` | NEW | Full meal detail — shared element target, edit quantity, delete |

**Animations:**
- A4 (shared element meal row photo → MealDetail hero, 380ms)
- A7 (bottom sheet spring for quantity edit)

**BE deps:** None new

---

## Phase 6 — Analytics, Profile, Settings

**Goal:** Complete the secondary screens with consistent design language.

**Deliverables:**

| File | Notes |
|---|---|
| `AnalyticsScreen.kt` | Weekly bar chart (Ink/Red bars), macro stacked bar, weight SVG line chart |
| `ProfileScreen.kt` | Avatar, stats row (weight/streak/days), settings list |
| `NotificationsScreen.kt` | Toggle rows with sub-rows for reminder times |
| `SettingsUnitsScreen.kt` | Segmented unit toggles |
| `SettingsConnectedAppsScreen.kt` | Apple Health / Google Fit connect buttons |
| `SettingsPrivacyScreen.kt` | Data export + delete account (destructive, Red) |

**BE deps:** BE-05, BE-06, BE-07, BE-08, BE-10, BE-11

**Animations:**
- Bar chart entrance: bars animate height 0→value, staggered 50ms, on screen mount

---

## Phase 7 — Paywall Redesign + Final Polish

**Goal:** End-of-onboarding paywall variant + global animation polish pass.

**Deliverables:**

| File | Notes |
|---|---|
| `PaywallScreen.kt` | REDESIGN — 3 feature cards, price toggle monthly/yearly, trust signals |
| Streak confetti | A10 — pulse loop + 12-particle Canvas burst on new milestone |
| Full haptic audit | Verify all A1–A12 haptic moments implemented correctly |
| Transition timing audit | Verify all durations/easings match spec |

**BE deps:** None new  
**BE-11** (Connected apps) can be wired when available.

---

## Phase Dependency Graph

```
Phase 0 (Tokens)
    │
    ▼
Phase 1 (Nav)
    │
    ▼
Phase 2 (Onboarding) ── needs BE-03
    │
    ▼
Phase 3 (Home)       ── needs BE-01, BE-02, BE-04
    │
    ├──► Phase 4 (AI Scan)  ── needs BE-09
    │
    ├──► Phase 5 (Logging)
    │
    ▼
Phase 6 (Analytics/Profile) ── needs BE-05..10
    │
    ▼
Phase 7 (Polish)
```

---

## Definition of Done (per phase)

- [ ] All new composables have `@Preview` functions
- [ ] No hardcoded colors outside `CalSnapColors.*`
- [ ] No hardcoded sizes outside `CalSnapTokens.*`
- [ ] Haptic calls use `HapticFeedback.*` (not direct platform APIs)
- [ ] Animations match spec durations/easings within ±50ms
- [ ] Screen compiles and runs on both Android emulator and iOS simulator
- [ ] Navigation forward and back tested manually
