# ✅ To-Do App

**To-Do App** is a playful, interactive Android application built using **Kotlin + XML**.  
It makes task management exciting with animations, quotes, and a personalized experience — not just another boring to-do list!  

---

## ✨ Features

### 👋 First-Time Welcome Experience
- **Name Dialog on First Launch** – Ask for the user's name.
- **Personalized Main Screen** – Displays the entered name with **fun animations**.
- **Lottie Welcome Animation** – Smooth animation at the top of the main activity.
- **Profile Image Support** – User-selected image is shown on the main screen.

### 💬 Daily Motivation
- **"Tap to See Quote"** – On tapping, a dialog pops up with a motivational quote.
- **Fresh Quotes Every Time** – Each app launch fetches a new quote *(requires internet)*.

### 📝 Task Management
- **Add Task Button** – Opens the Add Task page where you can:
  - Enter **Task Title**.
  - Enter **Task Description**.
- **View Your Tasks Button** – Opens the Task List page.

### 📋 Task List Features
- **Task List Screen Buttons**:
  - **➕ Add Task (Bottom-Left)** – Opens the Add Task page.
  - **❓ Help Button (Bottom-Right)** – Displays usage instructions in a dialog.
- **View Task** – Tap on a task to open it in **view-only mode**.
- **Edit Task** – Swipe **right** → Confirm in dialog → Edit the task.
- **Delete Task** – Swipe **left** → Confirm in dialog → Delete the task.
- **Mark Task as Complete** – Tap the **green dot** in a task.
- **Completed/Pending Toggle** – Switch between lists:
  - **Pending List** – Tasks not yet completed.
  - **Completed List** – Tasks already finished.
- **Revert to Pending** – In the completed list, tap a task to mark it as incomplete.

---

## 🛠 Technical Highlights
- **Language & UI** – Kotlin for logic, XML for UI.
- **Animations** – [Lottie Animations](https://lottiefiles.com/) for engaging visuals.
- **Persistent Storage** – Save tasks locally (SQLite / Room).
- **Swipe Gestures** – Edit and delete with confirmation dialogs.
- **Network Requests** – Fetch motivational quotes via API.
- **Custom Image Handling** – Store and display profile images.

---

## 🎯 Learning Outcomes
This project demonstrates:
- Implementing **first-time user onboarding** with dialogs.
- Integrating **Lottie animations** into Android apps.
- Using **RecyclerView** with swipe actions for task management.
- Fetching **API data** (quotes) over the internet.
- Building **multi-screen navigation** in Android.
- Designing **intuitive UI/UX** with Material Design components.


   ```bash
   git clone https://github.com/<your-username>/To-Do-App.git
