#!/usr/bin/env python3

import os
import re
import sys
from typing import List, Dict, Tuple, Set

def extract_inner_constructs_from_file(file_path: str) -> List[Dict]:
    """Extract all inner constructs from a Java file."""
    print(f"Processing: {file_path}")
    
    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            content = f.read()
    except Exception as e:
        print(f"Error reading {file_path}: {e}")
        return []
    
    lines = content.split('\n')
    package_name = extract_package_name(content)
    imports = extract_imports(content)
    
    inner_constructs = find_inner_constructs(lines, package_name)
    
    # Extract each inner construct
    for construct in inner_constructs:
        construct['content'] = extract_construct_content(lines, construct)
        construct['imports'] = determine_needed_imports(construct['content'], imports)
    
    return inner_constructs

def extract_package_name(content: str) -> str:
    """Extract package name from Java file content."""
    match = re.search(r'^package\s+([\w.]+);', content, re.MULTILINE)
    return match.group(1) if match else ""

def extract_imports(content: str) -> List[str]:
    """Extract all imports from Java file content."""
    imports = []
    for line in content.split('\n'):
        match = re.match(r'import\s+([\w.*]+);', line.strip())
        if match:
            imports.append(match.group(1))
    return imports

def find_inner_constructs(lines: List[str], package_name: str) -> List[Dict]:
    """Find all inner constructs in the file."""
    constructs = []
    outer_class_found = False
    brace_level = 0
    
    for i, line in enumerate(lines):
        # Skip package and import statements
        if re.match(r'^\s*(package|import)\s+', line):
            continue
        
        # Track braces
        brace_level += line.count('{') - line.count('}')
        
        # Look for class/interface/enum declarations
        pattern = r'^\s*(public|private|protected|static|\s)*\s*(abstract|final|\s)*\s*(class|interface|enum)\s+([A-Za-z_][A-Za-z0-9_]*)'
        match = re.match(pattern, line)
        if match:
            if not outer_class_found:
                outer_class_found = True
                continue
            
            # This is an inner construct
            construct_type = match.group(3)
            construct_name = match.group(4)
            modifiers = match.group(1).strip() if match.group(1) else ''
            
            constructs.append({
                'type': construct_type,
                'name': construct_name,
                'start_line': i,
                'modifiers': modifiers,
                'package': package_name,
                'visibility': determine_visibility(modifiers),
                'is_static': 'static' in modifiers
            })
    
    return constructs

def extract_construct_content(lines: List[str], construct: Dict) -> str:
    """Extract the complete content of an inner construct."""
    start_line = construct['start_line']
    brace_level = 0
    content_lines = []
    in_construct = False
    
    for i in range(start_line, len(lines)):
        line = lines[i]
        
        if not in_construct and '{' in line:
            in_construct = True
        
        if in_construct:
            content_lines.append(line)
            brace_level += line.count('{') - line.count('}')
            
            if brace_level == 0:
                break
    
    return '\n'.join(content_lines)

def determine_needed_imports(content: str, available_imports: List[str]) -> List[str]:
    """Determine which imports are needed for the extracted construct."""
    needed_imports = []
    
    # Common Java types that need imports
    import_patterns = {
        'AtomicBoolean': 'java.util.concurrent.atomic.AtomicBoolean',
        'AtomicLong': 'java.util.concurrent.atomic.AtomicLong',
        'Map': 'java.util.Map',
        'ConcurrentHashMap': 'java.util.concurrent.ConcurrentHashMap',
        'List': 'java.util.List',
        'ArrayList': 'java.util.ArrayList',
        'Set': 'java.util.Set',
        'HashSet': 'java.util.HashSet',
        'Logger': 'org.slf4j.Logger',
        'LoggerFactory': 'org.slf4j.LoggerFactory',
    }
    
    for type_name, import_path in import_patterns.items():
        if type_name in content and import_path in available_imports:
            needed_imports.append(import_path)
    
    # Add NonNullByDefault if present in original file
    if 'org.eclipse.jdt.annotation.NonNullByDefault' in available_imports:
        needed_imports.append('org.eclipse.jdt.annotation.NonNullByDefault')
    
    if 'org.eclipse.jdt.annotation.Nullable' in available_imports:
        needed_imports.append('org.eclipse.jdt.annotation.Nullable')
    
    return needed_imports

def determine_visibility(modifiers: str) -> str:
    """Determine the visibility modifier to use for standalone class."""
    if 'public' in modifiers:
        return 'public'
    elif 'protected' in modifiers:
        return 'public'  # Make public for external access
    elif 'private' in modifiers:
        return 'public'  # Make public for external access
    else:
        return 'public'  # Default to public for package access

