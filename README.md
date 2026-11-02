# SecondSight — Hazard-Awareness and Rider-Safety HUD

SecondSight is a rider-safety HUD for Ray-Ban Display glasses, built for the Meta Global AI Developer Hackathon (Startup Track, theme: Build for Your World). The glasses see the road ahead, detect hazards — obstacles, pedestrians, road signs, vehicles — and warn the rider audio-first, with a distinct sound per hazard class, so the rider's eyes stay on the road and they never have to look at a phone while moving. A minimal, glanceable HUD shows the camera view, any hazard banner, and a small set of factual readouts. The phone holds all controls; the glasses show the view and the HUD only. Detection is advisory only — SecondSight is an awareness aid, never a safety guarantee.

## What is in this repository

SecondSight is a modular holding app (the GlassesApp / SecondSight platform). This repository contains the holding app and the mode built for this hackathon:

- **VirtuRide — safety HUD mode.** Live hazard detection with audio-first warnings, plus a compact dashboard of factual readouts: speed, elevation, temperature, acceleration, cadence, heart rate (raw beats per minute, shown as a plain readout), ride duration and distance. A voice feature speaks factual status readouts on request — positions, speeds, readings — and nothing else: no advice, no recommendations.

**Ported in on Nov 2:** the VirtuRide ride layer from the development build — GPS tracking, personal Safety Ranges and the ride controller (the `virturide/` package), wired into the app shell with its location-permission gate. Build verified by CI (GitHub Actions, assembleDebug green).

The platform is designed to host further modes. Additional modes are developed in a separate private project; they are not part of this repository or this submission, and may be connected to the platform after the event.

## Declaration — pre-existing vs built during the event

- **Pre-existing:** the SecondSight concept and mode designs, and Ferdinand's existing KOF Services platform experience.
- **Built during the event (Oct 26 – Nov 4, 2026):** everything in this repository.

## Architecture overview

- **Phone app (this repo, Android/Kotlin):** owns all controls, sensor aggregation, and state.
- **Sensor inputs:** standard BLE GATT — Heart Rate service `0x180D` and Cycling Speed and Cadence service `0x1816` — connected directly from the phone; phone GPS for ground speed and elevation; and a deterministic mock feed for desk development when no sensors are present.
- **HUD layer:** a `HudState` (rider readouts + active hazards + hero metric) rendered through a `HudRenderer` interface. The Meta Wearables DAT display implementation plugs in behind that interface; a simple on-phone renderer stands in during the scaffold phase.
- **Hazard alerts:** audio-first. Each hazard class maps to a distinct sound pattern, delivered through a small alerter interface so the mapping is unit-testable.
- **Voice readouts:** a formatter turns the current state into short factual sentences for speech output.

## Status — what works, what's next

Everything under "works" is verified on real hardware (Ray-Ban Display + Pixel 9 Pro, Oct 28–31, 2026). This project is a proof of concept built for the Meta Global AI Developer Hackathon; the limitations list is deliberately public — it is the roadmap.

### What works today

- Live hazard detection at frame rate with warnings spoken in the glasses; the on-device layer never waits for the network. Proven the hard way on the Oct 31 road ride: for about two minutes every Meta API call failed on DNS, the on-device detection and warnings never stopped, and the debrief completed the moment coverage returned.
- Muse Spark hazard classification in the loop: in one parking-lot field test, Spark confirmed 3 hazards (correcting 1 on-device label, vehicle → pedestrian) and rejected 4 detections as non-hazards (NONE), suppressing them.
- Muse Spark debrief after every ride, from aggregate safety numbers only. The Oct 31 road ride's debrief was generated from live ride data minutes after the ride.
- The traveller's own BLE sensors, direct, no vendor SDK: heart rate with RR intervals → HRV (RMSSD), a fatigue indicator, and VOX, an effort-stability index; Safety Ranges with HUD colour states and spoken limit alerts (verified by deliberately triggering an over-limit state).
- NAV-lite: spoken turn prompts on OpenStreetMap routes (full street address routed, 15 turns, Oct 29).
- Ride survival: a glasses-session drop mid-ride auto-finalises the ride and restores it from a snapshot — proven in the field Oct 29; the debrief still generated.
- Plain-language sensor states and status dots (green only when a sensor is truly delivering its data).

