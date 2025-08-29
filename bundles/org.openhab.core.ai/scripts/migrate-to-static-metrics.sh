#!/bin/bash

# Migration script for converting instance-based metrics pattern classes to static utility classes
# This script helps automate the migration process for the openHAB AI bundle

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
BACKUP_DIR="${PROJECT_ROOT}/.migration-backup"
LOG_FILE="${PROJECT_ROOT}/migration.log"

# Pattern classes to migrate
PATTERN_CLASSES=(
    "TaskLifecycleMetrics"
    "ValidationRuleMetrics"
    "SkillExecutionMetrics"
    "AuditEventMetrics"
    "ConfigurationOperationMetrics"
    "CardBuildingMetrics"
)

# Function to print colored output
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Function to create backup
create_backup() {
    print_status "Creating backup of current state..."
    if [ -d "$BACKUP_DIR" ]; then
        rm -rf "$BACKUP_DIR"
    fi
    mkdir -p "$BACKUP_DIR"
    
    # Backup source files
    find "$PROJECT_ROOT/src" -name "*.java" -exec cp --parents {} "$BACKUP_DIR" \;
    
    print_success "Backup created at $BACKUP_DIR"
}

# Function to find files that need migration
find_files_to_migrate() {
    print_status "Finding files that need migration..."
    
    local files_to_migrate=()
    
    for pattern_class in "${PATTERN_CLASSES[@]}"; do
        # Find files that import the pattern class
        local import_files=$(find "$PROJECT_ROOT/src" -name "*.java" -exec grep -l "import.*${pattern_class}" {} \; 2>/dev/null || true)
        
        # Find files that instantiate the pattern class
        local instantiate_files=$(find "$PROJECT_ROOT/src" -name "*.java" -exec grep -l "new ${pattern_class}" {} \; 2>/dev/null || true)
        
        # Find files that use @Reference for the pattern class
        local reference_files=$(find "$PROJECT_ROOT/src" -name "*.java" -exec grep -l "@Reference.*${pattern_class}" {} \; 2>/dev/null || true)
        
        files_to_migrate+=($import_files $instantiate_files $reference_files)
    done
    
    # Remove duplicates and sort
    printf '%s\n' "${files_to_migrate[@]}" | sort -u
}

# Function to analyze a file for migration needs
analyze_file() {
    local file="$1"
    local needs_migration=false
    local issues=()
    
    for pattern_class in "${PATTERN_CLASSES[@]}"; do
        # Check for @Reference
        if grep -q "@Reference.*${pattern_class}" "$file"; then
            needs_migration=true
            issues+=("Has @Reference for ${pattern_class}")
        fi
        
        # Check for instantiation
        if grep -q "new ${pattern_class}" "$file"; then
            needs_migration=true
            issues+=("Instantiates ${pattern_class}")
        fi
        
        # Check for instance method calls
        if grep -q "${pattern_class,,}\." "$file"; then
            needs_migration=true
            issues+=("Uses instance methods of ${pattern_class}")
        fi
    done
    
    if [ "$needs_migration" = true ]; then
        echo "File: $file"
        for issue in "${issues[@]}"; do
            echo "  - $issue"
        done
        echo
    fi
}

