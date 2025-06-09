CashRoyale: Personal Budget Tracking App 

Keaton Donovan Jones – ST10336767 
 Zaire Tinyiko Mashaba – ST10291916 
Mohammed Hassan Abu Tahir - ST10283090 

GitHub Repository Link: https://github.com/Keaton-Jones/CashRoyale.git 

Overview -
CashRoyale is a user friendly personal budget tracker app, developed using Android 
Studio. The mobile applications aims to help users track their spending habits and 
savings goals, while also providing an engaging and interactive experience for the user. 

Relevance -
Budgeting can often be viewed as a stressful and tedious task but it doesn’t have to be 
this way especially for such an important aspect. CashRoyale aims to rid finanacial 
planning of these negative stigmas by: 

➢ Making budgeting engaging and interactive through gamification 

➢ Providing clear visuals of spending habits 

➢ Helping users build healthier financial habits 

➢ Offering offline functionality so users can track finances anywhere 
 
App Navigation 
1. Login and Registration :
Users will be met with a login screen, where they will enter a username and 
password, and if they do not have one, they will have the option to create an 
account. 
 
2.  Home Page :
Once a user logins in, they will be required to set a maximum and minimum 
monthly goal. After this, they will see the home page, which will include details 
such as remaining budget, amount spent, and monthly budget. The page will 
also have buttons redirecting the user to view their income, expenses and all 
transactions. There will also be a navigation bar, which will direct the user to 
goals page, the calendar page, and their statistics. At the top of the page, user 
will find a button to create and manage categories. 

3.  Category Management :
Users will be able to create, edit, delete, and manage categories while also 
customizing each one with a unique color. There will also be the option to label 
the category type e.g. income and expense. 
 
4.  Add Expense  :
Users will be given the option to log any expenses that they may have. They will 
also have the option to attach an image with each expense that they add. 
 
5. Add Income :
Users will be given the option to log any income that they may have. They will 
need to provide details such as the amount, date, and a short description. 
 
6.  Budget Goals :
Users will be able to set a minimum and maximum monthly spending goal. 
 
7.  Transaction History :
Users will be able to see the history of all transactions, both income and expense that the user has made. The transactions have been collated and displayed in one recyclerView.

Additional Features:

1. We have implemented a send monthly report feature that will generate an email to inform the user of their current months goals and spending habits.
2. The application has implemented gamification, by use of a dynmaic progress bar. The user will be able to keep track of their progress in terms of sticking to their expense goals, via saidprogress bar, along with personalised tips/advice depending on their progress.
 
Required Software :
To successfully run the app, you’ll need to have several key tools installed on your 
development machine. These tools are essential for building, running, and testing 
Android applications. 

➢ Android Studio is the official Integrated Development Environment (IDE) 
provided by Google for Android development. It includes everything needed to 
design layouts, write code, test applications, and run them on either virtual 
emulators or real devices. Android Studio is the central platform you’ll use to 
work on the app, and it handles much of the configuration and compilation 
automatically. You can download it from https://developer.android.com/studio. 

➢ The Android Software Development Kit (SDK) is a collection of tools and 
libraries required to build Android apps. It comes bundled with Android Studio 
and provides APIs that allow your app to interact with the Android operating 
system. It includes components for targeting different versions of Android, so 
you can test your app on multiple Android API levels. 

➢ The Java Development Kit (JDK) is necessary for compiling Java (or Kotlin) code, 
which the app is built with. Although Android development often uses Kotlin, the underlying build process still relies on Java tools. Android Studio typically installs 
a compatible version of the JDK automatically during setup, but it’s important to 
ensure the JDK is correctly configured in your system's environment if any errors 
occur. 

➢ Gradle is a build automation system that Android Studio uses to manage project 
builds. It handles dependency management and compiles the code into an APK 
(Android Package) that can run on devices. Gradle is tightly integrated into 
Android Studio and does not require separate installation, but a good internet 
connection is recommended to allow Gradle to download any required libraries 
the first time you build the app. 

How to Install Software 