**Known limitations today (honest list):**
- Announcement discipline in clutter: in a parking lot the on-device labeler fires often (carts, shelf stock, parked cars), and the audio announces detections immediately while Spark's verdict lands seconds later. Spark's NONE verdicts already filter part of it; tuning the coupling is the top post-hackathon enhancement (below).
- Cadence: the BK8 sensor must be switched to its cadence mode and mounted on the crank arm; wheel data is live today, and crank support is built and waiting on that physical step.
- The glasses' native video recording and the DAT camera stream are exclusive: filming with the glasses pauses detection for the duration (platform behaviour, observed Oct 31).
- The DAT session layer can drop mid-ride (platform behaviour, raised in the event's office hours); the watchdog + snapshot restore is the mitigation, not a cure.
- SpO2 cannot be derived from the current single-wavelength heart-rate band — that needs different hardware.

**After the hackathon (the plan):**
- Clutter-adaptive announcement discipline: feed classification results (NONE rate, scene density) back into persistence, relevance and audio cooldowns, so busy environments quiet down automatically while real hazards still cut through.
- An expanded indicator detail view (factual readouts only); VO2 max as a clearly labelled estimate only; wider BLE sensor support.
- Beta hardening: in-app sensor setup guidance, onboarding, and longer real-road validation.

**Contributors:**
- Ferdinand Ephrem (KOF Services) — creator. VirtuRide was designed, directed and field-tested by him during the event window (26 Oct – 4 Nov 2026).
- Musée — programming assistant and senior dev lead, leading a team of agents at KOF Services.

VirtuRide was designed and field-tested by Ferdinand Ephrem during the event window, and built with Musée and the KOF agent team.

**Links and form fields:**
- Demo video (approved v9, 141 s): https://www.youtube.com/watch?v=cbdr_CkdqGI (mirror: https://drive.google.com/file/d/1GRq4efVObQoeRchmKrmluWpihNbVH7CV/view)
- Public repo: https://github.com/FerEph/secondsight-hackathon
- X demo post (Most Viral field): https://x.com/kofservicesinc/status/2117247726262145134
- Try it: the APK reads its Meta Model API key from the app's Settings (a reviewer can supply their own key); with no key or no network, the on-device detection layer runs standalone, and the recorded rides, logs and demo video in the repo show the full Meta AI loop.

## Safety disclaimer

SecondSight is an advisory awareness aid only. Detection can miss hazards or flag them late, and no output from this app is a safety guarantee. It provides factual readouts only — no health or medical advice, and no riding advice. Riders remain fully responsible for their own awareness and decisions, and must follow local road and distracted-driving rules.

## Caution — safety and regulatory note

Before building VirtuRide, we researched how Canadian ground-transportation law treats displays and devices: the federal Motor Vehicle Safety framework and Transport Canada's driver-assistance guidance, Quebec's Highway Safety Code (sections 443.1 and 443.2), Ontario's Highway Traffic Act (section 78), British Columbia's Motor Vehicle Act (section 214.2), and the other provincial distracted-driving regimes. No Canadian statute regulates head-up displays by name, and no court here has tested a smart-glasses HUD, so we designed conservatively, against the rules that do exist.

VirtuRide is a concept built on one principle: minimal distraction in the service of safety. The HUD is deliberately minimalist — while the rider is moving it shows hazard warnings and speed only, placed peripherally in the normal line of sight, without obstructing the view of the road. It carries no messages, media, social or web content. The full interface is available only when stopped. Audio alerts use open-ear speakers rather than earphones, so the rider keeps full awareness of surrounding traffic sounds. The system is advisory only: it warns, and the rider decides. VirtuRide is designed for cycling.

Please treat this as a caution, not a clearance. Parts of the existing law were written for phones and in-vehicle screens — Quebec, for example, frames its screen exception around displays integrated into or mounted on the vehicle — so how a worn display is treated remains an open, untested question, and the rider is responsible for complying with the laws of the jurisdiction they are riding in. Our research informed the design; it is not legal advice, and it is not a claim of certified compliance in any province.
