#!/usr/bin/env python3
"""
Script to replace deprecated classes with their new counterparts.
Extracts @link references from @deprecated annotations and performs replacements.
"""

import argparse
import os
import re
import subprocess
from pathlib import Path
from typing import Dict, Set

def find_deprecated_mappings(src_dir: str) -> Dict[str, str]:
    """Find deprecated classes and their replacements from @deprecated @link annotations."""
    mappings = {}
    
    # Search for deprecated classes with @link annotations using multiple patterns
    patterns = [
        '@deprecated.*{@link',
        '@Deprecated.*{@link', 
        'deprecated.*{@link'
    ]
    
    for pattern in patterns:
        cmd = ['grep', '-r', '-n', pattern, src_dir, '--include=*.java', '-i']
        result = subprocess.run(cmd, capture_output=True, text=True)
        
        for line in result.stdout.strip().split('\n'):
            if not line:
                continue
                
            # Handle potential colons in file paths by finding the last occurrence  
            # that separates file:line:content
            colon_positions = [i for i, char in enumerate(line) if char == ':']
            if len(colon_positions) < 2:
                continue
                
            # Take the second colon as the separator between line number and content
            second_colon = colon_positions[1]
            file_path = line[:colon_positions[0]]
            content = line[second_colon + 1:].strip()
            
            # Extract old class from file path
            path_obj = Path(file_path)
            if '/src/main/java/' not in str(path_obj):
                continue
                
            # Get package and class name from file path
            java_path = str(path_obj).split('/src/main/java/')[1]
            package_name = java_path.replace('/', '.').replace('.java', '')
            
            # Extract new class from @link annotation - try multiple patterns
            link_patterns = [
                r'{@link\s+([^}]+)}',
                r'{@link\s+([^}\s]+)',
            ]
            
            for link_pattern in link_patterns:
                link_match = re.search(link_pattern, content)
                if link_match:
                    new_class = link_match.group(1).strip()
                    # Clean up the new class name (remove any trailing text)
                    new_class = re.sub(r'\s+.*$', '', new_class)
                    new_class = re.sub(r'}.*$', '', new_class)  # Remove closing braces
                    if new_class and '.' in new_class:  # Valid fully qualified class name
                        mappings[package_name] = new_class
                        break
                    
    return mappings

def find_all_java_files(src_dir: str) -> Set[str]:
    """Find all Java files in the source directory."""
    java_files = set()
    for root, _, files in os.walk(src_dir):
        for file in files:
            if file.endswith('.java'):
                java_files.add(os.path.join(root, file))
    return java_files

def replace_in_file(file_path: str, mappings: Dict[str, str], dry_run: bool = False) -> int:
    """Replace deprecated class references in a single file."""
    replacements_made = 0
    
    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            content = f.read()
        
        original_content = content
        
        for old_class, new_class in mappings.items():
            # Get the simple class name (last part after the last dot)
            old_simple_name = old_class.split('.')[-1]
            
            # Count replacements for this mapping
            mapping_replacements = 0
            
            # Pattern 1: Full qualified name usage
            pattern1 = re.compile(r'\b' + re.escape(old_class) + r'\b')
            matches = pattern1.findall(content)
            mapping_replacements += len(matches)
            content = pattern1.sub(new_class, content)
            
            # Pattern 2: Import statements
            import_pattern = re.compile(r'^import\s+' + re.escape(old_class) + r'\s*;', re.MULTILINE)
            import_matches = import_pattern.findall(content)
            mapping_replacements += len(import_matches)
            content = import_pattern.sub(f'import {new_class};', content)
            
            # Pattern 3: Simple class name usage (only if file contains the old class)
            if old_class in original_content or f'import {old_class}' in original_content:
                # Add import for new class if not present and not in same package
                new_import = f'import {new_class};'
                new_package = new_class.rsplit('.', 1)[0] if '.' in new_class else ''
                
                # Check if file is in same package as new class
                package_match = re.search(r'^package\s+([^;]+);', content, re.MULTILINE)
                current_package = package_match.group(1).strip() if package_match else ''
                
                # Only add import if not in same package and import not already present
                if (new_package != current_package and 
                    new_import not in content and 
                    f'import {new_class}' not in content):
                    
                    # Find where to insert the import
                    import_section = re.search(r'(import\s+[^;]+;\s*)+', content)
                    if import_section:
                        content = content[:import_section.end()] + new_import + '\n' + content[import_section.end():]
                    else:
                        # Insert after package declaration
                        if package_match:
                            content = content[:package_match.end()] + '\n' + new_import + '\n' + content[package_match.end():]
                
                # Replace simple name references (be careful not to replace unrelated classes with same name)
                simple_pattern = re.compile(r'\b' + re.escape(old_simple_name) + r'\b')
                simple_matches = simple_pattern.findall(content)
                # Only count these if we're confident they refer to the deprecated class
                if f'import {old_class}' in original_content:
                    mapping_replacements += len(simple_matches)
                content = simple_pattern.sub(new_class.split('.')[-1], content)
            
            replacements_made += mapping_replacements
        
        # Only write file if changes were made
        if content != original_content:
            if not dry_run:
                with open(file_path, 'w', encoding='utf-8') as f:
                    f.write(content)
            # Return the actual number of replacements made
            return replacements_made
        else:
            return 0
            
    except Exception as e:
        print(f"Error processing {file_path}: {e}")
        
    return 0

def main():
    """Main function to run the replacement script."""
    parser = argparse.ArgumentParser(
        description='Replace deprecated classes with their new counterparts'
    )
    parser.add_argument(
        '--dry-run', 
        action='store_true', 
        help='Show what would be changed without actually modifying files'
    )
    parser.add_argument(
        '--src-dir',
        default='/Users/kgoderis/Development/openhab/git/openhab-core/bundles/org.openhab.core.ai/src/main/java',
        help='Source directory to process (default: current project src/main/java)'
    )
    
    args = parser.parse_args()
    src_dir = args.src_dir
    dry_run = args.dry_run
    
    if dry_run:
        print("=== DRY RUN MODE - No files will be modified ===")
    
    print("Finding deprecated class mappings...")
    mappings = find_deprecated_mappings(src_dir)
    
    if not mappings:
        print("No deprecated class mappings found.")
        return
    
    print(f"Found {len(mappings)} deprecated class mappings:")
    for old, new in mappings.items():
        print(f"  {old} -> {new}")
    
    print("\nFinding Java files...")
    java_files = find_all_java_files(src_dir)
    print(f"Found {len(java_files)} Java files.")
    
    action_verb = "Would modify" if dry_run else "Modified"
    print(f"\n{'Simulating' if dry_run else 'Performing'} replacements...")
    total_replacements = 0
    files_modified = 0
    
    for file_path in java_files:
        replacements = replace_in_file(file_path, mappings, dry_run)
        if replacements > 0:
            files_modified += 1
            total_replacements += replacements
            relative_path = file_path.replace(src_dir, '')
            print(f"  {action_verb} {relative_path}: {replacements} replacements")
    
    print(f"\nSummary:")
    print(f"  Files processed: {len(java_files)}")
    print(f"  Files {'that would be' if dry_run else ''} modified: {files_modified}")
    print(f"  Total replacements {'that would be made' if dry_run else 'made'}: {total_replacements}")
    print(f"  Deprecated classes: {len(mappings)}")
    
    if dry_run and files_modified > 0:
        print(f"\nTo apply these changes, run the script without --dry-run")

if __name__ == '__main__':
    main()