1.Install Android Studio -
Go to this website: https://developer.android.com/studio 
Click the “Download Android Studio” button. 
Open the downloaded file and follow the installation instructions. 
During installation, keep everything selected (Android SDK, Emulator, JDK, etc.). 
After it finishes, open Android Studio and go through the setup wizard. 
 
2. Set Up Android SDK -
Open Android Studio. 
At the top, click “More Actions” > “SDK Manager” (or go to Tools > SDK Manager from the 
menu). 
In the SDK Platforms tab: 
Make sure at least one Android version is checked (preferably Android 11 or higher). 
Go to the SDK Tools tab and check these: 
Android SDK Build-Tools 
Android Emulator 
Android SDK Platform-Tools 
Google USB Driver (Windows only) 
Click OK to install them. 
 
3. Create or Open the Project -
If your project is already on your computer: 
Open Android Studio → click “Open” → select the project folder. 
If you downloaded it from a link or flash drive: 
Extract the folder if zipped, then open it through Android Studio. 
 
4. Let Android Studio Set Up Everything -
Android Studio will automatically download and sync everything your project needs. 
If prompted, just click “OK” or “Allow” to let it install dependencies or update Gradle. 
Wait until syncing is complete. 
 
 5. Run the App -
At the top of Android Studio, click the green play button to run the app. 
You can choose: 
A virtual device (emulator) if set up. 
 
What To Do If Software Fails: 

 Android Studio Doesn’t Open -
If Android Studio doesn’t open at all, first restart your computer and try again. Make sure 
your computer meets the system requirements and that nothing else is using too much 
memory. If it still won’t open, you can uninstall and reinstall Android Studio by 
downloading it again from the official website. This refreshes the program and often 
solves the problem. 

The App Doesn’t Load or Run -
If your app doesn’t start inside Android Studio, you may need to check that all the 
necessary settings are in place. Go to the top of Android Studio and click “Build” > 
“Rebuild Project” — this tells the program to get everything ready to run. Then click the 
green “Play” button again. If that doesn’t work, go to “File > Invalidate Caches / Restart” 
and follow the prompts. This helps Android Studio clear any saved errors. 

Emulator Isn’t Working -
If the built-in phone simulator doesn’t start, or it stays stuck on a blank screen, open the 
Device Manager in Android Studio and create a new phone. Choose a basic phone 
model like Pixel and finish the setup. If your computer feels slow, restart it to free up 
memory. You can also test the app on Bluestacks as an alternative. 

The App Doesn’t Respond or Shows Errors -
If the app opens but doesn’t respond (for example, the Add Income button doesn’t 
work), make sure all the input fields are filled correctly (amount, date, etc.). If something 
feels stuck, try closing Android Studio and opening it again. You can also restart your 
computer just in case a background issue is causing the problem. 
 
Demonstration Video -
YouTube video link : https://youtu.be/YkwjQNRFxYs 

References :
Web Colors : RapidTables , https://www.rapidtables.com/web/color/index.html

ViewModel overview: Developers, 11 February 2025, 
https://developer.android.com/topic/libraries/architecture/viewmodel 

Medium: Paul Stanescu, 23 September 2019, https://paulstanescu.medium.com/should-recyclerview-adapter-have-the-responsibility-of-datain-mvvm-architecture-2688d91455f5 

Medium: Ahmet Bostanciklioglu, 26 August 2023 , 
https://medium.com/@ahmetbostanciklioglu/repository-pattern2bcd99cd8a8b#:~:text=The%20Repository%20Pattern%20is%20a,APIs%2C%20and%20other%20external%20systems. 
 
Medium: Dilip Patel, 20 June 2024, https://medium.com/@dilip2882/why-useviewmodel-factory-understanding-parameterized-viewmodels-2dbfcf92a11d 

Recommendations for Android architecture: Developers, 10 February 2025 , 
http://developer.android.com/topic/architecture/recommendations 

Guide to app architecture: Developers , 10 February 2025, 
https://developer.android.com/topic/architecture 

Map: Developers , 10 February 2025 , 
https://developer.android.com/reference/java/util/Map 

Gemini: Google, 02 May 2025 , https://g.co/gemini/share/613a93de0d98

Gemini: Google, 05 June 2025 https://g.co/gemini/share/3dd862c80371
