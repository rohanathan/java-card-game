## Development Guides

### Git

#### 1.General

- Make sure you are connected to the _Cisco VPN_ if you are off campus
- If you are not ok with VPN you can use the _HTTPS_ repo address
- You need to have two remote addresses associated with your local machine
  1. **origin**: which is your own forked repo, this is used for `push` and
     `pull`
  2. **upstream**: This is the main repo. This _**MUST**_ only be used for
     pulling the changes to keep your project up to date with the development

#### 2. Workflow

- First you _**MUST**_ ensure that your `local/forked` project is up to date
  with the current main dev!
  - DO NOT start working without pulling the most up to date changes!
  - This will make your life hell when you start to `push`!
- Once you have the up to date project, you need to **branch** out
  - I would personaly name the branch with the issue number.
  - Feel free to give it a title alongside as well
- Once you are happy with the changes, you need to `merge` into your `main`
  branch
- Then you need to `push` to your own repo
- log in to gitlab and perform a `merge request`
- We then test (see below) and discuss any issues or changes required
- Once everyone is happy and gave the _thumbs up 👍_ we can go ahead and `merge`
  into the main development repo

#### 3. Testing the Merge Request

- You will need to login to the gitlab
- Go to `ITSD-TeamNo-30/cardGame` repo
- click on the _Code_ and follow the instructions to checkout the request for
  testing
  - before you can test the `merge request` you need to ensure your working
    directory is clean, ie, everything has been commited, or stashed otherwise
    you will have problems!
  - The instructions from gitlab will `pull` the request and then `checkout`
    into it automatically
- Once you are done with testing you can feel free to checkout your own main or
  whatever and then `delete` that branch

#### 4. Common Commands if You are using `cli`

```bash
# create a branch
git branch <branch name>
```

```bash
# checkignout a branch
git checkout <branch name>
```

```bash
# brings your main to the most up to date dev repo

git checkout main #if not in main
git pull upstream/main
```

```bash
git add <whatever you want to stage>
```

```bash
git commit -m "<message>"
```

```bash
# push to your fork
git push
```

##### Pushing to the repo

```bash
# Once you commited to the feature branch

# 1. Checkout main 
git checkout main
# 2. MAKING SURE UP TO DATE
git pull upstream main

# 3. Checkout your featured branch
git checkout <branch name>

# 4. merge the new changes
git merge main

git push
```

> You might get asked if you want the remote to create a branch with same name
> say yes

### Gitlab

#### Issues

- All issues need to have correct labels the most important ones being:
  - priorities (P0, P1, P2, P3, P4)
  - Story Points (C<number>)
  - The Sprint it belongs to (SprintONE ...)
    - This can be added when assiging issues to the team member (preferably)
    - Or by the person who is assigned an issue
  - Other general lables to say what functionality we are working on
- The issues and sprints are automatically then organised in the
  `plans > Issue Board`
  - This should include automatic closure using the correct `commit message`

#### Milestones

**TBA**

#### Merge Rerquests

- They need to be tested by all team members to ensure there are no bugs
- Once everyone is happy and given _Thumbs Up 👍_ we can go ahead and merge

> DO NOT MERGE WITHOUT DISCUSSIONS

- Feel free to assign to notify team members of the new `merge request`

### Unit Test

**TBA**
vitaly test
