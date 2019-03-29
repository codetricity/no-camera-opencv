# No-Camera OpenCV
Sample application that shows OpenCV running on both a physical 
device RICOH THETA V and an Android Video Device.  You
can develop a plug-in on the emulator and then have it run
on a physical device camera with only minor modifications.

Originally posted by roohii_3.
Modified by https://github.com/iamagod (kasper on theta360.guide)

## Important Note on App Permissions

You must enable storage permissions in the settings.
If you have using a physical device RICOH THETA, you can
use Vysor to go into Settings and enable storage permissions.

## Usage in emulator

* Use your mouse to press *Shutter* button. It will load an 
image into `/sdcard/DCIM/100RICOH/`
* Use your mouse to press the *Process* button. It will
display an image processed with OpenCV to the emulator screen.

## Using real camera

* Press the shutter button to take a picture
* After 4 seconds, press the Wi-Fi button to process the picture

You can see the processed image in Vysor. This demo does not
save the processed image to disk.

The [no-camera-template](https://github.com/codetricity/no-camera-template)
 shows how to save the processed file
to disk.
