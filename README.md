# RoboCupRAS
extra assignment from Dr Babrdel Bonab, Mohammad. Implements AI, Multi-Agent Systems, Simulation, and Advanced OOP.

## Folder Structure

Note : To avoid inherit *.git* that might cause trouble. We need a clean structure

- Special Characters : ├── │ └──

```
Robocup (You name yourself whatever)
    ├── external_deps (clone external repo)
    │   ├── cloned-repo1
    │   ├── cloned-repo2
    │   └── cloned-repo3
    │
    └── RoboCupRAS (Our repo, start here !!)
        ├── README.md
        ├── .gitignore
        ├── build.gradle
        ├── settings.gradle
        ├── main/
        └── test/
```

**Reminder :**
    - Don't upload external_deps, to avoid inherit *.git*

## Git Command You Need to Know

| Git Command | Notes |
| -- | -- |
| git clone <SSH/URLS> | clone(copy) a repo from github |
| git status | view current git status |
| git checkout -b <BRANCH_NAME> | create a new branch |
| git checkout <BRANCH_NAME> | switch between branch |
| git branch | List All branch |
| git branch -d <BRANCH_NAME> | Delete a branch (Do this after PR merge) |
| git add . | stage all files |
| git commit -m <MESSAGE> | write commit message |
| git push origin <BRANCH_NAME> | Push a branch to GitHub |

**Pull Request :**

Just navigate to GitHub repo and do it

**For Corruption Handling :**
| Git Command | Notes |
| -- | -- |
| git log | Check history commit |
| git log --oneline | Check history in oneline |
| git restore --source=<COMMIT_HASH> . | restore all files to version specified |

**For Merge Conflict in Branch :**
- This happened if your branch is behind multiple commits from main

| Git Command | Notes |
| -- | -- |
| git checkout <main/master> | checkout to main/master branch |
| git fetch origin | bring the main commit to your branch |

- Use Text Editor to manually solve the merge conflict

```
<<<<<<<<<HEAD
YOUR CODES
============
MAIN CODES
>>>>>>>>>main/master
```

- delete and keep , then save , push

