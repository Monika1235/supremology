1. Android System Inside the Car (Device App)

This system is responsible for:

Detecting current speed

Knowing the speed limit (hardcoded / GPS-based / API)

Checking if speed > limit

Creating a SpeedViolation object

Uploading the violation to Firestore → "violations" collection

This is the data producer.
