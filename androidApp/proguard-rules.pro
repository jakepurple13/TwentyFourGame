# Project-specific R8 rules for the release build.
# Compose, AndroidX, DataStore and Firebase ship their own consumer rules, so nothing
# library-wide belongs here. Add narrow rules only for classes R8 reports as missing in
# build/outputs/mapping/release/missing_rules.txt.

# Keep file and line info so Crashlytics stack traces can be retraced with mapping.txt.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
