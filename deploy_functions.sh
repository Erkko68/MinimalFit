#!/bin/bash
set -e

cd "$(dirname "$0")"

if [ ! -d "functions/venv" ]; then
    echo "Creating virtualenv..."
    python3.11 -m venv functions/venv
    functions/venv/bin/pip install -r functions/requirements.txt
fi

echo "Deploying Firebase Functions..."
firebase deploy --only functions --project minimalfit-e4f0d

echo "Deploy complete."
