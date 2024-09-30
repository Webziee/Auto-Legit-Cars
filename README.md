# 1. INTRODUCTION : READ ME
This comprehensive read me file will provide you with an overview of our project. It includes key information about the app and report structure such as 2) a table of contents, 3) who is in the team, 4) each team members group rating, 
5) important link, 6) the 3 features selected and implememted from the 5 in the planning phase, 7) a checklist of all features/functionalities included in the app, 8) an explination of how we created and implemented the API, 
9) details about the app in the form of a report with the following headings: purpose of
the app, design considerations, the manner in which GitHub and GitHub actions were utilized throughout the development processs, and lastly a conclusion that reflects on the project and its outcomes;
and 10) a reference list consisting of all references for research and code attribution.

## 2. Table of Contents
1. [Introduction](#introduction)
2. [Table of Contents](#table-of-contents)
3. [Team Members](#team-members)
4. [Group Ratings](#group-ratings)
5. [Important Links](#important-links)
6. [Selected Features Implemented](#selected-features-implemented)
7. [Checklist of Features/Functionalities](#checklist-of-featuresfunctionalities)
8. [API Implementation](#api-implementation)
9. [Report on 'Legit Auto Cars'](#9-report-on-legit-auto-cars)
   - [9.1 Purpose of the 'Legit Auto Cars' Application](#91-purpose-of-the-legit-auto-cars-application)
      - [9.1.1 Key Things To Note](#911-key-things-to-note)
   - [9.2 Design Considerations](#92-design-considerations)
      - [9.2.1 Focus On User Experience (UX)](#921-focus-on-user-experience-ux)
      - [9.2.2 Performance and Speed](#922-performance-and-speed)
      - [9.2.3 Scalability](#923-scalability)
      - [9.2.4 Security](#924-security)
      - [9.2.5 Visual Design](#925-visual-design)
   - [9.3 Utilization of GitHub](#93-utilization-of-github)
      - [9.3.1 Collaborative Development Process](#931-collaborative-development-process)
      - [9.3.2 Version Control and Commits](#932-version-control-and-commits)
      - [9.3.3 Repository Submission](#933-repository-submission)
      - [9.3.4 Documentation](#934-documentation)
   - [9.4 Utilization of GitHub Actions](#94-utilization-of-github-actions)
      - [9.4.1 Initial Setup](#941-initial-setup)
      - [9.4.2 Issues Encountered After Testing The Build](#942-issues-encountered-after-testing-the-build)
      - [9.4.3 Screenshots of GitHub Actions Results](#943-screenshots-of-github-actions-results)
   - [9.5 Conclusion](#95-conclusion)
10. [Reference List](#reference-list)

## 3. TEAM MEMBERS
- Ashwin Pillay (ST10088734)
- Tristan Singh (ST10022401)
- Jaiden Naidoo (10218221)

## 4. Team Member Group Rating

## 5. Important Links 
"Below are key links that provide access to essential resources, including our project repository, documentation, and any external tools or services used throughout the development of the 'Legit Auto Cars' application."
### 5.1 Repo Link
- https://github.com/Webziee/Auto-Legit-Cars.git
### 5.2 Youtube Link To The Legit Auto Cars Video Demonstration 
- https://www.youtube.com/watch?v=wskusqSF8sU

## 6. FEATURES SELECTED AND IMPLEMENTED
Below is a list of the 5 developer defined innovative features to be included in the 'Legit Auto Cars' application with a tick or 
cross next to each feature, indicating whether it is implemented in the application or not:
- Advanced Searching ✓ 
- Price Comparism Tool ✗ 
- Favourites Feature ✗ 
- Whatsapp Messaging Integration ✓ 
- Book a Test Drive ✓

## 7. CHECKLIST OF FEATURES AND FUNCTIONS INCLUDED
Below is a checklist of all the features/functionalities and expectations that are to be included and met as per the POE rubric, 
with a tick or cross indicating if that feature/functionality as been indluded or met:
- App Runs on Mobile Device ✓
- Feature: SSO Sign-in ✓
- Feature: Settings Menu ✓
- Creation of API ✓
- Implementation of REST API ✓
- Feature: User Defined 1 (Advanced Searching) ✓
- Feature: User Defined 2 (Whatsapp Messaging Integration) ✓
- Feature: User Defined 3 (Book a Test Drive) ✓
- User Interface ✓
- Read Me ✓
- Automated Testing ✓
- Demonstration Video ✓

## 8. CREATION AND IMPLEMENTATION OF API/REST API
### 8.1 Supabase Website and Dashboard
First, we headed over to *supabase.com* to set up our project. Once you sign in, create a new project, which will generate your API URL and keys. This API URL and key will be used to connect your app to Supabase.
### 8.2 Table Creation
In Supabase, we created two main tables: cars and bookings. Let’s take a quick look at the cars table structure. It includes fields like id, title, price, make, model, and more. These store key information about the cars in our listing.
The bookings table is used to store user bookings for test drives. It contains a car_id field, which references the id from the cars table, ensuring relational data integrity.
### 8.3 Connecting Supabase to the Legit Auto Cars Application
In the app, we use *Retrofit* to connect with Supabase. In our ApiServices Interface we define our API calls, such as fetching filtered cars and creating new bookings. Notice how we use @POST for adding a booking and @GET for retrieving filtered cars.
### 8.4 Fetching Cars
In the Buy.kt fragment where we display the car listings, we made use of Retrofit to fetch data from Supabase.
Once the cars are fetched, they are displayed in a RecyclerView, allowing users to browse through the available options.
### 8.5 Filtering Cars
We also implemented a powerful search functionality where users can filter cars by make, model, year, price, and more. The filtering works by dynamically building query parameters and sending them to Supabase.
By using filters like minimum price, maximum year, and transmission type, we retrieve only the relevant cars from our database.
### 8.6 Booking a Test Drive
Once a user selects a car, they can book a test drive directly from the app. This function submits the booking to Supabase by sending a POST request to the bookings table. Notice how we pass the car_id, date_time, and user_email to complete the booking.
### 8.7 Closing
And that’s it! With Supabase and a few lines of code, we’ve successfully built a full-stack car listing app with real-time data management and booking features. Supabase provides an excellent, easy-to-use backend solution, especially when you want to move fast with a scalable database and authentication features.

## 9. REPORT ON 'LEGIT AUTO CARS' 
The development team has started to develop and build the "Legit Auto Cars" application, which is designed to faciliate and ease the process of listing
and viewing vehicles that are for sale. This idea came about after extensive research on other apps like Auto-Trader, Cars.co.za, and WeBuyCars, but this 
app is leaning more towards and adopts more ideas from WeBuyCars and AutoTrader. With the use of 'Legit Auto Cars', users will be able to upload automobiles they want 
to sell and view the different vehicles for sale online all on one app. This Android applications goal is to provide a smooth and effective user experience by incorporating 
some fundamental functionality from the three popular car apps as well as new and creative/innovative features. These features are intended to improve the user 
engagement and the user's overall experience while viewing, uploading vehicles and interacting with the application.

### 9.1 Purpose of the 'Legit Auto Cars' Application
The 'Legit Auto Cars' application's main goal is to ease the processes of: viewing cars for sale, uploading cars to sell, making bookings, 
and communication between and among the company and its clients which is all done online and from the app. The application does not allow clients 
or the company to purchase or sell cars directly (No Money Involved), but rather provides a medium for the 2 primary users to engage, interact and 
communicate with each other. The app includes features that makes the clients and the company's lives much easier such as viewing, uploading,
WhatsApp messaging integration, booking a test drive, advanced searching, and to be implemented: a favourites feature and a price comparison tool. 
The app helps connect the company and its clients by providing a platform where users can quickly view cars for sale and submit their own cars to sell, 
while also ensuring a seamless digital experience for users by doing away with the necessity for physical paperwork and in-person company visits.
#### 9.1.1 Key Things To Note
- Interactions are between the comapny and client only, one to one relationship. Meaning there are no client to client interactions.
- In the buy page, customers will be able to see the cars for sale by the company and other clients. One thing to note is that the company acts as
the middle man between clinets, so if a client in intrested in a car uploaded by another client, they will have to deal with the company and cannot
speak to the client directly, Additionally the company implments a 10 percent markup on cars uploaded by clients for services provided. 
- When a client uploads a car to sell, the company will be able to see that vehicle as well as other clients. the company can then communicate
with the client directly about their vehicle if they are interested and if other clients are interested, they will have to communicate with the company
and not the client selling the vehicle. 
- Customers can book a test drive on any vehicle. The company handles all booking dates, times and place of meetup. 
- Our app inherits and implements more ideas from Autotrader and WeBuyCars rather than Cars.co.za. 

### 9.2 Design Considerations
The main concerns and difficulties the team took into account when creating 'Legit Auto Cars' are reflected in the following Design Considerations.
They guarantee the app's continued scalability, security, usability, and adaptability for new developments:

#### 9.2.1 Focus On User Experience (UX)
##### Simplicity and ease of use:
- The software is intended for a wide range of users, including those who might not be tech-smart. Thus, simplicity is of the utmost importance.
The screens are easy to use, and the navigation is maintained simple with obvious buttons and icons and key operations like making a test-drive
booking, uploading a vehicle, and changing settings require very few steps and are easy to manipulate.
##### Consistency 
- Every screen in the design has the same navigation structure and layout, thus increasing usability as colours, fonts, and icons are used often
since it makes it easier for users to anticipate interactions.

#### 9.2.2 Performance and Speed
##### Loading Times Optimized 
- The app's main focus, automotive images, are optimised for quick loading without compromising quality. This Performance can be enhanced by using lazy
loading to make sure that only the relevant photos load at first and appropriate indexing and querying algorithms in the Firestore database maintain the
efficiency of data retrieval for our advanced searching.
##### Efficiently of Mobile Data 
- The app was designed to consume as little data as possible, since it is likely to be used on mobile networks, this process involves reducing the number of
API requests made as well as compressing high quality images since they are not data freindly.

#### 9.2.3 Scalability
##### Architecture Style: Modular
- The app is built with a modular architecture, which allows for future expansion without having to rewrite significant parts of the code. New features,
such as price comparison tools or a favorites list, can be added easily, additionally the app’s infrastructure should be designed to handle an increasing
number of users and vehicle listings, which ensures long-term sustainability.

#### 9.2.4 Security
##### Protection of User Data
- Data security for vehicles and individuals is a crucial design factor. Data encryption, secure storage of sensitive data (such car listings), and secure
authentication (like email-based logins with SSO) were taken into consideration, hence we have decided to use firebase as it is a secure database to hold
all of our applications data.
##### Whatsapp Integration 
- It is crucial to guarantee the security of users' private talks when integrating WhatsApp for communication. In order to prevent vulnerabilities, links
and APIs were used to start conversations are secured as well as encryption of data.

#### 9.2.5 Visual Design
##### Identity
- The apps name (which is essentially the name of the sole company, in this case we used a fictitious name: 'Legit Auto Cars') and branding are
reflected in the app in order to keep a polished and recognisable look, the colour scheme, typography, and iconography adhere to the company's brand guidelines.

### 9.3 Utilization of GitHub
For the development of the 'Legit Auto Cars' application, GitHud was used and deamed an integral tools for collaboriation an version control,
here is how the development team used GitHub:
#### 9.3.1 Collaborative Development Process 
- Every member of the team copied the main repository into an instance of Android Studio. In order to make sure that everyone was working on the
most recent version of the project, we routinely pushed our modifications and pulled updates from the same source. We were able to work together
smoothly and effectively because to this procedure, which integrated each member's contributions.
#### 9.3.2 Version Control and Commits 
- Throughout the course of the project development, all changes were tracked using Git. All features, bug fixes, and modifications were committed
with commit messages, guaranteeing that the project's development was thoroughly recorded. Additionally, this gave us access to a thorough commit
history, which made it simple to identify changes, troubleshoot them, and roll back any modifications that were needed.
#### 9.3.3 Repository Submission 
- The entire project is stored in the repository and will be sent to our lecturer for review and marking. With the connection to our GitHub
repository, the lecturer will have complete access to our project and be able to examine our documentation, code, commits and organisation.
#### 9.3.4 Documentation
- We worked together to create an extensive README.md file using GitHub as part of documentation. The README makes it simpler for the lecturer
and any other users to understand the project by providing the names of the developers involved, and a comprehensive explanation regarding the
app's goal, design considerations, and use of GitHub and GitHub Actions.

### 9.4 Utilization of GitHub Actions
We used GitHub Actions in our project to automate the Android application's creation and testing. This guarantees that our code works reliably
not just on our local computers but also in various contexts and enviroments.
#### 9.4.1 Initial Setup 
- Workflow File Creation: In the.github/workflows folder, we produced a workflow file called android.yml. The procedures needed to build and test
our application are outlined in this file.
- JDK Configuration: At first, we encountered a problem with the build process utilising an out-of-date Java version (Java 11). The Android Gradle
plugin requires Java 17, thus we adjusted the workflow to use that instead.
#### 9.4.2 Issues Encountered After Testing The Build
- Dependency Problems: We had trouble downloading dependencies during the initial builds. To fix these problems, we We carefully reviewed and updated
our Gradle configuration to include all necessary dependencies and repositories.
- Lint issues: For Android 13 and above especially, lint issues pertaining to notification permissions caused the development process to fail. We discovered
that the AndroidManifest.xml file required the application to request the POST_NOTIFICATIONS permission. For the purpose of fixing the issue that the lint
checks produced, this was essential. This issue was resolved by adding POST_NOTIFICATIONS permission to the AndroidManifest.xml to comply with Android 13 requirements.
- Errors with the Gradle Daemon: We also came across issues when the Kotlin compile daemon abruptly stopped working. Making sure all configurations in the
build.gradle.kts files were aligned and set up appropriately solved this issue.
#### 9.4.3 Screenshots of GitHub Actions Results
![GitHub Actions Issue 01](https://github.com/user-attachments/assets/99da1152-af86-41ea-b6aa-d971e6669a8e)

![GitHub Actions Issue 02](https://github.com/user-attachments/assets/f2609d2a-a5c8-4d6f-b0d2-3e929b5c4c6f)

![GitHub Actions Issue 03](https://github.com/user-attachments/assets/6ad9da50-6c97-472d-baf5-ec040394a151)

![GitHub Actions Suceeded](https://github.com/user-attachments/assets/71fe2d2b-d64b-4e78-a77c-22fbe7d12a68)


### 9.5 Conclusion
Thus far, the "Legit Auto Cars" application has undergone a thorough development process that has placed a strong emphasis on user interaction, efficient procedures, and
a solid architecture. Our design decisions have been well considered in order to prioritise security and scalability while guaranteeing a flawless user experience.
Our team's ability to work together more effectively, manage changes more efficiently, and automate testing and building procedures has all improved workflow dependability 
since we started using GitHub and GitHub Actions. The difficulties we faced—such as handling dependencies and adjusting to Android's permission updates—have improved project
results and honed our problem-solving abilities.
In order to maintain the application's value for users, we will continue to develop its features and improve it with the aid of the insights gathered before the publication on 
the google play store. In addition to fostering teamwork, this experience has laid a solid platform for further improvement and adjustment to the application.

## 10. REFERENCE LIST
Anon., 2020. GeeksforGeeks. [Online] 
Available at: https://www.geeksforgeeks.org/how-to-send-message-on-whatsapp-in-android-using-kotlin/
[Accessed 18 September 2024].

Domenic, 2022. Youtube - The Android Factory. [Online] 
Available at: https://www.youtube.com/watch?v=-u4w_-x_3_I
[Accessed 17 September 2024].

Domenic, 2022. Youtube - The Androif Studio. [Online] 
Available at: https://www.youtube.com/watch?v=339-fxTcrmI
[Accessed 17 Septermber 2024].

Evan, 2020. Youtube. [Online] 
Available at: https://www.youtube.com/watch?v=vj9aY9b5hIg
[Accessed 18 September 2024].

Grier, M., 2020. Stack Overflow. [Online] 
Available at: https://stackoverflow.com/questions/65504266/kotlin-not-java-how-to-use-maketoast-within-a-fragment-or-make-a-click-work-in
[Accessed 13 September 2024].

minikate, 2019. Stack Overflow. [Online] 
Available at: https://stackoverflow.com/questions/54784101/firebase-android-get-current-user-email
[Accessed 13 September 2024].

Mourya, A. K., 2023. Medium. [Online] 
Available at: https://medium.com/@mr.appbuilder/navigating-android-fragments-with-the-navigation-component-part-1-1d238e000313
[Accessed 15 September 2024].

Obregon, A., 2023. Medium. [Online] 
Available at: https://medium.com/@AlexanderObregon/using-kotlin-with-firebase-a-guide-for-android-app-developers-28f3fd2b57bc
[Accessed 13 September 2024].

Pachigolla, R. k., 2013. Stack Overflow. [Online] 
Available at: https://stackoverflow.com/questions/14619234/how-to-make-toast-message-when-button-is-clicked
[Accessed 12 September 2024].

Poulopoulos, D., 2015. Stack Overflow. [Online] 
Available at: https://stackoverflow.com/questions/30846973/getting-arguments-from-a-bundle
[Accessed 17 September 2024].

Raghunandan, 2013. Stack Overflow. [Online] 
Available at: https://stackoverflow.com/questions/17599450/how-to-inflate-view-inside-fragment
[Accessed 13 September 2024].

ShoutCafe, 2023. [Online] 
Available at: https://www.youtube.com/watch?v=jWiiL8G4jbo
[Accessed 15 September 2024].

FirebaseAuth. (2023) Firebase Authentication setup [Online]. Available at: https://firebase.google.com/docs/auth 
[Accessed: 22 September 2024].

Raghunandan, P. (2013) Android fragment tutorials. [Online]. Available at: https://stackoverflow.com 
[Accessed: 23 September 2024].

Grier, M. (2020) Android Toast documentation. [Online]. Available at: https://developer.android.com/guide/topics/ui/notifiers/toasts 
[Accessed: 22 September 2024].

Supabase (n.d.) Supabase Documentation. [Online]. Available at: https://supabase.com/docs 
[Accessed: 22 September 2024].

Retrofit (n.d.) Retrofit Documentation. [Online]. Available at: https://square.github.io/retrofit/ 
[Accessed: 22 September 2024].

OkHttp (n.d.) OkHttp Documentation. [Online]. Available at: https://square.github.io/okhttp/ 
[Accessed: 22 September 2024].

Android Developers (n.d.) Fragment Transactions. [Online]. Available at: https://developer.android.com/guide/fragments/fragmentmanager 
[Accessed: 22 September 2024].

Google Developers (n.d.) Google Sign-In for Android Documentation. [Online]. Available at: https://developers.google.com/identity/sign-in/android/start 
[Accessed: 22 September 2024].

Android Developers (n.d.) Picasso Documentation. [Online]. Available at: https://square.github.io/picasso/ 
[Accessed: 22 September 2024].

StackOverflow (n.d.) Handling multiple images and spinners. [Online]. Available at: https://stackoverflow.com 
[Accessed: 22 September 2024].






