# Function to generate migration report
generate_migration_report() {
    print_status "Generating migration report..."
    
    local report_file="${PROJECT_ROOT}/migration-report.txt"
    
    echo "Migration Report - $(date)" > "$report_file"
    echo "=================================" >> "$report_file"
    echo >> "$report_file"
    
    echo "Files that need migration:" >> "$report_file"
    echo "-------------------------" >> "$report_file"
    
    local files_to_migrate=($(find_files_to_migrate))
    
    if [ ${#files_to_migrate[@]} -eq 0 ]; then
        echo "No files found that need migration." >> "$report_file"
        print_success "No files need migration!"
        return 0
    fi
    
    for file in "${files_to_migrate[@]}"; do
        analyze_file "$file" >> "$report_file"
    done
    
    echo "Total files to migrate: ${#files_to_migrate[@]}" >> "$report_file"
    
    print_success "Migration report generated: $report_file"
    print_warning "Please review the report before proceeding with migration"
}

# Function to perform automated migration
perform_migration() {
    print_status "Performing automated migration..."
    
    local files_to_migrate=($(find_files_to_migrate))
    
    if [ ${#files_to_migrate[@]} -eq 0 ]; then
        print_success "No files need migration!"
        return 0
    fi
    
    for file in "${files_to_migrate[@]}"; do
        print_status "Migrating file: $file"
        
        # Create a temporary file for the migration
        local temp_file="${file}.tmp"
        cp "$file" "$temp_file"
        
        # Perform migrations for each pattern class
        for pattern_class in "${PATTERN_CLASSES[@]}"; do
            # Remove @Reference lines
            sed -i "/@Reference.*${pattern_class}/d" "$temp_file"
            
            # Remove instance field declarations
            sed -i "/private.*${pattern_class}/d" "$temp_file"
            
            # Convert instance method calls to static method calls
            # This is a simplified conversion - manual review is recommended
            sed -i "s/${pattern_class,,}\.record/${pattern_class}.record/g" "$temp_file"
            
            # Add MetricsService parameter to static method calls
            # This is a simplified conversion - manual review is recommended
            sed -i "s/${pattern_class}\.record\([^(]*\)(/${pattern_class}.record\1(metricsService, /g" "$temp_file"
        done
        
        # Replace the original file with the migrated version
        mv "$temp_file" "$file"
        
        print_success "Migrated: $file"
    done
    
    print_success "Automated migration completed!"
    print_warning "Please review all changes and run tests to ensure correctness"
}

# Function to validate migration
validate_migration() {
    print_status "Validating migration..."
    
    local validation_errors=0
    
    for pattern_class in "${PATTERN_CLASSES[@]}"; do
        # Check for remaining @Reference
        local reference_files=$(find "$PROJECT_ROOT/src" -name "*.java" -exec grep -l "@Reference.*${pattern_class}" {} \; 2>/dev/null || true)
        if [ -n "$reference_files" ]; then
            print_error "Found remaining @Reference for ${pattern_class} in:"
            echo "$reference_files"
            ((validation_errors++))
        fi
        
        # Check for remaining instantiation
        local instantiate_files=$(find "$PROJECT_ROOT/src" -name "*.java" -exec grep -l "new ${pattern_class}" {} \; 2>/dev/null || true)
        if [ -n "$instantiate_files" ]; then
            print_error "Found remaining instantiation of ${pattern_class} in:"
            echo "$instantiate_files"
            ((validation_errors++))
        fi
    done
    
    if [ $validation_errors -eq 0 ]; then
        print_success "Migration validation passed!"
        return 0
    else
        print_error "Migration validation failed with $validation_errors errors"
        return 1
    fi
}

# Function to show usage
show_usage() {
    echo "Usage: $0 [OPTIONS]"
    echo
    echo "Options:"
    echo "  --analyze     Analyze files and generate migration report"
    echo "  --migrate     Perform automated migration (creates backup first)"
    echo "  --validate    Validate that migration was successful"
    echo "  --backup      Create backup of current state"
    echo "  --help        Show this help message"
    echo
    echo "Examples:"
    echo "  $0 --analyze                    # Analyze and generate report"
    echo "  $0 --migrate                    # Perform migration with backup"
    echo "  $0 --validate                   # Validate migration results"
}

# Main function
main() {
    case "${1:-}" in
        --analyze)
            generate_migration_report
            ;;
        --migrate)
            create_backup
            perform_migration
            validate_migration
            ;;
        --validate)
            validate_migration
            ;;
        --backup)
            create_backup
            ;;
        --help)
            show_usage
            ;;
        *)
            print_error "Invalid option: ${1:-}"
            show_usage
            exit 1
            ;;
    esac
}

# Run main function with all arguments
main "$@"