def create_standalone_file(construct: Dict, original_file_path: str) -> str:
    """Create a standalone Java file for the inner construct."""
    package_name = construct['package']
    construct_name = construct['name']
    construct_type = construct['type']
    content = construct['content']
    imports = construct['imports']
    
    # Determine file path
    package_dir = package_name.replace('.', '/')
    src_base = original_file_path.split('src/main/java/')[0] + 'src/main/java/'
    file_path = os.path.join(src_base, package_dir, f"{construct_name}.java")
    
    # Create the file content
    file_content = f"""package {package_name};

"""
    
    # Add imports
    if imports:
        for imp in sorted(set(imports)):
            file_content += f"import {imp};\n"
        file_content += "\n"
    
    # Add JavaDoc
    file_content += f"""/**
 * {construct_name} {construct_type}.
 * 
 * Originally extracted from {os.path.basename(original_file_path)} as an inner {construct_type}.
 * This {construct_type} has been extracted to improve code organization and reusability.
 * 
 * @author Generated - Inner Class Extraction
 * @since 1.0.0
 */
"""
    
    # Add NonNullByDefault annotation if needed
    if 'org.eclipse.jdt.annotation.NonNullByDefault' in imports:
        file_content += "@NonNullByDefault\n"
    
    # Add the construct content (modified for standalone use)
    content_lines = content.split('\n')
    for line in content_lines:
        # Replace inner class declaration with public class
        if re.match(r'^\s*(public|private|protected|static|\s)*\s*(abstract|final|\s)*\s*(class|interface|enum)\s+' + construct_name, line):
            # Make it public and remove static if present
            visibility = determine_visibility(construct['modifiers'])
            other_modifiers = construct['modifiers'].replace('static', '').replace('public', '').replace('private', '').replace('protected', '').strip()
            if other_modifiers:
                file_content += f"{visibility} {other_modifiers} {construct_type} {construct_name}"
            else:
                file_content += f"{visibility} {construct_type} {construct_name}"
            
            # Add the rest of the line (extends, implements, etc.)
            remaining = line.split(construct_name, 1)[1] if construct_name in line else ""
            file_content += remaining + "\n"
        else:
            file_content += line + "\n"
    
    return file_path, file_content

def update_original_file(file_path: str, constructs_to_remove: List[Dict]):
    """Update the original file to remove inner constructs and add imports."""
    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            content = f.read()
    except Exception as e:
        print(f"Error reading {file_path}: {e}")
        return False
    
    lines = content.split('\n')
    
    # Remove inner constructs (in reverse order to maintain line numbers)
    constructs_to_remove.sort(key=lambda x: x['start_line'], reverse=True)
    
    for construct in constructs_to_remove:
        start_line = construct['start_line']
        lines = remove_construct_from_lines(lines, construct)
    
    # Add imports for extracted classes
    import_section_end = find_import_section_end(lines)
    new_imports = []
    
    for construct in constructs_to_remove:
        # Add import for the extracted class if it's in the same package
        if construct['package'] and construct['name']:
            new_imports.append(f"import {construct['package']}.{construct['name']};")
    
    if new_imports:
        # Insert new imports
        for i, imp in enumerate(sorted(set(new_imports))):
            lines.insert(import_section_end + i + 1, imp)
    
    # Write back the modified content
    try:
        with open(file_path, 'w', encoding='utf-8') as f:
            f.write('\n'.join(lines))
        return True
    except Exception as e:
        print(f"Error writing {file_path}: {e}")
        return False

def remove_construct_from_lines(lines: List[str], construct: Dict) -> List[str]:
    """Remove an inner construct from the lines."""
    start_line = construct['start_line']
    brace_level = 0
    in_construct = False
    end_line = start_line
    
    for i in range(start_line, len(lines)):
        line = lines[i]
        
        if not in_construct and '{' in line:
            in_construct = True
        
        if in_construct:
            brace_level += line.count('{') - line.count('}')
            
            if brace_level == 0:
                end_line = i
                break
    
    # Remove the construct and any preceding javadoc/comments
    remove_start = start_line
    
    # Look backward for javadoc comments
    for i in range(start_line - 1, -1, -1):
        line = lines[i].strip()
        if line.startswith('/**') or line.startswith('*') or line.startswith('*/') or line == '':
            remove_start = i
        else:
            break
    
    # Remove the lines
    del lines[remove_start:end_line + 1]
    
    return lines

def find_import_section_end(lines: List[str]) -> int:
    """Find the end of the import section."""
    import_end = 0
    for i, line in enumerate(lines):
        if line.strip().startswith('import '):
            import_end = i
        elif line.strip().startswith('package '):
            continue
        elif line.strip() == '':
            continue
        elif import_end > 0:
            break
    return import_end

def process_all_files():
    """Process all Java files with inner constructs."""
    src_dir = 'src/main/java'
    if not os.path.exists(src_dir):
        print(f"Directory {src_dir} not found")
        return
    
    # Find all Java files with inner constructs using our previous analysis
    files_with_inner = [
        # Manually listing the first few for testing - in production would use find_inner_classes.py output
        'src/main/java/org/openhab/core/ai/transport/HttpServerConfiguration.java',
        'src/main/java/org/openhab/core/ai/config/ProtocolConfiguration.java',
        # Add more files as needed
    ]
    
    extraction_count = 0
    files_processed = 0
    
    for file_path in files_with_inner:
        if not os.path.exists(file_path):
            continue
            
        print(f"\nProcessing {file_path}...")
        
        # Extract inner constructs
        inner_constructs = extract_inner_constructs_from_file(file_path)
        
        if not inner_constructs:
            continue
        
        # Create standalone files
        created_files = []
        for construct in inner_constructs:
            try:
                standalone_path, standalone_content = create_standalone_file(construct, file_path)
                
                # Ensure directory exists
                os.makedirs(os.path.dirname(standalone_path), exist_ok=True)
                
                # Write the standalone file
                with open(standalone_path, 'w', encoding='utf-8') as f:
                    f.write(standalone_content)
                
                created_files.append(standalone_path)
                extraction_count += 1
                print(f"  Created: {standalone_path}")
                
            except Exception as e:
                print(f"  Error creating standalone file for {construct['name']}: {e}")
        
        # Update original file
        if created_files:
            if update_original_file(file_path, inner_constructs):
                print(f"  Updated: {file_path}")
                files_processed += 1
            else:
                print(f"  Failed to update: {file_path}")
    
    print(f"\nExtraction complete!")
    print(f"Files processed: {files_processed}")
    print(f"Inner constructs extracted: {extraction_count}")

if __name__ == "__main__":
    process_all_files()