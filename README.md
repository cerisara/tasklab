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

## Usage

- First create an empty repository, let us call it *todorep* in a gitlab server, for instance in gitlab.com or on your own gitlab instance.
- Then, on a desktop computer with git and connected with your gitlab server, add, commit and push a single file named "todo.txt" at the root of *todorep*. You may populate your todo.txt file with a few tasks (one per line), or just leave it empty for now.
- The app synchronizes through the gitlab API; so you need to create a *private token* in your gitlab instance (see [the gitlab doc](https://docs.gitlab.com/ce/api)); the app will ask for this token the first time you run it.
- When you first run the app, it will ask for the url of your gitlab repo and your private token. The URL must point to your todo.txt; so it must be of the form (check your gitlab instance doc to find the correct URL; you just need to point to the todo.txt file, the private token and other options will be added by the app):
	https://gitlab.domain.my/api/v4/projects/88/repository/files/todo.txt
- You may get the project ID with, e.g.:
	curl 'https://gitlab.domain.my/api/v4/projects?search=TOTO&owned=true&private_token=FDSFGJJGREIG'
- You can then press "GET" to download the HEAD version of your todo list from the gitlab server
- You can edit any line in your todo list by cliking once on it
- To add a new task, you just click on the bottom *New Task* line
- From outside the app, you can also add a new "task" by using the "share via" popup menu (for instance when you're browsing); in that case, simply choose the TaskLabApp in the share menu and the url will be added to your task list. This is a good option to store interesting references that you would like to access later on
- When you're connected to the internet, you should push your locally modified task list to the gitlab server by pressing the "PUT" button

## Limitations

- No support for conflicts; but in a normal usage with frequent synchronizations, conflicts should not happen; if they still do, it is easy to solve them with git on a PC. In all cases, the "Get" button in the app will warn and just download the HEAD repository file.


