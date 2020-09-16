# tasklab

Second version: it now exploits a client-server architecture,
with the client being a lightweight Java android app,
and the server a flask python application.

The objective is to bring powerful services onto the android smartphone,
with a bare minimum of consumed bandwidth (typically, a few kB).
To achieve this, the heavy work is realized on the server in python, and
a few simple text messages are transmitted between the client and the server.
The android client merely displays a scrollable list of texts, while the
state-full python server controls the interaction state by sending to the client
the list of text fields to show, and eventually to ask the user.
The python server also makes heavy use of a gitlab repository to save all of the
important information.

The target services include:

- get current weather
- read emails
- get various RSS feeds and their linked pages (through lynx browser)
- manage personal and pro calendars
- manage TODO lists and blocnotes
- Query arXiv abstracts
- Quick-save interesting URLs
- ...

# Primitives

So the communication between the client and the server is limited to the
following primitives (client point of view):

- select(item) when the user selects an item in the view
- menu() when the user presses the menu button
- save(text) for quick-saving some URL

In all cases, the server returns a list of items to show, separated with _n

