# Examples for R 

[R][1] is a system for statistical computation that can be used to plot data or analyze the gathered data for
statistical anomalies.
See also [Quick-R][2] for more information and help about R, as well as [Gardener's own][3]

Some need additional libraries that can be installed from within R

* RCurl : gathering of data via http. Unlike the simple `json_file <- "http:..."` RCurl also supports authentication which is needed for RHQ
* rjson : parse JSON into Lists and Vectors

`plot_raw.r` can also plot the moving average if library TTR is installed.

## FIles

* plot_metrics.r : plot the standard aggregate metric graphs like in the RHQ ui
* plot_raw.r: plot the last 24h of raw metrics along with some indicators.


[1]: http://www.r-project.org/
[2]: http://www.statmethods.net/index.html
[3]: http://www.gardenersown.co.uk/Education/Lectures/R/index.htm
