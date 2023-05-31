# Step Counter Android App

This Android application is designed to track the number of steps taken by the user and determine the direction of their walk. Additionally, it differentiates between taking the stairs and using a lift while moving up or down floors inside a building. The app utilizes pedestrian dead reckoning (PDR) to compute displacement from accelerometer readings.

## Features

- Step counting: The app uses accelerometer patterns to calculate the number of steps taken by the user.
- Direction detection: By utilizing a magnetometer, the app determines the direction of the user's movement.
- Stair vs Lift detection: The accelerometer pattern is used to distinguish between taking the stairs and using a lift to move up or down floors.
- Stride length calculation: The app employs heuristics based on the user's height and weight to estimate their stride length, a crucial factor in step counting.

## Prerequisites

Before running the Step Counter Android App, ensure that you have the following:

- Android Studio: The latest version of Android Studio installed on your machine.
- Android Device: A physical Android device with accelerometer and magnetometer sensors or an Android emulator with sensor support.

## Getting Started

Follow these steps to get started with the Step Counter Android App:

1. Clone the repository:

   ```shell
   git clone https://github.com/Mao-17/Step_Counter_Android_App.git
   ```

2. Open the project in Android Studio.

3. Connect your Android device to your computer or launch an Android emulator.

4. Build and run the app using Android Studio.

## Usage

Once the app is installed and running on your Android device, follow these instructions to use the Step Counter app effectively:

1. Hold the device in your hand, ensuring that the y-axis is oriented towards the north or the front.

2. Avoid shaking the device to maintain accurate step counts and direction detection.

3. Walk naturally, and the app will start counting your steps and displaying the direction of your movement.

4. When using stairs, the app will identify the pattern of your movement and indicate that you are taking the stairs.

5. Similarly, when using a lift, the app will detect the specific accelerometer pattern associated with using a lift.
