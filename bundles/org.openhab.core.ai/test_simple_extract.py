#!/usr/bin/env python3
"""
Simple test to analyze the EventFilter.java file structure
"""

from pathlib import Path
import re

def analyze_file(file_path):
    content = Path(file_path).read_text(encoding='utf-8')
    lines = content.split('\n')
    
    print(f"Analyzing: {file_path}")
    print(f"Total lines: {len(lines)}")
    print()
    
    # Find all class/interface/enum declarations
    for i, line in enumerate(lines, 1):
        stripped = line.strip()
        if re.search(r'(class|interface|enum)\s+\w+', stripped):
            indent = len(line) - len(line.lstrip())
            print(f"Line {i:3d} (indent {indent:2d}): {stripped}")
    
    print()
    # Analyze brace structure
    brace_count = 0
    for i, line in enumerate(lines, 1):
        old_count = brace_count
        brace_count += line.count('{') - line.count('}')
        
        if '{' in line or '}' in line:
            print(f"Line {i:3d}: brace_count {old_count} -> {brace_count}: {line.strip()}")

if __name__ == "__main__":
    analyze_file("test_extraction/EventFilter.java")