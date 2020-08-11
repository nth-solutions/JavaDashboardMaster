@echo off
title Generate SINC Test

echo Generates a ideal SINC test with solid black and red frames.
echo Make sure this .bat file is in an empty directory by itself before proceeding.
echo.

SET /P PRELIT=Duration in seconds of black frames BEFORE test begins (2 +- error): 
SET /P LIT=Duration in seconds of test (120): 
SET /P POSTLIT=Duration in seconds of black frames AFTER test ends (arbitrary): 

echo file pre-lit.mp4 > manifest.txt
echo file lit.mp4 >> manifest.txt
echo file post-lit.mp4 >> manifest.txt

ffmpeg -f lavfi -i color=c=black@1:duration=%PRELIT%:s=qcif:r=30 pre-lit.mp4
ffmpeg -f lavfi -i color=c=red@1:duration=%LIT%:s=qcif:r=30 lit.mp4
ffmpeg -f lavfi -i color=c=black@1:duration=%POSTLIT%:s=qcif:r=30 post-lit.mp4

cls

ffmpeg -f concat -i manifest.txt -c copy sinc-test.mp4

del pre-lit.mp4
del lit.mp4
del post-lit.mp4
del manifest.txt

cls

echo Done, saved video to "sinc-test.mp4".
echo Press any key to exit.
pause > NUL