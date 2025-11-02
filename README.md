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
