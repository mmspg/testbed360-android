# Testbed 360
A Testbed for Subjective Evaluation of Omnidirectional Visual Content - Android version.

This software was developed during a semester project in MMSPG, EPFL by [LM. Garret](https://github.com/lmgarret).
Please see the original (iOS) version of testbed360 here: https://github.com/mmspg/testbed360.

If you use this software in your research please cite MMSPG's paper:
```
@inproceedings{upenik_testbed_2016,
  author = {Upenik, Evgeniy and Rerabek, Martin and Ebrahimi, Touradj},
	title = {A Testbed for Subjective Evaluation of Omnidirectional Visual Content},
	booktitle = {32nd Picture Coding Symposium},
  address = {Nuremberg, Germany},
	year = {2016},
	month = {Dec}
}
```

The main purpose of this testbed is 
to provide researchers with a tool to perform
subjective assessment of omnidirectional images.
The testbed consists of a software application for the handheld
and mobile platforms and can be used with head-mounted
displays. The application is able to visualize omnidirectional
images and video represented in different projections. The
set of supported projections or geometrical representations
can be easily extended.

# How to add pictures

In order to add pictures to be viewed and graded, a specific architecture must be followed.
We will here talk about `training` and `evaluation` pictures. Pictures of the first set are displayed to the user in order to teach him/her what a 'good' or 'bad' picture is. Thus they have a preset grade, whereas `evaluation` pictures are to be graded by the user, and therefore do not have one set. Both follow a similar naming convention.

### Naming conventions

For each picture, a naming convention must be followed so that the app is able to to parse information easily about the picture being viewed. Here is how pictures should be named :
```
<author>_<title>_<projectionType>_<resolution>_<codec?>_<quality>_<grade?>.<imageExtension>
```
 - `<title>` : title of the picture.
 - `<author>` : author who took the picture.
 - `<projectionType>` : either `cubemap32` or `equirec`, depending on the projection type.
 - `<resolution>` : resolution of the picture.
 - `<codec?>` : used codec's name if any, otherwise empty (but only one `_`).
 - `<quality>` : quality used for the codec, of the format `qX` where X is a decimal from `00` to `99`.
 - `<grade?>` : only for `training` images, the preset grade as `gradeXX` with `XX` a double decimal from `01` (very bad) to `05` (excellent).
 - `<imageExtension>` : the image's file extension.

 Here are some examples of pictures' names :
```
jvet_Train_cubemap32_2250x1500_q36_grade03.png
jvet_SkateboardTrick_equirec_3000x1500_jpeg2000_q48.png
```

### Folder Structure

Now, pictures have to be put into the correct folder structure so that the app can detect them. Here is how it should be done for a given session of pictures :
```
<sessionId>
└── stimuli
    ├── evaluation
    │   └── <evaluation pictures here>
    └── training
        └── <training pictures here>
```
With `<sessionId>` an integer of your choice used to uniquely identify a session.

### Upload to Android

Files must now be uploaded to an Android device. It's recommended to first run the app so that it creates the necessary folder structure. Once done, files have to be uploaded to
```
Android/data/ch.epfl.mmspg.testbed360/files
```
There are various ways to do so :
 - via USB, by connecting the device on a computer and setting the connection in MTP mode. Beware that there exists [an issue on Android](https://issuetracker.google.com/issues/37071807) that may prevent folders from appearing in file explorer. One of the fix can be to reboot the Android device, although after some time folders should appear correctly.
 - via FTP, which is possible using various apps from the [Play Store](https://play.google.com/store/apps).
 - via a (mini/standard with dongle) USB key if the device supports OTG cables.

This folder
```
Android/data/ch.epfl.mmspg.testbed360/files
```
should contain all session folders, named using an integer as said previously. It is now possible to pick and start a session from the app !

## Fetching track records

Once a session is clicked and the training part is finished, the app will start to track the user's movement and actions. After he/she has finished evaluating all pictures, you can fetch the track records stored in Android's filesystem, using any of the ways mentioned previously.

Track records are stored in a new folder generated by the app inside the session's folder, called `tracking`. e.g. for a session with id `1`, track records are located in
```
Android/data/ch.epfl.mmspg.testbed360/files/1/tracking
```
There will be multiple files in it, each following one of this two patterns :
 - `XXXXXXXXXXXXXg` with each X an integer : .csv file holding all grades given to the `evaluation` pictures. The decimal part of the name is a timestamp of when the `evaluation` session was started.
 - `XXXXXXXXXXXXXt` with each X an integer : .csv file containing the camera's rotation angles, sampled every 100ms. The decimal part of the name is a timestamp of when the `evaluation` picture was loaded. The same timestamp is used to assign a grade to the picture in the `XXXXXXXXXXXXXg` file.

For each dry run there should be only one `XXXXXXXXXXXXXg` file associated and as much `XXXXXXXXXXXXXt` files as there are `evaluation` pictures to be displayed.

Data analysis can now start !
