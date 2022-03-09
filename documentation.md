# Documentation

The class `App` is the main class that apart from other methods includes also the `main` method.
This method is run when you run the program.

`Authorization` and `Server` classes are used to authorize the user with OAuth2.
The `Server` class creates a server on which OAuth2 is redirected. Both classes
follow the authorization protocol according to Strava API.
 - `Authorization` creates a URL that has to be opened in a browser to authorize via OAuth2
   - authorization in a browser is nearly impossible - APIs defend themselves this way against bots (like this one, hehe)
 - when the user clicks on the authorization button, he returns to the app while the authorization is being completed
   - `Server` obtains a code which is used by `Authorization` in a POST request to obtain an access token
   - when this is all done, we are ready for downloading the data

`Downloader` is used to get all the ids of all activities of the certain type.
It downloads it via multiple simple http requests. After that we use the ids
to download the data about every activity in the list of ids. This is, however, limited
by the Strava API limits (we will be able to get only ~100 activities which is enough for a whole year usually).

`GPXCreator` is then used to create a simple `.gpx` file from the downloaded raw data.

Finally, the `WebsiteCreator` class creates a simple website (with javascript),
which displays a [mapy.cz](mapy.cz) map with the activities.

