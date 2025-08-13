#!/usr/bin/env python3

import os
import re
import sys

def find_files_with_inner_constructs(root_dir):
    """Find Java files with inner classes, interfaces, or enums."""
    inner_construct_files = []
    
    for root, dirs, files in os.walk(root_dir):
        for file in files:
            if file.endswith('.java'):
                file_path = os.path.join(root, file)
                inner_constructs = analyze_java_file(file_path)
                if inner_constructs:
                    inner_construct_files.append((file_path, inner_constructs))
    
    return inner_construct_files

def analyze_java_file(file_path):
    """Analyze a Java file for inner constructs."""
    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            content = f.read()
    except Exception as e:
        print(f"Error reading {file_path}: {e}")
        return []
    
    # Remove comments
    content = remove_comments(content)
    
    # Find all class/interface/enum declarations
    pattern = r'^\s*(public|private|protected|static|\s)*\s*(abstract|final|\s)*\s*(class|interface|enum)\s+([A-Za-z_][A-Za-z0-9_]*)'
    matches = []
    
    lines = content.split('\n')
    in_outer_class = False
    brace_level = 0
    outer_class_found = False
    
    for i, line in enumerate(lines, 1):
        # Skip package and import statements
        if re.match(r'^\s*(package|import)\s+', line):
            continue
            
        # Track braces
        brace_level += line.count('{') - line.count('}')
        
        # Look for class/interface/enum declarations
        match = re.match(pattern, line, re.MULTILINE)
        if match:
            construct_type = match.group(3)  # class, interface, or enum
            construct_name = match.group(4)
            
            if not outer_class_found:
                outer_class_found = True
                # This is the main outer class
                continue
            else:
                # This is an inner construct
                matches.append({
                    'type': construct_type,
                    'name': construct_name,
                    'line': i,
                    'modifiers': match.group(1).strip() if match.group(1) else '',
                    'full_line': line.strip()
                })
    
    return matches

def remove_comments(content):
    """Remove comments from Java content."""
    # Remove single-line comments
    content = re.sub(r'//.*', '', content)
    
    # Remove multi-line comments
    content = re.sub(r'/\*.*?\*/', '', content, flags=re.DOTALL)
    
    return content

def main():
    src_dir = 'src/main/java'
    if not os.path.exists(src_dir):
        print(f"Directory {src_dir} not found")
        sys.exit(1)
    
    files_with_inner = find_files_with_inner_constructs(src_dir)
    
    if not files_with_inner:
        print("No files with inner constructs found.")
        return
    
    print(f"Found {len(files_with_inner)} files with inner constructs:\n")
    
    for file_path, inner_constructs in files_with_inner:
        rel_path = os.path.relpath(file_path, src_dir)
        print(f"{rel_path}: {len(inner_constructs)} inner constructs")
        for construct in inner_constructs:
            print(f"  - Line {construct['line']}: {construct['type']} {construct['name']}")
        print()

if __name__ == "__main__":
    main()