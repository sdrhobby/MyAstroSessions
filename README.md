# MyAstroSessions
## What is it about?
I started this program to keep track of my personal astrophoto sessions.
Each night under the sky produces lots of data, mainly - all the different types of captured images.
Sometimes I manage to document everything during a capturing session, either on paper on in a textfile - sometimes not.

What is there in any case are the images on the hard drive - and they hold lots of information. 
I could of course use some tools to browse exif and fits headers and collect what I need - everytime. 
OR I could write a tool - once - that does it for me - everytime.   

## What is it that I'm interested in? 
- Which equipment (optical, cameras, mount ...) did I use for a session?
- How many subs of which type did I capture during a special session?
- What have been the capture settings (exposure, iso, gain, bias)?
- How much total exposure, how much per capture setting ...?
- Perhaps some previews of the resulting images ...
- direct access to my session-related notes (if I managed to do some, or just to document things later on)
- a timeline (when did I capture what? What did I capture last october?)
- some kind of searching/filtering features 
- ... and much more

## How does it look like?


## How does it work?
### The file structure
My workflow - and therefore the organization of the files on the disk - is inspired by Siril https://siril.org/ (one of the coolest Open Source projects on earth).

My capture sessions may produce structures like this one:
- Leo-Triplet
    - 2023-03-08-Capture session 1
      - lights
          - lots of images
      - darks
          - lots of images
      - flats
          - lots of images
      - biases
          - lots of images
      - some object.fits/tiffs/jpegs as result images

**NOTE**: The date prefix in the session name is MANDATORY. Otherwise this folder will not be recognized as a capture session.

It does not matter how deep in the folder structure such a session folder is "hidden". 
You may have your own structuring of all the top folders, e.g. by galaxies, nebulae etc. 
As soon as there is a folder starting with the mentioned date prefix, it is treated as a capture session.
The name of the direct parent folder is interpreted as the name of the Astro-Object that was the target of the session. 

If you use filters the lights part may also look like this 
(any folder name that does not start with a "." or "_" directly below the lights folder is treated as a Filter):
    
    - 2023-03-09-Capture session 2
      - lights
          - L
              - lots of images
          - R
              - lots of images
          - G
              - lots of images
          - B
              - lots of images
          - H alpha
              - lots of images
          - name-of-another-expensive-filter
              - lots of images
      - [calibration frame part looks as above]

Until now the tool works on such type of structure, because - it works for me ;-), but it may be adapted to other common structures, if there is interest.

### What the tool does?
When you select the top folder of your capturings and press the "Scan" it will read the complete folder structure below.

I will detect capture sessions based on the date-prefix of format yyyy-mm-dd in their folder name.
The name of the sessions parent folder is treated as the name of the Astro-Object that has been captured.
