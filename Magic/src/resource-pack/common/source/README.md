# Auto-Skin Uploader

This folder contains scripts that can be used to auto-upload skins to your Minecraft
profile, so you can add your own custom icons.

You can either clone this repo, or copy the two PHP files and the the spyc.php file
that can be found in the web folder (easiest just to clone the repo for now!). 

You will need to create the folders "skin_images" and "source_images".

You will also need PHP, and a bit of experience with it, as well as some basic
browser debugging knowledge (Chrome is easiest to work with here)

## Making the Skins

If you already have your icons in player skin format, skip this step.

Put 8x8 icons in the source_images folder. The run "php makeicons.php". This will
convert all of the images to skin format.

The icon is put on all sides of the player head, and in the middle of the chest/back.
Just in case you can ever use these for skins (though probably not, since we're only
capturing the skin URL and not the full game profile).

## Uploading the Skins

Once you have a skin_images folder full of skins, you'll need to do a few things.

- Create an empty image_map.yml file, if you don't already have one. This is used to track uploaded images.
- Copy the file "mc_creds.php.sample" and rename it "mc_creds.php"
- Open up mc_creds.php and follow the instructions in there to fill out your auth info.

Make sure to never share, commit, or upload the mc_creds.php file! It contains sensitive
information that could be used to hack your MC account.

Once you have that all set up, run "php uploadicons.php" to start the icon upload process.

## Did it Work?

It takes about 10 minutes per icon (due to Mojang rate limit restrictions).

If you see "Updating YAML", it is working! If not, turn on DEBUG in mc_creds.php.