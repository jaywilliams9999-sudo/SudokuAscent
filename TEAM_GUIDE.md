# SudokuAscent — Team Guide

> **Branch:** `DevOpsJay`
> **Last updated:** March 23, 2026

---

## What Was Just Done

### Git LFS Migration

The training dataset `intelligence-service/training/sudoku-3m.csv` (~511 MB) was exceeding GitHub's 100 MB file-size limit, which blocked all pushes.

To fix this, the following steps were performed:

1. **Initialized Git LFS** and configured it to track all `*.csv` files (see `.gitattributes`).
2. **Ran `git lfs migrate import`** to rewrite the entire branch history so that `sudoku-3m.csv` (and any other CSV files) are stored as LFS objects instead of regular git blobs.
3. **Force-pushed** the rewritten `DevOpsJay` branch to the remote.

> ⚠️ **Because history was rewritten, every team member must re-clone or reset their local copy** (see instructions below).

---

## What's in This Branch

```
SudokuAscent/
├── backend/                    # Java/Spring backend (Maven)
│   └── pom.xml
│
├── frontend/                   # React frontend
│   ├── package.json
│   └── src/
│       └── App.jsx
│
├── intelligence-service/       # Python ML / AI service
│   ├── app/
│   │   ├── app.py              # Main application entry point
│   │   └── inference.py        # Model inference logic
│   ├── feature_extractor/
│   │   ├── __init__.py
│   │   ├── basic_stats.py      # Statistical feature extraction
│   │   └── tactic_checker.py   # Sudoku tactic detection
│   ├── models/
│   │   └── model.pkl           # Trained model artifact
│   ├── training/
│   │   ├── data_prep.py        # Data preparation script
│   │   ├── sudoku-3m.csv       # 3M puzzle dataset (~511 MB, stored via Git LFS)
│   │   └── train_model         # Model training script
│   └── requirements.txt
│
├── sudoku/                     # Python virtual environment (venv)
│
├── docs/
│   └── architecture-diagram.png
│
├── .gitattributes              # LFS tracking rules (*.csv)
├── .gitignore
├── README.md
└── TEAM_GUIDE.md               # ← You are here
```

---

## How to Set Up Your Local Copy

### Option A — Fresh Clone (Recommended)

If you have **never cloned** this repo, or want to start clean:

```bash
# 1. Install Git LFS (one-time setup per machine)
git lfs install

# 2. Clone the repo — LFS files are pulled automatically
git clone https://github.com/jaywilliams9999-sudo/SudokuAscent.git
cd SudokuAscent

# 3. Switch to the DevOpsJay branch
git checkout DevOpsJay

# 4. Verify LFS files were downloaded
git lfs ls-files
```

### Option B — You Already Have a Clone

Because the branch history was rewritten by the LFS migration, your local `DevOpsJay` branch is now out of sync. **Do not try to merge or rebase** — reset instead:

```bash
# 1. Install Git LFS if you haven't already
git lfs install

# 2. Fetch the latest from the remote
git fetch origin

# 3. Hard-reset your local branch to match the remote
git checkout DevOpsJay
git reset --hard origin/DevOpsJay

# 4. Pull LFS objects
git lfs pull

# 5. Verify
git lfs ls-files
```

---

## Creating Your Own Branch

Once your local copy is up to date:

```bash
# Create and switch to your new branch from DevOpsJay
git checkout -b YourName-feature-name

# ... make your changes ...

# Stage, commit, and push
git add .
git commit -m "Description of your changes"
git push origin YourName-feature-name
```

---

## Important Rules Going Forward

| Rule | Details |
|------|---------|
| **CSV files are tracked by LFS** | Any `*.csv` file you add will automatically go through Git LFS. No extra steps needed. |
| **Install Git LFS before cloning** | Run `git lfs install` once per machine. Without it, you'll get pointer files instead of actual data. |
| **Never commit files > 100 MB without LFS** | GitHub will reject the push. If you need to track other large file types, add them: `git lfs track "*.extension"` |
| **Don't force-push shared branches** | Only force-push if you've coordinated with the team (like this LFS migration). |
