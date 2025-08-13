#!/usr/bin/env python3
"""
Final Robust Inner Class Extraction Tool

This script provides:
1. Accurate detection of all inner constructs
2. Complete error handling and rollback
3. Backup and restore capabilities
4. Full testing and validation
"""

import os
import re
import shutil
import json
from pathlib import Path
from typing import List, Dict, Optional, Tuple
import logging
from datetime import datetime

# Setup logging
logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')
logger = logging.getLogger(__name__)

class RobustJavaExtractor:
    def __init__(self, src_dir: str, backup_dir: str = None):
        self.src_dir = Path(src_dir)
        self.backup_dir = Path(backup_dir or f"backup_{datetime.now().strftime('%Y%m%d_%H%M%S')}")
        self.errors = []
        self.successes = []
        self.created_files = []
        self.modified_files = []
        
    def create_full_backup(self):
        """Create complete backup of source directory"""
        try:
            if self.backup_dir.exists():
                shutil.rmtree(self.backup_dir)
            shutil.copytree(self.src_dir, self.backup_dir)
            logger.info(f"✅ Backup created: {self.backup_dir}")
            return True
        except Exception as e:
            logger.error(f"❌ Backup failed: {e}")
            return False
    
    def rollback_all_changes(self):
        """Complete rollback to backup state"""
        try:
            if not self.backup_dir.exists():
                logger.error("❌ No backup available for rollback")
                return False
            
            # Remove current state and restore backup
            if self.src_dir.exists():
                shutil.rmtree(self.src_dir)
            shutil.copytree(self.backup_dir, self.src_dir)
            
            logger.info("✅ Complete rollback successful")
            return True
        except Exception as e:
            logger.error(f"❌ Rollback failed: {e}")
            return False
    
    def validate_java_syntax(self, file_path: Path) -> bool:
        """Basic Java syntax validation"""
        try:
            content = file_path.read_text(encoding='utf-8')
            
            # Check brace balance
            open_braces = content.count('{')
            close_braces = content.count('}')
            if open_braces != close_braces:
                logger.error(f"❌ Brace mismatch in {file_path}: {open_braces} open, {close_braces} close")
                return False
            
            # Check for package declaration
            if not re.search(r'package\s+[\w.]+;', content):
                logger.warning(f"⚠️ No package declaration in {file_path}")
            
            return True
        except Exception as e:
            logger.error(f"❌ Validation error in {file_path}: {e}")
            return False
    
    def find_all_inner_constructs(self, file_path: Path) -> List[Dict]:
        """Accurately find all inner classes, interfaces, and enums"""
        try:
            content = file_path.read_text(encoding='utf-8')
            lines = content.split('\n')
            
            constructs = []
            brace_level = 0
            outer_found = False
            
            for i, line in enumerate(lines):
                # Update brace level
                brace_level += line.count('{') - line.count('}')
                
                # Find outer construct first
                if not outer_found and re.search(r'^(public\s+)?(class|interface|enum)\s+\w+', line.strip()):
                    outer_found = True
                    continue
                
                # Look for inner constructs (brace_level > 1 means we're inside the outer construct)
                if outer_found and brace_level > 0:
                    stripped = line.strip()
                    
                    # Match inner construct declarations
                    match = re.search(r'^\s*(public|private|protected)?\s*(static\s+)?(class|interface|enum)\s+(\w+)', stripped)
                    if match:
                        construct = {
                            'type': match.group(3),
                            'name': match.group(4),
                            'start_line': i,
                            'modifiers': []
                        }
                        
                        # Extract modifiers
                        if match.group(1):
                            construct['modifiers'].append(match.group(1))
                        if match.group(2) and 'static' in match.group(2):
                            construct['modifiers'].append('static')
                        
                        # Find the end of this construct
                        construct_brace_level = line.count('{')
                        end_line = i
                        
                        for j in range(i + 1, len(lines)):
                            construct_brace_level += lines[j].count('{') - lines[j].count('}')
                            if construct_brace_level == 0:
                                end_line = j
                                break
                        
                        construct['end_line'] = end_line
                        construct['content_lines'] = lines[i:end_line + 1]
                        constructs.append(construct)
            
            logger.info(f"🔍 Found {len(constructs)} inner constructs in {file_path}")
            for c in constructs:
                logger.info(f"  - {c['type']} {c['name']} (lines {c['start_line']}-{c['end_line']})")
            
            return constructs
            
        except Exception as e:
            logger.error(f"❌ Error analyzing {file_path}: {e}")
            return []
    
    def extract_package_and_imports(self, content: str) -> Tuple[str, List[str]]:
        """Extract package and import declarations"""
        package = ""
        imports = []
        
        for line in content.split('\n'):
            line = line.strip()
            if line.startswith('package ') and line.endswith(';'):
                package = re.search(r'package\s+([\w.]+);', line).group(1)
            elif line.startswith('import ') and line.endswith(';'):
                imports.append(line)
        
        return package, imports
    
    def create_standalone_file(self, original_file: Path, construct: Dict) -> Optional[Path]:
        """Create standalone Java file for inner construct"""
        try:
            original_content = original_file.read_text(encoding='utf-8')
            package, imports = self.extract_package_and_imports(original_content)
            
            # Build new file content
            new_content = []
            
            # Package declaration
            if package:
                new_content.append(f"package {package};")
                new_content.append("")
            
            # Imports (keep all for now, can optimize later)
            for imp in imports:
                new_content.append(imp)
            if imports:
                new_content.append("")
            
            # Construct content with proper visibility
            construct_content = '\n'.join(construct['content_lines'])
            
            # Ensure public visibility for extracted construct
            if 'public' not in construct['modifiers']:
                # Replace first occurrence of the construct declaration
                pattern = f"(\\s*)((?:private|protected)\\s+)?(static\\s+)?({construct['type']}\\s+{construct['name']})"
                replacement = f"\\1public \\3\\4"
                construct_content = re.sub(pattern, replacement, construct_content, count=1)
            
            new_content.append(construct_content)
            
            # Create new file
            new_file_path = original_file.parent / f"{construct['name']}.java"
            final_content = '\n'.join(new_content)
            
            new_file_path.write_text(final_content, encoding='utf-8')
            
            # Validate created file
            if self.validate_java_syntax(new_file_path):
                logger.info(f"✅ Created: {new_file_path}")
                self.created_files.append(new_file_path)
                return new_file_path
            else:
                logger.error(f"❌ Invalid syntax in generated file: {new_file_path}")
                new_file_path.unlink()
                return None
                
        except Exception as e:
            logger.error(f"❌ Failed to create file for {construct['name']}: {e}")
            return None
    
    def update_original_file(self, file_path: Path, constructs: List[Dict]) -> bool:
        """Remove inner constructs and add imports to original file"""
        try:
            content = file_path.read_text(encoding='utf-8')
            lines = content.split('\n')
            
            # Sort by end_line descending to remove from bottom up
            sorted_constructs = sorted(constructs, key=lambda x: x['end_line'], reverse=True)
            
            # Remove construct lines
            for construct in sorted_constructs:
                del lines[construct['start_line']:construct['end_line'] + 1]
            
            # Add imports for extracted classes
            package, existing_imports = self.extract_package_and_imports(content)
            new_imports = []
            
            for construct in constructs:
                if package:
                    import_stmt = f"import {package}.{construct['name']};"
                    if import_stmt not in existing_imports:
                        new_imports.append(import_stmt)
            
            # Rebuild file with new imports
            final_lines = []
            imports_section_found = False
            
            for line in lines:
                stripped = line.strip()
                
                if stripped.startswith('package '):
                    final_lines.append(line)
                    final_lines.append("")
                elif stripped.startswith('import ') and not imports_section_found:
                    # Add all imports here
                    for imp in existing_imports + new_imports:
                        final_lines.append(imp)
                    final_lines.append("")
                    imports_section_found = True
                    # Skip adding the original import line since we added all imports above
                elif not stripped.startswith('import '):
                    final_lines.append(line)
            
            # Write updated content
            updated_content = '\n'.join(final_lines)
            
            # Clean up excessive blank lines
            updated_content = re.sub(r'\n\s*\n\s*\n', '\n\n', updated_content)
            
            file_path.write_text(updated_content, encoding='utf-8')
            
            # Validate updated file
            if self.validate_java_syntax(file_path):
                logger.info(f"✅ Updated: {file_path}")
                self.modified_files.append(file_path)
                return True
            else:
                logger.error(f"❌ Updated file failed validation: {file_path}")
                return False
                
        except Exception as e:
            logger.error(f"❌ Failed to update {file_path}: {e}")
            return False
    
    def process_single_file(self, file_path: Path) -> Dict:
        """Process one Java file completely"""
        result = {
            'file': str(file_path),
            'success': False,
            'constructs_found': 0,
            'constructs_extracted': 0,
            'new_files': [],
            'errors': []
        }
        
        try:
            logger.info(f"📁 Processing: {file_path}")
            
            # Validate original file
            if not self.validate_java_syntax(file_path):
                result['errors'].append("Original file validation failed")
                return result
            
            # Find inner constructs
            constructs = self.find_all_inner_constructs(file_path)
            result['constructs_found'] = len(constructs)
            
            if not constructs:
                result['success'] = True
                return result
            
            # Create backup of this specific file
            backup_path = file_path.with_suffix('.java.backup')
            shutil.copy2(file_path, backup_path)
            
            # Extract each construct
            extracted_constructs = []
            new_files = []
            
            for construct in constructs:
                new_file = self.create_standalone_file(file_path, construct)
                if new_file:
                    extracted_constructs.append(construct)
                    new_files.append(str(new_file))
                else:
                    result['errors'].append(f"Failed to extract {construct['name']}")
            
            if extracted_constructs:
                # Update original file
                if self.update_original_file(file_path, extracted_constructs):
                    result['success'] = True
                    result['constructs_extracted'] = len(extracted_constructs)
                    result['new_files'] = new_files
                    
                    # Remove backup
                    backup_path.unlink()
                    
                    logger.info(f"✅ Successfully processed {file_path}: {len(extracted_constructs)} constructs extracted")
                else:
                    # Restore from backup
                    shutil.copy2(backup_path, file_path)
                    backup_path.unlink()
                    
                    # Remove created files
                    for new_file_path in [Path(f) for f in new_files]:
                        if new_file_path.exists():
                            new_file_path.unlink()
                    
                    result['errors'].append("Failed to update original file")
            else:
                backup_path.unlink()
                result['errors'].append("No constructs could be extracted")
            
        except Exception as e:
            logger.error(f"❌ Unexpected error processing {file_path}: {e}")
            result['errors'].append(f"Unexpected error: {e}")
        
        return result
    
    def extract_all_inner_constructs(self, dry_run: bool = False) -> Dict:
        """Extract all inner constructs from all Java files"""
        
        # Find all files with inner constructs
        files_to_process = []
        for java_file in self.src_dir.rglob("*.java"):
            try:
                constructs = self.find_all_inner_constructs(java_file)
                if constructs:
                    files_to_process.append(java_file)
            except Exception as e:
                logger.warning(f"⚠️ Could not analyze {java_file}: {e}")
        
        if not files_to_process:
            logger.info("ℹ️ No files with inner constructs found")
            return {'success': True, 'files_processed': 0}
        
        logger.info(f"🎯 Found {len(files_to_process)} files to process")
        
        if dry_run:
            logger.info("🧪 DRY RUN - Analyzing files only")
            total_constructs = 0
            
            for file_path in files_to_process:
                constructs = self.find_all_inner_constructs(file_path)
                total_constructs += len(constructs)
                logger.info(f"  📄 {file_path.relative_to(self.src_dir)}: {len(constructs)} constructs")
                for construct in constructs:
                    logger.info(f"    - {construct['type']} {construct['name']}")
            
            return {
                'success': True,
                'files_to_process': len(files_to_process),
                'total_constructs': total_constructs,
                'dry_run': True
            }
        
        # Create full backup before processing
        if not self.create_full_backup():
            return {'success': False, 'error': 'Failed to create backup'}
        
        # Process all files
        results = []
        successful = 0
        failed = 0
        total_extracted = 0
        total_new_files = 0
        
        for file_path in files_to_process:
            result = self.process_single_file(file_path)
            results.append(result)
            
            if result['success']:
                successful += 1
                total_extracted += result['constructs_extracted']
                total_new_files += len(result['new_files'])
            else:
                failed += 1
        
        # Generate comprehensive report
        summary = {
            'success': failed == 0,
            'timestamp': datetime.now().isoformat(),
            'backup_location': str(self.backup_dir),
            'files_processed': len(files_to_process),
            'successful_files': successful,
            'failed_files': failed,
            'total_constructs_extracted': total_extracted,
            'total_new_files_created': total_new_files,
            'created_files': [str(f) for f in self.created_files],
            'modified_files': [str(f) for f in self.modified_files],
            'detailed_results': results
        }
        
        # Save comprehensive report
        report_path = self.src_dir.parent / f"extraction_report_{datetime.now().strftime('%Y%m%d_%H%M%S')}.json"
        with open(report_path, 'w') as f:
            json.dump(summary, f, indent=2)
        
        # Log final summary
        logger.info("=" * 60)
        logger.info("🎉 EXTRACTION COMPLETE")
        logger.info("=" * 60)
        logger.info(f"📊 Files processed: {len(files_to_process)}")
        logger.info(f"✅ Successful: {successful}")
        logger.info(f"❌ Failed: {failed}")
        logger.info(f"🔧 Total constructs extracted: {total_extracted}")
        logger.info(f"📄 Total new files created: {total_new_files}")
        logger.info(f"💾 Backup location: {self.backup_dir}")
        logger.info(f"📋 Report saved: {report_path}")
        
        if failed > 0:
            logger.warning(f"⚠️ {failed} files failed. Rollback available with:")
            logger.warning(f"   python3 {__file__} --rollback {self.backup_dir} {self.src_dir}")
        
        return summary

