# Directories
SRC_DIRS := chat message utils

# Find all Java files in source directories
SRC_FILES := $(wildcard $(SRC_DIRS:%=%/*.java))

# Default target: compile the project
all: $(SRC_FILES)
	javac $(SRC_FILES)

# Clean up generated .class files
clean:
	find $(SRC_DIRS) -name "*.class" -type f -delete

.PHONY: all clean
