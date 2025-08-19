<div align="center">
  <img src="app/src/main/ic_launcher-playstore.png" alt="Listify Logo" width="128" height="128">
</div>

# Listify

A beautiful, intuitive task management app designed to help you track your endless JIRA tickets and daily tasks with style.

<div align= "center"> 
<img src="https://img.shields.io/badge/Kotlin-B125EA?style=for-the-badge&logo=kotlin&logoColor=white" alt="Kotlin"/>
<img src="https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=for-the-badge&logo=Jetpack%20Compose&logoColor=white" alt="Jetpack Compose"/>
<img src="https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java"/>
<img src="https://img.shields.io/badge/Material%20UI-007FFF?style=for-the-badge&logo=mui&logoColor=white" alt="Material UI" />
 </div>


##  Features

### Personalized Onboarding Experience
- Customize your profile with a profile picture and personal details
- Choose from a variety of theme colors to personalize your experience
- Smooth onboarding flow that remembers your preferences

### Smart Task Management
- **Create Tasks**: Easily add tasks with titles, categories, deadlines, and descriptions
- **Categorization**: Organize tasks with predefined categories (JIRA, Lunch, Sync-Up, Personal, Meeting)
- **Flexible Timing**: Set specific start/end times for duration-based tasks or simple deadlines
- **Task Tracking**: Monitor your progress with a visual progress summary

### Intuitive Task Interaction
- **Swipe Gestures**: 
  - Swipe right to delete tasks
  - Swipe left to mark tasks as complete
- **Expandable Details**: Tap on tasks to view detailed information
- **Visual Feedback**: Color-coded indicators for completed vs pending tasks

### Smart Notifications
- **Automated Reminders**: Get notified 10 minutes before task deadlines
- **Notification Center**: Keep track of all your task reminders in one place
- **Quick Actions**: Cancel tasks directly from notifications

### Beautiful UI/UX
- **Material Design 3**: Modern, clean interface following the latest design principles
- **Personalized Themes**: App adapts to your chosen color preference
- **Smooth Animations**: Elegant transitions between screens
- **Responsive Design**: Works beautifully on all screen sizes

## Getting Started

### Welcome Flow
1. Launch Listify for the first time
2. Experience the personalized welcome screen
3. Complete your profile setup with:
   - Profile picture (optional)
   - Your name and occupation
   - Favorite theme color

### Creating Your First Task
1. Tap the "+" floating action button on the homepage
2. Enter task details:
   - Task name (required)
   - Category selection
   - Deadline date
   - Duration (optional)
   - Description (optional)
3. Press "Create Task" to add it to your list

### Managing Tasks
- **View Tasks**: See all your tasks organized by date and completion status
- **Complete Tasks**: Swipe left on any task to mark it as complete
- **Delete Tasks**: Swipe right on any task to remove it permanently
- **View Details**: Tap on any task to expand and see additional information

## User Experience Highlights

### Seamless Navigation
- **Side Drawer**: Access different sections of the app through the side menu
- **Bottom Navigation**: Intuitive bottom navigation for core features
- **Progress Tracking**: Visual progress summary card on the homepage

### Visual Design
- **Personalized Color Scheme**: App adapts to your chosen theme color
- **Clear Typography**: Easy-to-read text with appropriate hierarchy
- **Thoughtful Spacing**: Ample whitespace for a clean, uncluttered interface
- **Interactive Elements**: Cards with subtle shadows and rounded corners

### Accessibility
- **High Contrast**: Sufficient color contrast for readability
- **Large Touch Targets**: Easy-to-tap buttons and interactive elements
- **Clear Visual Feedback**: Immediate response to user interactions

## Technical Implementation

### Architecture
- Built with Jetpack Compose for modern, declarative UI
- MVVM pattern with state management using Compose runtime
- SharedPreferences for local data persistence
- AlarmManager for precise notification scheduling

### Key Components
- **Task Management**: Custom Task data class with notification support
- **Navigation**: Compose Navigation with custom animations
- **Notifications**: Integrated notification system with user actions
- **Data Persistence**: Gson for JSON serialization/deserialization

### Permissions
- `SCHEDULE_EXACT_ALARM`: For precise task deadline notifications
- `POST_NOTIFICATIONS`: To display task reminders (Android 13+)

## Screens

1. **Welcome Screen**: First-time user experience
2. **Onboarding**: Profile setup and personalization
3. **Homepage**: Main dashboard with task list and progress summary
4. **Create Task**: Form for adding new tasks
5. **Notification Center**: History of task notifications
6. **Exercise**: Coming soon feature area

##  Target Audience

Listify is designed for:
- **Professionals** managing multiple projects and deadlines
- **Developers** tracking JIRA tickets and sprint tasks
- **Students** organizing assignments and study schedules
- **Anyone** looking for a beautiful, functional task manager

## Future Enhancements

While Listify is already a powerful task manager, future updates may include:
- Cloud synchronization across devices
- Advanced filtering and sorting options
- Recurring tasks support
- Integration with popular productivity tools
- Enhanced statistics and analytics
- Dark mode customization

## Built With

- [Kotlin](https://kotlinlang.org/) - Programming language
- [Jetpack Compose](https://developer.android.com/jetpack/compose) - Modern toolkit for building native UI
- [Material Design 3](https://m3.material.io/) - Design system
- [Gson](https://github.com/google/gson) - JSON serialization/deserialization


*Listify - Making task management beautiful and intuitive*