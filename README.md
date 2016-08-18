# gitbucket-desktopnotify-plugin

This plugin enhances [gitbucket/gitbucket](https://github.com/gitbucket/gitbucket) by providing an notify desktop.

## Features

It will notify the if there is something of activity desktop

## Usage

- Open a shell window at the root of the project, hit `sbt package`
- if you update gitbucket-desktopnotify-plugin, remove any existing copy of gitbucket-desktopnotify-plugin from GITBUCKET_HOME/plugins
- Copy target/scala-2.11/gitbucket-desktopnotify-plugin_2.11-x.x.jar into GITBUCKET_HOME/plugins
- Restart GitBucket

## Release Notes

### 4.3

- update to GitBucket 4.3
- support for anonymous access deny

### 4.0

- update to GitBucket 4.0

### 1.0

- introduce gitbucket-desktopnotify-plugin
