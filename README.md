# tasklab

This small application makes your life easier in two situations:

- When you're browsing the web, Twitter, Arxiv... and you find something interesting that you quickly want to save for later
- When you want to manage your own personal TODO list, and you want it to be versioned, private, secure but still synchronized in the cloud.

It achieves the first use case by adding itself in the "share via..." menu of Android: whenever you find something interesting, you
may just click on share, then on this TaskLab icon, and your URL is automatically added as a new item in your current TODO list.

It achieves the second use case by synchronizing the local TODO list (that you may of course also edit via the main TaskLab application)
with any gitlab server.
I'm aware that developers commonly manage their TODO lists with issue trackers, but while issue trackers are perfect for team work,
they're an overkill for personal private TODO lists. And they are clearly too slow to deal with on a mobile: I rather want
a very fast app where adding and modifying a task is done in just one click.

Now, because git pull/push is not so easy to do in android, the idea here is to rather rely on a git(lab/hub) api for pulling  and commits.
Ultimately, a companion desktop app or web service should be done to get a similar service on your desktop, but this is less urgent
as you can always for now access this todo list with any git client, or on the gitlab web page.

