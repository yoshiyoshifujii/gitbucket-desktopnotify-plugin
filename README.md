# gitbucket-desktopnotify-plugin

This plugin enhances [gitbucket/gitbucket](https://github.com/gitbucket/gitbucket) by providing an notify desktop.

## Features

Shows a desktop notification for new activity (issues, pull requests, pushes, comments, ...) by other users in
repositories you can read, while a GitBucket page is open in the browser.

- Signed-in users only; your own activity is not notified.
- Click the bell in the header to allow notifications; it is shown until you allow or block them. Until then, nothing is polled.
- New activity is checked every 30 seconds. Clicking a notification opens the issue, pull request or repository.
- Nothing is shown if the news feed is disabled (`gitbucket.disableNewsFeed`).

## Compatibility

Plugin version | GitBucket version
:--------------|:-----------------
5.0.x          | 4.48.x -
4.32.x         | 4.32.x - 4.33.x

GitBucket 4.34.0 moved the activity from the database to `GITBUCKET_HOME/activity.log`; 5.0.0 reads that file.

## Usage

- Open a shell window at the root of the project, hit `sbt assembly`
- if you update gitbucket-desktopnotify-plugin, remove any existing copy of gitbucket-desktopnotify-plugin from GITBUCKET_HOME/plugins
- Copy target/scala-2.13/gitbucket-desktopnotify-plugin-x.x.x.jar into GITBUCKET_HOME/plugins
- Restart GitBucket
