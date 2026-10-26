# SecondSight — Hazard-Awareness and Rider-Safety HUD

SecondSight is a rider-safety HUD for Ray-Ban Display glasses, built for the Meta Global AI Developer Hackathon (Startup Track, theme: Build for Your World). The glasses see the road ahead, detect hazards — obstacles, pedestrians, road signs, vehicles — and warn the rider audio-first, with a distinct sound per hazard class, so the rider's eyes stay on the road and they never have to look at a phone while moving. A minimal, glanceable HUD shows the camera view, any hazard banner, and a small set of factual readouts. The phone holds all controls; the glasses show the view and the HUD only. Detection is advisory only — SecondSight is an awareness aid, never a safety guarantee.

## Modes

SecondSight is the holding app (the GlassesApp / SecondSight platform). For the hackathon it ships two glasses modes, plus the platform's governance module:

- **AdScope — glasses mode.** The wearer sees only the live viewfinder with a heatmap overlay on the glasses — no buttons or controls on the HUD. All controls, scores, recommendations and exports live on the phone. AdScope segments advertising and signage regions in the camera view and weights them for the overlay.
- **VirtuRide — safety HUD mode.** Live hazard detection with audio-first warnings, plus a compact dashboard of factual readouts: speed, elevation, temperature, acceleration, cadence, heart rate (raw beats per minute, shown as a plain readout), ride duration and distance. A voice feature speaks factual status readouts on request — positions, speeds, readings — and nothing else: no advice, no recommendations.
- **Auditor — governance module.** A conversational governance auditor for AI agents: describe an AI agent design (or photograph its architecture) and get a scored audit against a 10-dimension risk rubric — autonomy bounds, termination and budget limits, accountability, memory governance, observability, acceptance gates, state ownership, trigger hygiene, policy versioning, and data boundaries — with concrete mitigations for each dimension.

## Declaration — pre-existing vs built during the event

- **Pre-existing:** the SecondSight concept and mode designs; the KOF 10-dimension governance rubric; and Ferdinand's existing KOF WhatsApp agent platform experience.
- **AdScope:** a pre-existing AdScope implementation (v17.15, in a separate private project) exists and is **not** part of this repository — this repo contains a fresh AdScope implementation built during the event.
- **VirtuRide:** pre-existing VirtuRide material is spec/design only — the implementation in this repo is built fresh during the event.
- **Built during the event (Oct 26 – Nov 4, 2026):** everything in this repository.

## Architecture overview

- **Phone app (this repo, Android/Kotlin):** owns all controls, sensor aggregation, and state.
- **Sensor inputs:** standard BLE GATT — Heart Rate service `0x180D` and Cycling Speed and Cadence service `0x1816` — connected directly from the phone; phone GPS for ground speed and elevation; and a deterministic mock feed for desk development when no sensors are present.
- **HUD layer:** a `HudState` (rider readouts + active hazards + hero metric) rendered through a `HudRenderer` interface. The Meta Wearables DAT display implementation plugs in behind that interface; a simple on-phone renderer stands in during the scaffold phase.
- **Hazard alerts:** audio-first. Each hazard class maps to a distinct sound pattern, delivered through a small alerter interface so the mapping is unit-testable.
- **Voice readouts:** a formatter turns the current state into short factual sentences for speech output.
- **AdScope:** a frame + segmented-regions state model behind a `SegmentationEngine` interface; the Meta Model API SAM 3.1 implementation is wired in during the event, and the glasses render only the viewfinder + heatmap overlay.
- **Auditor:** the rubric is an enum of the 10 dimensions with a deterministic placeholder scorer in this scaffold; the conversational layer via the Meta Model API is added later in the event.

## Status

Day 1 scaffold (Oct 26, 2026): project skeleton, sensor model with mock feed, BLE GATT parsing skeleton for heart rate and cycling speed/cadence, hazard classes with sound patterns, HUD state model, factual voice readout formatter, AdScope frame/segmentation skeleton, and the Auditor rubric skeleton. Live DAT display rendering and the Meta Model API layers come next.

## Safety disclaimer

SecondSight is an advisory awareness aid only. Detection can miss hazards or flag them late, and no output from this app is a safety guarantee. It provides factual readouts only — no health or medical advice, and no riding advice. Riders remain fully responsible for their own awareness and decisions, and must follow local road and distracted-driving rules.
