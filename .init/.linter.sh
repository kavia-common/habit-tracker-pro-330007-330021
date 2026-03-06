#!/bin/bash
cd /home/kavia/workspace/code-generation/habit-tracker-pro-330007-330021/habit_tracker_android_app
./gradlew lint
LINT_EXIT_CODE=$?
if [ $LINT_EXIT_CODE -ne 0 ]; then
   exit 1
fi

