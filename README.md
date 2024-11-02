# MDPCoursework
- Estimated coursework time is 40 hours
- Due date is November 21
- Weight is 25% or 5 credits

# Assessment Criteria
| Basic Functionality                                                                                                        | Marks |
|----------------------------------------------------------------------------------------------------------------------------|-------|
| The application has an Activity that allows the user to view the songs / bookmarks on the device (Audiobook list)          | 1     |
| The application has an Activity that allows the user to set the playback speed and background color (Setting)              | 1     |
| The application has an Activity that allows the user to play, pause and stop a playing audiobook, as well as Skip track    | 1     |
| The Activity displays the current progress of the playback and the playback speed                                          | 2     |
| The currently chosen playback speed is passed to and displayed by the currently playing activity                           | 2     |
| The Activity maintains speed / playing AudioBook throughout its expected lifecycle (when rotating hone or receiveing call) | 2     |
| The application has a Service that handles playing the track when another application  is in the foreground                | 2     |
| The bookmarks load the specified audiobook at the specified time                                                           | 2     |
| The application displays a notification when a AudioBook is playing                                                        | 2     |
| Best Practice (and adherence to specification)                                                                             |       |
| The application supports appropriate navigation, and appropriate between and management of components. good design.        | 15    |
| Total                                                                                                                      | 30    |

# Step-by-Step Outline

### 1.  Setup and Initial Configuration
 - Create a new Android project in Android Studio.
 - Familiarize yourself with the provided AudiobookPlayer class, which uses the Android MediaPlayer object to play mp3 files.

  
### 2. Designing the Audiobook Activities
- Audiobook List Activity: Displays audiobooks and bookmarks on the device.
  - Lists audiobook files from /sdcard/Music.
  - Allows users to select and play an audiobook or click a bookmark to play from a specific timestamp.
- Audiobook Player Activity: Controls playback.
  - Add controls for play, pause, stop, skip, and display current progress and playback speed.
  - Include a bookmark creation feature at the specified playback time.
- Settings Activity: Configures playback settings.
  - Provide options to set playback speed and background color.

  
###  3. Implementing the Service for Background Playback
- Create a Service to handle playback, ensuring it continues when users switch to other apps or activities.
- Enable the Service to interact with activities via Intents for seamless user experience.

  
### 4. Adding a Notification
- Configure a notification to display while the audiobook is playing. This should include basic controls if feasible (play/pause).

  
### 5. Ensuring Lifecycle and Persistence Management
- Handle Activity lifecycles to ensure:
  - Playback continues across screen rotations.
  - State persistence, like maintaining playback speed and background color, across Activity transitions.

   
### 6. Testing
- Test on an emulated Pixel 5 (1080x1920, 420dpi) running Android API 34 to ensure compatibility and stability.
- Verify that all functionalities work as expected, including handling playback when other apps are in the foreground.\

  
### 7. Finalization and Submission
- Review and comment on code for clarity.
- Package the project as a .zip file containing the source code and compiled .apk.
- Submit via Moodle before the deadline 21/11/24.
