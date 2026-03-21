#!/usr/bin/env bash

OUTPUT_FILE="output.txt"
ROOT_DIR="$(pwd)"

> "$OUTPUT_FILE"

find "$ROOT_DIR" -type f \( -name "*.java" -o -name "*.groovy" -o -name "*.md" -o -name "*.json" -o -name "*.gradle" \) ! -path "*/build/*" -print0 | \
sort -z | \
while IFS= read -r -d '' file; do
    relative_path="${file#$ROOT_DIR/}"

    echo "[begin: $relative_path]" >> "$OUTPUT_FILE"
    cat "$file" >> "$OUTPUT_FILE"
    echo "" >> "$OUTPUT_FILE"
    echo "[end: $relative_path]" >> "$OUTPUT_FILE"
    echo "" >> "$OUTPUT_FILE"
done

echo "Done. Output written to $OUTPUT_FILE"