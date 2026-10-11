# Upload HOTELIER to GitHub (Windows, PowerShell, `main` branch)

You do **not** need to run the project locally. You only need Git to upload it.

## 0. Extract the ZIP and find the project root

1. Right-click `hotelier.zip` → **Extract All…** → choose e.g. `C:\Projects`.
2. Open the extracted folder. **The project root is the folder that directly contains** `backend`, `frontend`, `docs`, `README.md` and `.gitignore`.
3. ZIP tools sometimes create a double folder (`hotelier\hotelier\backend`). If you open the first `hotelier` and see only *one* folder inside it, go one level deeper. Wrong root is the #1 reason hosting platforms cannot find `backend/`.
4. (Optional) In PowerShell you can check: `Get-ChildItem` must list `backend`, `frontend`, `docs`.

## 1. Install and verify Git

Download "Git for Windows" from https://git-scm.com/download/win (accept the defaults). Close and reopen PowerShell, then:

```powershell
git --version
```

Tell Git who you are (once):

```powershell
git config --global user.name "Your Name"
git config --global user.email "you@example.com"
```

Use the same e-mail as your GitHub account (or GitHub's private `noreply` e-mail).

## 2. Create an empty repository on GitHub

1. Sign in at https://github.com → **+** (top right) → **New repository**.
2. Name: `hotelier`. Choose Public or Private.
3. **Do NOT** tick "Add a README", ".gitignore" or "license" – leave the repo completely empty.
4. Click **Create repository** and copy the HTTPS URL, e.g. `https://github.com/YOUR-USERNAME/hotelier.git`.

## 3. Commit and push

Go into the project root (change the path to yours):

```powershell
cd C:\Projects\hotelier
Get-ChildItem
```

Create the repository on your PC, using `main` as the branch name:

```powershell
git init -b main
git branch --show-current
git status
```

`git branch --show-current` must print `main`. `git status` lists untracked files; `node_modules`, `target`, `.env` must **not** appear (the `.gitignore` hides them).

Stage and commit:

```powershell
git add .
git status
git commit -m "Initial commit: HOTELIER hotel management system"
```

Connect to GitHub and push:

```powershell
git remote add origin https://github.com/YOUR-USERNAME/hotelier.git
git remote -v
git push -u origin main
```

A browser window (Git Credential Manager) will ask you to sign in to GitHub – do it. If it asks for a password in the terminal, GitHub no longer accepts account passwords: create a **Personal Access Token** (GitHub → Settings → Developer settings → Personal access tokens) and paste that as the password. Never share or commit a token.

## 4. Verify the upload

```powershell
git log --oneline
git ls-files | Select-Object -First 15
git remote -v
git branch -a
```

Then refresh the GitHub page: you should see `backend`, `frontend`, `docs`, `README.md` at the **top level**, and the branch selector should say `main`.

## 5. Later changes

```powershell
git status
git add .
git commit -m "Describe what you changed"
git push
```

## Why Render (or another host) may not "see" your repository

Diagnose with the commands in section 4 first. Do not guess, and do **not** delete `.git` or run `git push --force` as a first reaction.

| Symptom | Likely cause | How to check / fix |
|---|---|---|
| Repo missing from Render's list | Render's GitHub app has no access to it, or is installed on a different account/organisation | In Render → Account Settings → GitHub, use "Configure" and grant access to the `hotelier` repository. Check you are signed in to the GitHub account that owns it |
| Repo is listed but "empty" or no code | The push never happened, or you pushed to a different branch | `git remote -v` must show your repo; `git log --oneline` must show a commit; the GitHub page must show files |
| Build says branch not found | Repo uses `master` or another branch while the service expects `main` | `git branch --show-current`; in GitHub check the default branch; set the same branch in Render |
| "Dockerfile not found" / "package.json not found" | **Root Directory** is wrong | Files must be at `backend/Dockerfile` and `frontend/package.json` in the repo. Set Root Directory to `backend` / `frontend` |
| Everything is inside one extra folder on GitHub (`hotelier/backend/...`) | You ran `git init` one level too high | Compare GitHub's top level with section 0. Fix by moving into the correct folder and re-uploading into a new empty repo, or set Root Directory to `hotelier/backend` |
| A single `.zip` file is on GitHub | The ZIP itself was uploaded | Hosting cannot read inside a ZIP. Extract it and push the extracted files |
| `git push` rejected: "fetch first" | The GitHub repo is not empty (it has a README you created) | Run `git pull origin main --rebase`, resolve any message, then `git push`. Read the output before choosing; do not force-push |
| `fatal: remote origin already exists` | You ran `remote add` twice | `git remote -v` to inspect; `git remote set-url origin <URL>` to correct it |
| 403 on push | Logged in as the wrong GitHub user or token lacks permission | Sign out in Windows Credential Manager (search the Start menu) and push again |

## No Git? Alternative

On the GitHub repo page choose **Add file → Upload files** and drag in the *contents* of the project root (the `backend`, `frontend`, `docs` folders and the loose files, **not** the ZIP). Folders with many files may need a few batches. Using Git is more reliable.
