#!/usr/bin/env python3
"""
Robust Inner Class Extraction Tool with Comprehensive Error Handling

This script safely extracts inner classes, interfaces, and enums from Java files
with extensive error handling, validation, and rollback capabilities.
"""

import os
import re
import shutil
import subprocess
from pathlib import Path
from typing import List, Dict, Optional, Tuple
import logging
import json
from datetime import datetime

# Setup logging
logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')
logger = logging.getLogger(__name__)

class JavaExtractor:
    def __init__(self, src_dir: str, backup_dir: str = None):
        self.src_dir = Path(src_dir)
        self.backup_dir = Path(backup_dir or f"backup_{datetime.now().strftime('%Y%m%d_%H%M%S')}")
        self.errors = []
        self.successes = []
        self.rollback_actions = []
        
    def create_backup(self):
        """Create a complete backup of the source directory"""
        try:
            if self.backup_dir.exists():
                shutil.rmtree(self.backup_dir)
            shutil.copytree(self.src_dir, self.backup_dir)
            logger.info(f"Backup created at: {self.backup_dir}")
            return True
        except Exception as e:
            logger.error(f"Failed to create backup: {e}")
            return False
    
    def rollback_changes(self):
        """Rollback all changes to the backup state"""
        try:
            if not self.backup_dir.exists():
                logger.error("No backup available for rollback")
                return False
                
            # Remove current directory and restore from backup
            if self.src_dir.exists():
                shutil.rmtree(self.src_dir)
            shutil.copytree(self.backup_dir, self.src_dir)
            logger.info("Successfully rolled back all changes")
            return True
        except Exception as e:
            logger.error(f"Rollback failed: {e}")
            return False
    
    def validate_java_file(self, file_path: Path) -> bool:
        """Validate that a Java file is syntactically correct"""
        try:
            # Basic syntax validation
            content = file_path.read_text(encoding='utf-8')
            
            # Check for basic Java syntax issues
            if not re.search(r'package\s+[\w.]+;', content):
                logger.warning(f"No package declaration found in {file_path}")
            
            # Count braces
            open_braces = content.count('{')
            close_braces = content.count('}')
            if open_braces != close_braces:
                logger.error(f"Mismatched braces in {file_path}: {open_braces} open, {close_braces} close")
                return False
            
            # Check for basic class/interface structure
            if not re.search(r'(class|interface|enum)\s+\w+', content):
                logger.warning(f"No class/interface/enum declaration found in {file_path}")
            
            return True
        except Exception as e:
            logger.error(f"Validation failed for {file_path}: {e}")
            return False
    
    def extract_imports(self, content: str) -> List[str]:
        """Extract import statements from Java content"""
        imports = []
        for line in content.split('\n'):
            line = line.strip()
            if line.startswith('import ') and line.endswith(';'):
                imports.append(line)
        return imports
    
    def extract_package(self, content: str) -> str:
        """Extract package declaration from Java content"""
        match = re.search(r'package\s+([\w.]+);', content)
        return match.group(1) if match else ""
    
    def find_inner_constructs(self, file_path: Path) -> List[Dict]:
        """Find all inner classes, interfaces, and enums in a Java file"""
        try:
            content = file_path.read_text(encoding='utf-8')
            lines = content.split('\n')
            
            constructs = []
            outer_brace_count = 0
            outer_construct_found = False
            
            i = 0
            while i < len(lines):
                line = lines[i]
                stripped = line.strip()
                
                # Skip comments and empty lines
                if stripped.startswith('//') or stripped.startswith('/*') or not stripped:
                    i += 1
                    continue
                
                # Count braces to track nesting level
                line_open_braces = line.count('{')
                line_close_braces = line.count('}')
                
                if not outer_construct_found:
                    # Look for the outer class/interface/enum
                    if re.search(r'(public\s+)?(class|interface|enum)\s+\w+', stripped):
                        outer_construct_found = True
                        outer_brace_count = line_open_braces
                        i += 1
                        continue
                
                if outer_construct_found:
                    # Update brace count
                    outer_brace_count += line_open_braces - line_close_braces
                    
                    # Look for inner constructs (only when we're inside the outer construct)
                    if outer_brace_count > 0:
                        construct_match = re.search(r'\s*(public|private|protected)?\s*(static\s+)?(class|interface|enum)\s+(\w+)', stripped)
                        if construct_match:
                            # Found an inner construct
                            construct = {
                                'type': construct_match.group(3),
                                'name': construct_match.group(4),
                                'modifiers': [],
                                'start_line': i,
                                'content_lines': []
                            }
                            
                            # Extract modifiers
                            if construct_match.group(1):
                                construct['modifiers'].append(construct_match.group(1))
                            if construct_match.group(2) and 'static' in construct_match.group(2):
                                construct['modifiers'].append('static')
                            
                            # Find the end of this construct
                            construct_lines = [line]
                            construct_brace_count = line_open_braces
                            
                            j = i + 1
                            while j < len(lines) and construct_brace_count > 0:
                                construct_line = lines[j]
                                construct_lines.append(construct_line)
                                
                                construct_brace_count += construct_line.count('{') - construct_line.count('}')
                                j += 1
                            
                            if construct_brace_count == 0:
                                construct['content_lines'] = construct_lines
                                construct['end_line'] = j - 1
                                constructs.append(construct)
                                
                                # Skip to the end of this construct
                                i = j
                                continue
                
                i += 1
            
            return constructs
            
        except Exception as e:
            logger.error(f"Error finding inner constructs in {file_path}: {e}")
            return []
    
    def create_standalone_file(self, original_file: Path, construct: Dict) -> Optional[Path]:
        """Create a standalone file for an inner construct"""
        try:
            original_content = original_file.read_text(encoding='utf-8')
            package = self.extract_package(original_content)
            imports = self.extract_imports(original_content)
            
            # Create new file content
            new_content_parts = []
            
            # Add package declaration
            if package:
                new_content_parts.append(f"package {package};")
                new_content_parts.append("")
            
            # Add imports (filter out unnecessary ones later)
            for import_stmt in imports:
                new_content_parts.append(import_stmt)
            
            if imports:
                new_content_parts.append("")
            
            # Add construct content with public visibility
            construct_content = '\n'.join(construct['content_lines'])
            
            # Make the class public if it isn't already
            if 'public' not in construct['modifiers']:
                static_part = 'static\\s+' if 'static' in construct['modifiers'] else ''
                pattern = f"((?:private|protected)\\s+)?({static_part})({construct['type']}\\s+{construct['name']})"
                replacement = f"public \\2\\3"
                construct_content = re.sub(pattern, replacement, construct_content, count=1)
            
            new_content_parts.append(construct_content)
            
            # Create new file
            new_file_path = original_file.parent / f"{construct['name']}.java"
            new_content = '\n'.join(new_content_parts)
            
            new_file_path.write_text(new_content, encoding='utf-8')
            
            # Validate the new file
            if self.validate_java_file(new_file_path):
                logger.info(f"Created standalone file: {new_file_path}")
                return new_file_path
            else:
                logger.error(f"Generated invalid Java file: {new_file_path}")
                new_file_path.unlink()  # Remove invalid file
                return None
                
        except Exception as e:
            logger.error(f"Error creating standalone file for {construct['name']}: {e}")
            return None
    
    def update_original_file(self, original_file: Path, constructs: List[Dict]) -> bool:
        """Update original file by removing inner constructs and adding imports"""
        try:
            content = original_file.read_text(encoding='utf-8')
            lines = content.split('\n')
            
            # Sort constructs by end_line in reverse order to remove from bottom up
            constructs_sorted = sorted(constructs, key=lambda x: x['end_line'], reverse=True)
            
            # Remove inner constructs
            for construct in constructs_sorted:
                start_line = construct['start_line']
                end_line = construct['end_line']
                
                # Remove the construct lines
                del lines[start_line:end_line + 1]
            
            # Add import statements for extracted classes
            package = self.extract_package(content)
            import_lines = []
            
            for line in lines:
                if line.strip().startswith('import '):
                    import_lines.append(line)
            
            # Add imports for extracted constructs
            for construct in constructs:
                if package:
                    import_stmt = f"import {package}.{construct['name']};"
                    if import_stmt not in [line.strip() for line in import_lines]:
                        import_lines.append(import_stmt)
            
            # Reconstruct file with imports
            new_lines = []
            package_added = False
            imports_added = False
            
            for line in lines:
                if line.strip().startswith('package ') and not package_added:
                    new_lines.append(line)
                    new_lines.append("")
                    package_added = True
                elif line.strip().startswith('import ') and not imports_added:
                    # Add all imports here
                    for import_line in import_lines:
                        new_lines.append(import_line)
                    new_lines.append("")
                    imports_added = True
                    # Skip original import lines
                elif not line.strip().startswith('import '):
                    new_lines.append(line)
            
            # Write updated content
            updated_content = '\n'.join(new_lines)
            
            # Clean up extra blank lines
            updated_content = re.sub(r'\n\s*\n\s*\n', '\n\n', updated_content)
            
            original_file.write_text(updated_content, encoding='utf-8')
            
            # Validate the updated file
            if self.validate_java_file(original_file):
                logger.info(f"Updated original file: {original_file}")
                return True
            else:
                logger.error(f"Updated file failed validation: {original_file}")
                return False
                
        except Exception as e:
            logger.error(f"Error updating original file {original_file}: {e}")
            return False
    
    def process_file(self, file_path: Path) -> Dict:
        """Process a single Java file"""
        result = {
            'file': str(file_path),
            'success': False,
            'constructs_extracted': 0,
            'new_files': [],
            'errors': []
        }
        
        try:
            logger.info(f"Processing file: {file_path}")
            
            # Validate original file
            if not self.validate_java_file(file_path):
                result['errors'].append("Original file failed validation")
                return result
            
            # Find inner constructs
            constructs = self.find_inner_constructs(file_path)
            
            if not constructs:
                logger.info(f"No inner constructs found in {file_path}")
                result['success'] = True
                return result
            
            logger.info(f"Found {len(constructs)} inner constructs in {file_path}")
            
            # Create standalone files
            extracted_constructs = []
            new_files = []
            
            for construct in constructs:
                new_file = self.create_standalone_file(file_path, construct)
                if new_file:
                    extracted_constructs.append(construct)
                    new_files.append(new_file)
                    self.rollback_actions.append(('delete_file', new_file))
                else:
                    result['errors'].append(f"Failed to extract {construct['name']}")
            
            if not extracted_constructs:
                result['errors'].append("No constructs could be extracted")
                return result
            
            # Create backup of original file
            backup_file = file_path.with_suffix('.java.backup')
            shutil.copy2(file_path, backup_file)
            self.rollback_actions.append(('restore_file', file_path, backup_file))
            
            # Update original file
            if self.update_original_file(file_path, extracted_constructs):
                result['success'] = True
                result['constructs_extracted'] = len(extracted_constructs)
                result['new_files'] = [str(f) for f in new_files]
                
                # Clean up backup
                backup_file.unlink()
                self.rollback_actions = [action for action in self.rollback_actions 
                                       if not (action[0] == 'restore_file' and action[1] == file_path)]
            else:
                result['errors'].append("Failed to update original file")
                
                # Restore original file
                shutil.copy2(backup_file, file_path)
                backup_file.unlink()
                
                # Remove created files
                for new_file in new_files:
                    if new_file.exists():
                        new_file.unlink()
            
        except Exception as e:
            logger.error(f"Unexpected error processing {file_path}: {e}")
            result['errors'].append(f"Unexpected error: {e}")
        
        return result
    
    def find_files_with_inner_constructs(self) -> List[Path]:
        """Find all Java files that contain inner constructs"""
        java_files = []
        
        for java_file in self.src_dir.rglob("*.java"):
            try:
                constructs = self.find_inner_constructs(java_file)
                if constructs:
                    java_files.append(java_file)
            except Exception as e:
                logger.warning(f"Could not analyze {java_file}: {e}")
        
        return java_files
    
    def extract_all(self, dry_run: bool = False) -> Dict:
        """Extract all inner constructs from all applicable files"""
        
        if not dry_run and not self.create_backup():
            return {'success': False, 'error': 'Failed to create backup'}
        
        files_to_process = self.find_files_with_inner_constructs()
        
        if not files_to_process:
            logger.info("No files with inner constructs found")
            return {'success': True, 'files_processed': 0}
        
        logger.info(f"Found {len(files_to_process)} files with inner constructs")
        
        if dry_run:
            logger.info("DRY RUN - No changes will be made")
            for file_path in files_to_process:
                constructs = self.find_inner_constructs(file_path)
                logger.info(f"  {file_path}: {len(constructs)} constructs")
                for construct in constructs:
                    logger.info(f"    - {construct['type']} {construct['name']}")
            
            return {
                'success': True, 
                'files_to_process': len(files_to_process),
                'dry_run': True
            }
        
        # Process files
        results = []
        successful = 0
        failed = 0
        
        for file_path in files_to_process:
            result = self.process_file(file_path)
            results.append(result)
            
            if result['success']:
                successful += 1
                self.successes.append(result)
            else:
                failed += 1
                self.errors.append(result)
        
        # Generate summary
        summary = {
            'success': failed == 0,
            'files_processed': len(files_to_process),
            'successful': successful,
            'failed': failed,
            'total_constructs_extracted': sum(r['constructs_extracted'] for r in results),
            'total_new_files': sum(len(r['new_files']) for r in results),
            'results': results
        }
        
        # Save report
        report_file = self.src_dir.parent / f"extraction_report_{datetime.now().strftime('%Y%m%d_%H%M%S')}.json"
        with open(report_file, 'w') as f:
            json.dump(summary, f, indent=2)
        
        logger.info(f"Extraction completed. Report saved to: {report_file}")
        logger.info(f"Success: {successful}/{len(files_to_process)} files")
        logger.info(f"Total constructs extracted: {summary['total_constructs_extracted']}")
        logger.info(f"Total new files created: {summary['total_new_files']}")
        
        if failed > 0:
            logger.warning(f"Failed to process {failed} files. Consider rollback if needed.")
        
        return summary

