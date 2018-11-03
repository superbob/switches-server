Switches
========

A simple server that can toggle NAT rules on a remote router.

[![Build
Status](https://travis-ci.org/superbob/switches-server.svg?branch=master)](https://travis-ci.org/superbob/switches-server)

My problem
----------

I have a router that I can configure only when I am inside my private home network (i.e: not from the Internet).

I'd like to control it, especially toggle NAT rules, when I'm not in my private home network (i.e: coming from the Internet).

I don't want to leave my private network open to the Internet (the least possible fixed NAT rules) with persistent SSH NAT rules.

Solution
--------

Look at the following diagram :

```
            |              |
+--------+  |  +--------+  |  +--------+
| Client | <=> | Bridge | <=> | Agent  |
+--------+  |  +--------+  |  +--------+
    ^       |              |      ^
    |       |              |      |
    O       |              |      v
   \|/      |              |  +--------+
    | me    |              |  | Router |
   / \      |              |  +--------+
            |              |
 Anywhere   |   Internet   | Private network
```

I have a **bridge** that is a web server on the Internet (somewhere on heroku) that is a rendezvous point between a **client** and an **agent**.

The **client** is a web page that I can run from anywhere (a desktop or a mobile browser).

For practical reasons the client page is hosted on the **bridge** web server, it could have been elsewhere.

The **agent** is small program that lives inside the private network and that has a full access to the router configuration. Especially, it can enable or disable NAT rules.

The client and the agent connect to the bridge through WebSockets using a custom 'send commands and notify status' protocol. This provides a very good reactivity.

This repository contents
------------------------

Here you can find two parts:

 * The bridge
 * The client actor

The agent actor source code is currently not available.

Technical details
-----------------

The bridge server is Java program using Ratpack and built with gradle.

The client is a simple HTML page with some JavaScript.

The agent is a python program.

Authentication is performed when connecting WebSockets to the bridge using Google Sign-In with Google ID Tokens.

The client authenticates with a personnal account (with a gmail address) using [Google Sign-In for Websites
](https://developers.google.com/identity/sign-in/web/).

The agent authenticates with an IAM service account. The ID token is obtained as described in [Authentication Between Services - Using a Google ID token](https://cloud.google.com/endpoints/docs/openapi/service-account-authentication)

Develop & run locally
---------------------

### Requirements

To run you will need a Google project OAuth 2 client ID and two principals (email addresses).

The client principal must be gmail address and the agent principal should be a google service account email associated with the Google project.

### Procedure using Gradle only

 1. Clone the repository
 2. Launch `./start-dev`
   * On first run, it will ask question to configure settings.
 3. (Optionally) start an agent, currently you will have to develop it as the source code of the agent I use is not available yet.
 4. Open a web browser to [localhost:5050](http://localhost:5050).

### Procedure using heroku local

 1. Clone the repository
 2. Make sure you have the required variables defined in the `.env` file (running `./start-dev`, then answering the questions, then leave, will create them)
 3. Create a `Procfile` containing the following line :
 ```
 web: build/install/switches-server/bin/switches-server
 ```
 4. Build the project by running `./gradlew installDist -x test`
 5. Run `herolu local web`
 6. Open a web browser to [localhost:5000](http://localhost:5000).

Deploy to Heroku
----------------

You will have to configure the following environment variables:

 * **AUDIENCE**: the google project client ID
 * **CLIENT_PRINCIPAL**: the email of the gmail account used
 * **AGENT_PRINCIPAL**: the email of the service account used for the agent

For example with heroku cli:
```
heroku config:set AUDIENCE=google-project-client-ID
heroku config:set CLIENT_PRINCIPAL=someone@gmail.com
heroku config:set AGENT_PRINCIPAL=some.account@some.project.iam.gserviceaccount.com
```