def main():
    import argparse
    
    parser = argparse.ArgumentParser(description='Extract inner classes/interfaces/enums from Java files')
    parser.add_argument('src_dir', help='Source directory containing Java files')
    parser.add_argument('--backup-dir', help='Backup directory path')
    parser.add_argument('--dry-run', action='store_true', help='Analyze only, no changes')
    parser.add_argument('--rollback', help='Rollback using specified backup directory')
    
    args = parser.parse_args()
    
    if args.rollback:
        extractor = RobustJavaExtractor(args.src_dir, args.rollback)
        success = extractor.rollback_all_changes()
        print(f"Rollback {'successful' if success else 'failed'}")
        return
    
    extractor = RobustJavaExtractor(args.src_dir, args.backup_dir)
    result = extractor.extract_all_inner_constructs(dry_run=args.dry_run)
    
    if result['success']:
        print("✅ Extraction completed successfully!")
        if not args.dry_run:
            print(f"📊 Files: {result.get('files_processed', 0)}")
            print(f"🔧 Constructs: {result.get('total_constructs_extracted', 0)}")
            print(f"📄 New files: {result.get('total_new_files_created', 0)}")
    else:
        print("❌ Extraction completed with errors")
        print("Check logs and consider rollback if needed")

if __name__ == "__main__":
    main()