def main():
    """Main function for command line usage"""
    import argparse
    
    parser = argparse.ArgumentParser(description='Extract inner classes from Java files')
    parser.add_argument('src_dir', help='Source directory containing Java files')
    parser.add_argument('--backup-dir', help='Backup directory (default: auto-generated)')
    parser.add_argument('--dry-run', action='store_true', help='Perform dry run without making changes')
    parser.add_argument('--rollback', help='Rollback using specified backup directory')
    
    args = parser.parse_args()
    
    if args.rollback:
        extractor = JavaExtractor(args.src_dir, args.rollback)
        if extractor.rollback_changes():
            print("Rollback completed successfully")
        else:
            print("Rollback failed")
        return
    
    extractor = JavaExtractor(args.src_dir, args.backup_dir)
    result = extractor.extract_all(dry_run=args.dry_run)
    
    if result['success']:
        print(f"Extraction completed successfully!")
        if not args.dry_run:
            print(f"Files processed: {result['files_processed']}")
            print(f"Constructs extracted: {result.get('total_constructs_extracted', 0)}")
            print(f"New files created: {result.get('total_new_files', 0)}")
    else:
        print(f"Extraction completed with errors. Check the logs.")
        print(f"Consider rollback: python3 {__file__} --rollback {extractor.backup_dir} {args.src_dir}")

if __name__ == "__main__":
    main()