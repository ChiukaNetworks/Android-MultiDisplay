# Android-MultiDisplay
Android-MultiDisplay
Current State:
In the current software implementation, apps are designed with only the car's central display in mind, focusing primarily on the driver's needs.
3.2.2 User Story:
As a user, I want to have a Co-Driver Display (CDD) and Rear Passenger Display in my car. I would like most of the apps (both system and user apps) to be supported on these secondary displays.
3.2.3 Requirements:
•Implement support for multiple displays, including the central display, Co-Driver Display (CDD), and Rear Passenger Display. Ensure that each display can operate independently, allowing for different content and interactions on each screen.
•Independent Control: Design a system that allows for independent control of each display using various input methods, such as rotary controls and touch inputs. Ensure that input handling is intuitive and consistent across different displays.
•Driver Distraction Handling: Implement features to minimize driver distraction, ensuring that critical information is prioritized on the central display. Consider using voice-first and glanceable interfaces to reduce the need for driver interaction with secondary displays.
•App Compatibility: Ensure that both system and user apps are compatible with secondary displays. Design a mechanism to manage app instances across multiple displays, ensuring seamless transiions and interactions.
•Consider Performance Optimization: Optimize the system to handle multiple displays without compromising performance. Consider resource management strategies to ensure efficient use of system resources.

**Assumptions:**
1. Safety, Navi related apps in Main display only. Media, browsing related in all displays.
2. To handle synchronization in multiple displays, If an app is opened in one display, make other displays as read-only.

3. **Architecture:**
4. Android critical componenents used:
5. DisplayManager, WindowManager, ActivityTaskManager/TaskDisplayAreas.
•	Occupant Zones (CarOccupantZoneManager) ↔ display mapping.
•	Car UX Restrictions (speed/gear based).
•	Multi-user (optional per-seat).
•	CarAudioManager` audio zones; media routing.
•	InputManager (per-display focus), Rotary APIs.
•	SurfaceFlinger/HWC multi-display composition, setFrameRate().

3.1 Display & Zone Mapping:
   class ZoneMapper{}

Component Responsibilities (what each box owns)
System Layer (you own these as priv-apps/services)
1. Display Management:

1.1 Maps physical displays ↔ occupant zones at boot/hotplug.

1.2 Enforces launch policy (what app can start where).

1.3 Performs handoff (“Send to CDD/RPD”), and places tasks via ActivityOptions.setLaunchDisplayId().

Integrates with AIM, UXR, Seat/User Manager.

2. Input Router (IR)

Maintains per-display focus; routes touch/rotary/keys to the correct focused task/window.

Binds steering wheel keys to the Driver zone.

Applies UXR gating to block restricted inputs on Driver display while moving.

3. UXR Policy Engine

Wraps CarUxRestrictionsManager and VHAL signals (speed, gear).

Publishes effective restrictions per zone/display to DO/IR and apps (SDK hints).

4. App Instance Manager (AIM)

Tracks (taskId, component, package, displayId).

Decides new instance vs. reuse and cross-display handoff.

Enforces single-writer or read-only policies when same data is opened on multiple displays.

5. Performance Governor (PG)

Sets frame rate caps per display; layer budgets; deco/codec budgets.

Applies thermal backoffs (e.g., rear displays drop to 30fps under heat).

Advises DO/AIM on LRU stopping of background tasks.

6. Audio Zone Router

Configures CarAudioService zones and routes MediaSession per zone.

Maps volume keys to focused display’s zone; isolates rear vs. front audio.

Compatibility Shell

Hosts legacy single-display apps on CDD/RPD:

Presentation on target display, or

TaskView/ActivityView embedding, or

VirtualDisplay fallback (only if necessary).

Seat/User Manager

Optional per-seat multi-user isolation.

Launches apps with startActivityAsUser() using the zone’s user.

Policy Store (XML/Room/ContentProvider)

Central store for allowlists, UXR overrides, app → preferred zones, handoff rules.

Key Framework Dependencies
DisplayManager: enumerate displays, categories.

WindowManager / ActivityTaskManager: task placement, TDA, split rules.

ActivityOptions: setLaunchDisplayId(), optional multi-window flags.

InputManager: raw input routing; rotary events; focus.

CarService: CarOccupantZoneManager, CarUxRestrictionsManager, CarAudioManager.

SurfaceFlinger/HWC: multi-display composition; refresh rate control.

Audio Router ↔ CarAudioService: per-zone routing; set active sessions & volumes.

Compat Shell ↔ DO: invoked when a target app is not multi-display-aware.

Deployment Boundaries
Privileged System Apps/Services: DO, IR, UXR Engine, AIM, PG, Audio Router, Seat/User Manager, Compat Shell, Policy Store.

AAOS Framework (AOSP): CarService + managers, WMS/ATMS, InputManager, DisplayManager, AudioService, SurfaceFlinger/HWC.

Apps: System apps (Nav/Media), OEM/3P apps (prefer multi-window & resizeable).

Static Design Rationale
Separation of concerns: DO owns where/what to launch; IR owns who gets inputs; UXR owns what is legal; PG owns how fast/heavy things run; AIM owns which instance; Audio Router isolates sound per zone.

Policy centralization: a single Policy Store avoids scattering allowlists/restrictions.


