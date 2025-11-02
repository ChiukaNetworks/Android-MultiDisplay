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

+------------------------------------------------------------------------------------+
|                                  Vehicle Platform                                  |
|                                                                                    |
|  +----------------------+   +--------------------+   +--------------------------+  |
|  |   Vehicle HAL (VHAL) |<->|  CarService (AOSP) |<->| System Managers (AAOS)  |  |
|  |  speed/gear/parking  |   |  CarUx, Audio,     |   |  DisplayMgr, WMS, ATMS  |  |
|  |  seat occupancy etc. |   |  OccupantZone, etc |   |  InputMgr, AudioService |  |
|  +----------^-----------+   +---------^----------+   +-----------^--------------+  |
|             |                             |                          |             |
+-------------|-----------------------------|--------------------------|-------------+
              |                             |                          |
              |                             |                          |
              v                             v                          v
+------------------------------------------------------------------------------------+
|                                   System Layer                                     |
|                                                                                    |
|  +----------------------+    +----------------------+    +----------------------+   |
|  |  Display Orchestrator|    |   Input Router       |    |  UXR Policy Engine  |   |
|  |  (DO, priv app/svc)  |<-->| (IR, priv app/svc)   |<-->| (CarUx + VHAL)      |   |
|  |  - display↔zone map  |    | - touch/rotary focus |    | - restrictions per  |   |
|  |  - launch policy     |    | - steering key route |    |   occupant zone     |   |
|  |  - handoff rules     |    | - focus arbitration  |    |                      |   |
|  +----------^-----------+    +----------^-----------+    +----------^-----------+   |
|             |                           |                          |               |
|             |                           |                          |               |
|  +----------+-----------+    +----------+-----------+    +----------+-----------+   |
|  | App Instance Manager |    |  Performance Gov.   |    |  Audio Zone Router  |   |
|  | (AIM)                |    |  (PG)               |    | (CarAudio/Media)    |   |
|  | - task registry      |    | - FPS caps per disp |    | - per-zone sessions |   |
|  | - multi-instance     |    | - layer/thermal hint|    | - vol/route policy  |   |
|  +----------^-----------+    +----------^-----------+    +----------^-----------+   |
|             |                           |                          |               |
|  +----------+-----------+    +----------+-----------+    +----------+-----------+   |
|  | Compatibility Shell  |    | Seat/User Manager   |    | Policy Store (XML/DB)|   |
|  | (Presentation/TaskView|   | (per seat user)     |    | - app allowlists     |   |
|  |  /VirtualDisplay)     |   | - startAsUser(...)  |    | - UXR/app policies   |   |
|  +----------------------+    +----------------------+    +----------------------+   |
|                                                                                    |
+------------------------------------v-----------------------------------------------+
                                     |
                                     |
                             +-------+----------------------------------------------------+
                             |                  Android Framework                         |
                             |  SurfaceFlinger/HWC | WindowManager/ATMS | DisplayManager  |
                             |  InputManager       | ActivityOptions     | Media/Audio     |
                             +-----------^---------+----------^----------+--------^-------+
                                         |                    |                    |
+----------------------------------------------------------------------------------+-----+
|                                      App Layer                                         |
|                                                                                        |
|   +---------------------+   +---------------------+   +-----------------------------+   |
|   |  System Apps        |   |   User Apps         |   |   Compat Host (if needed)  |   |
|   |  (Nav, Media, etc.) |   |  (3P or OEM Apps)   |   |   - wraps single-display   |   |
|   |  - multi-display    |   |  - multi-window     |   |     apps for CDD/RPD       |   |
|   +----------^----------+   +----------^----------+   +--------------^--------------+   |
|              |                         |                             |                  |
|              +-------------------------+-----------------------------+                  |
|                            (Activity launches, state handoff, UXR hints)                |
+----------------------------------------------------------------------------------------+
Component Responsibilities (what each box owns)
System Layer (you own these as priv-apps/services)
Display Orchestrator (DO)

Maps physical displays ↔ occupant zones at boot/hotplug.

Enforces launch policy (what app can start where).

Performs handoff (“Send to CDD/RPD”), and places tasks via ActivityOptions.setLaunchDisplayId().

Integrates with AIM, UXR, Seat/User Manager.

Input Router (IR)

Maintains per-display focus; routes touch/rotary/keys to the correct focused task/window.

Binds steering wheel keys to the Driver zone.

Applies UXR gating to block restricted inputs on Driver display while moving.

UXR Policy Engine

Wraps CarUxRestrictionsManager and VHAL signals (speed, gear).

Publishes effective restrictions per zone/display to DO/IR and apps (SDK hints).

App Instance Manager (AIM)

Tracks (taskId, component, package, displayId).

Decides new instance vs. reuse and cross-display handoff.

Enforces single-writer or read-only policies when same data is opened on multiple displays.

Performance Governor (PG)

Sets frame rate caps per display; layer budgets; deco/codec budgets.

Applies thermal backoffs (e.g., rear displays drop to 30fps under heat).

Advises DO/AIM on LRU stopping of background tasks.

Audio Zone Router

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

Major Interfaces (how components talk)
DO → ATMS/WMS: place/launch tasks on specific displays.

DO ↔ AIM: query/update running task registry; decide reuse vs. new instance.

IR ↔ UXR Engine: IR subscribes to zone restrictions; gates keystrokes/rotary.

PG → Apps/WM: apply Window.setFrameRate(), advise LMK-friendly stops.

Audio Router ↔ CarAudioService: per-zone routing; set active sessions & volumes.

DO/IR ↔ Policy Store: read app rules (driver-only, rear-only, handoff allowed).

Compat Shell ↔ DO: invoked when a target app is not multi-display-aware.

Deployment Boundaries
Privileged System Apps/Services: DO, IR, UXR Engine, AIM, PG, Audio Router, Seat/User Manager, Compat Shell, Policy Store.

AAOS Framework (AOSP): CarService + managers, WMS/ATMS, InputManager, DisplayManager, AudioService, SurfaceFlinger/HWC.

Apps: System apps (Nav/Media), OEM/3P apps (prefer multi-window & resizeable).

Static Design Rationale
Separation of concerns: DO owns where/what to launch; IR owns who gets inputs; UXR owns what is legal; PG owns how fast/heavy things run; AIM owns which instance; Audio Router isolates sound per zone.

Policy centralization: a single Policy Store avoids scattering allowlists/restrictions.

Compatibility path: Compat Shell ensures “most apps just work” on CDD/RPD while you engage developers to add native multi-display support.

Safety first: UXR Engine sits on the critical path for anything that could distract the driver.

Static Views (quick cross-check)
Data view

VHAL → CarService → UXR Engine → IR/DO/AIM.

Policy Store (DB/XML) → DO/IR/AIM/PG/Audio Router.

Control view

User action on any display → IR (focus & event) → App task.

App launch request → DO (+AIM, Policy) → ATMS (placed on display).
