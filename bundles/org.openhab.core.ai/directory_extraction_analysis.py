#!/usr/bin/env python3
"""
Directory-based Inner Class Extraction Analysis Script
"""

import subprocess
import re
from collections import defaultdict

def analyze_directories():
    # Run the extraction dry run
    result = subprocess.run(['python3', 'final_robust_extractor.py', 'src/main/java', '--dry-run'], 
                          capture_output=True, text=True)
    
    lines = result.stderr.split('\n')
    
    directory_stats = defaultdict(lambda: {'files': 0, 'constructs': 0, 'details': []})
    
    for line in lines:
        if '📄' in line and ': ' in line and 'constructs' in line:
            # Extract file path and construct count
            match = re.search(r'📄 org/openhab/core/ai/(.+): (\d+) constructs', line)
            if match:
                file_path = match.group(1)
                construct_count = int(match.group(2))
                
                # Parse directory structure
                path_parts = file_path.split('/')
                if len(path_parts) == 1:
                    # Root file
                    top_dir = path_parts[0].split('.')[0] if '.' in path_parts[0] else 'unknown'
                    sub_dir = '[root]'
                elif len(path_parts) == 2:
                    # Top level directory
                    top_dir = path_parts[0]
                    sub_dir = '[root]'
                else:
                    # Has subdirectory
                    top_dir = path_parts[0]
                    sub_dir = path_parts[1]
                
                dir_key = f"{top_dir}/{sub_dir}"
                directory_stats[dir_key]['files'] += 1
                directory_stats[dir_key]['constructs'] += construct_count
                directory_stats[dir_key]['details'].append({
                    'file': file_path.split('/')[-1],
                    'constructs': construct_count
                })
    
    return directory_stats

def print_analysis(stats):
    print("# 📊 Java Files Inner Class Extraction by Directory Structure\n")
    
    # Sort by total constructs (descending)
    sorted_dirs = sorted(stats.items(), key=lambda x: x[1]['constructs'], reverse=True)
    
    # Group by top-level directory
    top_level_groups = defaultdict(list)
    for dir_path, data in sorted_dirs:
        top_level = dir_path.split('/')[0]
        top_level_groups[top_level].append((dir_path, data))
    
    total_files = 0
    total_constructs = 0
    
    print("## 📁 **DETAILED BREAKDOWN BY TOP-LEVEL DIRECTORY**\n")
    
    for top_level, dirs in sorted(top_level_groups.items(), key=lambda x: sum(d[1]['constructs'] for d in x[1]), reverse=True):
        top_level_files = sum(data['files'] for _, data in dirs)
        top_level_constructs = sum(data['constructs'] for _, data in dirs)
        
        print(f"### 📁 **{top_level.upper()}/** - {top_level_files} files ({top_level_constructs} constructs)")
        
        for dir_path, data in sorted(dirs, key=lambda x: x[1]['constructs'], reverse=True):
            _, sub_dir = dir_path.split('/', 1)
            if sub_dir == '[root]':
                print(f"└── 📄 **Direct files** - {data['files']} files ({data['constructs']} constructs)")
            else:
                print(f"├── 📁 **{sub_dir}/** - {data['files']} files ({data['constructs']} constructs)")
            
            # Show complex files (3+ constructs)
            complex_files = [f for f in data['details'] if f['constructs'] >= 3]
            if complex_files:
                for f in sorted(complex_files, key=lambda x: x['constructs'], reverse=True):
                    print(f"    - {f['file']}: {f['constructs']} constructs")
        
        print()
        total_files += top_level_files
        total_constructs += top_level_constructs
    
    print(f"## 📊 **OVERALL SUMMARY**")
    print(f"- **Total files to extract**: {total_files}")
    print(f"- **Total inner constructs**: {total_constructs}")
    print(f"- **Average constructs per file**: {total_constructs/total_files:.1f}")
    
    # Priority ranking
    print(f"\n## 🎯 **EXTRACTION PRIORITY RANKING**")
    priority_dirs = [(top_level, sum(data['constructs'] for _, data in dirs), sum(data['files'] for _, data in dirs)) 
                     for top_level, dirs in top_level_groups.items()]
    
    for i, (dir_name, constructs, files) in enumerate(sorted(priority_dirs, key=lambda x: x[1], reverse=True), 1):
        priority = "🔥 HIGH" if constructs >= 15 else "⚡ MEDIUM" if constructs >= 5 else "📝 LOW"
        print(f"{i}. **{dir_name.upper()}** - {files} files, {constructs} constructs ({priority})")

if __name__ == "__main__":
    stats = analyze_directories()
    print_analysis(stats)