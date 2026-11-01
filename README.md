# SecondSight — Hazard-Awareness and Rider-Safety HUD

SecondSight is a rider-safety HUD for Ray-Ban Display glasses, built for the Meta Global AI Developer Hackathon (Startup Track, theme: Build for Your World). The glasses see the road ahead, detect hazards — obstacles, pedestrians, road signs, vehicles — and warn the rider audio-first, with a distinct sound per hazard class, so the rider's eyes stay on the road and they never have to look at a phone while moving. A minimal, glanceable HUD shows the camera view, any hazard banner, and a small set of factual readouts. The phone holds all controls; the glasses show the view and the HUD only. Detection is advisory only — SecondSight is an awareness aid, never a safety guarantee.

## What is in this repository

SecondSight is a modular holding app (the GlassesApp / SecondSight platform). This repository contains the holding app and the mode built for this hackathon:

- **VirtuRide — safety HUD mode.** Live hazard detection with audio-first warnings, plus a compact dashboard of factual readouts: speed, elevation, temperature, acceleration, cadence, heart rate (raw beats per minute, shown as a plain readout), ride duration and distance. A voice feature speaks factual status readouts on request — positions, speeds, readings — and nothing else: no advice, no recommendations.

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

## Status

Day 1 scaffold (Oct 26, 2026): project skeleton, sensor model with mock feed, BLE GATT parsing skeleton for heart rate and cycling speed/cadence, hazard classes with sound patterns, HUD state model, and the factual voice readout formatter. Live DAT display rendering and the Meta Model API layers come next.

## Safety disclaimer

SecondSight is an advisory awareness aid only. Detection can miss hazards or flag them late, and no output from this app is a safety guarantee. It provides factual readouts only — no health or medical advice, and no riding advice. Riders remain fully responsible for their own awareness and decisions, and must follow local road and distracted-driving rules.

## Caution — safety and regulatory note

Before building VirtuRide, we researched how Canadian ground-transportation law treats displays and devices: the federal Motor Vehicle Safety framework and Transport Canada's driver-assistance guidance, Quebec's Highway Safety Code (sections 443.1 and 443.2), Ontario's Highway Traffic Act (section 78), British Columbia's Motor Vehicle Act (section 214.2), and the other provincial distracted-driving regimes. No Canadian statute regulates head-up displays by name, and no court here has tested a smart-glasses HUD, so we designed conservatively, against the rules that do exist.

VirtuRide is a concept built on one principle: minimal distraction in the service of safety. The HUD is deliberately minimalist — while the rider is moving it shows hazard warnings and speed only, placed peripherally in the normal line of sight, without obstructing the view of the road. It carries no messages, media, social or web content. The full interface is available only when stopped. Audio alerts use open-ear speakers rather than earphones, so the rider keeps full awareness of surrounding traffic sounds. The system is advisory only: it warns, and the rider decides. VirtuRide is designed for cycling and is not intended for use while driving a motor vehicle.

Please treat this as a caution, not a clearance. Parts of the existing law were written for phones and in-vehicle screens — Quebec, for example, frames its screen exception around displays integrated into or mounted on the vehicle — so how a worn display is treated remains an open, untested question, and the rider is responsible for complying with the laws of the jurisdiction they are riding in. Our research informed the design; it is not legal advice, and it is not a claim of certified compliance in any province.
