# BioForce Technical Documentation

This document contains explanations of various technical details regarding the process of reading data from a module, organizing and manipulating it, as well as various standards used throughout the project.

## SerialComm

`SerialComm` handles all communications between the computer and the module over a USB serial port.

### Test Data

A data sample is stored in the module as a 16-bit unsigned integer, meaning a value between 0 and 65535.
Each data sample is composed of two bytes (2 * 8 bits in a byte = 16 bits).

> Example: `ax1` `ax2` are the two bytes composing a single acceleration data sample for the X axis.

To convert from byte format to integer format, multiply the first byte by 256 and then add the second byte.

> Example: `ax` = `ax1` * 256 + `ax2`.

The bytes are stored chronologically in the `finalData` array in the following order, with all samples in a given row being from the same point in time:

| Data Sample # | Acceleration X | Acceleration Y | Acceleration Z | Gyroscope X | Gyroscope Y | Gyroscope Z | Magnetometer X | Magnetometer Y | Magnetometer Z |
|---------------|----------------|----------------|----------------|-------------|-------------|-------------|----------------|----------------|----------------|
| 1             | ax1 ax2        | ay1 ay2        | az1 az2        | gx1 gx2     | gy1 gy2     | gz1 gz2     | mx1 mx2        | my1 my2        | mz1 mz2        |
| 2             | ax1 ax2        | ay1 ay2        | az1 az2        | gx1 gx2     | gy1 gy2     | gz1 gz2     |                |                |                |
| 3             | ax1 ax2        | ay1 ay2        | az1 az2        | gx1 gx2     | gy1 gy2     | gz1 gz2     |                |                |                |
| 4             | ax1 ax2        | ay1 ay2        | az1 az2        | gx1 gx2     | gy1 gy2     | gz1 gz2     |                |                |                |
| 5             | ax1 ax2        | ay1 ay2        | az1 az2        | gx1 gx2     | gy1 gy2     | gz1 gz2     |                |                |                |
| 6             | ax1 ax2        | ay1 ay2        | az1 az2        | gx1 gx2     | gy1 gy2     | gz1 gz2     |                |                |                |
| 7             | ax1 ax2        | ay1 ay2        | az1 az2        | gx1 gx2     | gy1 gy2     | gz1 gz2     |                |                |                |
| 8             | ax1 ax2        | ay1 ay2        | az1 az2        | gx1 gx2     | gy1 gy2     | gz1 gz2     |                |                |                |
| 9             | ax1 ax2        | ay1 ay2        | az1 az2        | gx1 gx2     | gy1 gy2     | gz1 gz2     |                |                |                |
| 10            | ax1 ax2        | ay1 ay2        | az1 az2        | gx1 gx2     | gy1 gy2     | gz1 gz2     |                |                |                |
| 11            | ax1 ax2        | ay1 ay2        | az1 az2        | gx1 gx2     | gy1 gy2     | gz1 gz2     | mx1 mx2        | my1 my2        | mz1 mz2        |
| ...           | ...            | ...            | ...            | ...         | ...         | ...         |                |                |                |

Because the magnetometer has 1/10th the sample rate of the accelerometer and gyroscope, it only has data for every 10th point on the time axis.

### Test Parameters

The test parameters associated with a test's data is stored in a .CSVP file as a series of integers separated by newlines. This .CSVP file is a custom file extension and technically is not a "comma separated" file, but this design decision is an artifact carried on from the original `DataOrganizer` codebase.

The format of `List<Integer> testParameters` is shown below:

0. Number of Tests
1. timer0
2. Delay After Start
3. Battery Timeout Flag
4. Time Test Flag
5. Trigger on Release Flag
6. Test Length
7. Accel/Gyro Sample Rate
8. Mag Sample Rate
9. Accel Sensitivity
10. Gyro Sensitivity
11. Accel Filter
12. Gyro Filter
13. Accel X Offset Min
14. Accel X Offset Max
15. Accel Y Offset Min
16. Accel Y Offset Max
17. Accel Z Offset Min
18. Accel Z Offset Max

### Acceleration Offsets

Indices 13-18 of `testParameters` are collectively known as the Inertial Measurement Unit (IMU) offsets. These values measure 

## Data Conversion

This section describes various formats used in `DataOrganizer`, `GenericTest`, and `AxisDataSeries` for converting raw data samples to physical quantities.

### Data Samples

Once data is read from the module via `SerialComm` converting byte format into 16-bit unsigned integer format, the raw data samples are stored in a `List<List<Double>>` named `dataSamples`. Each inner list represents a single sensor axis (accelerometer, gyroscope, and magnetometer along with either X, Y, or Z). The order of these lists is shown below:

0. Time
1. Acceleration X
2. Acceleration Y
3. Acceleration Z
4. Gyroscope (Angular Velocity) X
5. Gyroscope (Angular Velocity) Y
6. Gyroscope (Angular Velocity) Z
7. Magnetometer X
8. Magnetometer Y
9. Magnetometer Z

### Axis Data Series

The Data Analysis Graph further processes `dataSamples` into individual `AxisDataSeries` objects. Each `AxisDataSeries` represents a single axis's data (eg. Acceleration X or Angular Velocity Y). The key difference from `dataSamples` is that `AxisDataSeries` supports "virtual" axes -- generating integrated/differentiated data sets, such as velocity or angular acceleration, from native sensor data sets. In addition, `AxisDataSeries` encapsulates all methods related to data conversion, calculation, and manipulations in a single class. This improves readability and changes without affecting other portions of the codebase.

`GenericTest`, which represents all test data associated with a single module, contains the list of axes in its field `AxisDataSeries[] axes`, the format of which is described below:

0. Acceleration X
1. Acceleration Y
2. Acceleration Z
3. Acceleration Magnitude
4. Velocity X
5. Velocity Y
6. Velocity Z
7. Velocity Magnitude
8. Displacement X
9. Displacement Y
10. Displacement Z
11. Displacement Magnitude
12. Angular Acceleration X
13. Angular Acceleration Y
14. Angular Acceleration Z
15. Angular Acceleration Magnitude
16. Angular Velocity X
17. Angular Velocity Y
18. Angular Velocity Z
19. Angular Velocity Magnitude
20. Angular Displacement X
21. Angular Displacement Y
22. Angular Displacement Z
23. Angular Displacement Magnitude
24. Magnetometer X
25. Magnetometer Y
26. Magnetometer Z
27. Magnetometer Magnitude
